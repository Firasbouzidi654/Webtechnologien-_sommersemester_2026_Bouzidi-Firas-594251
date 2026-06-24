<template>
  <article class="task-card" :class="statusClass">
    <header>
      <div class="task-heading">
        <span class="time">{{ task.scheduledTime || '--:--' }}</span>
        <h3>{{ task.childName || 'Unknown child' }}</h3>
        <p>{{ task.medicationName || 'Medication' }}</p>
      </div>
      <span class="status">{{ normalizedStatus }}</span>
    </header>

    <dl>
      <div>
        <dt>Medication ID</dt>
        <dd>{{ task.medicationId || 'N/A' }}</dd>
      </div>
      <div>
        <dt>Dosage</dt>
        <dd>{{ task.dosage || 'Not set' }}</dd>
      </div>
    </dl>

    <p class="instructions">{{ task.instructions || 'No notes added.' }}</p>
    <p class="schedule-frequency">{{ frequencyLabel }}</p>
    <p v-if="task.reminderDue" class="reminder">Reminder: scheduled medication time has arrived.</p>

    <div class="status-control" aria-label="Medication status">
      <button
        v-for="status in statuses"
        :key="status"
        type="button"
        class="status-option"
        :class="[`status-${status.toLowerCase()}`, { active: normalizedStatus === status }]"
        @click="$emit('status-change', { medicationId: task.medicationId, status })"
      >
        {{ status }}
      </button>
    </div>

    <div class="card-actions">
      <button
        class="confirm-button"
        type="button"
        :disabled="task.status === 'Taken'"
        @click="$emit('confirm', task.medicationId)"
      >
        Confirm taken
      </button>
      <button class="secondary-button" type="button" @click="$emit('edit', task.medicationId)">Edit</button>
      <button class="delete-button" type="button" @click="$emit('delete', task.medicationId)">Delete</button>
    </div>
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
  emits: ['confirm', 'edit', 'delete', 'status-change'],
  data() {
    return {
      statuses: ['Pending', 'Taken', 'Missed', 'Upcoming']
    };
  },
  computed: {
    normalizedStatus() {
      return this.statuses.includes(this.task?.status) ? this.task.status : 'Pending';
    },
    statusClass() {
      return `status-${this.normalizedStatus.toLowerCase()}`;
    },
    frequencyLabel() {
      if (this.task.frequency === 'EVERY_X_DAYS') return `Every ${this.task.intervalDays || 2} days`;
      if (this.task.frequency === 'SPECIFIC_DAY') {
        const day = String(this.task.dayOfWeek || 'MONDAY').toLowerCase();
        return `Every ${day.replace(/^./, (letter) => letter.toUpperCase())}`;
      }
      return { DAILY: 'Daily', WEEKLY: 'Weekly', ONE_TIME: 'One-time' }[this.task.frequency] || 'Daily';
    }
  }
};
</script>

<style scoped>
.task-card {
  display: grid;
  gap: 16px;
  border: 1px solid var(--color-border);
  border-left: 6px solid var(--color-bg-tertiary);
  border-radius: 14px;
  padding: 18px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.04), transparent 34%),
    var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease, background-color 0.22s ease;
}

.task-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-lg);
}

.status-upcoming {
  border-left-color: var(--color-upcoming-border);
}

.status-pending {
  border-left-color: var(--color-pending-border);
}

.status-taken {
  border-left-color: var(--color-taken-border);
}

.status-missed {
  border-left-color: var(--color-missed-border);
}

header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.task-heading {
  min-width: 0;
}

.time,
.status {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 0.78rem;
  font-weight: 900;
  line-height: 1.2;
  white-space: nowrap;
}

.time {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.status {
  height: fit-content;
  background: var(--color-bg-tertiary);
  border: 1px solid transparent;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.status-pending .status {
  background: var(--color-pending);
  color: var(--color-pending-text);
  border-color: color-mix(in srgb, var(--color-pending-border) 45%, transparent);
}

.status-upcoming .status {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  border-color: color-mix(in srgb, var(--color-upcoming-border) 45%, transparent);
}

.status-taken .status {
  background: var(--color-taken);
  color: var(--color-taken-text);
  border-color: color-mix(in srgb, var(--color-taken-border) 45%, transparent);
}

.status-missed .status {
  background: var(--color-missed);
  color: var(--color-missed-text);
  border-color: color-mix(in srgb, var(--color-missed-border) 45%, transparent);
}

h3,
p,
dl,
dd {
  margin: 0;
}

h3 {
  margin-top: 10px;
  color: var(--color-text-primary);
  font-size: 1.08rem;
  line-height: 1.25;
}

header p,
dt {
  color: var(--color-text-secondary);
}

header p {
  margin-top: 3px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

dl {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

dl > div {
  min-width: 0;
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
}

dt {
  font-size: 0.78rem;
  font-weight: 800;
}

dd {
  font-weight: 800;
  overflow-wrap: anywhere;
}

.instructions {
  border-radius: 12px;
  padding: 12px 14px;
  background: var(--color-bg-primary);
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.schedule-frequency {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 0.84rem;
  font-weight: 800;
}

.reminder {
  border-radius: 8px;
  padding: 10px 12px;
  background: var(--color-pending);
  color: var(--color-pending-text);
  font-weight: 800;
}

.status-control {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
}

.status-option {
  min-height: 38px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 8px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 900;
  box-shadow: 0 5px 14px rgba(15, 23, 42, 0.04);
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease, filter 0.18s ease;
}

.status-option:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  filter: saturate(1.06);
}

.status-option.active {
  border-color: currentColor;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.12), var(--shadow-sm);
}

.status-option.status-upcoming {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.status-option.status-pending {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.status-option.status-taken {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.status-option.status-missed {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.confirm-button {
  min-height: 42px;
  border: none;
  border-radius: 10px;
  padding: 10px 16px;
  background: linear-gradient(135deg, var(--color-success), #2f855a);
  color: #fff;
  font-weight: 900;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(56, 161, 105, 0.18);
  transition: transform 0.18s ease, box-shadow 0.18s ease, opacity 0.18s ease;
}

.confirm-button:hover:not(:disabled),
.secondary-button:hover,
.delete-button:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.confirm-button:disabled {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  cursor: not-allowed;
}

.card-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.secondary-button,
.delete-button {
  min-height: 42px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 10px 16px;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.secondary-button {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  border-color: color-mix(in srgb, var(--color-upcoming-border) 40%, transparent);
}

.delete-button {
  background: var(--color-missed);
  color: var(--color-missed-text);
  border-color: color-mix(in srgb, var(--color-missed-border) 40%, transparent);
}

@media (max-width: 640px) {
  header,
  dl {
    grid-template-columns: 1fr;
  }

  header {
    display: grid;
  }

  .status-control {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .card-actions > button {
    flex: 1 1 150px;
  }
}
</style>
