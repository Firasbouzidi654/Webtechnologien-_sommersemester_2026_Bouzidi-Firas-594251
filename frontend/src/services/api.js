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

function toMedication(medication, child) {
  return {
    id: medication.id,
    medicationId: String(medication.id),
    childId: child?.id || null,
    childName: medication.childName,
    name: medication.name,
    dosage: medication.dosage || '',
    instructions: '',
    schedule: { frequency: 'Daily', dayPart: 'Specific time', specificTime: medication.time || '12:00' },
    history: [],
    prescriptionUploaded: false,
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

async function getChildren() {
  const { children, medications } = await loadRawRecords();
  return children.map((child) => ({
    ...child,
    groupName: 'Sunflowers',
    dateOfBirth: null,
    parentName: 'Parent',
    parentEmail: '',
    allergies: asList(child.allergies),
    chronicDiseases: [],
    medications: medications.filter((medication) => medication.childName === child.name).map((medication) => toMedication(medication, child))
  }));
}

async function getTodayTasks() {
  const { children, medications } = await loadRawRecords();
  return medications.map((medication) => {
    const child = children.find((item) => item.name === medication.childName);
    return {
      taskId: `TASK-${medication.id}`,
      medicationId: String(medication.id),
      childId: child?.id || null,
      childName: medication.childName,
      groupName: 'Sunflowers',
      medicationName: medication.name,
      dosage: medication.dosage || '',
      scheduledTime: medication.time || '12:00',
      scheduledDate: new Date().toISOString().slice(0, 10),
      instructions: '',
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
      ...saved, groupName: child.groupName || 'Sunflowers', allergies: asList(saved.allergies),
      chronicDiseases: [], medications: []
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
    const child = children.find((item) => item.id === childId);
    const saved = await request('/api/medications', {
      method: 'POST',
      body: JSON.stringify({
        name: medication.name,
        childName: child?.name || '',
        dosage: medication.dosage || '',
        time: medication.scheduledTime || '12:00',
        status: apiStatus(medication.status)
      })
    });
    return toMedication(saved, child);
  },
  updateMedication: (medicationId, medication) => request(`/api/medications/${medicationId}`, {
    method: 'PUT',
    body: JSON.stringify({
      name: medication.name,
      childName: medication.childName,
      dosage: medication.dosage,
      time: medication.scheduledTime || medication.time,
      status: medication.status ? apiStatus(medication.status) : undefined
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
