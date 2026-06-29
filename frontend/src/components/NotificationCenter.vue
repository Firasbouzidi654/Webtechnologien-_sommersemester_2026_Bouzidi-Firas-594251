<template>
  <div class="notification-center">
    <button
      class="notification-bell"
      type="button"
      :aria-expanded="dropdownOpen ? 'true' : 'false'"
      aria-haspopup="true"
      aria-label="Open notifications"
      @click="toggleDropdown"
    >
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
        <div class="notification-heading">
          <p class="eyebrow">Alerts</p>
          <h3>{{ audience === 'parent' ? 'Family updates' : 'Care updates' }}</h3>
        </div>
        <div class="notification-actions">
          <button
            v-if="hasMoreNotifications"
            type="button"
            :aria-pressed="showAll ? 'true' : 'false'"
            @click="showAll = !showAll"
          >
            {{ showAll ? 'Show recent' : 'View All' }}
          </button>
          <button type="button" aria-label="Mark all notifications as read" @click="markNotificationsRead">
            Mark all as read
          </button>
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
      dropdownMaxHeight: 380,
      showAll: false,
      dismissedToastIds: new Set(),
      toastTimers: new Map()
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
      immediate: true,
      handler(toasts) {
        toasts.forEach((toast) => {
          if (this.dismissedToastIds.has(toast.id)) {
            return;
          }

          this.dismissedToastIds.add(toast.id);
          this.toastTimers.set(toast.id, window.setTimeout(() => {
            clearToast(toast.id);
            this.toastTimers.delete(toast.id);
          }, 3000));
        });
      }
    }
  },
  mounted() {
    document.addEventListener('pointerdown', this.handleOutsideClick);
    window.addEventListener('resize', this.updateDropdownMaxHeight);
    window.addEventListener('scroll', this.updateDropdownMaxHeight, true);
  },
  beforeUnmount() {
    document.removeEventListener('pointerdown', this.handleOutsideClick);
    window.removeEventListener('resize', this.updateDropdownMaxHeight);
    window.removeEventListener('scroll', this.updateDropdownMaxHeight, true);
    this.toastTimers.forEach((timerId) => window.clearTimeout(timerId));
    this.toastTimers.clear();
  },
  methods: {
    toggleDropdown() {
      this.dropdownOpen = !this.dropdownOpen;
      this.showAll = false;

      if (this.dropdownOpen) {
        this.$nextTick(this.updateDropdownMaxHeight);
      }
    },
    handleOutsideClick(event) {
      if (!this.dropdownOpen || this.$el.contains(event.target)) {
        return;
      }

      this.dropdownOpen = false;
      this.showAll = false;
    },
    updateDropdownMaxHeight() {
      const dropdown = this.$refs.dropdown;

      if (!dropdown) {
        return;
      }

      const viewportPadding = 16;
      const availableHeight = window.innerHeight - dropdown.getBoundingClientRect().top - viewportPadding;
      this.dropdownMaxHeight = Math.max(220, Math.min(380, availableHeight));
    },
    markNotificationsRead() {
      markNotificationsRead((notification) => this.isVisibleToAudience(notification));
      this.dropdownOpen = false;
      this.showAll = false;
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
  flex: 0 0 auto;
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
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.notification-bell:hover,
.notification-bell:focus-visible {
  border-color: rgba(49, 130, 206, 0.32);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  outline: none;
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
  width: clamp(320px, 34vw, 380px);
  max-width: calc(100vw - 24px);
  max-height: min(var(--notification-max-height, 380px), calc(100dvh - 24px));
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 12px;
  overflow: hidden;
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
  gap: 12px;
  align-items: flex-start;
  min-width: 0;
  border-bottom: 1px solid var(--color-border-light);
  padding-bottom: 10px;
}

.notification-dropdown h3,
.notification-dropdown p {
  margin: 0;
}

.notification-heading {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.notification-dropdown h3 {
  color: var(--color-text-primary);
  font-size: 0.98rem;
  line-height: 1.2;
}

.notification-dropdown header button {
  flex: 0 0 auto;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  min-height: 32px;
  padding: 6px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  cursor: pointer;
  font-size: 0.78rem;
  font-weight: 900;
  white-space: nowrap;
  transition: background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.notification-dropdown header button:hover,
.notification-dropdown header button:focus-visible {
  border-color: rgba(49, 130, 206, 0.28);
  background: var(--color-bg-primary);
  box-shadow: var(--shadow-sm);
  outline: none;
}

.notification-list {
  display: grid;
  align-content: start;
  gap: 8px;
  min-height: 0;
  max-height: 100%;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding-right: 4px;
  scrollbar-gutter: stable;
}

.notification-actions {
  display: flex;
  flex: 0 0 auto;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  max-width: 190px;
  flex-wrap: wrap;
}

.notification-item {
  display: grid;
  grid-template-columns: 8px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 10px;
  background: var(--color-bg-primary);
  transition: background 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, transform 0.18s ease;
}

.notification-item:hover {
  border-color: rgba(49, 130, 206, 0.18);
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transform: translateY(-1px);
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
  gap: 4px;
}

.notification-item strong {
  color: var(--color-text-primary);
  font-size: 0.92rem;
  line-height: 1.25;
  font-weight: 900;
}

.notification-item p {
  margin: 0;
  color: var(--color-text-secondary);
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

.empty-note {
  margin: 0;
  border: 1px dashed var(--color-border);
  border-radius: 12px;
  padding: 18px 12px;
  background: var(--color-bg-primary);
  text-align: center;
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
  .notification-bell {
    width: 40px;
    height: 40px;
  }

  .notification-dropdown {
    top: calc(100% + 10px);
    right: auto;
    left: 0;
    width: min(360px, calc(100vw - 20px));
    max-width: none;
    max-height: min(var(--notification-max-height, 380px), calc(100dvh - 96px));
    padding: 10px;
    border-radius: 14px;
  }

  .toast-stack {
    right: 10px;
    left: 10px;
    bottom: 10px;
    width: auto;
  }

  .notification-dropdown header {
    align-items: stretch;
    flex-direction: column;
    gap: 8px;
    padding-bottom: 8px;
  }

  .notification-actions {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
    gap: 6px;
    max-width: none;
  }

  .notification-dropdown header button {
    min-height: 34px;
    padding: 6px 8px;
    white-space: normal;
  }

  .notification-list {
    gap: 6px;
  }

  .notification-item {
    padding: 8px;
  }

  .toast {
    padding: 10px 12px;
  }
}
</style>
