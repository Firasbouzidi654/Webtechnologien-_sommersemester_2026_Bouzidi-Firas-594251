<template>
  <main class="parent-dashboard">
    <div v-if="componentError" class="error-banner" role="alert">
      <strong>Something went wrong.</strong> Please refresh the page and try again.
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
        <NotificationCenter />
        <button class="logout-button secondary" type="button" @click="$emit('toggle-theme')">Theme</button>
        <button class="logout-button" type="button" @click="$emit('logout')">Log out</button>
      </div>
    </nav>

    <section class="welcome-panel" :class="{ 'panel-dark': isDark }">
      <div class="welcome-copy">
        <p class="eyebrow">Welcome back, {{ loggedInFirstName }}</p>
        <h2 v-if="hasChildren">Here is {{ firstName(selectedChild.name) }}'s health overview for today.</h2>
        <h2 v-else>Your KinderCare dashboard is ready.</h2>
        <p>
          A simple place to keep medication and allergy information ready for the kindergarten team.
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
            <h2>Medication</h2>
          </div>
        </header>

        <form class="simple-medication-form" @submit.prevent="submitParentMedication">
          <label>
            <span>Child name</span>
            <select v-model.number="medicationForm.childId" required>
              <option v-for="child in parentChildren" :key="child.id" :value="child.id">{{ child.name }}</option>
            </select>
          </label>
          <label>
            <span>Medication name</span>
            <input v-model.trim="medicationForm.name" type="text" required />
          </label>
          <label>
            <span>Dosage</span>
            <input v-model.trim="medicationForm.dosage" type="text" required />
          </label>
          <label>
            <span>Time</span>
            <input v-model="medicationForm.time" type="time" required />
          </label>
          <label>
            <span>Frequency</span>
            <select v-model="medicationForm.frequency">
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
              <option value="EVERY_X_DAYS">Every X days</option>
              <option value="WEEKDAYS_ONLY">Weekdays only</option>
            </select>
          </label>
          <label v-if="medicationForm.frequency === 'EVERY_X_DAYS'">
            <span>Every how many days?</span>
            <input v-model.number="medicationForm.intervalDays" type="number" min="2" required />
          </label>
          <button type="submit" :disabled="medicationSubmitting">
            {{ medicationSubmitting ? 'Saving…' : 'Add medication' }}
          </button>
        </form>

        <ul class="simple-medication-list">
          <li v-for="medication in parentMedications" :key="medication.medicationId">
            <strong>{{ medication.childName }}</strong>
            <span>{{ medication.name }} - {{ medication.dosage }} - {{ medication.schedule.specificTime }} - {{ medication.schedule.frequency }}</span>
            <span class="status-badge" :class="statusClass(medication.status)">{{ medication.status }}</span>
          </li>
          <p v-if="parentMedications.length === 0" class="empty-state">No medication added yet.</p>
        </ul>
      </section>
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
import NotificationCenter from '../components/NotificationCenter.vue';
import { currentUser } from '../state/authStore';
import {
  MEDICATION_STATUSES,
  addAllergy as storeAddAllergy,
  addChild as storeAddChild,
  addDisease as storeAddDisease,
  addMedication as storeAddMedication,
  editAllergy as storeEditAllergy,
  editDisease as storeEditDisease,
  loadChildren,
  kindercareStore,
  parentChildren,
  removeAllergy as storeRemoveAllergy,
  setParentAvatar,
  removeDisease as storeRemoveDisease,
  uploadPrescription as storeUploadPrescription,
  deleteChild as storeDeleteChild
} from '../state/kindercareStore';

export default {
  name: 'ParentDashboard',
  components: {
    NotificationCenter
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
      forms: this.emptyForms(),
      quickActions: [
        { key: 'child', label: 'Add child', icon: 'Kid' },
        { key: 'allergy', label: 'Add allergy', icon: 'All' },
        { key: 'disease', label: 'Add chronic disease', icon: 'Doc' }
      ],
      allergySuggestions: ['Peanuts', 'Milk', 'Eggs', 'Bee stings', 'Gluten', 'Dust', 'Other'],
      diseaseSuggestions: ['Asthma', 'Diabetes', 'Epilepsy', 'Heart condition', 'Food allergy', 'Other'],
      medicationForm: {
        childId: parentChildren()[0]?.id || null,
        name: '',
        dosage: '',
        time: '12:00',
        frequency: 'DAILY',
        intervalDays: 2
      },
      medicationSubmitting: false
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
        medications: [],
        prescriptionFileName: null,
        location: { lat: 0, lng: 0 }
      };
    },
    parentMedications() {
      return this.parentChildren
        .flatMap((child) => (child.medications || []).map((medication) => ({
          ...medication,
          childName: child.name,
          schedule: { specificTime: '12:00', ...(medication.schedule || {}) },
          status: this.medicationStatus(medication)
        })))
        .sort((first, second) => (first.schedule.specificTime || '').localeCompare(second.schedule.specificTime || ''));
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
        disease: 'Add chronic disease'
      };

      return titles[this.activeDialog] || '';
    }
  },
  async mounted() {
    await loadChildren();
    if (!this.selectedChildId && this.parentChildren.length > 0) {
      this.selectedChildId = this.parentChildren[0].id;
    }
    if (!this.medicationForm.childId && this.parentChildren.length > 0) {
      this.medicationForm.childId = this.parentChildren[0].id;
    }
  },
  errorCaptured() {
    this.componentError = 'A dashboard component failed to load.';
    return false;
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
        }
      };
    },
    firstName(name) {
      return (name || '').split(' ')[0] || 'your child';
    },
    meaningfulItems(items) {
      return (items || []).filter((item) => item && !['None', 'None known'].includes(item));
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
    async submitParentMedication() {
      if (this.medicationSubmitting) return;
      const { childId, name, dosage, time, frequency, intervalDays } = this.medicationForm;
      if (!childId || !name || !dosage || !time) return;

      this.medicationSubmitting = true;
      try {
        const saved = await storeAddMedication(childId, { name, dosage, time, frequency, intervalDays });
        if (!saved) return;
        this.medicationForm.name = '';
        this.medicationForm.dosage = '';
        this.medicationForm.time = '12:00';
        this.medicationForm.frequency = 'DAILY';
        this.medicationForm.intervalDays = 2;
      } finally {
        this.medicationSubmitting = false;
      }
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
    handleParentAvatar(event) {
      const [file] = event.target.files;

      if (!file) return;

      const reader = new FileReader();
      reader.onload = (e) => {
        setParentAvatar(e.target.result);
      };
      reader.readAsDataURL(file);
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

.quick-actions button:nth-child(1) span {
  background: rgba(34, 197, 94, 0.18);
  color: #166534;
}

.quick-actions button:nth-child(2) span {
  background: rgba(59, 130, 246, 0.18);
  color: #1e3a8a;
}

.quick-actions button:nth-child(3) span {
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

.health-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
  grid-template-columns: minmax(0, 1fr);
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

.simple-medication-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 12px;
  align-items: end;
  margin-top: 16px;
}

.simple-medication-list {
  display: grid;
  gap: 10px;
  margin: 16px 0 0;
  padding: 0;
  list-style: none;
}

.simple-medication-list li {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px 14px;
  background: var(--color-bg-secondary);
}

.status-badge {
  border-radius: 20px;
  padding: 6px 12px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  font-weight: 700;
}

.status-badge.status-upcoming {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.status-badge.status-pending {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.status-badge.status-taken {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.status-badge.status-missed {
  background: var(--color-missed);
  color: var(--color-missed-text);
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
:global([data-theme="dark"]) .parent-dashboard .quick-actions button,
:global([data-theme="dark"]) .parent-dashboard .health-summary article,
:global([data-theme="dark"]) .parent-dashboard .panel,
:global([data-theme="dark"]) .parent-dashboard .simple-medication-list li,
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
:global([data-theme="dark"]) .parent-dashboard .health-summary p {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .parent-dashboard .eyebrow,
:global([data-theme="dark"]) .parent-dashboard .health-summary span {
  color: #94a3b8;
}

:global([data-theme="dark"]) .parent-dashboard select,
:global([data-theme="dark"]) .parent-dashboard input,
:global([data-theme="dark"]) .parent-dashboard textarea,
:global([data-theme="dark"]) .parent-dashboard .saved-note {
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

:global([data-theme="dark"]) .parent-dashboard .quick-actions button:nth-child(3) span {
  background: var(--gradient-today-card);
}

:global([data-theme="dark"]) .parent-dashboard .quick-actions button:nth-child(4) span,
:global([data-theme="dark"]) .parent-dashboard .health-summary article:nth-child(3) {
  background: var(--gradient-children-card);
}

@media (max-width: 980px) {
  .welcome-panel,
  .manage-grid,
  .health-summary {
    grid-template-columns: 1fr;
  }

  .quick-actions {
    grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  }
}

@media (max-width: 640px) {
  .parent-dashboard {
    padding: 20px;
  }

  .topbar,
  .child-toolbar {
    grid-template-columns: 1fr;
  }

  .parent-top-actions {
    width: 100%;
  }

  .quick-actions {
    grid-template-columns: repeat(2, minmax(0, 1fr));
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
