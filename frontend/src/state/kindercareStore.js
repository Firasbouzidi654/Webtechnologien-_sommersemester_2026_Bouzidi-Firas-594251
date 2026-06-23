import { reactive } from 'vue';
import { api } from '../services/api';
import { currentUser } from './authStore';

export const MEDICATION_STATUSES = ['Pending', 'Taken', 'Missed', 'Upcoming'];

export const kindercareStore = reactive({
  children: [],
  medicationTasks: [],
  parentChildIds: [],
  parentAvatar: null,
  notifications: [],
  toasts: [],
  loading: false
});

function findChild(childId) {
  return kindercareStore.children.find((child) => child.id === childId);
}

function todayDateKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function safeStatus(status, fallback = 'Pending') {
  return MEDICATION_STATUSES.includes(status) ? status : fallback;
}

// ─── Per-user parentChildIds persistence ─────────────────────────────────────

function parentChildIdsKey() {
  const email = currentUser()?.email;
  return email ? `kindercare-parent-children-${email}` : null;
}

function loadParentChildIds() {
  const key = parentChildIdsKey();
  if (!key) return [];
  try {
    const raw = localStorage.getItem(key);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

function saveParentChildIds(ids) {
  const key = parentChildIdsKey();
  if (!key) return;
  try {
    localStorage.setItem(key, JSON.stringify(ids));
  } catch {}
}

function findMedicationOwner(medicationId) {
  return kindercareStore.children.find((child) =>
    Array.isArray(child.medications) && child.medications.some((medication) => medication.medicationId === medicationId)
  );
}

function statusNotificationType(status) {
  if (status === 'Taken') return 'success';
  if (status === 'Missed') return 'danger';
  if (status === 'Pending') return 'warning';
  return 'info';
}

function mergeChildrenFromApi(apiChildren) {
  const existing = {};
  kindercareStore.children.forEach((c) => { existing[c.id] = c; });

  kindercareStore.children = apiChildren.map((apiChild) => ({
    prescriptionFileName: existing[apiChild.id]?.prescriptionFileName || null,
    ...apiChild
  }));
}

// ─── Data loading ────────────────────────────────────────────────────────────

export async function loadChildren() {
  kindercareStore.loading = true;
  try {
    const children = await api.getChildren();
    mergeChildrenFromApi(children);
    // Restore only the IDs this specific logged-in parent created
    if (kindercareStore.parentChildIds.length === 0) {
      kindercareStore.parentChildIds = loadParentChildIds();
    }
  } catch {
    addNotification({ title: 'Load failed', message: 'Could not load children from the server.', type: 'danger' });
  } finally {
    kindercareStore.loading = false;
  }
}

export async function loadMedicationTasks() {
  try {
    const tasks = await api.getTodayTasks();
    kindercareStore.medicationTasks = Array.isArray(tasks) ? tasks : [];
  } catch {
    addNotification({ title: 'Load failed', message: 'Could not load medication tasks.', type: 'danger' });
  }
}

// ─── Notifications & toasts ──────────────────────────────────────────────────

export function addNotification({ title, message, type = 'info', toast = true }) {
  const notification = {
    id: Date.now() + Math.random(),
    title,
    message,
    type,
    read: false,
    createdAt: new Date().toISOString()
  };

  kindercareStore.notifications.unshift(notification);
  kindercareStore.notifications = kindercareStore.notifications.slice(0, 30);

  if (toast) {
    kindercareStore.toasts.unshift(notification);
    kindercareStore.toasts = kindercareStore.toasts.slice(0, 4);
  }

  return notification;
}

export function clearToast(id) {
  kindercareStore.toasts = kindercareStore.toasts.filter((toast) => toast.id !== id);
}

export function markNotificationsRead() {
  kindercareStore.notifications.forEach((notification) => {
    notification.read = true;
  });
}

// ─── Utility ─────────────────────────────────────────────────────────────────

export function taskReminderDue(task) {
  if (!task || task.status !== 'Pending' || !task.scheduledTime) {
    return false;
  }
  const taskDate = task.scheduledDate || todayDateKey();
  return taskDate === todayDateKey() && task.scheduledTime <= new Date().toTimeString().slice(0, 5);
}

export function parentChildren() {
  // In public demo mode (opening /#/parent directly), show the saved records.
  if (kindercareStore.parentChildIds.length === 0) {
    return kindercareStore.children;
  }
  return kindercareStore.children.filter((child) => kindercareStore.parentChildIds.includes(child.id));
}

export function setParentAvatar(dataUrl) {
  kindercareStore.parentAvatar = dataUrl;
}

// ─── Children CRUD ───────────────────────────────────────────────────────────

export async function addChild(data) {
  const user = currentUser();
  let child;
  try {
    child = await api.createChild({
      name: data.name,
      groupName: data.groupName || '',
      dateOfBirth: data.dateOfBirth || null,
      parentName: user?.fullName || '',
      parentEmail: user?.email || '',
      // Allergies use a comma-separated string in the persisted model.
      allergies: '',
      chronicDiseases: ''
    });
  } catch {
    addNotification({
      title: 'Could not add child',
      message: 'Server unavailable. Please check your connection and try again.',
      type: 'danger'
    });
    return null;
  }

  child.prescriptionFileName = null;

  if (!kindercareStore.children.find((c) => c.id === child.id)) {
    kindercareStore.children.push(child);
  }
  if (!kindercareStore.parentChildIds.includes(child.id)) {
    kindercareStore.parentChildIds.push(child.id);
  }
  saveParentChildIds(kindercareStore.parentChildIds);

  addNotification({
    title: 'Child added',
    message: `${child.name}'s profile was saved successfully.`,
    type: 'success'
  });
  return child;
}

export async function deleteChild(childId) {
  // Optimistic remove so the UI feels instant
  kindercareStore.children = kindercareStore.children.filter((c) => c.id !== childId);
  kindercareStore.parentChildIds = kindercareStore.parentChildIds.filter((id) => id !== childId);
  saveParentChildIds(kindercareStore.parentChildIds);

  try {
    await api.deleteChild(childId);
    addNotification({ title: 'Child removed', message: 'Child profile deleted successfully.', type: 'success' });
  } catch {
    addNotification({ title: 'Delete failed', message: 'Could not delete child from server.', type: 'danger' });
    await loadChildren();
  }
}

// ─── Allergies ───────────────────────────────────────────────────────────────

export async function addAllergy(childId, name) {
  const child = findChild(childId);
  if (!child) return null;
  const updated = [...(child.allergies || []).filter((item) => item !== 'None known'), name];
  child.allergies = updated;
  try {
    await api.updateChild(childId, { allergies: updated });
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not save allergy. Changes may not persist.', type: 'danger' });
  }
}

export async function editAllergy(childId, index, name) {
  const child = findChild(childId);
  if (!child) return;
  child.allergies ||= [];
  child.allergies.splice(index, 1, name);
  try {
    await api.updateChild(childId, { allergies: child.allergies });
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not update allergy.', type: 'danger' });
  }
}

export async function removeAllergy(childId, index) {
  const child = findChild(childId);
  if (!child) return;
  child.allergies.splice(index, 1);
  try {
    await api.updateChild(childId, { allergies: child.allergies });
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not remove allergy.', type: 'danger' });
  }
}

// ─── Chronic diseases ────────────────────────────────────────────────────────

export async function addDisease(childId, name) {
  const child = findChild(childId);
  if (!child) return null;
  const updated = [...(child.chronicDiseases || []).filter((item) => item !== 'None'), name];
  child.chronicDiseases = updated;
  try {
    await api.updateChild(childId, { chronicDiseases: updated });
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not save chronic disease.', type: 'danger' });
  }
}

export async function editDisease(childId, index, name) {
  const child = findChild(childId);
  if (!child) return;
  child.chronicDiseases ||= [];
  child.chronicDiseases.splice(index, 1, name);
  try {
    await api.updateChild(childId, { chronicDiseases: child.chronicDiseases });
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not update chronic disease.', type: 'danger' });
  }
}

export async function removeDisease(childId, index) {
  const child = findChild(childId);
  if (!child) return;
  child.chronicDiseases.splice(index, 1);
  try {
    await api.updateChild(childId, { chronicDiseases: child.chronicDiseases });
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not remove chronic disease.', type: 'danger' });
  }
}

// ─── Prescriptions (UI-only) ─────────────────────────────────────────────────

export function uploadPrescription(childId, fileName) {
  const child = findChild(childId);
  if (!child) return;
  child.prescriptionFileName = fileName;
  child.medications = (child.medications || []).map((medication) => ({
    ...medication,
    prescriptionUploaded: true
  }));
}

// ─── Medications ─────────────────────────────────────────────────────────────

export async function addMedication(childId, data) {
  const child = findChild(childId);
  if (!child) return null;
  const startDate = data.date || todayDateKey();

  try {
    const saved = await api.createMedication(childId, {
      name: data.name,
      dosage: data.dosage || '',
      instructions: data.instructions || data.notes || '',
      scheduledTime: data.time || '12:00',
      status: data.status || 'Pending',
      frequency: data.frequency || 'DAILY',
      intervalDays: data.intervalDays || null,
      startDate,
      dayPart: 'Specific time'
    });

    child.medications ||= [];
    child.medications.push(saved);

    kindercareStore.medicationTasks.push({
      taskId: `TASK-${saved.medicationId}`,
      medicationId: saved.medicationId,
      childId,
      childName: child.name,
      groupName: child.groupName,
      medicationName: data.name,
      dosage: data.dosage || '',
      scheduledTime: data.time || '12:00',
      scheduledDate: startDate,
      instructions: data.instructions || data.notes || '',
      status: data.status || 'Pending',
      frequency: data.frequency || 'DAILY',
      intervalDays: data.intervalDays || null,
      reminderDue: false
    });

    addNotification({
      title: 'Medication added',
      message: `${data.name} was added for ${child.name}.`,
      type: 'info'
    });

    await loadMedicationTasks();
    return saved;
  } catch (error) {
    addNotification({ title: 'Save failed', message: error.message || 'Could not add medication.', type: 'danger' });
    return null;
  }
}

export async function editMedication(childId, medicationId, data) {
  const currentChild = findMedicationOwner(medicationId) || findChild(childId);
  const medication = currentChild?.medications?.find((item) => item.medicationId === medicationId);
  if (!medication) return null;

  const updates = {
    name: data.name,
    dosage: data.dosage,
    instructions: data.instructions || data.notes || '',
    scheduledTime: data.time || medication.schedule?.specificTime || '12:00',
    status: data.status || medication.todayStatus || 'Pending',
    frequency: data.frequency || medication.schedule?.frequencyCode || 'DAILY',
    intervalDays: data.intervalDays || medication.schedule?.intervalDays || null,
    startDate: data.date || medication.schedule?.startDate || null,
    dayPart: medication.schedule?.dayPart || 'Specific time'
  };

  Object.assign(medication, {
    name: updates.name,
    dosage: updates.dosage,
    instructions: updates.instructions,
    schedule: {
      ...medication.schedule,
      frequencyCode: updates.frequency,
      intervalDays: updates.intervalDays,
      startDate: updates.startDate,
      specificTime: updates.scheduledTime,
      dosage: updates.dosage,
      instructions: updates.instructions
    }
  });

  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);
  if (task) {
    Object.assign(task, {
      medicationName: data.name,
      dosage: data.dosage,
      scheduledTime: updates.scheduledTime,
      frequency: updates.frequency,
      intervalDays: updates.intervalDays,
      scheduledDate: updates.startDate || task.scheduledDate,
      instructions: updates.instructions,
      status: updates.status
    });
  }

  try {
    await api.updateMedication(medicationId, updates);
    await loadMedicationTasks();
    return medication;
  } catch (error) {
    addNotification({ title: 'Save failed', message: error.message || 'Could not update medication.', type: 'danger' });
    await loadChildren();
    await loadMedicationTasks();
    return null;
  }
}

export async function removeMedication(childId, medicationId) {
  const child = findMedicationOwner(medicationId) || findChild(childId);
  if (child) {
    child.medications = child.medications.filter((m) => m.medicationId !== medicationId);
  }
  kindercareStore.medicationTasks = kindercareStore.medicationTasks.filter((t) => t.medicationId !== medicationId);
  try {
    await api.deleteMedication(medicationId);
  } catch {
    addNotification({ title: 'Delete failed', message: 'Could not remove medication from server.', type: 'danger' });
  }
  await loadMedicationTasks();
}

export async function markMedicationTaken(medicationId) {
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);
  if (task) {
    task.status = 'Taken';
    task.reminderDue = false;
  }

  const child = findChild(task?.childId);
  const medication = child?.medications?.find((item) => item.medicationId === medicationId);
  if (medication) {
    medication.todayStatus = 'Taken';
    medication.history ||= [];
    medication.history.unshift({
      id: Date.now(),
      status: 'Taken',
      adminName: 'Admin',
      loggedAt: new Date().toISOString(),
      note: 'Confirmed by staff'
    });
  }

  addNotification({
    title: 'Medication taken',
    message: `${task?.medicationName || 'Medication'} confirmed for ${task?.childName || 'child'}.`,
    type: 'success'
  });

  try {
    await api.markMedicationTaken(medicationId);
    await loadMedicationTasks();
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not confirm medication on server.', type: 'danger' });
  }
}

export async function setMedicationStatus(medicationId, status) {
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);
  if (!task) return;

  const nextStatus = safeStatus(status, task.status || 'Pending');
  const previousStatus = task.status;
  task.status = nextStatus;
  task.reminderDue = false;

  const child = findChild(task.childId);
  const medication = child?.medications?.find((item) => item.medicationId === medicationId);
  if (medication) {
    medication.todayStatus = nextStatus;
  }

  if (previousStatus !== nextStatus && ['Taken', 'Missed'].includes(nextStatus)) {
    addNotification({
      title: nextStatus === 'Taken' ? 'Medication taken' : 'Missed medication',
      message: `${task.medicationName || 'Medication'} for ${task.childName || 'child'} marked ${nextStatus}.`,
      type: statusNotificationType(nextStatus)
    });
  }

  try {
    await api.updateMedicationStatus(medicationId, nextStatus);
    await loadMedicationTasks();
  } catch {
    addNotification({ title: 'Save failed', message: 'Could not update status on server.', type: 'danger' });
  }
}
