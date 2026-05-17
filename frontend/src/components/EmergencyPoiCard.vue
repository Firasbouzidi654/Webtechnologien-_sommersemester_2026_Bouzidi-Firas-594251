<template>
  <article class="poi-card">
    <figure v-if="hasPoiImage" class="poi-image-wrap">
      <img
        class="poi-image"
        :src="poiImage"
        :alt="`${poi.label || poi.type || 'Emergency support'} image for ${poi.name || 'nearby support'}`"
        loading="lazy"
        @error="imageFailed = true"
      />
    </figure>

    <div class="poi-card-heading">
      <span v-if="!hasPoiImage" class="poi-icon" :class="poiTypeClass" aria-hidden="true">{{ iconLabel }}</span>
      <div>
        <p class="poi-type">{{ poi.label || 'Emergency support' }}</p>
        <h3>{{ poi.name || 'Nearby support' }}</h3>
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
        :aria-label="`Open route to ${poi.name || 'nearby support'}`"
      >
        Show route
      </a>
    </div>
  </article>
</template>

<script>
import { buildEmergencyRouteLink } from '../services/emergencyService';
import { formatDistanceMeters, estimateDriveTimeMinutes } from '../utils/formatters';

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
      default: () => ({ lat: 52.52, lng: 13.405 })
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
      return Number.isFinite(this.poi?.distance) ? formatDistanceMeters(this.poi.distance) : 'Distance unknown';
    },
    formattedEta() {
      return Number.isFinite(this.poi?.distance) ? estimateDriveTimeMinutes(this.poi.distance) : 'ETA unknown';
    },
    iconLabel() {
      const labels = {
        hospital: 'H',
        pharmacy: 'Rx',
        police: '112'
      };

      return labels[this.poi?.type] || 'SOS';
    },
    poiTypeClass() {
      return this.poi?.type || 'poi';
    },
    hasPoiImage() {
      return ['pharmacy', 'police'].includes(this.poi?.type) && !this.imageFailed;
    },
    poiImage() {
      const images = this.poi?.type === 'police' ? POLICE_IMAGES : PHARMACY_IMAGES;
      const seed = Number(this.poi?.id) || String(this.poi?.name || '').length;
      return images[Math.abs(seed) % images.length];
    }
  }
};
</script>

<style scoped>
.poi-card {
  display: grid;
  gap: 15px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-secondary);
  border-radius: 16px;
  padding: 14px;
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: transform 0.22s ease, box-shadow 0.22s ease, border-color 0.22s ease;
}

.poi-card:hover {
  transform: translateY(-4px);
  border-color: rgba(49, 130, 206, 0.28);
  box-shadow: var(--shadow-lg);
}

.poi-image-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  margin: 0;
  overflow: hidden;
  border-radius: 13px;
  background: var(--color-bg-tertiary);
}

.poi-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform: scale(1.01);
  transition: transform 0.35s ease, filter 0.35s ease;
}

.poi-card:hover .poi-image {
  transform: scale(1.06);
  filter: brightness(1.04) saturate(1.06);
}

.poi-card-heading {
  display: flex;
  gap: 14px;
  align-items: center;
  min-width: 0;
}

.poi-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(49, 130, 206, 0.12);
  color: var(--color-brand);
  font-size: 0.82rem;
  font-weight: 900;
}

.poi-icon.hospital {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.poi-icon.pharmacy {
  background: var(--color-taken);
  color: var(--color-taken-text);
}

.poi-icon.police {
  background: var(--color-upcoming);
  color: var(--color-upcoming-text);
}

.poi-type {
  margin: 0 0 2px;
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 900;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.poi-card h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 1.02rem;
  line-height: 1.25;
  overflow-wrap: anywhere;
}

.poi-card-details {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.detail-pill {
  display: inline-flex;
  justify-content: center;
  padding: 8px 12px;
  border-radius: 999px;
  font-size: 0.85rem;
  color: var(--color-text-primary);
  background: linear-gradient(135deg, var(--color-bg-tertiary), var(--color-bg-primary));
  border: 1px solid var(--color-border-light);
  font-weight: 800;
  box-shadow: 0 6px 12px rgba(15, 23, 42, 0.04);
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
  padding: 0 22px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-brand), #256db0);
  color: white;
  text-decoration: none;
  font-weight: 900;
  box-shadow: 0 10px 20px rgba(49, 130, 206, 0.2);
  transition: transform 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.route-link:hover,
.route-link:focus-visible {
  background: #1d6fb8;
  box-shadow: 0 8px 18px rgba(49, 130, 206, 0.25);
  transform: translateY(-1px);
}

@media (max-width: 520px) {
  .poi-card-details {
    grid-template-columns: 1fr;
  }

  .poi-card-actions,
  .route-link {
    width: 100%;
  }
}
</style>
