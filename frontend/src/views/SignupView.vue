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
  font-size: 1.7rem;
  color: inherit;
}

.page-header p {
  margin: 10px 0 0;
  color: rgba(91, 107, 123, 1);
  line-height: 1.6;
}

.auth-form {
  margin-top: 26px;
  animation: fade-up 0.55s ease;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-full {
  grid-column: 1 / -1;
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
  transition: border-color 0.2s, box-shadow 0.2s, background 0.2s;
}

.field input::placeholder {
  color: rgba(91, 107, 123, 0.8);
}

.field input:hover {
  border-color: rgba(45, 143, 123, 0.3);
}

.field input:focus {
  outline: none;
  border-color: rgba(45, 143, 123, 0.55);
  box-shadow: 0 0 0 4px rgba(45, 143, 123, 0.14);
}

.field input.invalid {
  border-color: rgba(228, 91, 91, 0.7);
  box-shadow: 0 0 0 4px rgba(228, 91, 91, 0.12);
}

.checkbox-row {
  margin-top: 18px;
}

.checkbox {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  color: rgba(91, 107, 123, 1);
  line-height: 1.5;
}

.checkbox input {
  margin-top: 3px;
}

.text-link {
  border: none;
  background: none;
  padding: 0;
  color: #2d8f7b;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.text-link:hover {
  color: #246f60;
  transform: translateY(-1px);
}

.primary-button {
  width: 100%;
  margin-top: 20px;
  border: none;
  border-radius: 18px;
  padding: 15px 18px;
  font: inherit;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #e45b5b, #d84848);
  cursor: pointer;
  box-shadow: 0 16px 30px rgba(228, 91, 91, 0.26);
  transition: transform 0.2s ease, box-shadow 0.2s ease, filter 0.2s ease;
}

.primary-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 20px 34px rgba(228, 91, 91, 0.3);
  filter: saturate(1.05);
}

.feedback {
  margin: 14px 0 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(45, 143, 123, 0.12);
  color: #1f6f60;
  font-weight: 600;
}

.feedback.error {
  background: rgba(228, 91, 91, 0.12);
  color: #b53f3f;
}

.auth-footer {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  color: rgba(91, 107, 123, 1);
}

.helper-panel {
  margin-top: 24px;
  padding: 20px;
  border-radius: 24px;
  background: rgba(45, 143, 123, 0.08);
  animation: fade-up 0.65s ease;
}

.helper-panel h4 {
  margin: 0 0 12px;
  font-size: 1rem;
}

.helper-panel ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 10px;
  color: rgba(91, 107, 123, 1);
}

@keyframes fade-up {
  from {
    opacity: 0;
    transform: translateY(14px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
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
}

:global(.dark-mode) .helper-panel {
  background: rgba(45, 143, 123, 0.12);
}

@media (max-width: 640px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .field-full {
    grid-column: auto;
  }
}
</style>
