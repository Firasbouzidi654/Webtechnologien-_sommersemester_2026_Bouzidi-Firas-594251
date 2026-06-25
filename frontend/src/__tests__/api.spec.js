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
        : [{ id: 4, name: 'Salbutamol', childId: 1, childName: 'Emma', dosage: '1 puff', time: '08:30', status: 'TAKEN' }];

      return Promise.resolve({ ok: true, json: () => Promise.resolve(data) });
    }));

    const tasks = await api.getTodayTasks();

    expect(tasks).toHaveLength(1);
    expect(tasks[0]).toMatchObject({ scheduledTime: '08:30', status: 'Taken', childId: 1 });
  });

  it('sends schedule fields when creating a medication', async () => {
    localStorage.setItem('kindercare-simple-user', JSON.stringify({ role: 'PARENT' }));
    const fetchMock = vi.fn((url, options = {}) => {
      if (String(url).endsWith('/api/children')) {
        return Promise.resolve({ ok: true, json: () => Promise.resolve([{ id: 1, name: 'Emma' }]) });
      }
      return Promise.resolve({
        ok: true,
        json: () => Promise.resolve({ id: 9, name: 'Vitamin D', childId: 1, childName: 'Emma', frequency: 'EVERY_X_DAYS', intervalDays: 3 })
      });
    });
    vi.stubGlobal('fetch', fetchMock);

    await api.createMedication(1, {
      name: 'Vitamin D', dosage: '5 drops', scheduledTime: '08:00',
      frequency: 'EVERY_X_DAYS', intervalDays: 3, startDate: '2026-06-23'
    });

    const payload = JSON.parse(fetchMock.mock.calls[1][1].body);
    expect(payload).toMatchObject({ frequency: 'EVERY_X_DAYS', intervalDays: 3, startDate: '2026-06-23' });
  });

  it('surfaces backend validation messages for invalid input', async () => {
    vi.stubGlobal('fetch', vi.fn(() => Promise.resolve({
      ok: false,
      status: 400,
      json: () => Promise.resolve({ message: 'A medication name is required.' })
    })));

    await expect(api.updateMedication(7, { name: ' ' })).rejects.toThrow('A medication name is required.');
  });
});
