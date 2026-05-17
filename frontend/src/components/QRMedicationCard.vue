<template>
  <article class="qr-card">
    <div class="qr-code" :aria-label="`QR placeholder for ${safeMedicationId}`">
      <span class="scanner-line" aria-hidden="true"></span>
      <span v-for="cell in cells" :key="cell" :class="{ filled: filledCells.includes(cell) }"></span>
    </div>
    <div>
      <p class="eyebrow">QR verification</p>
      <h3>{{ medicationName || 'Medication verification' }}</h3>
      <dl>
        <div>
          <dt>Unique medication ID</dt>
          <dd>{{ safeMedicationId }}</dd>
        </div>
        <div>
          <dt>QR payload</dt>
          <dd>{{ qrPayload || 'No payload available' }}</dd>
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
    }
  }
};
</script>

<style scoped>
.qr-card {
  display: grid;
  grid-template-columns: 96px minmax(0, 1fr);
  gap: 16px;
  align-items: center;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 16px;
  background: var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.qr-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.qr-code {
  position: relative;
  display: grid;
  width: 96px;
  height: 96px;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
  border: 6px solid var(--color-text-primary);
  border-radius: 12px;
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

dl {
  display: grid;
  gap: 8px;
  margin-top: 10px;
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
  .qr-card {
    grid-template-columns: 1fr;
  }
}
</style>
