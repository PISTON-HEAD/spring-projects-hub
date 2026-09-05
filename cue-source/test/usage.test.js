const test = require('node:test');
const assert = require('node:assert/strict');
const {
  estimateTokens,
  priceFor,
  costOf,
  createUsageMeter,
  formatCost,
  IMAGE_TOKENS
} = require('../src/usage');

test('estimateTokens is zero for empty or whitespace and positive otherwise', () => {
  assert.equal(estimateTokens(''), 0);
  assert.equal(estimateTokens('   '), 0);
  assert.equal(estimateTokens(null), 0);
  assert.ok(estimateTokens('hello world') >= 1);
  // ~4 chars/token: a 40-char string is ~10 tokens.
  assert.equal(estimateTokens('a'.repeat(40)), 10);
});

test('priceFor is case-insensitive and null for unknown models', () => {
  assert.ok(priceFor('gpt-4o-mini'));
  assert.deepEqual(priceFor('GPT-4O-MINI'), priceFor('gpt-4o-mini'));
  assert.equal(priceFor('some-unlisted-model'), null);
  assert.equal(priceFor(''), null);
});

test('costOf reports priced=false for unknown models and does not guess a cost', () => {
  const unknown = costOf('mystery-model', 1000, 1000);
  assert.equal(unknown.costUsd, 0);
  assert.equal(unknown.priced, false);

  const known = costOf('gpt-4o-mini', 1_000_000, 1_000_000);
  assert.equal(known.priced, true);
  // 0.15 in + 0.60 out per 1M tokens.
  assert.ok(Math.abs(known.costUsd - 0.75) < 1e-9);
});

test('meter accumulates estimated tokens and cost across requests', () => {
  const meter = createUsageMeter();
  meter.record({ model: 'gpt-4o-mini', inputText: 'a'.repeat(40), outputText: 'b'.repeat(40) });
  meter.record({ model: 'gpt-4o-mini', inputText: 'a'.repeat(40), outputText: 'b'.repeat(40) });

  const snap = meter.snapshot();
  assert.equal(snap.requests, 2);
  assert.equal(snap.inputTokens, 20); // 10 + 10
  assert.equal(snap.outputTokens, 20);
  assert.equal(snap.totalTokens, 40);
  assert.equal(snap.priced, true);
  assert.ok(snap.costUsd > 0);
  assert.equal(snap.byModel['gpt-4o-mini'].requests, 2);
});

test('explicit token counts win over the text estimate', () => {
  const meter = createUsageMeter();
  meter.record({ model: 'gpt-4o-mini', inputText: 'ignored', outputText: 'ignored', inputTokens: 500, outputTokens: 250 });
  const snap = meter.snapshot();
  assert.equal(snap.inputTokens, 500);
  assert.equal(snap.outputTokens, 250);
});

test('an image adds a conservative token surcharge to the input side', () => {
  const meter = createUsageMeter();
  meter.record({ model: 'gpt-4o', inputText: '', outputText: '', hasImage: true });
  const snap = meter.snapshot();
  assert.equal(snap.inputTokens, IMAGE_TOKENS);
});

test('unpriced usage keeps priced=false so the UI can show a dash', () => {
  const meter = createUsageMeter();
  meter.record({ model: 'local-ollama-model', inputText: 'hello', outputText: 'world' });
  const snap = meter.snapshot();
  assert.equal(snap.priced, false);
  assert.ok(snap.totalTokens > 0);
});

test('reset clears totals and per-model breakdown', () => {
  const meter = createUsageMeter();
  meter.record({ model: 'gpt-4o-mini', inputText: 'hello', outputText: 'world' });
  meter.reset();
  const snap = meter.snapshot();
  assert.equal(snap.requests, 0);
  assert.equal(snap.totalTokens, 0);
  assert.equal(snap.costUsd, 0);
  assert.deepEqual(snap.byModel, {});
});

test('formatCost renders compact, human-friendly strings', () => {
  assert.equal(formatCost(0, false), '—');
  assert.equal(formatCost(0, true), '$0.00');
  assert.equal(formatCost(0.004, true), '<$0.01');
  assert.equal(formatCost(1.239, true), '$1.24');
});
