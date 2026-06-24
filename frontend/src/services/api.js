export function apiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
}

function roleHeader() {
  try {
    const user = JSON.parse(localStorage.getItem('kindercare-simple-user'));
    return user?.role ? { 'X-User-Role': user.role } : {};
  } catch {
    return {};
  }
}

async function request(path, options = {}) {
  let response;

  try {
    response = await fetch(`${apiBaseUrl()}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...roleHeader(),
        ...options.headers
      }
    });
  } catch {
    throw new Error('Server unavailable. Please check your connection and try again.');
  }

  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 401) {
      throw new Error('Login failed. Please check your email address and password.');
    }

    if (response.status === 409) {
      throw new Error('Registration failed. An account with this email address already exists.');
    }

    if (response.status >= 500) {
      throw new Error('The server could not process your request. Please try again later.');
    }

    throw new Error(data.message || 'Your request could not be completed. Please check your input and try again.');
  }

  return data;
}

function asList(value) {
  return String(value || '').split(',').map((item) => item.trim()).filter(Boolean);
}

function displayStatus(status) {
  const statuses = { UPCOMING: 'Upcoming', PENDING: 'Pending', TAKEN: 'Taken', MISSED: 'Missed' };
  return statuses[String(status || '').toUpperCase()] || 'Pending';
}

function apiStatus(status) {
  return String(status || 'Pending').trim().toUpperCase();
}

function displayFrequency(frequency, intervalDays, dayOfWeek) {
  const labels = {
    DAILY: 'Daily',
    WEEKLY: 'Weekly',
    ONE_TIME: 'One-time'
  };
  if (String(frequency).toUpperCase() === 'EVERY_X_DAYS') {
    return `Every ${intervalDays || 2} days`;
  }
  if (String(frequency).toUpperCase() === 'SPECIFIC_DAY') {
    return `Every ${String(dayOfWeek || 'MONDAY').toLowerCase().replace(/^./, (letter) => letter.toUpperCase())}`;
  }
  return labels[String(frequency || 'DAILY').toUpperCase()] || 'Daily';
}

function isScheduledForDate(medication, dateKey) {
  const startDate = medication.startDate || dateKey;
  const start = new Date(`${startDate}T00:00:00`);
  const target = new Date(`${dateKey}T00:00:00`);
  if (Number.isNaN(start.getTime()) || target < start) return false;

  const days = Math.round((target - start) / 86400000);
  switch (String(medication.frequency || 'DAILY').toUpperCase()) {
    case 'WEEKLY': return days % 7 === 0;
    case 'EVERY_X_DAYS': return days % Math.max(Number(medication.intervalDays) || 2, 2) === 0;
    case 'SPECIFIC_DAY': {
      const weekdayNumbers = { SUNDAY: 0, MONDAY: 1, TUESDAY: 2, WEDNESDAY: 3, THURSDAY: 4, FRIDAY: 5, SATURDAY: 6 };
      return target.getDay() === weekdayNumbers[String(medication.dayOfWeek || 'MONDAY').toUpperCase()];
    }
    case 'ONE_TIME': return days === 0;
    default: return true;
  }
}

function toMedication(medication, child) {
  return {
    id: medication.id,
    medicationId: String(medication.id),
    childId: child?.id || medication.childId || null,
    childName: child?.name || 'Unknown child',
    name: medication.name,
    dosage: medication.dosage || '',
    instructions: '',
    schedule: {
      frequency: displayFrequency(medication.frequency, medication.intervalDays, medication.dayOfWeek),
      frequencyCode: medication.frequency || 'DAILY',
      intervalDays: medication.intervalDays || null,
      dayOfWeek: medication.dayOfWeek || null,
      dayPart: 'Specific time',
      specificTime: medication.time || '12:00',
      startDate: medication.startDate || null
    },
    history: [],
    todayStatus: displayStatus(medication.status)
  };
}

async function loadRawRecords() {
  const [children, medications] = await Promise.all([
    request('/api/children'),
    request('/api/medications')
  ]);
  return { children, medications };
}

function belongsToChild(medication, child) {
  return medication.childId === child.id;
}

async function getChildren() {
  const { children, medications } = await loadRawRecords();
  return children.map((child) => ({
    ...child,
    allergies: asList(child.allergies),
    medications: medications.filter((medication) => belongsToChild(medication, child)).map((medication) => toMedication(medication, child))
  }));
}

async function getTodayTasks() {
  const { children, medications } = await loadRawRecords();
  const today = new Date().toISOString().slice(0, 10);
  return medications.map((medication) => {
    const child = children.find((item) => belongsToChild(medication, item));
    return {
      taskId: `TASK-${medication.id}`,
      medicationId: String(medication.id),
      childId: child?.id || null,
      childName: child?.name || 'Unknown child',
      medicationName: medication.name,
      dosage: medication.dosage || '',
      scheduledTime: medication.time || '12:00',
      scheduledDate: medication.startDate || today,
      scheduledToday: isScheduledForDate(medication, today),
      instructions: '',
      status: displayStatus(medication.status),
      frequency: medication.frequency || 'DAILY',
      intervalDays: medication.intervalDays || null,
      dayOfWeek: medication.dayOfWeek || null,
      reminderDue: false
    };
  });
}

export const api = {
  getChildren,
  createChild: async (child) => {
    const saved = await request('/api/children', { method: 'POST', body: JSON.stringify(child) });
    return {
      ...saved,
      allergies: asList(saved.allergies),
      medications: []
    };
  },
  updateChild: async (childId, child) => {
    const payload = {
      ...child,
      allergies: Array.isArray(child.allergies) ? child.allergies.join(', ') : child.allergies
    };
    return request(`/api/children/${childId}`, { method: 'PUT', body: JSON.stringify(payload) });
  },
  deleteChild: (childId) => request(`/api/children/${childId}`, { method: 'DELETE' }),
  getTodayTasks,
  createMedication: async (childId, medication) => {
    const children = await request('/api/children');
    const child = children.find((item) => String(item.id) === String(childId));
    const saved = await request('/api/medications', {
      method: 'POST',
      body: JSON.stringify({
        name: medication.name,
        childId: child?.id ?? childId,
        dosage: medication.dosage || '',
        time: medication.scheduledTime || '12:00',
        status: apiStatus(medication.status),
        frequency: medication.frequency || 'DAILY',
        intervalDays: medication.intervalDays || null,
        dayOfWeek: medication.dayOfWeek || null,
        startDate: medication.startDate || null
      })
    });
    return toMedication(saved, child);
  },
  updateMedication: (medicationId, medication) => request(`/api/medications/${medicationId}`, {
    method: 'PUT',
    body: JSON.stringify({
      name: medication.name,
      dosage: medication.dosage,
      time: medication.scheduledTime || medication.time,
      status: medication.status ? apiStatus(medication.status) : undefined,
      frequency: medication.frequency,
      intervalDays: medication.intervalDays || null,
      dayOfWeek: medication.dayOfWeek || null,
      startDate: medication.startDate || null
    })
  }),
  deleteMedication: (medicationId) => request(`/api/medications/${medicationId}`, { method: 'DELETE' }),
  markMedicationTaken: (medicationId) => request(`/api/medications/${medicationId}`, {
    method: 'PUT',
    body: JSON.stringify({ status: 'TAKEN' })
  }),
  updateMedicationStatus: (medicationId, status) => request(`/api/medications/${medicationId}`, {
    method: 'PUT',
    body: JSON.stringify({ status: apiStatus(status) })
  }),
  registerUser: (user) => request('/api/auth/register', { method: 'POST', body: JSON.stringify(user) }),
  loginUser: (credentials) => request('/api/auth/login', { method: 'POST', body: JSON.stringify(credentials) })
};
