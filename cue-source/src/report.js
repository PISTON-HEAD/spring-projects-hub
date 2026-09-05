// report.js — builds an exportable Markdown review of a practice/interview
// session. Pure module (no electron, no fs) so it is unit-testable; the caller
// is responsible for writing the returned string to disk. Combines the answer
// metrics, usage totals, and transcript into one human-readable report.

const { analyzeAnswer } = require('./answer-metrics');
const { formatCost } = require('./usage');

function pad2(n) { return String(n).padStart(2, '0'); }

// "1h 04m 12s" / "4m 12s" / "12s" from a millisecond span.
function formatDuration(ms) {
  if (!Number.isFinite(ms) || ms <= 0) return '0s';
  const total = Math.round(ms / 1000);
  const h = Math.floor(total / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  if (h) return `${h}h ${pad2(m)}m ${pad2(s)}s`;
  if (m) return `${m}m ${pad2(s)}s`;
  return `${s}s`;
}

function avg(nums) {
  const vals = nums.filter((n) => Number.isFinite(n));
  if (!vals.length) return null;
  return vals.reduce((a, b) => a + b, 0) / vals.length;
}

function scoreLabel(score) {
  if (score >= 85) return 'Excellent';
  if (score >= 70) return 'Strong';
  if (score >= 55) return 'Fair';
  return 'Needs work';
}

// answers: [{ question, answer, durationMs, category }]
// Each answer is analyzed here so the report is the single source of scoring.
function summarizeAnswers(answers = []) {
  const analyzed = answers.map((a) => ({
    question: a.question || '',
    answer: a.answer || '',
    category: a.category || null,
    result: analyzeAnswer(a.answer || '', { durationMs: a.durationMs, category: a.category })
  }));

  const scores = analyzed.map((a) => a.result.score);
  const wpms = analyzed.map((a) => a.result.metrics.wpm).filter((x) => x != null);
  const totalFillers = analyzed.reduce((sum, a) => sum + a.result.metrics.fillerCount, 0);
  const quantifiedCount = analyzed.filter((a) => a.result.metrics.quantified).length;

  return {
    analyzed,
    count: analyzed.length,
    avgScore: avg(scores),
    avgWpm: avg(wpms),
    totalFillers,
    quantifiedCount
  };
}

// Aggregate recurring weaknesses across all answers into a short list.
function aggregateFocusAreas(analyzed) {
  const buckets = {
    structure: 0,
    metrics: 0,
    filler: 0,
    pace: 0,
    length: 0
  };
  for (const a of analyzed) {
    for (const tip of a.result.tips) {
      if (/STAR|structure/i.test(tip)) buckets.structure += 1;
      else if (/metric|number/i.test(tip)) buckets.metrics += 1;
      else if (/filler/i.test(tip)) buckets.filler += 1;
      else if (/pace|slow down|wpm/i.test(tip)) buckets.pace += 1;
      else if (/short|long|tighten/i.test(tip)) buckets.length += 1;
    }
  }
  const labels = {
    structure: 'Use complete STAR structure (Situation, Task, Action, Result)',
    metrics: 'Quantify impact with concrete numbers',
    filler: 'Reduce filler words',
    pace: 'Adjust speaking pace',
    length: 'Right-size answer length (~30-90s)'
  };
  return Object.entries(buckets)
    .filter(([, count]) => count > 0)
    .sort((a, b) => b[1] - a[1])
    .map(([key, count]) => ({ area: labels[key], count }));
}

function formatTranscript(transcript = []) {
  if (!transcript.length) return '_(no transcript captured)_';
  return transcript
    .map((t) => `- **${t.channel === 'them' ? 'Them' : 'You'}:** ${String(t.text || '').trim()}`)
    .join('\n');
}

// Build the full Markdown report. All inputs optional; sections are omitted
// when their data is absent so a bare session still produces a valid document.
function buildSessionReport({
  title = 'cue Practice Session',
  startedAt,
  endedAt,
  provider,
  model,
  jobTitle,
  transcript = [],
  answers = [],
  usage = null
} = {}) {
  const when = Number.isFinite(startedAt) ? new Date(startedAt) : new Date();
  const durationMs = Number.isFinite(startedAt) && Number.isFinite(endedAt) ? endedAt - startedAt : null;
  const s = summarizeAnswers(answers);
  const focus = aggregateFocusAreas(s.analyzed);

  const lines = [];
  lines.push(`# ${title}`);
  lines.push('');
  lines.push(`**Date:** ${when.toISOString().slice(0, 16).replace('T', ' ')}`);
  if (durationMs != null) lines.push(`**Duration:** ${formatDuration(durationMs)}`);
  if (jobTitle) lines.push(`**Target role:** ${jobTitle}`);
  if (provider || model) lines.push(`**Model:** ${[provider, model].filter(Boolean).join(' / ')}`);
  lines.push('');

  // ---- Summary ----
  lines.push('## Summary');
  lines.push('');
  if (s.count) {
    lines.push(`- **Questions answered:** ${s.count}`);
    if (s.avgScore != null) lines.push(`- **Average score:** ${Math.round(s.avgScore)}/100 (${scoreLabel(s.avgScore)})`);
    if (s.avgWpm != null) lines.push(`- **Average pace:** ${Math.round(s.avgWpm)} wpm`);
    lines.push(`- **Answers with metrics:** ${s.quantifiedCount}/${s.count}`);
    lines.push(`- **Total filler words:** ${s.totalFillers}`);
  } else {
    lines.push('- No scored answers in this session.');
  }
  if (usage) {
    lines.push(`- **Requests:** ${usage.requests} · **Tokens:** ${usage.totalTokens} · **Est. cost:** ${formatCost(usage.costUsd, usage.priced)}`);
  }
  lines.push('');

  // ---- Focus areas ----
  if (focus.length) {
    lines.push('## Top areas to improve');
    lines.push('');
    focus.forEach((f) => lines.push(`- ${f.area} _(flagged in ${f.count} answer${f.count === 1 ? '' : 's'})_`));
    lines.push('');
  }

  // ---- Per-question breakdown ----
  if (s.count) {
    lines.push('## Question-by-question');
    lines.push('');
    s.analyzed.forEach((a, i) => {
      const m = a.result.metrics;
      lines.push(`### Q${i + 1}. ${a.question || '(question not recorded)'}`);
      lines.push('');
      lines.push(`- **Score:** ${a.result.score}/100 (${scoreLabel(a.result.score)})`);
      lines.push(`- **Metrics:** ${m.wordCount} words` +
        (m.wpm != null ? ` · ${m.wpm} wpm` : '') +
        ` · ${m.fillerCount} fillers` +
        ` · STAR ${m.starCovered}/4` +
        ` · ${m.quantified ? 'quantified' : 'no metrics'}`);
      if (a.result.tips.length) {
        lines.push('- **Tips:**');
        a.result.tips.forEach((t) => lines.push(`  - ${t}`));
      }
      lines.push('');
    });
  }

  // ---- Transcript appendix ----
  lines.push('## Full transcript');
  lines.push('');
  lines.push(formatTranscript(transcript));
  lines.push('');

  return lines.join('\n');
}

module.exports = {
  buildSessionReport,
  summarizeAnswers,
  aggregateFocusAreas,
  formatDuration,
  scoreLabel
};
