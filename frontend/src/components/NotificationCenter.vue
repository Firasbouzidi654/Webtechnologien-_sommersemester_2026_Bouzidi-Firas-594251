<template>
  <div class="notification-center">
    <button class="notification-bell" type="button" aria-label="Open notifications" @click="toggleDropdown">
      <span class="bell-icon">!</span>
      <span v-if="unreadCount" class="notification-count">{{ unreadCount }}</span>
    </button>

    <section
      v-if="dropdownOpen"
      ref="dropdown"
      class="notification-dropdown"
      :style="{ '--notification-max-height': `${dropdownMaxHeight}px` }"
      aria-label="Recent notifications"
    >
      <header>
        <div>
          <p class="eyebrow">Alerts</p>
          <h3>{{ audience === 'parent' ? 'Family updates' : 'Care updates' }}</h3>
        </div>
        <div class="notification-actions">
          <button v-if="hasMoreNotifications" type="button" @click="showAll = !showAll">
            {{ showAll ? 'Show recent' : 'View All' }}
          </button>
          <button type="button" @click="markNotificationsRead">Mark read</button>
        </div>
      </header>

      <div class="notification-list">
        <article v-for="notification in recentNotifications" :key="notification.id" class="notification-item" :class="notification.type">
          <span class="notification-dot"></span>
          <div>
            <strong>{{ notification.title }}</strong>
            <p>{{ notification.message }}</p>
            <time>{{ formatTime(notification.createdAt) }}</time>
          </div>
        </article>
        <p v-if="recentNotifications.length === 0" class="empty-note">No notifications yet.</p>
      </div>
    </section>

    <div class="toast-stack" aria-live="polite">
      <article v-for="toast in toasts" :key="toast.id" class="toast" :class="toast.type">
        <strong>{{ toast.title }}</strong>
        <p>{{ toast.message }}</p>
      </article>
    </div>
  </div>
</template>

<script>
import { clearToast, kindercareStore, markNotificationsRead } from '../state/kindercareStore';

const PARENT_NOTIFICATION_TITLES = new Set([
  'Medication scheduled',
  'Medication administered',
  'Medication missed',
  'New allergy recorded',
  'Health update',
  'Incident report available',
  'Parent message received'
]);

export default {
  name: 'NotificationCenter',
  props: {
    audience: {
      type: String,
      default: 'staff'
    }
  },
  data() {
    return {
      dropdownOpen: false,
      dropdownMaxHeight: 310,
      showAll: false,
      dismissedToastIds: new Set()
    };
  },
  computed: {
    notifications() {
      return kindercareStore.notifications || [];
    },
    audienceNotifications() {
      return this.notifications.filter((notification) => this.isVisibleToAudience(notification));
    },
    recentNotifications() {
      return this.showAll ? this.audienceNotifications : this.audienceNotifications.slice(0, 5);
    },
    hasMoreNotifications() {
      return this.audienceNotifications.length > 5;
    },
    unreadCount() {
      return this.audienceNotifications.filter((notification) => !notification.read).length;
    },
    toasts() {
      return (kindercareStore.toasts || []).filter((notification) => this.isVisibleToAudience(notification));
    }
  },
  watch: {
    toasts: {
      deep: true,
      handler(toasts) {
        toasts.forEach((toast) => {
          if (this.dismissedToastIds.has(toast.id)) {
            return;
          }

          this.dismissedToastIds.add(toast.id);
          window.setTimeout(() => clearToast(toast.id), 3800);
        });
      }
    }
  },
  mounted() {
    window.addEventListener('resize', this.updateDropdownMaxHeight);
    window.addEventListener('scroll', this.updateDropdownMaxHeight, true);
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.updateDropdownMaxHeight);
    window.removeEventListener('scroll', this.updateDropdownMaxHeight, true);
  },
  methods: {
    toggleDropdown() {
      this.dropdownOpen = !this.dropdownOpen;
      this.showAll = false;

      if (this.dropdownOpen) {
        this.$nextTick(this.updateDropdownMaxHeight);
      }
    },
    updateDropdownMaxHeight() {
      const dropdown = this.$refs.dropdown;

      if (!dropdown) {
        return;
      }

      const viewportPadding = 16;
      const availableHeight = window.innerHeight - dropdown.getBoundingClientRect().top - viewportPadding;
      this.dropdownMaxHeight = Math.max(0, Math.min(310, availableHeight));
    },
    markNotificationsRead() {
      markNotificationsRead((notification) => this.isVisibleToAudience(notification));
    },
    isVisibleToAudience(notification) {
      return this.audience !== 'parent' || PARENT_NOTIFICATION_TITLES.has(notification.title);
    },
    formatTime(value) {
      const date = value ? new Date(value) : new Date();
      return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }
  }
};
</script>

<style scoped>
.notification-center {
  position: relative;
  min-width: 0;
}

.notification-bell {
  position: relative;
  display: grid;
  width: 44px;
  height: 44px;
  place-items: center;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-sm);
  cursor: pointer;
}

.notification-bell:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.bell-icon {
  display: grid;
  width: 24px;
  height: 24px;
  place-items: center;
  border-radius: 999px;
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  font-weight: 900;
}

.notification-count {
  position: absolute;
  top: -7px;
  right: -7px;
  min-width: 22px;
  height: 22px;
  border-radius: 999px;
  padding: 0 6px;
  background: var(--color-missed-border);
  color: #fff;
  font-size: 0.74rem;
  font-weight: 900;
  line-height: 22px;
  text-align: center;
  box-shadow: 0 8px 16px rgba(229, 62, 62, 0.24);
}

.notification-dropdown {
  position: absolute;
  top: calc(100% + 12px);
  right: 0;
  z-index: 1300;
  box-sizing: border-box;
  width: min(336px, calc(100vw - 32px));
  max-height: var(--notification-max-height, 310px);
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 12px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-xl);
  animation: dropdown-in 0.18s ease;
}

.notification-dropdown header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.notification-dropdown h3,
.notification-dropdown p {
  margin: 0;
}

.notification-dropdown h3 {
  color: var(--color-text-primary);
  font-size: 0.98rem;
}

.notification-dropdown header button {
  flex: 0 0 auto;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  padding: 7px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 900;
  white-space: nowrap;
}

.notification-dropdown header button:hover {
  background: var(--color-bg-primary);
}

.notification-list {
  flex: 1 1 auto;
  display: grid;
  align-content: start;
  gap: 8px;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 2px;
  scrollbar-gutter: stable;
}

.notification-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notification-item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 9px;
  background: var(--color-bg-primary);
}

.notification-dot {
  width: 8px;
  height: 8px;
  margin-top: 4px;
  border-radius: 999px;
  background: var(--color-upcoming-border);
}

.notification-item.success .notification-dot {
  background: var(--color-taken-border);
}

.notification-item.warning .notification-dot {
  background: var(--color-pending-border);
}

.notification-item.danger .notification-dot {
  background: var(--color-missed-border);
}

.notification-item > div {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.notification-item strong {
  color: var(--color-text-primary);
  font-size: 0.9rem;
  line-height: 1.25;
}

.notification-item p {
  margin: 0;
  color: var(--color-text-primary);
  display: -webkit-box;
  overflow: hidden;
  font-size: 0.82rem;
  font-weight: 500;
  line-height: 1.35;
  overflow-wrap: anywhere;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.notification-item time,
.empty-note {
  color: var(--color-text-secondary);
  font-size: 0.74rem;
  font-weight: 700;
}

.toast-stack {
  position: fixed;
  right: 22px;
  bottom: 22px;
  z-index: 1500;
  display: grid;
  gap: 10px;
  width: min(360px, calc(100vw - 32px));
  pointer-events: none;
}

.toast {
  border: 1px solid var(--color-border);
  border-left: 5px solid var(--color-upcoming-border);
  border-radius: 16px;
  padding: 14px 16px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-xl);
  animation: toast-in 0.28s ease;
}

.toast.success {
  border-left-color: var(--color-taken-border);
}

.toast.warning {
  border-left-color: var(--color-pending-border);
}

.toast.danger {
  border-left-color: var(--color-missed-border);
}

.toast strong,
.toast p {
  margin: 0;
}

.toast p {
  margin-top: 3px;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
}

@keyframes dropdown-in {
  from {
    opacity: 0;
    transform: translateY(-6px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 520px) {
  .notification-dropdown {
    position: fixed;
    top: 74px;
    right: 12px;
    left: 12px;
    width: auto;
    max-height: min(var(--notification-max-height, 310px), calc(100vh - 92px));
  }

  .toast-stack {
    right: 16px;
    left: 16px;
    bottom: 16px;
    width: auto;
  }

  .notification-dropdown header {
    align-items: stretch;
    flex-direction: column;
  }

  .notification-actions {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
}
</style>
