<template>
  <main class="parent-dashboard">
    <div v-if="componentError" class="error-banner" role="alert">
      <strong>Component error:</strong> {{ componentError }}
    </div>
    <nav class="topbar" :class="{ 'panel-dark': isDark }">
      <div class="parent-heading">
        <div class="parent-avatar">
          <div v-if="kindercareStore.parentAvatar">
            <img :src="kindercareStore.parentAvatar" alt="Parent avatar" class="parent-avatar-image" />
          </div>
          <div v-else class="initials">
            {{ parentInitials }}
          </div>
          <input ref="avatarInput" type="file" accept="image/*" class="hidden-input" @change="handleParentAvatar" />
        </div>
        <div>
          <p class="eyebrow">Parent dashboard</p>
          <h1>KinderCare Connect</h1>
        </div>
      </div>
      <div class="parent-top-actions">
        <label class="global-search" aria-label="Search parent dashboard">
          <span>Search</span>
          <input v-model.trim="searchQuery" type="search" placeholder="Child, medication, contact..." />
        </label>
        <NotificationCenter />
        <button class="logout-button secondary" type="button" @click="$emit('toggle-theme')">Theme</button>
        <button class="logout-button" type="button" @click="$emit('logout')">Log out</button>
      </div>
    </nav>

    <LiveDashboardBar :children-count="parentChildren.length" shift-label="Parent view" :is-dark="isDark">
      <WeatherHealthCard compact :is-dark="isDark" />
    </LiveDashboardBar>

    <section class="welcome-panel" :class="{ 'panel-dark': isDark }">
      <div class="welcome-copy">
        <p class="eyebrow">Welcome back, {{ loggedInFirstName }}</p>
        <h2 v-if="hasChildren">Here is {{ firstName(selectedChild.name) }}'s health overview for today.</h2>
        <h2 v-else>Your KinderCare dashboard is ready.</h2>
        <p>
          A simple place to keep medication, allergies, emergency contacts, and notes ready for the kindergarten team.
        </p>
      </div>
    </section>

    <!-- Empty state: new parent with no children yet -->
    <section v-if="!kindercareStore.loading && !hasChildren" class="empty-dashboard-state" :class="{ 'panel-dark': isDark }">
      <div class="empty-state-icon">👶</div>
      <h2>No child added yet</h2>
      <p>Add your child's profile so the kindergarten team can manage their health and medication.</p>
      <button type="button" class="empty-add-btn" @click="openDialog('child')">+ Add your child</button>
    </section>

    <p v-if="parentSearchEmpty" class="search-empty">No health record matches "{{ searchQuery }}".</p>

    <p v-if="kindercareStore.loading" style="text-align:center;padding:1rem;opacity:.6;">Loading children…</p>

    <section v-if="hasChildren" class="child-toolbar" :class="{ 'panel-dark': isDark }">
      <div class="child-toolbar-photo">
        <span class="child-toolbar-initials">{{ firstName(selectedChild.name)[0] }}</span>
      </div>
      <label>
        <span>Selected child</span>
        <select v-model.number="selectedChildId">
          <option v-for="child in parentChildren" :key="child.id" :value="child.id">
            {{ child.name }} - {{ child.groupName }}
          </option>
        </select>
      </label>
      <button type="button" @click="openDialog('child')">Add child</button>
      <button type="button" class="delete-child-btn" @click="confirmDeleteChild">Delete child</button>
    </section>

    <template v-if="hasChildren">
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
      <article :class="{ 'panel-dark': isDark }">
        <p>Allergies</p>
        <strong>{{ meaningfulItems(selectedChild.allergies).length }}</strong>
        <span>{{ allergySummary }}</span>
      </article>
      <article :class="{ 'panel-dark': isDark }">
        <p>Chronic diseases</p>
        <strong>{{ meaningfulItems(selectedChild.chronicDiseases).length }}</strong>
        <span>{{ chronicDiseaseSummary }}</span>
      </article>
      <article :class="{ 'panel-dark': isDark }">
        <p>Emergency contacts</p>
        <strong>{{ (selectedChild.emergencyContacts || []).length }}</strong>
        <span>{{ emergencyContactSummary }}</span>
      </article>
      <article :class="{ 'panel-dark': isDark }">
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

        <div class="medication-progress">
          <div>
            <strong>{{ medicationProgress.completed }}/{{ medicationProgress.total }}</strong>
            <span>completed today</span>
          </div>
          <div class="progress-track">
            <span :style="{ width: `${medicationProgress.percent}%` }"></span>
          </div>
        </div>

        <div class="daily-periods">
          <article v-for="section in medicationPeriods" :key="section.key">
            <strong>{{ section.label }}</strong>
            <span>{{ section.tasks.length }} item(s)</span>
          </article>
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
                   View QR / ID
                 </button>
                 <button type="button" @click="editMedicationItem(medication)">Edit</button>
                 <button type="button" @click="removeMedicationItem(medication.medicationId)">Remove</button>
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

        <section class="panel compact-list">
          <header>
            <div>
              <p class="eyebrow">Contacts</p>
              <h2>Emergency contacts</h2>
            </div>
            <button type="button" @click="openDialog('emergency')">Add emergency contact</button>
          </header>
          <article v-for="contact in (selectedChild.emergencyContacts || [])" :key="contact.id">
            <strong>{{ contact.name }}</strong>
            <span>{{ contact.relationship }} - {{ contact.phone }}</span>
          </article>
          <p v-if="(selectedChild.emergencyContacts || []).length === 0" class="empty-state">
            No emergency contact added yet.
          </p>
        </section>
      </aside>
    </section>

    </template><!-- /v-if="hasChildren" -->

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
          <button class="secondary-button" type="button" :disabled="submitting" @click="closeDialog">Cancel</button>
          <button type="submit" :disabled="submitting">{{ submitting ? 'Saving…' : 'Save' }}</button>
        </footer>
      </form>
    </section>
  </main>
</template>

<script>
import heroImage from '../assets/hero.png';
import LiveDashboardBar from '../components/LiveDashboardBar.vue';
import NotificationCenter from '../components/NotificationCenter.vue';
import WeatherHealthCard from '../components/WeatherHealthCard.vue';
import { currentUser } from '../state/authStore';
import {
  MEDICATION_STATUSES,
  addAllergy as storeAddAllergy,
  addChild as storeAddChild,
  addDisease as storeAddDisease,
  addEmergencyContact as storeAddEmergencyContact,
  addMedication as storeAddMedication,
  editAllergy as storeEditAllergy,
  editDisease as storeEditDisease,
  loadChildren,
  editMedication as storeEditMedication,
  kindercareStore,
  markMedicationTaken,
  parentChildren,
  removeAllergy as storeRemoveAllergy,
  setParentAvatar,
  removeDisease as storeRemoveDisease,
  removeMedication as storeRemoveMedication,
  uploadPrescription as storeUploadPrescription,
  deleteChild as storeDeleteChild
} from '../state/kindercareStore';

export default {
  name: 'ParentDashboard',
  components: {
    LiveDashboardBar,
    NotificationCenter,
    WeatherHealthCard
  },
  props: {
    isDark: {
      type: Boolean,
      default: false
    }
  },
  emits: ['navigate', 'logout', 'toggle-theme'],
  data() {
    return {
      heroImage,
      kindercareStore,
      selectedChildId: parentChildren()[0]?.id || null,
      componentError: '',
      submitting: false,
      activeDialog: '',
      editingMedicationId: '',
      searchQuery: '',
      forms: this.emptyForms(),
      quickActions: [
        { key: 'child', label: 'Add child', icon: 'Kid' },
        { key: 'allergy', label: 'Add allergy', icon: 'All' },
        { key: 'disease', label: 'Add chronic disease', icon: 'Doc' },
        { key: 'medication', label: 'Add medication', icon: 'Med' },
        { key: 'emergency', label: 'Add emergency contact', icon: 'SOS' },
        { key: 'prescription', label: 'Upload prescription', icon: 'Rx' }
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
      return this.parentChildren.find((child) => child.id === this.selectedChildId) || this.parentChildren[0] || {
        name: 'No child selected',
        groupName: '—',
        allergies: [],
        chronicDiseases: [],
        emergencyContacts: [],
        medications: [],
        prescriptionFileName: null,
        location: { lat: 0, lng: 0 }
      };
    },
    medicationTimeline() {
      return [...(this.selectedChild.medications || [])]
        .map((medication) => ({
          ...medication,
          schedule: {
            specificTime: '12:00',
            dosage: medication.dosage || '',
            instructions: medication.instructions || '',
            ...(medication.schedule || {})
          },
          status: this.medicationStatus(medication)
        }))
        .filter((medication) => this.matchesParentSearch([
          medication.name,
          medication.medicationId,
          medication.dosage,
          medication.instructions,
          medication.status
        ]))
        .sort((first, second) => (first.schedule?.specificTime || '').localeCompare(second.schedule?.specificTime || ''));
    },
    medicationProgress() {
      const total = this.medicationTimeline.length;
      const completed = this.medicationTimeline.filter((medication) => medication.status === 'Taken').length;
      return {
        total,
        completed,
        percent: total ? Math.round((completed / total) * 100) : 0
      };
    },
    medicationPeriods() {
      const sections = [
        { key: 'morning', label: 'Morning', tasks: [] },
        { key: 'afternoon', label: 'Afternoon', tasks: [] },
        { key: 'evening', label: 'Evening', tasks: [] }
      ];

      this.medicationTimeline.forEach((medication) => {
        const hour = Number((medication.schedule?.specificTime || '12:00').split(':')[0]);
        const target = hour < 12 ? sections[0] : hour < 17 ? sections[1] : sections[2];
        target.tasks.push(medication);
      });

      return sections;
    },
    normalizedSearchQuery() {
      return this.searchQuery.trim().toLowerCase();
    },
    parentSearchEmpty() {
      return Boolean(this.normalizedSearchQuery) && this.medicationTimeline.length === 0 && !this.matchesSelectedChildSearch;
    },
    matchesSelectedChildSearch() {
      return this.matchesParentSearch([
        this.selectedChild.name,
        this.selectedChild.parentName,
        this.selectedChild.groupName,
        ...(this.selectedChild.emergencyContacts || []).flatMap((contact) => [contact.name, contact.relationship, contact.phone]),
        ...(this.selectedChild.allergies || []),
        ...(this.selectedChild.chronicDiseases || [])
      ]);
    },
    hasChildren() {
      return this.parentChildren.length > 0;
    },
    loggedInFirstName() {
      return currentUser()?.fullName?.split(' ')[0] || 'there';
    },
    parentInitials() {
      const name = currentUser()?.fullName || '';
      if (!name) return '?';
      return name.split(' ').map((p) => p[0]).join('').slice(0, 2).toUpperCase();
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
      return this.selectedChild.emergencyContacts?.[0]?.name || 'No emergency contact added yet.';
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
      return (this.selectedChild.medications || [])
        .flatMap((medication) => (medication.history || []).map((entry) => ({
          ...entry,
          medicationName: medication.name,
          time: this.formatTime(entry.loggedAt)
        })))
        .sort((first, second) => (second.loggedAt || '').localeCompare(first.loggedAt || ''));
    },
    prescriptionStatus() {
      if (this.selectedChild.prescriptionFileName) {
        return {
          label: 'Uploaded',
          detail: this.selectedChild.prescriptionFileName
        };
      }

      const uploadedMedication = (this.selectedChild.medications || []).find((medication) => medication.prescriptionUploaded);

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
          title: 'Documents',
          text: this.prescriptionStatus.detail
        }
      ];
    }
  },
  async mounted() {
    await loadChildren();
    if (!this.selectedChildId && this.parentChildren.length > 0) {
      this.selectedChildId = this.parentChildren[0].id;
    }
  },
  errorCaptured(err, vm, info) {
    // surface runtime errors to the UI for easier debugging
    // eslint-disable-next-line no-console
    console.error('Captured error in ParentDashboard:', err, info);
    try {
      this.componentError = err?.message || String(err);
    } catch (e) {
      this.componentError = 'Unknown error';
    }
    return false; // allow global handler to also run
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
      return (name || '').split(' ')[0] || 'your child';
    },
    meaningfulItems(items) {
      return (items || []).filter((item) => item && !['None', 'None known'].includes(item));
    },
    matchesParentSearch(values) {
      if (!this.normalizedSearchQuery) {
        return true;
      }

      return values
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(this.normalizedSearchQuery);
    },
    applySuggestion(formName) {
      const selectedSuggestion = this.forms[formName].suggestion;

      if (!selectedSuggestion) {
        return;
      }

      this.forms[formName].name = selectedSuggestion === 'Other' ? '' : selectedSuggestion;
    },
    statusClass(status) {
      const normalizedStatus = MEDICATION_STATUSES.includes(status) ? status : 'Upcoming';
      return `status-${normalizedStatus.toLowerCase()}`;
    },
    medicationStatus(medication) {
      const task = this.kindercareStore.medicationTasks.find((item) => item.medicationId === medication.medicationId);

      if (MEDICATION_STATUSES.includes(task?.status)) {
        return task.status;
      }

      return MEDICATION_STATUSES.includes(medication.todayStatus) ? medication.todayStatus : 'Upcoming';
    },
    handleQuickAction(actionKey) {
      if (actionKey === 'prescription') {
        this.$refs.prescriptionInput.click();
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
    async submitDialog() {
      if (this.submitting) return;
      const dialog = this.activeDialog;
      this.submitting = true;
      this.closeDialog();

      try {
        if (dialog === 'child') await this.addChild();
        if (dialog === 'allergy') await this.addAllergy();
        if (dialog === 'disease') await this.addDisease();
        if (dialog === 'medication') await this.saveMedication();
        if (dialog === 'emergency') await this.addEmergencyContact();
      } finally {
        this.submitting = false;
      }
    },
    async addChild() {
      const newChild = await storeAddChild({
        name: this.forms.child.name,
        groupName: this.forms.child.groupName,
        dateOfBirth: this.forms.child.dateOfBirth
      });

      if (newChild) {
        this.selectedChildId = newChild.id;
      }
    },
    async confirmDeleteChild() {
      const name = this.selectedChild?.name || 'this child';
      if (!window.confirm(`Delete "${name}" permanently? This cannot be undone.`)) return;
      const idToDelete = this.selectedChildId;
      await storeDeleteChild(idToDelete);
      this.selectedChildId = this.parentChildren[0]?.id || null;
    },
    async addAllergy() {
      await storeAddAllergy(this.selectedChildId, this.forms.allergy.name);
    },
    async addDisease() {
      await storeAddDisease(this.selectedChildId, this.forms.disease.name);
    },
    async addEmergencyContact() {
      await storeAddEmergencyContact(this.selectedChildId, this.forms.emergency);
    },
    async saveMedication() {
      const medicationData = {
        name: this.forms.medication.name,
        dosage: this.forms.medication.dosage,
        time: this.forms.medication.time,
        instructions: this.forms.medication.instructions
      };

      if (this.editingMedicationId) {
        await storeEditMedication(this.selectedChildId, this.editingMedicationId, medicationData);
        return;
      }

      await storeAddMedication(this.selectedChildId, medicationData);
    },
    handlePrescriptionFile(event) {
      const [file] = event.target.files;

      if (!file) {
        return;
      }

      storeUploadPrescription(this.selectedChildId, file.name);

      event.target.value = '';
    },
    async editAllergyItem(index, allergy) {
      const nextValue = window.prompt('Edit allergy', allergy);

      if (nextValue) {
        await storeEditAllergy(this.selectedChildId, index, nextValue);
      }
    },
    async removeAllergyItem(index) {
      await storeRemoveAllergy(this.selectedChildId, index);
    },
    async editDiseaseItem(index, disease) {
      const nextValue = window.prompt('Edit chronic disease', disease);

      if (nextValue) {
        await storeEditDisease(this.selectedChildId, index, nextValue);
      }
    },
    async removeDiseaseItem(index) {
      await storeRemoveDisease(this.selectedChildId, index);
    },
    editMedicationItem(medication) {
      this.forms = this.emptyForms();
      this.forms.medication = {
        suggestion: '',
        name: medication.name,
        dosage: medication.dosage,
        time: medication.schedule?.specificTime || '12:00',
        instructions: medication.instructions
      };
      this.editingMedicationId = medication.medicationId;
      this.activeDialog = 'medication';
    },
    async removeMedicationItem(medicationId) {
      await storeRemoveMedication(this.selectedChildId, medicationId);
    },
    handleParentAvatar(event) {
      const [file] = event.target.files;

      if (!file) return;

      const reader = new FileReader();
      reader.onload = (e) => {
        setParentAvatar(e.target.result);
      };
      reader.readAsDataURL(file);
    },

    formatTime(value) {
      return new Date(value).toLocaleTimeString('en-US', {
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
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
}

:global([data-theme="dark"]) .parent-dashboard {
  background:
    linear-gradient(
      180deg,
      #0f172a 0%,
      #111827 100%
    );
  color: #f8fafc;
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
  background: var(--color-bg-secondary);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: var(--shadow-md);
  margin-bottom: 20px;
  border: 1px solid var(--color-border);
  backdrop-filter: blur(10px);
}

.parent-heading,
.parent-avatar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.parent-avatar {
  gap: 10px;
}

.parent-avatar-image,
.initials {
  width: 56px;
  height: 56px;
  border-radius: 999px;
}

.parent-avatar-image {
  object-fit: cover;
}

.initials {
  display: grid;
  place-items: center;
  background: var(--color-care-bg);
  color: #1f2937;
  font-weight: 800;
}

.parent-top-actions {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.global-search {
  position: relative;
  display: grid;
  gap: 4px;
  min-width: min(300px, 100%);
}

.global-search span {
  color: var(--color-text-secondary);
  font-size: 0.72rem;
  font-weight: 900;
  text-transform: uppercase;
}

.global-search input {
  min-height: 44px;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 10px 14px 10px 38px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-sm);
}

.global-search::before {
  position: absolute;
  left: 13px;
  bottom: 12px;
  width: 12px;
  height: 12px;
  border: 2px solid var(--color-text-tertiary);
  border-radius: 999px;
  content: '';
}

.global-search::after {
  position: absolute;
  left: 25px;
  bottom: 10px;
  width: 7px;
  height: 2px;
  border-radius: 999px;
  background: var(--color-text-tertiary);
  content: '';
  transform: rotate(45deg);
}

.search-empty {
  max-width: 1240px;
  margin: 16px auto 0;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 14px 16px;
  background: var(--color-bg-secondary);
  color: var(--color-text-secondary);
  font-weight: 800;
  box-shadow: var(--shadow-sm);
}

.error-banner {
  background: #fee2e2;
  color: #991b1b;
  border: 1px solid #fecaca;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.eyebrow,
h1,
h2,
h3,
p {
  margin: 0;
}

.eyebrow {
  color: var(--color-brand);
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

h1 {
  margin-top: 4px;
  font-size: 2.25rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-dark));
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
  box-shadow: var(--shadow-md);
}

.logout-button {
  background: linear-gradient(135deg, var(--color-brand-dark), #1a202c);
  color: #fff;
}

.logout-button.secondary {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.logout-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(45, 55, 72, 0.3);
}

.welcome-panel {
  display: grid;
  grid-template-columns: 1fr;
  gap: 14px;
  align-items: center;
  margin-top: 16px;
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  padding: 20px 22px;
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--color-bg-primary) 100%);
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.welcome-copy h2 {
  max-width: 700px;
  margin-top: 8px;
  font-size: clamp(1.35rem, 2.4vw, 1.9rem);
  line-height: 1.15;
  font-weight: 700;
  color: #111827;
}

.welcome-copy p:not(.eyebrow) {
  max-width: 660px;
  margin-top: 12px;
  color: #64748b;
  line-height: 1.7;
  font-weight: 500;
}

.child-toolbar {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 12px;
  align-items: end;
  margin-top: 16px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(10px);
}

label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: var(--color-text-primary);
}

select,
input,
textarea {
  width: 100%;
  border: 2px solid var(--color-border);
  border-radius: 10px;
  padding: 12px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font: inherit;
  transition: border-color 0.3s ease;
}

select:focus,
input:focus,
textarea:focus {
  outline: none;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

textarea {
  resize: vertical;
}

.child-toolbar button,
.note-card button,
.modal footer button:not(.secondary-button) {
  background: linear-gradient(135deg, var(--color-success), #2f855a);
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
  grid-template-columns: repeat(auto-fit, minmax(132px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.quick-actions button {
  display: grid;
  gap: 8px;
  justify-items: center;
  min-height: 70px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.quick-actions button:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.quick-actions span {
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border-radius: 50%;
  background: var(--color-icon-bg);
  color: var(--color-icon-text);
  font-size: 0.875rem;
  font-weight: 700;
}

.quick-actions button:nth-child(1) span,
.care-cues article:nth-child(1) span {
  background: rgba(34, 197, 94, 0.18);
  color: #166534;
}

.quick-actions button:nth-child(2) span,
.care-cues article:nth-child(2) span {
  background: rgba(59, 130, 246, 0.18);
  color: #1e3a8a;
}

.quick-actions button:nth-child(3) span,
.care-cues article:nth-child(3) span {
  background: rgba(139, 92, 246, 0.18);
  color: #5b21b6;
}

.quick-actions button:nth-child(4) span {
  background: rgba(99, 102, 241, 0.18);
  color: #3730a3;
}

.quick-actions button:nth-child(5) span,
.quick-actions button:nth-child(6) span,
.quick-actions button:nth-child(7) span {
  background: rgba(245, 158, 11, 0.18);
  color: #92400e;
}

.care-cues {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
  max-width: 1240px;
  margin: 16px auto 0;
}

.care-cues article {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
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
  background: var(--color-care-bg);
  color: var(--color-care-text);
  font-weight: 700;
}

.care-cues p {
  margin-top: 4px;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.health-summary {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.health-summary article,
.panel {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}

.health-summary article:hover,
.panel:hover {
  transform: translateY(-4px);
}

.health-summary article {
  display: grid;
  gap: 6px;
  min-height: 108px;
  padding: 16px;
  text-align: center;
}

.health-summary article:nth-child(1) {
  background: var(--gradient-live-card);
}

.health-summary article:nth-child(2) {
  background: var(--gradient-weather-card);
}

.health-summary article:nth-child(3) {
  background: var(--gradient-today-card);
}

.health-summary article:nth-child(4) {
  background: var(--gradient-children-card);
}

.health-summary p {
  color: var(--color-text-secondary);
  font-weight: 600;
}

.health-summary strong {
  font-size: 1.45rem;
  font-weight: 800;
  color: var(--color-text-primary);
}

.health-summary span {
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.manage-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.compact-list header button {
  min-height: 40px;
  border: none;
  border-radius: 10px;
  padding: 10px 14px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s ease;
}

.compact-list header button:hover {
  background: var(--color-border);
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
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s ease;
}

.item-actions button:hover,
.inline-actions button:hover {
  background: var(--color-border);
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(300px, 360px);
  gap: 16px;
  margin-top: 16px;
  align-items: start;
}

.panel {
  padding: 16px;
}

.panel header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.panel header span {
  color: var(--color-text-secondary);
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

.medication-progress {
  display: grid;
  gap: 10px;
  margin-top: 16px;
  border: 1px solid var(--color-border-light);
  border-radius: 14px;
  padding: 14px;
  background: var(--color-bg-primary);
}

.medication-progress > div:first-child {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  color: var(--color-text-primary);
  font-weight: 900;
}

.medication-progress span {
  color: var(--color-text-secondary);
}

.progress-track {
  height: 12px;
  overflow: hidden;
  border-radius: 999px;
  background: var(--color-bg-tertiary);
}

.progress-track span {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--color-taken-border), var(--color-upcoming-border));
  transition: width 0.35s ease;
}

.daily-periods {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.daily-periods article {
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
}

.daily-periods strong,
.daily-periods span {
  display: block;
}

.daily-periods span {
  margin-top: 2px;
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  font-weight: 800;
}

.timeline-item {
  display: grid;
  grid-template-columns: 70px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  border: 1px solid var(--color-border);
  border-left: 5px solid #9fb0be;
  border-radius: 12px;
  padding: 16px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.3s ease;
}

.timeline-item:hover {
  transform: translateY(-2px);
}

.timeline-item time {
  font-weight: 700;
  color: var(--color-text-primary);
}

.timeline-item p,
.timeline-item small {
  color: var(--color-text-secondary);
}

.timeline-item small {
  display: block;
  margin-top: 4px;
  font-weight: 600;
}

.status-badge {
  border-radius: 20px;
  padding: 6px 12px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  font-weight: 700;
}

.timeline-item.status-upcoming {
  border-left-color: #4f86c6;
}

.timeline-item.status-pending {
  border-left-color: var(--color-warning);
}

.timeline-item.status-taken {
  border-left-color: var(--color-success);
}

.timeline-item.status-missed {
  border-left-color: var(--color-danger);
}

.status-badge.status-upcoming,
.status-upcoming .status-badge {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.status-badge.status-pending,
.status-pending .status-badge {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.status-badge.status-taken,
.status-taken .status-badge {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.status-badge.status-missed,
.status-missed .status-badge {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.status-explainer {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 16px;
  border-radius: 10px;
  padding: 12px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.status-explainer strong {
  color: var(--color-text-primary);
}

.side-stack {
  display: grid;
  gap: 12px;
}

.update-card h2 {
  margin-top: 8px;
  color: var(--color-text-secondary);
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
  background: var(--color-taken);
  color: var(--color-taken-text);
  font-weight: 600;
}

.saved-note {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.compact-list {
  display: grid;
  gap: 12px;
}

.compact-list article {
  display: grid;
  gap: 3px;
  border-top: 1px solid var(--color-border-light);
  padding-top: 12px;
}

.compact-list span {
  color: var(--color-text-secondary);
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
  background: var(--color-bg-secondary);
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25);
  color: var(--color-text-primary);
}

.qr-modal {
  width: min(500px, 100%);
}

.qr-content {
  display: grid;
  gap: 16px;
}

.qr-section {
  display: grid;
  gap: 12px;
  align-items: center;
}

.qr-section h3 {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.dosage {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  margin: 0;
}

.qr-display {
  display: grid;
  place-items: center;
  padding: 16px;
  background: var(--color-bg-tertiary);
  border-radius: 12px;
  border: 1px solid var(--color-border);
}

.mock-qr {
  display: grid;
  width: 120px;
  height: 120px;
  grid-template-columns: repeat(6, 1fr);
  gap: 3px;
  border: 5px solid var(--color-text-primary);
  padding: 4px;
  background: var(--color-bg-secondary);
}

.mock-qr span {
  background: var(--color-bg-tertiary);
}

.mock-qr .filled {
  background: var(--color-text-primary);
}

.medication-id-box {
  display: grid;
  gap: 6px;
  padding: 12px;
  background: var(--color-bg-tertiary);
  border: 1px solid var(--color-border);
  border-radius: 10px;
}

.medication-id-box .label {
  font-size: 0.875rem;
  color: var(--color-text-tertiary);
  margin: 0;
}

.medication-id-box .id {
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-brand);
  font-family: 'Courier New', monospace;
  margin: 0;
}

.payload-info {
  font-size: 0.75rem;
  color: var(--color-text-tertiary);
  font-family: 'Courier New', monospace;
  margin: 0;
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
  background: var(--color-bg-tertiary);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  cursor: pointer;
  transition: background 0.3s ease;
  color: var(--color-text-primary);
  font-weight: 700;
}

.modal header button:hover {
  background: var(--color-border);
}

.secondary-button {
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
}

.secondary-button:hover {
  background: var(--color-border);
}

:global([data-theme="dark"]) .parent-dashboard .topbar,
:global([data-theme="dark"]) .parent-dashboard .welcome-panel,
:global([data-theme="dark"]) .parent-dashboard .child-toolbar,
:global([data-theme="dark"]) .parent-dashboard .search-empty,
:global([data-theme="dark"]) .parent-dashboard .quick-actions button,
:global([data-theme="dark"]) .parent-dashboard .care-cues article,
:global([data-theme="dark"]) .parent-dashboard .health-summary article,
:global([data-theme="dark"]) .parent-dashboard .panel,
:global([data-theme="dark"]) .parent-dashboard .timeline-item,
:global([data-theme="dark"]) .parent-dashboard .modal {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background:
    linear-gradient(
      135deg,
      #111827 0%,
      #1e293b 100%
    );
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) .parent-dashboard .welcome-copy h2,
:global([data-theme="dark"]) .parent-dashboard h1,
:global([data-theme="dark"]) .parent-dashboard h2,
:global([data-theme="dark"]) .parent-dashboard h3,
:global([data-theme="dark"]) .parent-dashboard strong,
:global([data-theme="dark"]) .parent-dashboard label,
:global([data-theme="dark"]) .parent-dashboard time {
  color: #f8fafc;
  -webkit-text-fill-color: #f8fafc;
}

:global([data-theme="dark"]) .parent-dashboard p,
:global([data-theme="dark"]) .parent-dashboard span,
:global([data-theme="dark"]) .parent-dashboard small,
:global([data-theme="dark"]) .parent-dashboard .welcome-copy p:not(.eyebrow),
:global([data-theme="dark"]) .parent-dashboard .panel header span,
:global([data-theme="dark"]) .parent-dashboard .timeline-item p,
:global([data-theme="dark"]) .parent-dashboard .timeline-item small,
:global([data-theme="dark"]) .parent-dashboard .health-summary p {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .parent-dashboard .eyebrow,
:global([data-theme="dark"]) .parent-dashboard .health-summary span,
:global([data-theme="dark"]) .parent-dashboard .payload-info,
:global([data-theme="dark"]) .parent-dashboard .medication-id-box .label {
  color: #94a3b8;
}

:global([data-theme="dark"]) .parent-dashboard select,
:global([data-theme="dark"]) .parent-dashboard input,
:global([data-theme="dark"]) .parent-dashboard textarea,
:global([data-theme="dark"]) .parent-dashboard .global-search input,
:global([data-theme="dark"]) .parent-dashboard .medication-progress,
:global([data-theme="dark"]) .parent-dashboard .daily-periods article,
:global([data-theme="dark"]) .parent-dashboard .status-explainer,
:global([data-theme="dark"]) .parent-dashboard .saved-note,
:global([data-theme="dark"]) .parent-dashboard .qr-display,
:global([data-theme="dark"]) .parent-dashboard .medication-id-box,
:global([data-theme="dark"]) .parent-dashboard .mock-qr {
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .parent-dashboard .logout-button.secondary,
:global([data-theme="dark"]) .parent-dashboard .compact-list header button,
:global([data-theme="dark"]) .parent-dashboard .item-actions button,
:global([data-theme="dark"]) .parent-dashboard .inline-actions button,
:global([data-theme="dark"]) .parent-dashboard .secondary-button,
:global([data-theme="dark"]) .parent-dashboard .modal header button {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .parent-dashboard .logout-button.secondary:hover,
:global([data-theme="dark"]) .parent-dashboard .compact-list header button:hover,
:global([data-theme="dark"]) .parent-dashboard .item-actions button:hover,
:global([data-theme="dark"]) .parent-dashboard .inline-actions button:hover,
:global([data-theme="dark"]) .parent-dashboard .secondary-button:hover,
:global([data-theme="dark"]) .parent-dashboard .modal header button:hover {
  background: rgba(255, 255, 255, 0.12);
}

:global([data-theme="dark"]) .parent-dashboard .quick-actions span,
:global([data-theme="dark"]) .parent-dashboard .care-cues span,
:global([data-theme="dark"]) .parent-dashboard .initials {
  background: rgba(59, 130, 246, 0.22) !important;
  color: #bfdbfe !important;
}

:global([data-theme="dark"]) .parent-dashboard .quick-actions button:nth-child(1) span,
:global([data-theme="dark"]) .parent-dashboard .health-summary article:nth-child(1) {
  background: var(--gradient-live-card);
}

:global([data-theme="dark"]) .parent-dashboard .quick-actions button:nth-child(2) span,
:global([data-theme="dark"]) .parent-dashboard .health-summary article:nth-child(2) {
  background: var(--gradient-weather-card);
}

:global([data-theme="dark"]) .parent-dashboard .quick-actions button:nth-child(3) span,
:global([data-theme="dark"]) .parent-dashboard .health-summary article:nth-child(3) {
  background: var(--gradient-today-card);
}

:global([data-theme="dark"]) .parent-dashboard .quick-actions button:nth-child(4) span,
:global([data-theme="dark"]) .parent-dashboard .health-summary article:nth-child(4) {
  background: var(--gradient-children-card);
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
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .parent-dashboard {
    padding: 20px;
  }

  .topbar,
  .child-toolbar,
  .timeline-item,
  .status-explainer {
    grid-template-columns: 1fr;
  }

  .parent-top-actions,
  .global-search {
    width: 100%;
  }

  .quick-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .daily-periods {
    grid-template-columns: 1fr;
  }

  .welcome-copy h2 {
    font-size: 1.5rem;
  }

  .modal {
    width: 100%;
    margin: 0 auto;
  }
}

/* ─── Child toolbar photo + delete ──────────────────────────────────────── */

.child-toolbar {
  grid-template-columns: auto minmax(0, 1fr) auto auto;
}

.child-toolbar-photo {
  display: flex;
  align-items: center;
}

.child-toolbar-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border);
}

.child-toolbar-initials {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--color-care-bg);
  color: #1f2937;
  font-weight: 800;
  font-size: 1.1rem;
}

.delete-child-btn {
  min-height: 44px;
  border: none;
  border-radius: 10px;
  padding: 12px 18px;
  background: linear-gradient(135deg, #e53e3e, #9b2c2c);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-md);
}

.delete-child-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(229, 62, 62, 0.35);
}

/* ─── Photo form field ───────────────────────────────────────────────────── */

.photo-file-input {
  cursor: pointer;
}

.photo-preview-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.photo-preview {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid var(--color-border);
}

.remove-photo-btn {
  border: none;
  border-radius: 8px;
  padding: 6px 12px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-weight: 600;
  cursor: pointer;
}

/* ─── Empty-dashboard state ──────────────────────────────────────────────── */

.empty-dashboard-state {
  display: grid;
  justify-items: center;
  gap: 14px;
  max-width: 1240px;
  margin: 32px auto;
  padding: 48px 24px;
  border: 2px dashed var(--color-border);
  border-radius: 18px;
  background: var(--color-bg-secondary);
  text-align: center;
}

.empty-state-icon {
  font-size: 3rem;
  line-height: 1;
}

.empty-dashboard-state h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.empty-dashboard-state p {
  max-width: 440px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.empty-add-btn {
  min-height: 48px;
  border: none;
  border-radius: 12px;
  padding: 12px 28px;
  background: linear-gradient(135deg, var(--color-success), #2f855a);
  color: #fff;
  font-size: 1rem;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 14px rgba(56, 161, 105, 0.35);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.empty-add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(56, 161, 105, 0.45);
}

:global([data-theme="dark"]) .empty-dashboard-state {
  border-color: rgba(255, 255, 255, 0.1);
  background: linear-gradient(135deg, #111827 0%, #1e293b 100%);
}
</style>
