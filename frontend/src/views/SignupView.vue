<template>
  <div class="signup-container" :class="{ 'dark-mode': isDark, 'lang-de': language === 'de' }">
    <!-- Dark Mode Toggle Button -->
    <button @click="toggleDarkMode" class="dark-mode-toggle">
      {{ isDark ? 'â˜€ï¸' : 'ðŸŒ™' }}
    </button>

    <!-- Language Dropdown -->
    <select @change="toggleLanguage" v-model="language" class="lang-dropdown">
      <option value="en">ðŸ‡ºðŸ‡¸ English</option>
      <option value="de">ðŸ‡©ðŸ‡ª Deutsch</option>
    </select>

    <aside class="playground-aside">
      <div class="visual-content">
        <div class="play-icons">
          <span class="sun">â˜€ï¸</span>
          <span class="blocks">ðŸ§±</span>
        </div>
        <h2>{{ t('welcomeTitle') }}</h2>
        <p>{{ t('welcomeText') }}</p>
      </div>
      <div class="visual-overlay"></div>
    </aside>

    <main class="form-section">
      <div class="form-card">
        <header class="form-header">
          <h1 class="brand-title">
            <span class="red-text">KinderCare</span>
            <span class="green-text">Connect</span>
          </h1>
          <p class="brand-tagline">
            <span class="green-text">{{ t('tagline1') }}</span>
            <br>
            <span class="red-text">{{ t('tagline2') }}</span>
          </p>
        </header>

        <form @submit.prevent="submitForm" class="signup-form">
          <div class="form-grid">
            <div class="input-wrapper">
              <label for="fullName">{{ t('fullNameLabel') }}</label>
              <input id="fullName" v-model="fullName" type="text" :placeholder="t('fullNamePlaceholder')" required />
            </div>

            <div class="input-wrapper">
              <label for="email">{{ t('emailLabel') }}</label>
              <input id="email" v-model="email" type="email" :placeholder="t('emailPlaceholder')" required />
            </div>

            <div class="input-wrapper">
              <label for="password">{{ t('passwordLabel') }}</label>
              <input id="password" v-model="password" type="password" :placeholder="t('passwordPlaceholder')" required />
            </div>

            <div class="input-wrapper">
              <label for="confirmPassword">{{ t('confirmPasswordLabel') }}</label>
              <input id="confirmPassword" v-model="confirmPassword" type="password" :placeholder="t('confirmPasswordPlaceholder')" required />
            </div>

            <div class="input-wrapper full-width">
              <label for="phoneNumber">{{ t('phoneLabel') }}</label>
              <input id="phoneNumber" v-model="phoneNumber" type="tel" :placeholder="t('phonePlaceholder')" required />
            </div>
          </div>

          <div class="options-row">
            <label class="custom-checkbox">
              <input type="checkbox" v-model="agreeToTerms" required />
              <span class="label-text">{{ t('agreeText') }} <a href="#" @click.prevent="showPrivacy = true">{{ t('privacyLink') }}</a></span>
            </label>
          </div>

          <button type="submit" class="primary-btn">{{ t('submitButton') }}</button>

          <footer class="form-footer">
            <span>{{ t('alreadyMember') }}</span>
            <a href="#" class="login-link">{{ t('signInLink') }}</a>
          </footer>
        </form>

        <FeaturesList :language="language" />
      </div>
    </main>

    <!-- Privacy Modal -->
    <div v-if="showPrivacy" class="modal-overlay" @click="showPrivacy = false">
      <div class="modal-content" @click.stop>
        <h2>{{ t('privacyTitle') }}</h2>
        <p>{{ t('privacyText1') }}</p>
        <p>{{ t('privacyText2') }}</p>
        <button @click="showPrivacy = false" class="close-btn">{{ t('closeButton') }}</button>
      </div>
    </div>
  </div>
</template>

<script>
import FeaturesList from '../components/FeaturesList.vue';

export default {
  name: 'SignupView',
  components: {
    FeaturesList
  },
  data() {
    return {
      fullName: '',
      email: '',
      password: '',
      confirmPassword: '',
      phoneNumber: '',
      agreeToTerms: false,
      isDark: false,
      showPrivacy: false,
      language: 'en', // Default language
      translations: {
        en: {
          welcomeTitle: 'Welcome to the fun zone!',
          welcomeText: 'Your child\'s health journey starts with play and safety.',
          tagline1: 'Making health communication',
          tagline2: 'feel like child\'s play!',
          fullNameLabel: 'Parent\'s Full Name',
          fullNamePlaceholder: 'e.g., Papa Bear',
          emailLabel: 'Email Address',
          emailPlaceholder: 'email@example.com',
          passwordLabel: 'Password',
          passwordPlaceholder: 'â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢',
          confirmPasswordLabel: 'Confirm Password',
          confirmPasswordPlaceholder: 'â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢',
          phoneLabel: 'Emergency Number',
          phonePlaceholder: '+49 176 12345678',
          agreeText: 'I accept the',
          privacyLink: 'Privacy',
          submitButton: 'Join KinderCare!',
          alreadyMember: 'Already a member?',
          signInLink: 'Sign In',
          privacyTitle: 'Privacy Policy',
          privacyText1: 'This is the privacy policy for KinderCare Connect. We respect your privacy and are committed to protecting your personal information.',
          privacyText2: 'Details about data collection, usage, and your rights...',
          closeButton: 'Close'
        },
        de: {
          welcomeTitle: 'Willkommen in der SpaÃŸzone!',
          welcomeText: 'Die Gesundheitsreise Ihres Kindes beginnt mit Spiel und Sicherheit.',
          tagline1: 'Gesundheitskommunikation machen',
          tagline2: 'wie Kinderspiel!',
          fullNameLabel: 'Name des Elternteils',
          fullNamePlaceholder: 'z.B., Papa BÃ¤r',
          emailLabel: 'E-Mail-Adresse',
          emailPlaceholder: 'email@beispiel.com',
          passwordLabel: 'Passwort',
          passwordPlaceholder: 'â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢',
          confirmPasswordLabel: 'Passwort bestÃ¤tigen',
          confirmPasswordPlaceholder: 'â€¢â€¢â€¢â€¢â€¢â€¢â€¢â€¢',
          phoneLabel: 'Notfallnummer',
          phonePlaceholder: '+49 176 12345678',
          agreeText: 'Ich akzeptiere die',
          privacyLink: 'Datenschutz',
          submitButton: 'Anmelden',
          alreadyMember: 'Bereits Mitglied?',
          signInLink: 'Anmelden',
          privacyTitle: 'Datenschutzrichtlinie',
          privacyText1: 'Dies ist die Datenschutzrichtlinie fÃ¼r KinderCare Connect. Wir respektieren Ihre PrivatsphÃ¤re und verpflichten uns, Ihre persÃ¶nlichen Informationen zu schÃ¼tzen.',
          privacyText2: 'Details zur Datenerfassung, Nutzung und Ihren Rechten...',
          closeButton: 'SchlieÃŸen'
        }
      }
    };
  },
  methods: {
    submitForm() {
      const errorMsg = this.language === 'en' ? "Oh no! Passwords don't match like friends!" : "Oh nein! PasswÃ¶rter passen nicht zusammen wie Freunde!";
      if (this.password !== this.confirmPassword) {
        alert(errorMsg);
        return;
      }
      console.log("Form Submitted:", { ...this.$data });
    },
    toggleDarkMode() {
      this.isDark = !this.isDark;
    },
    toggleLanguage() {
      this.language = this.language === 'en' ? 'de' : 'en';
    },
    t(key) {
      return this.translations[this.language][key] || key;
    }
  }
};
</script>

<style scoped>
/* Colors and Variables */
:root {
  --red: #e63946;
  --green: #2a9d8f;
  --bg-light: #f0fff4;
  --bg-dark: #121212;
  --text-dark: #e0e0e0;
}

/* Global styles for full screen */
:global(html), :global(body) {
  margin: 0;
  padding: 0;
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
}

/* Classes for requested text colors */
.red-text { color: #e63946; }
.green-text { color: #2a9d8f; }

.signup-container {
  display: flex;
  min-height: 100vh;
  background-color: #f0fff4;
  width: 100%;
  overflow-x: hidden;
  position: relative;
}

/* Dark Mode Toggle Button */
.dark-mode-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  font-size: 1.5rem;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  transition: background 0.3s;
  z-index: 10;
}

.dark-mode-toggle:hover {
  background: rgba(255, 255, 255, 1);
}

/* Language Dropdown */
.lang-dropdown {
  position: absolute;
  top: 20px;
  right: 80px;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 50px;
  padding: 10px 20px;
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  transition: background 0.3s;
  z-index: 10;
}

.lang-dropdown:hover {
  background: rgba(255, 255, 255, 1);
}

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
  min-height: 100vh;
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

/* FORM SECTION */
.form-section {
  flex: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  min-height: 100vh;
  width: 100%;
}

.form-card {
  width: 100%;
  max-width: 600px;
  background: white;
  padding: 40px;
  border-radius: 25px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.1);
  box-sizing: border-box;
  margin: auto;
}

/* CORRECTION DES CARREAUX (INPUTS) */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
  width: 100%;
  margin-bottom: 15px;
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
  width: 100%;
  padding: 12px 20px;
  border-radius: 50px;
  border: 2px solid #d1fae5;
  background-color: #f9fffd;
  box-sizing: border-box;
  transition: all 0.25s ease;
  font-size: 0.95rem;
  color: #333;
}

/* Focus effect */
.input-wrapper input:focus {
  outline: none;
  border-color: #2a9d8f;
  box-shadow: 0 0 0 4px rgba(42, 157, 143, 0.15);
}

/* Hover effect */
.input-wrapper input:hover {
  border-color: #a7f3d0;
}

/* OPTIONS AND CHECKBOX */
.options-row {
  margin: 20px 0;
  width: 100%;
}

.custom-checkbox {
  display: flex;
  align-items: center;
  font-size: 0.9rem;
  color: #333;
  cursor: pointer;
}

.custom-checkbox input {
  margin-right: 8px;
  cursor: pointer;
}

.label-text {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.label-text a {
  color: #2a9d8f;
  text-decoration: none;
  font-weight: 600;
}

.label-text a:hover {
  text-decoration: underline;
}

/* BUTTON AND FOOTER */
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

/* DARK MODE STYLES */
.dark-mode {
  background-color: var(--bg-dark);
  color: var(--text-dark);
}

.dark-mode .form-card {
  background: #1e1e1e;
  box-shadow: 0 15px 35px rgba(0,0,0,0.3);
}

.dark-mode .input-wrapper input {
  background-color: #2c2c2c;
  border-color: #444;
  color: #e0e0e0;
}

.dark-mode .primary-btn {
  background: #2a9d8f;
}

.dark-mode .primary-btn:hover {
  background: #218c74;
}

/* MODAL STYLES */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.close-btn {
  margin-top: 15px;
  padding: 10px 20px;
  background: #e63946;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background 0.3s;
}

.close-btn:hover {
  background: #c12b36;
}

/* RESPONSIVE */
@media (max-width: 1200px) {
  .playground-aside {
    flex: 0.8;
  }
  .form-section {
    flex: 1.2;
  }
}

@media (max-width: 900px) {
  .signup-container {
    flex-direction: column;
    min-height: auto;
  }

  .playground-aside {
    display: none;
  }

  .form-section {
    flex: 1;
    padding: 20px;
    min-height: 100vh;
  }

  .form-grid {
    grid-template-columns: 1fr;
  }

  .full-width {
    grid-column: span 1;
  }

  .form-card {
    max-width: 100%;
    width: 100%;
    padding: 30px;
  }

  .brand-title {
    font-size: 1.8rem;
  }
}

@media (max-width: 600px) {
  .form-section {
    padding: 15px;
    min-height: 100vh;
  }

  .form-card {
    padding: 20px;
    border-radius: 15px;
  }

  .form-grid {
    gap: 12px;
  }

  .dark-mode-toggle {
    top: 15px;
    right: 15px;
    width: 45px;
    height: 45px;
    font-size: 1.2rem;
  }

  .lang-dropdown {
    top: 15px;
    right: 65px;
    padding: 8px 15px;
    font-size: 0.9rem;
  }

  .brand-title {
    font-size: 1.5rem;
  }

  .input-wrapper label {
    font-size: 0.8rem;
  }

  .input-wrapper input {
    padding: 10px 15px;
    font-size: 0.9rem;
  }

  .primary-btn {
    padding: 12px;
    font-size: 1rem;
  }

  .form-footer {
    font-size: 0.85rem;
  }
}

.brand-title {
  font-size: 2rem;
  font-weight: 900;
  text-align: center;
  margin-bottom: 10px;
  transform: rotate(-1deg);
}
</style>
