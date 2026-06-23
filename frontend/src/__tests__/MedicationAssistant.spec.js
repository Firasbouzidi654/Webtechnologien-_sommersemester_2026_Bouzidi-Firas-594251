import { describe, expect, it } from 'vitest';
import { mount } from '@vue/test-utils';
import MedicationAssistant from '../components/MedicationAssistant.vue';

describe('MedicationAssistant', () => {
  it('shows one inline Search button and no Open button', () => {
    const wrapper = mount(MedicationAssistant, { props: { compact: true } });

    expect(wrapper.find('.medication-assistant').classes()).not.toContain('panel');
    expect(wrapper.findAll('button').map((button) => button.text())).toEqual(['Search']);
  });
});
