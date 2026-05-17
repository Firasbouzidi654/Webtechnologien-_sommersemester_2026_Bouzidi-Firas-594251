import { reactive } from 'vue';
import { children, cloneMockData, medicationTasks } from '../data/kindercareMockData';

export const MEDICATION_STATUSES = ['Pending', 'Taken', 'Missed', 'Upcoming'];

export const kindercareStore = reactive({
  children: cloneMockData(children),
  medicationTasks: cloneMockData(medicationTasks),
  parentChildIds: [1, 3],
  parentNotes: {},
  parentAvatar: null,
  verificationLogs: [],
  notifications: [],
  toasts: []
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

function nextId(items) {
  return Math.max(0, ...items.map((item) => Number(item.id) || 0)) + 1;
}

function statusNotificationType(status) {
  if (status === 'Taken') return 'success';
  if (status === 'Missed') return 'danger';
  if (status === 'Pending') return 'warning';
  return 'info';
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

export function markNotificationsRead() {
  kindercareStore.notifications.forEach((notification) => {
    notification.read = true;
  });
}

export function nextMedicationId() {
  const highestNumber = kindercareStore.children
    .flatMap((child) => child.medications || [])
    .map((medication) => Number(medication.medicationId.replace('MED-', '')))
    .filter((number) => Number.isFinite(number))
    .reduce((highest, number) => Math.max(highest, number), 0);

  return `MED-${String(highestNumber + 1).padStart(3, '0')}`;
}

export function taskReminderDue(task) {
  if (!task || task.status !== 'Pending' || !task.scheduledTime) {
    return false;
  }

  const taskDate = task.scheduledDate || todayDateKey();
  return taskDate === todayDateKey() && task.scheduledTime <= new Date().toTimeString().slice(0, 5);
}

export function parentChildren() {
  return kindercareStore.children.filter((child) => kindercareStore.parentChildIds.includes(child.id));
}

export function addChild(data) {
  const child = {
    id: nextId(kindercareStore.children),
    name: data.name,
    groupName: data.groupName,
    dateOfBirth: data.dateOfBirth,
    parentName: 'Sara Schneider',
    photo: data.photo || null,
    parentEmail: 'sara.schneider@example.com',
    photo: null,
    allergies: [],
    chronicDiseases: [],
    healthNotes: '',
    medications: [],
    emergencyContacts: []
  };

  kindercareStore.children.push(child);
  kindercareStore.parentChildIds.push(child.id);
  return child;
}

export function setParentAvatar(dataUrl) {
  kindercareStore.parentAvatar = dataUrl;
}

export function setChildPhoto(childId, dataUrl) {
  const child = findChild(childId);

  if (child) {
    child.photo = dataUrl;
  }
}

export function addVerificationLog(entry) {
  kindercareStore.verificationLogs.unshift({ id: Date.now(), ...entry });
}

export function addAllergy(childId, name) {
  const child = findChild(childId);
  if (!child) return null;
  child.allergies = [...(child.allergies || []).filter((item) => item !== 'None known'), name];
}

export function editAllergy(childId, index, name) {
  const child = findChild(childId);
  if (!child) return;
  child.allergies ||= [];
  child.allergies.splice(index, 1, name);
}

export function removeAllergy(childId, index) {
  findChild(childId)?.allergies.splice(index, 1);
}

export function addDisease(childId, name) {
  const child = findChild(childId);
  if (!child) return null;
  child.chronicDiseases = [...(child.chronicDiseases || []).filter((item) => item !== 'None'), name];
}

export function editDisease(childId, index, name) {
  const child = findChild(childId);
  if (!child) return;
  child.chronicDiseases ||= [];
  child.chronicDiseases.splice(index, 1, name);
}

export function removeDisease(childId, index) {
  findChild(childId)?.chronicDiseases.splice(index, 1);
}

export function addEmergencyContact(childId, data) {
  const child = findChild(childId);
  if (!child) return null;
  child.emergencyContacts ||= [];
  const contact = {
    id: nextId(child.emergencyContacts),
    name: data.name,
    relationship: data.relationship,
    phone: data.phone,
    email: data.email,
    priority: Number(data.priority) || child.emergencyContacts.length + 1
  };

  child.emergencyContacts.push(contact);
  child.emergencyContacts.sort((first, second) => first.priority - second.priority);
  return contact;
}

export function addMedication(childId, data) {
  const child = findChild(childId);
  if (!child) {
    return null;
  }

  child.medications ||= [];
  const medicationId = nextMedicationId();
  const status = safeStatus(data.status);
  const date = data.date || todayDateKey();
  const time = data.time || '12:00';
  const instructions = data.instructions || data.notes || '';
  const medication = {
    id: Date.now(),
    medicationId,
    childId,
    childName: child.name,
    name: data.name,
    activeIngredient: '',
    dosage: data.dosage || '',
    instructions,
    prescriptionUploaded: false,
    todayStatus: status,
    qrPayload: `kindercare-connect:medication:${medicationId}`,
    schedule: {
      frequency: 'Daily',
      dayPart: 'Specific time',
      specificTime: time,
      date,
      dosage: data.dosage || '',
      instructions
    },
    history: []
  };

  child.medications.push(medication);
  kindercareStore.medicationTasks.push({
    taskId: `TASK-${String(kindercareStore.medicationTasks.length + 1).padStart(3, '0')}`,
    medicationId,
    childId,
    childName: child.name,
    groupName: child.groupName,
    medicationName: data.name,
    dosage: data.dosage || '',
    scheduledTime: time,
    scheduledDate: date,
    instructions,
    status,
    reminderDue: false,
    qrPayload: medication.qrPayload
  });

  addNotification({
    title: 'New medication added',
    message: `${data.name || 'Medication'} was added for ${child.name}.`,
    type: 'info'
  });

  return medication;
}

export function editMedication(childId, medicationId, data) {
  const currentChild = findMedicationOwner(medicationId) || findChild(childId);
  const nextChild = findChild(data.childId ?? childId) || currentChild;
  const medication = currentChild?.medications?.find((item) => item.medicationId === medicationId);
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);

  if (!currentChild || !nextChild || !medication) {
    return null;
  }

  const status = safeStatus(data.status, task?.status || medication.todayStatus || 'Pending');
  const date = data.date || medication.schedule?.date || task?.scheduledDate || todayDateKey();
  const time = data.time || medication.schedule?.specificTime || task?.scheduledTime || '12:00';
  const instructions = data.instructions || data.notes || '';

  if (currentChild.id !== nextChild.id) {
    currentChild.medications = currentChild.medications.filter((item) => item.medicationId !== medicationId);
    nextChild.medications ||= [];
    nextChild.medications.push(medication);
  }

  Object.assign(medication, {
    childId: nextChild.id,
    childName: nextChild.name,
    name: data.name,
    dosage: data.dosage,
    instructions,
    todayStatus: status,
    schedule: {
      ...medication.schedule,
      specificTime: time,
      date,
      dosage: data.dosage,
      instructions
    }
  });

  if (task) {
    Object.assign(task, {
      childId: nextChild.id,
      childName: nextChild.name,
      groupName: nextChild.groupName,
      medicationName: data.name,
      dosage: data.dosage,
      scheduledTime: time,
      scheduledDate: date,
      instructions,
      status,
      reminderDue: false
    });
  }

  if (status === 'Taken' && medication.history?.[0]?.status !== 'Taken') {
    medication.history.unshift({
      id: Date.now(),
      status: 'Taken',
      adminName: 'Ms. Mueller',
      loggedAt: new Date().toISOString(),
      note: 'Status updated by staff'
    });
  }

  return medication;
}

export function removeMedication(childId, medicationId) {
  const child = findMedicationOwner(medicationId) || findChild(childId);
  if (!child) {
    return;
  }

  child.medications = child.medications.filter((medication) => medication.medicationId !== medicationId);
  kindercareStore.medicationTasks = kindercareStore.medicationTasks.filter((task) => task.medicationId !== medicationId);
}

export function uploadPrescription(childId, fileName) {
  const child = findChild(childId);
  if (!child) return;
  child.prescriptionFileName = fileName;
  child.medications = (child.medications || []).map((medication) => ({
    ...medication,
    prescriptionUploaded: true
  }));
}

export function saveParentNote(childId, note) {
  kindercareStore.parentNotes[childId] = note;
  const child = findChild(childId);

  addNotification({
    title: 'New parent note',
    message: `${child?.parentName || 'A parent'} updated the note for ${child?.name || 'a child'}.`,
    type: 'info'
  });
}

export function markMedicationTaken(medicationId) {
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);

  if (!task) {
    return;
  }

  task.status = 'Taken';
  task.reminderDue = false;

  const child = findChild(task.childId);
  const medication = child?.medications?.find((item) => item.medicationId === medicationId);

  if (medication) {
    medication.todayStatus = 'Taken';
    medication.history.unshift({
      id: Date.now(),
      status: 'Taken',
      adminName: 'Ms. Mueller',
      loggedAt: new Date().toISOString(),
      note: 'Confirmed by staff from admin dashboard'
    });
  }

  addNotification({
    title: 'Medication taken',
    message: `${task.medicationName || 'Medication'} confirmed for ${task.childName || 'child'}.`,
    type: 'success'
  });
}

export function setMedicationStatus(medicationId, status) {
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);
  if (!task) {
    return;
  }

  const nextStatus = safeStatus(status, task.status || 'Pending');
  const previousStatus = task.status;
  task.status = nextStatus;
  task.reminderDue = false;

  const child = findChild(task.childId);
  const medication = child?.medications?.find((item) => item.medicationId === medicationId);

  if (!medication) {
    return;
  }

  medication.todayStatus = nextStatus;

  if (nextStatus === 'Taken' && previousStatus !== 'Taken') {
    medication.history.unshift({
      id: Date.now(),
      status: nextStatus,
      adminName: 'Ms. Mueller',
      loggedAt: new Date().toISOString(),
      note: 'Status updated by staff'
    });
  }

  if (previousStatus !== nextStatus && ['Taken', 'Missed'].includes(nextStatus)) {
    addNotification({
      title: nextStatus === 'Taken' ? 'Medication taken' : 'Missed medication',
      message: `${task.medicationName || 'Medication'} for ${task.childName || 'child'} marked ${nextStatus}.`,
      type: statusNotificationType(nextStatus)
    });
  }
}
