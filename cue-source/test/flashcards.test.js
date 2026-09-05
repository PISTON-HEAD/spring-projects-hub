const test = require('node:test');
const assert = require('node:assert/strict');
const {
  buildFlashcardSystem,
  parseFlashcards,
  fallbackFlashcards,
  stripFence
} = require('../src/flashcards');

test('buildFlashcardSystem requests a fixed count and JSON output', () => {
  const sys = buildFlashcardSystem({ count: 6, jobDescription: 'Backend role', resumeText: 'Java dev' });
  assert.match(sys, /exactly 6 practice flashcards/);
  assert.match(sys, /JSON array/);
  assert.match(sys, /Backend role/);
  assert.match(sys, /Java dev/);
});

test('buildFlashcardSystem clamps the count into a sane range', () => {
  assert.match(buildFlashcardSystem({ count: 999 }), /exactly 30 practice flashcards/);
  assert.match(buildFlashcardSystem({ count: 0 }), /exactly 8 practice flashcards/);
});

test('stripFence removes markdown code fences', () => {
  assert.equal(stripFence('```json\n[1,2]\n```'), '[1,2]');
  assert.equal(stripFence('[1,2]'), '[1,2]');
});

test('parseFlashcards parses a clean JSON array', () => {
  const cards = parseFlashcards('[{"category":"behavioral","question":"Tell me about a challenge","answer":"I did X"}]');
  assert.equal(cards.length, 1);
  assert.equal(cards[0].category, 'behavioral');
  assert.equal(cards[0].question, 'Tell me about a challenge');
  assert.equal(cards[0].answer, 'I did X');
});

test('parseFlashcards tolerates a fenced JSON array with surrounding prose', () => {
  const reply = 'Here are your cards:\n```json\n[{"question":"Why us?","answer":"Because mission"}]\n```';
  const cards = parseFlashcards(reply);
  assert.equal(cards.length, 1);
  assert.equal(cards[0].question, 'Why us?');
});

test('parseFlashcards falls back to Q/A parsing when JSON is absent', () => {
  const reply = 'Q: Tell me about yourself\nA: I am an engineer.\n---\nQ: Why leaving?\nA: New challenge.';
  const cards = parseFlashcards(reply);
  assert.equal(cards.length, 2);
  assert.equal(cards[0].question, 'Tell me about yourself');
  assert.equal(cards[0].answer, 'I am an engineer.');
  assert.equal(cards[1].question, 'Why leaving?');
});

test('parseFlashcards drops entries without a question', () => {
  const cards = parseFlashcards('[{"answer":"orphan"},{"question":"Real?","answer":"yes"}]');
  assert.equal(cards.length, 1);
  assert.equal(cards[0].question, 'Real?');
});

test('parseFlashcards returns empty array for garbage input', () => {
  assert.deepEqual(parseFlashcards('not structured at all'), []);
  assert.deepEqual(parseFlashcards(''), []);
});

test('fallbackFlashcards yields the requested number of bank questions', () => {
  const cards = fallbackFlashcards({ count: 5 });
  assert.equal(cards.length, 5);
  cards.forEach((c) => {
    assert.ok(c.question);
    assert.equal(c.answer, '');
    assert.ok(c.category);
  });
});

test('fallbackFlashcards honors a category filter', () => {
  const cards = fallbackFlashcards({ categories: ['technical'], count: 2 });
  cards.forEach((c) => assert.equal(c.category, 'technical'));
});
