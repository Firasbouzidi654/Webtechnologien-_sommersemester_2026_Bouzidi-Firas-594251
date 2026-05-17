<template>
  <AuthShell
    :common="common"
    :hero="hero"
    :is-dark="isDark"
    :language="language"
    :page="forgotPassword"
    @toggle-theme="$emit('toggle-theme')"
    @update-language="$emit('update-language', $event)"
  >
    <section class="page-header">
      <h3>{{ forgotPassword.title }}</h3>
      <p>{{ forgotPassword.subtitle }}</p>
    </section>

    <form class="reset-form" @submit.prevent="sendResetLink">
      <label class="field">
        <span>{{ forgotPassword.emailLabel }}</span>
        <input
          v-model.trim="email"
          type="email"
          :placeholder="forgotPassword.emailPlaceholder"
          autocomplete="email"
          required
        />
      </label>

      <Transition name="fade-slide">
        <p v-if="successMessage" class="success-message" role="status">
          {{ successMessage }}
        </p>
      </Transition>

      <button class="primary-button" type="submit">
        {{ forgotPassword.submitButton }}
      </button>

      <button class="secondary-button" type="button" @click="$emit('navigate', '/signin')">
        {{ forgotPassword.backToLogin }}
      </button>
    </form>
  </AuthShell>
</template>

<script>
import AuthShell from '../components/AuthShell.vue';

export default {
  name: 'ForgotPasswordView',
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
  data() {
    return {
      email: '',
      successMessage: ''
    };
  },
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
    forgotPassword() {
      return this.dictionary.forgotPassword || this.translations.en.forgotPassword;
    }
  },
  methods: {
    sendResetLink() {
      this.successMessage = this.forgotPassword.successMessage.replace('{email}', this.email);
    }
  }
};
</script>

<style scoped>
.page-header {
  animation: fade-in 0.36s ease both;
}

.page-header h3 {
  margin: 0;
  font-size: 1.7rem;
  line-height: 1.2;
}

.page-header p {
  margin: 10px 0 0;
  color: rgba(91, 107, 123, 1);
  line-height: 1.6;
}

.reset-form {
  margin-top: 26px;
  display: grid;
  gap: 16px;
  animation: fade-in 0.44s ease both;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field span {
  font-size: 0.92rem;
  font-weight: 700;
}

.field input {
  width: 100%;
  box-sizing: border-box;
  border: 1px solid rgba(34, 62, 79, 0.14);
  border-radius: 16px;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.78);
  color: inherit;
  font: inherit;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.field input:hover {
  border-color: rgba(45, 143, 123, 0.3);
}

.field input:focus {
  outline: none;
  border-color: rgba(45, 143, 123, 0.55);
  box-shadow: 0 0 0 4px rgba(45, 143, 123, 0.14);
}

.success-message {
  margin: 0;
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(45, 143, 123, 0.16), rgba(92, 184, 155, 0.12));
  border: 1px solid rgba(45, 143, 123, 0.24);
  color: #1f6f60;
  font-weight: 700;
  line-height: 1.5;
}

.primary-button,
.secondary-button {
  width: 100%;
  border: none;
  border-radius: 18px;
  padding: 15px 18px;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.primary-button {
  color: #fff;
  background: linear-gradient(135deg, #2d8f7b, #246f60);
  box-shadow: 0 16px 30px rgba(45, 143, 123, 0.26);
}

.secondary-button {
  color: #246f60;
  background: rgba(45, 143, 123, 0.12);
}

.primary-button:hover,
.secondary-button:hover {
  transform: translateY(-1px);
}

.primary-button:focus,
.secondary-button:focus {
  outline: 2px solid rgba(45, 143, 123, 0.35);
  outline-offset: 3px;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.24s ease, transform 0.24s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:global(.dark-mode) .page-header p {
  color: #b6c3ce;
}

:global(.dark-mode) .field input {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(212, 230, 241, 0.12);
}

:global(.dark-mode) .success-message {
  background: rgba(45, 143, 123, 0.18);
  border-color: rgba(120, 214, 188, 0.2);
  color: #9ee3d1;
}

:global(.dark-mode) .secondary-button {
  color: #9ee3d1;
  background: rgba(45, 143, 123, 0.18);
}

@media (max-width: 640px) {
  .page-header h3 {
    font-size: 1.5rem;
  }

  .reset-form {
    margin-top: 22px;
  }
}
</style>
