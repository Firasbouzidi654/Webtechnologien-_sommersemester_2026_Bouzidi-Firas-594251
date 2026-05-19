<template>
  <article class="qr-card" :class="{ expanded: detailsVisible }">
    <div class="qr-main">
      <div class="qr-code" :aria-label="`QR placeholder for ${safeMedicationId}`">
        <span class="scanner-line" aria-hidden="true"></span>
        <span v-for="cell in cells" :key="cell" :class="{ filled: filledCells.includes(cell) }"></span>
      </div>
      <div class="qr-meta">
        <div class="qr-heading-row">
          <div>
            <p class="eyebrow">QR verification</p>
            <h3>{{ medicationName || 'Medication verification' }}</h3>
          </div>
          <button
            type="button"
            class="toggle-button"
            v-if="compact"
            @click="toggleDetails"
            :aria-expanded="detailsVisible"
            :aria-controls="detailsId"
          >
            {{ buttonLabel }}
          </button>
        </div>
        <div class="qr-summary">
          <dl>
            <div>
              <dt>Unique medication ID</dt>
              <dd>{{ safeMedicationId }}</dd>
            </div>
          </dl>
        </div>
      </div>
    </div>

    <div :id="detailsId" class="qr-details" :class="{ open: detailsVisible }">
      <dl>
        <div>
          <dt>QR payload</dt>
          <dd>{{ qrPayload || 'No payload available' }}</dd>
        </div>
        <div>
          <dt>Medication name</dt>
          <dd>{{ medicationName }}</dd>
        </div>
        <div>
          <dt>Verification</dt>
          <dd>Use the code or medication ID for quick check-in.</dd>
        </div>
      </dl>
    </div>
  </article>
</template>

<script>
export default {
  name: 'QRMedicationCard',
  props: {
    medicationId: {
      type: String,
      required: true
    },
    medicationName: {
      type: String,
      required: true
    },
    qrPayload: {
      type: String,
      required: true
    },
    compact: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      detailsOpen: !this.compact
    };
  },
  watch: {
    compact(value) {
      this.detailsOpen = !value;
    }
  },
  computed: {
    cells() {
      return Array.from({ length: 49 }, (_, index) => index);
    },
    filledCells() {
      const seed = this.safeMedicationId
        .split('')
        .reduce((sum, char) => sum + char.charCodeAt(0), 0);

      return this.cells.filter((cell) => (cell + seed) % 3 === 0 || cell % 11 === 0);
    },
    safeMedicationId() {
      return this.medicationId || 'MED-000';
    },
    detailsId() {
      const safeId = String(this.medicationId || 'qr-card').replace(/\s+/g, '-').toLowerCase();
      return `qr-card-details-${safeId}`;
    },
    detailsVisible() {
      return !this.compact || this.detailsOpen;
    },
    buttonLabel() {
      return this.detailsOpen ? 'Hide details' : 'View QR Code';
    }
  },
  methods: {
    toggleDetails() {
      this.detailsOpen = !this.detailsOpen;
    }
  }
};
</script>

<style scoped>
.qr-card {
  width: 100%;
  max-width: 560px;
  display: grid;
  gap: 14px;
  border: 1px solid var(--color-border);
  border-radius: 18px;
  padding: 16px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  overflow: hidden;
}

.qr-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.qr-main {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.qr-code {
  position: relative;
  display: grid;
  width: 96px;
  height: 96px;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
  border: 6px solid var(--color-text-primary);
  border-radius: 18px;
  padding: 5px;
  background: var(--color-bg-secondary);
  overflow: hidden;
  box-shadow: inset 0 0 0 1px var(--color-border);
}

.scanner-line {
  position: absolute;
  left: 5px;
  right: 5px;
  top: 10px;
  z-index: 2;
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(90deg, transparent, var(--color-taken-border), transparent);
  box-shadow: 0 0 14px var(--color-taken-border);
  animation: scan-line 1.8s ease-in-out infinite;
}

.qr-code span {
  background: var(--color-bg-tertiary);
}

.qr-code .filled {
  background: var(--color-text-primary);
}

.qr-meta {
  display: grid;
  gap: 12px;
  min-width: 0;
}

.qr-heading-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
  flex-wrap: wrap;
}

.eyebrow,
h3,
dl,
dd {
  margin: 0;
}

.eyebrow {
  color: var(--color-brand);
  font-size: 0.78rem;
  font-weight: 900;
  text-transform: uppercase;
}

h3 {
  margin-top: 4px;
  font-size: 1rem;
}

.qr-summary dl {
  display: grid;
  gap: 8px;
}

dt {
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 800;
}

dd {
  overflow-wrap: anywhere;
  font-weight: 800;
  color: var(--color-text-primary);
}

.toggle-button {
  min-height: 40px;
  border: 1px solid var(--color-border);
  border-radius: 999px;
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  padding: 10px 16px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.2s ease, background-color 0.2s ease, border-color 0.2s ease;
}

.toggle-button:hover {
  transform: translateY(-1px);
  border-color: var(--color-brand);
  background: rgba(49, 130, 206, 0.12);
}

.qr-details {
  max-height: 0;
  opacity: 0;
  overflow: hidden;
  transition: max-height 0.32s ease, opacity 0.32s ease, transform 0.32s ease;
  transform: translateY(-8px);
}

.qr-details.open {
  max-height: 460px;
  opacity: 1;
  transform: translateY(0);
}

.qr-details dl {
  display: grid;
  gap: 10px;
}

.qr-details dt {
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.qr-details dd {
  color: var(--color-text-primary);
  font-weight: 700;
  overflow-wrap: anywhere;
}

@keyframes scan-line {
  0%,
  100% {
    transform: translateY(0);
    opacity: 0.4;
  }

  50% {
    transform: translateY(66px);
    opacity: 1;
  }
}

@media (max-width: 520px) {
  .qr-main {
    grid-template-columns: 1fr;
  }

  .qr-heading-row {
    flex-direction: column;
    align-items: stretch;
  }

  .toggle-button {
    width: 100%;
  }
}
</style>
