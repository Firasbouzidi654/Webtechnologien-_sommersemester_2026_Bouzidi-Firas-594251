<template>
  <section class="calendar-panel">
    <header>
      <div>
        <p class="eyebrow">Medication calendar</p>
        <h2>{{ monthLabel }}</h2>
      </div>
      <div class="calendar-actions">
        <button type="button" aria-label="Previous month" @click="moveMonth(-1)">‹</button>
        <span>{{ todayLabel }}</span>
        <button type="button" aria-label="Next month" @click="moveMonth(1)">›</button>
      </div>
    </header>

    <div class="weekdays">
      <strong v-for="day in weekdays" :key="day">{{ day }}</strong>
    </div>

    <div class="calendar-grid">
      <button
        v-for="day in calendarDays"
        :key="`${day.date}-${day.inMonth}`"
        class="day-cell"
        :class="{ muted: !day.inMonth, today: day.isToday, selected: day.date === selectedDate }"
        type="button"
        @click="onDayClick(day)"
      >
        <span>{{ day.dayNumber }}</span>
        <small v-if="tasksForDate(day.date).length">{{ tasksForDate(day.date).length }} task(s)</small>
      </button>
    </div>

    <section class="day-plan">
      <div class="day-plan-header">
        <h3>{{ selectedDateLabel }}</h3>
        <button type="button" @click="$emit('create-task', selectedDate)">Add task</button>
      </div>
      <article v-for="task in selectedTasks" :key="task.taskId || task.medicationId" class="calendar-task" :class="statusClass(task.status)">
        <strong>{{ task.scheduledTime || '--:--' }}</strong>
        <span>{{ task.childName || 'Unknown child' }} - {{ task.medicationName || 'Medication' }}</span>
        <small>{{ task.scheduledDate }}</small>
        <em>{{ normalizedStatus(task.status) }}</em>
        <div class="task-actions">
          <button type="button" @click="$emit('edit-task', task.medicationId)">Edit</button>
          <button type="button" @click="$emit('delete-task', task.medicationId)">Delete</button>
        </div>
      </article>
      <p v-if="selectedTasks.length === 0" class="empty">No medication tasks planned.</p>
    </section>
  </section>
</template>

<script>
export default {
  name: 'AdminCalendar',
  props: {
    tasks: {
      type: Array,
      required: true
    }
  },
  data() {
    const today = new Date();
    return {
      selectedDate: this.toDateKey(today),
      calendarCursor: new Date(today.getFullYear(), today.getMonth(), 1),
      weekdays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    };
  },
  computed: {
    today() {
      return new Date();
    },
    monthLabel() {
      return this.calendarCursor.toLocaleDateString('en-US', { month: 'long', year: 'numeric' });
    },
    todayLabel() {
      return this.today.toLocaleDateString('en-US', { weekday: 'long', day: 'numeric', month: 'short' });
    },
    selectedDateLabel() {
      return new Date(`${this.selectedDate}T12:00:00`).toLocaleDateString('en-US', {
        weekday: 'long',
        month: 'short',
        day: 'numeric'
      });
    },
    calendarDays() {
      const year = this.calendarCursor.getFullYear();
      const month = this.calendarCursor.getMonth();
      const firstDay = new Date(year, month, 1);
      const startOffset = (firstDay.getDay() + 6) % 7;
      const firstVisibleDay = new Date(year, month, 1 - startOffset);

      return Array.from({ length: 42 }, (_, index) => {
        const date = new Date(firstVisibleDay);
        date.setDate(firstVisibleDay.getDate() + index);
        const dateKey = this.toDateKey(date);

        return {
          date: dateKey,
          dayNumber: date.getDate(),
          inMonth: date.getMonth() === month,
          isToday: dateKey === this.toDateKey(this.today)
        };
      });
    },
    selectedTasks() {
      return this.tasksForDate(this.selectedDate);
    }
  },
  methods: {
    toDateKey(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    onDayClick(day) {
      this.selectedDate = day.date;
      this.$emit('day-click', day.date);
    },
    moveMonth(direction) {
      this.calendarCursor = new Date(
        this.calendarCursor.getFullYear(),
        this.calendarCursor.getMonth() + direction,
        1
      );
    },
    normalizedStatus(status) {
      return ['Pending', 'Taken', 'Missed', 'Upcoming'].includes(status) ? status : 'Pending';
    },
    statusClass(status) {
      return this.normalizedStatus(status).toLowerCase();
    },
    isTaskScheduledForDate(task, dateKey) {
      return (task.scheduledDate || this.toDateKey(this.today)) === dateKey;
    },

    tasksForDate(dateKey) {
      if (!Array.isArray(this.tasks)) return [];
      return this.tasks
        .filter((task) => this.isTaskScheduledForDate(task, dateKey))
        .sort((first, second) => (first.scheduledTime || '').localeCompare(second.scheduledTime || ''));
    }
  }
};
</script>

<style scoped>
.calendar-panel {
  display: grid;
  gap: 14px;
  min-width: 0;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 18px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-md);
}

header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.eyebrow,
h2,
h3,
.empty {
  margin: 0;
}

.eyebrow {
  color: var(--color-brand);
  font-size: 0.76rem;
  font-weight: 900;
  text-transform: uppercase;
}

.calendar-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.calendar-actions span {
  border-radius: 999px;
  padding: 5px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 900;
  white-space: nowrap;
}

.calendar-actions button,
.day-plan-header button,
.task-actions button {
  min-height: 32px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 6px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  cursor: pointer;
  font-weight: 900;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.calendar-actions button:hover,
.day-plan-header button:hover,
.task-actions button:hover,
.day-cell:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

.weekdays,
.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 6px;
}

.weekdays strong {
  color: var(--color-text-secondary);
  font-size: 0.72rem;
  text-align: center;
}

.day-cell {
  display: grid;
  min-height: 58px;
  place-items: center;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 6px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.day-cell span {
  font-weight: 900;
}

.day-cell small {
  color: var(--color-brand);
  font-size: 0.68rem;
  font-weight: 900;
}

.day-cell.muted {
  opacity: 0.42;
}

.day-cell.today {
  border-color: var(--color-success);
  background: var(--color-taken);
}

.day-cell.selected {
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.3);
}

.day-plan {
  display: grid;
  gap: 8px;
  border-top: 1px solid var(--color-border);
  padding-top: 14px;
}

.day-plan-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.calendar-task {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto auto auto;
  gap: 8px;
  align-items: center;
  border-left: 4px solid var(--color-pending-border);
  border-radius: 8px;
  padding: 9px 10px;
  background: var(--color-bg-primary);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.calendar-task:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-sm);
}

.calendar-task.upcoming {
  border-left-color: var(--color-upcoming-border);
}

.calendar-task.taken {
  border-left-color: var(--color-taken-border);
}

.calendar-task.missed {
  border-left-color: var(--color-missed-border);
}

.calendar-task span {
  overflow-wrap: anywhere;
  color: var(--color-text-secondary);
}

.calendar-task strong {
  color: var(--color-text-primary);
}

.calendar-task em,
.calendar-task small,
.empty {
  color: var(--color-text-tertiary);
  font-size: 0.76rem;
  font-style: normal;
  font-weight: 900;
}

.calendar-task em {
  border-radius: 999px;
  padding: 5px 9px;
  box-shadow: 0 5px 12px rgba(15, 23, 42, 0.06);
}

.calendar-task small {
  color: var(--color-text-secondary);
  white-space: nowrap;
}

.calendar-task.pending em {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.calendar-task.upcoming em {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.calendar-task.taken em {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.calendar-task.missed em {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.task-actions {
  display: flex;
  gap: 6px;
  justify-content: end;
  flex-wrap: wrap;
}

:global([data-theme="dark"]) .calendar-panel {
  border-color: rgba(255, 255, 255, 0.1) !important;
  background: linear-gradient(135deg, #111827 0%, #1e293b 100%) !important;
  color: #f8fafc !important;
}

:global([data-theme="dark"]) .calendar-panel h2,
:global([data-theme="dark"]) .calendar-panel h3,
:global([data-theme="dark"]) .calendar-task strong,
:global([data-theme="dark"]) .day-cell span {
  color: #f8fafc !important;
  -webkit-text-fill-color: #f8fafc !important;
}

:global([data-theme="dark"]) .weekdays strong,
:global([data-theme="dark"]) .calendar-actions span,
:global([data-theme="dark"]) .calendar-task span,
:global([data-theme="dark"]) .calendar-task small,
:global([data-theme="dark"]) .empty {
  color: #cbd5e1 !important;
  -webkit-text-fill-color: #cbd5e1 !important;
}

:global([data-theme="dark"]) .day-cell,
:global([data-theme="dark"]) .calendar-task,
:global([data-theme="dark"]) .calendar-actions span,
:global([data-theme="dark"]) .calendar-actions button,
:global([data-theme="dark"]) .day-plan-header button,
:global([data-theme="dark"]) .task-actions button {
  border-color: rgba(255, 255, 255, 0.1) !important;
  background: rgba(255, 255, 255, 0.07) !important;
  color: #f8fafc !important;
}

:global([data-theme="dark"]) .day-cell small {
  color: #93c5fd !important;
  -webkit-text-fill-color: #93c5fd !important;
}

:global([data-theme="dark"]) .day-cell.muted {
  opacity: 0.7;
}

:global([data-theme="dark"]) .day-cell.today {
  border-color: var(--color-taken-border) !important;
  background: var(--color-taken) !important;
}

:global([data-theme="dark"]) .day-cell.selected {
  border-color: #93c5fd !important;
  box-shadow: 0 0 0 3px rgba(147, 197, 253, 0.24);
}

:global([data-theme="dark"]) .calendar-task.pending em {
  background: var(--color-pending) !important;
  color: var(--color-pending-text) !important;
  -webkit-text-fill-color: var(--color-pending-text) !important;
}

:global([data-theme="dark"]) .calendar-task.upcoming em {
  background: var(--color-upcoming) !important;
  color: var(--color-upcoming-text) !important;
  -webkit-text-fill-color: var(--color-upcoming-text) !important;
}

:global([data-theme="dark"]) .calendar-task.taken em {
  background: var(--color-taken) !important;
  color: var(--color-taken-text) !important;
  -webkit-text-fill-color: var(--color-taken-text) !important;
}

:global([data-theme="dark"]) .calendar-task.missed em {
  background: var(--color-missed) !important;
  color: var(--color-missed-text) !important;
  -webkit-text-fill-color: var(--color-missed-text) !important;
}

@media (max-width: 640px) {
  .calendar-panel {
    padding: 14px;
  }

  header,
  .day-plan-header {
    align-items: stretch;
    flex-direction: column;
  }

  .calendar-actions {
    justify-content: stretch;
  }

  .calendar-actions span {
    flex: 1 1 100%;
    text-align: center;
  }

  .calendar-actions button,
  .day-plan-header button {
    flex: 1 1 0;
  }

  .calendar-task {
    grid-template-columns: 1fr;
  }

  .task-actions button {
    flex: 1 1 120px;
  }

  .day-cell {
    min-height: 48px;
    padding: 4px;
  }

  .day-cell small {
    display: none;
  }
}

@media (max-width: 480px) {
  .calendar-panel {
    gap: 10px;
    padding: 12px;
    border-radius: 14px;
  }

  header,
  .day-plan-header {
    gap: 8px;
  }

  .calendar-actions {
    gap: 6px;
  }

  .calendar-actions button,
  .day-plan-header button,
  .task-actions button {
    min-height: 34px;
    padding: 6px 9px;
  }

  .day-cell {
    min-height: 44px;
    border-radius: 8px;
  }

  .selected-task,
  .calendar-task {
    gap: 7px;
    padding: 8px;
  }
}

@media (max-width: 420px) {
  .weekdays,
  .calendar-grid {
    gap: 4px;
  }

  .weekdays strong {
    font-size: 0.66rem;
  }

  .day-cell {
    min-height: 42px;
    border-radius: 6px;
  }
}
</style>
