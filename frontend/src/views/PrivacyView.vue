<template>
  <AuthShell
    :common="common"
    :hero="hero"
    :is-dark="isDark"
    :language="language"
    :page="privacy"
    @navigate="$emit('navigate', $event)"
    @toggle-theme="$emit('toggle-theme')"
    @update-language="$emit('update-language', $event)"
  >
    <section class="page-header">
      <h3>{{ privacy.title }}</h3>
      <p>{{ privacy.intro }}</p>
    </section>

    <section class="privacy-sections">
      <article v-for="section in privacy.sections" :key="section.heading" class="privacy-card">
        <h4>{{ section.heading }}</h4>
        <p>{{ section.text }}</p>
      </article>
    </section>

    <div class="privacy-actions">
      <button class="primary-button" type="button" @click="$emit('navigate', '/signup')">
        {{ common.backToSignUp }}
      </button>
      <button class="secondary-button" type="button" @click="$emit('navigate', '/signin')">
        {{ common.goToSignIn }}
      </button>
    </div>
  </AuthShell>
</template>

<script>
import AuthShell from '../components/AuthShell.vue';

export default {
  name: 'PrivacyView',
  components: {
    AuthShell
  },
  props: {
    isDark: {
      type: Boolean,
      default: false
    },
    language: {
      type: String,
      default: 'en'
    },
    translations: {
      type: Object,
      required: true
    }
  },
  emits: ['navigate', 'toggle-theme', 'update-language'],
  computed: {
    dictionary() {
      return this.translations[this.language] || this.translations.en;
    },
    common() {
      return this.dictionary.common;
    },
    hero() {
      return this.dictionary.hero;
    },
    privacy() {
      return this.dictionary.privacy;
    }
  }
};
</script>

<style scoped>
.page-header h3 {
  margin: 0;
  font-size: 1.7rem;
}

.page-header p {
  margin: 10px 0 0;
  color: rgba(91, 107, 123, 1);
  line-height: 1.7;
}

.privacy-sections {
  margin-top: 24px;
  display: grid;
  gap: 16px;
}

.privacy-card {
  padding: 20px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(34, 62, 79, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.privacy-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(22, 42, 59, 0.08);
}

.privacy-card h4 {
  margin: 0 0 10px;
  font-size: 1.05rem;
}

.privacy-card p {
  margin: 0;
  color: rgba(91, 107, 123, 1);
  line-height: 1.7;
}

.privacy-actions {
  margin-top: 24px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.primary-button,
.secondary-button {
  border: none;
  border-radius: 18px;
  padding: 14px 18px;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.primary-button {
  color: #fff;
  background: linear-gradient(135deg, #e45b5b, #d84848);
}

.secondary-button {
  color: inherit;
  background: rgba(45, 143, 123, 0.12);
}

.primary-button:hover,
.secondary-button:hover {
  transform: translateY(-1px);
}

:global(.dark-mode) .page-header p,
:global(.dark-mode) .privacy-card p {
  color: #b6c3ce;
}

:global(.dark-mode) .privacy-card {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(212, 230, 241, 0.12);
}

:global(.dark-mode) .secondary-button {
  background: rgba(45, 143, 123, 0.18);
}
</style>
