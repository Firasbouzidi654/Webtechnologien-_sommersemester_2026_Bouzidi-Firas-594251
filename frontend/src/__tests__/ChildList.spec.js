import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import ChildList from '../components/ChildList.vue';

describe('ChildList', () => {
  it('shows a child in the list', () => {
    const wrapper = mount(ChildList, {
      props: { children: [{ id: 1, name: 'Emma', allergies: 'Peanuts' }] }
    });

    expect(wrapper.text()).toContain('Emma');
    expect(wrapper.findAll('.allergy-chip').map((chip) => chip.text())).toEqual(['Peanuts']);
  });

  it('shows an empty allergy state when no allergies are recorded', () => {
    const wrapper = mount(ChildList, {
      props: { children: [{ id: 2, name: 'Lina', allergies: [], medications: [] }] }
    });

    expect(wrapper.text()).toContain('No allergies recorded');
  });
});
