<template>
  <main class="login-page">
    <section class="login-panel">
      <p class="eyebrow">KinderCare Connect</p>
      <h1>Simple health handover for kindergarten teams and parents</h1>
      <p class="intro">
        A calm prototype for child profiles, medication schedules, emergency contacts, and daily staff confirmations.
      </p>

      <form @submit.prevent="login" class="auth-card">
        <label>
          <span>Email</span>
          <input v-model.trim="email" type="email" placeholder="parent@example.com" required />
        </label>

        <label>
          <span>Password</span>
          <input v-model="password" type="password" placeholder="prototype" required />
        </label>

        <div class="role-select">
          <p class="role-label">Continue as</p>
          <div class="role-cards">
            <button
              type="button"
              :class="['role-card', { active: role === 'parent' }]"
              @click="role = 'parent'"
              aria-pressed="role === 'parent'"
            >
              <div class="icon">👪</div>
              <div class="text">Parent</div>
            </button>

            <button
              type="button"
              :class="['role-card', { active: role === 'admin' }]"
              @click="role = 'admin'"
              aria-pressed="role === 'admin'"
            >
              <div class="icon">🩺</div>
              <div class="text">Staff</div>
            </button>
          </div>
        </div>

        <div class="actions">
          <button class="primary" type="submit">Open dashboard</button>
        </div>
      </form>
    </section>

    <aside class="visual-panel">
      <img :src="heroImage" alt="KinderCare Connect dashboard preview" />
      <section class="preview-content">
        <p class="eyebrow">First prototype</p>
        <h2>Ready to demo</h2>
        <ul>
          <li>Role-based redirect after mock login</li>
          <li>Unique medication IDs for QR verification</li>
          <li>Admin calendar, reminders, and missed alerts</li>
        </ul>
      </section>
    </aside>
  </main>
</template>

<script>
import heroImage from '../assets/hero.png';

export default {
  name: 'LoginPage',
  emits: ['navigate'],
  data() {
    return {
      heroImage,
      email: 'parent@example.com',
      password: 'prototype',
      role: 'parent'
    };
  },
  methods: {
    login() {
      // Redirect to the selected role dashboard
      this.$emit('navigate', this.role === 'parent' ? '/parent' : '/admin');
    }
  }
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 32px;
  align-items: center;
  padding: 48px;
  background:
    linear-gradient(135deg, #f0f4f8 0%, #e1e8ed 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.login-panel,
.visual-panel {
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(10px);
}

.login-panel {
  padding: 40px;
}

.visual-panel img {
  display: block;
  width: 100%;
  height: min(42vh, 360px);
  object-fit: cover;
  border-radius: 16px 16px 0 0;
}

.preview-content {
  padding: 32px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #3182ce;
  font-size: 0.875rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

h1,
h2,
p {
  margin: 0;
}

h1 {
  max-width: 720px;
  font-size: clamp(2rem, 5vw, 4rem);
  line-height: 1.02;
  font-weight: 800;
  background: linear-gradient(135deg, #3182ce, #2d3748);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

h2 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #2d3748;
}

.intro {
  max-width: 620px;
  margin-top: 16px;
  color: #4a5568;
  font-size: 1.125rem;
  line-height: 1.6;
  font-weight: 500;
}

form {
  display: grid;
  gap: 20px;
  max-width: 520px;
  margin-top: 24px;
}

.auth-card {
  padding: 18px 0 0;
}

.role-select {
  margin-top: 2px;
}

.role-label {
  margin: 0 0 8px;
  font-weight: 800;
  color: #2d3748;
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
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 8px 12px;
  border-radius: 14px;
  border: 1px solid transparent;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.25s ease;
}

.role-card .icon {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 999px;
  background: rgba(14, 165, 233, 0.12);
  color: currentColor;
  font-size: 0.9rem;
}

.role-card.active {
  transform: translateY(-1px);
  background: linear-gradient(135deg, #0ea5e9 0%, #14b8a6 100%);
  color: #ffffff;
  box-shadow: 0 0 18px rgba(20, 184, 166, 0.25);
}

.role-card:hover {
  transform: translateY(-1px);
}

.role-card .text {
  font-weight: 850;
}

.actions {
  display: flex;
  justify-content: flex-end;
}

.primary {
  min-height: 50px;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, #3182ce, #2d8f7b);
  color: #fff;
  font-weight: 800;
  cursor: pointer;
}

label {
  display: grid;
  gap: 8px;
  color: var(--text-primary);
  font-weight: 600;
}

input,
select {
  width: 100%;
  border: 2px solid var(--border-color);
  border-radius: 10px;
  padding: 14px 16px;
  background: var(--bg-card);
  color: var(--text-primary);
  font-weight: 500;
  transition: border-color 0.3s ease;
}

input:focus,
select:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

button {
  min-height: 50px;
  border: none;
  border-radius: 12px;
  padding: 14px 20px;
  background: linear-gradient(135deg, #38a169, #2f855a);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(56, 161, 105, 0.3);
}

button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(56, 161, 105, 0.4);
}

ul {
  display: grid;
  gap: 12px;
  margin: 20px 0 0;
  padding-left: 20px;
  color: #4a5568;
  font-weight: 500;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .login-panel {
    padding: 32px;
  }

  .preview-content {
    padding: 24px;
  }
}
</style>
