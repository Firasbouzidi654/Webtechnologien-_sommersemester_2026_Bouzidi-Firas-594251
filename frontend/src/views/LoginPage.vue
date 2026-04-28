<template>
  <main class="login-page">
    <section class="login-panel">
      <p class="eyebrow">KinderCare Connect</p>
      <h1>Simple health handover for kindergarten teams and parents</h1>
      <p class="intro">
        A calm prototype for child profiles, medication schedules, emergency contacts, and daily staff confirmations.
      </p>

      <form @submit.prevent="login">
        <label>
          <span>Email</span>
          <input v-model.trim="email" type="email" placeholder="parent@example.com" required />
        </label>
        <label>
          <span>Password</span>
          <input v-model="password" type="password" placeholder="prototype" required />
        </label>
        <label>
          <span>Role</span>
          <select v-model="role">
            <option value="parent">Parent</option>
            <option value="admin">Kindergarten staff / administrator</option>
          </select>
        </label>

        <button type="submit">Open dashboard</button>
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
    radial-gradient(circle at 18% 14%, rgba(255, 232, 168, 0.56), transparent 32%),
    linear-gradient(135deg, rgba(45, 143, 123, 0.16), rgba(231, 240, 255, 0.72)),
    #f7faf8;
}

.login-panel,
.visual-panel {
  overflow: hidden;
  border: 1px solid rgba(32, 48, 63, 0.12);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 22px 54px rgba(32, 48, 63, 0.13);
}

.login-panel {
  padding: 34px;
}

.visual-panel img {
  display: block;
  width: 100%;
  height: min(42vh, 360px);
  object-fit: cover;
}

.preview-content {
  padding: 28px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #287b68;
  font-size: 0.8rem;
  font-weight: 900;
  text-transform: uppercase;
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
}

h2 {
  font-size: 1.45rem;
}

.intro {
  max-width: 620px;
  margin-top: 16px;
  color: #536577;
  font-size: 1.05rem;
}

form {
  display: grid;
  gap: 16px;
  max-width: 520px;
  margin-top: 28px;
}

label {
  display: grid;
  gap: 7px;
  color: #20303f;
  font-weight: 800;
}

input,
select {
  width: 100%;
  border: 1px solid rgba(32, 48, 63, 0.14);
  border-radius: 8px;
  padding: 13px 14px;
  background: #fff;
  color: #20303f;
}

button {
  min-height: 48px;
  border: none;
  border-radius: 8px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #2d8f7b, #246f60);
  color: #fff;
  font-weight: 900;
  cursor: pointer;
}

ul {
  display: grid;
  gap: 12px;
  margin: 18px 0 0;
  padding-left: 20px;
  color: #536577;
}

@media (max-width: 860px) {
  .login-page {
    grid-template-columns: 1fr;
    padding: 22px;
  }
}
</style>
