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
      return this.calendarCursor.toLocaleDateString([], { month: 'long', year: 'numeric' });
    },
    todayLabel() {
      return this.today.toLocaleDateString([], { weekday: 'long', day: 'numeric', month: 'short' });
    },
    selectedDateLabel() {
      return new Date(`${this.selectedDate}T12:00:00`).toLocaleDateString([], {
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

    tasksForDate(dateKey) {
      if (!Array.isArray(this.tasks)) return [];
      return this.tasks
        .filter((task) => (task?.scheduledDate || this.toDateKey(this.today)) === dateKey)
        .sort((first, second) => (first.scheduledTime || '').localeCompare(second.scheduledTime || ''));
    }
  }
};
</script>

<style scoped>
.calendar-panel {
  display: grid;
  gap: 14px;
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
  grid-template-columns: 48px minmax(0, 1fr) auto auto;
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

@media (max-width: 520px) {
  .calendar-task {
    grid-template-columns: 1fr;
  }
}
</style>
