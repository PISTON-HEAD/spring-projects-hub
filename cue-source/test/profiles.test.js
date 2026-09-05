const test = require('node:test');
const assert = require('node:assert/strict');
const {
  PROFILE_FIELDS,
  extractProfile,
  applyProfile,
  saveProfile,
  loadProfile,
  deleteProfile,
  listProfiles
} = require('../src/profiles');

function sampleSettings(overrides = {}) {
  return {
    provider: 'openai',
    apiKeys: { openai: 'secret-key' },
    responseLanguage: 'English',
    resumeText: 'Engineer at Acme',
    jobDescription: 'Senior Engineer\nOwn the platform',
    starStories: 'story one',
    whyCompany: 'great mission',
    salaryTarget: '$180k',
    profiles: {},
    activeProfile: '',
    ...overrides
  };
}

test('extractProfile picks only profile fields, never API keys', () => {
  const p = extractProfile(sampleSettings());
  for (const key of PROFILE_FIELDS) assert.ok(key in p, `expected ${key}`);
  assert.ok(!('apiKeys' in p), 'must not capture credentials');
});

test('applyProfile overlays fields without mutating the input', () => {
  const settings = sampleSettings();
  const applied = applyProfile(settings, { resumeText: 'New resume', provider: 'gemini' });
  assert.equal(applied.resumeText, 'New resume');
  assert.equal(applied.provider, 'gemini');
  // Original untouched.
  assert.equal(settings.resumeText, 'Engineer at Acme');
  // Non-profile fields preserved.
  assert.deepEqual(applied.apiKeys, settings.apiKeys);
});

test('saveProfile snapshots current content under a name', () => {
  const data = saveProfile(sampleSettings(), 'Acme', 1000);
  assert.ok(data.profiles.Acme);
  assert.equal(data.profiles.Acme.savedAt, 1000);
  assert.equal(data.profiles.Acme.fields.resumeText, 'Engineer at Acme');
  assert.equal(data.activeProfile, 'Acme');
  // Credentials are never stored inside a profile.
  assert.ok(!('apiKeys' in data.profiles.Acme.fields));
});

test('saveProfile rejects an empty name', () => {
  assert.throws(() => saveProfile(sampleSettings(), '   '), /cannot be empty/);
});

test('loadProfile applies a saved profile and marks it active', () => {
  let data = saveProfile(sampleSettings(), 'Acme', 1000);
  // Simulate the user editing settings for another company.
  data = { ...data, resumeText: 'Different', provider: 'anthropic' };
  const loaded = loadProfile(data, 'Acme');
  assert.equal(loaded.resumeText, 'Engineer at Acme');
  assert.equal(loaded.provider, 'openai');
  assert.equal(loaded.activeProfile, 'Acme');
  // Global credentials survive the switch.
  assert.deepEqual(loaded.apiKeys, data.apiKeys);
});

test('loadProfile throws for an unknown profile', () => {
  assert.throws(() => loadProfile(sampleSettings(), 'Ghost'), /not found/);
});

test('deleteProfile removes it and clears active pointer', () => {
  let data = saveProfile(sampleSettings(), 'Acme', 1000);
  data = deleteProfile(data, 'Acme');
  assert.ok(!data.profiles.Acme);
  assert.equal(data.activeProfile, '');
});

test('listProfiles returns sorted metadata without credentials', () => {
  let data = saveProfile(sampleSettings(), 'Zeta', 1);
  data = saveProfile(data, 'Alpha', 2);
  const list = listProfiles(data);
  assert.deepEqual(list.map((p) => p.name), ['Alpha', 'Zeta']);
  assert.equal(list[0].title, 'Senior Engineer');
  // No field leaks the api key.
  assert.ok(!JSON.stringify(list).includes('secret-key'));
});
