<template>
  <article class="poi-card">
    <div class="poi-card-heading">
      <span class="poi-icon" aria-hidden="true">{{ poi.icon }}</span>
      <div>
        <p class="poi-type">{{ poi.label }}</p>
        <h3>{{ poi.name }}</h3>
      </div>
    </div>

    <div class="poi-card-details">
      <span class="detail-pill">{{ formattedDistance }}</span>
      <span class="detail-pill">{{ formattedEta }}</span>
    </div>

    <div class="poi-card-actions">
      <a
        :href="routeLink"
        class="route-link"
        target="_blank"
        rel="noreferrer noopener"
        :aria-label="`Open route to ${poi.name}`"
      >
        Show route
      </a>
    </div>
  </article>
</template>

<script>
import { buildEmergencyRouteLink } from '../services/emergencyService';
import { formatDistanceMeters, estimateDriveTimeMinutes } from '../utils/formatters';

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
    formattedEta() {
      return estimateDriveTimeMinutes(this.poi.distance);
    }
  }
};
</script>

<style scoped>
.poi-card {
  display: grid;
  gap: 16px;
  border: 1px solid rgba(66, 97, 135, 0.15);
  background: var(--color-bg-secondary);
  border-radius: 14px;
  padding: 18px;
  box-shadow: var(--shadow-sm);
}

.poi-card-heading {
  display: flex;
  gap: 14px;
  align-items: center;
}

.poi-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(49, 130, 206, 0.12);
  color: var(--color-brand);
  font-size: 1.4rem;
}

.poi-type {
  margin: 0 0 2px;
  color: var(--color-text-secondary);
  font-size: 0.85rem;
  font-weight: 700;
}

.poi-card h3 {
  margin: 0;
  font-size: 1rem;
}

.poi-card-details {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, max-content));
  gap: 10px;
}

.detail-pill {
  display: inline-flex;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  color: var(--color-text-primary);
  background: var(--color-bg-tertiary);
}

.poi-card-actions {
  display: flex;
  justify-content: flex-end;
}

.route-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 20px;
  border-radius: 12px;
  background: var(--color-brand);
  color: white;
  text-decoration: none;
  font-weight: 700;
}

.route-link:hover,
.route-link:focus-visible {
  background: #1d6fb8;
}
</style>
