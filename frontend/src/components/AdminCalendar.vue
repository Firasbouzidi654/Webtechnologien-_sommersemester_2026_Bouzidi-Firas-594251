p<template>
  <section class="calendar-panel">
    <header>
      <div>
        <p class="eyebrow">Medication calendar</p>
        <h2>{{ monthLabel }}</h2>
      </div>
      <span>{{ todayLabel }}</span>
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
        @click="selectedDate = day.date"
      >
        <span>{{ day.dayNumber }}</span>
        <small v-if="tasksForDate(day.date).length">{{ tasksForDate(day.date).length }} task(s)</small>
      </button>
    </div>

    <section class="day-plan">
      <h3>{{ selectedDateLabel }}</h3>
      <article v-for="task in selectedTasks" :key="task.taskId" class="calendar-task" :class="task.status.toLowerCase()">
        <strong>{{ task.scheduledTime }}</strong>
        <span>{{ task.childName }} - {{ task.medicationName }}</span>
        <em>{{ task.status }}</em>
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
      weekdays: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
    };
  },
  computed: {
    today() {
      return new Date();
    },
    monthLabel() {
      return this.today.toLocaleDateString([], { month: 'long', year: 'numeric' });
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
      const year = this.today.getFullYear();
      const month = this.today.getMonth();
      const firstDay = new Date(year, month, 1);
      const startOffset = (firstDay.getDay() + 6) % 7;
      const firstVisibleDay = new Date(year, month, 1 - startOffset);

      return Array.from({ length: 35 }, (_, index) => {
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
    tasksForDate(dateKey) {
      const todayKey = this.toDateKey(this.today);
      const tomorrow = new Date(this.today);
      tomorrow.setDate(this.today.getDate() + 1);
      const tomorrowKey = this.toDateKey(tomorrow);

      if (dateKey === todayKey) {
        return this.tasks;
      }

      if (dateKey === tomorrowKey) {
        return this.tasks
          .filter((task) => task.status !== 'Missed')
          .map((task) => ({ ...task, status: 'Pending' }));
      }

      return [];
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

header span {
  border-radius: 999px;
  padding: 5px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 900;
  white-space: nowrap;
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

.calendar-task {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  border-left: 4px solid var(--color-pending-text);
  border-radius: 8px;
  padding: 9px 10px;
  background: var(--color-bg-primary);
}

.calendar-task.taken {
  border-left-color: var(--color-taken-text);
}

.calendar-task.missed {
  border-left-color: var(--color-missed-text);
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

@media (max-width: 520px) {
  .calendar-task {
    grid-template-columns: 1fr;
  }
}
</style>
