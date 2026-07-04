import { reactive } from 'vue';
import { api } from '../services/api';

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
  kindercareStore.children = apiChildren;
}

export async function loadChildren() {
  kindercareStore.loading = true;
  try {
    const children = await api.getChildren();
    mergeChildrenFromApi(children);
    kindercareStore.parentChildIds = children.map((child) => child.id);
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

export function markNotificationsRead(filter = () => true) {
  kindercareStore.notifications.forEach((notification) => {
    if (filter(notification)) {
      notification.read = true;
    }
  });
}

export function taskReminderDue(task) {
  if (!task || task.status !== 'Pending' || !task.scheduledTime) {
    return false;
  }
  const taskDate = task.scheduledDate || todayDateKey();
  return taskDate === todayDateKey() && task.scheduledTime <= new Date().toTimeString().slice(0, 5);
}

export function parentChildren() {
  return kindercareStore.children;
}

export function setParentAvatar(dataUrl) {
  kindercareStore.parentAvatar = dataUrl;
}

export async function addChild(data) {
  let child;
  try {
    child = await api.createChild({
      name: data.name,
      allergies: ''
    });
  } catch {
    addNotification({
      title: 'Could not add child',
      message: 'Server unavailable. Please check your connection and try again.',
      type: 'danger'
    });
    return null;
  }

  if (!kindercareStore.children.find((c) => c.id === child.id)) {
    kindercareStore.children.push(child);
  }
  if (!kindercareStore.parentChildIds.includes(child.id)) {
    kindercareStore.parentChildIds.push(child.id);
  }

  addNotification({
    title: 'Child added',
    message: `${child.name}'s profile was saved successfully.`,
    type: 'success'
  });
  return child;
}

export async function deleteChild(childId) {
  kindercareStore.children = kindercareStore.children.filter((c) => c.id !== childId);
  kindercareStore.parentChildIds = kindercareStore.parentChildIds.filter((id) => id !== childId);

  try {
    await api.deleteChild(childId);
    addNotification({ title: 'Child removed', message: 'Child profile deleted successfully.', type: 'success' });
  } catch {
    addNotification({ title: 'Delete failed', message: 'Could not delete child from server.', type: 'danger' });
    await loadChildren();
  }
}

export async function addAllergy(childId, name) {
  const child = findChild(childId);
  if (!child) return null;
  const allergy = String(name || '').trim();
  if (!allergy) {
    addNotification({ title: 'Allergy required', message: 'Enter an allergy before saving.', type: 'warning' });
    return null;
  }

  const updated = [...(child.allergies || []).filter((item) => item !== 'None known'), allergy];
  child.allergies = updated;
  try {
    await api.updateChild(childId, { allergies: updated });
    addNotification({
      title: 'New allergy recorded',
      message: `${allergy} was recorded for ${child.name}.`,
      type: 'warning'
    });
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

export async function addMedication(childId, data) {
  const child = findChild(childId);
  if (!child) return null;
  const scheduledDate = data.date || todayDateKey();

  try {
    const saved = await api.createMedication(childId, {
      name: data.name,
      dosage: data.dosage || '',
      scheduledTime: data.time || '12:00',
      status: data.status || 'Pending',
      scheduledDate,
      dayPart: 'Specific time'
    });

    child.medications ||= [];
    child.medications.push(saved);

    kindercareStore.medicationTasks.push({
      taskId: `TASK-${saved.medicationId}`,
      medicationId: saved.medicationId,
      childId,
      childName: child.name,
      medicationName: data.name,
      dosage: data.dosage || '',
      scheduledTime: data.time || '12:00',
      scheduledDate,
      status: data.status || 'Pending',
      scheduledToday: scheduledDate === todayDateKey(),
      reminderDue: false
    });

    addNotification({
      title: 'Medication scheduled',
      message: `${data.name} was scheduled for ${child.name}.`,
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
    scheduledTime: data.time || medication.schedule?.specificTime || '12:00',
    status: data.status || medication.todayStatus || 'Pending',
    scheduledDate: data.date || medication.schedule?.scheduledDate || medication.schedule?.date || null,
    dayPart: medication.schedule?.dayPart || 'Specific time'
  };

  Object.assign(medication, {
    name: updates.name,
    dosage: updates.dosage,
    schedule: {
      ...medication.schedule,
      date: updates.scheduledDate,
      scheduledDate: updates.scheduledDate,
      specificTime: updates.scheduledTime,
      dosage: updates.dosage,
    }
  });

  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);
  if (task) {
    Object.assign(task, {
      medicationName: data.name,
      dosage: data.dosage,
      scheduledTime: updates.scheduledTime,
      scheduledDate: updates.scheduledDate || task.scheduledDate,
      scheduledToday: (updates.scheduledDate || task.scheduledDate) === todayDateKey(),
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
    });
  }

  addNotification({
    title: 'Medication administered',
    message: `${task?.medicationName || 'Medication'} was administered to ${task?.childName || 'child'}.`,
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
      title: nextStatus === 'Taken' ? 'Medication administered' : 'Medication missed',
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
