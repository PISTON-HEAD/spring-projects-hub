const test = require('node:test');
const assert = require('node:assert/strict');
const { MODES, languageDirective } = require('../src/prompts');

test('languageDirective defaults to English for empty/English input', () => {
  assert.match(languageDirective(''), /natural English/);
  assert.match(languageDirective(null), /natural English/);
  assert.match(languageDirective('English'), /natural English/);
  assert.match(languageDirective('english'), /natural English/);
});

test('languageDirective supports auto/match to mirror the speaker', () => {
  assert.match(languageDirective('auto'), /same language the other person/i);
  assert.match(languageDirective('match'), /same language the other person/i);
});

test('languageDirective names a specific requested language', () => {
  assert.match(languageDirective('Spanish'), /natural Spanish/);
  assert.match(languageDirective('Japanese'), /natural Japanese/);
});

test('conversational modes embed the requested language in their system prompt', () => {
  for (const mode of ['assist', 'say', 'ask', 'answerThis', 'followup', 'recap']) {
    const sys = MODES[mode].buildSystem(null, '', 'Spanish');
    assert.match(sys, /natural Spanish/, `${mode} should request Spanish`);
  }
});

test('omitting the language argument preserves English (backward compatible)', () => {
  const sys = MODES.assist.buildSystem(null, '');
  assert.match(sys, /natural English/);
});

test('leetcode ignores response language (coding answers stay strict)', () => {
  const sys = MODES.leetcode.buildSystem(null, '', 'Spanish');
  assert.ok(!/natural Spanish/.test(sys));
  assert.match(sys, /competitive programmer/);
});
