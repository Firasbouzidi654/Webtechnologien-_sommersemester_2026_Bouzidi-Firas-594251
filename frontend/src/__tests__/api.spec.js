import { afterEach, describe, expect, it, vi } from 'vitest';
import { api } from '../services/api';

afterEach(() => {
  vi.unstubAllGlobals();
  localStorage.clear();
});

describe('medication timeline API mapping', () => {
  it('loads the saved medication time and status after refresh', async () => {
    localStorage.setItem('kindercare-simple-user', JSON.stringify({ role: 'PARENT' }));
    vi.stubGlobal('fetch', vi.fn((url) => {
      const data = String(url).endsWith('/api/children')
        ? [{ id: 1, name: 'Emma', allergies: 'Peanuts' }]
        : [{ id: 4, name: 'Salbutamol', childName: 'Emma', dosage: '1 puff', time: '08:30', status: 'TAKEN' }];

      return Promise.resolve({ ok: true, json: () => Promise.resolve(data) });
    }));

    const tasks = await api.getTodayTasks();

    expect(tasks).toHaveLength(1);
    expect(tasks[0]).toMatchObject({ scheduledTime: '08:30', status: 'Taken', childId: 1 });
  });
});
