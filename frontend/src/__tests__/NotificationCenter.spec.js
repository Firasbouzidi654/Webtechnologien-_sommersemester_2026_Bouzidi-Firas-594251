import { afterEach, describe, expect, it, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import NotificationCenter from '../components/NotificationCenter.vue';
import { addNotification, kindercareStore } from '../state/kindercareStore';

function resetStore() {
  kindercareStore.notifications = [];
  kindercareStore.toasts = [];
}

afterEach(() => {
  vi.useRealTimers();
  resetStore();
});

describe('NotificationCenter', () => {
  it('automatically clears visible toasts after 3 seconds', async () => {
    vi.useFakeTimers();
    addNotification({
      title: 'Medication scheduled',
      message: 'Vitamin D was scheduled for Emma.',
      type: 'info'
    });

    const wrapper = mount(NotificationCenter, { props: { audience: 'parent' } });
    expect(wrapper.find('.toast').exists()).toBe(true);

    vi.advanceTimersByTime(3000);
    await wrapper.vm.$nextTick();

    expect(kindercareStore.toasts).toHaveLength(0);
    expect(wrapper.find('.toast').exists()).toBe(false);
  });

  it('closes the notification dropdown when clicking outside', async () => {
    addNotification({
      title: 'Medication scheduled',
      message: 'Vitamin D was scheduled for Emma.',
      type: 'info',
      toast: false
    });

    const wrapper = mount(NotificationCenter, {
      attachTo: document.body,
      props: { audience: 'parent' }
    });

    await wrapper.get('.notification-bell').trigger('click');
    expect(wrapper.find('.notification-dropdown').exists()).toBe(true);

    document.body.dispatchEvent(new Event('pointerdown', { bubbles: true }));
    await wrapper.vm.$nextTick();

    expect(wrapper.find('.notification-dropdown').exists()).toBe(false);
    wrapper.unmount();
  });

  it('marks parent notifications as read and hides the unread badge', async () => {
    addNotification({
      title: 'Medication scheduled',
      message: 'Vitamin D was scheduled for Emma.',
      type: 'info',
      toast: false
    });

    const wrapper = mount(NotificationCenter, { props: { audience: 'parent' } });
    expect(wrapper.find('.notification-count').exists()).toBe(true);

    await wrapper.get('.notification-bell').trigger('click');
    await wrapper.get('.notification-actions button:last-child').trigger('click');

    expect(kindercareStore.notifications.every((notification) => notification.read)).toBe(true);
    expect(wrapper.find('.notification-count').exists()).toBe(false);
  });
});
