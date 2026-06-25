<template>
  <div :data-theme="isDark ? 'dark' : 'light'" :class="{ 'dark-mode': isDark }">
    <router-view v-slot="{ Component }">
      <component
        :is="Component"
        :is-dark="isDark"
        :language="language"
        :translations="translations"
        @navigate="navigate"
        @logout="handleLogout"
        @toggle-theme="toggleTheme"
        @update-language="updateLanguage"
      />
    </router-view>
  </div>
</template>

<script>
import { translations } from './content/siteContent';
import { clearAuth } from './state/authStore';

export default {
  name: 'App',
  data() {
    return {
      language: 'en',
      isDark: false,
      translations
    };
  },
  methods: {
    handleLogout() {
      clearAuth();
      this.navigate('/signin');
    },
    navigate(path) {
      this.$router.push(path);
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
