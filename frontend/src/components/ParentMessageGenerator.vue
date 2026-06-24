<template>
  <section class="parent-message-generator" aria-labelledby="parent-message-title">
    <header class="generator-header">
      <div>
        <p class="eyebrow">AI support</p>
        <h2 id="parent-message-title">AI Parent Message Generator</h2>
      </div>
      <span class="ai-badge">AI</span>
    </header>

    <p class="generator-description">
      Generate a professional message for parents based on a childcare situation.
    </p>

    <label class="message-field" for="parent-message-input">
      <span>Situation or update</span>
      <textarea
        id="parent-message-input"
        v-model.trim="message"
        rows="5"
        maxlength="4000"
        placeholder="Describe the situation..."
        :disabled="loading"
      ></textarea>
    </label>

    <button class="generate-button" type="button" :disabled="loading || !message" @click="generateMessage">
      <span v-if="loading" class="spinner" aria-hidden="true"></span>
      {{ loading ? 'Generating message...' : 'Generate Parent Message' }}
    </button>

    <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>

    <article v-if="parentMessage" class="generated-message" aria-live="polite">
      <p class="eyebrow">Generated parent message</p>
      <p class="message-content">{{ parentMessage }}</p>
      <button class="copy-button" type="button" @click="copyMessage">
        {{ copied ? 'Copied' : 'Copy Message' }}
      </button>
    </article>
  </section>
</template>

<script>
import { api } from '../services/api';

export default {
  name: 'ParentMessageGenerator',
  data() {
    return {
      message: '',
      parentMessage: '',
      loading: false,
      errorMessage: '',
      copied: false,
      copyResetTimer: null
    };
  },
  beforeUnmount() {
    window.clearTimeout(this.copyResetTimer);
  },
  methods: {
    async generateMessage() {
      if (!this.message || this.loading) return;

      this.loading = true;
      this.errorMessage = '';
      this.copied = false;

      try {
        const response = await api.generateParentMessage({ message: this.message });
        this.parentMessage = response.parentMessage || '';
      } catch (error) {
        this.parentMessage = '';
        this.errorMessage = error.message || 'Could not generate a parent message. Please try again.';
      } finally {
        this.loading = false;
      }
    },
    async copyMessage() {
      if (!this.parentMessage) return;

      try {
        await navigator.clipboard.writeText(this.parentMessage);
        this.copied = true;
        window.clearTimeout(this.copyResetTimer);
        this.copyResetTimer = window.setTimeout(() => {
          this.copied = false;
        }, 2000);
      } catch {
        this.errorMessage = 'Could not copy the message. Please select and copy it manually.';
      }
    }
  }
};
</script>

<style scoped>
.parent-message-generator {
  display: grid;
  gap: 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
}

.generator-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.eyebrow {
  margin: 0 0 4px;
  color: var(--color-text-secondary);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 1.15rem;
}

.ai-badge {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 10px;
  background: var(--color-info);
  color: var(--color-info-text);
  font-size: 0.75rem;
  font-weight: 900;
}

.generator-description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  line-height: 1.5;
}

.message-field {
  display: grid;
  gap: 7px;
  color: var(--color-text-primary);
  font-size: 0.85rem;
  font-weight: 800;
}

textarea {
  width: 100%;
  min-height: 116px;
  resize: vertical;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 11px 12px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font: inherit;
  font-size: 0.88rem;
  line-height: 1.45;
}

textarea:focus {
  outline: 2px solid color-mix(in srgb, var(--color-brand) 36%, transparent);
  border-color: var(--color-brand);
}

.generate-button,
.copy-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  border: 0;
  border-radius: 11px;
  padding: 10px 14px;
  cursor: pointer;
  font: inherit;
  font-size: 0.86rem;
  font-weight: 800;
}

.generate-button {
  background: var(--color-brand);
  color: white;
}

.generate-button:hover:not(:disabled) {
  filter: brightness(1.06);
}

.generate-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
}

.spinner {
  width: 15px;
  height: 15px;
  border: 2px solid rgba(255, 255, 255, 0.45);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

.error-message {
  margin: 0;
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--color-missed);
  color: var(--color-missed-text);
  font-size: 0.84rem;
  font-weight: 700;
}

.generated-message {
  display: grid;
  gap: 12px;
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 14px;
  background: var(--color-bg-primary);
}

.message-content {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 0.9rem;
  line-height: 1.55;
  white-space: pre-wrap;
}

.copy-button {
  justify-self: start;
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
}

.copy-button:hover {
  border-color: var(--color-brand);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

:global([data-theme="dark"]) .parent-message-generator {
  border-color: rgba(255, 255, 255, 0.06);
  background: linear-gradient(135deg, #111827 0%, #1e293b 100%);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) textarea,
:global([data-theme="dark"]) .generated-message,
:global([data-theme="dark"]) .copy-button {
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .generator-description,
:global([data-theme="dark"]) .eyebrow {
  color: #cbd5e1;
}

@media (max-width: 640px) {
  .parent-message-generator {
    padding: 15px;
  }

  .generate-button,
  .copy-button {
    width: 100%;
  }
}
</style>
