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
      <div class="role-select" role="group" :aria-label="signin.roleLabel">
        <p class="role-label">{{ signin.roleLabel }}</p>
        <div class="role-cards">
          <button
            v-for="option in roleOptions"
            :key="option.value"
            type="button"
            :class="['role-card', { active: role === option.value }]"
            :aria-pressed="role === option.value"
            @click="setRole(option.value)"
          >
            <span class="role-icon" aria-hidden="true">{{ option.initial }}</span>
            <span class="role-copy">
              <span class="role-title">{{ option.label }}</span>
              <span class="role-description">{{ option.description }}</span>
            </span>
          </button>
        </div>
      </div>

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

      <div class="signin-row">
        <label class="checkbox">
          <input v-model="form.rememberMe" type="checkbox" />
          <span>{{ signin.rememberMe }}</span>
        </label>

        <button class="text-link" type="button" @click="$emit('navigate', '/forgot-password')">
          {{ signin.forgotPassword }}
        </button>
      </div>

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

const fallbackContent = {
  common: {
    brandLead: 'Making health communication',
    brandAccent: 'feel safe, simple, and human.',
    languageLabel: 'Language',
    lightMode: 'Light mode',
    darkMode: 'Dark mode'
  },
  hero: {
    eyebrow: 'Trusted childcare communication',
    title: 'Keeping children safe through better communication',
    text: 'KinderCare Connect helps families and staff coordinate child wellbeing.',
    image: '',
    highlights: []
  },
  signin: {
    badge: 'Welcome back',
    title: 'Sign in to your account',
    subtitle: 'Access your Kids dashboard, messages, and care information.',
    emailLabel: 'Email address',
    emailPlaceholder: 'parent@example.com',
    passwordLabel: 'Password',
    passwordPlaceholder: 'Enter your password',
    rememberMe: 'Keep me signed in on this device',
    forgotPassword: 'Forgot password?',
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
    securityBadges: ['GDPR compliant', 'Encrypted medical data', 'Trusted childcare communication']
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
      role: 'parent',
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
    roleOptions() {
      return [
        {
          value: 'parent',
          initial: 'P',
          label: this.signin.parentRole,
          description: this.signin.parentRoleDescription
        },
        {
          value: 'admin',
          initial: 'S',
          label: this.signin.staffRole,
          description: this.signin.staffRoleDescription
        }
      ];
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
    setRole(role) {
      this.role = role;
      this.clearFeedback();
    },
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
    credentialsLookInvalid() {
      const email = (this.form.email || '').toLowerCase();
      const password = (this.form.password || '').toLowerCase();

      return email.includes('invalid') || password === 'invalid' || password === 'password';
    },
    submitForm() {
      if (this.isLoading) return;

      this.clearFeedback();

      if (!this.validateForm()) {
        this.rejectForm(this.signin.validationError || this.signin.invalidCredentials);
        return;
      }

      this.isLoading = true;

      window.setTimeout(() => {
        this.isLoading = false;

        if (this.credentialsLookInvalid()) {
          this.rejectForm(this.signin.invalidCredentials);
          return;
        }

        this.isSuccess = true;
        this.feedback = this.signin.successMessage;
        this.feedbackType = 'success';
        console.log('Sign in form ready:', { role: this.role, ...this.form });

        this.redirectTimer = window.setTimeout(() => {
          this.$emit('navigate', this.role === 'admin' ? '/admin' : '/parent');
        }, 650);
      }, 850);
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

.field label,
.role-label {
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

.role-select {
  display: grid;
  gap: 8px;
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  min-height: 52px;
  padding: 4px;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  background: color-mix(in srgb, var(--bg-card) 82%, transparent);
}

.role-card {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  min-width: 0;
  min-height: 44px;
  padding: 8px 12px;
  border-radius: 14px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  overflow: hidden;
  text-align: center;
  transition: all 0.25s ease;
}

.role-card:hover {
  transform: translateY(-1px);
  background: rgba(14, 165, 233, 0.08);
}

.role-card.active {
  border-color: transparent;
  background: linear-gradient(135deg, #0ea5e9 0%, #14b8a6 100%);
  color: #ffffff;
  box-shadow: 0 0 18px rgba(20, 184, 166, 0.25);
  transform: translateY(-1px);
}

.role-card:focus-visible,
.text-link:focus-visible,
.primary-button:focus-visible,
.password-toggle:focus-visible {
  outline: 2px solid rgba(45, 143, 123, 0.46);
  outline-offset: 3px;
}

.role-icon {
  flex: 0 0 auto;
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.12);
  color: currentColor;
  font-size: 0.76rem;
  font-weight: 900;
}

.role-copy {
  min-width: 0;
  display: block;
}

.role-title {
  color: currentColor;
  font-weight: 850;
}

.role-description {
  display: none;
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

.signin-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
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
:global(.dark-mode) .role-description,
:global(.dark-mode) .security-badges li {
  color: #b6c3ce;
}

:global(.dark-mode) .field label,
:global(.dark-mode) .role-label {
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

:global(.dark-mode) .role-card {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.08);
  color: #cbd5e1;
}

:global(.dark-mode) .role-card.active {
  background: linear-gradient(135deg, #0ea5e9 0%, #14b8a6 100%);
  border-color: transparent;
  color: #ffffff;
  box-shadow: 0 0 18px rgba(20, 184, 166, 0.25);
}

:global(.dark-mode) .role-title,
:global(.dark-mode) .role-icon {
  color: currentColor;
}

:global(.dark-mode) .role-icon {
  background: rgba(255, 255, 255, 0.12);
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

@media (max-width: 760px) {
  .role-cards {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .page-header h3 {
    font-size: 1.5rem;
  }

  .auth-form {
    gap: 14px;
  }

  .signin-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .role-card {
    min-height: 72px;
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
  .role-card,
  .password-toggle,
  .text-link,
  .primary-button,
  .security-badges li {
    transition: none;
  }
}
</style>
