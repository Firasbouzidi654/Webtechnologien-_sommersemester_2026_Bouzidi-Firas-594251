<template>
  <main class="dashboard admin-dashboard">
    <nav class="topbar">
      <div>
        <p class="eyebrow">Admin control center</p>
        <h1>Medication tasks today</h1>
      </div>
      <div class="top-actions">
        <button class="emergency" type="button">Emergency mode</button>
        <button type="button" @click="$emit('navigate', '/login')">Log out</button>
      </div>
    </nav>

    <section class="hero-strip">
      <div>
        <p class="eyebrow">Live care operations</p>
        <h2>Clear schedule, quick confirmations, safer handovers</h2>
        <p>Staff can scan the plan, confirm medication, see missed alerts, and check the daily calendar without leaving the dashboard.</p>
      </div>
      <img :src="heroImage" alt="Modern kindergarten health dashboard" />
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
          @confirm="confirmMedication"
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
import MedicationTaskCard from '../components/MedicationTaskCard.vue';
import QRMedicationCard from '../components/QRMedicationCard.vue';
import heroImage from '../assets/hero.png';
import { kindercareStore, markMedicationTaken, taskReminderDue } from '../state/kindercareStore';

export default {
  name: 'AdminDashboard',
  components: {
    AdminCalendar,
    EmergencyContactCard,
    MedicationTaskCard,
    QRMedicationCard
  },
  emits: ['navigate'],
  data() {
    return {
      heroImage,
      groupFilter: 'all',
      childFilter: 'all'
    };
  },
  computed: {
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
  methods: {
    confirmMedication(medicationId) {
      markMedicationTaken(medicationId);
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
  background:
    linear-gradient(135deg, #f0f4f8 0%, #e1e8ed 100%);
  color: #1a202c;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.topbar,
.hero-strip,
.stats-row,
.filters,
.alerts-panel,
.child-overview,
.admin-grid {
  max-width: 1440px;
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
  font-size: 2.5rem;
  font-weight: 800;
  background: linear-gradient(135deg, #3182ce, #2d3748);
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
  background: linear-gradient(135deg, #2d3748, #1a202c);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(45, 55, 72, 0.3);
}

.top-actions button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(45, 55, 72, 0.4);
}

.top-actions .emergency {
  background: linear-gradient(135deg, #e53e3e, #c53030);
}

.top-actions .emergency:hover {
  box-shadow: 0 6px 20px rgba(229, 62, 62, 0.4);
}

.hero-strip {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
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

.hero-strip h2 {
  margin: 8px 0 12px;
  font-size: clamp(1.5rem, 3vw, 2.5rem);
  font-weight: 700;
  color: #2d3748;
}

.hero-strip p:not(.eyebrow) {
  max-width: 720px;
  color: #4a5568;
  line-height: 1.6;
}

.hero-strip img {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.stats-row article,
.filters,
.panel {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stats-row article:hover,
.filters:hover,
.panel:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 35px rgba(0, 0, 0, 0.15);
}

.stats-row article {
  min-height: 110px;
  padding: 20px;
  text-align: center;
}

.stats-row span {
  display: block;
  font-size: 2.5rem;
  font-weight: 800;
  margin-bottom: 8px;
}

.stats-row p {
  color: #4a5568;
  font-weight: 600;
}

.pending span {
  color: #ed8936;
}

.taken span {
  color: #38a169;
}

.missed span {
  color: #e53e3e;
}

.alerts strong {
  display: block;
  margin-bottom: 8px;
  color: #2d3748;
  font-weight: 700;
}

.filters {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
  padding: 20px;
}

.filters label {
  display: grid;
  gap: 8px;
  font-weight: 600;
  color: #2d3748;
}

.filters select {
  min-height: 44px;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  padding: 12px;
  background: #fff;
  color: #2d3748;
  font-weight: 500;
  transition: border-color 0.3s ease;
}

.filters select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.alerts-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.alerts-panel article {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.alerts-panel h2 {
  margin-top: 8px;
  font-size: 1.25rem;
  font-weight: 700;
  color: #2d3748;
}

.alerts-panel article p:not(.eyebrow) {
  margin-top: 10px;
  color: #4a5568;
  font-weight: 500;
  line-height: 1.5;
}

.child-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.child-overview article {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
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
  background: linear-gradient(135deg, #38a169, #2f855a);
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
  color: #4a5568;
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.child-overview dd {
  margin: 4px 0 0;
  color: #2d3748;
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
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.panel header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.panel header span {
  color: #4a5568;
  font-weight: 600;
}

.note-item {
  display: grid;
  gap: 6px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  padding-top: 12px;
}

.note-item p {
  color: #4a5568;
  font-weight: 500;
}

.empty-state {
  border-radius: 10px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #edf2f7, #e2e8f0);
  color: #4a5568;
  font-weight: 600;
}

@media (max-width: 980px) {
  .hero-strip,
  .stats-row,
  .filters,
  .alerts-panel,
  .child-overview,
  .admin-grid {
    grid-template-columns: 1fr;
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
}
</style>
