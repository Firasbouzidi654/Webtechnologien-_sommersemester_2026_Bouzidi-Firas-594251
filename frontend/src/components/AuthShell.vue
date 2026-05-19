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
  --surface-strong: var(--bg-card);
  --text-main: var(--text-primary);
  --text-soft: var(--text-secondary);
  --border: var(--border-color);
  --accent-red: #e45b5b;
  --accent-green: #2d8f7b;
  --shadow: 0 24px 60px rgba(22, 42, 59, 0.12);
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(320px, 0.95fr) minmax(320px, 1.05fr);
  position: relative;
  overflow: hidden;
  background:
    radial-gradient(ellipse at top left, rgba(228, 91, 91, 0.26), transparent 42%),
    radial-gradient(ellipse at right center, rgba(45, 143, 123, 0.22), transparent 38%),
    linear-gradient(135deg, var(--color-bg-tertiary) 0%, var(--bg-primary) 100%);
  color: var(--text-main);
}

:global(.dark-mode) .auth-shell,
:global([data-theme="dark"]) .auth-shell {
  --surface: rgba(17, 24, 39, 0.88);
  --surface-strong: var(--bg-card);
  --text-main: var(--text-primary);
  --text-soft: var(--text-secondary);
  --border: var(--border-color);
  background:
    radial-gradient(ellipse at top left, rgba(20, 83, 45, 0.34), transparent 42%),
    radial-gradient(ellipse at right center, rgba(30, 58, 138, 0.28), transparent 38%),
    linear-gradient(180deg, #0f172a 0%, #111827 100%);
}

.auth-shell::before,
.auth-shell::after {
  content: "";
  position: absolute;
  inset: -22%;
  pointer-events: none;
  will-change: transform, opacity;
}

.auth-shell::before {
  background:
    radial-gradient(ellipse at 20% 25%, rgba(255, 255, 255, 0.56), transparent 28%),
    radial-gradient(ellipse at 82% 34%, rgba(45, 143, 123, 0.2), transparent 30%),
    radial-gradient(ellipse at 38% 84%, rgba(228, 91, 91, 0.16), transparent 32%);
  filter: blur(22px);
  opacity: 0.85;
  animation: ambient-shift 18s ease-in-out infinite alternate;
}

.auth-shell::after {
  background:
    linear-gradient(115deg, transparent 0%, rgba(255, 255, 255, 0.24) 44%, transparent 62%),
    radial-gradient(ellipse at 70% 72%, rgba(108, 181, 218, 0.13), transparent 34%);
  filter: blur(30px);
  opacity: 0.58;
  animation: soft-sweep 22s ease-in-out infinite;
}

.dark-mode {
  --surface: rgba(17, 24, 39, 0.9);
  --surface-strong: #111827;
  --text-main: #f8fafc;
  --text-soft: #cbd5e1;
  --border: rgba(255, 255, 255, 0.08);
  --shadow: 0 24px 60px rgba(0, 0, 0, 0.35);
  background:
    linear-gradient(
      180deg,
      #0f172a 0%,
      #111827 100%
    );
}

.hero-panel {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 100vh;
  padding: 32px 40px 40px 48px;
  overflow: hidden;
  z-index: 1;
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
  filter: saturate(1.06) contrast(1.03);
}

.hero-overlay {
  background:
    linear-gradient(135deg, rgba(17, 63, 85, 0.82), rgba(32, 138, 117, 0.58)),
    linear-gradient(180deg, rgba(5, 22, 31, 0.12), rgba(5, 22, 31, 0.45));
}

.hero-panel::after {
  content: "";
  position: absolute;
  inset: 8% -28% 4% 12%;
  background:
    linear-gradient(120deg, transparent, rgba(255, 255, 255, 0.2), transparent),
    radial-gradient(ellipse at 48% 46%, rgba(199, 249, 240, 0.18), transparent 48%);
  filter: blur(28px);
  opacity: 0.8;
  transform: translate3d(-6%, -3%, 0);
  animation: hero-light 13s ease-in-out infinite alternate;
  pointer-events: none;
}

.hero-card {
  position: relative;
  z-index: 2;
  max-width: 540px;
  padding: 40px;
  border-radius: 32px;
  background:
    linear-gradient(150deg, rgba(255, 255, 255, 0.24), rgba(255, 255, 255, 0.08) 58%),
    rgba(255, 255, 255, 0.1);
  color: #fff;
  box-shadow:
    0 28px 80px rgba(3, 20, 28, 0.3),
    inset 0 1px 0 rgba(255, 255, 255, 0.28);
  border: 1px solid rgba(255, 255, 255, 0.24);
  backdrop-filter: blur(22px) saturate(1.22);
  transition: transform 0.28s ease, box-shadow 0.28s ease, border-color 0.28s ease;
}

.hero-card:hover {
  transform: translateY(-2px);
  border-color: rgba(255, 255, 255, 0.34);
  box-shadow:
    0 34px 90px rgba(3, 20, 28, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.34);
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
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.hero-highlights li:hover {
  transform: translateX(3px);
  background: rgba(255, 255, 255, 0.16);
  border-color: rgba(255, 255, 255, 0.24);
}

.hero-icon {
  font-size: 1.1rem;
}

.content-panel {
  display: flex;
  align-items: start;
  justify-content: center;
  padding: 32px 24px 24px;
  position: relative;
  z-index: 1;
}

.content-card {
  width: min(100%, 660px);
  padding: 28px 36px 36px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 32px;
  box-shadow: var(--shadow);
  backdrop-filter: blur(24px) saturate(1.16);
  animation: card-arrive 0.46s ease both;
  transition: border-color 0.24s ease, box-shadow 0.24s ease, background 0.24s ease;
}

.content-card:hover {
  border-color: rgba(45, 143, 123, 0.18);
  box-shadow: 0 28px 72px rgba(22, 42, 59, 0.16);
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
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
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
  box-shadow: 0 10px 22px rgba(22, 42, 59, 0.1);
}

.theme-toggle:focus,
.language-picker select:focus {
  outline: 2px solid rgba(45, 143, 123, 0.35);
  outline-offset: 2px;
}

@keyframes ambient-shift {
  from {
    transform: translate3d(-2%, -1%, 0) scale(1);
  }

  to {
    transform: translate3d(3%, 2%, 0) scale(1.04);
  }
}

@keyframes soft-sweep {
  0% {
    transform: translate3d(-8%, -4%, 0) rotate(0deg);
    opacity: 0.42;
  }

  50% {
    opacity: 0.72;
  }

  100% {
    transform: translate3d(8%, 5%, 0) rotate(3deg);
    opacity: 0.46;
  }
}

@keyframes hero-light {
  from {
    transform: translate3d(-8%, -4%, 0) scale(0.98);
  }

  to {
    transform: translate3d(8%, 5%, 0) scale(1.04);
  }
}

@keyframes card-arrive {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 980px) {
  .auth-shell {
    grid-template-columns: 1fr;
    overflow: auto;
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
    align-items: stretch;
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

@media (prefers-reduced-motion: reduce) {
  .auth-shell::before,
  .auth-shell::after,
  .hero-panel::after,
  .content-card {
    animation: none;
  }

  .hero-card,
  .hero-highlights li,
  .theme-toggle,
  .language-picker select {
    transition: none;
  }
}
</style>
