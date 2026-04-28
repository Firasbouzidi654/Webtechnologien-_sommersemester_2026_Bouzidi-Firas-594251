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
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-left: 5px solid #d7e0e7;
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.status-pending {
  border-left-color: #f0a83a;
}

.status-taken {
  border-left-color: #2d8f7b;
}

.status-missed {
  border-left-color: #d94a4a;
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
  background: #edf2f7;
  color: #405265;
}

.status {
  height: fit-content;
  background: #edf2f7;
}

.status-pending .status {
  background: #fff1d6;
  color: #8a5b00;
}

.status-taken .status {
  background: #e9f6f2;
  color: #287b68;
}

.status-missed .status {
  background: #ffe3e3;
  color: #a12d2d;
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
  color: #637486;
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
  color: #405265;
}

.reminder {
  border-radius: 8px;
  padding: 10px 12px;
  background: #fff1d6;
  color: #7d5100;
  font-weight: 800;
}

.confirm-button {
  min-height: 42px;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  background: #2d8f7b;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

.confirm-button:disabled {
  background: #cad5dd;
  color: #637486;
  cursor: not-allowed;
}
</style>
