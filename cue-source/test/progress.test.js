const test = require('node:test');
const assert = require('node:assert/strict');
const {
  summarizeSession,
  appendHistory,
  movingAverage,
  computeTrends
} = require('../src/progress');

const strongAnswer =
  'When our onboarding funnel was dropping users, my job was to improve conversion. ' +
  'So I redesigned the signup flow and led the rollout. As a result, we increased activation by 25%.';

test('summarizeSession stores aggregates only, no answer text', () => {
  const rec = summarizeSession({
    startedAt: 1000,
    endedAt: 2000,
    answers: [{ question: 'q', answer: strongAnswer, durationMs: 40000, category: 'behavioral' }],
    usage: { costUsd: 0.01 }
  });
  assert.equal(rec.date, 1000);
  assert.equal(rec.questions, 1);
  assert.ok(rec.avgScore > 0);
  assert.equal(rec.costUsd, 0.01);
  assert.ok(!('answers' in rec));
  assert.ok(!JSON.stringify(rec).includes('onboarding'));
});

test('appendHistory caps length dropping oldest first', () => {
  let hist = [];
  for (let i = 0; i < 5; i++) hist = appendHistory(hist, { date: i, avgScore: i }, 3);
  assert.equal(hist.length, 3);
  assert.deepEqual(hist.map((h) => h.date), [2, 3, 4]);
});

test('movingAverage smooths scores with a trailing window', () => {
  const hist = [{ avgScore: 60 }, { avgScore: 70 }, { avgScore: 80 }];
  assert.deepEqual(movingAverage(hist, 2), [60, 65, 75]);
});

test('computeTrends reports empty state cleanly', () => {
  const t = computeTrends([]);
  assert.equal(t.scoredCount, 0);
  assert.equal(t.latestScore, null);
  assert.equal(t.trend, 'flat');
});

test('computeTrends detects improvement', () => {
  const hist = [{ avgScore: 50 }, { avgScore: 55 }, { avgScore: 75 }];
  const t = computeTrends(hist);
  assert.equal(t.latestScore, 75);
  assert.equal(t.bestScore, 75);
  assert.ok(t.improvement > 0);
  assert.equal(t.trend, 'up');
});

test('computeTrends detects regression', () => {
  const hist = [{ avgScore: 80 }, { avgScore: 78 }, { avgScore: 60 }];
  const t = computeTrends(hist);
  assert.ok(t.improvement < 0);
  assert.equal(t.trend, 'down');
});

test('computeTrends ignores unscored sessions', () => {
  const hist = [{ avgScore: null }, { avgScore: 70 }, { avgScore: 72 }];
  const t = computeTrends(hist);
  assert.equal(t.count, 3);
  assert.equal(t.scoredCount, 2);
});
