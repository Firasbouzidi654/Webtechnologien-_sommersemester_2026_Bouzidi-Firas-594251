<template>
  <section class="live-dashboard-bar" aria-label="Live dashboard information">
    <article class="widget-live">
      <span class="info-icon live-icon" aria-hidden="true">
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
    <article class="date-widget widget-today">
      <span class="info-icon" aria-hidden="true">
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
    <article class="widget-shift">
      <span class="info-icon" aria-hidden="true">
        <svg viewBox="0 0 24 24">
          <path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" />
          <path d="M9.5 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
          <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
          <path d="M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
      </span>
      <div>
        <p>Active staff shift</p>
        <strong>{{ shiftLabel }}</strong>
      </div>
    </article>
    <article class="widget-children">
      <span class="info-icon" aria-hidden="true">
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
  grid-template-columns: repeat(5, minmax(180px, 1fr));
  gap: 12px;
  max-width: 1240px;
  margin: 12px auto 0;
  align-items: stretch;
}

.live-dashboard-bar article {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: flex-start;
  min-width: 0;
  min-height: 110px;
  max-height: 110px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px 16px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.live-dashboard-bar article:hover {
  transform: translateY(-3px);
  border-color: rgba(49, 130, 206, 0.28);
  box-shadow: 0 14px 28px rgba(15, 23, 42, 0.1);
}

.widget-live {
  border-color: transparent;
  background: radial-gradient(circle at top left, rgba(255, 255, 255, 0.24), transparent 34%),
    linear-gradient(135deg, #2563eb 0%, #14b8a6 55%, #0ea5e9 100%);
  color: #ffffff;
  box-shadow: 0 24px 42px rgba(16, 185, 129, 0.22);
}

.widget-live p,
.widget-live strong,
.widget-live small {
  color: #f8fafc;
}

.widget-today {
  border-color: color-mix(in srgb, #9f7aea 28%, var(--color-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, #ede9fe 74%, var(--color-bg-secondary)), var(--color-bg-secondary));
}

.widget-shift {
  border-color: color-mix(in srgb, #7bb7ec 30%, var(--color-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, #e7f4ff 76%, var(--color-bg-secondary)), var(--color-bg-secondary));
}

.widget-children {
  border-color: color-mix(in srgb, var(--color-pending-border) 30%, var(--color-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, var(--color-pending) 82%, #fff), var(--color-bg-secondary));
}

.info-icon {
  flex: 0 0 36px;
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.74);
  color: var(--color-upcoming-text);
  font-size: 0.72rem;
  font-weight: 900;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.56), 0 8px 16px rgba(15, 23, 42, 0.08);
}

.widget-live .info-icon {
  color: var(--color-taken-text);
}

.widget-today .info-icon {
  color: #5b46a4;
}

.widget-shift .info-icon {
  color: #22639d;
}

.widget-children .info-icon {
  color: var(--color-pending-text);
}

.info-icon svg {
  width: 19px;
  height: 19px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 2;
}

.live-icon {
  position: relative;
  background: var(--color-taken) !important;
  color: var(--color-taken-text);
}

.live-icon::after {
  position: absolute;
  right: 6px;
  top: 6px;
  width: 11px;
  height: 11px;
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
  color: var(--color-text-secondary);
  font-size: 0.7rem;
  font-weight: 800;
  text-transform: uppercase;
  line-height: 1.2;
}

.live-dashboard-bar strong {
  display: block;
  margin-top: 3px;
  color: var(--color-text-primary);
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
  font-size: 0.72rem;
  font-weight: 800;
  line-height: 1.15;
  text-transform: uppercase;
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
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  }
}

@media (max-width: 560px) {
  .live-dashboard-bar {
    grid-template-columns: 1fr;
  }
}
</style>
