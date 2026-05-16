import { reactive } from 'vue';
import { children, cloneMockData, medicationTasks } from '../data/kindercareMockData';

export const kindercareStore = reactive({
  children: cloneMockData(children),
  medicationTasks: cloneMockData(medicationTasks),
  parentChildIds: [1, 3],
  parentNotes: {},
  parentAvatar: null,
  verificationLogs: []
});

function findChild(childId) {
  return kindercareStore.children.find((child) => child.id === childId);
}

function nextId(items) {
  return Math.max(0, ...items.map((item) => Number(item.id) || 0)) + 1;
}

export function nextMedicationId() {
  const highestNumber = kindercareStore.children
    .flatMap((child) => child.medications)
    .map((medication) => Number(medication.medicationId.replace('MED-', '')))
    .filter((number) => Number.isFinite(number))
    .reduce((highest, number) => Math.max(highest, number), 0);

  return `MED-${String(highestNumber + 1).padStart(3, '0')}`;
}

export function taskReminderDue(task) {
  return task.status === 'Pending' && task.scheduledTime <= new Date().toTimeString().slice(0, 5);
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
  child.allergies = [...child.allergies.filter((item) => item !== 'None known'), name];
}

export function editAllergy(childId, index, name) {
  const child = findChild(childId);
  child.allergies.splice(index, 1, name);
}

export function removeAllergy(childId, index) {
  findChild(childId).allergies.splice(index, 1);
}

export function addDisease(childId, name) {
  const child = findChild(childId);
  child.chronicDiseases = [...child.chronicDiseases.filter((item) => item !== 'None'), name];
}

export function editDisease(childId, index, name) {
  const child = findChild(childId);
  child.chronicDiseases.splice(index, 1, name);
}

export function removeDisease(childId, index) {
  findChild(childId).chronicDiseases.splice(index, 1);
}

export function addEmergencyContact(childId, data) {
  const child = findChild(childId);
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
  const medicationId = nextMedicationId();
  const medication = {
    id: Date.now(),
    medicationId,
    childId,
    childName: child.name,
    name: data.name,
    activeIngredient: '',
    dosage: data.dosage,
    instructions: data.instructions,
    prescriptionUploaded: false,
    todayStatus: 'Upcoming',
    qrPayload: `kindercare-connect:medication:${medicationId}`,
    schedule: {
      frequency: 'Daily',
      dayPart: 'Specific time',
      specificTime: data.time,
      dosage: data.dosage,
      instructions: data.instructions
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
    dosage: data.dosage,
    scheduledTime: data.time,
    instructions: data.instructions,
    status: 'Pending',
    reminderDue: false,
    qrPayload: medication.qrPayload
  });

  return medication;
}

export function editMedication(childId, medicationId, data) {
  const child = findChild(childId);
  const medication = child.medications.find((item) => item.medicationId === medicationId);
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);

  Object.assign(medication, {
    name: data.name,
    dosage: data.dosage,
    instructions: data.instructions,
    schedule: {
      ...medication.schedule,
      specificTime: data.time,
      dosage: data.dosage,
      instructions: data.instructions
    }
  });

  if (task) {
    Object.assign(task, {
      medicationName: data.name,
      dosage: data.dosage,
      scheduledTime: data.time,
      instructions: data.instructions
    });
  }
}

export function removeMedication(childId, medicationId) {
  const child = findChild(childId);
  child.medications = child.medications.filter((medication) => medication.medicationId !== medicationId);
  kindercareStore.medicationTasks = kindercareStore.medicationTasks.filter((task) => task.medicationId !== medicationId);
}

export function uploadPrescription(childId, fileName) {
  const child = findChild(childId);
  child.prescriptionFileName = fileName;
  child.medications = child.medications.map((medication) => ({
    ...medication,
    prescriptionUploaded: true
  }));
}

export function saveParentNote(childId, note) {
  kindercareStore.parentNotes[childId] = note;
}

export function markMedicationTaken(medicationId) {
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);

  if (!task) {
    return;
  }

  task.status = 'Taken';
  task.reminderDue = false;

  const child = findChild(task.childId);
  const medication = child.medications.find((item) => item.medicationId === medicationId);

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
}

export function setMedicationStatus(medicationId, status) {
  const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medicationId);
  if (!task) {
    return;
  }

  task.status = status;
  task.reminderDue = false;

  const child = findChild(task.childId);
  const medication = child?.medications.find((item) => item.medicationId === medicationId);

  if (!medication) {
    return;
  }

  medication.todayStatus = status;

  if (status === 'Taken') {
    medication.history.unshift({
      id: Date.now(),
      status: 'Taken',
      adminName: 'Ms. Mueller',
      loggedAt: new Date().toISOString(),
      note: 'Status updated by staff'
    });
  }
}
