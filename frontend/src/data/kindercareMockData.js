export const children = [
  {
    id: 1,
    name: 'Adam Schneider',
    groupName: 'Sunflowers',
    dateOfBirth: '2021-03-15',
    parentName: 'Sara Schneider',
    parentEmail: 'sara.schneider@example.com',
    allergies: ['Peanuts'],
    chronicDiseases: ['Mild asthma'],
    healthNotes: 'Needs inhaler available during outdoor play.',
    medications: [
      {
        id: 101,
        medicationId: 'MED-001',
        childId: 1,
        childName: 'Adam Schneider',
        name: 'Asthma Spray',
        activeIngredient: 'Salbutamol',
        dosage: '1 puff',
        instructions: 'Shake before use.',
        prescriptionUploaded: true,
        qrPayload: 'kindercare-connect:medication:MED-001',
        schedule: {
          frequency: 'Daily',
          dayPart: 'Morning',
          specificTime: '08:30',
          dosage: '1 puff',
          instructions: 'Shake before use.'
        },
        history: [
          {
            id: 2101,
            status: 'Taken',
            adminName: 'M. Keller',
            loggedAt: '2026-04-27T08:30:00',
            note: 'Sample previous-day log'
          }
        ]
      }
    ],
    emergencyContacts: [
      {
        id: 1,
        name: 'Sara Schneider',
        relationship: 'Mother',
        phone: '+49 151 123456',
        email: 'sara.schneider@example.com',
        priority: 1
      },
      {
        id: 2,
        name: 'Paul Schneider',
        relationship: 'Father',
        phone: '+49 152 222333',
        email: 'paul.schneider@example.com',
        priority: 2
      }
    ]
  },
  {
    id: 2,
    name: 'Noah Becker',
    groupName: 'Sunflowers',
    dateOfBirth: '2020-07-22',
    parentName: 'Jonas Becker',
    parentEmail: 'jonas.becker@example.com',
    allergies: ['None known'],
    chronicDiseases: ['Asthma'],
    healthNotes: 'Observe breathing after sports activities.',
    medications: [
      {
        id: 102,
        medicationId: 'MED-002',
        childId: 2,
        childName: 'Noah Becker',
        name: 'Asthma Spray',
        activeIngredient: 'Salbutamol',
        dosage: '1 puff',
        instructions: 'Use spacer and monitor breathing.',
        prescriptionUploaded: false,
        qrPayload: 'kindercare-connect:medication:MED-002',
        schedule: {
          frequency: 'Daily',
          dayPart: 'Specific time',
          specificTime: '10:00',
          dosage: '1 puff',
          instructions: 'Use spacer and monitor breathing.'
        },
        history: [
          {
            id: 2102,
            status: 'Taken',
            adminName: 'M. Keller',
            loggedAt: '2026-04-27T10:00:00',
            note: 'Sample previous-day log'
          }
        ]
      },
      {
        id: 103,
        medicationId: 'MED-003',
        childId: 2,
        childName: 'Noah Becker',
        name: 'Vitamin D Drops',
        activeIngredient: 'Colecalciferol',
        dosage: '2 drops',
        instructions: 'Give after snack.',
        prescriptionUploaded: true,
        qrPayload: 'kindercare-connect:medication:MED-003',
        schedule: {
          frequency: 'Daily',
          dayPart: 'Daily',
          specificTime: '12:30',
          dosage: '2 drops',
          instructions: 'Give after snack.'
        },
        history: [
          {
            id: 2103,
            status: 'Taken',
            adminName: 'M. Keller',
            loggedAt: '2026-04-27T12:30:00',
            note: 'Sample previous-day log'
          }
        ]
      }
    ],
    emergencyContacts: [
      {
        id: 3,
        name: 'Jonas Becker',
        relationship: 'Father',
        phone: '+49 152 987654',
        email: 'jonas.becker@example.com',
        priority: 1
      }
    ]
  },
  {
    id: 3,
    name: 'Lina Wagner',
    groupName: 'Rainbows',
    dateOfBirth: '2021-01-10',
    parentName: 'Amira Wagner',
    parentEmail: 'amira.wagner@example.com',
    allergies: ['Bee stings'],
    chronicDiseases: ['None'],
    healthNotes: 'Emergency spray is stored in the office cabinet.',
    medications: [
      {
        id: 104,
        medicationId: 'MED-004',
        childId: 3,
        childName: 'Lina Wagner',
        name: 'Allergy Spray',
        activeIngredient: 'Antihistamine',
        dosage: '1 spray',
        instructions: 'Use only if symptoms appear.',
        prescriptionUploaded: true,
        qrPayload: 'kindercare-connect:medication:MED-004',
        schedule: {
          frequency: 'Daily',
          dayPart: 'Morning',
          specificTime: '09:15',
          dosage: '1 spray',
          instructions: 'Use only if symptoms appear.'
        },
        history: [
          {
            id: 2104,
            status: 'Taken',
            adminName: 'M. Keller',
            loggedAt: '2026-04-27T09:15:00',
            note: 'Sample previous-day log'
          }
        ]
      }
    ],
    emergencyContacts: [
      {
        id: 5,
        name: 'Amira Wagner',
        relationship: 'Mother',
        phone: '+49 176 456789',
        email: 'amira.wagner@example.com',
        priority: 1
      }
    ]
  }
];

export const medicationTasks = [
  {
    taskId: 'TASK-001',
    medicationId: 'MED-001',
    childId: 1,
    childName: 'Adam Schneider',
    groupName: 'Sunflowers',
    medicationName: 'Asthma Spray',
    dosage: '1 puff',
    scheduledTime: '08:30',
    instructions: 'Shake before use.',
    status: 'Pending',
    reminderDue: true,
    qrPayload: 'kindercare-connect:medication:MED-001'
  },
  {
    taskId: 'TASK-002',
    medicationId: 'MED-002',
    childId: 2,
    childName: 'Noah Becker',
    groupName: 'Sunflowers',
    medicationName: 'Asthma Spray',
    dosage: '1 puff',
    scheduledTime: '10:00',
    instructions: 'Use spacer and monitor breathing.',
    status: 'Taken',
    reminderDue: false,
    qrPayload: 'kindercare-connect:medication:MED-002'
  },
  {
    taskId: 'TASK-003',
    medicationId: 'MED-003',
    childId: 2,
    childName: 'Noah Becker',
    groupName: 'Sunflowers',
    medicationName: 'Vitamin D Drops',
    dosage: '2 drops',
    scheduledTime: '12:30',
    instructions: 'Give after snack.',
    status: 'Pending',
    reminderDue: false,
    qrPayload: 'kindercare-connect:medication:MED-003'
  },
  {
    taskId: 'TASK-004',
    medicationId: 'MED-004',
    childId: 3,
    childName: 'Lina Wagner',
    groupName: 'Rainbows',
    medicationName: 'Allergy Spray',
    dosage: '1 spray',
    scheduledTime: '09:15',
    instructions: 'Use only if symptoms appear.',
    status: 'Missed',
    reminderDue: false,
    qrPayload: 'kindercare-connect:medication:MED-004'
  }
];

export function cloneMockData(data) {
  return JSON.parse(JSON.stringify(data));
}
