// mock-interview.js — drives a practice interview where cue plays the
// interviewer. It plans a set of questions, tracks the candidate's spoken
// answers, and hands the recorded answers to report.js for scoring. Pure module
// (no electron, no network): the LLM-backed question generation lives in the
// main process, and the built-in QUESTION_BANK here is both the offline fallback
// and the source of the default plan.

const QUESTION_BANK = {
  experience: [
    'Tell me about yourself and your background.',
    'Walk me through your most recent role and what you were responsible for.',
    'What is a project you are most proud of, and what was your specific contribution?'
  ],
  behavioral: [
    'Tell me about a time you faced a significant challenge at work and how you handled it.',
    'Describe a situation where you disagreed with a teammate or manager. What did you do?',
    'Give me an example of a time you failed. What did you learn?',
    'Tell me about a time you had to deliver under a tight deadline.'
  ],
  motivation: [
    'Why are you interested in this role?',
    'Why are you looking to leave your current position?',
    'Where do you see yourself in five years?'
  ],
  situational: [
    'What would you do if you joined and found the codebase in poor shape?',
    'How would you prioritize work if everything was marked urgent?',
    'How would you handle a production outage in your first month?'
  ],
  technical: [
    'How would you design a URL shortener?',
    'Explain the difference between SQL and NoSQL databases and when you would use each.',
    'How would you improve the performance of a slow API endpoint?'
  ]
};

// A balanced default interview arc: warm up with experience, then behavioral,
// motivation, situational, and finish technical.
const DEFAULT_CATEGORIES = ['experience', 'behavioral', 'motivation', 'situational', 'technical'];

const DIFFICULTY = new Set(['easy', 'medium', 'hard']);

function pickCategories(categories) {
  const list = (Array.isArray(categories) ? categories : DEFAULT_CATEGORIES)
    .filter((c) => QUESTION_BANK[c]);
  return list.length ? list : DEFAULT_CATEGORIES;
}

// Build a fallback question plan from the built-in bank. Round-robins across the
// requested categories so a 5-question plan spans several areas rather than
// exhausting one. Deterministic (no randomness) so tests and the UI are stable.
function defaultQuestionPlan({ categories, count = 5 } = {}) {
  const cats = pickCategories(categories);
  const n = Math.max(1, Math.min(20, Math.floor(count) || 5));
  const cursors = Object.fromEntries(cats.map((c) => [c, 0]));
  const plan = [];
  let i = 0;
  while (plan.length < n) {
    const cat = cats[i % cats.length];
    const bank = QUESTION_BANK[cat];
    const pos = cursors[cat];
    if (pos < bank.length) {
      plan.push({ category: cat, question: bank[pos] });
      cursors[cat] += 1;
    }
    i += 1;
    // Stop if every category is exhausted before reaching count.
    if (i > cats.length * 20) break;
  }
  return plan;
}

function clip(text, limit) {
  if (!text) return '';
  const s = String(text).trim();
  return s.length <= limit ? s : s.slice(0, limit).trimEnd() + '…';
}

// System prompt that turns the model into an interviewer that asks ONE question
// at a time. Used by the main process when generating questions with the LLM.
function buildInterviewerSystem({ jobTitle, jobDescription, resumeText, difficulty = 'medium', categories } = {}) {
  const diff = DIFFICULTY.has(difficulty) ? difficulty : 'medium';
  const cats = pickCategories(categories);
  const parts = [];
  parts.push(
    'You are an experienced technical interviewer conducting a realistic mock interview. ' +
    'Ask exactly ONE question at a time, then wait. Do not answer for the candidate, ' +
    'do not preface with commentary, and do not number the questions. ' +
    `Keep questions at ${diff} difficulty.`
  );
  parts.push('Cover these areas over the course of the interview: ' + cats.join(', ') + '.');
  if (jobTitle) parts.push('Target role: ' + clip(jobTitle, 120) + '.');
  if (jobDescription) parts.push('Job description:\n' + clip(jobDescription, 800));
  if (resumeText) parts.push('Candidate background (ask questions grounded in this):\n' + clip(resumeText, 1200));
  parts.push('Ask only the question itself, as a single sentence or two.');
  return parts.join('\n\n');
}

// Stateful driver for one practice session. Records answers on the current
// question and exposes progress + a results array shaped for report.js.
function createMockSession({ questions = [] } = {}) {
  const items = (questions || [])
    .filter((q) => q && q.question)
    .map((q) => ({ category: q.category || null, question: String(q.question), answer: null, durationMs: null }));
  let idx = 0;

  function current() {
    return idx < items.length ? items[idx] : null;
  }

  return {
    total: items.length,
    currentIndex() { return idx; },
    current,
    // Record the candidate's answer on the current question. Returns false when
    // the session is already finished. Does not auto-advance so the UI can show
    // feedback first.
    submitAnswer(text, durationMs) {
      const item = current();
      if (!item) return false;
      item.answer = String(text || '');
      item.durationMs = Number.isFinite(durationMs) ? durationMs : null;
      return true;
    },
    // Advance to the next question; returns the new current question or null.
    next() {
      if (idx < items.length) idx += 1;
      return current();
    },
    isDone() { return idx >= items.length; },
    remaining() { return Math.max(0, items.length - idx); },
    answeredCount() { return items.filter((i) => i.answer != null).length; },
    progress() {
      return { index: Math.min(idx, items.length), total: items.length, answered: items.filter((i) => i.answer != null).length };
    },
    // Results for report.buildSessionReport({ answers }).
    getResults() {
      return items
        .filter((i) => i.answer != null)
        .map((i) => ({ question: i.question, answer: i.answer, durationMs: i.durationMs, category: i.category }));
    }
  };
}

module.exports = {
  QUESTION_BANK,
  DEFAULT_CATEGORIES,
  defaultQuestionPlan,
  buildInterviewerSystem,
  createMockSession
};
