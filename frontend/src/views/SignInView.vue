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

    <form class="auth-form" @submit.prevent="submitForm">
      <label class="field">
        <span>{{ signin.emailLabel }}</span>
        <input v-model.trim="form.email" type="email" :placeholder="signin.emailPlaceholder" required />
      </label>

      <label class="field">
        <span>{{ signin.passwordLabel }}</span>
        <input v-model="form.password" type="password" :placeholder="signin.passwordPlaceholder" required />
      </label>

      <div class="signin-row">
        <label class="checkbox">
          <input v-model="form.rememberMe" type="checkbox" />
          <span>{{ signin.rememberMe }}</span>
        </label>

        <button class="text-link" type="button" @click="$emit('navigate', '/privacy')">
          {{ signin.forgotPassword }}
        </button>
      </div>

      <p v-if="feedback" class="feedback">{{ feedback }}</p>

      <button class="primary-button" type="submit">{{ signin.submitButton }}</button>

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
      feedback: ''
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
    signin() {
      return this.dictionary.signin;
    },
    features() {
      return this.dictionary.features;
    }
  },
  methods: {
    submitForm() {
      this.feedback = this.signin.successMessage;
      console.log('Sign in form ready:', { ...this.form });
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
  line-height: 1.6;
}

.auth-form {
  margin-top: 24px;
  display: grid;
  gap: 16px;
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
}

.field input:focus {
  outline: none;
  border-color: rgba(45, 143, 123, 0.55);
  box-shadow: 0 0 0 4px rgba(45, 143, 123, 0.14);
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

.text-link {
  border: none;
  background: none;
  padding: 0;
  color: #2d8f7b;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.feedback {
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(45, 143, 123, 0.12);
  color: #1f6f60;
  font-weight: 600;
}

.primary-button {
  width: 100%;
  border: none;
  border-radius: 18px;
  padding: 15px 18px;
  font: inherit;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #2d8f7b, #246f60);
  cursor: pointer;
  box-shadow: 0 16px 30px rgba(45, 143, 123, 0.26);
}

.auth-footer {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  color: rgba(91, 107, 123, 1);
}

:global(.dark-mode) .page-header p,
:global(.dark-mode) .checkbox,
:global(.dark-mode) .auth-footer {
  color: #b6c3ce;
}

:global(.dark-mode) .field input {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(212, 230, 241, 0.12);
}
</style>
