// profiles.js — pure helpers for named settings profiles (e.g. one per company
// or interview). A profile snapshots the content fields a candidate tailors per
// role — resume, job description, prep answers, language, provider — but never
// API keys, which stay global. Pure (no electron/fs) so store.js can wrap these
// and the logic stays unit-testable.

// Fields a profile captures. Deliberately excludes apiKeys/azureEndpoint and
// window geometry: credentials and layout are global, not per-interview.
const PROFILE_FIELDS = [
  'provider',
  'responseLanguage',
  'resumeText',
  'jobDescription',
  'starStories',
  'whyCompany',
  'whyLeaving',
  'workStyle',
  'salaryTarget',
  'questionsToAsk',
  'aiRules'
];

const MAX_NAME_LEN = 60;

function sanitizeName(name) {
  const n = String(name == null ? '' : name).trim().slice(0, MAX_NAME_LEN);
  if (!n) throw new Error('Profile name cannot be empty.');
  return n;
}

// Pull the profile-relevant fields out of a settings object. Captures every
// field (defaulting missing ones to '') so a profile is a complete snapshot:
// loading it clears fields that weren't set when it was saved.
function extractProfile(settings = {}) {
  const out = {};
  for (const key of PROFILE_FIELDS) {
    out[key] = settings[key] !== undefined ? settings[key] : '';
  }
  return out;
}

// Overlay a saved profile's fields onto a settings object. Returns a new object;
// does not mutate the input.
function applyProfile(settings = {}, profile = {}) {
  const next = { ...settings };
  for (const key of PROFILE_FIELDS) {
    if (profile[key] !== undefined) next[key] = profile[key];
  }
  return next;
}

// Short human label for a profile, preferring the job title/first JD line.
function deriveTitle(fields = {}) {
  const jd = String(fields.jobDescription || '').trim();
  if (jd) return jd.split('\n')[0].slice(0, 80);
  return '';
}

// Save the current settings as a named profile. Returns a new data object with
// data.profiles[name] set; input is not mutated.
function saveProfile(data = {}, name, now = Date.now()) {
  const clean = sanitizeName(name);
  const profiles = { ...(data.profiles || {}) };
  profiles[clean] = { savedAt: now, fields: extractProfile(data) };
  return { ...data, profiles, activeProfile: clean };
}

// Apply a saved profile's fields onto the settings and mark it active.
function loadProfile(data = {}, name) {
  const clean = sanitizeName(name);
  const profiles = data.profiles || {};
  const entry = profiles[clean];
  if (!entry) throw new Error(`Profile "${clean}" not found.`);
  const applied = applyProfile(data, entry.fields || {});
  applied.profiles = profiles;
  applied.activeProfile = clean;
  return applied;
}

// Remove a saved profile. Clears activeProfile if it pointed at the removed one.
function deleteProfile(data = {}, name) {
  const clean = sanitizeName(name);
  const profiles = { ...(data.profiles || {}) };
  delete profiles[clean];
  const next = { ...data, profiles };
  if (next.activeProfile === clean) next.activeProfile = '';
  return next;
}

// List profiles for the UI, sorted by name. Never exposes credentials.
function listProfiles(data = {}) {
  const profiles = data.profiles || {};
  return Object.keys(profiles)
    .sort((a, b) => a.localeCompare(b))
    .map((name) => ({
      name,
      savedAt: profiles[name].savedAt || null,
      title: deriveTitle(profiles[name].fields || {}),
      active: data.activeProfile === name
    }));
}

module.exports = {
  PROFILE_FIELDS,
  extractProfile,
  applyProfile,
  deriveTitle,
  saveProfile,
  loadProfile,
  deleteProfile,
  listProfiles,
  MAX_NAME_LEN
};
