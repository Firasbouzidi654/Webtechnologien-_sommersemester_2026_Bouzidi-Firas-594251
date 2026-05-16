<template>
  <div :data-theme="isDark ? 'dark' : 'light'">
    <component
      :is="currentView"
      :is-dark="isDark"
      :language="language"
      :translations="translations"
      @navigate="navigate"
      @toggle-theme="toggleTheme"
      @update-language="updateLanguage"
    />
    <ExampleEntities v-if="showExampleEntities" />
    <!-- Affiche le composant ExampleEntities si showExampleEntities est vrai -->
  </div>
</template>

<script>
import ExampleEntities from './components/ExampleEntities.vue';
import AdminDashboard from './views/AdminDashboard.vue';
import LoginPage from './views/LoginPage.vue';
import ParentDashboard from './views/ParentDashboard.vue';
import SignInView from './views/SignInView.vue';
import SignupView from './views/SignupView.vue';
import PrivacyView from './views/PrivacyView.vue';
import { translations } from './content/siteContent';

const routes = {
  '/': 'signin',
  '/login': 'signin',
  '/signup': 'signup',
  '/signin': 'signin',
  '/parent': 'parent',
  '/parent/': 'parent',
  '/admin': 'admin',
  '/admin/': 'admin',
  '/privacy': 'privacy',
  '/privacy/': 'privacy'
};

export default {
  name: 'App',
  components: {
    AdminDashboard,
    ExampleEntities,
    LoginPage,
    ParentDashboard,
    SignInView,
    SignupView,
    PrivacyView
  },
  data() {
    return {
      currentRoute: this.resolveRoute(window.location.pathname),
      language: 'en',
      isDark: false,
      translations
    };
  },
  computed: {
    currentView() {
      if (this.currentRoute === 'login') {
        return 'LoginPage';
      }

      if (this.currentRoute === 'parent') {
        return 'ParentDashboard';
      }

      if (this.currentRoute === 'admin') {
        return 'AdminDashboard';
      }

      if (this.currentRoute === 'signin') {
        return 'SignInView';
      }

      if (this.currentRoute === 'privacy') {
        return 'PrivacyView';
      }

      return 'SignupView';
    },

    // Show the example / overview only to staff (admin view)
    showExampleEntities() {
      return this.currentRoute === 'admin';
    }
  },
  created() {
    window.addEventListener('popstate', this.handlePopState);
  },
  beforeUnmount() {
    window.removeEventListener('popstate', this.handlePopState);
  },
  methods: {
    normalizePath(pathname) {
      const clean = pathname.split('?')[0].split('#')[0].replace(/\/+$|^\s+|\s+$/g, '');
      return clean === '' ? '/' : clean;
    },
    resolveRoute(pathname) {
      return routes[this.normalizePath(pathname)] || 'signup';
    },
    handlePopState() {
      this.currentRoute = this.resolveRoute(window.location.pathname);
    },
    navigate(path) {
      if (!routes[path]) {
        return;
      }

      if (window.location.pathname !== path) {
        window.history.pushState({}, '', path);
      }

      this.currentRoute = this.resolveRoute(path);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    toggleTheme() {
      this.isDark = !this.isDark;
    },
    updateLanguage(language) {
      this.language = language;
    }
  }
};
</script>
