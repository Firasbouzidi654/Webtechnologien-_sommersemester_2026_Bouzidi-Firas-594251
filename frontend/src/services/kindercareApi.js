import { children, cloneMockData, medicationTasks } from '../data/kindercareMockData';

export async function getChildren() {
  return cloneMockData(children);
}

export async function getMedicationTasks() {
  return cloneMockData(medicationTasks);
}

export async function getMedications() {
  return cloneMockData(children.flatMap((child) => child.medications));
}

export async function markMedicationTaken(medicationId, tasks) {
  return tasks.map((task) => (
    task.medicationId === medicationId
      ? { ...task, status: 'Taken', reminderDue: false }
      : task
  ));
}
