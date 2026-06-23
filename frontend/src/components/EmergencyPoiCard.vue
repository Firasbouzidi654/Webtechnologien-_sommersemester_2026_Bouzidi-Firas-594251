<template>
  <article class="poi-card">
    <div>
      <p>{{ poi.label }}</p>
      <h3>{{ poi.name }}</h3>
    </div>
    <span>{{ formattedDistance }} · {{ estimatedTravelTime }}</span>
    <a :href="routeLink" target="_blank" rel="noreferrer noopener">Show route</a>
  </article>
</template>

<script>
import { buildEmergencyRouteLink } from '../services/emergencyService';
import { estimateDriveTimeMinutes, formatDistanceMeters } from '../utils/formatters';

export default {
  name: 'EmergencyPoiCard',
  props: {
    poi: {
      type: Object,
      required: true
    },
    from: {
      type: Object,
      required: true
    }
  },
  computed: {
    routeLink() {
      return buildEmergencyRouteLink(this.from, this.poi);
    },
    formattedDistance() {
      return formatDistanceMeters(this.poi.distance);
    },
    estimatedTravelTime() {
      return estimateDriveTimeMinutes(this.poi.distance);
    }
  }
};
</script>

<style scoped>
.poi-card {
  display: grid;
  gap: 8px;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px;
  background: var(--color-bg-primary);
}

.poi-card p,
.poi-card h3 {
  margin: 0;
}

.poi-card p,
.poi-card > span {
  color: var(--color-text-secondary);
  font-size: 0.82rem;
}

.poi-card h3 {
  margin-top: 3px;
  color: var(--color-text-primary);
  font-size: 0.98rem;
}

.poi-card a {
  color: var(--color-brand);
  font-weight: 800;
  text-decoration: none;
}
</style>
