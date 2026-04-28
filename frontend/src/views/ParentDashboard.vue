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
    radial-gradient(circle at 10% 8%, rgba(255, 232, 168, 0.42), transparent 28%),
    linear-gradient(180deg, #f8fbf8 0%, #eef5f1 100%);
  color: #20303f;
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
}

.eyebrow,
h1,
h2,
h3,
p {
  margin: 0;
}

.eyebrow {
  color: #287b68;
  font-size: 0.78rem;
  font-weight: 900;
  text-transform: uppercase;
}

h1 {
  margin-top: 4px;
  font-size: 1.7rem;
}

.logout-button,
.child-toolbar button,
.quick-actions button,
.note-card button,
.modal footer button {
  min-height: 42px;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  font-weight: 900;
  cursor: pointer;
}

.logout-button {
  background: #20303f;
  color: #fff;
}

.welcome-panel {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 22px;
  align-items: center;
  margin-top: 24px;
  border: 1px solid rgba(32, 48, 63, 0.1);
  border-radius: 8px;
  padding: 22px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 18px 40px rgba(32, 48, 63, 0.08);
}

.welcome-copy h2 {
  max-width: 700px;
  margin-top: 8px;
  font-size: clamp(1.7rem, 4vw, 3rem);
  line-height: 1.04;
}

.welcome-copy p:not(.eyebrow) {
  max-width: 660px;
  margin-top: 12px;
  color: #536577;
}

.welcome-panel img {
  width: 100%;
  height: 190px;
  border-radius: 8px;
  object-fit: cover;
}

.child-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  margin-top: 18px;
  border: 1px solid rgba(32, 48, 63, 0.1);
  border-radius: 8px;
  padding: 16px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(32, 48, 63, 0.07);
}

label {
  display: grid;
  gap: 7px;
  font-weight: 800;
}

select,
input,
textarea {
  width: 100%;
  border: 1px solid rgba(32, 48, 63, 0.14);
  border-radius: 8px;
  padding: 11px 12px;
  background: #fff;
  color: #20303f;
  font: inherit;
}

textarea {
  resize: vertical;
}

.child-toolbar button,
.note-card button,
.modal footer button:not(.secondary-button) {
  background: #2d8f7b;
  color: #fff;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 10px;
  margin-top: 14px;
}

.quick-actions button {
  display: grid;
  gap: 6px;
  justify-items: center;
  min-height: 76px;
  background: #fff;
  color: #20303f;
  border: 1px solid rgba(32, 48, 63, 0.1);
  box-shadow: 0 10px 24px rgba(32, 48, 63, 0.06);
}

.quick-actions span {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  border-radius: 50%;
  background: #e9f6f2;
  color: #287b68;
  font-size: 0.72rem;
  font-weight: 900;
}

.care-cues {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  max-width: 1240px;
  margin: 18px auto 0;
}

.care-cues article {
  display: grid;
  grid-template-columns: 38px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  border: 1px solid rgba(32, 48, 63, 0.1);
  border-radius: 8px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 12px 28px rgba(32, 48, 63, 0.06);
}

.care-cues span {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border-radius: 50%;
  background: #ffe8a8;
  color: #5d4b12;
  font-weight: 900;
}

.care-cues p {
  margin-top: 4px;
  color: #637486;
}

.health-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.health-summary article,
.panel {
  border: 1px solid rgba(32, 48, 63, 0.1);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 32px rgba(32, 48, 63, 0.07);
}

.health-summary article {
  display: grid;
  gap: 6px;
  min-height: 120px;
  padding: 18px;
}

.health-summary p {
  color: #637486;
  font-weight: 800;
}

.health-summary strong {
  font-size: 1.45rem;
}

.health-summary span {
  color: #536577;
}

.manage-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.compact-list header button {
  min-height: 36px;
  border: none;
  border-radius: 8px;
  padding: 8px 11px;
  background: #edf2f7;
  color: #405265;
  font-weight: 900;
  cursor: pointer;
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
  padding: 7px 10px;
  background: #edf2f7;
  color: #405265;
  font-weight: 900;
  cursor: pointer;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  margin-top: 18px;
  align-items: start;
}

.panel {
  padding: 18px;
}

.panel header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.panel header span {
  color: #637486;
  font-weight: 800;
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
  margin-top: 14px;
}

.timeline-item {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  border: 1px solid rgba(32, 48, 63, 0.1);
  border-left: 5px solid #9fb0be;
  border-radius: 8px;
  padding: 14px;
  background: #fbfcfd;
}

.qr-inline {
  display: grid;
  grid-template-columns: 84px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  margin-top: 12px;
  border-radius: 8px;
  padding: 12px;
  background: #fff;
}

.mock-qr {
  display: grid;
  width: 78px;
  height: 78px;
  grid-template-columns: repeat(6, 1fr);
  gap: 3px;
  border: 5px solid #20303f;
  padding: 4px;
  background: #fff;
}

.mock-qr span {
  background: #edf2f7;
}

.mock-qr .filled {
  background: #20303f;
}

.timeline-item time {
  font-weight: 900;
}

.timeline-item p,
.timeline-item small {
  color: #637486;
}

.timeline-item small {
  display: block;
  margin-top: 4px;
  font-weight: 800;
}

.status-badge {
  border-radius: 999px;
  padding: 5px 10px;
  background: #edf2f7;
  color: #405265;
  font-size: 0.78rem;
  font-weight: 900;
}

.timeline-item.status-upcoming {
  border-left-color: #4f86c6;
}

.timeline-item.status-pending {
  border-left-color: #f0a83a;
}

.timeline-item.status-taken {
  border-left-color: #2d8f7b;
}

.timeline-item.status-missed {
  border-left-color: #d94a4a;
}

.status-badge.status-upcoming,
.status-upcoming .status-badge {
  background: #e7f0ff;
  color: #255b9a;
}

.status-badge.status-pending,
.status-pending .status-badge {
  background: #fff1d6;
  color: #8a5b00;
}

.status-badge.status-taken,
.status-taken .status-badge {
  background: #e9f6f2;
  color: #287b68;
}

.status-badge.status-missed,
.status-missed .status-badge {
  background: #ffe3e3;
  color: #a12d2d;
}

.status-explainer {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 16px;
  border-radius: 8px;
  padding: 12px;
  background: #f8fafb;
  color: #536577;
}

.status-explainer strong {
  color: #20303f;
}

.side-stack {
  display: grid;
  gap: 16px;
}

.update-card h2 {
  margin-top: 8px;
  color: #405265;
  font-size: 1.1rem;
  line-height: 1.45;
}

.note-card {
  display: grid;
  gap: 12px;
}

.feedback,
.saved-note,
.empty-state {
  border-radius: 8px;
  padding: 10px 12px;
  background: #e9f6f2;
  color: #287b68;
  font-weight: 800;
}

.saved-note {
  background: #f8fafb;
  color: #405265;
}

.compact-list {
  display: grid;
  gap: 12px;
}

.compact-list article {
  display: grid;
  gap: 3px;
  border-top: 1px solid rgba(32, 48, 63, 0.08);
  padding-top: 12px;
}

.compact-list span {
  color: #637486;
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
  background: rgba(32, 48, 63, 0.42);
  z-index: 10;
}

.modal {
  display: grid;
  gap: 14px;
  width: min(480px, 100%);
  border-radius: 8px;
  padding: 20px;
  background: #fff;
  box-shadow: 0 24px 70px rgba(32, 48, 63, 0.26);
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
  background: #edf2f7;
  border-radius: 50%;
  width: 34px;
  height: 34px;
  cursor: pointer;
}

.secondary-button {
  background: #edf2f7;
  color: #405265;
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
    padding: 18px;
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
}
</style>
