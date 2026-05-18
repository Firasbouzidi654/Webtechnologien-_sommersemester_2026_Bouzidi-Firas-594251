<template>
  <section class="weather-health-card" :class="{ compact }">
    <header>
      <div>
        <p class="eyebrow">Weather Health</p>
        <h2>{{ weatherTitle }}</h2>
      </div>
      <button v-if="!compact" type="button" :disabled="loading" @click="loadWeather">
        <span v-if="loading" class="weather-spinner" aria-hidden="true"></span>
        <span>{{ loading ? 'Loading' : 'Refresh' }}</span>
      </button>
    </header>

    <div v-if="loading" class="weather-state">
      <span class="weather-spinner large" aria-hidden="true"></span>
      <p>Loading local weather conditions...</p>
    </div>

    <div v-else-if="errorMessage" class="weather-state error-state" role="alert">
      <strong>Weather unavailable</strong>
      <p>{{ errorMessage }}</p>
      <form v-if="needsApiKey" class="weather-key-form" @submit.prevent="saveKeyAndLoadWeather">
        <label>
          <span>OpenWeather free API key</span>
          <input
            v-model.trim="apiKeyDraft"
            type="password"
            placeholder="Paste API key"
            autocomplete="off"
          />
        </label>
        <button type="submit" :disabled="!apiKeyDraft">Save key</button>
      </form>
    </div>

    <template v-else>
      <div class="weather-main">
        <div class="weather-icon-wrap" aria-hidden="true">
          <svg v-if="compact" class="weather-svg" viewBox="0 0 24 24">
            <path class="sun" d="M6.5 7.5a4 4 0 1 1 6.9 2.7" />
            <path class="sun-rays" d="M10.5 1.8v1.7M4.1 4.1l1.2 1.2M1.8 10.5h1.7M16.9 4.1l-1.2 1.2" />
            <path d="M7.5 20h9.2a4.2 4.2 0 0 0 .1-8.4 5.6 5.6 0 0 0-10.5 1.5A3.5 3.5 0 0 0 7.5 20Z" />
          </svg>
          <img v-else-if="iconUrl" :src="iconUrl" :alt="weatherDescription" />
          <span v-else class="weather-orb"></span>
        </div>
        <div>
          <strong>{{ temperatureLabel }}</strong>
          <span>{{ weatherDescription }}</span>
          <small>{{ locationLabel }}</small>
        </div>
      </div>

      <dl class="weather-metrics">
        <div>
          <dt>Humidity</dt>
          <dd>{{ metricLabel(weather?.humidity, '%') }}</dd>
        </div>
        <div>
          <dt>Wind</dt>
          <dd>{{ metricLabel(weather?.windSpeed, 'm/s') }}</dd>
        </div>
        <div>
          <dt>Rain</dt>
          <dd>{{ metricLabel(weather?.rainProbability, '%') }}</dd>
        </div>
      </dl>

      <div v-if="!compact" class="health-alerts">
        <article v-for="alert in healthAlerts" :key="alert.type" :class="`alert-${alert.type}`">
          <strong>{{ alert.title }}</strong>
          <p>{{ alert.message }}</p>
        </article>
      </div>
    </template>
  </section>
</template>

<script>
import { getStoredOpenWeatherApiKey, loadWeatherHealthData, saveOpenWeatherApiKey } from '../services/weatherService';

export default {
  name: 'WeatherHealthCard',
  props: {
    compact: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      loading: true,
      weather: null,
      errorMessage: '',
      apiKeyDraft: getStoredOpenWeatherApiKey(),
      mounted: false
    };
  },
  computed: {
    needsApiKey() {
      return this.errorMessage.includes('OpenWeather API key required');
    },
    weatherTitle() {
      return this.weather?.city ? `${this.weather.city} conditions` : 'Local conditions';
    },
    temperatureLabel() {
      const temperature = Number(this.weather?.temperature);
      return Number.isFinite(temperature) ? `${Math.round(temperature)} C` : '-- C';
    },
    weatherDescription() {
      return this.weather?.description || this.weather?.condition || 'Weather data pending';
    },
    locationLabel() {
      if (!this.weather) {
        return 'Checking location';
      }

      const fallback = this.weather.locationFallback ? 'Fallback location' : 'Local weather';
      const place = [this.weather.city, this.weather.country].filter(Boolean).join(', ');
      const source = this.weather.source ? ` - ${this.weather.source}` : '';

      return place ? `${place} - ${fallback}${source}` : `${fallback}${source}`;
    },
    iconUrl() {
      return this.weather?.icon ? `https://openweathermap.org/img/wn/${this.weather.icon}@2x.png` : '';
    },
    healthAlerts() {
      return this.weather?.healthAlerts?.length
        ? this.weather.healthAlerts
        : [{
          type: 'normal',
          title: 'Outdoor check',
          message: 'No weather-related child health alerts are active right now.'
        }];
    }
  },
  mounted() {
    this.mounted = true;
    this.loadWeather();
  },
  beforeUnmount() {
    this.mounted = false;
  },
  methods: {
    metricLabel(value, unit) {
      const number = Number(value);
      return Number.isFinite(number) ? `${Math.round(number)}${unit}` : 'N/A';
    },
    async loadWeather() {
      this.loading = true;
      this.errorMessage = '';

      try {
        const weather = await loadWeatherHealthData();

        if (this.mounted) {
          this.weather = weather;
        }
      } catch (error) {
        if (this.mounted) {
          this.errorMessage = error.message || 'Weather data is temporarily unavailable.';
        }
      } finally {
        if (this.mounted) {
          this.loading = false;
        }
      }
    },
    saveKeyAndLoadWeather() {
      saveOpenWeatherApiKey(this.apiKeyDraft);
      this.loadWeather();
    }
  }
};
</script>

<style scoped>
.weather-health-card {
  display: grid;
  gap: 16px;
  overflow: hidden;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  padding: 18px;
  background:
    linear-gradient(135deg, rgba(49, 130, 206, 0.16), rgba(56, 161, 105, 0.12)),
    var(--color-bg-secondary);
  box-shadow: var(--shadow-sm);
  color: var(--color-text-primary);
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.weather-health-card.compact {
  min-height: 110px;
  max-height: 110px;
  height: 110px;
  grid-template-columns: minmax(0, 1.1fr) minmax(86px, 0.9fr);
  gap: 10px;
  align-items: center;
  overflow: hidden;
  border-radius: 12px;
  padding: 14px 16px;
  border-color: color-mix(in srgb, #67c7dd 32%, var(--color-border));
  background:
    linear-gradient(135deg, color-mix(in srgb, #d9f7fb 78%, var(--color-bg-secondary)), var(--color-bg-secondary)),
    var(--color-bg-secondary);
}

.weather-health-card.compact header {
  display: none;
}

.weather-health-card:hover {
  transform: translateY(-3px);
  border-color: rgba(49, 130, 206, 0.28);
  box-shadow: var(--shadow-lg);
}

.weather-health-card header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.weather-health-card h2 {
  margin: 4px 0 0;
  color: var(--color-text-primary);
  font-size: 1.18rem;
}

.weather-health-card button {
  display: inline-flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 9px 12px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
  cursor: pointer;
  font-weight: 800;
  box-shadow: var(--shadow-sm);
}

.weather-health-card button:hover {
  border-color: var(--color-brand);
}

.weather-health-card button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.weather-main {
  display: grid;
  grid-template-columns: 86px minmax(0, 1fr);
  gap: 14px;
  align-items: center;
}

.weather-health-card.compact .weather-main {
  grid-template-columns: 36px minmax(0, 1fr);
  gap: 12px;
  min-width: 0;
}

.weather-health-card.compact .weather-icon-wrap {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  color: #1d7188;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.56), 0 8px 16px rgba(15, 23, 42, 0.08);
  animation: none;
}

.weather-health-card.compact .weather-icon-wrap img {
  width: 38px;
  height: 38px;
}

.weather-health-card.compact .weather-main strong {
  font-size: 1.08rem;
  line-height: 1;
}

.weather-health-card.compact .weather-main span {
  overflow: hidden;
  margin-top: 4px;
  font-size: 0.74rem;
  line-height: 1.15;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.weather-health-card.compact .weather-main small {
  display: none;
}

.weather-icon-wrap {
  display: grid;
  width: 78px;
  height: 78px;
  place-items: center;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.62);
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.48), var(--shadow-sm);
  animation: weather-float 3.4s ease-in-out infinite;
}

:global([data-theme="dark"]) .weather-icon-wrap {
  background: rgba(36, 49, 73, 0.82);
}

.weather-icon-wrap img {
  width: 72px;
  height: 72px;
  object-fit: contain;
}

.weather-svg {
  width: 22px;
  height: 22px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 2;
}

.weather-svg .sun {
  stroke: var(--color-warning);
}

.weather-svg .sun-rays {
  stroke: var(--color-warning);
  stroke-width: 1.8;
}

.weather-orb {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f6c453, #72b7f2);
}

.weather-main strong {
  display: block;
  color: var(--color-text-primary);
  font-size: 2.35rem;
  line-height: 1;
}

.weather-main span,
.weather-main small {
  display: block;
  margin-top: 5px;
  color: var(--color-text-secondary);
  font-weight: 800;
  text-transform: capitalize;
}

.weather-main small {
  color: var(--color-text-tertiary);
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: none;
}

.weather-metrics {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin: 0;
}

.weather-health-card.compact .weather-metrics {
  grid-template-columns: 1fr;
  gap: 6px;
  min-width: 0;
}

.weather-health-card.compact .weather-metrics div {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
  border-radius: 8px;
  padding: 6px 8px;
}

.weather-health-card.compact .weather-metrics div:first-child {
  grid-column: auto;
}

.weather-health-card.compact .weather-metrics div:nth-child(3) {
  display: none;
}

.weather-health-card.compact .weather-metrics dt {
  font-size: 0.62rem;
  line-height: 1.1;
}

.weather-health-card.compact .weather-metrics dd {
  margin: 0;
  font-size: 0.74rem;
  line-height: 1.1;
}

.weather-health-card.compact .weather-state {
  grid-column: 1 / -1;
  min-height: 70px;
  padding: 10px;
}

.weather-health-card.compact .weather-state p {
  font-size: 0.7rem;
  line-height: 1.2;
}

.weather-metrics div {
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 11px;
  background: color-mix(in srgb, var(--color-bg-primary) 86%, transparent);
}

.weather-metrics dt {
  color: var(--color-text-tertiary);
  font-size: 0.72rem;
  font-weight: 900;
  text-transform: uppercase;
}

.weather-metrics dd {
  margin: 3px 0 0;
  color: var(--color-text-primary);
  font-size: 1.05rem;
  font-weight: 900;
}

.health-alerts {
  display: grid;
  gap: 8px;
}

.health-alerts article {
  border: 1px solid var(--color-border-light);
  border-left: 4px solid var(--color-brand);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
}

.health-alerts strong,
.weather-state strong {
  color: var(--color-text-primary);
}

.health-alerts p,
.weather-state p {
  margin: 3px 0 0;
  color: var(--color-text-secondary);
  font-size: 0.9rem;
  font-weight: 650;
  line-height: 1.42;
}

.alert-heat {
  border-left-color: var(--color-danger);
}

.alert-cold,
.alert-wind {
  border-left-color: var(--color-info);
}

.alert-rain {
  border-left-color: var(--color-brand);
}

.alert-respiratory,
.alert-air {
  border-left-color: var(--color-warning);
}

.weather-state {
  display: grid;
  gap: 10px;
  justify-items: start;
  border: 1px solid var(--color-border-light);
  border-radius: 14px;
  padding: 14px;
  background: var(--color-bg-primary);
}

.weather-key-form {
  display: grid;
  width: 100%;
  gap: 10px;
}

.weather-key-form label {
  display: grid;
  gap: 6px;
  color: var(--color-missed-text);
  font-weight: 800;
}

.weather-key-form span {
  font-size: 0.78rem;
  text-transform: uppercase;
}

.weather-key-form input {
  width: 100%;
  min-height: 42px;
  border: 1px solid var(--color-missed-border);
  border-radius: 10px;
  padding: 10px 12px;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

.weather-key-form button {
  width: 100%;
  background: var(--color-bg-primary);
}

.error-state {
  border-color: var(--color-missed-border);
  background: var(--color-missed);
}

.error-state p,
.error-state strong {
  color: var(--color-missed-text);
}

.weather-spinner {
  width: 16px;
  height: 16px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-brand);
  border-radius: 50%;
  animation: weather-spin 0.8s linear infinite;
}

.weather-spinner.large {
  width: 34px;
  height: 34px;
  border-width: 4px;
}

@keyframes weather-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes weather-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-5px);
  }
}

@media (max-width: 640px) {
  .weather-health-card header,
  .weather-main {
    grid-template-columns: 1fr;
  }

  .weather-health-card header {
    flex-direction: column;
  }

  .weather-health-card button {
    width: 100%;
  }

  .weather-metrics {
    grid-template-columns: 1fr;
  }

  .weather-health-card.compact .weather-main {
    grid-template-columns: 36px minmax(0, 1fr);
  }

  .weather-health-card.compact .weather-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
