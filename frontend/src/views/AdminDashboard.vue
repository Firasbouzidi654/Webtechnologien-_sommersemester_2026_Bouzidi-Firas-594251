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
    linear-gradient(180deg, #f6f9fb 0%, #eef3f6 100%);
  color: #20303f;
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
}

.eyebrow,
h1,
h2,
p {
  margin: 0;
}

.eyebrow {
  color: #287b68;
  font-size: 0.8rem;
  font-weight: 900;
  text-transform: uppercase;
}

h1 {
  margin-top: 4px;
  font-size: 2rem;
}

.top-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

button,
select {
  font: inherit;
}

.top-actions button {
  min-height: 42px;
  border: none;
  border-radius: 8px;
  padding: 10px 14px;
  background: #20303f;
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

.top-actions .emergency {
  background: #d94a4a;
}

.hero-strip {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 22px;
  align-items: center;
  margin-top: 24px;
  border: 1px solid rgba(32, 48, 63, 0.1);
  border-radius: 8px;
  padding: 22px;
  background: linear-gradient(135deg, #ffffff, #e7f0ff);
  box-shadow: 0 18px 40px rgba(32, 48, 63, 0.08);
}

.hero-strip h2 {
  margin: 6px 0 8px;
  font-size: clamp(1.5rem, 3vw, 2.35rem);
}

.hero-strip p:not(.eyebrow) {
  max-width: 720px;
  color: #536577;
}

.hero-strip img {
  width: 100%;
  height: 190px;
  border-radius: 8px;
  object-fit: cover;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.stats-row article,
.filters,
.panel {
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 14px 32px rgba(32, 48, 63, 0.08);
}

.stats-row article {
  min-height: 104px;
  padding: 18px;
}

.stats-row span {
  display: block;
  font-size: 2rem;
  font-weight: 900;
}

.stats-row p {
  color: #637486;
}

.pending span {
  color: #a86a00;
}

.taken span {
  color: #287b68;
}

.missed span {
  color: #a12d2d;
}

.alerts strong {
  display: block;
  margin-bottom: 8px;
}

.filters {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
  padding: 16px;
}

.filters label {
  display: grid;
  gap: 7px;
  font-weight: 800;
}

.filters select {
  min-height: 42px;
  border: 1px solid rgba(32, 48, 63, 0.14);
  border-radius: 8px;
  padding: 9px 12px;
  background: #fff;
  color: #20303f;
}

.alerts-panel {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.alerts-panel article,
.child-overview article {
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  padding: 18px;
  background: #fff;
  box-shadow: 0 14px 32px rgba(32, 48, 63, 0.08);
}

.alerts-panel h2 {
  margin-top: 6px;
  font-size: 1.15rem;
}

.alerts-panel article p:not(.eyebrow) {
  margin-top: 8px;
  color: #637486;
  font-weight: 700;
}

.child-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
}

.child-overview header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.child-overview header span {
  border-radius: 999px;
  padding: 5px 10px;
  background: #e9f6f2;
  color: #287b68;
  font-size: 0.78rem;
  font-weight: 900;
  white-space: nowrap;
}

.child-overview dl {
  display: grid;
  gap: 10px;
  margin: 14px 0 0;
}

.child-overview dt {
  color: #637486;
  font-size: 0.76rem;
  font-weight: 900;
}

.child-overview dd {
  margin: 2px 0 0;
  color: #20303f;
  font-weight: 700;
}

.admin-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 18px;
  margin-top: 18px;
  align-items: start;
}

.task-stack,
.control-stack,
.panel {
  display: grid;
  gap: 16px;
}

.panel {
  padding: 18px;
}

.panel header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.panel header span {
  color: #637486;
  font-weight: 800;
}

.note-item {
  display: grid;
  gap: 6px;
  border-top: 1px solid rgba(32, 48, 63, 0.08);
  padding-top: 12px;
}

.note-item p {
  color: #536577;
}

.empty-state {
  border-radius: 8px;
  padding: 10px 12px;
  background: #f8fafb;
  color: #637486;
  font-weight: 800;
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
    padding: 18px;
  }

  .topbar {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
