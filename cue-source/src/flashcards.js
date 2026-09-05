// flashcards.js — generate interview practice flashcards (question + model
// answer) from a job description and resume. Pure module (no electron/network):
// the main process runs buildFlashcardSystem through the LLM, then parseFlashcards
// turns the reply into structured cards. fallbackFlashcards covers the offline
// case using the built-in question bank.

const { QUESTION_BANK, DEFAULT_CATEGORIES } = require('./mock-interview');

function clip(text, limit) {
  if (!text) return '';
  const s = String(text).trim();
  return s.length <= limit ? s : s.slice(0, limit).trimEnd() + '…';
}

// System prompt asking for a parseable set of Q&A cards. Requests JSON but the
// parser tolerates the common "Q:/A:" fallback shape too.
function buildFlashcardSystem({ jobDescription, resumeText, count = 8, categories } = {}) {
  const n = Math.max(1, Math.min(30, Math.floor(count) || 8));
  const cats = (Array.isArray(categories) && categories.length ? categories : DEFAULT_CATEGORIES).join(', ');
  const parts = [];
  parts.push(
    `You are an interview coach. Produce exactly ${n} practice flashcards for the candidate. ` +
    'Each card has a likely interview "question" and a strong, concise model "answer" written in first person. ' +
    `Cover these areas where relevant: ${cats}.`
  );
  parts.push(
    'Return ONLY a JSON array, no prose, no code fence, in this shape:\n' +
    '[{"category":"behavioral","question":"...","answer":"..."}]'
  );
  if (jobDescription) parts.push('Job description:\n' + clip(jobDescription, 900));
  if (resumeText) parts.push('Candidate background (ground answers in this):\n' + clip(resumeText, 1200));
  return parts.join('\n\n');
}

// Strip a leading/trailing markdown code fence if the model wrapped its output.
function stripFence(text) {
  return String(text || '')
    .replace(/^\s*```(?:json)?\s*/i, '')
    .replace(/\s*```\s*$/i, '')
    .trim();
}

function tryParseJson(text) {
  const body = stripFence(text);
  const start = body.indexOf('[');
  const end = body.lastIndexOf(']');
  if (start === -1 || end === -1 || end <= start) return null;
  try {
    const arr = JSON.parse(body.slice(start, end + 1));
    if (!Array.isArray(arr)) return null;
    return arr;
  } catch {
    return null;
  }
}

// Fallback parser for a "Q: ...\nA: ..." style reply, blocks optionally
// separated by blank lines or "---".
function parseQA(text) {
  const cards = [];
  const blocks = String(text || '').split(/\n\s*(?:---+|\*\*\*+)\s*\n|\n{2,}/);
  for (const block of blocks) {
    const qMatch = /(?:^|\n)\s*(?:Q|Question)\s*[:.\)]\s*(.+?)(?=\n\s*(?:A|Answer)\s*[:.\)]|$)/is.exec(block);
    const aMatch = /(?:^|\n)\s*(?:A|Answer)\s*[:.\)]\s*([\s\S]+)$/i.exec(block);
    if (qMatch) {
      cards.push({
        category: null,
        question: qMatch[1].trim().replace(/\s+/g, ' '),
        answer: aMatch ? aMatch[1].trim() : ''
      });
    }
  }
  return cards;
}

// Parse an LLM reply into normalized flashcards. Tries JSON first, then Q/A.
function parseFlashcards(text) {
  const json = tryParseJson(text);
  const raw = json || parseQA(text);
  return raw
    .map((c) => ({
      category: c && c.category ? String(c.category) : null,
      question: c && c.question ? String(c.question).trim() : '',
      answer: c && c.answer ? String(c.answer).trim() : ''
    }))
    .filter((c) => c.question);
}

// Offline flashcards: questions from the built-in bank with an empty answer the
// candidate fills in themselves.
function fallbackFlashcards({ categories, count = 8 } = {}) {
  const cats = (Array.isArray(categories) && categories.length ? categories : DEFAULT_CATEGORIES)
    .filter((c) => QUESTION_BANK[c]);
  const use = cats.length ? cats : DEFAULT_CATEGORIES;
  const n = Math.max(1, Math.min(30, Math.floor(count) || 8));
  const cards = [];
  const cursors = Object.fromEntries(use.map((c) => [c, 0]));
  let i = 0;
  while (cards.length < n && i < use.length * 30) {
    const cat = use[i % use.length];
    const bank = QUESTION_BANK[cat];
    if (cursors[cat] < bank.length) {
      cards.push({ category: cat, question: bank[cursors[cat]], answer: '' });
      cursors[cat] += 1;
    }
    i += 1;
  }
  return cards;
}

module.exports = {
  buildFlashcardSystem,
  parseFlashcards,
  fallbackFlashcards,
  stripFence
};
