<template>
  <section class="medication-assistant panel" :class="{ compact }">
    <header>
      <div>
        <p class="eyebrow">OpenFDA</p>
        <h2>Medication Assistant</h2>
      </div>
      <button type="button" @click="openAssistant">Open</button>
    </header>

    <form v-if="compact" class="assistant-inline-search" @submit.prevent="openAndSearch">
      <label class="inline-search-field" aria-label="Search medication information">
        <span></span>
        <input
          v-model.trim="query"
          type="search"
          placeholder="Search drug labels..."
          autocomplete="off"
        />
      </label>
      <button type="submit" :disabled="loading || !query">
        <span v-if="loading" class="assistant-spinner" aria-hidden="true"></span>
        <span>{{ loading ? 'Searching' : 'Search' }}</span>
      </button>
    </form>

    <p class="assistant-summary">
      Search public FDA labeling information for brand names, warnings, side effects, dosage, and manufacturer details.
    </p>

    <p class="assistant-disclaimer">
      This information is for educational support only and does not replace professional medical advice.
    </p>

    <section v-if="isOpen" class="modal-backdrop" @click.self="closeAssistant" role="dialog" aria-modal="true">
      <div class="modal medication-modal">
        <header>
          <div>
            <p class="eyebrow">Drug Information</p>
            <h2>Medication Assistant</h2>
          </div>
          <button type="button" aria-label="Close medication assistant" @click="closeAssistant">x</button>
        </header>

        <form class="medication-search" @submit.prevent="searchMedication">
          <label class="search-field">
            <span>Medication name</span>
            <input
              v-model.trim="query"
              type="search"
              placeholder="Ibuprofen, Paracetamol, Amoxicillin"
              autocomplete="off"
            />
          </label>
          <button type="submit" :disabled="loading || !query">
            <span v-if="loading" class="assistant-spinner" aria-hidden="true"></span>
            <span>{{ loading ? 'Searching' : 'Search' }}</span>
          </button>
        </form>

        <p class="assistant-disclaimer">
          This information is for educational support only and does not replace professional medical advice.
        </p>

        <div v-if="errorMessage" class="assistant-message error-message" role="alert">
          {{ errorMessage }}
        </div>

        <div v-else-if="loading" class="assistant-message">
          <span class="assistant-spinner" aria-hidden="true"></span>
          <span>Loading medication information from OpenFDA...</span>
        </div>

        <div v-else-if="hasSearched && results.length === 0" class="assistant-message">
          No results found. Try a brand or generic medication name.
        </div>

        <div v-if="results.length" class="medication-results">
          <article v-for="result in results" :key="result.id" class="medication-result-card">
            <div class="result-heading">
              <div>
                <p class="eyebrow">Medication</p>
                <h3>{{ result.medicationName || 'Medication label' }}</h3>
              </div>
              <span>{{ result.manufacturer || 'Manufacturer not listed' }}</span>
            </div>

            <dl class="result-grid">
              <div>
                <dt>Generic name</dt>
                <dd>{{ result.genericName || 'Not listed' }}</dd>
              </div>
              <div>
                <dt>Description</dt>
                <dd>{{ result.description || 'Not listed in this label.' }}</dd>
              </div>
              <div>
                <dt>Warnings</dt>
                <dd>{{ result.warnings || 'Not listed in this label.' }}</dd>
              </div>
              <div>
                <dt>Side effects</dt>
                <dd>{{ result.sideEffects || 'Not listed in this label.' }}</dd>
              </div>
              <div>
                <dt>Dosage</dt>
                <dd>{{ result.dosage || 'Not listed in this label.' }}</dd>
              </div>
              <div>
                <dt>Manufacturer</dt>
                <dd>{{ result.manufacturer || 'Manufacturer not listed' }}</dd>
              </div>
            </dl>
          </article>
        </div>
      </div>
    </section>
  </section>
</template>

<script>
import { searchMedicationInfo } from '../services/openFdaService';

export default {
  name: 'MedicationAssistant',
  props: {
    compact: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      isOpen: false,
      query: '',
      results: [],
      loading: false,
      errorMessage: '',
      hasSearched: false,
      activeController: null
    };
  },
  beforeUnmount() {
    this.activeController?.abort();
  },
  methods: {
    openAssistant() {
      this.isOpen = true;
      this.$nextTick(() => {
        const input = this.$el.querySelector('.medication-search input');
        input?.focus();
      });
    },
    openAndSearch() {
      this.isOpen = true;
      this.searchMedication();
    },
    closeAssistant() {
      this.isOpen = false;
      this.activeController?.abort();
      this.loading = false;
    },
    async searchMedication() {
      if (!this.query) {
        return;
      }

      this.activeController?.abort();
      this.activeController = new AbortController();
      this.loading = true;
      this.errorMessage = '';
      this.hasSearched = true;

      try {
        this.results = await searchMedicationInfo(this.query, {
          signal: this.activeController.signal
        });
      } catch (error) {
        if (error.name !== 'AbortError') {
          this.errorMessage = 'OpenFDA is temporarily unavailable. Please try again in a moment.';
          this.results = [];
        }
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.medication-assistant {
  display: grid;
  gap: 12px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 20px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  color: var(--color-text-primary);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.medication-assistant.compact {
  gap: 10px;
  padding: 16px;
}

.medication-assistant.compact h2 {
  font-size: 1.12rem;
}

.medication-assistant:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.medication-assistant header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.medication-assistant h2 {
  margin: 4px 0 0;
  color: var(--color-text-primary);
}

.medication-assistant header button,
.medication-search button {
  min-height: 42px;
  border: none;
  border-radius: 10px;
  padding: 10px 16px;
  background: linear-gradient(135deg, var(--color-brand), #256db0);
  color: #fff;
  cursor: pointer;
  font-weight: 800;
  box-shadow: var(--shadow-sm);
  transition: transform 0.22s ease, box-shadow 0.22s ease, opacity 0.22s ease;
}

.medication-assistant header button:hover,
.medication-search button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.medication-search button:disabled {
  cursor: not-allowed;
  opacity: 0.62;
  transform: none;
}

.assistant-summary,
.assistant-disclaimer {
  color: var(--color-text-secondary);
  font-weight: 600;
  line-height: 1.5;
}

.medication-assistant.compact .assistant-summary {
  font-size: 0.9rem;
}

.medication-assistant.compact .assistant-disclaimer {
  padding: 10px 12px;
  font-size: 0.78rem;
}

.assistant-inline-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.inline-search-field {
  position: relative;
  min-width: 0;
}

.inline-search-field::before {
  position: absolute;
  left: 13px;
  top: 50%;
  width: 12px;
  height: 12px;
  border: 2px solid var(--color-text-tertiary);
  border-radius: 999px;
  content: '';
  transform: translateY(-58%);
}

.inline-search-field::after {
  position: absolute;
  left: 25px;
  top: 50%;
  width: 7px;
  height: 2px;
  border-radius: 999px;
  background: var(--color-text-tertiary);
  content: '';
  transform: translateY(4px) rotate(45deg);
}

.inline-search-field input {
  width: 100%;
  min-height: 42px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 12px 10px 38px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-sm);
}

.assistant-inline-search button {
  min-height: 42px;
  border: none;
  border-radius: 12px;
  padding: 10px 14px;
  background: linear-gradient(135deg, var(--color-brand), #256db0);
  color: #fff;
  cursor: pointer;
  font-weight: 900;
  box-shadow: var(--shadow-sm);
}

.assistant-inline-search button:disabled {
  cursor: not-allowed;
  opacity: 0.62;
}

.assistant-disclaimer {
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 12px;
  background: var(--color-bg-tertiary);
  font-size: 0.88rem;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  z-index: 20;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
}

.modal {
  display: grid;
  gap: 16px;
  border: 1px solid var(--color-border);
  border-radius: 18px;
  padding: 24px;
  background: var(--color-bg-secondary);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  color: var(--color-text-primary);
}

.modal > header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.modal > header > button {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border: none;
  border-radius: 50%;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  cursor: pointer;
  font-weight: 800;
}

.medication-modal {
  width: min(980px, 100%);
  max-height: calc(100vh - 40px);
  overflow: auto;
}

.medication-search {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
}

.search-field {
  position: relative;
  display: grid;
  gap: 8px;
  color: var(--color-text-primary);
  font-weight: 800;
}

.search-field::before {
  position: absolute;
  left: 15px;
  bottom: 16px;
  width: 12px;
  height: 12px;
  border: 2px solid var(--color-text-tertiary);
  border-radius: 999px;
  content: '';
}

.search-field::after {
  position: absolute;
  left: 27px;
  bottom: 14px;
  width: 7px;
  height: 2px;
  border-radius: 999px;
  background: var(--color-text-tertiary);
  content: '';
  transform: rotate(45deg);
}

.search-field input {
  min-height: 48px;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 12px 14px 12px 42px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

.medication-search button {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  min-width: 116px;
  min-height: 48px;
}

.assistant-spinner {
  width: 18px;
  height: 18px;
  border: 3px solid rgba(255, 255, 255, 0.38);
  border-top-color: #fff;
  border-radius: 50%;
  animation: assistant-spin 0.8s linear infinite;
}

.assistant-message {
  display: flex;
  gap: 10px;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 14px 16px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-weight: 800;
}

.assistant-message .assistant-spinner {
  border-color: var(--color-border);
  border-top-color: var(--color-brand);
}

.error-message {
  border-color: var(--color-missed-border);
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.medication-results {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 14px;
}

.medication-result-card {
  display: grid;
  gap: 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 16px;
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.medication-result-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-md);
}

.result-heading {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.result-heading h3 {
  margin: 4px 0 0;
  color: var(--color-text-primary);
  font-size: 1.12rem;
}

.result-heading > span {
  max-width: 180px;
  border-radius: 999px;
  padding: 6px 10px;
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  font-size: 0.74rem;
  font-weight: 900;
  line-height: 1.25;
  text-align: right;
}

.result-grid {
  display: grid;
  gap: 10px;
  margin: 0;
}

.result-grid div {
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 11px 12px;
  background: var(--color-bg-secondary);
}

.result-grid dt {
  color: var(--color-text-tertiary);
  font-size: 0.72rem;
  font-weight: 900;
  text-transform: uppercase;
}

.result-grid dd {
  margin: 4px 0 0;
  color: var(--color-text-primary);
  font-size: 0.9rem;
  font-weight: 650;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

@keyframes assistant-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 640px) {
  .medication-search,
  .assistant-inline-search,
  .result-heading {
    grid-template-columns: 1fr;
  }

  .medication-search {
    display: grid;
  }

  .medication-search button,
  .assistant-inline-search button,
  .result-heading > span {
    width: 100%;
    max-width: none;
    text-align: left;
  }
}
</style>
