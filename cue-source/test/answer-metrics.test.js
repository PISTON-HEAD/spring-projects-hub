const test = require('node:test');
const assert = require('node:assert/strict');
const {
  analyzeAnswer,
  countFillers,
  detectStar,
  hasQuantification,
  computeWpm
} = require('../src/answer-metrics');

test('countFillers counts words and phrases without double counting', () => {
  const { total, hits } = countFillers('Um, you know, I basically, uh, like, actually did it');
  assert.ok(total >= 6);
  assert.ok(hits['you know'] >= 1);
  assert.ok(hits['um'] >= 1);
});

test('detectStar recognizes a full behavioral answer', () => {
  const answer =
    'When our checkout was failing, my task was to fix it. So I built a retry queue and ' +
    'redesigned the flow, and as a result we reduced errors by 40%.';
  const { present, covered } = detectStar(answer);
  assert.equal(present.situation, true);
  assert.equal(present.task, true);
  assert.equal(present.action, true);
  assert.equal(present.result, true);
  assert.equal(covered, 4);
});

test('detectStar flags a vague answer as low coverage', () => {
  const { covered } = detectStar('It was fine and things went well overall.');
  assert.ok(covered < 2);
});

test('hasQuantification finds numbers, percentages, and scale', () => {
  assert.equal(hasQuantification('reduced latency by 40%'), true);
  assert.equal(hasQuantification('served 12000 users'), true);
  assert.equal(hasQuantification('$50k saved'), true);
  assert.equal(hasQuantification('it was much better'), false);
});

test('computeWpm needs a positive duration', () => {
  assert.equal(computeWpm(150, 0), null);
  assert.equal(computeWpm(150, undefined), null);
  // 150 words in 60s = 150 wpm.
  assert.equal(computeWpm(150, 60000), 150);
});

test('a strong behavioral answer scores high with no critical tips', () => {
  const answer =
    'When our onboarding funnel was dropping users, my job was to improve conversion. ' +
    'So I redesigned the signup flow, ran three A/B tests, and led the rollout. ' +
    'As a result, we increased activation by 25% and cut support tickets by 30%.';
  const r = analyzeAnswer(answer, { durationMs: 45000, category: 'behavioral' });
  assert.ok(r.score >= 75, 'expected high score, got ' + r.score);
  assert.equal(r.metrics.quantified, true);
  assert.equal(r.metrics.starCovered, 4);
});

test('a weak, rambling answer scores low and returns actionable tips', () => {
  const answer = 'Um, you know, it was like, basically, uh, kind of good I think, honestly.';
  const r = analyzeAnswer(answer, { durationMs: 20000, category: 'behavioral' });
  assert.ok(r.score < 55, 'expected low score, got ' + r.score);
  assert.ok(r.tips.length >= 2);
  assert.ok(r.tips.some((t) => /filler/i.test(t)));
  assert.ok(r.tips.some((t) => /metric|number/i.test(t)));
});

test('score and sub-scores stay within bounds', () => {
  const r = analyzeAnswer('Short answer.', {});
  assert.ok(r.score >= 0 && r.score <= 100);
  for (const v of Object.values(r.subScores)) assert.ok(v >= 0);
});

test('non-behavioral category does not penalize missing STAR structure', () => {
  const technical = 'A hash map gives O(1) average lookup by hashing the key to a bucket index.';
  const behavioral = analyzeAnswer(technical, { category: 'behavioral' });
  const tech = analyzeAnswer(technical, { category: 'technical' });
  assert.ok(tech.subScores.structure >= behavioral.subScores.structure);
});

test('fast pace produces a slow-down tip', () => {
  const answer = ('word ').repeat(200).trim(); // 200 words
  const r = analyzeAnswer(answer, { durationMs: 45000 }); // ~267 wpm
  assert.ok(r.metrics.wpm > 170);
  assert.ok(r.tips.some((t) => /slow down/i.test(t)));
});
