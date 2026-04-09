<template>
  <div class="signup-container">
    <aside class="playground-aside">
      <div class="visual-content">
        <div class="play-icons">
          <span class="sun">☀️</span>
          <span class="blocks">🧱</span>
        </div>
        <h2>Welcome to the fun zone!</h2>
        <p>Your child's health journey starts with play and safety.</p>
      </div>
      <div class="visual-overlay"></div>
    </aside>

    <main class="form-section">
      <div class="form-card">
        <header class="form-header">
          <h1 class="brand-title">
            <span class="red-text">KinderCare</span>
            <span class="green-text">Connect</span>
            <br><br>
          </h1>
          <p class="brand-tagline">
            <span class="green-text">Making health communication</span>
            <br>
            <span class="red-text">feel like child's play!</span>
          </p>
        </header>

        <form @submit.prevent="submitForm" class="signup-form">
          <div class="form-grid">
            <div class="input-wrapper">
              <label for="fullName">Parent's Full Name</label>
              <input id="fullName" v-model="fullName" type="text" placeholder="e.g., Papa Bear" required />
            </div>

            <div class="input-wrapper">
              <label for="email">Email Address</label>
              <input id="email" v-model="email" type="email" placeholder="email@example.com" required />
            </div>

            <div class="input-wrapper">
              <label for="password">Password</label>
              <input id="password" v-model="password" type="password" placeholder="••••••••" required />
            </div>

            <div class="input-wrapper">
              <label for="confirmPassword">Confirm Password</label>
              <input id="confirmPassword" v-model="confirmPassword" type="password" placeholder="••••••••" required />
            </div>

            <div class="input-wrapper full-width">
              <label for="phoneNumber">Emergency Number</label>
              <input id="phoneNumber" v-model="phoneNumber" type="tel" placeholder="+49 176 12345678" required />
            </div>
          </div>

          <div class="options-row">
            <label class="custom-checkbox">
              <input type="checkbox" v-model="agreeToTerms" required />
              <span class="label-text">I accept the <a href="#">Fun Rules</a> & <a href="#">Privacy</a></span>
            </label>
          </div>

          <button type="submit" class="primary-btn">Join KinderCare!</button>

          <footer class="form-footer">
            <span>Already a member?</span>
            <a href="#" class="login-link">Sign In</a>
          </footer>
        </form>
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'SignupView',
  data() {
    return {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
      phoneNumber: '',
      agreeToTerms: false
    };
  },
  methods: {
    submitForm() {
      if (this.password !== this.confirmPassword) {
        alert("Oh no! Passwords don't match like friends!");
        return;
      }
      console.log("Form Submitted:", { ...this.$data });
    }
  }
};
</script>

<style scoped>
/* COULEURS ET VARIABLES */
:root {
  --red: #e63946;
  --green: #2a9d8f;
  --bg-light: #f0fff4;
}

/* Classes pour les couleurs de texte demandées */
.red-text { color: #e63946; }
.green-text { color: #2a9d8f; }

.signup-container {
  display: flex;
  min-height: 100vh;
  background-color: #f0fff4;
  width: 100%;
  overflow-x: hidden;
}

/* PANNEAU GAUCHE */
.playground-aside {
  flex: 1;
  position: relative;
  background-image: url('https://images.unsplash.com/photo-1596464716127-f2a82984de30?auto=format&fit=crop&w=1000&q=80');
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.visual-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(230, 57, 70, 0.8), rgba(42, 157, 143, 0.7));
}

.visual-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  padding: 30px;
  border-radius: 20px;
}

/* SECTION FORMULAIRE */
.form-section {
  flex: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.form-card {
  width: 100%;
  max-width: 500px; /* Limite la largeur pour éviter le débordement */
  background: white;
  padding: 40px;
  border-radius: 25px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
  box-sizing: border-box; /* RÈGLE MAGIQUE : force tout à rester dedans */
}

.brand-title {
  font-size: 2.5rem;
  font-weight: 900;
  text-align: center;
  margin-bottom: 10px;
  transform: rotate(-1deg);
}

.brand-tagline {
  text-align: center;
  margin-bottom: 30px;
  font-weight: 700;
  font-size: 1rem;
}

/* CORRECTION DES CARREAUX (INPUTS) */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px; /* Espacement entre les carreaux */
  width: 100%;
}

.full-width {
  grid-column: span 2;
}

.input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.input-wrapper label {
  font-size: 0.85rem;
  font-weight: 700;
  color: #1e3a8a;
  margin-left: 10px;
}

.input-wrapper input {
  width: 100%; /* S'adapte à la colonne */
  padding: 12px 20px;
  border-radius: 50px;
  border: 2px solid #e2e8f0;
  box-sizing: border-box; /* EMPÊCHE DE SORTIR DU CADRE */
  transition: all 0.3s;
}

.input-wrapper input:focus {
  outline: none;
  border-color: #2a9d8f;
  box-shadow: 0 0 0 4px rgba(42, 157, 143, 0.1);
}

/* BOUTON ET FOOTER */
.primary-btn {
  width: 100%;
  padding: 15px;
  background: #e63946;
  color: white;
  border: none;
  border-radius: 50px;
  font-weight: 800;
  font-size: 1.1rem;
  margin-top: 25px;
  cursor: pointer;
  transition: 0.3s;
}

.primary-btn:hover {
  background: #c12b36;
  transform: translateY(-2px);
}

.form-footer {
  margin-top: 20px;
  text-align: center;
  font-size: 0.9rem;
}

.login-link {
  color: #2a9d8f;
  font-weight: 800;
  text-decoration: none;
}

/* RESPONSIVE */
@media (max-width: 900px) {
  .playground-aside { display: none; }
  .form-grid { grid-template-columns: 1fr; }
  .full-width { grid-column: span 1; }
}
</style>