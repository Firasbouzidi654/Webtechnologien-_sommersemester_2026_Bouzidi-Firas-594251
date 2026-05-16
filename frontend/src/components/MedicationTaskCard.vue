<template>
  <article class="task-card" :class="statusClass">
    <header>
      <div>
        <span class="time">{{ task.scheduledTime }}</span>
        <h3>{{ task.childName }}</h3>
        <p>{{ task.groupName }} - {{ task.medicationName }}</p>
      </div>
      <span class="status">{{ task.status }}</span>
    </header>

    <dl>
      <div>
        <dt>Medication ID</dt>
        <dd>{{ task.medicationId }}</dd>
      </div>
      <div>
        <dt>Dosage</dt>
        <dd>{{ task.dosage }}</dd>
      </div>
    </dl>

    <p class="instructions">{{ task.instructions }}</p>
    <p v-if="task.reminderDue" class="reminder">Reminder: scheduled medication time has arrived.</p>

    <button
      class="confirm-button"
      type="button"
      :disabled="task.status === 'Taken'"
      @click="$emit('confirm', task.medicationId)"
    >
      Confirm taken
    </button>
  </article>
</template>

<script>
export default {
  name: 'MedicationTaskCard',
  props: {
    task: {
      type: Object,
      required: true
    }
  },
  emits: ['confirm'],
  computed: {
    statusClass() {
      return `status-${this.task.status.toLowerCase()}`;
    }
  }
};
</script>

<style scoped>
.task-card {
  display: grid;
  gap: 14px;
  border: 1px solid var(--color-border);
  border-left: 5px solid var(--color-bg-tertiary);
  border-radius: 8px;
  padding: 16px;
  background: var(--color-bg-secondary);
}

.status-pending {
  border-left-color: var(--color-pending-text);
}

.status-taken {
  border-left-color: var(--color-taken-text);
}

.status-missed {
  border-left-color: var(--color-missed-text);
}

header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.time,
.status {
  display: inline-flex;
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 0.78rem;
  font-weight: 900;
}

.time {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.status {
  height: fit-content;
  background: var(--color-bg-tertiary);
}

.status-pending .status {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.status-taken .status {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.status-missed .status {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

h3,
p,
dl,
dd {
  margin: 0;
}

h3 {
  margin-top: 8px;
  font-size: 1.1rem;
}

header p,
dt {
  color: var(--color-text-secondary);
}

dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

dt {
  font-size: 0.78rem;
  font-weight: 800;
}

dd {
  font-weight: 800;
}

.instructions {
  color: var(--color-text-secondary);
}

.reminder {
  border-radius: 8px;
  padding: 10px 12px;
  background: var(--color-pending);
  color: var(--color-pending-text);
  font-weight: 800;
}

.confirm-button {
  min-height: 42px;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  background: linear-gradient(135deg, var(--color-success), #2f855a);
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

.confirm-button:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  cursor: not-allowed;
}
</style>
