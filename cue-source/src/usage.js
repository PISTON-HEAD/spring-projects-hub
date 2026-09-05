// usage.js — token estimation and cost accounting for bring-your-own-key usage.
// Pure module (no electron, no network) so it is unit-testable and usable from
// both the main and renderer processes. Streamed responses don't return exact
// provider token counts, so this uses a transparent heuristic; when a caller has
// real token counts it can pass them and they win over the estimate.

// Approx chars-per-token. OpenAI, Anthropic, and Gemini all land near 4 for
// typical English prose, close enough for a live cost readout.
const CHARS_PER_TOKEN = 4;

// Extra tokens a screenshot adds to a request. Vision models bill images by
// tiles; this is a deliberately conservative flat estimate so the meter never
// silently under-reports an image-bearing turn to zero.
const IMAGE_TOKENS = 1000;

// Published list prices, USD per 1,000,000 tokens, as { in, out }. Providers
// change these often, so an unknown model falls back to a zero-cost estimate
// that is reported as unpriced rather than silently guessing a number.
const PRICES = {
  // OpenAI
  'gpt-4o-mini': { in: 0.15, out: 0.60 },
  'gpt-4o': { in: 2.50, out: 10.00 },
  'gpt-4.1-mini': { in: 0.40, out: 1.60 },
  'gpt-4.1': { in: 2.00, out: 8.00 },
  // Anthropic
  'claude-3-5-haiku-latest': { in: 0.80, out: 4.00 },
  'claude-3-5-sonnet-latest': { in: 3.00, out: 15.00 },
  // Google Gemini
  'gemini-2.5-flash': { in: 0.30, out: 2.50 },
  'gemini-2.5-pro': { in: 1.25, out: 10.00 },
  // Groq (Llama)
  'llama-3.1-8b-instant': { in: 0.05, out: 0.08 },
  'llama-3.3-70b-versatile': { in: 0.59, out: 0.79 }
};

function estimateTokens(text) {
  if (!text) return 0;
  const s = String(text);
  if (!s.trim()) return 0;
  return Math.max(1, Math.ceil(s.length / CHARS_PER_TOKEN));
}

// Case-insensitive price lookup so provider-cased ids ("GPT-4o") still match.
function priceFor(model) {
  if (!model) return null;
  const key = String(model).toLowerCase();
  for (const id of Object.keys(PRICES)) {
    if (id.toLowerCase() === key) return PRICES[id];
  }
  return null;
}

// Cost of a single request in USD. Returns { costUsd, priced } so the UI can
// show "—" instead of "$0.00" when the model has no known price.
function costOf(model, inputTokens, outputTokens) {
  const price = priceFor(model);
  if (!price) return { costUsd: 0, priced: false };
  const costUsd = (inputTokens / 1e6) * price.in + (outputTokens / 1e6) * price.out;
  return { costUsd, priced: true };
}

// Session-scoped accumulator. `record` accepts either raw text (estimated) or
// explicit token counts (preferred when the provider returns them).
function createUsageMeter() {
  const totals = { requests: 0, inputTokens: 0, outputTokens: 0, costUsd: 0, priced: false };
  const byModel = {};

  return {
    record({ model = '', inputText = '', outputText = '', hasImage = false, inputTokens, outputTokens } = {}) {
      const inTok = (Number.isFinite(inputTokens) ? inputTokens : estimateTokens(inputText)) +
        (hasImage ? IMAGE_TOKENS : 0);
      const outTok = Number.isFinite(outputTokens) ? outputTokens : estimateTokens(outputText);
      const { costUsd, priced } = costOf(model, inTok, outTok);

      totals.requests += 1;
      totals.inputTokens += inTok;
      totals.outputTokens += outTok;
      totals.costUsd += costUsd;
      if (priced) totals.priced = true;

      const m = byModel[model] || (byModel[model] = { requests: 0, inputTokens: 0, outputTokens: 0, costUsd: 0, priced });
      m.requests += 1;
      m.inputTokens += inTok;
      m.outputTokens += outTok;
      m.costUsd += costUsd;
      m.priced = m.priced || priced;

      return { inputTokens: inTok, outputTokens: outTok, costUsd, priced };
    },
    snapshot() {
      return {
        requests: totals.requests,
        inputTokens: totals.inputTokens,
        outputTokens: totals.outputTokens,
        totalTokens: totals.inputTokens + totals.outputTokens,
        costUsd: totals.costUsd,
        priced: totals.priced,
        byModel: JSON.parse(JSON.stringify(byModel))
      };
    },
    reset() {
      totals.requests = 0;
      totals.inputTokens = 0;
      totals.outputTokens = 0;
      totals.costUsd = 0;
      totals.priced = false;
      for (const k of Object.keys(byModel)) delete byModel[k];
    }
  };
}

// Compact "$1.23" / "<$0.01" / "—" formatting for the pill readout.
function formatCost(costUsd, priced = true) {
  if (!priced) return '—';
  if (costUsd <= 0) return '$0.00';
  if (costUsd < 0.01) return '<$0.01';
  return '$' + costUsd.toFixed(2);
}

module.exports = {
  estimateTokens,
  priceFor,
  costOf,
  createUsageMeter,
  formatCost,
  CHARS_PER_TOKEN,
  IMAGE_TOKENS,
  PRICES
};
