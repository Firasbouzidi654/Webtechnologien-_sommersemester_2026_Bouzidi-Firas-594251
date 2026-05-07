<template>
  <AuthShell
    :common="common"
    :hero="hero"
    :is-dark="isDark"
    :language="language"
    :page="signup"
    @navigate="$emit('navigate', $event)"
    @toggle-theme="$emit('toggle-theme')"
    @update-language="$emit('update-language', $event)"
  >
    <section class="page-header">
      <h3>{{ signup.title }}</h3>
      <p>{{ signup.subtitle }}</p>
    </section>

    <form class="auth-form" @submit.prevent="submitForm">
      <div class="form-grid">
        <label class="field">
          <span>{{ signup.fullNameLabel }}</span>
          <input v-model.trim="form.fullName" type="text" :placeholder="signup.fullNamePlaceholder" required />
        </label>

        <label class="field">
          <span>{{ signup.emailLabel }}</span>
          <input v-model.trim="form.email" type="email" :placeholder="signup.emailPlaceholder" required />
        </label>

        <label class="field">
          <span>{{ signup.passwordLabel }}</span>
          <input
            v-model="form.password"
            :class="{ invalid: showPasswordError }"
            type="password"
            :placeholder="signup.passwordPlaceholder"
            minlength="8"
            required
          />
        </label>

        <label class="field">
          <span>{{ signup.confirmPasswordLabel }}</span>
          <input
            v-model="form.confirmPassword"
            :class="{ invalid: showPasswordError }"
            type="password"
            :placeholder="signup.confirmPasswordPlaceholder"
            minlength="8"
            required
          />
        </label>

        <label class="field field-full">
          <span>{{ signup.phoneLabel }}</span>
          <input v-model.trim="form.phoneNumber" type="tel" :placeholder="signup.phonePlaceholder" required />
        </label>
      </div>

      <div class="checkbox-row">
        <label class="checkbox">
          <input v-model="form.agreeToTerms" type="checkbox" required />
          <span>
            {{ signup.checkboxText }}
            <button class="text-link" type="button" @click="$emit('navigate', '/privacy')">
              {{ common.privacyLink }}
            </button>
          </span>
        </label>
      </div>

      <p v-if="feedback" class="feedback" :class="{ error: feedbackType === 'error' }">{{ feedback }}</p>

      <button class="primary-button" type="submit">{{ signup.submitButton }}</button>

      <footer class="auth-footer">
        <span>{{ signup.alternatePrompt }}</span>
        <button class="text-link" type="button" @click="$emit('navigate', '/signin')">
          {{ signup.alternateAction }}
        </button>
      </footer>
    </form>

    <aside class="helper-panel">
      <h4>{{ signup.helperTitle }}</h4>
      <ul>
        <li v-for="item in signup.helperItems" :key="item">{{ item }}</li>
      </ul>
    </aside>

    <FeaturesList :content="features" :is-dark="isDark" />
  </AuthShell>
</template>

<script>
import AuthShell from '../components/AuthShell.vue';
import FeaturesList from '../components/FeaturesList.vue';

export default {
  name: 'SignupView',
  components: {
    AuthShell,
    FeaturesList
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
      form: {
        fullName: '',
        email: '',
        password: '',
        confirmPassword: '',
        phoneNumber: '',
        agreeToTerms: false
      },
      feedback: '',
      feedbackType: 'success'
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
    signup() {
      return this.dictionary.signup;
    },
    features() {
      return this.dictionary.features;
    },
    showPasswordError() {
      return Boolean(this.form.password && this.form.confirmPassword && this.form.password !== this.form.confirmPassword);
    }
  },
  methods: {
    submitForm() {
      if (this.form.password !== this.form.confirmPassword) {
        this.feedback = this.signup.mismatchError;
        this.feedbackType = 'error';
        return;
      }

      this.feedback = this.signup.successMessage;
      this.feedbackType = 'success';
      console.log('Sign up form ready:', { ...this.form });
    }
  }
};
</script>

<style scoped>
.page-header h3 {
  margin: 0;
  font-size: 1.875rem;
  font-weight: 800;
  color: #2d3748;
  background: linear-gradient(135deg, #3182ce, #2d3748);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  margin: 12px 0 0;
  color: #4a5568;
  line-height: 1.6;
  font-weight: 500;
}

.auth-form {
  margin-top: 28px;
  animation: fade-up 0.55s ease;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 20px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.field-full {
  grid-column: 1 / -1;
}

.field span {
  font-size: 0.95rem;
  font-weight: 700;
  color: #2d3748;
}

.field input {
  width: 100%;
  box-sizing: border-box;
  border: 2px solid rgba(0, 0, 0, 0.1);
  border-radius: 12px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.9);
  color: #1a202c;
  font: inherit;
  font-weight: 500;
  transition: border-color 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;
}

.field input::placeholder {
  color: #a0aec0;
}

.field input:hover {
  border-color: rgba(49, 130, 206, 0.3);
}

.field input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 4px rgba(49, 130, 206, 0.1);
  background: #fff;
}

.field input.invalid {
  border-color: #e53e3e;
  box-shadow: 0 0 0 4px rgba(229, 62, 62, 0.1);
}

.checkbox-row {
  margin-top: 20px;
}

.checkbox {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  color: #4a5568;
  line-height: 1.6;
  font-weight: 500;
}

.checkbox input {
  margin-top: 4px;
  width: 18px;
  height: 18px;
}

.text-link {
  border: none;
  background: none;
  padding: 0;
  color: #3182ce;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.3s ease, transform 0.3s ease;
}

.text-link:hover {
  color: #2c5282;
  transform: translateY(-1px);
}

.primary-button {
  width: 100%;
  margin-top: 24px;
  border: none;
  border-radius: 14px;
  padding: 16px 20px;
  font: inherit;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, #38a169, #2f855a);
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(56, 161, 105, 0.3);
  transition: transform 0.3s ease, box-shadow 0.3s ease, filter 0.3s ease;
}

.primary-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(56, 161, 105, 0.4);
  filter: saturate(1.05);
}

.feedback {
  margin: 16px 0 0;
  padding: 14px 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e9f6f2, #c6f6df);
  color: #22543d;
  font-weight: 600;
}

.feedback.error {
  background: linear-gradient(135deg, #ffe3e3, #feb2b2);
  color: #742a2a;
}

.auth-footer {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  color: #4a5568;
  font-weight: 500;
}

.helper-panel {
  margin-top: 28px;
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(56, 161, 105, 0.08), rgba(49, 130, 206, 0.04));
  animation: fade-up 0.65s ease;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.helper-panel h4 {
  margin: 0 0 14px;
  font-size: 1.125rem;
  font-weight: 700;
  color: #2d3748;
}

.helper-panel ul {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: 12px;
  color: #4a5568;
  font-weight: 500;
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(16px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

:global(.dark-mode) .page-header h3 {
  background: linear-gradient(135deg, #63b3ed, #a0aec0);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:global(.dark-mode) .page-header p,
:global(.dark-mode) .checkbox,
:global(.dark-mode) .auth-footer,
:global(.dark-mode) .helper-panel ul,
:global(.dark-mode) .field input::placeholder {
  color: #b6c3ce;
}

:global(.dark-mode) .field input {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(212, 230, 241, 0.12);
  color: #e2e8f0;
}

:global(.dark-mode) .field input:focus {
  background: rgba(255, 255, 255, 0.1);
}

:global(.dark-mode) .helper-panel {
  background: linear-gradient(135deg, rgba(56, 161, 105, 0.12), rgba(49, 130, 206, 0.08));
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .field-full {
    grid-column: auto;
  }

  .page-header h3 {
    font-size: 1.5rem;
  }
}
</style>
