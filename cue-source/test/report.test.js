const test = require('node:test');
const assert = require('node:assert/strict');
const {
  buildSessionReport,
  summarizeAnswers,
  aggregateFocusAreas,
  formatDuration,
  scoreLabel
} = require('../src/report');

const strongAnswer =
  'When our onboarding funnel was dropping users, my job was to improve conversion. ' +
  'So I redesigned the signup flow, ran three A/B tests, and led the rollout. ' +
  'As a result, we increased activation by 25% and cut support tickets by 30%.';

const weakAnswer = 'Um, you know, it was like basically fine, uh, honestly.';

test('formatDuration renders h/m/s spans', () => {
  assert.equal(formatDuration(0), '0s');
  assert.equal(formatDuration(12000), '12s');
  assert.equal(formatDuration(252000), '4m 12s');
  assert.equal(formatDuration(3852000), '1h 04m 12s');
});

test('scoreLabel buckets scores into words', () => {
  assert.equal(scoreLabel(90), 'Excellent');
  assert.equal(scoreLabel(72), 'Strong');
  assert.equal(scoreLabel(60), 'Fair');
  assert.equal(scoreLabel(40), 'Needs work');
});

test('summarizeAnswers aggregates scores and counts', () => {
  const s = summarizeAnswers([
    { question: 'Tell me about a challenge', answer: strongAnswer, durationMs: 45000, category: 'behavioral' },
    { question: 'Another one', answer: weakAnswer, durationMs: 15000, category: 'behavioral' }
  ]);
  assert.equal(s.count, 2);
  assert.ok(s.avgScore != null);
  assert.equal(s.quantifiedCount, 1);
  assert.ok(s.totalFillers >= 3);
});

test('aggregateFocusAreas surfaces recurring weaknesses sorted by frequency', () => {
  const s = summarizeAnswers([
    { question: 'q1', answer: weakAnswer, category: 'behavioral' },
    { question: 'q2', answer: weakAnswer, category: 'behavioral' }
  ]);
  const focus = aggregateFocusAreas(s.analyzed);
  assert.ok(focus.length > 0);
  // Most frequent first.
  for (let i = 1; i < focus.length; i++) assert.ok(focus[i - 1].count >= focus[i].count);
});

test('buildSessionReport produces a well-formed Markdown document', () => {
  const md = buildSessionReport({
    startedAt: Date.UTC(2026, 0, 15, 10, 0, 0),
    endedAt: Date.UTC(2026, 0, 15, 10, 12, 0),
    provider: 'openai',
    model: 'gpt-4o-mini',
    jobTitle: 'Senior Engineer',
    transcript: [
      { channel: 'them', text: 'Tell me about a challenge' },
      { channel: 'you', text: strongAnswer }
    ],
    answers: [
      { question: 'Tell me about a challenge', answer: strongAnswer, durationMs: 45000, category: 'behavioral' }
    ],
    usage: { requests: 3, totalTokens: 1500, costUsd: 0.0032, priced: true }
  });

  assert.match(md, /^# cue Practice Session/m);
  assert.match(md, /\*\*Duration:\*\* 12m 00s/);
  assert.match(md, /\*\*Target role:\*\* Senior Engineer/);
  assert.match(md, /## Summary/);
  assert.match(md, /Questions answered:\*\* 1/);
  assert.match(md, /## Question-by-question/);
  assert.match(md, /### Q1\. Tell me about a challenge/);
  assert.match(md, /## Full transcript/);
  assert.match(md, /\*\*Them:\*\*/);
  assert.match(md, /Est\. cost:\*\* \$0\.01|<\$0\.01/);
});

test('buildSessionReport stays valid with no answers or transcript', () => {
  const md = buildSessionReport({});
  assert.match(md, /# cue Practice Session/);
  assert.match(md, /No scored answers in this session\./);
  assert.match(md, /_\(no transcript captured\)_/);
});
