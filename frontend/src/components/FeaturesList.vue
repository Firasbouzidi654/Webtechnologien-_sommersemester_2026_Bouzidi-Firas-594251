<template>
  <div class="features-list">
    <h3>{{ t('featuresTitle') }}</h3>
    <ul>
      <li v-for="feature in features" :key="feature.id" class="feature-item">
        <span class="feature-icon">{{ feature.icon }}</span>
        <span class="feature-text">{{ feature.text }}</span>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'FeaturesList',
  props: {
    language: {
      type: String,
      default: 'en'
    }
  },
  computed: {
    features() {
      const featuresData = {
        en: [
          { id: 1, icon: '👨‍👩‍👧', text: 'Parent-Teacher Communication' },
          { id: 2, icon: '📅', text: 'Health Tracking' },
          { id: 3, icon: '🔔', text: 'Emergency Alerts' },
          { id: 4, icon: '📊', text: 'Medication Logs' }
        ],
        de: [
          { id: 1, icon: '👨‍👩‍👧', text: 'Eltern-Lehrer-Kommunikation' },
          { id: 2, icon: '📅', text: 'Gesundheitsverfolgung' },
          { id: 3, icon: '🔔', text: 'Notfallbenachrichtigungen' },
          { id: 4, icon: '📊', text: 'Medikationsprotokolle' }
        ]
      };
      return featuresData[this.language] || featuresData.en;
    }
  },
  methods: {
    t(key) {
      const translations = {
        en: { featuresTitle: 'Key Features' },
        de: { featuresTitle: 'Hauptmerkmale' }
      };
      return translations[this.language][key] || key;
    }
  }
};
</script>

<style scoped>
.features-list {
  margin-top: 20px;
  text-align: center;
}

.features-list h3 {
  color: #2a9d8f;
  margin-bottom: 15px;
}

.features-list ul {
  list-style: none;
  padding: 0;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.feature-icon {
  font-size: 1.5rem;
  margin-right: 10px;
}

.feature-text {
  font-weight: 500;
  color: #1e3a8a;
}
</style>
