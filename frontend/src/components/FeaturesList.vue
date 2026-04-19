<template>
  <section class="features-list">
    <h3>{{ content.title }}</h3>
    <ul>
      <li v-for="feature in content.items" :key="feature.id" class="feature-item">
        <span class="feature-icon" aria-hidden="true">{{ feature.icon }}</span>
        <span class="feature-text">{{ feature.text }}</span>
      </li>
    </ul>
  </section>
</template>

<script>
export default {
  name: 'FeaturesList',
  props: {
    content: {
      type: Object,
      required: true
    },
    isDark: {
      type: Boolean,
      default: false
    }
  }
};
</script>

<style scoped>
.features-list {
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid rgba(34, 62, 79, 0.12);
}

.features-list h3 {
  margin: 0 0 14px;
  color: #2d8f7b;
  font-size: 1rem;
}

.features-list ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(34, 62, 79, 0.08);
  border-radius: 18px;
}

.feature-icon {
  font-size: 1.4rem;
}

.feature-text {
  color: #20303f;
  font-weight: 600;
}

:global(.dark-mode) .feature-item {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(212, 230, 241, 0.12);
}

:global(.dark-mode) .feature-text {
  color: #edf4f8;
}

:global(.dark-mode) .features-list {
  border-top-color: rgba(212, 230, 241, 0.12);
}
</style>
