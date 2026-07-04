<template>
  <main class="dashboard admin-dashboard">
    <nav class="topbar" :class="{ 'panel-dark': isDark }">
      <div>
        <p class="eyebrow">Admin control center</p>
        <h1>Medication tasks today</h1>
      </div>
      <div class="top-actions">
        <div class="top-action-buttons">
          <div class="control-actions" aria-label="Dashboard controls">
            <button class="theme-button" type="button" @click="$emit('toggle-theme')">Theme</button>
            <button class="emergency" type="button" @click="openEmergency">Emergency mode</button>
            <button class="logout-button" type="button" @click="$emit('logout')">Log out</button>
          </div>
        </div>
      </div>
    </nav>

    <section class="hero-strip" :class="{ 'panel-dark': isDark }">
      <div>
        <p class="eyebrow">Live care operations</p>
        <h2>Supporting children’s health, learning and wellbeing every day.</h2>
        <p class="care-welcome-message">A shared workspace for safe care, engaging activities and confident handovers.</p>
        <p>Staff can review the day, coordinate routines and keep each child’s information ready when it matters.</p>
      </div>
    </section>

    <section class="children-panel">
      <header>
        <div>
          <p class="eyebrow">All children</p>
          <h2>Children directory</h2>
        </div>
        <input
          v-model.trim="childSearch"
          type="search"
          class="children-search"
          placeholder="Search children..."
          aria-label="Search children"
        />
      </header>
      <ChildList
        :children="filteredChildren"
        :selected-child-id="selectedEmergencyChildId"
        title="Children"
        eyebrow="All children"
        @select-child="selectedEmergencyChildId = $event"
      />
      <p v-if="filteredChildren.length === 0" class="empty-state">No children match your search.</p>
    </section>

    <section v-if="taskModalActive" class="modal-backdrop medication-task-backdrop" @click.self="closeTaskModal" role="dialog" aria-modal="true">
      <form
        class="modal medication-task-modal"
        @submit.prevent="saveTask"
        @invalid.capture="setEnglishValidationMessage"
        @input.capture="clearValidationMessage"
        @change.capture="clearValidationMessage"
      >
        <header class="medication-task-header">
          <div class="medication-title-group">
            <span class="medication-icon" aria-hidden="true"><CareIcon name="health" /></span>
            <div>
              <p class="eyebrow">Medication task</p>
              <h2>{{ taskModalMode === 'edit' ? 'Edit medication task' : 'Add medication task' }}</h2>
              <p class="modal-intro">Plan a clear, safe care reminder for the classroom team.</p>
            </div>
          </div>
          <button class="medication-close-button" type="button" aria-label="Close medication task form" @click="closeTaskModal">
            <span aria-hidden="true">Close</span>
          </button>
        </header>

        <div class="medication-task-content">
          <section class="medication-form-card">
            <div class="section-heading">
              <p class="eyebrow">Child and medicine</p>
              <h3>Care details</h3>
            </div>

            <div class="modal-fields medication-field-grid">
              <label class="form-field form-field-wide">
                <span>Child name</span>
                <select v-model.number="taskForm.childId" required>
                  <option v-for="child in children" :key="child.id" :value="child.id">{{ child.name }}</option>
                </select>
                <small>Choose the child this care task belongs to.</small>
              </label>
              <label class="form-field">
                <span>Medication</span>
                <input v-model="taskForm.medicationName" placeholder="E.g. Amoxicillin" required />
                <small>Use the exact medication name from the parent note or package.</small>
              </label>
              <label class="form-field">
                <span>Dosage</span>
                <input v-model="taskForm.dosage" placeholder="E.g. 5 ml" required />
                <small>Include the unit, for example ml, drops or tablet count.</small>
              </label>
            </div>
          </section>

          <section class="medication-form-card">
            <div class="section-heading">
              <p class="eyebrow">Schedule</p>
              <h3>When should it happen?</h3>
            </div>

            <div class="modal-fields medication-field-grid">
              <label class="form-field">
                <span>Date</span>
                <input v-model="taskForm.date" type="date" required />
                <small>Double-check the planned care date before saving.</small>
              </label>
              <label class="form-field">
                <span>Hour</span>
                <input v-model="taskForm.time" type="time" required />
                <small>Use the time agreed with the parent or care plan.</small>
              </label>
              <label class="form-field">
                <span>Status</span>
                <select v-model="taskForm.status" required>
                  <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
                </select>
                <small>New tasks usually start as Pending.</small>
              </label>
            </div>
          </section>

          <aside class="medication-safety-note" aria-label="Medication safety note">
            <strong>Safety note</strong>
            <span>Confirm medication, dosage, date and time with the parent record before administration.</span>
          </aside>

          <p v-if="taskError" class="form-error medication-form-error">{{ taskError }}</p>

          <div class="modal-actions medication-task-actions">
            <button type="submit" class="primary-medication-button">{{ taskModalMode === 'edit' ? 'Save changes' : 'Create task' }}</button>
            <button type="button" class="secondary-button medication-cancel-button" @click="closeTaskModal">Cancel</button>
          </div>
        </div>
      </form>
    </section>

    <section v-if="emergencyActive" class="modal-backdrop" @click.self="closeEmergency" role="dialog" aria-modal="true">
      <div class="modal emergency-modal">
        <button class="modal-close" type="button" aria-label="Close map" @click="closeEmergency">x</button>

        <div class="emergency-body">
          <aside class="emergency-poi-panel" aria-label="Nearby emergency support">
            <div v-if="poiLoading" class="poi-state">Searching for nearby support…</div>
            <div v-else-if="displayedEmergencyPOIs.length === 0" class="poi-state">No nearby emergency support found.</div>
            <div v-else class="poi-list">
              <EmergencyPoiCard
                v-for="poi in displayedEmergencyPOIs"
                :key="poi.id"
                :poi="poi"
                :from="selectedEmergencyLocation"
              />
            </div>
          </aside>

          <div id="emergency-map" class="emergency-map" aria-label="Child location map"></div>
        </div>
      </div>
    </section>

    <section class="stats-row">
      <article class="pending" :class="{ 'panel-dark': isDark }">
        <span>{{ stats.Pending }}</span>
        <p>Pending today</p>
      </article>
      <article class="taken" :class="{ 'panel-dark': isDark }">
        <span>{{ stats.Taken }}</span>
        <p>Taken today</p>
      </article>
      <article class="missed" :class="{ 'panel-dark': isDark }">
        <span>{{ stats.Missed }}</span>
        <p>Missed today</p>
      </article>
      <article class="upcoming" :class="{ 'panel-dark': isDark }">
        <span>{{ stats.Upcoming }}</span>
        <p>Upcoming today</p>
      </article>
    </section>

    <CareHighlights class="care-highlights-panel" :medication-count="stats.Pending + stats.Upcoming" />

    <section class="operations-row">
      <article class="day-timeline-panel">
        <div class="timeline-section" v-for="section in medicationTimelineSections" :key="section.key">
          <header>
            <div>
              <h3>{{ section.label }}</h3>
              <p>{{ section.range }}</p>
            </div>
            <span>{{ section.tasks.length }}</span>
          </header>
          <div class="mini-timeline">
            <p v-if="section.tasks.length === 0">No medication scheduled.</p>
            <span
              v-for="task in section.tasks.slice(0, 4)"
              :key="`${section.key}-${task.medicationId}`"
              :class="`status-${task.status.toLowerCase()}`"
            >
              {{ task.scheduledTime }} {{ task.medicationName }}
            </span>
          </div>
        </div>
      </article>

      <article class="holiday-panel">
        <div>
          <p class="eyebrow">External API · Nager.Date</p>
          <h2>Upcoming German public holidays</h2>
        </div>
        <p v-if="holidays.length === 0" class="empty-state">No holiday information available.</p>
        <ul v-else class="holiday-list">
          <li v-for="holiday in holidays" :key="holiday.date">
            <span class="holiday-date">{{ holiday.date }}</span>
            <span class="holiday-name">{{ holiday.localName }}</span>
          </li>
        </ul>
      </article>
    </section>

    <section class="admin-grid">
      <div class="task-stack">
        <AdminCalendar
          :tasks="tasks"
          @day-click="openCalendarTaskModal"
          @create-task="openCalendarTaskModal"
          @edit-task="openTaskModal('edit', $event)"
          @delete-task="deleteTask($event)"
        />

        <MedicationTaskCard
          v-for="task in filteredTasks"
          :key="task.taskId"
          :task="task"
          @confirm="confirmMedication"
          @edit="openTaskModal('edit', $event)"
          @delete="deleteTask($event)"
          @status-change="changeTaskStatus"
        />
      </div>

      <aside class="control-stack">
        <MedicationAssistant compact />
        <ChildCareAssistant />
      </aside>
    </section>
  </main>
</template>

<script>
import AdminCalendar from '../components/AdminCalendar.vue';
import ChildList from '../components/ChildList.vue';
import EmergencyPoiCard from '../components/EmergencyPoiCard.vue';
import MedicationAssistant from '../components/MedicationAssistant.vue';
import ChildCareAssistant from '../components/ChildCareAssistant.vue';
import CareHighlights from '../components/CareHighlights.vue';
import MedicationTaskCard from '../components/MedicationTaskCard.vue';
import CareIcon from '../components/CareIcon.vue';
import L from 'leaflet';
import { MEDICATION_STATUSES, kindercareStore, markMedicationTaken, setMedicationStatus, taskReminderDue, addMedication, editMedication, removeMedication, loadChildren, loadMedicationTasks } from '../state/kindercareStore';
import { fetchNearbyEmergencyPOIs } from '../services/emergencyService';
import { getGermanPublicHolidays } from '../services/holidayService.js';

const FALLBACK_POLICE_STATION = {
  id: 'fallback-police-station',
  name: 'Police Station Alexanderplatz',
  type: 'police',
  label: 'Police station',
  lat: 52.5215,
  lng: 13.4132
};

export default {
  name: 'AdminDashboard',
  components: {
    AdminCalendar,
    ChildList,
    EmergencyPoiCard,
    MedicationAssistant,
    ChildCareAssistant,
    CareHighlights,
    MedicationTaskCard,
    CareIcon
  },
  props: {
    isDark: {
      type: Boolean,
      default: false
    }
  },
  emits: ['navigate', 'logout'],
  data() {
    return {
      emergencyActive: false,
      selectedEmergencyChildId: null,
      nearbyPOIs: [],
      poiLoading: false,
      emergencyMap: null,
      childLocationMarker: null,
      taskModalActive: false,
      taskModalMode: 'add',
      taskForm: {
        medicationId: null,
        childId: null,
        medicationName: '',
        dosage: '',
        date: this.todayDateKey(),
        time: '',
        status: 'Pending'
      },
      statusOptions: MEDICATION_STATUSES,
      taskError: '',
      holidays: [],
      syncInterval: null,
      childSearch: ''
    };
  },
  async mounted() {
    await Promise.all([loadChildren(), loadMedicationTasks()]);
    try {
      this.holidays = await getGermanPublicHolidays();
    } catch {
      this.holidays = [];
    }
    if (!this.selectedEmergencyChildId && this.children.length > 0) {
      this.selectedEmergencyChildId = this.children[0].id;
    }
    this.syncInterval = window.setInterval(() => {
      loadChildren();
      loadMedicationTasks();
    }, 5000);
  },
  beforeUnmount() {
    window.clearInterval(this.syncInterval);
    if (this.emergencyMap) {
      this.emergencyMap.remove();
      this.emergencyMap = null;
      this.childLocationMarker = null;
    }
  },
  computed: {
    selectedEmergencyChild() {
      return this.children.find((child) => child.id === this.selectedEmergencyChildId) || this.children[0] || null;
    },
    selectedEmergencyLocation() {
      const { lat = 52.52, lng = 13.405 } = this.selectedEmergencyChild?.location || {};
      return {
        lat,
        lng
      };
    },
    hasSelectedEmergencyLocation() {
      const location = this.selectedEmergencyChild?.location;
      return Number.isFinite(location?.lat) && Number.isFinite(location?.lng);
    },
    displayedEmergencyPOIs() {
      const pois = [...this.nearbyPOIs];
      if (!pois.some((poi) => poi.type === 'police')) {
        pois.push(this.withDistance(FALLBACK_POLICE_STATION));
      }
      return pois;
    },
    children() {
      return kindercareStore.children;
    },
    filteredChildren() {
      const query = this.childSearch.trim().toLowerCase();
      if (!query) return this.children;
      return this.children.filter((child) => (child.name || '').toLowerCase().includes(query));
    },
    tasks() {
      return kindercareStore.medicationTasks.map((task) => ({
        ...task,
        scheduledDate: task.scheduledDate || this.todayDateKey(),
        status: this.statusOptions.includes(task.status) ? task.status : 'Pending',
        reminderDue: taskReminderDue(task)
      }));
    },
    filteredTasks() {
      return this.tasks.filter((task) => task.scheduledToday);
    },
    medicationTimelineSections() {
      const sections = [
        { key: 'morning', label: 'Morning', range: 'Before 12:00', tasks: [] },
        { key: 'afternoon', label: 'Afternoon', range: '12:00 - 17:00', tasks: [] },
        { key: 'evening', label: 'Evening', range: 'After 17:00', tasks: [] }
      ];

      this.filteredTasks.forEach((task) => {
        const hour = Number((task.scheduledTime || '12:00').split(':')[0]);
        const target = hour < 12 ? sections[0] : hour < 17 ? sections[1] : sections[2];
        target.tasks.push(task);
      });

      return sections;
    },
    stats() {
      return this.filteredTasks.reduce((counts, task) => {
        const status = this.statusOptions.includes(task.status) ? task.status : 'Pending';
        counts[status] += 1;
        return counts;
      }, { Pending: 0, Taken: 0, Missed: 0, Upcoming: 0 });
    },
  },
  watch: {
    selectedEmergencyChildId() {
      if (this.emergencyActive) {
        this.$nextTick(() => this.initEmergencyModal());
      }
    }
  },
  methods: {
    todayDateKey() {
      const date = new Date();
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    async confirmMedication(medicationId) {
      await markMedicationTaken(medicationId);
    },
    openEmergency() {
      if (!this.selectedEmergencyChildId) {
        this.selectedEmergencyChildId = this.children?.[0]?.id || null;
      }
      this.emergencyActive = true;
      this.$nextTick(() => this.initEmergencyModal());
    },
    withDistance(poi) {
      return {
        ...poi,
        distance: Math.round(this.distanceBetweenMeters(this.selectedEmergencyLocation, poi))
      };
    },
    distanceBetweenMeters(from, to) {
      const toRadians = (value) => (value * Math.PI) / 180;
      const radius = 6371000;
      const deltaLat = toRadians(to.lat - from.lat);
      const deltaLng = toRadians(to.lng - from.lng);
      const value =
        Math.sin(deltaLat / 2) ** 2 +
        Math.cos(toRadians(from.lat)) * Math.cos(toRadians(to.lat)) * Math.sin(deltaLng / 2) ** 2;

      return radius * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
    },
    initEmergencyModal() {
      const { lat, lng } = this.selectedEmergencyLocation;
      const mapEl = document.getElementById('emergency-map');
      if (!mapEl) {
        return;
      }

      try {
        if (!this.emergencyMap) {
          this.emergencyMap = L.map(mapEl, { scrollWheelZoom: false }).setView([lat, lng], 13);
          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
          }).addTo(this.emergencyMap);
        } else {
          this.emergencyMap.setView([lat, lng], 13);
        }

        if (this.childLocationMarker) {
          this.emergencyMap.removeLayer(this.childLocationMarker);
          this.childLocationMarker = null;
        }
        if (this.hasSelectedEmergencyLocation) {
          this.childLocationMarker = L.marker([lat, lng]).addTo(this.emergencyMap);
        }
        this.loadNearbyPois(lat, lng);
      } catch (e) {
        console.warn('Leaflet map init error', e);
      }
    },
    async loadNearbyPois(lat, lng) {
      this.poiLoading = true;
      try {
        this.nearbyPOIs = await fetchNearbyEmergencyPOIs(lat, lng);
      } catch {
        this.nearbyPOIs = [];
      } finally {
        this.poiLoading = false;
      }
    },
    openCalendarTaskModal(date) {
      this.openTaskModal('add', null, date);
    },
    openTaskModal(mode, medicationId = null, selectedDate = null) {
      this.taskModalMode = mode;
      this.taskError = '';

      if (mode === 'edit' && medicationId) {
        const task = this.tasks.find((item) => item.medicationId === medicationId);
        if (task) {
          this.taskForm = {
            medicationId: task.medicationId,
            childId: task.childId,
            medicationName: task.medicationName,
            dosage: task.dosage,
            date: task.scheduledDate || this.todayDateKey(),
            time: task.scheduledTime,
            status: this.statusOptions.includes(task.status) ? task.status : 'Pending'
          };
        }
      } else {
        this.taskForm = {
          medicationId: null,
          childId: this.children?.[0]?.id || null,
          medicationName: '',
          dosage: '',
          date: selectedDate || this.todayDateKey(),
          time: '12:00',
          status: 'Pending'
        };
      }

      this.taskModalActive = true;
    },
    closeTaskModal() {
      this.taskModalActive = false;
      this.taskError = '';
    },
    setEnglishValidationMessage(event) {
      const field = event.target;
      if (typeof field?.setCustomValidity !== 'function') return;
      field.setCustomValidity(field.validity?.valueMissing ? 'Please fill out this field.' : '');
    },
    clearValidationMessage(event) {
      const field = event.target;
      if (typeof field?.setCustomValidity === 'function') {
        field.setCustomValidity('');
      }
    },
    async saveTask() {
      if (!this.taskForm.childId || !this.taskForm.medicationName || !this.taskForm.dosage || !this.taskForm.date || !this.taskForm.time) {
        this.taskError = 'Please complete all required fields.';
        return;
      }

      let saved;
      if (this.taskModalMode === 'edit' && this.taskForm.medicationId) {
        saved = await editMedication(this.taskForm.childId, this.taskForm.medicationId, {
          name: this.taskForm.medicationName,
          dosage: this.taskForm.dosage,
          date: this.taskForm.date,
          time: this.taskForm.time,
          status: this.taskForm.status,
          childId: this.taskForm.childId
        });
      } else {
        saved = await addMedication(this.taskForm.childId, {
          name: this.taskForm.medicationName,
          dosage: this.taskForm.dosage,
          date: this.taskForm.date,
          time: this.taskForm.time,
          status: this.taskForm.status
        });
      }
      if (!saved) {
        this.taskError = 'The medication could not be saved. Check the displayed error and try again.';
        return;
      }
      this.closeTaskModal();
    },
    async deleteTask(medicationId) {
      const task = this.tasks.find((item) => item.medicationId === medicationId);
      if (!task) {
        return;
      }

      if (!window.confirm('Delete this medication task? This action cannot be undone.')) {
        return;
      }

      await removeMedication(task.childId, medicationId);
    },
    closeEmergency() {
      this.emergencyActive = false;
      if (this.emergencyMap) {
        this.emergencyMap.remove();
        this.emergencyMap = null;
        this.childLocationMarker = null;
      }
    },
    async changeTaskStatus({ medicationId, status }) {
      if (!medicationId || !this.statusOptions.includes(status)) {
        return;
      }

      await setMedicationStatus(medicationId, status);
    }
  }
};
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: clamp(14px, 2vw, 24px);
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
  position: relative;
  isolation: isolate;
  overflow: hidden;
}

.dashboard::before {
  position: absolute;
  z-index: -1;
  top: 90px;
  right: -100px;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(157, 210, 255, .22), rgba(157, 210, 255, 0) 68%);
  content: '';
  pointer-events: none;
}

:global([data-theme="dark"]) .dashboard {
  background:
    linear-gradient(
      180deg,
      #0f172a 0%,
      #111827 100%
    );
  color: #f8fafc;
}

.topbar,
.hero-strip,
.stats-row,
.admin-grid {
  max-width: 1240px;
  margin: 0 auto;
}

.topbar {
  display: grid;
  grid-template-columns: minmax(0, auto) minmax(0, 1fr);
  gap: 14px;
  align-items: center;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 12px 16px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
  margin-bottom: 10px;
  backdrop-filter: blur(10px);
}

:global([data-theme="dark"]) .topbar {
  background: rgba(17, 24, 39, 0.92);
  border-color: rgba(255, 255, 255, 0.06);
  box-shadow: 0 14px 34px rgba(0, 0, 0, 0.34);
}

.topbar > div:first-child {
  min-width: 0;
}

.eyebrow,
h1,
h2,
p {
  margin: 0;
}

.eyebrow {
  color: var(--color-brand);
  font-size: 0.76rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

h1 {
  margin-top: 2px;
  font-size: clamp(1.5rem, 2.4vw, 2rem);
  font-weight: 800;
  line-height: 1.12;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:global([data-theme="dark"]) .admin-dashboard h1 {
  background: none;
  color: #f8fafc;
  -webkit-text-fill-color: #f8fafc;
}

.top-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-end;
  min-width: 0;
}

.top-action-buttons,
.control-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.top-action-buttons {
  justify-content: flex-end;
}

.control-actions {
  padding-left: 8px;
  border-left: 1px solid var(--color-border);
}

button,
select {
  font: inherit;
  transition: all 0.25s ease;
}

.top-actions button {
  min-height: 42px;
  border: none;
  border-radius: 14px;
  padding: 10px 15px;
  background: linear-gradient(135deg, var(--color-brand-dark), #1a202c);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
  white-space: nowrap;
}

.top-actions button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(15, 23, 42, 0.16);
}

.top-actions .theme-button {
  background: linear-gradient(135deg, #eef2ff 0%, #c7d2fe 100%);
  color: #312e81;
}

.top-actions .primary-action {
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-dark));
}

.top-actions .emergency {
  background: linear-gradient(135deg, var(--color-danger), #c53030);
}

.top-actions .emergency:hover {
  box-shadow: 0 6px 20px rgba(229, 62, 62, 0.4);
}

.top-actions .logout-button {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  color: #334155;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.08);
}

.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
  padding: 16px;
  animation: fade-in 0.18s ease;
}

.modal {
  width: min(100%, 620px);
  max-height: 92vh;
  overflow-y: auto;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 24px;
  box-shadow: var(--shadow-xl);
}

.medication-task-backdrop {
  background:
    radial-gradient(circle at 20% 12%, rgba(255, 237, 213, 0.24), transparent 32%),
    radial-gradient(circle at 78% 24%, rgba(199, 210, 254, 0.22), transparent 28%),
    rgba(15, 23, 42, 0.58);
}

.medication-task-modal {
  width: min(940px, 100%);
  padding: 0;
  border: 1px solid rgba(49, 130, 206, 0.16);
  border-radius: 28px;
  background:
    linear-gradient(180deg, rgba(255, 251, 235, 0.82) 0%, rgba(240, 253, 250, 0.9) 42%, var(--color-bg-secondary) 100%);
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.22);
}

.medication-task-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  padding: 24px 26px 18px;
  border-bottom: 1px solid rgba(49, 130, 206, 0.12);
  background:
    linear-gradient(135deg, rgba(255, 247, 237, 0.96), rgba(236, 253, 245, 0.9)),
    var(--color-bg-secondary);
}

.medication-title-group {
  display: flex;
  gap: 14px;
  min-width: 0;
}

.medication-icon {
  display: grid;
  flex: 0 0 auto;
  width: 52px;
  height: 52px;
  place-items: center;
  border-radius: 18px;
  background: linear-gradient(135deg, #c7f9cc 0%, #bfdbfe 100%);
  color: #166534;
  box-shadow: 0 12px 24px rgba(49, 130, 206, 0.15);
}

.medication-icon :deep(.care-icon) {
  width: 27px;
  height: 27px;
  stroke-width: 2;
}

.medication-task-header h2 {
  margin-top: 3px;
  color: #0f172a;
  font-size: clamp(1.35rem, 2.2vw, 1.9rem);
  line-height: 1.12;
}

.modal-intro {
  margin-top: 7px;
  color: #64748b;
  font-size: 0.95rem;
  line-height: 1.5;
}

.medication-close-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 42px;
  border: 1px solid rgba(148, 163, 184, 0.38);
  border-radius: 999px;
  padding: 10px 16px;
  background: rgba(255, 255, 255, 0.86);
  color: #334155;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.08);
}

.medication-close-button:hover,
.medication-close-button:focus-visible {
  border-color: rgba(49, 130, 206, 0.36);
  background: #eff6ff;
  color: #1d4ed8;
  transform: translateY(-1px);
}

.medication-task-content {
  display: grid;
  gap: 16px;
  padding: 22px 26px 26px;
}

.medication-form-card {
  border: 1px solid rgba(148, 163, 184, 0.18);
  border-radius: 22px;
  padding: 18px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.08);
}

.section-heading {
  margin-bottom: 15px;
}

.section-heading h3 {
  margin: 3px 0 0;
  color: #1e293b;
  font-size: 1.05rem;
  line-height: 1.2;
}

.medication-field-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.form-field-wide {
  grid-column: 1 / -1;
}

.form-field span {
  color: #334155;
  font-size: 0.88rem;
  font-weight: 800;
}

.form-field input,
.form-field select {
  border-color: rgba(148, 163, 184, 0.32);
  background: #ffffff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.form-field input:focus,
.form-field select:focus {
  border-color: var(--color-brand);
  box-shadow: 0 0 0 4px rgba(49, 130, 206, 0.12);
  outline: none;
}

.form-field small {
  color: #64748b;
  font-size: 0.78rem;
  line-height: 1.4;
}

.medication-safety-note {
  display: grid;
  gap: 4px;
  border: 1px solid rgba(240, 168, 58, 0.34);
  border-radius: 18px;
  padding: 14px 16px;
  background: linear-gradient(135deg, rgba(255, 251, 235, 0.96), rgba(254, 243, 199, 0.78));
  color: #7c2d12;
  box-shadow: 0 10px 24px rgba(240, 168, 58, 0.12);
}

.medication-safety-note strong {
  font-size: 0.9rem;
}

.medication-safety-note span {
  color: #92400e;
  font-size: 0.86rem;
  line-height: 1.45;
}

.medication-form-error {
  margin: 0;
  border: 1px solid var(--color-missed-border);
  border-radius: 16px;
  padding: 12px 14px;
  background: var(--color-missed);
}

.medication-task-actions {
  justify-content: flex-end;
  margin-top: 0;
  padding-top: 4px;
}

.primary-medication-button,
.medication-cancel-button {
  min-height: 46px;
  border-radius: 14px;
  padding: 12px 18px;
  font-weight: 850;
  cursor: pointer;
}

.primary-medication-button {
  border: 0;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-dark));
  color: #fff;
  box-shadow: 0 14px 26px rgba(49, 130, 206, 0.24);
}

.primary-medication-button:hover,
.primary-medication-button:focus-visible {
  transform: translateY(-1px);
  box-shadow: 0 16px 30px rgba(49, 130, 206, 0.3);
}

:global([data-theme="dark"]) .medication-task-modal {
  border-color: rgba(255, 255, 255, 0.08);
  background: linear-gradient(180deg, #111827 0%, #0f172a 100%);
}

:global([data-theme="dark"]) .medication-task-header,
:global([data-theme="dark"]) .medication-form-card {
  border-color: rgba(255, 255, 255, 0.08);
  background: rgba(30, 41, 59, 0.82);
}

:global([data-theme="dark"]) .medication-task-header h2,
:global([data-theme="dark"]) .section-heading h3,
:global([data-theme="dark"]) .form-field span {
  color: #f8fafc;
}

:global([data-theme="dark"]) .modal-intro,
:global([data-theme="dark"]) .form-field small {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .form-field input,
:global([data-theme="dark"]) .form-field select,
:global([data-theme="dark"]) .medication-close-button {
  border-color: rgba(255, 255, 255, 0.12);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

.emergency-modal {
  position: relative;
  width: min(1120px, calc(100vw - 32px));
  height: min(76vh, 680px);
  max-height: 76vh;
  overflow: hidden;
  padding: 0;
  border-radius: 18px;
  animation: modal-rise 0.22s ease;
}

.modal-close {
  position: absolute;
  z-index: 1001;
  top: 10px;
  right: 10px;
  display: grid;
  width: 40px;
  height: 40px;
  place-items: center;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  cursor: pointer;
  font-weight: 900;
  line-height: 1;
}

.modal-close:hover {
  transform: rotate(90deg) scale(1.04);
  border-color: rgba(229, 62, 62, 0.35);
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.emergency-map {
  width: 100%;
  height: 100%;
  min-height: 0;
  border: 0;
  border-radius: 0;
  margin: 0;
}

.emergency-body {
  display: grid;
  grid-template-columns: minmax(min(280px, 100%), 0.75fr) minmax(0, 1.25fr);
  gap: 16px;
  width: 100%;
  height: 100%;
  padding: 16px;
  background: var(--color-bg-primary);
}

.emergency-poi-panel {
  min-width: 0;
  overflow: auto;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 12px;
  background: var(--color-bg-secondary);
}

.poi-list {
  display: grid;
  gap: 10px;
}

.poi-state {
  color: var(--color-text-secondary);
  font-weight: 700;
}

.modal-fields {
  display: grid;
  gap: 16px;
}

.modal-fields label {
  display: grid;
  gap: 8px;
  color: var(--color-text-primary);
}

.modal-fields input,
.modal-fields select,
.modal-fields textarea {
  width: 100%;
  min-height: 44px;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 12px 14px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

.modal-fields textarea {
  resize: vertical;
}

.modal-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 8px;
}

.form-error {
  color: var(--color-danger);
  font-weight: 700;
}

.success,
.error {
  margin-top: 14px;
  border-radius: 14px;
  padding: 12px 14px;
  font-weight: 900;
  box-shadow: var(--shadow-sm);
  animation: modal-rise 0.2s ease;
}

.success {
  border: 1px solid var(--color-taken-border);
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.error {
  border: 1px solid var(--color-missed-border);
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.hero-strip {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
  align-items: center;
  margin-top: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 16px 18px;
  background: linear-gradient(135deg, var(--bg-card) 0%, var(--color-bg-primary) 100%);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.hero-strip h2 {
  margin: 4px 0 6px;
  font-size: clamp(1.1rem, 1.6vw, 1.45rem);
  font-weight: 700;
  line-height: 1.2;
  color: #111827;
}

.hero-strip p:not(.eyebrow) {
  max-width: 720px;
  color: #64748b;
  font-size: 0.92rem;
  line-height: 1.5;
}

.care-welcome-message {
  margin: 8px 0 4px;
  color: var(--color-brand-dark) !important;
  font-size: .9rem !important;
  font-weight: 750;
}

:global([data-theme="dark"]) .hero-strip {
  border-color: rgba(255, 255, 255, 0.06);
  background:
    linear-gradient(135deg, rgba(30, 41, 59, 0.9) 0%, rgba(17, 24, 39, 0.96) 100%);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.32);
}

:global([data-theme="dark"]) .hero-strip h2,
:global([data-theme="dark"]) .panel header h2 {
  color: #f8fafc;
}

:global([data-theme="dark"]) .hero-strip p:not(.eyebrow),
:global([data-theme="dark"]) .panel header span {
  color: #cbd5e1;
}

.children-panel {
  max-width: 1240px;
  margin: 16px auto 0;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
}

.children-panel header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 14px;
}

.children-search {
  min-height: 42px;
  min-width: min(220px, 100%);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 14px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font: inherit;
}

:global([data-theme="dark"]) .children-panel {
  border-color: rgba(255, 255, 255, 0.06);
  background: linear-gradient(135deg, #111827 0%, #1e293b 100%);
}

:global([data-theme="dark"]) .children-search {
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(180px, 100%), 1fr));
  gap: 10px;
  margin-top: 12px;
}

.care-highlights-panel {
  max-width: 1240px;
  margin: 12px auto 0;
}

.stats-row article {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef6ff 100%);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 78px;
  padding: 12px 14px;
  text-align: left;
}

:global([data-theme="dark"]) .stats-row article {
  border-color: rgba(255, 255, 255, 0.06);
  background: linear-gradient(135deg, #111827 0%, #1e293b 100%);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
}

.stats-row article:hover,
.panel:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stats-row span {
  display: block;
  font-size: 1.45rem;
  font-weight: 800;
  line-height: 1.1;
  margin-bottom: 3px;
}

.stats-row p {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  font-weight: 600;
}

.pending span {
  color: var(--color-warning);
}

.stats-row article.pending {
  border-color: rgba(240, 168, 58, 0.35);
  background: linear-gradient(135deg, var(--color-bg-secondary), var(--color-pending));
}

.taken span {
  color: var(--color-success);
}

.stats-row article.taken {
  border-color: rgba(56, 161, 105, 0.35);
  background: linear-gradient(135deg, var(--color-bg-secondary), var(--color-taken));
}

.missed span {
  color: var(--color-danger);
}

.stats-row article.missed {
  border-color: rgba(229, 62, 62, 0.35);
  background: linear-gradient(135deg, var(--color-bg-secondary), var(--color-missed));
}

.upcoming span {
  color: var(--color-info);
}

.stats-row article.upcoming {
  border-color: rgba(49, 130, 206, 0.35);
  background: linear-gradient(135deg, var(--color-bg-secondary), var(--color-upcoming));
}

.operations-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 12px;
  max-width: 1240px;
  margin: 16px auto 0;
}

.day-timeline-panel,
.holiday-panel {
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 18px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
}

.holiday-panel {
  display: grid;
  align-content: start;
  gap: 14px;
}

.holiday-panel h2 {
  margin-top: 6px;
  color: var(--color-text-primary);
  font-size: 1.3rem;
}

.holiday-list {
  display: grid;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.holiday-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
}

.holiday-date {
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 800;
  white-space: nowrap;
}

.holiday-name {
  color: var(--color-text-primary);
  font-weight: 700;
  text-align: right;
}

.day-timeline-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(180px, 100%), 1fr));
  gap: 12px;
}

.timeline-section {
  min-width: 0;
  border: 1px solid var(--color-border-light);
  border-radius: 14px;
  padding: 14px;
  background: var(--color-bg-primary);
}

.timeline-section header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.timeline-section h3,
.timeline-section p {
  margin: 0;
}

.timeline-section h3 {
  color: var(--color-text-primary);
  font-size: 1rem;
}

.timeline-section p {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  font-weight: 700;
}

.timeline-section header > span {
  display: grid;
  min-width: 32px;
  height: 32px;
  place-items: center;
  border-radius: 999px;
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  font-weight: 900;
}

.mini-timeline {
  display: grid;
  gap: 8px;
  margin-top: 12px;
}

.mini-timeline > span {
  border-radius: 999px;
  padding: 7px 9px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-size: 0.78rem;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-timeline .status-taken {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.mini-timeline .status-missed {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.mini-timeline .status-pending {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.mini-timeline .status-upcoming {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.admin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(min(320px, 100%), 380px);
  gap: 16px;
  margin-top: 16px;
  align-items: start;
}

.task-stack,
.control-stack {
  display: grid;
  gap: 16px;
}

.empty-state {
  border-radius: 10px;
  padding: 12px 16px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-weight: 600;
}

:global([data-theme="dark"]) .admin-dashboard .hero-strip,
:global([data-theme="dark"]) .admin-dashboard .day-timeline-panel,
:global([data-theme="dark"]) .admin-dashboard .holiday-panel,
:global([data-theme="dark"]) .admin-dashboard .timeline-section,
:global([data-theme="dark"]) .admin-dashboard .modal,
:global([data-theme="dark"]) .admin-dashboard :deep(.qr-card),
:global([data-theme="dark"]) .admin-dashboard :deep(.verification-history),
:global([data-theme="dark"]) .admin-dashboard :deep(.calendar-card),
:global([data-theme="dark"]) .admin-dashboard :deep(.medication-card) {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background:
    linear-gradient(
      135deg,
      #111827 0%,
      #1e293b 100%
    );
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
}

:global([data-theme="dark"]) .admin-dashboard .hero-strip {
  background:
    linear-gradient(
      135deg,
      #111827 0%,
      #1e293b 100%
    );
}

:global([data-theme="dark"]) .admin-dashboard h1,
:global([data-theme="dark"]) .admin-dashboard h2,
:global([data-theme="dark"]) .admin-dashboard h3,
:global([data-theme="dark"]) .admin-dashboard strong,
:global([data-theme="dark"]) .admin-dashboard label,
:global([data-theme="dark"]) .admin-dashboard dt,
:global([data-theme="dark"]) .admin-dashboard dd {
  color: #f8fafc;
  -webkit-text-fill-color: #f8fafc;
}

:global([data-theme="dark"]) .admin-dashboard p,
:global([data-theme="dark"]) .admin-dashboard span,
:global([data-theme="dark"]) .admin-dashboard small,
:global([data-theme="dark"]) .admin-dashboard .hero-strip p:not(.eyebrow),
:global([data-theme="dark"]) .admin-dashboard .timeline-section p {
  color: #cbd5e1;
}

:global([data-theme="dark"]) .admin-dashboard .eyebrow,
:global([data-theme="dark"]) .admin-dashboard .empty-state,
:global([data-theme="dark"]) .admin-dashboard .date-widget small {
  color: #94a3b8;
}

:global([data-theme="dark"]) .admin-dashboard select,
:global([data-theme="dark"]) .admin-dashboard input,
:global([data-theme="dark"]) .admin-dashboard textarea,
:global([data-theme="dark"]) .admin-dashboard .timeline-section,
:global([data-theme="dark"]) .admin-dashboard .empty-state {
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .admin-dashboard input::placeholder,
:global([data-theme="dark"]) .admin-dashboard textarea::placeholder {
  color: #94a3b8;
  -webkit-text-fill-color: #94a3b8;
}

:global([data-theme="dark"]) .admin-dashboard .mini-timeline > span {
  border: 1px solid rgba(255, 255, 255, 0.08);
}

:global([data-theme="dark"]) .admin-dashboard .mini-timeline .status-taken {
  background: var(--color-taken);
  color: var(--color-taken-text);
  -webkit-text-fill-color: var(--color-taken-text);
}

:global([data-theme="dark"]) .admin-dashboard .mini-timeline .status-missed {
  background: var(--color-missed);
  color: var(--color-missed-text);
  -webkit-text-fill-color: var(--color-missed-text);
}

:global([data-theme="dark"]) .admin-dashboard .mini-timeline .status-pending {
  background: var(--color-pending);
  color: var(--color-pending-text);
  -webkit-text-fill-color: var(--color-pending-text);
}

:global([data-theme="dark"]) .admin-dashboard .mini-timeline .status-upcoming {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
  -webkit-text-fill-color: var(--color-upcoming-text);
}

:global([data-theme="dark"]) .admin-dashboard .medication-safety-note {
  border-color: var(--color-pending-border);
  background: var(--color-pending);
  color: var(--color-pending-text);
}

:global([data-theme="dark"]) .admin-dashboard .medication-safety-note strong,
:global([data-theme="dark"]) .admin-dashboard .medication-safety-note span {
  color: var(--color-pending-text);
  -webkit-text-fill-color: var(--color-pending-text);
}

:global([data-theme="dark"]) .admin-dashboard .primary-medication-button,
:global([data-theme="dark"]) .admin-dashboard .top-actions button {
  color: #ffffff;
  -webkit-text-fill-color: #ffffff;
}

:global([data-theme="dark"]) .admin-dashboard .top-actions .theme-button,
:global([data-theme="dark"]) .admin-dashboard .top-actions .logout-button {
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.08);
  color: #f8fafc;
  -webkit-text-fill-color: #f8fafc;
}

:global([data-theme="dark"]) .admin-dashboard .secondary-button,
:global([data-theme="dark"]) .admin-dashboard :deep(.toggle-button) {
  border: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
}

:global([data-theme="dark"]) .admin-dashboard .secondary-button:hover,
:global([data-theme="dark"]) .admin-dashboard :deep(.toggle-button:hover) {
  background: rgba(255, 255, 255, 0.12);
}

@keyframes fade-in {
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
}

@keyframes modal-rise {
  from {
    opacity: 0;
    transform: translateY(12px) scale(0.98);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@media (max-width: 980px) {
  .topbar {
    grid-template-columns: 1fr;
    align-items: flex-start;
  }

  .top-actions {
    justify-content: flex-start;
  }

  .global-search {
    max-width: none;
  }

  .stats-row,
  .admin-grid,
  .operations-row,
  .day-timeline-panel {
    grid-template-columns: 1fr;
  }

  .emergency-body {
    grid-template-columns: 1fr;
    height: auto;
    overflow: auto;
  }

  .emergency-modal {
    height: min(86vh, 900px);
    max-height: 86vh;
  }

  .emergency-map {
    min-height: 360px;
  }

}

@media (max-width: 640px) {
  .dashboard {
    padding: 14px;
  }

  .topbar {
    align-items: flex-start;
    flex-direction: column;
    padding: 14px;
  }

  .top-actions,
  .top-action-buttons,
  .global-search {
    width: 100%;
  }

  .top-action-buttons {
    justify-content: flex-start;
  }

  .control-actions {
    width: 100%;
    padding-top: 8px;
    padding-left: 0;
    border-top: 1px solid var(--color-border);
    border-left: 0;
  }

  .control-actions button {
    flex: 1 1 120px;
    min-width: 0;
    white-space: normal;
  }

  h1 {
    font-size: 1.65rem;
  }

  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-grid {
    grid-template-columns: 1fr;
  }

  .modal-backdrop {
    align-items: flex-start;
    padding: 10px;
  }

  .modal {
    padding: 18px;
    border-radius: 14px;
  }

  .medication-task-modal {
    padding: 0;
    border-radius: 20px;
  }

  .medication-task-header {
    padding: 18px;
  }

  .medication-title-group {
    align-items: flex-start;
  }

  .medication-task-content {
    padding: 16px;
  }

  .medication-form-card {
    padding: 15px;
    border-radius: 18px;
  }

  .medication-field-grid {
    grid-template-columns: 1fr;
  }

  .medication-task-actions {
    justify-content: stretch;
  }

  .modal-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .modal-actions button,
  .children-search {
    width: 100%;
  }

  .emergency-modal {
    width: 100%;
    height: calc(100vh - 20px);
    max-height: calc(100vh - 20px);
    border-radius: 14px;
  }

  .emergency-body {
    padding: 10px;
  }

  .emergency-poi-panel {
    max-height: 36vh;
  }
}

@media (max-width: 480px) {
  .dashboard {
    padding: 10px;
  }

  .topbar,
  .hero-strip,
  .children-panel,
  .day-timeline-panel,
  .holiday-panel {
    border-radius: 12px;
    padding: 12px;
  }

  .topbar {
    gap: 10px;
    margin-bottom: 8px;
  }

  h1 {
    font-size: 1.4rem;
  }

  .hero-strip {
    margin-top: 8px;
  }

  .hero-strip h2 {
    font-size: 1.08rem;
  }

  .hero-strip p:not(.eyebrow) {
    font-size: 0.84rem;
    line-height: 1.4;
  }

  .children-panel {
    margin-top: 10px;
  }

  .stats-row,
  .care-highlights-panel,
  .operations-row,
  .admin-grid {
    margin-top: 10px;
  }

  .stats-row {
    gap: 8px;
  }

  .stats-row article {
    min-height: 64px;
    padding: 10px;
  }

  .stats-row span {
    font-size: 1.2rem;
  }

  .operations-row,
  .day-timeline-panel,
  .task-stack,
  .control-stack {
    gap: 10px;
  }

  .timeline-section {
    padding: 10px;
  }

  .mini-timeline {
    gap: 6px;
    margin-top: 8px;
  }

  .top-actions button {
    min-height: 40px;
    padding: 9px 11px;
    border-radius: 11px;
  }

  .modal-backdrop {
    padding: 8px;
  }

  .modal {
    max-height: calc(100dvh - 16px);
    padding: 14px;
  }

  .medication-task-modal {
    border-radius: 18px;
  }

  .medication-task-header {
    padding: 14px;
  }

  .medication-icon {
    width: 42px;
    height: 42px;
    border-radius: 14px;
  }

  .medication-icon :deep(.care-icon) {
    width: 22px;
    height: 22px;
  }

  .medication-task-header h2 {
    font-size: 1.22rem;
  }

  .modal-intro {
    margin-top: 4px;
    font-size: 0.84rem;
    line-height: 1.35;
  }

  .medication-close-button {
    min-height: 38px;
    padding: 8px 11px;
    font-size: 0.82rem;
  }

  .medication-task-content {
    gap: 10px;
    padding: 12px;
  }

  .medication-form-card {
    padding: 12px;
    border-radius: 15px;
  }

  .section-heading {
    margin-bottom: 10px;
  }

  .form-field small,
  .medication-safety-note span {
    font-size: 0.74rem;
  }

  .medication-safety-note {
    padding: 10px 12px;
    border-radius: 14px;
  }

  .primary-medication-button,
  .medication-cancel-button {
    min-height: 42px;
    padding: 10px 14px;
  }

  .emergency-modal {
    height: calc(100dvh - 16px);
    max-height: calc(100dvh - 16px);
  }

  .emergency-map {
    min-height: 240px;
  }
}

@media (max-width: 420px) {
  .stats-row {
    grid-template-columns: 1fr;
  }

  .top-actions button {
    width: 100%;
    white-space: normal;
  }

  .top-action-buttons,
  .control-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .medication-task-header {
    gap: 14px;
  }

  .medication-title-group {
    display: grid;
    gap: 10px;
  }

  .medication-close-button {
    padding-inline: 12px;
  }

  .holiday-list li,
  .timeline-section header {
    align-items: flex-start;
    flex-direction: column;
  }

  .holiday-name {
    text-align: left;
  }

  .emergency-map {
    min-height: 280px;
  }
}
</style>
