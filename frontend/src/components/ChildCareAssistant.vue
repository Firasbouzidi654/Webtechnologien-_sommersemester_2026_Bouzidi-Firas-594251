<template>
  <section class="childcare-assistant">
    <header>
      <div>
        <p class="eyebrow">AI support</p>
        <h2>AI Child Care Assistant</h2>
      </div>
      <span class="ai-badge">Educational support</span>
    </header>

    <p class="assistant-summary">Ask for support about medication, symptoms, allergies, or incident situations.</p>

    <div class="quick-prompts" aria-label="Quick prompts">
      <button
        v-for="prompt in prompts"
        :key="prompt.type"
        type="button"
        :class="{ active: type === prompt.type }"
        @click="selectPrompt(prompt)"
      >{{ prompt.label }}</button>
    </div>

    <label class="assistant-message-field">
      <span>Situation or concern</span>
      <textarea
        v-model.trim="message"
        rows="5"
        placeholder="Describe the situation, medication, symptoms, or concern…"
      ></textarea>
    </label>

    <button class="ask-ai-button" type="button" :disabled="loading || !message" @click="askAi">
      <span v-if="loading" class="assistant-spinner" aria-hidden="true"></span>
      <span>{{ loading ? 'Asking AI…' : 'Ask AI' }}</span>
    </button>

    <p v-if="errorMessage" class="assistant-message error-message" role="alert">{{ errorMessage }}</p>
    <p v-else-if="loading" class="assistant-message">
      <span class="assistant-spinner" aria-hidden="true"></span>
      Preparing a safe, general response…
    </p>

    <article v-if="answer" class="ai-answer-card">
      <p class="eyebrow">AI response</p>
      <p>{{ answer }}</p>
    </article>

    <p class="assistant-disclaimer">
      AI support is for educational purposes only and does not replace professional medical advice.
    </p>
  </section>
</template>

<script>
import { api } from '../services/api';

const PROMPTS = [
  {
    type: 'MEDICATION',
    label: 'Medication information',
    message: 'Explain this medication in simple terms, including common uses, precautions, and when staff should contact parents or a doctor.'
  },
  {
    type: 'SYMPTOMS',
    label: 'Child symptoms',
    message: 'Analyze these child symptoms and suggest safe general next steps for childcare staff.'
  },
  {
    type: 'ALLERGY',
    label: 'Allergy concern',
    message: 'Help assess this possible allergy situation and remind staff what safety steps to follow.'
  },
  {
    type: 'INCIDENT',
    label: 'Incident report',
    message: 'Transform this situation into a professional incident report with: child, time, symptoms/problem, action taken, priority level, and recommendation.'
  }
];

export default {
  name: 'ChildCareAssistant',
  data() {
    return {
      prompts: PROMPTS,
      type: 'MEDICATION',
      message: '',
      answer: '',
      errorMessage: '',
      loading: false
    };
  },
  methods: {
    selectPrompt(prompt) {
      this.type = prompt.type;
      this.message = prompt.message;
      this.answer = '';
      this.errorMessage = '';
    },
    async askAi() {
      if (!this.message || this.loading) return;

      this.loading = true;
      this.answer = '';
      this.errorMessage = '';
      try {
        const response = await api.askChildCareAssistant({
          type: this.type,
          message: this.message
        });
        this.answer = response.answer || 'No answer was returned. Please try again.';
      } catch (error) {
        this.errorMessage = error.message || 'AI support is currently unavailable. Please try again later.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.childcare-assistant {
  display: grid;
  gap: 14px;
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  align-self: start;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  color: var(--color-text-primary);
}

.childcare-assistant header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: flex-start;
}

.childcare-assistant h2 { margin: 4px 0 0; font-size: 1.12rem; }

.ai-badge {
  border-radius: 999px;
  padding: 6px 9px;
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  font-size: 0.7rem;
  font-weight: 800;
  white-space: nowrap;
}

.assistant-summary,
.assistant-disclaimer { margin: 0; color: var(--color-text-secondary); font-size: 0.88rem; line-height: 1.5; }

.quick-prompts { display: flex; flex-wrap: wrap; gap: 8px; }

.quick-prompts button {
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 7px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 0.76rem;
  font-weight: 750;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.quick-prompts button:hover,
.quick-prompts button.active { border-color: var(--color-brand); background: color-mix(in srgb, var(--color-brand) 14%, var(--color-bg-tertiary)); color: var(--color-text-primary); }

.assistant-message-field { display: grid; gap: 8px; color: var(--color-text-primary); font-size: 0.82rem; font-weight: 800; }

.assistant-message-field textarea {
  width: 100%;
  box-sizing: border-box;
  resize: vertical;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 11px 12px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font: inherit;
  line-height: 1.45;
}

.ask-ai-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 42px;
  border: none;
  border-radius: 12px;
  padding: 10px 14px;
  background: linear-gradient(135deg, var(--color-brand), #256db0);
  color: #fff;
  cursor: pointer;
  font-weight: 900;
  transition: box-shadow 0.22s ease, opacity 0.22s ease;
}

.ask-ai-button:disabled { cursor: not-allowed; opacity: 0.62; }
.ask-ai-button:hover:not(:disabled) { box-shadow: var(--shadow-md); }

.assistant-message {
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 0;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.86rem;
  font-weight: 700;
}

.error-message { border-color: var(--color-missed-border); background: var(--color-missed); color: var(--color-missed-text); }

.ai-answer-card {
  border: 1px solid var(--color-border-light);
  border-radius: 14px;
  padding: 14px;
  background: var(--color-bg-primary);
}

.ai-answer-card p:last-child { margin: 7px 0 0; color: var(--color-text-primary); white-space: pre-wrap; line-height: 1.55; font-size: 0.9rem; }

.assistant-disclaimer { border: 1px solid var(--color-border-light); border-radius: 12px; padding: 10px 12px; background: var(--color-bg-tertiary); font-size: 0.78rem; }

.assistant-spinner { width: 16px; height: 16px; border: 3px solid rgba(255, 255, 255, 0.38); border-top-color: currentColor; border-radius: 50%; animation: assistant-spin 0.8s linear infinite; }

@keyframes assistant-spin { to { transform: rotate(360deg); } }
</style>
