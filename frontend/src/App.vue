<template>
  <component
    :is="currentView"
    :is-dark="isDark"
    :language="language"
    :translations="translations"
    @navigate="navigate"
    @toggle-theme="toggleTheme"
    @update-language="updateLanguage"
  />
  <ExampleEntities />
</template>

<script>
import ExampleEntities from './components/ExampleEntities.vue';
import SignInView from './views/SignInView.vue';
import SignupView from './views/SignupView.vue';
import PrivacyView from './views/PrivacyView.vue';
import { translations } from './content/siteContent';

const routes = {
  '/': 'signup',
  '/signup': 'signup',
  '/signin': 'signin',
  '/privacy': 'privacy'
};

export default {
  name: 'App',
  components: {
    ExampleEntities,
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
      if (this.currentRoute === 'signin') {
        return 'SignInView';
      }

      if (this.currentRoute === 'privacy') {
        return 'PrivacyView';
      }

      return 'SignupView';
    }
  },
  created() {
    window.addEventListener('popstate', this.handlePopState);
  },
  beforeUnmount() {
    window.removeEventListener('popstate', this.handlePopState);
  },
  methods: {
    resolveRoute(pathname) {
      return routes[pathname] || 'signup';
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
