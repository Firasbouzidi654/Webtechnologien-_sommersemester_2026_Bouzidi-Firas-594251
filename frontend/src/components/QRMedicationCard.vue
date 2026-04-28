<template>
  <article class="qr-card">
    <div class="qr-code" :aria-label="`QR placeholder for ${medicationId}`">
      <span v-for="cell in cells" :key="cell" :class="{ filled: filledCells.includes(cell) }"></span>
    </div>
    <div>
      <p class="eyebrow">QR verification</p>
      <h3>{{ medicationName }}</h3>
      <dl>
        <div>
          <dt>Unique medication ID</dt>
          <dd>{{ medicationId }}</dd>
        </div>
        <div>
          <dt>QR payload</dt>
          <dd>{{ qrPayload }}</dd>
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
      const seed = this.medicationId
        .split('')
        .reduce((sum, char) => sum + char.charCodeAt(0), 0);

      return this.cells.filter((cell) => (cell + seed) % 3 === 0 || cell % 11 === 0);
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
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  padding: 16px;
  background: #fff;
}

.qr-code {
  display: grid;
  width: 96px;
  height: 96px;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
  border: 6px solid #20303f;
  padding: 5px;
  background: #fff;
}

.qr-code span {
  background: #eef3f6;
}

.qr-code .filled {
  background: #20303f;
}

.eyebrow,
h3,
dl,
dd {
  margin: 0;
}

.eyebrow {
  color: #287b68;
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
  color: #637486;
  font-size: 0.78rem;
  font-weight: 800;
}

dd {
  overflow-wrap: anywhere;
  font-weight: 800;
}

@media (max-width: 520px) {
  .qr-card {
    grid-template-columns: 1fr;
  }
}
</style>
