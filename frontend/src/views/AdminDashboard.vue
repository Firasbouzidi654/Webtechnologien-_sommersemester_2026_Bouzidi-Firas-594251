<template>
  <main class="dashboard admin-dashboard">
    <nav class="topbar" :class="{ 'panel-dark': isDark }">
      <div>
        <p class="eyebrow">Admin control center</p>
        <h1>Medication tasks today</h1>
      </div>
      <div class="top-actions">
        <div class="top-action-buttons">
          <NotificationCenter />
          <button class="primary-action" type="button" @click="openTaskModal('add')">Add medication</button>
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
        <h2>Clear schedule, quick confirmations, safer handovers</h2>
        <p>Staff can scan the plan, confirm medication, see missed alerts, and check the daily calendar without leaving the dashboard.</p>
      </div>
    </section>
    <section v-if="taskModalActive" class="modal-backdrop" @click.self="closeTaskModal">
      <form class="modal" @submit.prevent="saveTask">
        <header>
          <div>
            <p class="eyebrow">Medication task</p>
            <h2>{{ taskModalMode === 'edit' ? 'Edit medication task' : 'Add medication task' }}</h2>
          </div>
          <button type="button" aria-label="Close" @click="closeTaskModal">x</button>
        </header>

        <div class="modal-fields">
          <label>
            <span>Child name</span>
            <select v-model.number="taskForm.childId" required>
              <option v-for="child in children" :key="child.id" :value="child.id">{{ child.name }} — {{ child.groupName }}</option>
            </select>
          </label>
          <label>
            <span>Medication</span>
            <input v-model="taskForm.medicationName" placeholder="E.g. Amoxicillin" required />
          </label>
          <label>
            <span>Dosage</span>
            <input v-model="taskForm.dosage" placeholder="E.g. 5 ml" required />
          </label>
          <label>
            <span>Date</span>
            <input v-model="taskForm.date" type="date" required />
          </label>
          <label>
            <span>Hour</span>
            <input v-model="taskForm.time" type="time" required />
          </label>
          <label>
            <span>Status</span>
            <select v-model="taskForm.status" required>
              <option v-for="status in statusOptions" :key="status" :value="status">{{ status }}</option>
            </select>
          </label>
          <label>
            <span>Notes</span>
            <textarea v-model="taskForm.instructions" placeholder="Take with food or water" rows="3"></textarea>
          </label>
          <p v-if="taskError" class="form-error">{{ taskError }}</p>
          <div class="modal-actions">
            <button type="submit">{{ taskModalMode === 'edit' ? 'Save changes' : 'Create task' }}</button>
            <button type="button" class="secondary-button" @click="closeTaskModal">Cancel</button>
          </div>
        </div>
      </form>
    </section>

    <!-- Emergency modal -->
    <section v-if="emergencyActive" class="modal-backdrop" @click.self="closeEmergency" role="dialog" aria-modal="true">
      <div class="modal emergency-modal">
        <header class="modal-header">
          <div>
            <p class="eyebrow">Emergency mode</p>
            <h2>Child safety instant response</h2>
            <p class="modal-subtitle">Review medical context, locate nearby support, and contact help quickly.</p>
          </div>
          <button class="modal-close" type="button" aria-label="Close emergency dialog" @click="closeEmergency">x</button>
        </header>

        <div class="emergency-body">
          <div class="emergency-details">
            <label class="field-label">
              <span>Active child</span>
              <select v-model.number="selectedEmergencyChildId" aria-label="Select child for emergency response">
                <option v-for="c in children" :key="c.id" :value="c.id">{{ c.name }} — {{ c.groupName }}</option>
              </select>
            </label>

            <template v-if="selectedEmergencyChild">
              <section class="child-summary" aria-label="Selected child summary">
                <span class="child-photo placeholder">{{ initials(selectedEmergencyChild.name) }}</span>
                <div class="child-summary-content">
                  <div class="child-summary-heading">
                    <h3>{{ selectedEmergencyChild.name || 'Unknown child' }}</h3>
                    <p>{{ selectedEmergencyChild.groupName || 'No group assigned' }}</p>
                  </div>
                  <dl class="child-medical-grid">
                    <div>
                      <dt>Allergies</dt>
                      <dd>{{ meaningfulList(selectedEmergencyChild.allergies, 'None recorded') }}</dd>
                    </div>
                    <div>
                      <dt>Chronic conditions</dt>
                      <dd>{{ meaningfulList(selectedEmergencyChild.chronicDiseases, 'None recorded') }}</dd>
                    </div>
                    <div>
                      <dt>Location</dt>
                      <dd>{{ selectedEmergencyLocation.lat }}, {{ selectedEmergencyLocation.lng }}</dd>
                    </div>
                  </dl>
                </div>
              </section>

              <section class="support-grid" aria-label="Emergency support shortcuts">
                <article
                  v-for="card in emergencySupportCards"
                  :key="card.key"
                  class="support-card"
                  :class="card.key"
                >
                  <img v-if="card.image" class="support-photo" :src="card.image" :alt="card.title" loading="lazy" />
                  <span v-else class="support-icon">{{ card.icon }}</span>
                  <div class="support-card-copy">
                    <strong>{{ card.title }}</strong>
                    <span>{{ card.name }}</span>
                    <div class="support-metrics">
                      <small>{{ card.distance }}</small>
                      <small>{{ card.eta }}</small>
                    </div>
                  </div>
                  <button type="button" @click="card.action">{{ card.actionLabel }}</button>
                </article>
              </section>

              <div class="map-card">
                <div class="map-card-header">
                  <div>
                    <p class="eyebrow">Live map</p>
                    <h3>Nearby route context</h3>
                  </div>
                  <span>{{ nearbyPOIs.length }} result(s)</span>
                </div>

                <div id="emergency-map" class="emergency-map" aria-label="Emergency location map">
                <template v-if="emergencyMapError">
                  <div class="map-fallback">
                    <p>{{ emergencyMapError }}</p>
                    <p><strong>Coordinates:</strong> {{ selectedEmergencyLocation.lat }}, {{ selectedEmergencyLocation.lng }}</p>
                  </div>
                </template>
                <template v-else>
                  Map loading…
                </template>
                </div>
              </div>

              <div class="emergency-actions">
                <button type="button" class="secondary-button" @click="callServices">Call services</button>
              </div>
            </template>
          </div>

          <aside class="emergency-poi-panel">
            <div class="panel-header">
              <div>
                <p class="eyebrow">Nearby emergency support</p>
                <h3>Fast response options</h3>
              </div>
              <p class="panel-note">Route directly from the child's location.</p>
            </div>

            <div class="poi-list">
              <div v-if="poiLoading" class="loading-state">
                <span class="spinner" aria-hidden="true"></span>
                <div>
                  <strong>Searching nearby support</strong>
                  <p>Looking for hospitals, pharmacies, and police stations...</p>
                </div>
              </div>
              <div v-else-if="nearbyPOIs.length === 0" class="empty-state">No nearby emergency points found. Try again in a moment.</div>
              <EmergencyPoiCard
                v-for="poi in displayedEmergencyPOIs"
                :key="poi.id"
                :poi="poi"
                :from="selectedEmergencyLocation"
              />
            </div>
          </aside>
        </div>
      </div>
    </section>

    <section class="stats-row">
      <article class="pending" :class="{ 'panel-dark': isDark }">
        <span>{{ stats.Pending }}</span>
        <p>Pending medications</p>
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
        <p>Upcoming</p>
      </article>
    </section>

    <section class="operations-row">
      <article class="progress-panel">
        <div>
          <p class="eyebrow">Daily medication progress</p>
          <h2>{{ medicationProgress.completed }} of {{ medicationProgress.total }} completed</h2>
        </div>
        <div class="progress-track" aria-label="Medication completion progress">
          <span :style="{ width: `${medicationProgress.percent}%` }"></span>
        </div>
        <div class="progress-metrics">
          <span class="taken">{{ medicationProgress.completed }} taken</span>
          <span class="pending">{{ medicationProgress.pending }} pending/upcoming</span>
          <span class="missed">{{ medicationProgress.missed }} missed</span>
        </div>
      </article>

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
      </aside>
    </section>
  </main>
</template>

<script>
import AdminCalendar from '../components/AdminCalendar.vue';
import EmergencyPoiCard from '../components/EmergencyPoiCard.vue';
import MedicationAssistant from '../components/MedicationAssistant.vue';
import MedicationTaskCard from '../components/MedicationTaskCard.vue';
import NotificationCenter from '../components/NotificationCenter.vue';
import L from 'leaflet';
import { MEDICATION_STATUSES, addNotification, kindercareStore, markMedicationTaken, setMedicationStatus, taskReminderDue, addMedication, editMedication, removeMedication, loadChildren, loadMedicationTasks } from '../state/kindercareStore';
import { buildEmergencyRouteLink, fetchNearbyEmergencyPOIs } from '../services/emergencyService';
import { estimateDriveTimeMinutes, formatDistanceMeters } from '../utils/formatters';
import { getGermanPublicHolidays } from '../services/holidayService.js';

const PHARMACY_IMAGE = 'https://images.unsplash.com/photo-1766258630872-2b1403439fb5?auto=format&fit=crop&q=80&w=300';
const POLICE_IMAGE = 'https://images.unsplash.com/photo-1693329900318-9686ec84b1cd?auto=format&fit=crop&q=80&w=300';
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
    EmergencyPoiCard,
    MedicationAssistant,
    MedicationTaskCard,
    NotificationCenter
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
      emergencyMarkers: [],
      emergencyMapError: '',
      taskModalActive: false,
      taskModalMode: 'add',
      taskForm: {
        medicationId: null,
        childId: null,
        medicationName: '',
        dosage: '',
        date: this.todayDateKey(),
        time: '',
        instructions: '',
        status: 'Pending'
      },
      statusOptions: MEDICATION_STATUSES,
      taskError: '',
      holidays: [],
      syncInterval: null
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
    // Parents add children from a separate session, so poll the backend
    // periodically to pick up new children/medications without a page reload.
    this.syncInterval = window.setInterval(() => {
      loadChildren();
      loadMedicationTasks();
    }, 5000);
  },
  beforeUnmount() {
    window.clearInterval(this.syncInterval);
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
    emergencySupportCards() {
      const hospital = this.supportPoiByType('hospital');
      const pharmacy = this.supportPoiByType('pharmacy');
      const police = this.supportPoiByType('police');

      return [
        {
          key: 'hospital',
          icon: 'H',
          title: 'Hospital',
          name: hospital?.name || 'Nearest hospital',
          distance: this.supportDistance(hospital),
          eta: this.supportEta(hospital),
          actionLabel: 'Show route',
          action: () => this.routeToPoiType('hospital')
        },
        {
          key: 'pharmacy',
          icon: 'Rx',
          title: 'Pharmacy',
          name: pharmacy?.name || 'Nearest pharmacy',
          distance: this.supportDistance(pharmacy),
          eta: this.supportEta(pharmacy),
          image: PHARMACY_IMAGE,
          actionLabel: 'Show route',
          action: () => this.routeToPoiType('pharmacy')
        },
        {
          key: 'police',
          icon: 'Police',
          title: 'Police Station',
          name: police?.name || 'Nearest police station',
          distance: this.supportDistance(police),
          eta: this.supportEta(police),
          image: POLICE_IMAGE,
          actionLabel: 'Show route',
          action: () => this.routeToPoiType('police')
        }
      ];
    },
    displayedEmergencyPOIs() {
      if (this.poiLoading) {
        return [];
      }

      const pois = Array.isArray(this.nearbyPOIs) ? [...this.nearbyPOIs] : [];

      if (!pois.some((poi) => poi.type === 'police')) {
        pois.push(this.withDistance(FALLBACK_POLICE_STATION));
      }

      return pois;
    },
    children() {
      return kindercareStore.children;
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
      return this.tasks;
    },
    medicationProgress() {
      const total = this.filteredTasks.length;
      const completed = this.filteredTasks.filter((task) => task.status === 'Taken').length;
      const pending = this.filteredTasks.filter((task) => ['Pending', 'Upcoming'].includes(task.status)).length;
      const missed = this.filteredTasks.filter((task) => task.status === 'Missed').length;
      const percent = total ? Math.round((completed / total) * 100) : 0;

      return {
        total,
        completed,
        pending,
        missed,
        percent
      };
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
      return this.tasks.reduce((counts, task) => {
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
    initials(name) {
      return (name || '?')
        .split(' ')
        .map((part) => part[0])
        .join('')
        .slice(0, 2)
        .toUpperCase();
    },
    nearestPoiByType(type) {
      return this.displayedEmergencyPOIs.find((poi) => poi.type === type) || null;
    },
    supportPoiByType(type) {
      if (this.poiLoading) {
        return null;
      }

      return this.nearestPoiByType(type);
    },
    supportDistance(poi) {
      if (this.poiLoading) {
        return 'Searching...';
      }

      return Number.isFinite(poi?.distance) ? formatDistanceMeters(poi.distance) : 'Route pending';
    },
    supportEta(poi) {
      if (this.poiLoading) {
        return 'ETA pending';
      }

      return Number.isFinite(poi?.distance) ? estimateDriveTimeMinutes(poi.distance) : 'ETA pending';
    },
    withDistance(poi) {
      return {
        ...poi,
        distance: Math.round(this.distanceBetweenMeters(this.selectedEmergencyLocation, poi)),
        icon: 'Police',
        label: 'Police station'
      };
    },
    distanceBetweenMeters(from, to) {
      if (!from || !to) {
        return 0;
      }

      const toRad = (value) => (value * Math.PI) / 180;
      const radius = 6371000;
      const dLat = toRad(to.lat - from.lat);
      const dLng = toRad(to.lng - from.lng);
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(toRad(from.lat)) * Math.cos(toRad(to.lat)) *
        Math.sin(dLng / 2) * Math.sin(dLng / 2);

      return radius * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    },
    routeToPoiType(type) {
      const poi = this.nearestPoiByType(type);

      if (!poi) {
        window.alert(`No ${type} route is available yet.`);
        return;
      }

      window.open(buildEmergencyRouteLink(this.selectedEmergencyLocation, poi), '_blank', 'noreferrer');
    },
    async confirmMedication(medicationId) {
      await markMedicationTaken(medicationId);
    },
    openEmergency() {
      this.selectedEmergencyChildId = this.children?.[0]?.id || null;
      this.emergencyActive = true;
      addNotification({
        title: 'Emergency mode activation',
        message: 'Staff opened Emergency Mode from the admin dashboard.',
        type: 'danger'
      });
      this.$nextTick(() => this.initEmergencyModal());
    },
    async initEmergencyModal() {
      if (!this.selectedEmergencyChild) {
        this.emergencyMapError = 'No child location available.';
        return;
      }

      const { lat, lng } = this.selectedEmergencyLocation;
      const mapEl = document.getElementById('emergency-map');
      if (!mapEl) {
        return;
      }

      this.emergencyMapError = '';

      try {
        if (!this.emergencyMap) {
          this.emergencyMap = L.map(mapEl, { scrollWheelZoom: false }).setView([lat, lng], 13);
          const tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
          });
          tileLayer.on('tileerror', () => {
            this.emergencyMapError = 'Map tiles failed to load. Emergency POIs are still listed below.';
          });
          tileLayer.addTo(this.emergencyMap);
        } else {
          this.emergencyMap.setView([lat, lng], 13);
        }

        this.clearEmergencyMarkers();
        this.addEmergencyMarker([lat, lng], `${this.selectedEmergencyChild.name || 'Child'} location`);
        await this.loadNearbyPois(lat, lng);
      } catch (e) {
        console.warn('Leaflet map init error', e);
      }
    },
    clearEmergencyMarkers() {
      if (!this.emergencyMap) {
        return;
      }

      this.emergencyMarkers.forEach((marker) => {
        this.emergencyMap.removeLayer(marker);
      });
      this.emergencyMarkers = [];
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
            instructions: task.instructions || '',
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
          instructions: '',
          status: 'Pending'
        };
      }

      this.taskModalActive = true;
    },
    closeTaskModal() {
      this.taskModalActive = false;
      this.taskError = '';
    },
    async saveTask() {
      if (!this.taskForm.childId || !this.taskForm.medicationName || !this.taskForm.dosage || !this.taskForm.date || !this.taskForm.time) {
        this.taskError = 'Please complete all required fields.';
        return;
      }

      this.closeTaskModal();

      if (this.taskModalMode === 'edit' && this.taskForm.medicationId) {
        await editMedication(this.taskForm.childId, this.taskForm.medicationId, {
          name: this.taskForm.medicationName,
          dosage: this.taskForm.dosage,
          instructions: this.taskForm.instructions,
          date: this.taskForm.date,
          time: this.taskForm.time,
          status: this.taskForm.status,
          childId: this.taskForm.childId
        });
      } else {
        await addMedication(this.taskForm.childId, {
          name: this.taskForm.medicationName,
          dosage: this.taskForm.dosage,
          instructions: this.taskForm.instructions,
          date: this.taskForm.date,
          time: this.taskForm.time,
          status: this.taskForm.status
        });
      }
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
    addEmergencyMarker(position, label) {
      if (!this.emergencyMap) {
        return;
      }

      const marker = L.marker(position).addTo(this.emergencyMap).bindPopup(label);
      this.emergencyMarkers.push(marker);
      return marker;
    },
    async loadNearbyPois(lat, lng) {
      this.poiLoading = true;
      this.nearbyPOIs = [];

      const pois = await fetchNearbyEmergencyPOIs(lat, lng);
      this.nearbyPOIs = Array.isArray(pois) ? pois : [];

      if (this.emergencyMap && this.nearbyPOIs.length) {
        this.nearbyPOIs.forEach((poi) => {
          const marker = L.marker([poi.lat, poi.lng]).addTo(this.emergencyMap).bindPopup(`${poi.name} — ${poi.type} — ${poi.distance} m`);
          this.emergencyMarkers.push(marker);
        });
      }

      this.poiLoading = false;
    },
    closeEmergency() {
      this.emergencyActive = false;
      this.emergencyMapError = '';
    },
    callServices() {
      window.open('tel:112');
    },
    async changeTaskStatus({ medicationId, status }) {
      if (!medicationId || !this.statusOptions.includes(status)) {
        return;
      }

      await setMedicationStatus(medicationId, status);
    },
    meaningfulList(items, fallback) {
      const values = (items || []).filter((item) => item && !['None', 'None known'].includes(item));
      return values.length ? values.join(', ') : fallback;
    },
  }
};
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: 24px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
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
  grid-template-columns: minmax(210px, auto) minmax(0, 1fr);
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

.emergency-modal {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  width: min(1120px, calc(100vw - 32px));
  max-height: min(92vh, 880px);
  overflow: hidden;
  padding: 0;
  border-radius: 18px;
  animation: modal-rise 0.22s ease;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.emergency-modal .modal-header {
  position: sticky;
  top: 0;
  z-index: 2;
  margin-bottom: 0;
  padding: 22px 24px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
}

.emergency-modal .modal-header h2 {
  margin-top: 4px;
  color: var(--color-text-primary);
  font-size: clamp(1.35rem, 2.4vw, 2rem);
  line-height: 1.15;
}

.modal-subtitle {
  max-width: 560px;
  margin-top: 6px;
  color: var(--color-text-secondary);
  font-weight: 500;
  line-height: 1.5;
}

.modal-close {
  display: grid;
  flex: 0 0 40px;
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

.emergency-body {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(320px, 0.75fr);
  gap: 20px;
  min-height: 0;
  overflow: auto;
  padding: 22px 24px 24px;
  background: linear-gradient(180deg, var(--color-bg-primary), var(--color-bg-secondary));
}

.emergency-details,
.emergency-poi-panel {
  min-width: 0;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
}

.emergency-details {
  display: grid;
  align-content: start;
  gap: 16px;
  padding: 18px;
}

.field-label {
  display: grid;
  gap: 8px;
  font-weight: 800;
  color: var(--color-text-primary);
}

.field-label span {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.field-label select {
  min-height: 46px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

.child-summary {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.emergency-modal .child-summary {
  grid-template-columns: 72px minmax(0, 1fr);
  align-items: start;
  gap: 14px;
  margin-bottom: 0;
  border: 1px solid rgba(229, 62, 62, 0.16);
  border-radius: 16px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(229, 62, 62, 0.08), rgba(49, 130, 206, 0.08));
}

.child-summary img.child-photo {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  object-fit: cover;
  border: 1px solid var(--color-border);
}

.emergency-modal .child-photo {
  width: 72px;
  height: 72px;
  border-radius: 16px;
}

.child-photo.placeholder {
  display: grid;
  place-items: center;
  background: var(--color-missed);
  color: var(--color-missed-text);
  font-weight: 900;
}

.child-summary-content {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.child-summary-heading h3,
.map-card-header h3,
.panel-header h3 {
  margin: 0;
  color: var(--color-text-primary);
}

.child-summary-heading p {
  margin-top: 3px;
  color: var(--color-text-secondary);
  font-weight: 700;
}

.child-medical-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 0;
}

.child-medical-grid div {
  min-width: 0;
  border-radius: 14px;
  padding: 12px 14px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.03);
}

.child-medical-grid dt {
  color: var(--color-text-tertiary);
  font-size: 0.72rem;
  font-weight: 900;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.child-medical-grid dd {
  margin: 4px 0 0;
  color: var(--color-text-primary);
  font-weight: 800;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.support-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.support-card {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  grid-template-rows: auto auto;
  gap: 12px;
  align-items: start;
  min-height: 150px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 14px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  text-align: left;
  box-shadow: var(--shadow-sm);
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease, background 0.22s ease;
}

.support-card:hover {
  transform: translateY(-4px);
  border-color: rgba(49, 130, 206, 0.25);
  box-shadow: var(--shadow-lg);
}

.support-icon {
  display: grid;
  width: 48px;
  height: 48px;
  place-items: center;
  border-radius: 14px;
  font-size: 0.78rem;
  font-weight: 900;
}

.support-photo {
  display: block;
  width: 48px;
  height: 48px;
  border-radius: 14px;
  object-fit: cover;
  box-shadow: 0 8px 16px rgba(15, 23, 42, 0.14);
  transition: transform 0.25s ease, filter 0.25s ease;
}

.support-card:hover .support-photo {
  transform: scale(1.08);
  filter: brightness(1.05) saturate(1.08);
}

.support-card strong,
.support-card small,
.support-card-copy span {
  display: block;
  overflow-wrap: anywhere;
}

.support-card-copy {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.support-card-copy > span {
  color: var(--color-text-primary);
  font-size: 0.92rem;
  font-weight: 800;
  line-height: 1.3;
}

.support-card small {
  color: var(--color-text-secondary);
  font-size: 0.76rem;
  font-weight: 800;
  line-height: 1.3;
}

.support-metrics {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.support-metrics small {
  border: 1px solid var(--color-border-light);
  border-radius: 999px;
  padding: 5px 8px;
  background: var(--color-bg-secondary);
}

.support-card > button {
  grid-column: 1 / -1;
  min-height: 40px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-brand), #256db0);
  color: #fff;
  cursor: pointer;
  font-weight: 900;
  box-shadow: 0 10px 18px rgba(49, 130, 206, 0.18);
}

.support-card > button:hover {
  transform: translateY(-2px);
  box-shadow: 0 14px 24px rgba(49, 130, 206, 0.24);
}

.support-card.hospital .support-icon {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.support-card.pharmacy .support-icon {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.support-card.police .support-icon {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.support-card.police {
  border-color: color-mix(in srgb, var(--color-upcoming-border) 35%, var(--color-border));
}

.support-card.hospital {
  border-color: color-mix(in srgb, var(--color-missed-border) 35%, var(--color-border));
}

.support-card.pharmacy {
  border-color: color-mix(in srgb, var(--color-taken-border) 35%, var(--color-border));
}

.support-card.contacts {
  border-color: color-mix(in srgb, var(--color-pending-border) 35%, var(--color-border));
}

.support-card.contacts .support-icon {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.map-card {
  display: grid;
  gap: 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 16px;
  background: var(--color-bg-primary);
}

.map-card-header,
.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.map-card-header span {
  border-radius: 999px;
  padding: 6px 10px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-size: 0.76rem;
  font-weight: 900;
  white-space: nowrap;
}

.child-summary h3 {
  margin-bottom: 10px;
  font-size: 1.2rem;
}

.summary-line {
  margin: 6px 0;
  color: var(--color-text-secondary);
  line-height: 1.5;
}

.emergency-map {
  min-height: 280px;
  border-radius: 18px;
  overflow: hidden;
  background: var(--color-bg-tertiary);
  margin-top: 18px;
}

.emergency-modal .emergency-map {
  display: grid;
  min-height: clamp(230px, 32vh, 360px);
  place-items: center;
  margin-top: 0;
  border: 1px solid var(--color-border);
  color: var(--color-text-secondary);
  font-weight: 800;
}

.map-fallback {
  padding: 24px;
  text-align: center;
  color: var(--color-text-secondary);
}

.map-fallback p {
  margin: 0 0 12px;
}

.emergency-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 18px;
}

.emergency-modal .emergency-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 0;
}

.emergency-actions .secondary-button {
  min-width: 160px;
  background: rgba(45, 143, 123, 0.12);
  color: var(--color-text-primary);
}

.emergency-modal .emergency-actions .secondary-button {
  min-width: 0;
  min-height: 44px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-bg-primary);
  font-weight: 900;
}

.emergency-poi-panel {
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  gap: 16px;
  padding: 20px;
}

.panel-note {
  max-width: 190px;
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  font-weight: 700;
  line-height: 1.35;
  text-align: right;
}

.poi-list {
  display: grid;
  align-content: start;
  gap: 14px;
  min-height: 0;
  overflow: auto;
  padding-right: 4px;
}

.loading-state {
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 16px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

.loading-state p {
  margin-top: 3px;
  color: var(--color-text-secondary);
  font-size: 0.88rem;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 4px solid var(--color-bg-tertiary);
  border-top-color: var(--color-brand);
  border-radius: 50%;
  animation: spin 0.9s linear infinite;
}

.emergency-actions .secondary-button:hover {
  opacity: 0.92;
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

.emergency-map {
  min-height: 280px;
  border-radius: 18px;
  overflow: hidden;
  background: var(--color-bg-tertiary);
  margin-top: 18px;
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

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-top: 16px;
}

.stats-row article {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #eef6ff 100%);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 96px;
  padding: 16px;
  text-align: center;
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
  font-size: 2rem;
  font-weight: 800;
  margin-bottom: 6px;
}

.stats-row p {
  color: var(--color-text-secondary);
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

.progress-panel,
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

.progress-panel {
  display: grid;
  gap: 14px;
}

.progress-panel h2,
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

.progress-metrics {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.progress-metrics span {
  border-radius: 999px;
  padding: 7px 10px;
  font-size: 0.78rem;
  font-weight: 900;
}

.progress-metrics .taken {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.progress-metrics .pending {
  background: var(--color-pending);
  color: var(--color-pending-text);
}

.progress-metrics .missed {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.day-timeline-panel {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
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
  grid-template-columns: minmax(0, 1fr) minmax(320px, 380px);
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
:global([data-theme="dark"]) .admin-dashboard .progress-panel,
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
:global([data-theme="dark"]) .admin-dashboard .mini-timeline > span,
:global([data-theme="dark"]) .admin-dashboard .empty-state {
  border-color: rgba(255, 255, 255, 0.06);
  background: rgba(255, 255, 255, 0.06);
  color: #f8fafc;
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

  .emergency-modal {
    max-height: calc(100vh - 24px);
  }

  .emergency-body {
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .emergency-poi-panel {
    min-height: auto;
  }

  .poi-list {
    max-height: none;
    overflow: visible;
    padding-right: 0;
  }

}

@media (max-width: 640px) {
  .dashboard {
    padding: 20px;
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

  .emergency-modal {
    width: 100%;
    max-height: calc(100vh - 20px);
    border-radius: 14px;
  }

  .emergency-modal .modal-header {
    padding: 18px;
  }

  .emergency-body {
    padding: 16px;
  }

  .emergency-modal .child-summary,
  .child-medical-grid,
  .support-grid,
  .emergency-modal .emergency-actions,
  .map-card-header,
  .panel-header {
    grid-template-columns: 1fr;
  }

  .emergency-modal .child-summary {
    display: grid;
  }

  .support-card {
    min-height: 70px;
  }

  .support-card > button {
    width: 100%;
  }

  .panel-note {
    max-width: none;
    text-align: left;
  }

  .map-card-header,
  .panel-header {
    flex-direction: column;
  }
}
</style>
