<template>
  <section class="panel verification-history-panel" aria-label="Medication verification history">
    <header>
      <h2>Verification history</h2>
      <span>{{ logs.length }} entries</span>
    </header>

    <div v-if="logs.length === 0" class="empty-state">
      No medication verifications have been recorded yet.
    </div>

    <ul v-else class="verification-list">
      <li v-for="log in logs" :key="log.id" class="verification-item">
        <div class="verification-meta">
          <strong>{{ log.medicationId }}</strong>
          <span>{{ log.method.toUpperCase() }}</span>
        </div>
        <div class="verification-details">
          <p>{{ log.admin }}</p>
          <p>{{ formattedTime(log.time) }}</p>
        </div>
      </li>
    </ul>
  </section>
</template>

<script>
import { formatDateTime } from '../utils/formatters';

export default {
  name: 'VerificationHistoryPanel',
  props: {
    logs: {
      type: Array,
      required: true
    }
  },
  methods: {
    formattedTime(value) {
      return formatDateTime(value);
    }
  }
};
</script>

<style scoped>
.verification-history-panel {
  border-radius: 16px;
  border: 1px solid rgba(66, 97, 135, 0.1);
  background: var(--color-bg-secondary);
  padding: 18px;
  box-shadow: var(--shadow-sm);
}

.verification-history-panel header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.verification-history-panel h2 {
  margin: 0;
  font-size: 1.1rem;
}

.verification-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.verification-item {
  padding: 14px;
  border-radius: 14px;
  background: var(--color-bg-tertiary);
  border: 1px solid rgba(49, 130, 206, 0.12);
}

.verification-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-weight: 700;
}

.verification-details {
  display: grid;
  gap: 4px;
  color: var(--color-text-secondary);
  font-size: 0.92rem;
}

.empty-state {
  color: var(--color-text-secondary);
  padding: 14px 0;
}
</style>
