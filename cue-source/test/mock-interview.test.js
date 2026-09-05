const test = require('node:test');
const assert = require('node:assert/strict');
const {
  QUESTION_BANK,
  DEFAULT_CATEGORIES,
  defaultQuestionPlan,
  buildInterviewerSystem,
  createMockSession
} = require('../src/mock-interview');

test('defaultQuestionPlan returns the requested count spanning categories', () => {
  const plan = defaultQuestionPlan({ count: 5 });
  assert.equal(plan.length, 5);
  const cats = new Set(plan.map((q) => q.category));
  assert.ok(cats.size >= 3, 'a 5-question plan should span several categories');
  plan.forEach((q) => assert.ok(q.question && q.category));
});

test('defaultQuestionPlan is deterministic', () => {
  assert.deepEqual(defaultQuestionPlan({ count: 4 }), defaultQuestionPlan({ count: 4 }));
});

test('defaultQuestionPlan honors a restricted category list', () => {
  const plan = defaultQuestionPlan({ categories: ['technical'], count: 3 });
  assert.ok(plan.length >= 1);
  plan.forEach((q) => assert.equal(q.category, 'technical'));
});

test('defaultQuestionPlan clamps silly counts and ignores unknown categories', () => {
  const plan = defaultQuestionPlan({ categories: ['nonsense'], count: 0 });
  assert.ok(plan.length >= 1);
  // Unknown category falls back to the default arc.
  assert.ok(DEFAULT_CATEGORIES.includes(plan[0].category));
});

test('buildInterviewerSystem instructs one-question-at-a-time and embeds context', () => {
  const sys = buildInterviewerSystem({
    jobTitle: 'Staff Engineer',
    jobDescription: 'Own the payments platform.',
    resumeText: 'Led a team of 6 building a billing service.',
    difficulty: 'hard',
    categories: ['behavioral', 'technical']
  });
  assert.match(sys, /ONE question at a time/);
  assert.match(sys, /hard difficulty/);
  assert.match(sys, /Staff Engineer/);
  assert.match(sys, /payments platform/);
  assert.match(sys, /billing service/);
  assert.match(sys, /behavioral, technical/);
});

test('buildInterviewerSystem falls back to medium difficulty for bad input', () => {
  const sys = buildInterviewerSystem({ difficulty: 'nightmare' });
  assert.match(sys, /medium difficulty/);
});

test('createMockSession walks questions and records answers', () => {
  const session = createMockSession({
    questions: [
      { category: 'experience', question: 'Tell me about yourself.' },
      { category: 'behavioral', question: 'Describe a challenge.' }
    ]
  });
  assert.equal(session.total, 2);
  assert.equal(session.isDone(), false);
  assert.equal(session.current().question, 'Tell me about yourself.');

  assert.equal(session.submitAnswer('I am an engineer with 8 years experience.', 30000), true);
  assert.equal(session.next().question, 'Describe a challenge.');
  assert.equal(session.submitAnswer('When our system failed, I fixed it.', 25000), true);
  session.next();

  assert.equal(session.isDone(), true);
  assert.equal(session.current(), null);
  assert.equal(session.submitAnswer('too late', 1000), false);

  const results = session.getResults();
  assert.equal(results.length, 2);
  assert.equal(results[0].durationMs, 30000);
  assert.equal(results[1].category, 'behavioral');
});

test('progress reflects answered and total', () => {
  const session = createMockSession({ questions: defaultQuestionPlan({ count: 3 }) });
  assert.deepEqual(session.progress(), { index: 0, total: 3, answered: 0 });
  session.submitAnswer('answer one');
  session.next();
  assert.deepEqual(session.progress(), { index: 1, total: 3, answered: 1 });
});

test('createMockSession ignores malformed questions', () => {
  const session = createMockSession({ questions: [{ question: '' }, null, { question: 'valid?' }] });
  assert.equal(session.total, 1);
  assert.equal(session.current().question, 'valid?');
});

test('results feed report.buildSessionReport without throwing', () => {
  const { buildSessionReport } = require('../src/report');
  const session = createMockSession({ questions: defaultQuestionPlan({ count: 2 }) });
  session.submitAnswer('When our onboarding dropped users I redesigned the flow and increased activation by 25%.', 40000);
  session.next();
  session.submitAnswer('I led the migration to a new billing service.', 30000);
  session.next();
  const md = buildSessionReport({ answers: session.getResults() });
  assert.match(md, /Question-by-question/);
});
