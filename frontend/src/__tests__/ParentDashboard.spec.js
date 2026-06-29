import { afterEach, describe, expect, it, vi } from 'vitest';
import { flushPromises, mount } from '@vue/test-utils';
import ParentDashboard from '../views/ParentDashboard.vue';
import { kindercareStore } from '../state/kindercareStore';

function resetStore() {
  kindercareStore.children = [];
  kindercareStore.medicationTasks = [];
  kindercareStore.parentChildIds = [];
  kindercareStore.parentAvatar = null;
  kindercareStore.notifications = [];
  kindercareStore.toasts = [];
  kindercareStore.loading = false;
}

function jsonResponse(data, status = 200) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    json: () => Promise.resolve(data)
  });
}

afterEach(() => {
  vi.unstubAllGlobals();
  localStorage.clear();
  resetStore();
});

describe('ParentDashboard', () => {
  it('creates a child from the parent UI', async () => {
    let children = [];
    const fetchMock = vi.fn((url, options = {}) => {
      if (String(url).endsWith('/api/children') && options.method === 'POST') {
        const body = JSON.parse(options.body);
        children = [{ id: 5, name: body.name, allergies: '', medications: [] }];
        return jsonResponse(children[0], 201);
      }
      if (String(url).endsWith('/api/children')) return jsonResponse(children);
      if (String(url).endsWith('/api/medications')) return jsonResponse([]);
      return jsonResponse({});
    });
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('kindercare-simple-user', JSON.stringify({ email: 'parent@example.test', role: 'PARENT', fullName: 'Parent User' }));

    const wrapper = mount(ParentDashboard, {
      global: { stubs: { NotificationCenter: true, CareHighlights: true, CareIcon: true } }
    });
    await flushPromises();

    await wrapper.get('.empty-add-btn').trigger('click');
    await wrapper.get('.modal input[type="text"]').setValue('Emma Becker');
    await wrapper.get('.modal').trigger('submit');
    await flushPromises();

    expect(fetchMock).toHaveBeenCalledWith(expect.stringContaining('/api/children'), expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ name: 'Emma Becker', allergies: '' })
    }));
    expect(wrapper.text()).toContain('Emma Becker');
  });

  it('creates medication from the parent medication form', async () => {
    const children = [{ id: 1, name: 'Emma', allergies: '', medications: [] }];
    let medications = [];
    const fetchMock = vi.fn((url, options = {}) => {
      if (String(url).endsWith('/api/children')) return jsonResponse(children);
      if (String(url).endsWith('/api/medications') && options.method === 'POST') {
        const body = JSON.parse(options.body);
        medications = [{ id: 8, childId: 1, childName: 'Emma', status: 'PENDING', ...body }];
        return jsonResponse(medications[0], 201);
      }
      if (String(url).endsWith('/api/medications')) return jsonResponse(medications);
      return jsonResponse({});
    });
    vi.stubGlobal('fetch', fetchMock);
    localStorage.setItem('kindercare-simple-user', JSON.stringify({ email: 'parent@example.test', role: 'PARENT', fullName: 'Parent User' }));

    const wrapper = mount(ParentDashboard, {
      global: { stubs: { NotificationCenter: true, CareHighlights: true, CareIcon: true } }
    });
    await flushPromises();

    const form = wrapper.get('.simple-medication-form');
    await form.find('input[type="text"]').setValue('Vitamin D');
    await form.findAll('input[type="text"]')[1].setValue('5 drops');
    await form.find('input[type="time"]').setValue('08:30');
    await form.find('input[type="date"]').setValue('2026-06-30');
    await form.trigger('submit');
    await flushPromises();

    const medicationCall = fetchMock.mock.calls.find(([url, options]) =>
      String(url).endsWith('/api/medications') && options?.method === 'POST'
    );
    expect(JSON.parse(medicationCall[1].body)).toMatchObject({
      name: 'Vitamin D',
      childId: 1,
      dosage: '5 drops',
      time: '08:30',
      frequency: 'ONE_TIME',
      startDate: '2026-06-30'
    });
    expect(wrapper.text()).toContain('Vitamin D');
  });
});
