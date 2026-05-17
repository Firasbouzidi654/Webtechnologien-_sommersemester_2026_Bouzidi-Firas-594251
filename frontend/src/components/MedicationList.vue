<template>
  <section class="medication-list">
    <article v-for="medication in medications" :key="medication.medicationId" class="medication-card">
      <header>
        <div>
          <span class="med-id">{{ medication.medicationId || 'N/A' }}</span>
          <h3>{{ medication.name || 'Medication' }}</h3>
          <p>{{ medication.activeIngredient || 'No active ingredient listed' }}</p>
        </div>
        <span class="upload-badge" :class="{ missing: !medication.prescriptionUploaded }">
          {{ medication.prescriptionUploaded ? 'Prescription uploaded' : 'Upload needed' }}
        </span>
      </header>

      <dl class="schedule-grid">
        <div>
          <dt>Frequency</dt>
          <dd>{{ scheduleFor(medication).frequency }}</dd>
        </div>
        <div>
          <dt>Time</dt>
          <dd>{{ scheduleFor(medication).specificTime }}</dd>
        </div>
        <div>
          <dt>Dosage</dt>
          <dd>{{ scheduleFor(medication).dosage }}</dd>
        </div>
      </dl>

      <p class="instructions">{{ medication.instructions || 'No instructions added.' }}</p>

      <section class="history">
        <h4>Medication history</h4>
        <p v-for="log in medication.history || []" :key="log.id">
          {{ formatDate(log.loggedAt) }} - {{ log.status }} by {{ log.adminName }}
        </p>
      </section>
    </article>
  </section>
</template>

<script>
export default {
  name: 'MedicationList',
  props: {
    medications: {
      type: Array,
      required: true
    }
  },
  methods: {
    scheduleFor(medication) {
      return {
        frequency: 'Daily',
        specificTime: '--:--',
        dosage: medication?.dosage || 'Not set',
        ...(medication?.schedule || {})
      };
    },
    formatDate(value) {
      return new Date(value).toLocaleString([], {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    }
  }
};
</script>

<style scoped>
.medication-list {
  display: grid;
  gap: 14px;
}

.medication-card {
  display: grid;
  gap: 14px;
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.med-id,
.upload-badge {
  display: inline-flex;
  border-radius: 999px;
  padding: 4px 9px;
  font-size: 0.78rem;
  font-weight: 800;
}

.med-id {
  background: #e9f6f2;
  color: #287b68;
}

.upload-badge {
  background: #e7f0ff;
  color: #255b9a;
  white-space: nowrap;
}

.upload-badge.missing {
  background: #fff1d6;
  color: #8a5b00;
}

h3,
h4,
p,
dl,
dd {
  margin: 0;
}

h3 {
  margin-top: 8px;
  font-size: 1.08rem;
}

header p,
dt,
.history p {
  color: #637486;
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

dt {
  font-size: 0.78rem;
  font-weight: 800;
}

dd {
  font-weight: 800;
}

.instructions {
  border-left: 4px solid #2d8f7b;
  padding-left: 10px;
  color: #405265;
}

.history {
  display: grid;
  gap: 6px;
  border-top: 1px solid rgba(32, 48, 63, 0.1);
  padding-top: 12px;
}

@media (max-width: 640px) {
  header,
  .schedule-grid {
    grid-template-columns: 1fr;
  }

  header {
    display: grid;
  }
}
</style>
