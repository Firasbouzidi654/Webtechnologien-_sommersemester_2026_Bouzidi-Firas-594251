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

    <form
      class="auth-form"
      :class="{ 'form-rejected': formWasRejected, 'form-success': isSuccess }"
      novalidate
      :aria-busy="isLoading ? 'true' : 'false'"
      @submit.prevent="submitForm"
    >
      <div class="role-select" role="group" aria-label="Register as">
        <p class="role-label">Register as</p>
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
            </span>
          </button>
        </div>
      </div>

      <div class="form-grid">
        <div class="field" :class="fieldClass('fullName')">
          <label for="signup-full-name">{{ signup.fullNameLabel }}</label>
          <input
            id="signup-full-name"
            v-model.trim="form.fullName"
            type="text"
            :placeholder="signup.fullNamePlaceholder"
            autocomplete="name"
            :aria-invalid="Boolean(errors.fullName)"
            aria-describedby="signup-full-name-error"
            @input="handleFieldInput('fullName')"
            @blur="validateField('fullName')"
          />
          <Transition name="field-message">
            <p v-if="errors.fullName" id="signup-full-name-error" class="field-error">{{ errors.fullName }}</p>
          </Transition>
        </div>

        <div class="field" :class="fieldClass('email')">
          <label for="signup-email">{{ signup.emailLabel }}</label>
          <input
            id="signup-email"
            v-model.trim="form.email"
            type="email"
            :placeholder="signup.emailPlaceholder"
            autocomplete="email"
            :aria-invalid="Boolean(errors.email)"
            aria-describedby="signup-email-error"
            @input="handleFieldInput('email')"
            @blur="validateField('email')"
          />
          <Transition name="field-message">
            <p v-if="errors.email" id="signup-email-error" class="field-error">{{ errors.email }}</p>
          </Transition>
        </div>

        <div class="field password-field" :class="fieldClass('password')">
          <label for="signup-password">{{ signup.passwordLabel }}</label>
          <div class="password-control">
            <input
              id="signup-password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              :placeholder="signup.passwordPlaceholder"
              autocomplete="new-password"
              :aria-invalid="Boolean(errors.password)"
              aria-describedby="signup-password-error signup-password-strength signup-caps-warning"
              @input="handlePasswordInput"
              @blur="handlePasswordBlur"
              @keydown="updateCapsLock"
              @keyup="updateCapsLock"
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showPassword ? signup.hidePassword : signup.showPassword"
              @click="showPassword = !showPassword"
            >
              <span class="eye-icon" :class="{ hidden: showPassword }" aria-hidden="true"></span>
            </button>
          </div>

          <div id="signup-password-strength" class="password-meter" :class="`strength-${passwordStrength.level}`" aria-live="polite">
            <span class="meter-track">
              <span class="meter-fill"></span>
            </span>
            <span class="meter-label">{{ passwordStrength.label }}</span>
          </div>

          <Transition name="field-message">
            <p v-if="capsLockOn" id="signup-caps-warning" class="field-hint">{{ signup.capsLockWarning }}</p>
          </Transition>
          <Transition name="field-message">
            <p v-if="errors.password" id="signup-password-error" class="field-error">{{ errors.password }}</p>
          </Transition>
        </div>

        <div class="field password-field" :class="fieldClass('confirmPassword')">
          <label for="signup-confirm-password">{{ signup.confirmPasswordLabel }}</label>
          <div class="password-control">
            <input
              id="signup-confirm-password"
              v-model="form.confirmPassword"
              :type="showConfirmPassword ? 'text' : 'password'"
              :placeholder="signup.confirmPasswordPlaceholder"
              autocomplete="new-password"
              :aria-invalid="Boolean(errors.confirmPassword)"
              aria-describedby="signup-confirm-password-error"
              @input="handleFieldInput('confirmPassword')"
              @blur="validateField('confirmPassword')"
            />
            <button
              class="password-toggle"
              type="button"
              :aria-label="showConfirmPassword ? signup.hidePassword : signup.showPassword"
              @click="showConfirmPassword = !showConfirmPassword"
            >
              <span class="eye-icon" :class="{ hidden: showConfirmPassword }" aria-hidden="true"></span>
            </button>
          </div>
          <Transition name="field-message">
            <p v-if="errors.confirmPassword" id="signup-confirm-password-error" class="field-error">{{ errors.confirmPassword }}</p>
          </Transition>
        </div>

      </div>

      <section class="password-helper" :aria-label="signup.passwordHelperTitle">
        <div class="helper-heading">
          <p>{{ signup.passwordHelperTitle }}</p>
          <span>{{ passwordStrength.label }}</span>
        </div>
        <ul>
          <li v-for="item in passwordRequirements" :key="item.key" :class="{ complete: item.valid }">
            <span class="requirement-dot" aria-hidden="true"></span>
            <span>{{ item.label }}</span>
          </li>
        </ul>
      </section>

      <div class="checkbox-row">
        <label class="checkbox" :class="{ invalid: errors.agreeToTerms }">
          <input
            v-model="form.agreeToTerms"
            type="checkbox"
            :aria-invalid="Boolean(errors.agreeToTerms)"
            aria-describedby="signup-terms-error"
            @change="handleFieldInput('agreeToTerms')"
          />
          <span>
            {{ signup.checkboxText }}
            <router-link custom to="/privacy" v-slot="{ navigate }">
              <button class="text-link" type="button" @click="navigate">
                {{ common.privacyLink }}
              </button>
            </router-link>
          </span>
        </label>
        <Transition name="field-message">
          <p v-if="errors.agreeToTerms" id="signup-terms-error" class="field-error">{{ errors.agreeToTerms }}</p>
        </Transition>
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

      <footer class="auth-footer">
        <span>{{ signup.alternatePrompt }}</span>
        <router-link custom to="/signin" v-slot="{ navigate }">
          <button class="text-link" type="button" @click="navigate">
            {{ signup.alternateAction }}
          </button>
        </router-link>
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
import { signup } from '../state/authStore';

const fallbackSignup = {
  badge: 'Create account',
  title: 'Register for KinderCare Connect',
  subtitle: 'Create a secure family account for childcare health coordination.',
  fullNameLabel: 'Full name',
  fullNamePlaceholder: 'e.g. Sofia Becker',
  emailLabel: 'Email address',
  emailPlaceholder: 'parent@example.com',
  passwordLabel: 'Password',
  passwordPlaceholder: 'At least 8 characters',
  confirmPasswordLabel: 'Confirm password',
  confirmPasswordPlaceholder: 'Repeat your password',
  checkboxText: 'I agree to the',
  submitButton: 'Create account',
  loadingButton: 'Creating secure account',
  successButton: 'Account ready',
  alternatePrompt: 'Already have an account?',
  alternateAction: 'Sign in',
  successMessage: 'Your account details passed validation and are ready for secure registration.',
  validationError: 'Please review the highlighted registration details.',
  fullNameRequired: 'Enter your full name.',
  emailRequired: 'Enter your email address.',
  emailInvalid: 'Enter a valid email address.',
  passwordRequired: 'Create a password.',
  passwordTooWeak: 'Use a stronger password before continuing.',
  confirmPasswordRequired: 'Confirm your password.',
  mismatchError: 'The passwords do not match yet.',
  termsRequired: 'Please accept the privacy policy to continue.',
  showPassword: 'Show password',
  hidePassword: 'Hide password',
  capsLockWarning: 'Caps Lock is on.',
  strengthEmpty: 'Password strength',
  strengthWeak: 'Weak',
  strengthMedium: 'Medium',
  strengthStrong: 'Strong',
  passwordHelperTitle: 'Password requirements',
  passwordRequirementLength: 'At least 8 characters',
  passwordRequirementCase: 'Uppercase and lowercase letters',
  passwordRequirementNumber: 'At least one number',
  passwordRequirementSpecial: 'A symbol or 14+ characters',
  passwordRequirementMatch: 'Passwords match',
  helperTitle: 'Why families use it',
  helperItems: []
};

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
      role: 'PARENT',
      form: {
        fullName: '',
        email: '',
        password: '',
        confirmPassword: '',
        agreeToTerms: false
      },
      errors: {},
      touched: {},
      feedback: '',
      feedbackType: '',
      isLoading: false,
      isSuccess: false,
      formWasRejected: false,
      showPassword: false,
      showConfirmPassword: false,
      capsLockOn: false,
      rejectionTimer: null,
      successTimer: null,
      redirectTimer: null
    };
  },
  computed: {
    dictionary() {
      return (this.translations && (this.translations[this.language] || this.translations.en)) || {};
    },
    common() {
      return this.dictionary.common || {};
    },
    hero() {
      return this.dictionary.hero || {};
    },
    signup() {
      return { ...fallbackSignup, ...(this.dictionary.signup || {}) };
    },
    features() {
      return this.dictionary.features || { title: 'Key features', items: [] };
    },
    passwordChecks() {
      const password = this.form.password || '';

      return {
        length: password.length >= 8,
        case: /[a-z]/.test(password) && /[A-Z]/.test(password),
        number: /\d/.test(password),
        special: /[^A-Za-z0-9]/.test(password) || password.length >= 14,
        match: Boolean(password && this.form.confirmPassword && password === this.form.confirmPassword)
      };
    },
    passwordRequirements() {
      return [
        { key: 'length', label: this.signup.passwordRequirementLength, valid: this.passwordChecks.length },
        { key: 'case', label: this.signup.passwordRequirementCase, valid: this.passwordChecks.case },
        { key: 'number', label: this.signup.passwordRequirementNumber, valid: this.passwordChecks.number },
        { key: 'special', label: this.signup.passwordRequirementSpecial, valid: this.passwordChecks.special },
        { key: 'match', label: this.signup.passwordRequirementMatch, valid: this.passwordChecks.match }
      ];
    },
    passwordStrength() {
      const password = this.form.password || '';
      if (!password) return { level: 'empty', score: 0, label: this.signup.strengthEmpty };

      const score = ['length', 'case', 'number', 'special'].reduce((total, key) => {
        return total + (this.passwordChecks[key] ? 1 : 0);
      }, 0);

      if (score >= 4) return { level: 'strong', score, label: this.signup.strengthStrong };
      if (score >= 3) return { level: 'medium', score, label: this.signup.strengthMedium };
      return { level: 'weak', score, label: this.signup.strengthWeak };
    },
    submitButtonLabel() {
      if (this.isLoading) return this.signup.loadingButton;
      if (this.isSuccess) return this.signup.successButton;
      return this.signup.submitButton;
    },
    roleOptions() {
      return [
        { value: 'PARENT', initial: 'P', label: 'Parent', description: 'Family dashboard' },
        { value: 'STAFF', initial: 'S', label: 'Staff', description: 'Care team workspace' }
      ];
    }
  },
  beforeUnmount() {
    window.clearTimeout(this.rejectionTimer);
    window.clearTimeout(this.successTimer);
    window.clearTimeout(this.redirectTimer);
  },
  methods: {
    fieldClass(field) {
      return {
        invalid: Boolean(this.errors[field]),
        valid: Boolean(this.touched[field] && !this.errors[field] && this.form[field])
      };
    },
    clearFeedback() {
      this.feedback = '';
      this.feedbackType = '';
      this.isSuccess = false;
    },
    handleFieldInput(field) {
      this.touched = { ...this.touched, [field]: true };
      this.clearFeedback();
      this.validateField(field);

      if (field === 'password' && this.touched.confirmPassword) {
        this.validateField('confirmPassword');
      }
    },
    handlePasswordInput() {
      this.handleFieldInput('password');

      if (this.touched.confirmPassword) {
        this.validateField('confirmPassword');
      }
    },
    handlePasswordBlur() {
      this.capsLockOn = false;
      this.validateField('password');
    },
    updateCapsLock(event) {
      this.capsLockOn = Boolean(event.getModifierState && event.getModifierState('CapsLock'));
    },
    getFieldError(field) {
      const value = this.form[field];

      if (field === 'fullName') {
        if (!value) return this.signup.fullNameRequired;
        if (value.trim().split(/\s+/).length < 2) return this.signup.fullNameRequired;
      }

      if (field === 'email') {
        if (!value) return this.signup.emailRequired;
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) return this.signup.emailInvalid;
      }

      if (field === 'password') {
        if (!value) return this.signup.passwordRequired;
        if (this.passwordStrength.level === 'weak') return this.signup.passwordTooWeak;
      }

      if (field === 'confirmPassword') {
        if (!value) return this.signup.confirmPasswordRequired;
        if (value !== this.form.password) return this.signup.mismatchError;
      }

      if (field === 'agreeToTerms' && !value) {
        return this.signup.termsRequired;
      }

      return '';
    },
    validateField(field) {
      this.touched = { ...this.touched, [field]: true };
      const message = this.getFieldError(field);
      this.errors = { ...this.errors, [field]: message };
      return !message;
    },
    validateForm() {
      const fields = ['fullName', 'email', 'password', 'confirmPassword', 'agreeToTerms'];
      const nextErrors = {};
      const nextTouched = {};

      fields.forEach((field) => {
        nextTouched[field] = true;
        nextErrors[field] = this.getFieldError(field);
      });

      this.touched = nextTouched;
      this.errors = nextErrors;
      return fields.every((field) => !nextErrors[field]);
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
    setRole(role) {
      this.role = role;
      this.clearFeedback();
    },
    async submitForm() {
      if (this.isLoading) return;

      this.clearFeedback();

      if (!this.validateForm()) {
        this.rejectForm(this.signup.validationError);
        return;
      }

      this.isLoading = true;

      try {
        const user = await signup(
          this.form.fullName,
          this.form.email,
          this.form.password,
          this.role
        );

        this.isSuccess = true;
        this.feedback = this.signup.successMessage;
        this.feedbackType = 'success';

        const role = (user.role || '').toUpperCase();
        const destination = (role === 'ADMIN' || role === 'STAFF') ? '/admin' : '/parent';

        this.redirectTimer = window.setTimeout(() => {
          this.$emit('navigate', destination);
        }, 650);
      } catch (err) {
        this.rejectForm(err.message || this.signup.validationError);
      } finally {
        this.isLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.page-header {
  animation: fade-up 0.42s ease both;
}

.page-header h3 {
  margin: 0;
  font-size: 1.75rem;
  line-height: 1.2;
  font-weight: 850;
  color: var(--color-text-primary);
  background: linear-gradient(135deg, var(--color-brand), var(--color-success));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-header p {
  margin: 10px 0 0;
  color: var(--color-text-secondary);
  line-height: 1.6;
  font-weight: 500;
}

.auth-form {
  margin-top: 26px;
  display: grid;
  gap: 18px;
  animation: fade-up 0.52s ease both;
}

.role-select {
  display: grid;
  gap: 8px;
}

.role-label {
  margin: 0;
  font-size: 0.92rem;
  font-weight: 800;
  color: var(--color-text-primary);
}

.role-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
  min-height: 52px;
  padding: 4px;
  border: 1px solid rgba(34, 62, 79, 0.14);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.58);
}

.role-card {
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
  color: var(--color-text-secondary);
  cursor: pointer;
  text-align: center;
  transition: all 0.25s ease;
}

.role-card:hover {
  transform: translateY(-1px);
  background: rgba(45, 143, 123, 0.08);
}

.role-card.active {
  border-color: transparent;
  background: linear-gradient(135deg, #2d8f7b, #246f60);
  color: #ffffff;
  box-shadow: 0 0 18px rgba(45, 143, 123, 0.25);
  transform: translateY(-1px);
}

.role-icon {
  flex: 0 0 auto;
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: rgba(45, 143, 123, 0.12);
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

.auth-form.form-rejected {
  animation: form-shake 0.38s ease both;
}

.auth-form.form-success {
  animation: form-confirm 0.58s ease both;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.field label {
  font-size: 0.92rem;
  font-weight: 800;
  color: var(--color-text-primary);
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
  font-weight: 500;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.field input::placeholder {
  color: var(--color-text-tertiary);
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

.field.invalid input,
.checkbox.invalid input {
  border-color: rgba(228, 91, 91, 0.72);
  box-shadow: 0 0 0 4px rgba(228, 91, 91, 0.12);
}

.field.valid input {
  border-color: rgba(45, 143, 123, 0.36);
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

.password-toggle:focus-visible,
.text-link:focus-visible,
.primary-button:focus-visible {
  outline: 2px solid rgba(45, 143, 123, 0.46);
  outline-offset: 3px;
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

.password-meter {
  display: flex;
  align-items: center;
  gap: 10px;
}

.meter-track {
  position: relative;
  flex: 1;
  height: 7px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(34, 62, 79, 0.1);
}

.meter-fill {
  position: absolute;
  inset: 0 auto 0 0;
  width: 0%;
  border-radius: inherit;
  background: #d7dde2;
  transition: width 0.28s ease, background 0.28s ease;
}

.strength-weak .meter-fill {
  width: 34%;
  background: #e45b5b;
}

.strength-medium .meter-fill {
  width: 67%;
  background: #f0a83a;
}

.strength-strong .meter-fill {
  width: 100%;
  background: #2d8f7b;
}

.meter-label {
  min-width: 98px;
  color: var(--color-text-secondary);
  font-size: 0.78rem;
  font-weight: 850;
  text-align: right;
  transition: color 0.24s ease;
}

.strength-weak .meter-label {
  color: #b54040;
}

.strength-medium .meter-label {
  color: #925812;
}

.strength-strong .meter-label {
  color: #1f6f60;
}

.password-helper,
.helper-panel {
  border: 1px solid rgba(34, 62, 79, 0.1);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 10px 26px rgba(22, 42, 59, 0.06);
}

.password-helper {
  padding: 16px;
}

.helper-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.helper-heading p,
.helper-heading span {
  margin: 0;
  font-size: 0.84rem;
  font-weight: 850;
}

.helper-heading p {
  color: var(--color-text-primary);
}

.helper-heading span {
  color: var(--color-text-secondary);
}

.password-helper ul {
  list-style: none;
  margin: 12px 0 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px 12px;
}

.password-helper li {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-secondary);
  font-size: 0.82rem;
  font-weight: 650;
  line-height: 1.35;
  transition: color 0.2s ease, transform 0.2s ease;
}

.password-helper li.complete {
  color: #1f6f60;
}

.requirement-dot {
  width: 9px;
  height: 9px;
  flex: 0 0 auto;
  border-radius: 999px;
  background: rgba(34, 62, 79, 0.22);
  box-shadow: inset 0 0 0 1px rgba(34, 62, 79, 0.06);
  transition: background 0.2s ease, transform 0.2s ease;
}

.password-helper li.complete .requirement-dot {
  background: #2d8f7b;
  transform: scale(1.12);
}

.checkbox-row {
  display: grid;
  gap: 8px;
}

.checkbox {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  color: var(--color-text-secondary);
  line-height: 1.55;
  font-weight: 500;
}

.checkbox input {
  margin-top: 4px;
  width: 19px;
  height: 19px;
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

.feedback {
  margin: 0;
  padding: 13px 15px;
  border-radius: 14px;
  background: rgba(45, 143, 123, 0.12);
  color: #1f6f60;
  font-weight: 750;
  line-height: 1.45;
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
  box-shadow: 0 16px 30px rgba(45, 143, 123, 0.24);
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

.auth-footer {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.helper-panel {
  margin-top: 24px;
  padding: 20px;
  animation: fade-up 0.62s ease both;
}

.helper-panel h4 {
  margin: 0 0 12px;
  font-size: 1rem;
  font-weight: 800;
  color: var(--color-text-primary);
}

.helper-panel ul {
  margin: 0;
  padding-left: 18px;
  display: grid;
  gap: 9px;
  color: var(--color-text-secondary);
  font-size: 0.92rem;
  font-weight: 500;
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
:global(.dark-mode) .meter-label,
:global(.dark-mode) .password-helper li,
:global(.dark-mode) .checkbox,
:global(.dark-mode) .auth-footer,
:global(.dark-mode) .helper-panel ul,
:global(.dark-mode) .helper-heading span {
  color: #b6c3ce;
}

:global(.dark-mode) .field label,
:global(.dark-mode) .helper-heading p,
:global(.dark-mode) .helper-panel h4 {
  color: #edf4f8;
}

:global(.dark-mode) .field input,
:global(.dark-mode) .password-helper,
:global(.dark-mode) .helper-panel,
:global(.dark-mode) .role-cards {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(212, 230, 241, 0.12);
}

:global(.dark-mode) .field input:hover,
:global(.dark-mode) .field input:focus {
  background: rgba(255, 255, 255, 0.08);
}

:global(.dark-mode) .password-toggle {
  background: rgba(45, 143, 123, 0.16);
}

:global(.dark-mode) .eye-icon {
  border-color: #9ee3d1;
}

:global(.dark-mode) .eye-icon::before,
:global(.dark-mode) .eye-icon.hidden::after {
  background: #9ee3d1;
}

:global(.dark-mode) .password-helper li.complete,
:global(.dark-mode) .strength-strong .meter-label,
:global(.dark-mode) .feedback {
  color: #9ee3d1;
}

:global(.dark-mode) .feedback {
  background: rgba(45, 143, 123, 0.18);
}

:global(.dark-mode) .feedback.error {
  background: rgba(228, 91, 91, 0.18);
  color: #ffb4b4;
}

:global(.dark-mode) .field-error {
  color: #ffb4b4;
}

:global(.dark-mode) .field-hint,
:global(.dark-mode) .strength-medium .meter-label {
  color: #f3cf8d;
}

:global(.dark-mode) .strength-weak .meter-label {
  color: #ffb4b4;
}

@media (max-width: 760px) {
  .form-grid,
  .password-helper ul {
    grid-template-columns: 1fr;
  }

}

@media (max-width: 640px) {
  .page-header h3 {
    font-size: 1.38rem;
  }

  .page-header p {
    margin-top: 7px;
    font-size: 0.92rem;
    line-height: 1.45;
  }

  .auth-form {
    margin-top: 16px;
    gap: 12px;
  }

  .form-grid {
    gap: 12px;
  }

  .field {
    gap: 6px;
  }

  .field input {
    border-radius: 13px;
    padding: 12px 14px;
  }

  .role-cards {
    min-height: 46px;
  }

  .role-card {
    min-height: 40px;
    padding: 7px 10px;
  }

  .primary-button {
    min-height: 48px;
    border-radius: 15px;
    padding: 13px 16px;
  }

  .password-meter,
  .helper-heading {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .meter-track {
    width: 100%;
    flex: none;
  }

  .meter-label {
    min-width: 0;
    text-align: left;
  }
}

@media (max-width: 430px) {
  .page-header h3 {
    font-size: 1.28rem;
  }

  .page-header p {
    font-size: 0.86rem;
  }

  .auth-form {
    margin-top: 12px;
    gap: 10px;
  }

  .role-select {
    gap: 6px;
  }

  .role-card {
    gap: 6px;
    font-size: 0.82rem;
  }

  .role-icon {
    width: 22px;
    height: 22px;
  }

  .field label,
  .field-error,
  .field-hint,
  .checkbox,
  .auth-footer,
  .helper-heading p,
  .helper-heading span,
  .password-helper li {
    font-size: 0.82rem;
  }

  .field input {
    padding: 10px 12px;
  }

  .password-control input {
    padding-right: 48px;
  }

  .password-toggle {
    right: 6px;
    width: 36px;
    height: 36px;
  }

  .password-helper,
  .helper-panel {
    border-radius: 14px;
    padding: 12px;
  }

  .password-helper ul {
    margin-top: 8px;
    gap: 6px;
  }

  .helper-panel {
    margin-top: 14px;
  }

  .helper-panel ul {
    gap: 6px;
    font-size: 0.82rem;
  }
}

@media (prefers-reduced-motion: reduce) {
  .page-header,
  .auth-form,
  .auth-form.form-rejected,
  .auth-form.form-success,
  .helper-panel,
  .spinner {
    animation: none;
  }

  .field input,
  .password-toggle,
  .text-link,
  .primary-button,
  .meter-fill,
  .meter-label,
  .password-helper li,
  .requirement-dot {
    transition: none;
  }
}
</style>
