"use strict";

// ── Application state ────────────────────────────────────────────────────────
const state = {
    token: null,
    mode: null,        // "ORGANIZATION" | "INDIVIDUAL"
    label: null,       // display label for the badge
    role: null,        // "ORG_ADMIN" | "ORG_MEMBER" | "INDIVIDUAL" | "ADMIN"
    sessionId: null,   // conversation id for follow-up questions
};

const STORE_KEY = "documind.session";

// ── Tiny DOM helpers ─────────────────────────────────────────────────────────
const $ = (id) => document.getElementById(id);
const show = (id) => $(id).classList.remove("hidden");
const hide = (id) => $(id).classList.add("hidden");

// ── Router (hash-based, enables browser Back/Forward) ────────────────────
const VIEWS = ["landing", "login", "dashboard"];
let activeView = null;

function hashToView(hash) {
    if (hash === "#/login") return "login";
    if (hash === "#/app") return "dashboard";
    return "landing";
}
function viewToHash(view) {
    return view === "login" ? "#/login" : view === "dashboard" ? "#/app" : "#/";
}
function showOnly(view) {
    VIEWS.forEach((v) => hide("view-" + v));
    show("view-" + view);
}
function navigate(view) {
    const hash = viewToHash(view);
    if (location.hash === hash) render();   // same route — re-render manually
    else location.hash = hash;              // different route — hashchange fires render
}
function render() {
    let view = hashToView(location.hash);
    // Guards: dashboard needs a session; login is pointless once signed in.
    if (view === "dashboard" && !state.token) view = "landing";
    if (view === "login" && state.token) view = "dashboard";

    const wanted = viewToHash(view);
    if (location.hash !== wanted) history.replaceState(null, "", wanted);

    showOnly(view);
    updateBackButton(view);
    if (view !== activeView) {
        activeView = view;
        onEnter(view);
    }
}
function onEnter(view) {
    if (view === "dashboard") {
        const status = $("uploadStatus");
        status.className = "upload-status hidden"; // drop any status left over from a prior session
        status.textContent = "";
        applyAuthUI();
        renderDocuments([]); // clear the previous scope's list instantly (no cross-scope flash)
        refreshDocuments();  // then load this scope's own documents
    } else {
        $("sessionBadge").classList.add("hidden");
        if (view === "login") {
            hide("loginError");
            $("password").value = "";
            $("username").focus();
        }
    }
}
function updateBackButton(view) {
    $("navBack").classList.toggle("hidden", view === "landing");
}
function applyAuthUI() {
    const isOrg = state.mode === "ORGANIZATION";
    $("sessionBadge").classList.remove("hidden");
    $("sessionLabel").textContent = (isOrg ? "🏢 " : "👤 ") + state.label;

    const chip = $("roleChip");
    if (isOrg) {
        const isAdmin = canUpload();
        chip.textContent = isAdmin ? "Admin" : "Member";
        chip.className = "role-chip " + (isAdmin ? "admin" : "member");
    } else {
        chip.className = "role-chip hidden";
    }

    $("docsTitle").textContent = isOrg ? "Organization Library" : "Your Documents";
    $("scopePill").textContent = isOrg ? "Organization" : "Private";

    const allowed = canUpload();
    $("uploadZone").classList.toggle("hidden", !allowed);
    $("uploadLocked").classList.toggle("hidden", allowed);
}

function canUpload() {
    return state.mode === "INDIVIDUAL" || state.role === "ORG_ADMIN" || state.role === "ADMIN";
}
function handleSessionExpired() {
    state.token = null;
    state.mode = null;
    state.label = null;
    state.sessionId = null;
    clearSession();
    navigate("landing");
    toast("Session expired — please sign in again.", true);
}

let toastTimer;
function toast(message, isError) {
    const t = $("toast");
    t.textContent = message;
    t.classList.toggle("err", !!isError);
    t.classList.remove("hidden");
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => t.classList.add("hidden"), 3200);
}

// ── API wrapper ──────────────────────────────────────────────────────────────
async function api(path, { method = "GET", body, headers = {}, isForm = false } = {}) {
    const opts = { method, headers: { ...headers } };
    if (state.token) opts.headers["Authorization"] = "Bearer " + state.token;
    if (body !== undefined) {
        if (isForm) {
            opts.body = body; // FormData — let the browser set Content-Type
        } else {
            opts.headers["Content-Type"] = "application/json";
            opts.body = JSON.stringify(body);
        }
    }
    const res = await fetch(path, opts);
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { data = { raw: text }; }
    if (!res.ok) {
        // A protected call failing auth means the token is gone/expired — reset cleanly.
        if ((res.status === 401 || res.status === 403) && state.token && path.startsWith("/api/")) {
            handleSessionExpired();
            throw new Error("Your session has expired. Please sign in again.");
        }
        const msg = (data && (data.error || data.message)) || `Request failed (${res.status})`;
        throw new Error(msg);
    }
    return data;
}

// ── Session persistence ──────────────────────────────────────────────────────
function saveSession() {
    sessionStorage.setItem(STORE_KEY, JSON.stringify({
        token: state.token, mode: state.mode, label: state.label, role: state.role,
    }));
}
function clearSession() {
    sessionStorage.removeItem(STORE_KEY);
}
function restoreSession() {
    const raw = sessionStorage.getItem(STORE_KEY);
    if (!raw) return false;
    try {
        const s = JSON.parse(raw);
        if (!s.token) return false;
        state.token = s.token;
        state.mode = s.mode;
        state.label = s.label;
        state.role = s.role;
        return true;
    } catch { return false; }
}

// ── Auth flows ───────────────────────────────────────────────────────────────
async function loginOrg(username, password) {
    const data = await api("/auth/login", { method: "POST", body: { username, password } });
    state.token = data.token;
    state.mode = "ORGANIZATION";
    state.label = data.orgName || username;
    state.role = data.role || "ORG_MEMBER";
    saveSession();
}

async function startIndividual() {
    const data = await api("/auth/individual-session", { method: "POST" });
    state.token = data.token;
    state.mode = "INDIVIDUAL";
    state.label = "Individual workspace";
    state.role = "INDIVIDUAL";
    saveSession();
}

function logout() {
    state.token = null;
    state.mode = null;
    state.label = null;
    state.role = null;
    state.sessionId = null;
    clearSession();
    navigate("landing");
}

// ── Documents ────────────────────────────────────────────────────────────────
async function refreshDocuments() {
    let docs = [];
    try {
        docs = await api("/api/documents");
    } catch (e) {
        if (state.token) toast(e.message, true); // suppressed when the session was just cleared
        return;
    }
    renderDocuments(docs);
}

function renderDocuments(docs) {
    const list = $("docList");
    const select = $("docScopeSelect");
    list.innerHTML = "";

    // Rebuild the scope selector, preserving the "All documents" option.
    select.innerHTML = "";
    const allOpt = document.createElement("option");
    allOpt.value = "ALL";
    allOpt.textContent = "All documents";
    select.appendChild(allOpt);

    if (!docs.length) {
        $("docEmpty").textContent = canUpload()
            ? "No documents yet. Upload one to get started."
            : "No documents available in this library yet.";
        show("docEmpty");
    } else {
        hide("docEmpty");
        docs.forEach((doc) => {
            const li = document.createElement("li");
            li.className = "doc-item";

            const icon = document.createElement("span");
            icon.className = "doc-icon";
            icon.textContent = "📄";

            const meta = document.createElement("div");
            meta.className = "doc-meta";
            const name = document.createElement("div");
            name.className = "doc-name";
            name.textContent = doc.filename;
            name.title = doc.filename;
            const sub = document.createElement("div");
            sub.className = "doc-sub";
            sub.textContent = doc.chunks + " chunk" + (doc.chunks === 1 ? "" : "s");
            meta.appendChild(name);
            meta.appendChild(sub);

            li.appendChild(icon);
            li.appendChild(meta);
            if (canUpload()) {
                const del = document.createElement("button");
                del.className = "doc-del";
                del.title = "Remove document";
                del.setAttribute("aria-label", "Remove " + doc.filename);
                del.textContent = "🗑";
                del.addEventListener("click", (e) => {
                    e.stopPropagation();
                    removeDocument(doc.documentId, doc.filename);
                });
                li.appendChild(del);
            }
            list.appendChild(li);

            const opt = document.createElement("option");
            opt.value = doc.documentId;
            opt.textContent = doc.filename;
            select.appendChild(opt);
        });
    }

    const hasDocs = docs.length > 0;
    $("questionInput").disabled = !hasDocs;
    $("sendBtn").disabled = !hasDocs;
    $("questionInput").placeholder = hasDocs
        ? "Ask a question…"
        : (canUpload() ? "Upload a document first…" : "No documents available yet…");
}

async function uploadFile(file) {
    if (!file) return;
    const status = $("uploadStatus");
    status.className = "upload-status busy";
    status.textContent = `Uploading & indexing “${file.name}”…`;
    show("uploadStatus");

    const form = new FormData();
    form.append("file", file);
    try {
        const res = await api("/api/documents/upload", { method: "POST", body: form, isForm: true });
        status.className = "upload-status ok";
        status.textContent = `Indexed “${res.filename}” (${res.totalChunks} chunks).`;
        await refreshDocuments();
    } catch (e) {
        status.className = "upload-status err";
        status.textContent = e.message;
    }
}

// ── Confirm + remove ─────────────────────────────────────────────────────────
function confirmDialog(message, okLabel) {
    return new Promise((resolve) => {
        const overlay = $("confirmModal");
        $("confirmMessage").textContent = message;
        $("confirmOk").textContent = okLabel || "Remove";
        overlay.classList.remove("hidden");
        $("confirmOk").focus();

        function close(result) {
            overlay.classList.add("hidden");
            $("confirmOk").onclick = null;
            $("confirmCancel").onclick = null;
            overlay.onclick = null;
            document.removeEventListener("keydown", onKey);
            resolve(result);
        }
        function onKey(e) {
            if (e.key === "Escape") close(false);
            else if (e.key === "Enter") close(true);
        }
        $("confirmOk").onclick = () => close(true);
        $("confirmCancel").onclick = () => close(false);
        overlay.onclick = (e) => { if (e.target === overlay) close(false); };
        document.addEventListener("keydown", onKey);
    });
}

async function removeDocument(documentId, filename) {
    const confirmed = await confirmDialog(
        `Are you sure you want to remove “${filename}”? This permanently deletes it from the library.`,
        "Remove");
    if (!confirmed) return;
    try {
        await api(`/api/documents/${encodeURIComponent(documentId)}`, { method: "DELETE" });
        toast(`Removed “${filename}”.`);
        await refreshDocuments();
    } catch (e) {
        toast(e.message, true);
    }
}

// ── Chat ─────────────────────────────────────────────────────────────────────
function resetChat() {
    state.sessionId = null;
    const box = $("chatMessages");
    box.innerHTML = "";
    const ph = document.createElement("div");
    ph.className = "chat-placeholder";
    ph.id = "chatPlaceholder";
    ph.innerHTML = '<span class="placeholder-icon">💬</span>';
    const p = document.createElement("p");
    p.textContent = canUpload()
        ? "Upload a document, then ask a question about it."
        : "Ask a question about your organization's documents.";
    ph.appendChild(p);
    box.appendChild(ph);
}

function removePlaceholder() {
    const ph = $("chatPlaceholder");
    if (ph) ph.remove();
}

function addMessage(role, text) {
    removePlaceholder();
    const box = $("chatMessages");
    const msg = document.createElement("div");
    msg.className = "msg " + role;

    const who = document.createElement("div");
    who.className = "who";
    who.textContent = role === "user" ? "You" : "Assistant";

    const bubble = document.createElement("div");
    bubble.className = "bubble";
    bubble.textContent = text;

    msg.appendChild(who);
    msg.appendChild(bubble);
    box.appendChild(msg);
    box.scrollTop = box.scrollHeight;
    return msg;
}

function addTyping() {
    removePlaceholder();
    const box = $("chatMessages");
    const msg = document.createElement("div");
    msg.className = "msg assistant";
    msg.innerHTML =
        '<div class="who">Assistant</div>' +
        '<div class="bubble"><span class="typing"><span></span><span></span><span></span></span></div>';
    box.appendChild(msg);
    box.scrollTop = box.scrollHeight;
    return msg;
}

function fillAnswer(node, answer, chunks) {
    node.querySelector(".bubble").textContent = answer;
    if (chunks && chunks.length) {
        const details = document.createElement("details");
        details.className = "sources";
        const summary = document.createElement("summary");
        summary.textContent = `Sources (${chunks.length})`;
        const ol = document.createElement("ol");
        chunks.forEach((c) => {
            const li = document.createElement("li");
            li.textContent = c;
            ol.appendChild(li);
        });
        details.appendChild(summary);
        details.appendChild(ol);
        node.appendChild(details);
    }
    $("chatMessages").scrollTop = $("chatMessages").scrollHeight;
}

async function askQuestion(question) {
    addMessage("user", question);
    const typingNode = addTyping();
    $("sendBtn").disabled = true;

    const scope = $("docScopeSelect").value;
    const headers = state.sessionId ? { "X-Session-Id": state.sessionId } : {};

    try {
        let res;
        if (scope === "ALL") {
            res = await api("/api/query", { method: "POST", body: { question }, headers });
        } else {
            res = await api(`/api/documents/${encodeURIComponent(scope)}/query`,
                { method: "POST", body: { question }, headers });
        }
        state.sessionId = res.sessionId; // keep the thread for follow-ups
        fillAnswer(typingNode, res.answer, res.relevantChunks);
    } catch (e) {
        typingNode.querySelector(".bubble").textContent = "⚠️ " + e.message;
    } finally {
        $("sendBtn").disabled = false;
        $("questionInput").focus();
    }
}

// ── Wire up events ───────────────────────────────────────────────────────────
function init() {
    // Landing choices (re-entering a mode you're already in just reopens the dashboard)
    $("chooseOrg").addEventListener("click", () => {
        if (state.token && state.mode === "ORGANIZATION") { navigate("dashboard"); return; }
        navigate("login");
    });
    $("chooseIndividual").addEventListener("click", async () => {
        if (state.token && state.mode === "INDIVIDUAL") { navigate("dashboard"); return; }
        const btn = $("chooseIndividual");
        btn.disabled = true;
        try {
            await startIndividual();
            resetChat();
            navigate("dashboard");
        } catch (e) {
            toast(e.message, true);
        } finally {
            btn.disabled = false;
        }
    });

    // In-app back / home navigation
    document.querySelectorAll("[data-back]").forEach((el) =>
        el.addEventListener("click", () => navigate(el.dataset.back)));
    $("brandHome").addEventListener("click", () => navigate("landing"));
    $("navBack").addEventListener("click", () => history.back());

    // Org login
    $("loginForm").addEventListener("submit", async (e) => {
        e.preventDefault();
        hide("loginError");
        const btn = $("loginSubmit");
        btn.disabled = true;
        btn.textContent = "Signing in…";
        try {
            await loginOrg($("username").value.trim(), $("password").value);
            resetChat();
            navigate("dashboard");
        } catch (err) {
            const box = $("loginError");
            box.textContent = err.message;
            box.classList.remove("hidden");
        } finally {
            btn.disabled = false;
            btn.textContent = "Sign in";
        }
    });

    // Logout
    $("logoutBtn").addEventListener("click", logout);

    // Upload interactions
    $("browseBtn").addEventListener("click", () => $("fileInput").click());
    $("uploadZone").addEventListener("click", (e) => {
        if (e.target.id !== "browseBtn") $("fileInput").click();
    });
    $("fileInput").addEventListener("change", (e) => {
        if (e.target.files[0]) uploadFile(e.target.files[0]);
        e.target.value = "";
    });
    const zone = $("uploadZone");
    ["dragenter", "dragover"].forEach((ev) =>
        zone.addEventListener(ev, (e) => { e.preventDefault(); zone.classList.add("drag"); }));
    ["dragleave", "drop"].forEach((ev) =>
        zone.addEventListener(ev, (e) => { e.preventDefault(); zone.classList.remove("drag"); }));
    zone.addEventListener("drop", (e) => {
        const file = e.dataTransfer.files[0];
        if (file) uploadFile(file);
    });

    // Chat
    $("newChatBtn").addEventListener("click", () => {
        resetChat();
        toast("Started a new conversation.");
    });
    $("chatForm").addEventListener("submit", (e) => {
        e.preventDefault();
        const input = $("questionInput");
        const q = input.value.trim();
        if (!q) return;
        input.value = "";
        askQuestion(q);
    });

    // Browser Back/Forward drives navigation
    window.addEventListener("hashchange", render);

    // Resume an existing session on refresh, otherwise route from the current URL
    if (restoreSession()) {
        resetChat();
        navigate("dashboard");
    } else {
        render();
    }
}

document.addEventListener("DOMContentLoaded", init);
