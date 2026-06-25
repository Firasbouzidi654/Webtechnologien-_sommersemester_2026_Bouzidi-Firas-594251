import { afterEach, describe, expect, it, vi } from 'vitest';
import { kindercareStore, setMedicationStatus } from '../state/kindercareStore';

function resetStore() {
  kindercareStore.children = [];
  kindercareStore.medicationTasks = [];
  kindercareStore.parentChildIds = [];
  kindercareStore.notifications = [];
  kindercareStore.toasts = [];
  kindercareStore.loading = false;
}

function jsonResponse(data) {
  return Promise.resolve({
    ok: true,
    status: 200,
    json: () => Promise.resolve(data)
  });
}

afterEach(() => {
  vi.unstubAllGlobals();
  localStorage.clear();
  resetStore();
});

describe('kindercareStore medication status updates', () => {
  it('updates medication status through the backend API', async () => {
    kindercareStore.children = [{
      id: 1,
      name: 'Emma',
      allergies: [],
      medications: [{ medicationId: '7', todayStatus: 'Pending' }]
    }];
    kindercareStore.medicationTasks = [{
      taskId: 'TASK-7',
      medicationId: '7',
      childId: 1,
      childName: 'Emma',
      medicationName: 'Vitamin D',
      status: 'Pending',
      scheduledTime: '08:00',
      scheduledToday: true
    }];
    localStorage.setItem('kindercare-simple-user', JSON.stringify({ role: 'STAFF' }));

    const fetchMock = vi.fn((url, options = {}) => {
      if (String(url).endsWith('/api/medications/7')) {
        return jsonResponse({ id: 7, status: 'MISSED' });
      }
      if (String(url).endsWith('/api/children')) {
        return jsonResponse([{ id: 1, name: 'Emma', allergies: '' }]);
      }
      if (String(url).endsWith('/api/medications')) {
        return jsonResponse([{ id: 7, childId: 1, name: 'Vitamin D', dosage: '5 drops', time: '08:00', status: 'MISSED' }]);
      }
      return jsonResponse({});
    });
    vi.stubGlobal('fetch', fetchMock);

    await setMedicationStatus('7', 'Missed');

    const updateCall = fetchMock.mock.calls.find(([url]) => String(url).endsWith('/api/medications/7'));
    expect(updateCall[1]).toMatchObject({ method: 'PUT' });
    expect(JSON.parse(updateCall[1].body)).toEqual({ status: 'MISSED' });
    expect(updateCall[1].headers).toMatchObject({ 'X-User-Role': 'STAFF' });
    expect(kindercareStore.medicationTasks[0].status).toBe('Missed');
  });
});
