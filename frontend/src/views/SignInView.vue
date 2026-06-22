<template>
  <AuthShell
    :common="common"
    :hero="hero"
    :is-dark="isDark"
    :language="language"
    :page="signin"
    @navigate="$emit('navigate', $event)"
    @toggle-theme="$emit('toggle-theme')"
    @update-language="$emit('update-language', $event)"
  >
    <section class="page-header">
      <h3>{{ signin.title }}</h3>
      <p>{{ signin.subtitle }}</p>
    </section>

    <form
      class="auth-form"
      :class="{ 'form-rejected': formWasRejected, 'form-success': isSuccess }"
      novalidate
      :aria-busy="isLoading ? 'true' : 'false'"
      @submit.prevent="submitForm"
    >
      <div class="field" :class="{ invalid: errors.email }">
        <label for="signin-email">{{ signin.emailLabel }}</label>
        <input
          id="signin-email"
          v-model.trim="form.email"
          type="email"
          :placeholder="signin.emailPlaceholder"
          autocomplete="email"
          :aria-invalid="Boolean(errors.email)"
          aria-describedby="signin-email-error"
          @input="clearFieldError('email')"
          @blur="validateField('email')"
        />
        <Transition name="field-message">
          <p v-if="errors.email" id="signin-email-error" class="field-error">{{ errors.email }}</p>
        </Transition>
      </div>

      <div class="field password-field" :class="{ invalid: errors.password }">
        <label for="signin-password">{{ signin.passwordLabel }}</label>
        <div class="password-control">
          <input
            id="signin-password"
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            :placeholder="signin.passwordPlaceholder"
            autocomplete="current-password"
            :aria-invalid="Boolean(errors.password)"
            aria-describedby="signin-password-error signin-caps-warning"
            @input="clearFieldError('password')"
            @blur="handlePasswordBlur"
            @keydown="updateCapsLock"
            @keyup="updateCapsLock"
          />
          <button
            class="password-toggle"
            type="button"
            :aria-label="showPassword ? signin.hidePassword : signin.showPassword"
            @click="showPassword = !showPassword"
          >
            <span class="eye-icon" :class="{ hidden: showPassword }" aria-hidden="true"></span>
          </button>
        </div>

        <Transition name="field-message">
          <p v-if="capsLockOn" id="signin-caps-warning" class="field-hint">{{ signin.capsLockWarning }}</p>
        </Transition>
        <Transition name="field-message">
          <p v-if="errors.password" id="signin-password-error" class="field-error">{{ errors.password }}</p>
        </Transition>
      </div>

      <label class="checkbox">
        <input v-model="form.rememberMe" type="checkbox" />
        <span>{{ signin.rememberMe }}</span>
      </label>

      <Transition name="form-feedback">
        <p v-if="feedback" class="feedback" :class="feedbackType" :role="feedbackType === 'error' ? 'alert' : 'status'">
          {{ feedback }}
        </p>
      </Transition>

      <button class="primary-button" type="submit" :disabled="isLoading">
        <span v-if="isLoading" class="spinner" aria-hidden="true"></span>
        <span v-else-if="isSuccess" class="success-check" aria-hidden="true"></span>
        <span>{{ submitButtonLabel }}</span>
      </button>

      <ul class="security-badges" :aria-label="signin.securityTitle">
        <li v-for="badge in securityBadges" :key="badge">{{ badge }}</li>
      </ul>

      <footer class="auth-footer">
        <span>{{ signin.alternatePrompt }}</span>
        <button class="text-link" type="button" @click="$emit('navigate', '/signup')">
          {{ signin.alternateAction }}
        </button>
      </footer>
    </form>

    <FeaturesList :content="features" :is-dark="isDark" />
  </AuthShell>
</template>

<script>
import AuthShell from '../components/AuthShell.vue';
import FeaturesList from '../components/FeaturesList.vue';
import { login } from '../state/authStore';

const fallbackContent = {
  common: {
    brandLead: 'Making health information',
    brandAccent: 'feel safe, simple, and human.',
    languageLabel: 'Language',
    lightMode: 'Light mode',
    darkMode: 'Dark mode'
  },
  hero: {
    eyebrow: 'Child health overview',
    title: 'A clear view of child health and medication',
    text: 'KinderCare Connect helps families and staff coordinate child wellbeing.',
    image: '',
    highlights: []
  },
  signin: {
    badge: 'Welcome back',
    title: 'Sign in to your account',
    subtitle: 'Access the child and medication dashboards.',
    emailLabel: 'Email address',
    emailPlaceholder: 'parent@example.com',
    passwordLabel: 'Password',
    passwordPlaceholder: 'Enter your password',
    rememberMe: 'Keep me signed in on this device',
    submitButton: 'Sign in',
    loadingButton: 'Checking credentials',
    successButton: 'Opening dashboard',
    alternatePrompt: 'New to KinderCare Connect?',
    alternateAction: 'Create an account',
    successMessage: 'Sign-in verified. Opening your dashboard.',
    invalidCredentials: 'We could not sign you in. Please check your email and password.',
    validationError: 'Please review the highlighted sign-in details.',
    emailRequired: 'Enter the email linked to your account.',
    emailInvalid: 'Enter a valid email address.',
    passwordRequired: 'Enter your password.',
    passwordTooShort: 'Use at least 8 characters.',
    showPassword: 'Show password',
    hidePassword: 'Hide password',
    capsLockWarning: 'Caps Lock is on.',
    roleLabel: 'Continue as',
    parentRole: 'Parent',
    parentRoleDescription: 'Family dashboard',
    staffRole: 'Staff',
    staffRoleDescription: 'Care team workspace',
    securityTitle: 'Security indicators',
    securityBadges: ['Password-protected account', 'PostgreSQL data storage', 'Child and medication overview']
  },
  features: {
    title: 'Key features',
    items: []
  }
};

export default {
  name: 'SignInView',
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
        email: '',
        password: '',
        rememberMe: false
      },
      errors: {},
      feedback: '',
      feedbackType: '',
      isLoading: false,
      isSuccess: false,
      formWasRejected: false,
      showPassword: false,
      capsLockOn: false,
      redirectTimer: null,
      rejectionTimer: null
    };
  },
  computed: {
    dictionary() {
      return (this.translations && (this.translations[this.language] || this.translations.en)) || fallbackContent;
    },
    common() {
      return { ...fallbackContent.common, ...(this.dictionary.common || {}) };
    },
    hero() {
      return { ...fallbackContent.hero, ...(this.dictionary.hero || {}) };
    },
    signin() {
      return { ...fallbackContent.signin, ...(this.dictionary.signin || {}) };
    },
    features() {
      return { ...fallbackContent.features, ...(this.dictionary.features || {}) };
    },
    securityBadges() {
      return Array.isArray(this.signin.securityBadges) ? this.signin.securityBadges : fallbackContent.signin.securityBadges;
    },
    submitButtonLabel() {
      if (this.isLoading) return this.signin.loadingButton;
      if (this.isSuccess) return this.signin.successButton;
      return this.signin.submitButton;
    }
  },
  beforeUnmount() {
    window.clearTimeout(this.redirectTimer);
    window.clearTimeout(this.rejectionTimer);
  },
  methods: {
    clearFieldError(field) {
      if (this.errors[field]) {
        this.errors = { ...this.errors, [field]: '' };
      }
      this.clearFeedback();
    },
    clearFeedback() {
      this.feedback = '';
      this.feedbackType = '';
      this.isSuccess = false;
    },
    updateCapsLock(event) {
      this.capsLockOn = Boolean(event.getModifierState && event.getModifierState('CapsLock'));
    },
    getFieldError(field) {
      const email = this.form.email || '';
      const password = this.form.password || '';

      if (field === 'email') {
        if (!email) return this.signin.emailRequired;
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return this.signin.emailInvalid;
      }

      if (field === 'password') {
        if (!password) return this.signin.passwordRequired;
        if (password.length < 8) return this.signin.passwordTooShort;
      }

      return '';
    },
    validateField(field) {
      const message = this.getFieldError(field);
      this.errors = { ...this.errors, [field]: message };
      return !message;
    },
    handlePasswordBlur() {
      this.capsLockOn = false;
      this.validateField('password');
    },
    validateForm() {
      const nextErrors = {
        email: this.getFieldError('email'),
        password: this.getFieldError('password')
      };

      this.errors = nextErrors;
      return !nextErrors.email && !nextErrors.password;
    },
    rejectForm(message) {
      window.clearTimeout(this.rejectionTimer);
      this.formWasRejected = false;
      this.$nextTick(() => {
        this.formWasRejected = true;
        this.feedback = message;
        this.feedbackType = 'error';
        this.rejectionTimer = window.setTimeout(() => {
          this.formWasRejected = false;
        }, 420);
      });
    },
    async submitForm() {
      if (this.isLoading) return;

      this.clearFeedback();

      if (!this.validateForm()) {
        this.rejectForm(this.signin.validationError || this.signin.invalidCredentials);
        return;
      }

      this.isLoading = true;

      try {
        const user = await login(this.form.email, this.form.password);

        this.isSuccess = true;
        this.feedback = this.signin.successMessage;
        this.feedbackType = 'success';

        const role = (user.role || '').toUpperCase();
        const destination = (role === 'ADMIN' || role === 'STAFF') ? '/admin' : '/parent';

        this.redirectTimer = window.setTimeout(() => {
          this.$emit('navigate', destination);
        }, 650);
      } catch (err) {
        this.rejectForm(err.message || this.signin.invalidCredentials);
      } finally {
        this.isLoading = false;
      }
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

.auth-form {
  margin-top: 24px;
  display: grid;
  gap: 16px;
  animation: fade-in 0.44s ease both;
}

.auth-form.form-rejected {
  animation: form-shake 0.38s ease both;
}

.auth-form.form-success {
  animation: form-confirm 0.58s ease both;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field label {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 800;
  color: rgba(34, 62, 79, 0.88);
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
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.field input:hover {
  border-color: rgba(45, 143, 123, 0.28);
  background: rgba(255, 255, 255, 0.9);
}

.field input:focus {
  outline: none;
  border-color: rgba(45, 143, 123, 0.62);
  box-shadow: 0 0 0 4px rgba(45, 143, 123, 0.14), 0 12px 22px rgba(22, 42, 59, 0.08);
  transform: translateY(-1px);
}

.field.invalid input {
  border-color: rgba(228, 91, 91, 0.72);
  box-shadow: 0 0 0 4px rgba(228, 91, 91, 0.12);
}

.field-error,
.field-hint {
  margin: 0;
  font-size: 0.84rem;
  line-height: 1.4;
}

.field-error {
  color: #b54040;
  font-weight: 700;
}

.field-hint {
  color: #8a6338;
  font-weight: 700;
}

.text-link:focus-visible,
.primary-button:focus-visible,
.password-toggle:focus-visible {
  outline: 2px solid rgba(45, 143, 123, 0.46);
  outline-offset: 3px;
}

.password-control {
  position: relative;
}

.password-control input {
  padding-right: 54px;
}

.password-toggle {
  position: absolute;
  top: 50%;
  right: 8px;
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border: none;
  border-radius: 999px;
  background: rgba(45, 143, 123, 0.1);
  cursor: pointer;
  transform: translateY(-50%);
  transition: background 0.2s ease, transform 0.2s ease;
}

.password-toggle:hover {
  background: rgba(45, 143, 123, 0.16);
  transform: translateY(-50%) scale(1.04);
}

.eye-icon {
  position: relative;
  width: 18px;
  height: 12px;
  border: 2px solid #246f60;
  border-radius: 999px 999px 999px 999px / 75% 75% 75% 75%;
}

.eye-icon::before {
  content: "";
  position: absolute;
  top: 50%;
  left: 50%;
  width: 5px;
  height: 5px;
  border-radius: 999px;
  background: #246f60;
  transform: translate(-50%, -50%);
}

.eye-icon.hidden::after {
  content: "";
  position: absolute;
  top: 50%;
  left: -4px;
  width: 26px;
  height: 2px;
  border-radius: 999px;
  background: #246f60;
  transform: rotate(-35deg);
}

.checkbox {
  display: flex;
  gap: 10px;
  align-items: center;
  color: rgba(91, 107, 123, 1);
}

.checkbox input {
  width: 18px;
  height: 18px;
  accent-color: #2d8f7b;
}

.text-link {
  border: none;
  background: none;
  padding: 0;
  color: #2d8f7b;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.text-link:hover {
  color: #246f60;
  transform: translateY(-1px);
}

.feedback {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  font-weight: 700;
  line-height: 1.45;
}

.feedback.success {
  background: rgba(45, 143, 123, 0.12);
  color: #1f6f60;
}

.feedback.error {
  background: rgba(228, 91, 91, 0.12);
  color: #a53636;
}

.primary-button {
  width: 100%;
  min-height: 52px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border: none;
  border-radius: 18px;
  padding: 15px 18px;
  font: inherit;
  font-weight: 850;
  color: #fff;
  background: linear-gradient(135deg, #2d8f7b, #246f60);
  cursor: pointer;
  box-shadow: 0 16px 30px rgba(45, 143, 123, 0.26);
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease, opacity 0.18s ease;
}

.primary-button:hover:not(:disabled) {
  filter: saturate(1.06);
  transform: translateY(-2px);
  box-shadow: 0 20px 38px rgba(45, 143, 123, 0.3);
}

.primary-button:active:not(:disabled) {
  transform: translateY(0) scale(0.99);
}

.primary-button:disabled {
  cursor: wait;
  opacity: 0.82;
}

.spinner {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.42);
  border-top-color: #fff;
  animation: spin 0.72s linear infinite;
}

.success-check {
  width: 18px;
  height: 18px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  position: relative;
}

.success-check::after {
  content: "";
  position: absolute;
  left: 5px;
  top: 3px;
  width: 5px;
  height: 9px;
  border: solid #fff;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.security-badges {
  list-style: none;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: center;
  margin: 0;
  padding: 0;
}

.security-badges li {
  border: 1px solid rgba(34, 62, 79, 0.1);
  border-radius: 999px;
  padding: 8px 10px;
  background: rgba(255, 255, 255, 0.58);
  color: rgba(68, 82, 95, 0.95);
  font-size: 0.74rem;
  font-weight: 800;
}

.auth-footer {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  color: rgba(91, 107, 123, 1);
}

.field-message-enter-active,
.field-message-leave-active,
.form-feedback-enter-active,
.form-feedback-leave-active {
  transition: opacity 0.22s ease, transform 0.22s ease;
}

.field-message-enter-from,
.field-message-leave-to,
.form-feedback-enter-from,
.form-feedback-leave-to {
  opacity: 0;
  transform: translateY(-6px);
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

@keyframes form-shake {
  0%,
  100% {
    transform: translateX(0);
  }

  25% {
    transform: translateX(-7px);
  }

  50% {
    transform: translateX(6px);
  }

  75% {
    transform: translateX(-3px);
  }
}

@keyframes form-confirm {
  0% {
    transform: translateY(0);
  }

  45% {
    transform: translateY(-3px);
  }

  100% {
    transform: translateY(0);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

:global(.dark-mode) .page-header p,
:global(.dark-mode) .checkbox,
:global(.dark-mode) .auth-footer,
:global(.dark-mode) .security-badges li {
  color: #b6c3ce;
}

:global(.dark-mode) .field label {
  color: #edf4f8;
}

:global(.dark-mode) .field input {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(212, 230, 241, 0.12);
}

:global(.dark-mode) .field input:hover,
:global(.dark-mode) .field input:focus {
  background: rgba(255, 255, 255, 0.08);
}

:global(.dark-mode) .password-toggle {
  background: rgba(45, 143, 123, 0.16);
  border-color: rgba(212, 230, 241, 0.12);
}

:global(.dark-mode) .eye-icon {
  border-color: #9ee3d1;
}

:global(.dark-mode) .eye-icon::before,
:global(.dark-mode) .eye-icon.hidden::after {
  background: #9ee3d1;
}

:global(.dark-mode) .security-badges li {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(212, 230, 241, 0.13);
  color: #9ee3d1;
}

:global(.dark-mode) .feedback.success {
  background: rgba(45, 143, 123, 0.18);
  color: #9ee3d1;
}

:global(.dark-mode) .feedback.error {
  background: rgba(228, 91, 91, 0.18);
  color: #ffb4b4;
}

:global(.dark-mode) .field-error {
  color: #ffb4b4;
}

:global(.dark-mode) .field-hint {
  color: #f3cf8d;
}

@media (max-width: 640px) {
  .page-header h3 {
    font-size: 1.5rem;
  }

  .auth-form {
    gap: 14px;
  }

  .security-badges {
    justify-content: flex-start;
  }
}

@media (prefers-reduced-motion: reduce) {
  .page-header,
  .auth-form,
  .auth-form.form-rejected,
  .auth-form.form-success,
  .spinner {
    animation: none;
  }

  .field input,
  .password-toggle,
  .text-link,
  .primary-button,
  .security-badges li {
    transition: none;
  }
}
</style>
