import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import AdminCalendar from '../components/AdminCalendar.vue';

describe('AdminCalendar', () => {
  it('shows a one-time medication only on its selected date', () => {
    const wrapper = mount(AdminCalendar, {
      props: {
        tasks: [{
          medicationId: '12',
          medicationName: 'One-time dose',
          childName: 'Emma',
          scheduledDate: '2026-06-24',
          scheduledTime: '12:00',
          frequency: 'ONE_TIME',
          status: 'Pending'
        }]
      }
    });

    expect(wrapper.vm.tasksForDate('2026-06-24')).toHaveLength(1);
    expect(wrapper.vm.tasksForDate('2026-07-01')).toHaveLength(0);
  });
});
