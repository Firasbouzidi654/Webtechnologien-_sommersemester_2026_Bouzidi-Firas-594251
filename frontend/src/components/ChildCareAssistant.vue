<template>
  <section class="ai-care-hub" aria-labelledby="ai-care-hub-title">
    <header class="hub-header">
      <div>
        <p class="eyebrow">AI support</p>
        <h2 id="ai-care-hub-title">AI Child Care Assistant</h2>
      </div>
      <span class="ai-badge">Educational support</span>
    </header>

    <p class="hub-summary">Support for staff communications, medication, symptoms, allergies, and incident situations.</p>

    <div class="ai-tabs" role="tablist" aria-label="AI support options">
      <button
        v-for="tab in tabs"
        :id="`ai-tab-${tab.key}`"
        :key="tab.key"
        type="button"
        role="tab"
        :aria-selected="activeTab === tab.key"
        :class="{ active: activeTab === tab.key }"
        @click="selectTab(tab.key)"
      >{{ tab.label }}</button>
    </div>

    <label class="message-field" for="ai-care-message">
      <span>{{ activeTab === 'PARENT' ? 'Situation or update' : 'Situation or concern' }}</span>
      <textarea
        id="ai-care-message"
        v-model.trim="message"
        rows="4"
        maxlength="4000"
        :placeholder="activeTab === 'PARENT' ? 'Describe the situation...' : 'Describe the situation, medication, symptoms, or concern...'"
        :disabled="loading"
      ></textarea>
    </label>

    <button class="primary-button" type="button" :disabled="loading || !message" @click="generateResponse">
      <span v-if="loading" class="spinner" aria-hidden="true"></span>
      {{ loading ? loadingLabel : actionLabel }}
    </button>

    <p v-if="errorMessage" class="status-message error-message" role="alert">{{ errorMessage }}</p>
    <p v-else-if="loading" class="status-message">
      <span class="spinner dark-spinner" aria-hidden="true"></span>
      {{ activeTab === 'PARENT' ? 'Preparing a parent message...' : 'Preparing a safe, general response...' }}
    </p>

    <article v-if="answer" class="ai-answer-card" aria-live="polite">
      <p class="eyebrow">{{ activeTab === 'PARENT' ? 'Generated parent message' : 'AI response' }}</p>
      <p class="answer-content">{{ answer }}</p>

      <div class="answer-actions">
        <button
          v-if="activeTab === 'INCIDENT'"
          class="secondary-button"
          type="button"
          :disabled="loading"
          @click="generateParentMessageFromIncident"
        >Generate Parent Message</button>
        <button v-if="activeTab === 'PARENT'" class="secondary-button" type="button" @click="copyMessage">
          {{ copied ? 'Copied' : 'Copy Message' }}
        </button>
      </div>
    </article>

    <p class="assistant-disclaimer">
      AI support is for educational purposes only and does not replace professional medical advice.
    </p>
  </section>
</template>

<script>
import { api } from '../services/api';

const TABS = [
  { key: 'MEDICATION', label: 'Medication Information' },
  { key: 'SYMPTOMS', label: 'Child Symptoms' },
  { key: 'ALLERGY', label: 'Allergy Concern' },
  { key: 'INCIDENT', label: 'Incident Report' },
  { key: 'PARENT', label: 'Parent Message' }
];

export default {
  name: 'ChildCareAssistant',
  data() {
    return {
      tabs: TABS,
      activeTab: 'MEDICATION',
      message: '',
      answer: '',
      errorMessage: '',
      loading: false,
      copied: false,
      copyResetTimer: null
    };
  },
  computed: {
    isParentMessage() {
      return this.activeTab === 'PARENT';
    },
    actionLabel() {
      return this.isParentMessage ? 'Generate Parent Message' : 'Ask AI';
    },
    loadingLabel() {
      return this.isParentMessage ? 'Generating message...' : 'Asking AI...';
    }
  },
  beforeUnmount() {
    window.clearTimeout(this.copyResetTimer);
  },
  methods: {
    selectTab(tabKey) {
      this.activeTab = tabKey;
      this.message = '';
      this.answer = '';
      this.errorMessage = '';
      this.copied = false;
    },
    async generateResponse() {
      if (!this.message || this.loading) return;

      this.loading = true;
      this.answer = '';
      this.errorMessage = '';
      this.copied = false;

      try {
        const response = this.isParentMessage
          ? await api.generateParentMessage({ message: this.message })
          : await api.askChildCareAssistant({ type: this.activeTab, message: this.message });
        this.answer = this.isParentMessage
          ? response.parentMessage || 'No parent message was returned. Please try again.'
          : response.answer || 'No answer was returned. Please try again.';
      } catch (error) {
        this.errorMessage = error.message || 'AI support is currently unavailable. Please try again later.';
      } finally {
        this.loading = false;
      }
    },
    async generateParentMessageFromIncident() {
      if (!this.answer || this.loading) return;

      const incidentReport = this.answer;
      this.activeTab = 'PARENT';
      this.message = incidentReport;
      await this.generateResponse();
    },
    async copyMessage() {
      if (!this.answer) return;

      try {
        await navigator.clipboard.writeText(this.answer);
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
.ai-care-hub {
  display: grid;
  gap: 13px;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  color: var(--color-text-primary);
}

.hub-header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
  min-width: 0;
}

.eyebrow { margin: 0 0 4px; color: var(--color-text-secondary); font-size: 0.72rem; font-weight: 800; letter-spacing: 0.08em; text-transform: uppercase; }
h2 { margin: 0; color: var(--color-text-primary); font-size: 1.12rem; }

.ai-badge {
  border-radius: 999px;
  padding: 6px 9px;
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  font-size: 0.7rem;
  font-weight: 800;
  white-space: nowrap;
}

.hub-summary,
.assistant-disclaimer { margin: 0; color: var(--color-text-secondary); font-size: 0.86rem; line-height: 1.45; }

.ai-tabs { display: flex; flex-wrap: wrap; gap: 7px; }

.ai-tabs button {
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 7px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font: inherit;
  font-size: 0.75rem;
  font-weight: 750;
}

.ai-tabs button:hover,
.ai-tabs button.active { border-color: var(--color-brand); background: color-mix(in srgb, var(--color-brand) 14%, var(--color-bg-tertiary)); color: var(--color-text-primary); }

.message-field { display: grid; gap: 7px; color: var(--color-text-primary); font-size: 0.82rem; font-weight: 800; }

textarea {
  width: 100%;
  min-height: 100px;
  box-sizing: border-box;
  resize: vertical;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font: inherit;
  font-size: 0.88rem;
  line-height: 1.45;
}

textarea:focus { outline: 2px solid color-mix(in srgb, var(--color-brand) 36%, transparent); border-color: var(--color-brand); }

.primary-button,
.secondary-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 41px;
  border-radius: 12px;
  padding: 9px 13px;
  cursor: pointer;
  font: inherit;
  font-size: 0.84rem;
  font-weight: 850;
}

.primary-button { border: 0; background: linear-gradient(135deg, var(--color-brand), #256db0); color: #fff; }
.primary-button:disabled,
.secondary-button:disabled { cursor: not-allowed; opacity: 0.62; }
.primary-button:hover:not(:disabled) { box-shadow: var(--shadow-md); }

.status-message {
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 0;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.84rem;
  font-weight: 700;
}

.error-message { border-color: var(--color-missed-border); background: var(--color-missed); color: var(--color-missed-text); }

.ai-answer-card {
  display: grid;
  gap: 10px;
  border: 1px solid var(--color-border-light);
  border-radius: 14px;
  padding: 13px;
  background: var(--color-bg-primary);
}

.answer-content { margin: 0; color: var(--color-text-primary); white-space: pre-wrap; line-height: 1.52; font-size: 0.89rem; }
.answer-actions { display: flex; flex-wrap: wrap; gap: 8px; }
.secondary-button { border: 1px solid var(--color-border); background: var(--color-bg-secondary); color: var(--color-text-primary); }
.secondary-button:hover:not(:disabled) { border-color: var(--color-brand); }

.assistant-disclaimer { border: 1px solid var(--color-border-light); border-radius: 12px; padding: 9px 11px; background: var(--color-bg-tertiary); font-size: 0.77rem; }

.spinner { width: 15px; height: 15px; border: 2px solid rgba(255, 255, 255, 0.45); border-top-color: #fff; border-radius: 50%; animation: spin 0.8s linear infinite; }
.dark-spinner { border-color: color-mix(in srgb, var(--color-text-secondary) 30%, transparent); border-top-color: var(--color-text-secondary); }

@keyframes spin { to { transform: rotate(360deg); } }

:global([data-theme="dark"]) .ai-care-hub {
  border-color: rgba(255, 255, 255, 0.06);
  background: linear-gradient(135deg, #111827 0%, #1e293b 100%);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) textarea,
:global([data-theme="dark"]) .ai-answer-card,
:global([data-theme="dark"]) .secondary-button {
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .hub-summary,
:global([data-theme="dark"]) .eyebrow { color: #cbd5e1; }

@media (max-width: 640px) {
  .ai-care-hub { padding: 15px; }
  .hub-header { flex-direction: column; }
  .ai-tabs { display: grid; grid-template-columns: 1fr; }
  .ai-tabs button { width: 100%; text-align: left; }
  .primary-button,
  .secondary-button { width: 100%; }
  .ai-badge { display: none; }
}
</style>
