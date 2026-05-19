<template>
  <section class="live-dashboard-bar" aria-label="Live dashboard information">
    <article class="live-time-card widget-live" :class="{ 'dark-mode': isDark }">
      <span class="info-icon live-time-icon live-icon" :class="{ 'dark-mode': isDark }" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M12 7v5l3 2" />
          <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
        </svg>
      </span>
      <div>
        <p>Live time</p>
        <strong>{{ liveTime }}</strong>
      </div>
    </article>
    <slot></slot>
    <article class="today-card date-widget widget-today" :class="{ 'dark-mode': isDark }">
      <span class="info-icon today-icon" :class="{ 'dark-mode': isDark }" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M8 2v4" />
          <path d="M16 2v4" />
          <path d="M3 10h18" />
          <path d="M5 4h14a2 2 0 0 1 2 2v13a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2Z" />
        </svg>
      </span>
      <div>
        <p>Today</p>
        <strong>{{ weekday }}</strong>
        <small>{{ compactDate }}</small>
      </div>
    </article>
    <article class="staff-card widget-shift" :class="{ 'dark-mode': isDark }">
      <span class="info-icon staff-icon" :class="{ 'dark-mode': isDark }" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" />
          <path d="M9.5 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
          <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
      </span>
      <div>
        <p>Active staff shift</p>
        <strong>{{ activeShiftLabel }}</strong>
      </div>
    </article>
    <article class="children-card widget-children" :class="{ 'dark-mode': isDark }">
      <span class="info-icon children-icon" :class="{ 'dark-mode': isDark }" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M16 19v-1a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v1" />
          <path d="M10 10a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
          <path d="m17 11 2 2 4-4" />
        </svg>
      </span>
      <div>
        <p>Children present</p>
        <strong>{{ presentLabel }}</strong>
      </div>
    </article>
  </section>
</template>

<script>
export default {
  name: 'LiveDashboardBar',
  props: {
    childrenCount: {
      type: Number,
      default: 0
    },
    shiftLabel: {
      type: String,
      default: 'Morning care team'
    },
    isDark: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      now: new Date(),
      timerId: null
    };
  },
  computed: {
    liveTime() {
      return this.now.toLocaleTimeString('en-US', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    weekday() {
      return this.now.toLocaleDateString('en-US', { weekday: 'long' });
    },
    currentDate() {
      return this.now.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    },
    compactDate() {
      return this.now.toLocaleDateString('en-US', {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      });
    },
    presentLabel() {
      return `${this.childrenCount} checked in`;
    },
    activeShiftLabel() {
      const hour = this.now.getHours();

      if (hour >= 6 && hour < 12) {
        return 'Morning care team';
      }

      if (hour >= 12 && hour < 18) {
        return 'Afternoon care team';
      }

      return 'Night support team';
    }
  },
  mounted() {
    this.timerId = window.setInterval(() => {
      this.now = new Date();
    }, 1000);
  },
  beforeUnmount() {
    window.clearInterval(this.timerId);
  }
};
</script>

<style scoped>
.live-dashboard-bar {
  display: grid;
  grid-template-columns: repeat(5, minmax(160px, 1fr));
  gap: 10px;
  max-width: 1240px;
  margin: 10px auto 0;
  align-items: stretch;
}

.live-dashboard-bar article {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  min-width: 0;
  min-height: 90px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 18px;
  padding: 14px 16px;
  background: transparent;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
  transition: all 0.25s ease;
}

.live-dashboard-bar article:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.live-dashboard-bar .live-time-card {
  background: var(--gradient-live-card) !important;
  color: #111827;
}

.live-dashboard-bar .live-time-card p,
.live-dashboard-bar .live-time-card strong,
.live-dashboard-bar .live-time-card small {
  color: #111827;
}

.live-dashboard-bar .today-card {
  background: var(--gradient-today-card) !important;
  color: #111827;
}

.live-dashboard-bar .staff-card {
  background: var(--gradient-staff-card) !important;
  color: #111827;
}

.live-dashboard-bar .children-card {
  background: var(--gradient-children-card) !important;
  color: #111827;
}

.info-icon {
  flex: 0 0 36px;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.78);
  color: #0f172a;
  font-size: 0.8rem;
  font-weight: 800;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.74), 0 6px 12px rgba(15, 23, 42, 0.07);
}

.live-dashboard-bar .live-time-icon {
  background: rgba(34, 197, 94, 0.18) !important;
  color: #166534;
}

.live-dashboard-bar .today-icon {
  background: rgba(139, 92, 246, 0.18) !important;
  color: #5b46a4;
}

.live-dashboard-bar .staff-icon {
  background: rgba(99, 102, 241, 0.18) !important;
  color: #3730a3;
}

.live-dashboard-bar .children-icon {
  background: rgba(245, 158, 11, 0.18) !important;
  color: #92400e;
}

.info-icon svg {
  width: 17px;
  height: 17px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 2;
}

.live-icon {
  position: relative;
  background: rgba(34, 197, 94, 0.18) !important;
  color: #166534 !important;
}

.live-icon::after {
  position: absolute;
  right: 5px;
  top: 5px;
  width: 9px;
  height: 9px;
  border-radius: 999px;
  background: var(--color-taken-border);
  content: '';
  animation: pulse-live 1.4s ease-in-out infinite;
}

.live-dashboard-bar p,
.live-dashboard-bar strong,
.live-dashboard-bar small {
  margin: 0;
}

.live-dashboard-bar p {
  color: #64748b;
  font-size: 0.68rem;
  font-weight: 700;
  text-transform: uppercase;
  line-height: 1.2;
}

.live-dashboard-bar strong {
  display: block;
  margin-top: 4px;
  color: #111827;
  font-size: 0.95rem;
  line-height: 1.2;
  overflow-wrap: anywhere;
}

.date-widget strong {
  font-size: 0.98rem;
}

.date-widget small {
  display: block;
  margin-top: 2px;
  color: var(--color-text-tertiary);
  font-size: 0.68rem;
  font-weight: 800;
  line-height: 1.15;
  text-transform: uppercase;
}

:global([data-theme="dark"]) .live-dashboard-bar article,
.live-dashboard-bar article.dark-mode {
  color: #f8fafc;
  border: 1px solid rgba(255, 255, 255, 0.06);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) .live-dashboard-bar article:hover,
.live-dashboard-bar article.dark-mode:hover {
  transform: translateY(-2px);
  filter: brightness(1.05);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.45);
}

:global([data-theme="dark"]) .live-dashboard-bar .live-time-card,
.live-dashboard-bar .live-time-card.dark-mode {
  background: var(--gradient-live-card) !important;
}

:global([data-theme="dark"]) .live-dashboard-bar .today-card,
.live-dashboard-bar .today-card.dark-mode {
  background: var(--gradient-today-card) !important;
}

:global([data-theme="dark"]) .live-dashboard-bar .staff-card,
.live-dashboard-bar .staff-card.dark-mode {
  background: var(--gradient-staff-card) !important;
}

:global([data-theme="dark"]) .live-dashboard-bar .children-card,
.live-dashboard-bar .children-card.dark-mode {
  background: var(--gradient-children-card) !important;
}

:global([data-theme="dark"]) .live-dashboard-bar p,
:global([data-theme="dark"]) .live-dashboard-bar strong,
:global([data-theme="dark"]) .live-dashboard-bar small,
.live-dashboard-bar article.dark-mode p,
.live-dashboard-bar article.dark-mode strong,
.live-dashboard-bar article.dark-mode small {
  color: #f8fafc;
}

:global([data-theme="dark"]) .live-dashboard-bar p,
.live-dashboard-bar article.dark-mode p {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .date-widget small,
.live-dashboard-bar article.dark-mode small {
  color: #94a3b8;
}

:global([data-theme="dark"]) .live-dashboard-bar .live-time-icon,
.live-dashboard-bar .live-time-icon.dark-mode {
  background: rgba(34, 197, 94, 0.22) !important;
  color: #bbf7d0 !important;
}

:global([data-theme="dark"]) .live-dashboard-bar .today-icon,
.live-dashboard-bar .today-icon.dark-mode {
  background: rgba(139, 92, 246, 0.22) !important;
  color: #ddd6fe !important;
}

:global([data-theme="dark"]) .live-dashboard-bar .staff-icon,
.live-dashboard-bar .staff-icon.dark-mode {
  background: rgba(99, 102, 241, 0.22) !important;
  color: #c7d2fe !important;
}

:global([data-theme="dark"]) .live-dashboard-bar .children-icon,
.live-dashboard-bar .children-icon.dark-mode {
  background: rgba(245, 158, 11, 0.22) !important;
  color: #fde68a !important;
}

@keyframes pulse-live {
  0%,
  100% {
    transform: scale(0.8);
    opacity: 0.7;
  }

  50% {
    transform: scale(1.18);
    opacity: 1;
  }
}

@media (max-width: 980px) {
  .live-dashboard-bar {
    grid-template-columns: repeat(auto-fit, minmax(170px, 1fr));
  }
}

@media (max-width: 560px) {
  .live-dashboard-bar {
    grid-template-columns: 1fr;
  }
}
</style>
