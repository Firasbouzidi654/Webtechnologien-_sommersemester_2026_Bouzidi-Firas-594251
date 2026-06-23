<template>
  <article class="poi-card">
    <figure v-if="hasPoiImage" class="poi-image-wrap">
      <img
        class="poi-image"
        :src="poiImage"
        :alt="`${poi.label} image for ${poi.name}`"
        loading="lazy"
        @error="imageFailed = true"
      />
    </figure>

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

const PHARMACY_IMAGES = [
  'https://images.unsplash.com/photo-1766258630872-2b1403439fb5?auto=format&fit=crop&q=80&w=900',
  'https://images.unsplash.com/photo-1569597062614-5f8dcdbe0581?auto=format&fit=crop&q=80&w=900'
];

const POLICE_IMAGES = [
  'https://images.unsplash.com/photo-1693329900318-9686ec84b1cd?auto=format&fit=crop&q=80&w=900'
];

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
  data() {
    return {
      imageFailed: false
    };
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
    },
    hasPoiImage() {
      return ['pharmacy', 'police'].includes(this.poi.type) && !this.imageFailed;
    },
    poiImage() {
      const images = this.poi.type === 'police' ? POLICE_IMAGES : PHARMACY_IMAGES;
      const seed = Number(this.poi.id) || this.poi.name.length;
      return images[Math.abs(seed) % images.length];
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

.poi-image-wrap {
  width: 100%;
  aspect-ratio: 16 / 9;
  margin: 0;
  overflow: hidden;
  border-radius: 10px;
  background: var(--color-bg-tertiary);
}

.poi-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
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
