<template>
  <div class="auth-shell" :class="{ 'dark-mode': isDark }">
    <aside class="hero-panel">
      <div class="hero-backdrop" :style="{ backgroundImage: `url(${hero.image})` }"></div>
      <div class="hero-overlay"></div>
      <div class="hero-card">
        <p class="hero-eyebrow">{{ hero.eyebrow }}</p>
        <h1 class="hero-title">{{ hero.title }}</h1>
        <p class="hero-text">{{ hero.text }}</p>

        <ul class="hero-highlights">
          <li v-for="item in hero.highlights" :key="item.id">
            <span class="hero-icon" aria-hidden="true">{{ item.icon }}</span>
            <span>{{ item.text }}</span>
          </li>
        </ul>
      </div>
    </aside>

    <main class="content-panel">
      <div class="content-card">
        <div class="card-toolbar">
          <div class="card-controls" aria-label="Display controls">
            <label class="language-picker">
              <span class="control-label">{{ common.languageLabel }}</span>
              <select :value="language" @change="$emit('update-language', $event.target.value)">
                <option value="en">English</option>
                <option value="de">Deutsch</option>
              </select>
            </label>

            <button class="theme-toggle" type="button" @click="$emit('toggle-theme')">
              <span class="theme-icon">{{ isDark ? '☀️' : '🌙' }}</span>
              <span>{{ isDark ? common.lightMode : common.darkMode }}</span>
            </button>
          </div>
        </div>

        <div class="brand-block">
          <p class="brand-kicker">{{ page.badge }}</p>
          <h2 class="brand-title">
            <span class="brand-red">KinderCare</span>
            <span class="brand-green">Connect</span>
          </h2>
          <p class="brand-tagline">
            <span>{{ common.brandLead }}</span>
            <span>{{ common.brandAccent }}</span>
          </p>
        </div>

        <slot />
      </div>
    </main>
  </div>
</template>

<script>
export default {
  name: 'AuthShell',
  props: {
    common: {
      type: Object,
      required: true
    },
    hero: {
      type: Object,
      required: true
    },
    isDark: {
      type: Boolean,
      default: false
    },
    language: {
      type: String,
      default: 'en'
    },
    page: {
      type: Object,
      required: true
    }
  },
  emits: ['toggle-theme', 'update-language']
};
</script>

<style scoped>
.auth-shell {
  --surface: rgba(255, 255, 255, 0.88);
  --surface-strong: #ffffff;
  --text-main: #20303f;
  --text-soft: #5b6b7b;
  --border: rgba(34, 62, 79, 0.12);
  --accent-red: #e45b5b;
  --accent-green: #2d8f7b;
  --shadow: 0 24px 60px rgba(22, 42, 59, 0.12);
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(320px, 0.95fr) minmax(320px, 1.05fr);
  background:
    radial-gradient(circle at top left, rgba(228, 91, 91, 0.28), transparent 38%),
    radial-gradient(circle at right center, rgba(45, 143, 123, 0.24), transparent 32%),
    linear-gradient(135deg, #eef6f3 0%, #fffaf7 100%);
  color: var(--text-main);
}

.dark-mode {
  --surface: rgba(18, 26, 34, 0.9);
  --surface-strong: #15202b;
  --text-main: #edf4f8;
  --text-soft: #b6c3ce;
  --border: rgba(212, 230, 241, 0.14);
  --shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
  background:
    radial-gradient(circle at top left, rgba(228, 91, 91, 0.16), transparent 38%),
    radial-gradient(circle at right center, rgba(45, 143, 123, 0.18), transparent 32%),
    linear-gradient(135deg, #0f1820 0%, #16232e 100%);
}

.hero-panel {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 100vh;
  padding: 32px 40px 40px 48px;
  overflow: hidden;
}

.hero-backdrop,
.hero-overlay {
  position: absolute;
  inset: 0;
}

.hero-backdrop {
  background-size: cover;
  background-position: center;
  transform: scale(1.04);
}

.hero-overlay {
  background:
    linear-gradient(135deg, rgba(17, 63, 85, 0.82), rgba(32, 138, 117, 0.58)),
    linear-gradient(180deg, rgba(5, 22, 31, 0.12), rgba(5, 22, 31, 0.45));
}

.hero-card {
  position: relative;
  z-index: 1;
  max-width: 540px;
  padding: 40px;
  border-radius: 32px;
  background:
    linear-gradient(160deg, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.08)),
    rgba(255, 255, 255, 0.08);
  color: #fff;
  box-shadow: var(--shadow);
  border: 1px solid rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(14px);
}

.hero-eyebrow {
  text-transform: uppercase;
  letter-spacing: 0.12em;
  font-size: 0.78rem;
  font-weight: 700;
  opacity: 0.9;
}

.hero-title {
  margin: 18px 0 16px;
  font-size: clamp(2.2rem, 5vw, 3.8rem);
  line-height: 1.02;
}

.hero-text {
  max-width: 34rem;
  font-size: 1.05rem;
  line-height: 1.6;
  opacity: 0.95;
}

.hero-highlights {
  list-style: none;
  margin: 28px 0 0;
  padding: 0;
  display: grid;
  gap: 12px;
}

.hero-highlights li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.hero-icon {
  font-size: 1.1rem;
}

.content-panel {
  display: flex;
  align-items: start;
  justify-content: center;
  padding: 32px 24px 24px;
}

.content-card {
  width: min(100%, 660px);
  padding: 28px 36px 36px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 32px;
  box-shadow: var(--shadow);
  backdrop-filter: blur(20px);
}

.card-toolbar {
  display: flex;
  justify-content: end;
  margin-bottom: 22px;
}

.card-controls {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.brand-block {
  margin-bottom: 28px;
}

.brand-kicker {
  margin: 0 0 10px;
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--accent-green);
}

.brand-title {
  margin: 0;
  font-size: clamp(2rem, 4vw, 2.75rem);
  line-height: 1.05;
}

.brand-title span + span {
  margin-left: 0.25em;
}

.brand-red {
  color: var(--accent-red);
}

.brand-green {
  color: var(--accent-green);
}

.brand-tagline {
  margin: 14px 0 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: var(--text-soft);
  font-size: 1rem;
}

.theme-toggle,
.language-picker {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--border);
  border-radius: 999px;
  color: var(--text-main);
  background: var(--surface-strong);
  min-height: 42px;
}

.theme-toggle {
  padding: 10px 14px;
  cursor: pointer;
  font: inherit;
  font-weight: 700;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.theme-icon {
  font-size: 0.95rem;
}

.language-picker {
  padding: 6px 8px 6px 12px;
}

.control-label {
  font-size: 0.78rem;
  font-weight: 700;
  color: var(--text-soft);
}

.language-picker select {
  border: 1px solid var(--border);
  background: var(--surface-strong);
  color: var(--text-main);
  border-radius: 999px;
  padding: 8px 34px 8px 12px;
  font: inherit;
  font-weight: 700;
  appearance: none;
  min-width: 118px;
  background-image:
    linear-gradient(45deg, transparent 50%, currentColor 50%),
    linear-gradient(135deg, currentColor 50%, transparent 50%);
  background-position:
    calc(100% - 16px) calc(50% - 3px),
    calc(100% - 10px) calc(50% - 3px);
  background-size: 6px 6px, 6px 6px;
  background-repeat: no-repeat;
}

.theme-toggle:hover,
.language-picker select:hover {
  transform: translateY(-1px);
}

.theme-toggle:focus,
.language-picker select:focus {
  outline: 2px solid rgba(45, 143, 123, 0.35);
  outline-offset: 2px;
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .hero-panel {
    min-height: auto;
    padding: 24px 20px 0;
  }

  .hero-card {
    width: 100%;
  }

  .content-panel {
    padding-top: 20px;
  }
}

@media (max-width: 640px) {
  .hero-panel {
    padding: 20px 16px 0;
  }

  .content-panel {
    padding: 18px 16px 16px;
  }

  .content-card,
  .hero-card {
    padding: 24px;
    border-radius: 24px;
  }

  .card-toolbar {
    justify-content: stretch;
  }

  .card-controls {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .theme-toggle,
  .language-picker {
    width: 100%;
    justify-content: space-between;
  }

  .control-label {
    font-size: 0.8rem;
  }
}
</style>
