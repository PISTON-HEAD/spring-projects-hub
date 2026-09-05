// answer-metrics.js — heuristic scoring of a spoken interview answer.
// Pure module (no electron, no network) so it is unit-testable. Turns an answer
// transcript (optionally with a measured spoken duration) into structured
// metrics, a 0-100 score, and concrete coaching tips. These are deliberately
// transparent heuristics meant to coach practice, not to grade with authority.

// Spoken filler words/phrases. Longer phrases are checked before bare words so
// "you know" isn't double-counted as "know".
const FILLER_PATTERNS = [
  /\byou know\b/gi,
  /\bkind of\b/gi,
  /\bsort of\b/gi,
  /\bi mean\b/gi,
  /\bum+\b/gi,
  /\buh+\b/gi,
  /\ber+\b/gi,
  /\bhmm+\b/gi,
  /\blike\b/gi,
  /\bbasically\b/gi,
  /\bactually\b/gi,
  /\bliterally\b/gi,
  /\bhonestly\b/gi
];

// STAR component signals. Each answer component is detected if any pattern hits.
const STAR_SIGNALS = {
  situation: [
    /\bwhen\b/i, /\bthere was\b/i, /\bwe were\b/i, /\bthe (situation|problem|issue|challenge)\b/i,
    /\bat (the time|my|our)\b/i, /\bfaced\b/i, /\bstruggl/i, /\bbackground\b/i
  ],
  task: [
    /\bmy (job|role|task|goal) was\b/i, /\bi was responsible\b/i, /\bi (had|needed) to\b/i,
    /\bwe (had|needed) to\b/i, /\bthe goal was\b/i, /\bwas asked to\b/i, /\bassigned\b/i
  ],
  action: [
    /\bi (built|created|led|implemented|designed|organized|decided|refactored|migrated|automated|proposed|drove|coordinated|developed|wrote|set up|introduced|negotiated)\b/i,
    /\bwe (built|created|implemented|designed|shipped|delivered)\b/i, /\bso i\b/i, /\bmy approach\b/i, /\bfirst,? i\b/i
  ],
  result: [
    /\bas a result\b/i, /\bresulted in\b/i, /\bwhich (led to|meant|improved|reduced|increased)\b/i,
    /\b(increased|reduced|improved|saved|grew|cut|boosted|decreased)\b/i, /\bin the end\b/i, /\bultimately\b/i,
    /\d+\s?%/, /\bby \d+/i
  ]
};

// Quantification: numbers, percentages, money, multipliers, time spans.
const QUANT_PATTERNS = [
  /\d+\s?%/, /\$\s?\d+/, /\b\d+x\b/i, /\b\d[\d,\.]*\s?(k|m|bn|thousand|million|billion)\b/i,
  /\b\d+\s?(users|customers|people|hours|days|weeks|months|requests|ms|seconds|minutes|engineers|teams|projects)\b/i,
  /\b\d{2,}\b/
];

const IDEAL_WPM_MIN = 110;
const IDEAL_WPM_MAX = 170;
const IDEAL_WORDS_MIN = 60;   // ~30s of speech
const IDEAL_WORDS_MAX = 320;  // ~2min of speech

function words(text) {
  const m = String(text || '').trim().match(/\b[\w']+\b/g);
  return m || [];
}

function countFillers(text) {
  const s = String(text || '');
  const hits = {};
  let total = 0;
  for (const re of FILLER_PATTERNS) {
    const found = s.match(re);
    if (found && found.length) {
      const key = re.source.replace(/\\b|\+|\(|\)|\?/g, '').replace(/\s+/g, ' ').trim();
      hits[key] = (hits[key] || 0) + found.length;
      total += found.length;
    }
  }
  return { total, hits };
}

function detectStar(text) {
  const s = String(text || '');
  const present = {};
  for (const [key, patterns] of Object.entries(STAR_SIGNALS)) {
    present[key] = patterns.some((re) => re.test(s));
  }
  const covered = Object.values(present).filter(Boolean).length;
  return { present, covered };
}

function hasQuantification(text) {
  const s = String(text || '');
  return QUANT_PATTERNS.some((re) => re.test(s));
}

function clamp(n, lo, hi) { return Math.max(lo, Math.min(hi, n)); }

// Words-per-minute from a measured duration; null when no duration is supplied.
function computeWpm(wordCount, durationMs) {
  if (!Number.isFinite(durationMs) || durationMs <= 0) return null;
  return Math.round((wordCount / durationMs) * 60000);
}

// Analyze one answer. Returns metrics + a 0-100 score + ordered coaching tips.
// category (optional) is the interview-context category, e.g. 'behavioral';
// STAR structure only counts toward the score for behavioral/situational answers.
function analyzeAnswer(text, { durationMs, category } = {}) {
  const w = words(text);
  const wordCount = w.length;
  const filler = countFillers(text);
  const star = detectStar(text);
  const quantified = hasQuantification(text);
  const wpm = computeWpm(wordCount, durationMs);
  const fillerRate = wordCount ? filler.total / wordCount : 0;
  const sentences = String(text || '').split(/[.!?]+/).filter((s) => s.trim().length).length;

  const wantsStar = category === 'behavioral' || category === 'situational' || category == null;

  // ---- Sub-scores ----
  const structure = wantsStar ? Math.round((star.covered / 4) * 35) : 30; // non-STAR modes get a neutral baseline
  const specificity = clamp((quantified ? 15 : 0) + (wordCount >= IDEAL_WORDS_MIN ? 10 : Math.round((wordCount / IDEAL_WORDS_MIN) * 10)), 0, 25);
  const fluency = clamp(Math.round(20 - fillerRate * 200), 0, 20); // ~10% fillers wipes out the fluency points
  let pace = 10;
  if (wpm != null) {
    if (wpm < IDEAL_WPM_MIN) pace = clamp(Math.round(10 - (IDEAL_WPM_MIN - wpm) / 6), 0, 10);
    else if (wpm > IDEAL_WPM_MAX) pace = clamp(Math.round(10 - (wpm - IDEAL_WPM_MAX) / 6), 0, 10);
  }
  let conciseness = 10;
  if (wordCount < IDEAL_WORDS_MIN) conciseness = clamp(Math.round((wordCount / IDEAL_WORDS_MIN) * 10), 0, 10);
  else if (wordCount > IDEAL_WORDS_MAX) conciseness = clamp(Math.round(10 - (wordCount - IDEAL_WORDS_MAX) / 30), 0, 10);

  const score = clamp(structure + specificity + fluency + pace + conciseness, 0, 100);

  // ---- Tips (ordered by impact) ----
  const tips = [];
  if (wantsStar && star.covered < 4) {
    const missing = Object.entries(star.present).filter(([, v]) => !v).map(([k]) => k);
    tips.push(`Strengthen STAR structure — missing: ${missing.join(', ')}.`);
  }
  if (!quantified) tips.push('Add a concrete number or metric to show impact (%, $, time saved, scale).');
  if (fillerRate > 0.03) tips.push(`Cut filler words — ${filler.total} detected (${(fillerRate * 100).toFixed(1)}% of words).`);
  if (wpm != null && wpm > IDEAL_WPM_MAX) tips.push(`Slow down — ${wpm} wpm is fast; aim for ${IDEAL_WPM_MIN}-${IDEAL_WPM_MAX}.`);
  if (wpm != null && wpm < IDEAL_WPM_MIN) tips.push(`Pick up the pace a little — ${wpm} wpm; aim for ${IDEAL_WPM_MIN}-${IDEAL_WPM_MAX}.`);
  if (wordCount < IDEAL_WORDS_MIN) tips.push('Answer is short — add specifics; aim for 30-90 seconds spoken.');
  if (wordCount > IDEAL_WORDS_MAX) tips.push('Answer is long — tighten it; aim for under ~2 minutes spoken.');
  if (!tips.length) tips.push('Strong answer — clear structure, specific, and well paced.');

  return {
    score,
    subScores: { structure, specificity, fluency, pace, conciseness },
    metrics: {
      wordCount,
      sentenceCount: sentences,
      fillerCount: filler.total,
      fillerRate: Number(fillerRate.toFixed(4)),
      fillerBreakdown: filler.hits,
      wpm,
      quantified,
      star: star.present,
      starCovered: star.covered
    },
    tips
  };
}

module.exports = {
  analyzeAnswer,
  countFillers,
  detectStar,
  hasQuantification,
  computeWpm,
  words,
  FILLER_PATTERNS,
  IDEAL_WPM_MIN,
  IDEAL_WPM_MAX
};
