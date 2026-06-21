export function apiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
}

async function request(path, options = {}) {
  const response = await fetch(`${apiBaseUrl()}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.message || 'The request failed.');
  return data;
}

function asList(value) {
  return String(value || '').split(',').map((item) => item.trim()).filter(Boolean);
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
    schedule: { frequency: 'Daily', dayPart: 'Specific time', specificTime: '12:00' },
    history: [],
    prescriptionUploaded: false,
    todayStatus: 'Pending'
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
    healthNotes: '',
    emergencyContacts: [],
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
      scheduledTime: '12:00',
      scheduledDate: new Date().toISOString().slice(0, 10),
      instructions: '',
      status: 'Pending',
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
      chronicDiseases: [], healthNotes: '', emergencyContacts: [], medications: []
    };
  },
  // The following actions update the visible dashboard only. The milestone data is saved through the GET/POST routes above.
  updateChild: async () => null,
  deleteChild: async () => null,
  addEmergencyContact: async (_childId, contact) => ({ id: Date.now(), priority: 1, ...contact }),
  getTodayTasks,
  createMedication: async (childId, medication) => {
    const children = await request('/api/children');
    const child = children.find((item) => item.id === childId);
    const saved = await request('/api/medications', {
      method: 'POST',
      body: JSON.stringify({ name: medication.name, childName: child?.name || '', dosage: medication.dosage || '' })
    });
    return toMedication(saved, child);
  },
  updateMedication: async () => null,
  deleteMedication: async () => null,
  markMedicationTaken: async () => null,
  updateMedicationStatus: async () => null,
  getStaff: () => request('/api/staff'),
  createStaff: (staff) => request('/api/staff', { method: 'POST', body: JSON.stringify(staff) })
};
