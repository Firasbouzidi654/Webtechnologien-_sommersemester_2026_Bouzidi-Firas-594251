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
  color: var(--color-text-primary);
  background: linear-gradient(135deg, var(--color-brand), var(--color-brand-dark));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  margin: 12px 0 0;
  color: var(--color-text-secondary);
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
  color: var(--color-text-primary);
}

.field input {
  width: 100%;
  box-sizing: border-box;
  border: 2px solid var(--color-border);
  border-radius: 12px;
  padding: 16px;
  background: var(--color-bg-secondary);
  color: var(--color-text-primary);
  font: inherit;
  font-weight: 500;
  transition: border-color 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;
}

.field input::placeholder {
  color: var(--color-text-tertiary);
}

.field input:hover {
  border-color: rgba(49, 130, 206, 0.3);
}

.field input:focus {
  outline: none;
  border-color: var(--color-brand);
  box-shadow: 0 0 0 4px rgba(49, 130, 206, 0.08);
  background: var(--color-bg-secondary);
}

.field input.invalid {
  border-color: var(--color-missed-text);
  box-shadow: 0 0 0 4px rgba(229, 62, 62, 0.08);
}

.checkbox-row {
  margin-top: 20px;
}

.checkbox {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  color: var(--color-text-secondary);
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
  color: var(--color-brand);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
  transition: color 0.3s ease, transform 0.3s ease;
}

.text-link:hover {
  color: var(--color-info);
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
  background: linear-gradient(135deg, var(--color-success), #2f855a);
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(56, 161, 105, 0.18);
  transition: transform 0.3s ease, box-shadow 0.3s ease, filter 0.3s ease;
}

.primary-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(56, 161, 105, 0.25);
  filter: saturate(1.05);
}

.feedback {
  margin: 16px 0 0;
  padding: 14px 16px;
  border-radius: 12px;
  background: var(--color-taken);
  color: var(--color-taken-text);
  font-weight: 600;
}

.feedback.error {
  background: var(--color-missed);
  color: var(--color-missed-text);
}

.auth-footer {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.helper-panel {
  margin-top: 28px;
  padding: 24px;
  border-radius: 16px;
  background: var(--color-bg-tertiary);
  animation: fade-up 0.65s ease;
  box-shadow: var(--shadow-sm);
}

.helper-panel h4 {
  margin: 0 0 14px;
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text-primary);
}

.helper-panel ul {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: 12px;
  color: var(--color-text-secondary);
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
