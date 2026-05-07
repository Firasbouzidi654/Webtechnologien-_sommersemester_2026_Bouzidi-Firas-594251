<template>
  <main class="parent-dashboard">
    <nav class="topbar">
      <div>
        <p class="eyebrow">Parent dashboard</p>
        <h1>KinderCare Connect</h1>
      </div>
      <button class="logout-button" type="button" @click="$emit('navigate', '/login')">Log out</button>
    </nav>

    <section class="welcome-panel">
      <div class="welcome-copy">
        <p class="eyebrow">Welcome back, Sara</p>
        <h2>Here is {{ firstName(selectedChild.name) }}'s health overview for today.</h2>
        <p>
          A simple place to keep medication, allergies, emergency contacts, and notes ready for the kindergarten team.
        </p>
      </div>
      <img :src="heroImage" alt="Child-friendly health care overview" />
    </section>

    <section class="child-toolbar">
      <label>
        <span>Selected child</span>
        <select v-model.number="selectedChildId">
          <option v-for="child in parentChildren" :key="child.id" :value="child.id">
            {{ child.name }} - {{ child.groupName }}
          </option>
        </select>
      </label>
      <button type="button" @click="openDialog('child')">Add child</button>
    </section>

    <section class="quick-actions" aria-label="Parent quick actions">
      <button
        v-for="action in quickActions"
        :key="action.key"
        type="button"
        @click="handleQuickAction(action.key)"
      >
        <span>{{ action.icon }}</span>
        {{ action.label }}
      </button>
    </section>

    <section class="care-cues">
      <article v-for="cue in careCues" :key="cue.title">
        <span>{{ cue.icon }}</span>
        <div>
          <h3>{{ cue.title }}</h3>
          <p>{{ cue.text }}</p>
        </div>
      </article>
    </section>

    <section class="health-summary">
      <article>
        <p>Allergies</p>
        <strong>{{ meaningfulItems(selectedChild.allergies).length }}</strong>
        <span>{{ allergySummary }}</span>
      </article>
      <article>
        <p>Chronic diseases</p>
        <strong>{{ meaningfulItems(selectedChild.chronicDiseases).length }}</strong>
        <span>{{ chronicDiseaseSummary }}</span>
      </article>
      <article>
        <p>Emergency contacts</p>
        <strong>{{ selectedChild.emergencyContacts.length }}</strong>
        <span>{{ emergencyContactSummary }}</span>
      </article>
      <article>
        <p>Prescription status</p>
        <strong>{{ prescriptionStatus.label }}</strong>
        <span>{{ prescriptionStatus.detail }}</span>
      </article>
    </section>

    <section class="manage-grid">
      <section class="panel compact-list">
        <header>
          <div>
            <p class="eyebrow">Manage</p>
            <h2>Allergies</h2>
          </div>
          <button type="button" @click="openDialog('allergy')">Add allergy</button>
        </header>
        <article v-for="(allergy, index) in meaningfulItems(selectedChild.allergies)" :key="`${allergy}-${index}`">
          <strong>{{ allergy }}</strong>
          <span class="item-actions">
            <button type="button" @click="editAllergyItem(index, allergy)">Edit</button>
            <button type="button" @click="removeAllergyItem(index)">Remove</button>
          </span>
        </article>
        <p v-if="meaningfulItems(selectedChild.allergies).length === 0" class="empty-state">
          No allergy recorded yet.
        </p>
      </section>

      <section class="panel compact-list">
        <header>
          <div>
            <p class="eyebrow">Manage</p>
            <h2>Chronic diseases</h2>
          </div>
          <button type="button" @click="openDialog('disease')">Add disease</button>
        </header>
        <article v-for="(disease, index) in meaningfulItems(selectedChild.chronicDiseases)" :key="`${disease}-${index}`">
          <strong>{{ disease }}</strong>
          <span class="item-actions">
            <button type="button" @click="editDiseaseItem(index, disease)">Edit</button>
            <button type="button" @click="removeDiseaseItem(index)">Remove</button>
          </span>
        </article>
        <p v-if="meaningfulItems(selectedChild.chronicDiseases).length === 0" class="empty-state">
          No chronic disease recorded yet.
        </p>
      </section>
    </section>

    <section class="dashboard-grid">
      <section class="panel medication-panel">
        <header>
          <div>
            <p class="eyebrow">Today</p>
            <h2>Medication timeline</h2>
          </div>
          <span>{{ medicationTimeline.length }} item(s)</span>
        </header>

        <div class="status-legend">
          <span
            v-for="status in timelineStatuses"
            :key="status"
            class="status-badge"
            :class="statusClass(status)"
          >
            {{ status }}
          </span>
        </div>

        <div class="timeline">
          <article
            v-for="medication in medicationTimeline"
            :key="medication.medicationId"
            class="timeline-item"
            :class="statusClass(medication.status)"
          >
            <time>{{ medication.schedule.specificTime }}</time>
            <div>
              <h3>{{ medication.name }}</h3>
              <p>{{ medication.dosage }} - {{ medication.instructions }}</p>
              <small>{{ medication.medicationId }}</small>
              <div class="inline-actions">
                <button type="button" @click="toggleQr(medication.medicationId)">
                  {{ selectedQrId === medication.medicationId ? 'Hide QR' : 'View QR code / Medication ID' }}
                </button>
                <button type="button" @click="editMedicationItem(medication)">Edit</button>
                <button type="button" @click="removeMedicationItem(medication.medicationId)">Remove</button>
              </div>
              <div v-if="selectedQrId === medication.medicationId" class="qr-inline">
                <div class="mock-qr">
                  <span v-for="cell in qrCells(medication.medicationId)" :key="cell" :class="{ filled: cell % 2 === 0 || cell % 7 === 0 }"></span>
                </div>
                <p>QR payload: {{ medication.qrPayload }}</p>
              </div>
            </div>
            <span class="status-badge">{{ medication.status }}</span>
          </article>
          <p v-if="medicationTimeline.length === 0" class="empty-state">
            No medication scheduled for today.
          </p>
        </div>

        <section class="status-explainer">
          <p><strong>Upcoming</strong> = scheduled later</p>
          <p><strong>Pending</strong> = waiting for confirmation</p>
          <p><strong>Taken</strong> = confirmed by staff</p>
          <p><strong>Missed</strong> = time passed without confirmation</p>
        </section>
      </section>

      <aside class="side-stack">
        <section class="panel update-card">
          <p class="eyebrow">Reminder</p>
          <h2>{{ nextMedicationReminder }}</h2>
        </section>

        <section class="panel update-card">
          <p class="eyebrow">Last kindergarten update</p>
          <h2>{{ lastKindergartenUpdate }}</h2>
        </section>

        <section class="panel compact-list">
          <header>
            <div>
              <p class="eyebrow">Medication history</p>
              <h2>Staff confirmations</h2>
            </div>
          </header>
          <article v-for="entry in medicationHistory" :key="entry.id">
            <strong>{{ entry.time }} {{ entry.medicationName }}</strong>
            <span>{{ entry.status }} by {{ entry.adminName }}</span>
          </article>
          <p v-if="medicationHistory.length === 0" class="empty-state">
            No medication confirmation history yet.
          </p>
        </section>

        <section class="panel note-card">
          <header>
            <div>
              <p class="eyebrow">Parent note</p>
              <h2>Send note to staff</h2>
            </div>
          </header>
          <textarea
            ref="noteTextarea"
            v-model="parentNoteDraft"
            rows="5"
            placeholder="Write a note for the kindergarten team..."
          ></textarea>
          <button type="button" @click="saveParentNote">Save note</button>
          <p v-if="noteFeedback" class="feedback">{{ noteFeedback }}</p>
          <p v-if="savedParentNote" class="saved-note">{{ savedParentNote }}</p>
        </section>

        <section class="panel compact-list">
          <header>
            <div>
              <p class="eyebrow">Contacts</p>
              <h2>Emergency contacts</h2>
            </div>
            <button type="button" @click="openDialog('emergency')">Add emergency contact</button>
          </header>
          <article v-for="contact in selectedChild.emergencyContacts" :key="contact.id">
            <strong>{{ contact.name }}</strong>
            <span>{{ contact.relationship }} - {{ contact.phone }}</span>
          </article>
          <p v-if="selectedChild.emergencyContacts.length === 0" class="empty-state">
            No emergency contact added yet.
          </p>
        </section>
      </aside>
    </section>

    <input
      ref="prescriptionInput"
      class="hidden-input"
      type="file"
      accept=".pdf,.png,.jpg,.jpeg"
      @change="handlePrescriptionFile"
    />

    <section v-if="activeDialog" class="modal-backdrop" @click.self="closeDialog">
      <form class="modal" @submit.prevent="submitDialog">
        <header>
          <h2>{{ dialogTitle }}</h2>
          <button type="button" aria-label="Close dialog" @click="closeDialog">x</button>
        </header>

        <template v-if="activeDialog === 'child'">
          <label>
            <span>Child name</span>
            <input v-model.trim="forms.child.name" type="text" required />
          </label>
          <label>
            <span>Group / class</span>
            <input v-model.trim="forms.child.groupName" type="text" required />
          </label>
          <label>
            <span>Date of birth</span>
            <input v-model="forms.child.dateOfBirth" type="date" required />
          </label>
        </template>

        <template v-if="activeDialog === 'emergency'">
          <label>
            <span>Name</span>
            <input v-model.trim="forms.emergency.name" type="text" required />
          </label>
          <label>
            <span>Relationship</span>
            <input v-model.trim="forms.emergency.relationship" type="text" required />
          </label>
          <label>
            <span>Phone</span>
            <input v-model.trim="forms.emergency.phone" type="tel" required />
          </label>
          <label>
            <span>Email</span>
            <input v-model.trim="forms.emergency.email" type="email" required />
          </label>
          <label>
            <span>Priority</span>
            <input v-model.number="forms.emergency.priority" type="number" min="1" required />
          </label>
        </template>

        <template v-if="activeDialog === 'allergy'">
          <label>
            <span>Suggested allergy</span>
            <select v-model="forms.allergy.suggestion" @change="applySuggestion('allergy')">
              <option value="">Choose a suggestion</option>
              <option v-for="suggestion in allergySuggestions" :key="suggestion" :value="suggestion">
                {{ suggestion }}
              </option>
            </select>
          </label>
          <label>
            <span>Allergy name or custom value</span>
            <input v-model.trim="forms.allergy.name" type="text" required />
          </label>
        </template>

        <template v-if="activeDialog === 'disease'">
          <label>
            <span>Suggested chronic disease</span>
            <select v-model="forms.disease.suggestion" @change="applySuggestion('disease')">
              <option value="">Choose a suggestion</option>
              <option v-for="suggestion in diseaseSuggestions" :key="suggestion" :value="suggestion">
                {{ suggestion }}
              </option>
            </select>
          </label>
          <label>
            <span>Chronic disease name or custom value</span>
            <input v-model.trim="forms.disease.name" type="text" required />
          </label>
        </template>

        <template v-if="activeDialog === 'medication'">
          <label>
            <span>Suggested medication</span>
            <select v-model="forms.medication.suggestion" @change="applySuggestion('medication')">
              <option value="">Choose a suggestion</option>
              <option v-for="suggestion in medicationSuggestions" :key="suggestion" :value="suggestion">
                {{ suggestion }}
              </option>
            </select>
          </label>
          <label>
            <span>Medication name or custom value</span>
            <input v-model.trim="forms.medication.name" type="text" required />
          </label>
          <label>
            <span>Dosage</span>
            <input v-model.trim="forms.medication.dosage" type="text" required />
          </label>
          <label>
            <span>Time</span>
            <input v-model="forms.medication.time" type="time" required />
          </label>
          <label>
            <span>Instructions</span>
            <textarea v-model.trim="forms.medication.instructions" rows="3" required></textarea>
          </label>
        </template>

        <footer>
          <button class="secondary-button" type="button" @click="closeDialog">Cancel</button>
          <button type="submit">Save</button>
        </footer>
      </form>
    </section>
  </main>
</template>

<script>
import heroImage from '../assets/hero.png';
import {
  addAllergy as storeAddAllergy,
  addChild as storeAddChild,
  addDisease as storeAddDisease,
  addEmergencyContact as storeAddEmergencyContact,
  addMedication as storeAddMedication,
  editAllergy as storeEditAllergy,
  editDisease as storeEditDisease,
  editMedication as storeEditMedication,
  kindercareStore,
  markMedicationTaken,
  parentChildren,
  removeAllergy as storeRemoveAllergy,
  removeDisease as storeRemoveDisease,
  removeMedication as storeRemoveMedication,
  saveParentNote as storeSaveParentNote,
  uploadPrescription as storeUploadPrescription
} from '../state/kindercareStore';

export default {
  name: 'ParentDashboard',
  emits: ['navigate'],
  data() {
    return {
      heroImage,
      selectedChildId: parentChildren()[0].id,
      activeDialog: '',
      parentNoteDraft: '',
      noteFeedback: '',
      selectedQrId: '',
      editingMedicationId: '',
      forms: this.emptyForms(),
      quickActions: [
        { key: 'child', label: 'Add child', icon: 'Kid' },
        { key: 'allergy', label: 'Add allergy', icon: 'All' },
        { key: 'disease', label: 'Add chronic disease', icon: 'Doc' },
        { key: 'medication', label: 'Add medication', icon: 'Med' },
        { key: 'emergency', label: 'Add emergency contact', icon: 'SOS' },
        { key: 'prescription', label: 'Upload prescription', icon: 'Rx' },
        { key: 'note', label: 'Send note to staff', icon: 'Msg' }
      ],
      timelineStatuses: ['Upcoming', 'Pending', 'Taken', 'Missed'],
      allergySuggestions: ['Peanuts', 'Milk', 'Eggs', 'Bee stings', 'Gluten', 'Dust', 'Other'],
      diseaseSuggestions: ['Asthma', 'Diabetes', 'Epilepsy', 'Heart condition', 'Food allergy', 'Other'],
      medicationSuggestions: ['Inhaler', 'Antibiotic', 'Allergy tablets', 'Insulin', 'Pain relief', 'Other']
    };
  },
  computed: {
    parentChildren() {
      return parentChildren();
    },
    selectedChild() {
      return this.parentChildren.find((child) => child.id === this.selectedChildId) || this.parentChildren[0];
    },
    medicationTimeline() {
      return [...this.selectedChild.medications]
        .map((medication) => ({
          ...medication,
          status: this.medicationStatus(medication)
        }))
        .sort((first, second) => first.schedule.specificTime.localeCompare(second.schedule.specificTime));
    },
    savedParentNote() {
      return kindercareStore.parentNotes[this.selectedChildId] || '';
    },
    parentNotes() {
      return kindercareStore.parentNotes;
    },
    allergySummary() {
      const allergies = this.meaningfulItems(this.selectedChild.allergies);
      return allergies.length ? allergies.join(', ') : 'No allergy recorded yet.';
    },
    chronicDiseaseSummary() {
      const diseases = this.meaningfulItems(this.selectedChild.chronicDiseases);
      return diseases.length ? diseases.join(', ') : 'No chronic disease recorded yet.';
    },
    emergencyContactSummary() {
      return this.selectedChild.emergencyContacts[0]?.name || 'No emergency contact added yet.';
    },
    lastKindergartenUpdate() {
      const confirmed = this.medicationHistory[0];

      if (!confirmed) {
        return 'No update from kindergarten yet.';
      }

      return `${confirmed.medicationName} confirmed at ${confirmed.time} by ${confirmed.adminName}.`;
    },
    nextMedicationReminder() {
      const nextMedication = this.medicationTimeline
        .filter((medication) => ['Upcoming', 'Pending'].includes(medication.status))
        .sort((first, second) => first.schedule.specificTime.localeCompare(second.schedule.specificTime))[0];

      if (!nextMedication) {
        return 'No medication scheduled today.';
      }

      return `Next medication: ${nextMedication.name} at ${nextMedication.schedule.specificTime}`;
    },
    medicationHistory() {
      return this.selectedChild.medications
        .flatMap((medication) => (medication.history || []).map((entry) => ({
          ...entry,
          medicationName: medication.name,
          time: this.formatTime(entry.loggedAt)
        })))
        .sort((first, second) => second.loggedAt.localeCompare(first.loggedAt));
    },
    prescriptionStatus() {
      if (this.selectedChild.prescriptionFileName) {
        return {
          label: 'Uploaded',
          detail: this.selectedChild.prescriptionFileName
        };
      }

      const uploadedMedication = this.selectedChild.medications.find((medication) => medication.prescriptionUploaded);

      if (uploadedMedication) {
        return {
          label: 'Ready',
          detail: `${uploadedMedication.name} prescription available`
        };
      }

      return {
        label: 'Missing',
        detail: 'No prescription uploaded yet.'
      };
    },
    dialogTitle() {
      const titles = {
        child: 'Add child',
        allergy: 'Add allergy',
        disease: 'Add chronic disease',
        medication: this.editingMedicationId ? 'Edit medication' : 'Add medication',
        emergency: 'Add emergency contact'
      };

      return titles[this.activeDialog] || '';
    },
    careCues() {
      return [
        {
          icon: '1',
          title: 'Check today',
          text: this.medicationTimeline.length
            ? `${this.medicationTimeline.length} medication item(s) are visible in the timeline.`
            : 'No medication scheduled for today.'
        },
        {
          icon: '2',
          title: 'Keep staff informed',
          text: this.savedParentNote ? 'A parent note is saved for this child.' : 'Add a short note if anything changed this morning.'
        },
        {
          icon: '3',
          title: 'Documents',
          text: this.prescriptionStatus.detail
        }
      ];
    }
  },
  watch: {
    selectedChildId: {
      immediate: true,
      handler(childId) {
        this.parentNoteDraft = this.parentNotes[childId] || '';
        this.noteFeedback = '';
      }
    }
  },
  methods: {
    emptyForms() {
      return {
        child: {
          name: '',
          groupName: '',
          dateOfBirth: ''
        },
        allergy: {
          suggestion: '',
          name: ''
        },
        disease: {
          suggestion: '',
          name: ''
        },
        medication: {
          suggestion: '',
          name: '',
          dosage: '',
          time: '12:00',
          instructions: ''
        },
        emergency: {
          name: '',
          relationship: '',
          phone: '',
          email: '',
          priority: 1
        }
      };
    },
    firstName(name) {
      return name.split(' ')[0];
    },
    meaningfulItems(items) {
      return items.filter((item) => item && !['None', 'None known'].includes(item));
    },
    applySuggestion(formName) {
      const selectedSuggestion = this.forms[formName].suggestion;

      if (!selectedSuggestion) {
        return;
      }

      this.forms[formName].name = selectedSuggestion === 'Other' ? '' : selectedSuggestion;
    },
    statusClass(status) {
      return `status-${status.toLowerCase()}`;
    },
    medicationStatus(medication) {
      const task = kindercareStore.medicationTasks.find((item) => item.medicationId === medication.medicationId);

      if (task?.status === 'Taken' || task?.status === 'Missed') {
        return task.status;
      }

      return medication.todayStatus || 'Upcoming';
    },
    handleQuickAction(actionKey) {
      if (actionKey === 'prescription') {
        this.$refs.prescriptionInput.click();
        return;
      }

      if (actionKey === 'note') {
        this.focusParentNote();
        return;
      }

      this.openDialog(actionKey);
    },
    openDialog(dialogName) {
      this.forms = this.emptyForms();
      this.editingMedicationId = '';
      this.activeDialog = dialogName;
    },
    closeDialog() {
      this.activeDialog = '';
    },
    submitDialog() {
      if (this.activeDialog === 'child') {
        this.addChild();
      }

      if (this.activeDialog === 'allergy') {
        this.addAllergy();
      }

      if (this.activeDialog === 'disease') {
        this.addDisease();
      }

      if (this.activeDialog === 'medication') {
        this.saveMedication();
      }

      if (this.activeDialog === 'emergency') {
        this.addEmergencyContact();
      }

      this.closeDialog();
    },
    addChild() {
      const newChild = storeAddChild({
        name: this.forms.child.name,
        groupName: this.forms.child.groupName,
        dateOfBirth: this.forms.child.dateOfBirth
      });

      this.selectedChildId = newChild.id;
    },
    addAllergy() {
      storeAddAllergy(this.selectedChildId, this.forms.allergy.name);
    },
    addDisease() {
      storeAddDisease(this.selectedChildId, this.forms.disease.name);
    },
    addEmergencyContact() {
      storeAddEmergencyContact(this.selectedChildId, this.forms.emergency);
    },
    saveMedication() {
      const medicationData = {
        name: this.forms.medication.name,
        dosage: this.forms.medication.dosage,
        time: this.forms.medication.time,
        instructions: this.forms.medication.instructions
      };

      if (this.editingMedicationId) {
        storeEditMedication(this.selectedChildId, this.editingMedicationId, medicationData);
        return;
      }

      storeAddMedication(this.selectedChildId, medicationData);
    },
    handlePrescriptionFile(event) {
      const [file] = event.target.files;

      if (!file) {
        return;
      }

      storeUploadPrescription(this.selectedChildId, file.name);

      event.target.value = '';
    },
    focusParentNote() {
      this.noteFeedback = 'Write your message below, then press Save note.';
      this.$nextTick(() => {
        this.$refs.noteTextarea?.focus();
      });
    },
    saveParentNote() {
      storeSaveParentNote(this.selectedChildId, this.parentNoteDraft);
      this.noteFeedback = 'Note saved locally for the prototype.';
    },
    editAllergyItem(index, allergy) {
      const nextValue = window.prompt('Edit allergy', allergy);

      if (nextValue) {
        storeEditAllergy(this.selectedChildId, index, nextValue);
      }
    },
    removeAllergyItem(index) {
      storeRemoveAllergy(this.selectedChildId, index);
    },
    editDiseaseItem(index, disease) {
      const nextValue = window.prompt('Edit chronic disease', disease);

      if (nextValue) {
        storeEditDisease(this.selectedChildId, index, nextValue);
      }
    },
    removeDiseaseItem(index) {
      storeRemoveDisease(this.selectedChildId, index);
    },
    editMedicationItem(medication) {
      this.forms = this.emptyForms();
      this.forms.medication = {
        suggestion: '',
        name: medication.name,
        dosage: medication.dosage,
        time: medication.schedule.specificTime,
        instructions: medication.instructions
      };
      this.editingMedicationId = medication.medicationId;
      this.activeDialog = 'medication';
    },
    removeMedicationItem(medicationId) {
      storeRemoveMedication(this.selectedChildId, medicationId);
    },
    toggleQr(medicationId) {
      this.selectedQrId = this.selectedQrId === medicationId ? '' : medicationId;
    },
    qrCells(medicationId) {
      const seed = medicationId.split('').reduce((total, character) => total + character.charCodeAt(0), 0);
      return Array.from({ length: 36 }, (_, index) => index + seed);
    },
    formatTime(value) {
      return new Date(value).toLocaleTimeString([], {
        hour: '2-digit',
        minute: '2-digit'
      });
    },
    confirmDemoMedication(medicationId) {
      markMedicationTaken(medicationId);
    }
  }
};
</script>

<style scoped>
.parent-dashboard {
  min-height: 100vh;
  padding: 28px;
  background:
    linear-gradient(135deg, #f7fafc 0%, #edf2f7 100%);
  color: #1a202c;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.topbar,
.welcome-panel,
.child-toolbar,
.quick-actions,
.manage-grid,
.health-summary,
.dashboard-grid {
  max-width: 1240px;
  margin: 0 auto;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.eyebrow,
h1,
h2,
h3,
p {
  margin: 0;
}

.eyebrow {
  color: #3182ce;
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

h1 {
  margin-top: 4px;
  font-size: 2.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, #3182ce, #2d3748);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.logout-button,
.child-toolbar button,
.quick-actions button,
.note-card button,
.modal footer button {
  min-height: 44px;
  border: none;
  border-radius: 10px;
  padding: 12px 18px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.logout-button {
  background: linear-gradient(135deg, #2d3748, #1a202c);
  color: #fff;
}

.logout-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(45, 55, 72, 0.3);
}

.welcome-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 24px;
  align-items: center;
  margin-top: 24px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  padding: 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 255, 255, 0.8));
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.welcome-copy h2 {
  max-width: 700px;
  margin-top: 8px;
  font-size: clamp(1.875rem, 4vw, 3.5rem);
  line-height: 1.1;
  font-weight: 700;
  color: #2d3748;
}

.welcome-copy p:not(.eyebrow) {
  max-width: 660px;
  margin-top: 12px;
  color: #4a5568;
  line-height: 1.6;
  font-weight: 500;
}

.welcome-panel img {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.child-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  margin-top: 20px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: #2d3748;
}

select,
input,
textarea {
  width: 100%;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  color: #1a202c;
  font: inherit;
  transition: border-color 0.3s ease;
}

select:focus,
input:focus,
textarea:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

textarea {
  resize: vertical;
}

.child-toolbar button,
.note-card button,
.modal footer button:not(.secondary-button) {
  background: linear-gradient(135deg, #38a169, #2f855a);
  color: #fff;
}

.child-toolbar button:hover,
.note-card button:hover,
.modal footer button:not(.secondary-button):hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(56, 161, 105, 0.3);
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.quick-actions button {
  display: grid;
  gap: 8px;
  justify-items: center;
  min-height: 80px;
  background: rgba(255, 255, 255, 0.9);
  color: #1a202c;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.quick-actions button:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.quick-actions span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #bee3f8, #90cdf4);
  color: #2c5282;
  font-size: 0.875rem;
  font-weight: 700;
}

.care-cues {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  max-width: 1240px;
  margin: 20px auto 0;
}

.care-cues article {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}

.care-cues article:hover {
  transform: translateY(-4px);
}

.care-cues span {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  background: linear-gradient(135deg, #fef5e7, #fbd38d);
  color: #744210;
  font-weight: 700;
}

.care-cues p {
  margin-top: 4px;
  color: #4a5568;
  line-height: 1.5;
}

.health-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.health-summary article,
.panel {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}

.health-summary article:hover,
.panel:hover {
  transform: translateY(-4px);
}

.health-summary article {
  display: grid;
  gap: 8px;
  min-height: 130px;
  padding: 20px;
  text-align: center;
}

.health-summary p {
  color: #4a5568;
  font-weight: 600;
}

.health-summary strong {
  font-size: 1.75rem;
  font-weight: 800;
  color: #2d3748;
}

.health-summary span {
  color: #718096;
  font-weight: 500;
}

.manage-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.compact-list header button {
  min-height: 40px;
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  background: linear-gradient(135deg, #edf2f7, #e2e8f0);
  color: #4a5568;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s ease;
}

.compact-list header button:hover {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e0);
}

.item-actions,
.inline-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.item-actions button,
.inline-actions button {
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #edf2f7, #e2e8f0);
  color: #4a5568;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s ease;
}

.item-actions button:hover,
.inline-actions button:hover {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e0);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 20px;
  margin-top: 20px;
  align-items: start;
}

.panel {
  padding: 20px;
}

.panel header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.panel header span {
  color: #4a5568;
  font-weight: 600;
}

.timeline {
  display: grid;
  gap: 12px;
  margin-top: 16px;
}

.status-legend {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 16px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-left: 5px solid #9fb0be;
  border-radius: 12px;
  padding: 16px;
  background: rgba(251, 252, 253, 0.9);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease;
}

.timeline-item:hover {
  transform: translateY(-2px);
}

.qr-inline {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  margin-top: 12px;
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.mock-qr {
  display: grid;
  width: 78px;
  height: 78px;
  grid-template-columns: repeat(6, 1fr);
  gap: 3px;
  border: 5px solid #1a202c;
  padding: 4px;
  background: #fff;
}

.mock-qr span {
  background: #edf2f7;
}

.mock-qr .filled {
  background: #1a202c;
}

.timeline-item time {
  font-weight: 700;
  color: #2d3748;
}

.timeline-item p,
.timeline-item small {
  color: #4a5568;
}

.timeline-item small {
  display: block;
  margin-top: 4px;
  font-weight: 600;
}

.status-badge {
  border-radius: 20px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #edf2f7, #e2e8f0);
  color: #4a5568;
  font-size: 0.875rem;
  font-weight: 700;
}

.timeline-item.status-upcoming {
  border-left-color: #4f86c6;
}

.timeline-item.status-pending {
  border-left-color: #f0a83a;
}

.timeline-item.status-taken {
  border-left-color: #38a169;
}

.timeline-item.status-missed {
  border-left-color: #e53e3e;
}

.status-badge.status-upcoming,
.status-upcoming .status-badge {
  background: linear-gradient(135deg, #e7f0ff, #bee3f8);
  color: #2c5282;
}

.status-badge.status-pending,
.status-pending .status-badge {
  background: linear-gradient(135deg, #fff1d6, #fbd38d);
  color: #744210;
}

.status-badge.status-taken,
.status-taken .status-badge {
  background: linear-gradient(135deg, #e9f6f2, #c6f6df);
  color: #22543d;
}

.status-badge.status-missed,
.status-missed .status-badge {
  background: linear-gradient(135deg, #ffe3e3, #feb2b2);
  color: #742a2a;
}

.status-explainer {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 16px;
  border-radius: 10px;
  padding: 12px;
  background: linear-gradient(135deg, #f8fafb, #edf2f7);
  color: #4a5568;
}

.status-explainer strong {
  color: #2d3748;
}

.side-stack {
  display: grid;
  gap: 16px;
}

.update-card h2 {
  margin-top: 8px;
  color: #4a5568;
  font-size: 1.125rem;
  line-height: 1.5;
  font-weight: 600;
}

.note-card {
  display: grid;
  gap: 12px;
}

.feedback,
.saved-note,
.empty-state {
  border-radius: 10px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #e9f6f2, #c6f6df);
  color: #22543d;
  font-weight: 600;
}

.saved-note {
  background: linear-gradient(135deg, #f8fafb, #edf2f7);
  color: #4a5568;
}

.compact-list {
  display: grid;
  gap: 12px;
}

.compact-list article {
  display: grid;
  gap: 3px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-top: 12px;
}

.compact-list span {
  color: #4a5568;
}

.hidden-input {
  display: none;
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(5px);
  z-index: 10;
}

.modal {
  display: grid;
  gap: 16px;
  width: min(480px, 100%);
  border-radius: 16px;
  padding: 24px;
  background: #fff;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
}

.modal header,
.modal footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.modal header button {
  border: none;
  background: linear-gradient(135deg, #edf2f7, #e2e8f0);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.modal header button:hover {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e0);
}

.secondary-button {
  background: linear-gradient(135deg, #edf2f7, #e2e8f0);
  color: #4a5568;
}

.secondary-button:hover {
  background: linear-gradient(135deg, #e2e8f0, #cbd5e0);
}

@media (max-width: 980px) {
  .welcome-panel,
  .care-cues,
  .manage-grid,
  .health-summary,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .parent-dashboard {
    padding: 20px;
  }

  .topbar,
  .child-toolbar,
  .timeline-item,
  .qr-inline,
  .status-explainer {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .welcome-copy h2 {
    font-size: 2rem;
  }
}
</style>
