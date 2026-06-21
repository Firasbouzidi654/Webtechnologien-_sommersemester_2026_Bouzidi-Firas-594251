<template>
  <div :data-theme="isDark ? 'dark' : 'light'" :class="{ 'dark-mode': isDark }">
    <component
      :is="currentView"
      :is-dark="isDark"
      :language="language"
      :translations="translations"
      @navigate="navigate"
      @logout="handleLogout"
      @toggle-theme="toggleTheme"
      @update-language="updateLanguage"
    />
  </div>
</template>

<script>
import AdminDashboard from './views/AdminDashboard.vue';
import ParentDashboard from './views/ParentDashboard.vue';
import SignInView from './views/SignInView.vue';
import SignupView from './views/SignupView.vue';
import PrivacyView from './views/PrivacyView.vue';
import ForgotPasswordView from './views/ForgotPasswordView.vue';
import { translations } from './content/siteContent';
import { clearAuth } from './state/authStore';

const routes = {
  '/': 'signin',
  '/login': 'signin',
  '/signup': 'signup',
  '/signin': 'signin',
  '/parent': 'parent',
  '/parent/': 'parent',
  '/admin': 'admin',
  '/admin/': 'admin',
  '/forgot-password': 'forgotPassword',
  '/forgot-password/': 'forgotPassword',
  '/privacy': 'privacy',
  '/privacy/': 'privacy'
};

export default {
  name: 'App',
  components: {
    AdminDashboard,
    ParentDashboard,
    SignInView,
    SignupView,
    PrivacyView,
    ForgotPasswordView
  },
  data() {
    return {
      currentRoute: this.resolveRoute(
        // prefer hash if present, otherwise pathname
        window.location.hash ? window.location.hash.slice(1) : window.location.pathname
      ),
      language: 'en',
      isDark: false,
      translations
    };
  },
  computed: {
    currentView() {
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

      if (this.currentRoute === 'forgotPassword') {
        return 'ForgotPasswordView';
      }

      return 'SignupView';
    },

  },
  created() {
    // support both history and hash navigation
    window.addEventListener('popstate', this.handlePopState);
    window.addEventListener('hashchange', this.handlePopState);

    // If the user directly loaded a SPA route like `/parent` and the server
    // didn't rewrite to index.html with a hash, convert the pathname to a
    // hash URL so the app reliably resolves routes without relying on server
    // rewrites. This preserves the intended route while ensuring the app
    // renders correctly in dev environments.
    const pathname = this.normalizePath(window.location.pathname);
    if (!window.location.hash && routes[pathname]) {
      // replace the URL with a hash-based equivalent without creating a new
      // history entry (so Back still works as expected)
      window.history.replaceState({}, '', `/#${pathname}`);
      this.currentRoute = this.resolveRoute(pathname);
    }
  },
  beforeUnmount() {
    window.removeEventListener('popstate', this.handlePopState);
    window.removeEventListener('hashchange', this.handlePopState);
  },
  methods: {
    normalizePath(pathname) {
      const clean = pathname.split('?')[0].split('#')[0].replace(/\/+$|^\s+|\s+$/g, '');
      return clean === '' ? '/' : clean;
    },
    resolveRoute(pathname) {
      const resolved = routes[this.normalizePath(pathname)] || 'signup';
      return resolved;
    },
    handlePopState() {
      const path = window.location.hash ? window.location.hash.slice(1) : window.location.pathname;
      this.currentRoute = this.resolveRoute(path);
    },
    handleLogout() {
      clearAuth();
      this.navigate('/signin');
    },
    navigate(path) {
      if (!routes[path]) return;

      // prefer hash navigation to avoid server rewrite issues; keep pathname
      // navigation where appropriate
      try {
        if (window.location.hash !== `#${path}`) {
          // push a hash entry so direct reloads work in static hosts/dev server
          window.history.pushState({}, '', `/#${path}`);
        }
      } catch (e) {
        // fallback to history push
        if (window.location.pathname !== path) window.history.pushState({}, '', path);
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

<style>
#app,
[data-theme] {
  width: 100%;
  min-height: 100vh;
  background: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}
</style>
