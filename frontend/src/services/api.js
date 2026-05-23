export function apiBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || '';
}

async function request(method, path, body) {
  const url = `${apiBaseUrl()}/api${path}`;
  const options = { method, headers: { 'Content-Type': 'application/json' } };
  if (body !== undefined) {
    options.body = JSON.stringify(body);
  }
  const response = await fetch(url, options);
  if (response.status === 204) return null;
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data?.message || `Request failed: ${response.status}`);
  }
  return data;
}

export const api = {
  // Children
  getChildren: () => request('GET', '/children'),
  createChild: (data) => request('POST', '/children', data),
  updateChild: (id, fields) => request('PUT', `/children/${id}`, fields),
  deleteChild: (id) => request('DELETE', `/children/${id}`),

  // Emergency contacts
  addEmergencyContact: (childId, data) => request('POST', `/children/${childId}/emergency-contacts`, data),
  updateEmergencyContact: (contactId, data) => request('PUT', `/children/emergency-contacts/${contactId}`, data),
  deleteEmergencyContact: (contactId) => request('DELETE', `/children/emergency-contacts/${contactId}`),

  // Parent notes
  saveParentNote: (childId, note) => request('POST', `/children/${childId}/notes`, { note }),
  getParentNote: (childId) => request('GET', `/children/${childId}/notes`),

  // Medications
  getAllMedications: () => request('GET', '/medications'),
  createMedication: (childId, data) => request('POST', `/medications/child/${childId}`, data),
  updateMedication: (medicationId, data) => request('PUT', `/medications/${medicationId}`, data),
  deleteMedication: (medicationId) => request('DELETE', `/medications/${medicationId}`),
  markMedicationTaken: (medicationId) => request('PUT', `/medications/${medicationId}/taken`),
  updateMedicationStatus: (medicationId, status) => request('PUT', `/medications/${medicationId}/status`, { status }),

  // Admin
  getTodayTasks: () => request('GET', '/admin/tasks/today'),
  getTodayStats: () => request('GET', '/admin/stats/today'),
};
