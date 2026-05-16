<template>
  <main class="dashboard admin-dashboard">
    <nav class="topbar">
      <div>
        <p class="eyebrow">Admin control center</p>
        <h1>Medication tasks today</h1>
      </div>
      <div class="top-actions">
        <button class="emergency" type="button" @click="openEmergency">Emergency mode</button>
        <button type="button" @click="openTaskModal('add')">Add medication</button>
        <button type="button" @click="$emit('navigate', '/')">Log out</button>
      </div>
    </nav>

    <section class="hero-strip">
      <div>
        <p class="eyebrow">Live care operations</p>
        <h2>Clear schedule, quick confirmations, safer handovers</h2>
        <p>Staff can scan the plan, confirm medication, see missed alerts, and check the daily calendar without leaving the dashboard.</p>
      </div>
    </section>
    <!-- Verification modal -->
    <section v-if="verificationActive" class="modal-backdrop" @click.self="verificationActive = false">
      <form class="modal" @submit.prevent="verifyMedication">
        <header>
          <h2>Confirm medication — verification</h2>
          <button type="button" aria-label="Close" @click="verificationActive = false">x</button>
        </header>
        <div>
          <p v-if="verificationTask"><strong>{{ verificationTask.childName }} — {{ verificationTask.medicationName }}</strong></p>
          <label>
            <span>Enter Medication ID or scan (mock)</span>
            <input v-model="verificationInput" placeholder="MED-001 or scan result" />
          </label>
          <div style="display:flex;gap:10px;margin-top:10px;align-items:center;flex-wrap:wrap;">
            <button type="submit" :disabled="verificationLoading">Verify</button>
            <button type="button" @click="verificationInput = verificationTask?.medicationId">Fill ID</button>
            <div v-if="verificationLoading">Checking…</div>
          </div>
          <p v-if="verificationMessage" :class="{ 'success': verificationState === 'success', 'error': verificationState === 'error' }">{{ verificationMessage }}</p>
        </div>
      </form>
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
            <span>Child</span>
            <select v-model.number="taskForm.childId" :disabled="taskModalMode === 'edit'" required>
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
            <span>Time</span>
            <input v-model="taskForm.time" type="time" required />
          </label>
          <label>
            <span>Instructions</span>
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
          </div>
          <button type="button" aria-label="Close emergency dialog" @click="closeEmergency">x</button>
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
                <img v-if="selectedEmergencyChild.photo" :src="selectedEmergencyChild.photo" class="child-photo" alt="Selected child photo" />
                <div>
                  <h3>{{ selectedEmergencyChild.name }}</h3>
                  <p class="summary-line"><strong>Class:</strong> {{ selectedEmergencyChild.groupName }}</p>
                  <p class="summary-line"><strong>Allergies:</strong> {{ selectedEmergencyChild.allergies.join(', ') || 'None' }}</p>
                  <p class="summary-line"><strong>Chronic:</strong> {{ selectedEmergencyChild.chronicDiseases.join(', ') || 'None' }}</p>
                  <p class="summary-line"><strong>Parent contact:</strong> {{ selectedEmergencyChild.emergencyContacts[0]?.name || 'No parent' }} - {{ selectedEmergencyChild.emergencyContacts[0]?.phone || 'N/A' }}</p>
                </div>
              </section>

              <div id="emergency-map" class="emergency-map" aria-label="Emergency location map">
                <template v-if="emergencyMapError">
                  <div class="map-fallback">
                    <p>{{ emergencyMapError }}</p>
                    <p><strong>Coordinates:</strong> {{ selectedEmergencyChild.location.lat }}, {{ selectedEmergencyChild.location.lng }}</p>
                  </div>
                </template>
                <template v-else>
                  Map loading…
                </template>
              </div>

              <div class="emergency-actions">
                <button type="button" class="secondary-button" @click="callParent">Call parent</button>
                <button type="button" class="secondary-button" @click="callServices">Call services</button>
                <button type="button" class="secondary-button" @click="shareEmergencyInfo">Share info</button>
              </div>
            </template>
          </div>

          <aside class="emergency-poi-panel">
            <div class="panel-header">
              <p class="eyebrow">Nearby emergency support</p>
              <p class="panel-note">Route directly from the child's location.</p>
            </div>

            <div class="poi-list">
              <div v-if="poiLoading" class="loading-state">Searching nearby hospitals, pharmacies, and police...</div>
              <div v-else-if="nearbyPOIs.length === 0" class="empty-state">No nearby emergency points found. Try again in a moment.</div>
              <EmergencyPoiCard
                v-for="poi in nearbyPOIs"
                :key="poi.id"
                :poi="poi"
                :from="selectedEmergencyChild?.location"
              />
            </div>
          </aside>
        </div>
      </div>
    </section>

    <section class="stats-row">
      <article class="pending">
        <span>{{ stats.Pending }}</span>
        <p>Pending medications</p>
      </article>
      <article class="taken">
        <span>{{ stats.Taken }}</span>
        <p>Taken today</p>
      </article>
      <article class="missed">
        <span>{{ stats.Missed }}</span>
        <p>Missed today</p>
      </article>
      <article class="alerts">
        <strong>{{ missedTasks.length }} missed alert(s)</strong>
        <p>Follow up with emergency contacts if needed.</p>
      </article>
    </section>

    <section class="filters">
      <label>
        <span>Filter by group</span>
        <select v-model="groupFilter">
          <option value="all">All groups</option>
          <option v-for="group in groups" :key="group" :value="group">{{ group }}</option>
        </select>
      </label>
      <label>
        <span>Filter by child</span>
        <select v-model="childFilter">
          <option value="all">All children</option>
          <option v-for="child in children" :key="child.id" :value="child.name">{{ child.name }}</option>
        </select>
      </label>
    </section>

    <section class="alerts-panel">
      <article>
        <p class="eyebrow">Reminder alerts</p>
        <h2>{{ reminderTasks.length ? `${reminderTasks.length} medication time(s) arrived` : 'No medication reminder due now' }}</h2>
        <p v-for="task in reminderTasks" :key="task.taskId">
          {{ task.scheduledTime }} - {{ task.childName }} needs {{ task.medicationName }}
        </p>
      </article>
      <article>
        <p class="eyebrow">Missed medication alerts</p>
        <h2>{{ missedTasks.length ? `${missedTasks.length} missed medication(s)` : 'No missed medication alerts' }}</h2>
        <p v-for="task in missedTasks" :key="task.taskId">
          {{ task.childName }} missed {{ task.medicationName }} at {{ task.scheduledTime }}
        </p>
      </article>
    </section>

    <section class="child-overview">
      <article v-for="child in filteredChildren" :key="child.id">
        <header>
          <div>
            <p class="eyebrow">{{ child.groupName }}</p>
            <h2>{{ child.name }}</h2>
          </div>
          <span>{{ child.medications.length }} med(s)</span>
        </header>
        <dl>
          <div>
            <dt>Allergies</dt>
            <dd>{{ meaningfulList(child.allergies, 'No allergy recorded') }}</dd>
          </div>
          <div>
            <dt>Chronic diseases</dt>
            <dd>{{ meaningfulList(child.chronicDiseases, 'No chronic disease recorded') }}</dd>
          </div>
          <div>
            <dt>Prescription</dt>
            <dd>{{ child.prescriptionFileName || prescriptionLabel(child) }}</dd>
          </div>
          <div>
            <dt>Parent note</dt>
            <dd>{{ parentNotes[child.id] || 'No parent note yet' }}</dd>
          </div>
        </dl>
      </article>
    </section>

    <section class="admin-grid">
      <div class="task-stack">
        <AdminCalendar :tasks="tasks" />

        <MedicationTaskCard
          v-for="task in filteredTasks"
          :key="task.taskId"
          :task="task"
          @confirm="openVerificationModal"
          @edit="openTaskModal('edit', $event)"
          @delete="deleteTask($event)"
          @toggle-status="toggleTaskStatus"
        />
      </div>

      <aside class="control-stack">
        <section class="panel">
          <header>
            <h2>Parent notes</h2>
            <span>Shared mock state</span>
          </header>
          <article v-for="child in childrenWithNotes" :key="child.id" class="note-item">
            <strong>{{ child.name }}</strong>
            <p>{{ parentNotes[child.id] }}</p>
          </article>
          <p v-if="childrenWithNotes.length === 0" class="empty-state">No notes from parents yet.</p>
        </section>

        <section class="panel">
          <header>
            <h2>QR verification</h2>
            <span>Unique medication IDs</span>
          </header>
          <QRMedicationCard
            v-for="task in filteredTasks"
            :key="task.medicationId"
            :medication-id="task.medicationId"
            :medication-name="`${task.childName} - ${task.medicationName}`"
            :qr-payload="task.qrPayload"
          />
        </section>

        <VerificationHistoryPanel :logs="verificationLogs" />

        <section class="panel">
          <header>
            <h2>Emergency contacts</h2>
            <span>Visible to staff</span>
          </header>
          <EmergencyContactCard
            v-for="contact in visibleEmergencyContacts"
            :key="`${contact.childName}-${contact.id}`"
            :contact="contact"
          />
        </section>
      </aside>
    </section>
  </main>
</template>

<script>
import AdminCalendar from '../components/AdminCalendar.vue';
import EmergencyContactCard from '../components/EmergencyContactCard.vue';
import EmergencyPoiCard from '../components/EmergencyPoiCard.vue';
import QRMedicationCard from '../components/QRMedicationCard.vue';
import VerificationHistoryPanel from '../components/VerificationHistoryPanel.vue';
import L from 'leaflet';
import { kindercareStore, markMedicationTaken, setMedicationStatus, taskReminderDue, addVerificationLog, addMedication, editMedication, removeMedication } from '../state/kindercareStore';
import { fetchNearbyEmergencyPOIs } from '../services/emergencyService';

export default {
  name: 'AdminDashboard',
  components: {
    AdminCalendar,
    EmergencyContactCard,
    EmergencyPoiCard,
    QRMedicationCard,
    VerificationHistoryPanel
  },
  emits: ['navigate'],
  data() {
    return {
      groupFilter: 'all',
      childFilter: 'all',
      verificationActive: false,
      verificationInput: '',
      verificationTask: null,
      verificationMessage: '',
      verificationState: '',
      verificationLoading: false,
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
        time: '',
        instructions: ''
      },
      taskError: ''
    };
  },
  created() {
    // noop
  },
  computed: {
    selectedEmergencyChild() {
      return this.children.find((child) => child.id === this.selectedEmergencyChildId) || this.children[0] || null;
    },
    verificationLogs() {
      return kindercareStore.verificationLogs;
    },
    children() {
      return kindercareStore.children;
    },
    tasks() {
      return kindercareStore.medicationTasks.map((task) => ({
        ...task,
        reminderDue: taskReminderDue(task)
      }));
    },
    parentNotes() {
      return kindercareStore.parentNotes;
    },
    groups() {
      return [...new Set(this.children.map((child) => child.groupName))];
    },
    filteredChildren() {
      return this.children.filter((child) => {
        const groupMatches = this.groupFilter === 'all' || child.groupName === this.groupFilter;
        const childMatches = this.childFilter === 'all' || child.name === this.childFilter;
        return groupMatches && childMatches;
      });
    },
    filteredTasks() {
      return this.tasks.filter((task) => {
        const groupMatches = this.groupFilter === 'all' || task.groupName === this.groupFilter;
        const childMatches = this.childFilter === 'all' || task.childName === this.childFilter;
        return groupMatches && childMatches;
      });
    },
    stats() {
      return this.tasks.reduce((counts, task) => {
        counts[task.status] += 1;
        return counts;
      }, { Pending: 0, Taken: 0, Missed: 0 });
    },
    missedTasks() {
      return this.tasks.filter((task) => task.status === 'Missed');
    },
    reminderTasks() {
      return this.tasks.filter((task) => task.reminderDue);
    },
    childrenWithNotes() {
      return this.children.filter((child) => this.parentNotes[child.id]);
    },
    visibleEmergencyContacts() {
      return this.filteredChildren
        .flatMap((child) => child.emergencyContacts.map((contact) => ({
          ...contact,
          childName: child.name,
          name: `${contact.name} (${child.name})`
        })));
    }
  },
  watch: {
    selectedEmergencyChildId() {
      if (this.emergencyActive) {
        this.$nextTick(() => this.initEmergencyModal());
      }
    }
  },
  methods: {
    confirmMedication(medicationId) {
      markMedicationTaken(medicationId);
    },
    openVerificationModal(medicationId) {
      this.verificationTask = this.tasks.find((t) => t.medicationId === medicationId) || null;
      this.verificationInput = '';
      this.verificationMessage = '';
      this.verificationState = '';
      this.verificationLoading = false;
      this.verificationActive = true;
    },
    async verifyMedication() {
      if (!this.verificationTask) return;

      this.verificationLoading = true;
      const input = this.verificationInput.trim();
      const ok = input === this.verificationTask.medicationId || (this.verificationTask.qrPayload && this.verificationTask.qrPayload.includes(input));

      await new Promise((r) => setTimeout(r, 500));

      if (ok) {
        addVerificationLog({ medicationId: this.verificationTask.medicationId, method: input === this.verificationTask.medicationId ? 'ID' : 'QR', admin: 'Staff Demo', time: new Date().toISOString() });
        markMedicationTaken(this.verificationTask.medicationId);
        this.verificationMessage = 'Verification successful — medication confirmed.';
        this.verificationState = 'success';
      } else {
        this.verificationMessage = 'Verification failed — ID or QR did not match.';
        this.verificationState = 'error';
      }

      this.verificationLoading = false;
    },
    openEmergency() {
      this.selectedEmergencyChildId = this.children?.[0]?.id || null;
      this.emergencyActive = true;
      this.$nextTick(() => this.initEmergencyModal());
    },
    async initEmergencyModal() {
      if (!this.selectedEmergencyChild) {
        this.emergencyMapError = 'No child location available.';
        return;
      }

      const { lat = 52.52, lng = 13.405 } = this.selectedEmergencyChild.location || {};
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
        this.addEmergencyMarker([lat, lng], `${this.selectedEmergencyChild.name} location`);
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
    openTaskModal(mode, medicationId = null) {
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
            time: task.scheduledTime,
            instructions: task.instructions || ''
          };
        }
      } else {
        this.taskForm = {
          medicationId: null,
          childId: this.children?.[0]?.id || null,
          medicationName: '',
          dosage: '',
          time: '',
          instructions: ''
        };
      }

      this.taskModalActive = true;
    },
    closeTaskModal() {
      this.taskModalActive = false;
      this.taskError = '';
    },
    saveTask() {
      if (!this.taskForm.childId || !this.taskForm.medicationName || !this.taskForm.dosage || !this.taskForm.time) {
        this.taskError = 'Please complete all required fields.';
        return;
      }

      if (this.taskModalMode === 'edit' && this.taskForm.medicationId) {
        editMedication(this.taskForm.childId, this.taskForm.medicationId, {
          name: this.taskForm.medicationName,
          dosage: this.taskForm.dosage,
          instructions: this.taskForm.instructions,
          time: this.taskForm.time
        });
      } else {
        addMedication(this.taskForm.childId, {
          name: this.taskForm.medicationName,
          dosage: this.taskForm.dosage,
          instructions: this.taskForm.instructions,
          time: this.taskForm.time
        });
      }

      this.closeTaskModal();
    },
    deleteTask(medicationId) {
      const task = this.tasks.find((item) => item.medicationId === medicationId);
      if (!task) {
        return;
      }

      if (!window.confirm('Delete this medication task? This action cannot be undone.')) {
        return;
      }

      removeMedication(task.childId, medicationId);
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
      this.nearbyPOIs = pois;

      if (this.emergencyMap && pois.length) {
        pois.forEach((poi) => {
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
    callParent() {
      const phone = this.selectedEmergencyChild.emergencyContacts[0]?.phone || 'N/A';
      if (phone !== 'N/A') {
        window.open(`tel:${phone}`);
      } else {
        window.alert('Parent contact number unavailable.');
      }
    },
    callServices() {
      window.open('tel:112');
    },
    shareEmergencyInfo() {
      window.alert('Copied emergency summary to clipboard');
    },
    toggleTaskStatus(medicationId) {
      const current = this.tasks.find((task) => task.medicationId === medicationId)?.status;
      if (!current) return;
      const statuses = ['Pending', 'Taken', 'Missed'];
      const next = statuses[(statuses.indexOf(current) + 1) % statuses.length];
      setMedicationStatus(medicationId, next);
    },
    meaningfulList(items, fallback) {
      const values = items.filter((item) => item && !['None', 'None known'].includes(item));
      return values.length ? values.join(', ') : fallback;
    },
    prescriptionLabel(child) {
      return child.medications.some((medication) => medication.prescriptionUploaded)
        ? 'Prescription available'
        : 'No prescription uploaded';
    }
  }
};
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  padding: 28px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  font-family: 'Segoe UI', -apple-system, BlinkMacSystemFont, sans-serif;
  transition: background-color 0.3s ease, color 0.3s ease;
}

.topbar,
.hero-strip,
.stats-row,
.filters,
.alerts-panel,
.child-overview,
.admin-grid {
  max-width: 1240px;
  margin: 0 auto;
}

.topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: var(--shadow-md);
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
}

.eyebrow,
h1,
h2,
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
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.top-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

button,
select {
  font: inherit;
  transition: all 0.3s ease;
}

.top-actions button {
  min-height: 44px;
  border: none;
  border-radius: 10px;
  padding: 12px 18px;
  background: linear-gradient(135deg, var(--color-brand-dark), #1a202c);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  box-shadow: var(--shadow-md);
}

.top-actions button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(45, 55, 72, 0.4);
}

.top-actions .emergency {
  background: linear-gradient(135deg, var(--color-danger), #c53030);
}

.top-actions .emergency:hover {
  box-shadow: 0 6px 20px rgba(229, 62, 62, 0.4);
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 18px;
}

.child-summary {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}

.child-summary img.child-photo {
  width: 88px;
  height: 88px;
  border-radius: 18px;
  object-fit: cover;
  border: 1px solid var(--color-border);
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

.emergency-actions .secondary-button {
  min-width: 160px;
  background: rgba(45, 143, 123, 0.12);
  color: var(--color-text-primary);
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
  gap: 24px;
  align-items: center;
  margin-top: 24px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 24px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(10px);
}

.hero-strip h2 {
  margin: 8px 0 12px;
  font-size: clamp(1.5rem, 3vw, 2.5rem);
  font-weight: 700;
  color: var(--color-text-primary);
}

.hero-strip p:not(.eyebrow) {
  max-width: 720px;
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.stats-row article {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  min-height: 110px;
  padding: 20px;
  text-align: center;
}

.stats-row article:hover,
.filters:hover,
.panel:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stats-row span {
  display: block;
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 8px;
}

.stats-row p {
  color: var(--color-text-secondary);
  font-weight: 600;
}

.pending span {
  color: var(--color-warning);
}

.taken span {
  color: var(--color-success);
}

.missed span {
  color: var(--color-danger);
}

.alerts strong {
  display: block;
  margin-bottom: 8px;
  color: var(--color-text-primary);
  font-weight: 700;
}

.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-top: 20px;
  padding: 20px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  box-shadow: var(--shadow-sm);
}

.filters label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.filters select {
  min-height: 44px;
  border: 2px solid var(--color-border);
  border-radius: 10px;
  padding: 12px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font-weight: 500;
  transition: border-color 0.3s ease;
}

.filters select:focus {
  outline: none;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.alerts-panel {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.alerts-panel article {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
}

.alerts-panel h2 {
  margin-top: 8px;
  font-size: 1.25rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.alerts-panel article p:not(.eyebrow) {
  margin-top: 10px;
  color: var(--color-text-secondary);
  font-weight: 500;
  line-height: 1.5;
}

.child-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.child-overview article {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 20px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}

.child-overview article:hover {
  transform: translateY(-4px);
}

.child-overview header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
  margin-bottom: 16px;
}

.child-overview header span {
  border-radius: 20px;
  padding: 6px 12px;
  background: linear-gradient(135deg, var(--color-success), #2f855a);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 700;
  white-space: nowrap;
}

.child-overview dl {
  display: grid;
  gap: 12px;
  margin: 0;
}

.child-overview dt {
  color: var(--color-text-secondary);
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.child-overview dd {
  margin: 4px 0 0;
  color: var(--color-text-primary);
  font-weight: 600;
}

.admin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 20px;
  margin-top: 20px;
  align-items: start;
}

.task-stack,
.control-stack,
.panel {
  display: grid;
  gap: 16px;
}

.panel {
  padding: 20px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  backdrop-filter: blur(10px);
}

.panel header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.panel header h2 {
  color: var(--color-text-primary);
}

.panel header span {
  color: var(--color-text-secondary);
  font-weight: 600;
  font-size: 0.875rem;
}

.note-item {
  display: grid;
  gap: 6px;
  border-top: 1px solid var(--color-border-light);
  padding-top: 12px;
}

.note-item strong {
  color: var(--color-text-primary);
}

.note-item p {
  color: var(--color-text-secondary);
  font-weight: 500;
}

.empty-state {
  border-radius: 10px;
  padding: 12px 16px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-secondary);
  font-weight: 600;
}

@media (max-width: 980px) {
  .stats-row,
  .alerts-panel,
  .admin-grid {
    grid-template-columns: 1fr;
  }

  .child-overview {
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  }
}

@media (max-width: 640px) {
  .dashboard {
    padding: 20px;
  }

  .topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  h1 {
    font-size: 2rem;
  }

  .stats-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .admin-grid {
    grid-template-columns: 1fr;
  }
}
</style>
