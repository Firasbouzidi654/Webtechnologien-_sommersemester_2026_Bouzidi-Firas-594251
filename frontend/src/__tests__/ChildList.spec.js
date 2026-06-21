import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ChildList from '../components/ChildList.vue';

describe('ChildList', () => {
  it('shows a child in the list', () => {
    const wrapper = mount(ChildList, {
      props: { children: [{ id: 1, name: 'Emma', allergies: 'Peanuts' }] }
    });

    expect(wrapper.text()).toContain('Emma');
  });
});
