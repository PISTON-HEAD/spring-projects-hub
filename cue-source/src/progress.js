// progress.js — turns finished practice sessions into compact history records
// and computes improvement trends over time. Pure module (no electron/network);
// store.js persists the returned records and the UI renders the trends.

const { summarizeAnswers } = require('./report');

// Compact, storable summary of one session. Keeps only aggregates (no transcript
// or answer text) so the history file stays small and private.
function summarizeSession({ startedAt = Date.now(), endedAt = null, answers = [], usage = null } = {}) {
  const s = summarizeAnswers(answers);
  return {
    date: startedAt,
    endedAt,
    questions: s.count,
    avgScore: s.avgScore != null ? Math.round(s.avgScore) : null,
    avgWpm: s.avgWpm != null ? Math.round(s.avgWpm) : null,
    totalFillers: s.totalFillers,
    quantifiedCount: s.quantifiedCount,
    costUsd: usage && Number.isFinite(usage.costUsd) ? usage.costUsd : 0
  };
}

// Append a record, capping history length (oldest dropped first).
function appendHistory(history = [], record, max = 100) {
  const next = Array.isArray(history) ? history.slice() : [];
  next.push(record);
  return next.length > max ? next.slice(next.length - max) : next;
}

function average(nums) {
  const vals = nums.filter((n) => Number.isFinite(n));
  if (!vals.length) return null;
  return vals.reduce((a, b) => a + b, 0) / vals.length;
}

// Trailing moving average of scores, window N. Returns null-padded array aligned
// to the input so the UI can plot it against sessions.
function movingAverage(history = [], window = 3) {
  const scores = history.map((h) => (Number.isFinite(h.avgScore) ? h.avgScore : null));
  return scores.map((_, i) => {
    const slice = scores.slice(Math.max(0, i - window + 1), i + 1).filter((s) => s != null);
    return slice.length ? Math.round(average(slice)) : null;
  });
}

// Overall trend summary across scored sessions.
function computeTrends(history = []) {
  const scored = (history || []).filter((h) => Number.isFinite(h.avgScore));
  const base = {
    count: (history || []).length,
    scoredCount: scored.length,
    latestScore: null,
    bestScore: null,
    averageScore: null,
    improvement: null,
    trend: 'flat'
  };
  if (!scored.length) return base;

  const scores = scored.map((h) => h.avgScore);
  const latestScore = scores[scores.length - 1];
  const bestScore = Math.max(...scores);
  const averageScore = Math.round(average(scores));

  let improvement = null;
  if (scores.length >= 2) {
    const prev = scores.slice(0, -1);
    improvement = latestScore - Math.round(average(prev));
  }
  const trend = improvement == null ? 'flat' : improvement > 2 ? 'up' : improvement < -2 ? 'down' : 'flat';

  return { ...base, latestScore, bestScore, averageScore, improvement, trend };
}

module.exports = {
  summarizeSession,
  appendHistory,
  movingAverage,
  computeTrends
};
