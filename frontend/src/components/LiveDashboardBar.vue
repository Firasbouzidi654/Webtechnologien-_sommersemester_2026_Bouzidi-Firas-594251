<template>
  <section class="live-dashboard-bar" aria-label="Live dashboard information">
    <article>
      <span class="live-dot"></span>
      <div>
        <p>Live time</p>
        <strong>{{ liveTime }}</strong>
      </div>
    </article>
    <article>
      <span>{{ currentDate }}</span>
      <div>
        <p>Today</p>
        <strong>{{ weekday }}</strong>
      </div>
    </article>
    <article>
      <span>{{ shiftInitials }}</span>
      <div>
        <p>Active staff shift</p>
        <strong>{{ shiftLabel }}</strong>
      </div>
    </article>
    <article>
      <span>{{ childrenCount }}</span>
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
      return this.now.toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
    },
    weekday() {
      return this.now.toLocaleDateString([], { weekday: 'long' });
    },
    currentDate() {
      return this.now.toLocaleDateString([], { month: 'short', day: 'numeric' });
    },
    shiftInitials() {
      return this.shiftLabel
        .split(' ')
        .map((part) => part[0])
        .join('')
        .slice(0, 2)
        .toUpperCase();
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
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  max-width: 1240px;
  margin: 18px auto 0;
}

.live-dashboard-bar article {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  min-width: 0;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 14px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.live-dashboard-bar article:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.live-dashboard-bar article > span {
  display: grid;
  width: 42px;
  height: 42px;
  place-items: center;
  border-radius: 14px;
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  font-size: 0.78rem;
  font-weight: 900;
}

.live-dot {
  position: relative;
  background: var(--color-taken) !important;
}

.live-dot::after {
  width: 12px;
  height: 12px;
  border-radius: 999px;
  background: var(--color-taken-border);
  content: '';
  animation: pulse-live 1.4s ease-in-out infinite;
}

.live-dashboard-bar p,
.live-dashboard-bar strong {
  margin: 0;
}

.live-dashboard-bar p {
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 800;
}

.live-dashboard-bar strong {
  display: block;
  margin-top: 2px;
  color: var(--color-text-primary);
  font-size: 1rem;
  overflow-wrap: anywhere;
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
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .live-dashboard-bar {
    grid-template-columns: 1fr;
  }
}
</style>
