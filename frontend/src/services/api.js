export function apiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
}

function roleHeader() {
  try {
    const user = JSON.parse(localStorage.getItem('kindercare-simple-user'));
    return {
      ...(user?.role ? { 'X-User-Role': user.role } : {}),
      ...(user?.id ? { 'X-User-Id': String(user.id) } : {})
    };
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

function medicationDate(medication, fallbackDate) {
  return medication.scheduledDate || fallbackDate;
}

function toMedication(medication, child, fallbackDate = new Date().toISOString().slice(0, 10)) {
  const scheduledDate = medicationDate(medication, fallbackDate);
  return {
    id: medication.id,
    medicationId: String(medication.id),
    childId: child?.id || medication.childId || null,
    childName: child?.name || 'Unknown child',
    name: medication.name,
    dosage: medication.dosage || '',
    schedule: {
      date: scheduledDate,
      dayPart: 'Specific time',
      specificTime: medication.time || '12:00',
      scheduledDate
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
  const today = new Date().toISOString().slice(0, 10);
  return children.map((child) => ({
    ...child,
    allergies: asList(child.allergies),
    medications: medications.filter((medication) => belongsToChild(medication, child)).map((medication) => toMedication(medication, child, today))
  }));
}

async function getTodayTasks() {
  const { children, medications } = await loadRawRecords();
  const today = new Date().toISOString().slice(0, 10);
  return medications.map((medication) => {
    const child = children.find((item) => belongsToChild(medication, item));
    const scheduledDate = medicationDate(medication, today);
    return {
      taskId: `TASK-${medication.id}`,
      medicationId: String(medication.id),
      childId: child?.id || null,
      childName: child?.name || 'Unknown child',
      medicationName: medication.name,
      dosage: medication.dosage || '',
      scheduledTime: medication.time || '12:00',
      scheduledDate,
      scheduledToday: scheduledDate === today,
      status: displayStatus(medication.status),
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
        scheduledDate: medication.scheduledDate || null
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
      scheduledDate: medication.scheduledDate || null
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
  askChildCareAssistant: ({ type, message }) => request('/api/ai/childcare-assistant', {
    method: 'POST',
    body: JSON.stringify({ type, message })
  }),
  generateParentMessage: ({ message }) => request('/api/ai/parent-message', {
    method: 'POST',
    body: JSON.stringify({ message })
  }),
  registerUser: (user) => request('/api/auth/register', { method: 'POST', body: JSON.stringify(user) }),
  loginUser: (credentials) => request('/api/auth/login', { method: 'POST', body: JSON.stringify(credentials) })
};
