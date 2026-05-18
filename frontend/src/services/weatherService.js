const DEFAULT_LOCATION = {
  lat: 52.52,
  lon: 13.405,
  label: 'Berlin fallback'
};

const REQUEST_TIMEOUT_MS = 12000;
const GEOLOCATION_TIMEOUT_MS = 6500;
const OPENWEATHER_BASE_URL = 'https://api.openweathermap.org/data/2.5';
const OPEN_METEO_BASE_URL = 'https://api.open-meteo.com/v1/forecast';
const OPENWEATHER_KEY_STORAGE = 'kindercare-openweather-api-key';

function apiBaseUrl() {
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL;
  }

  return '';
}

function openWeatherApiKey() {
  return import.meta.env.VITE_OPENWEATHER_API_KEY || getStoredOpenWeatherApiKey();
}

export function getStoredOpenWeatherApiKey() {
  try {
    return window.localStorage.getItem(OPENWEATHER_KEY_STORAGE) || '';
  } catch {
    return '';
  }
}

export function saveOpenWeatherApiKey(apiKey) {
  const value = String(apiKey || '').trim();

  try {
    if (value) {
      window.localStorage.setItem(OPENWEATHER_KEY_STORAGE, value);
    } else {
      window.localStorage.removeItem(OPENWEATHER_KEY_STORAGE);
    }
  } catch {
    // localStorage can be unavailable in strict privacy contexts.
  }

  return value;
}

function getBrowserPosition() {
  if (!navigator.geolocation) {
    return Promise.resolve({ ...DEFAULT_LOCATION, isFallback: true });
  }

  return new Promise((resolve) => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          lat: position.coords.latitude,
          lon: position.coords.longitude,
          isFallback: false
        });
      },
      () => resolve({ ...DEFAULT_LOCATION, isFallback: true }),
      {
        enableHighAccuracy: false,
        maximumAge: 15 * 60 * 1000,
        timeout: GEOLOCATION_TIMEOUT_MS
      }
    );
  });
}

function buildAlerts(weather) {
  const temperature = Number(weather?.temperature);
  const windSpeed = Number(weather?.windSpeed);
  const rainProbability = Number(weather?.rainProbability);
  const humidity = Number(weather?.humidity);
  const condition = String(weather?.condition || '').toLowerCase();
  const alerts = [];

  if (Number.isFinite(temperature) && temperature >= 30) {
    alerts.push({
      type: 'heat',
      title: 'High heat warning',
      message: 'High temperatures may affect outdoor activities.'
    });
  }

  if (Number.isFinite(temperature) && temperature <= 3) {
    alerts.push({
      type: 'cold',
      title: 'Cold weather warning',
      message: 'Layer clothing and keep outdoor breaks short for younger children.'
    });
  }

  if (condition.includes('rain') || (Number.isFinite(rainProbability) && rainProbability >= 45)) {
    alerts.push({
      type: 'rain',
      title: 'Rain activity alert',
      message: 'Outdoor activities may need indoor alternatives.'
    });
  }

  if (Number.isFinite(windSpeed) && windSpeed >= 10) {
    alerts.push({
      type: 'wind',
      title: 'Wind caution',
      message: 'Secure outdoor materials and supervise playground time closely.'
    });
  }

  if (Number.isFinite(humidity) && humidity >= 80) {
    alerts.push({
      type: 'respiratory',
      title: 'Respiratory comfort note',
      message: 'Children with asthma should avoid prolonged outdoor exposure if symptoms appear.'
    });
  }

  alerts.push({
    type: 'air',
    title: 'Air quality placeholder',
    message: 'Review local air quality guidance for respiratory-sensitive children.'
  });

  return alerts.slice(0, 4);
}

export async function loadWeatherHealthData() {
  if (!navigator.onLine) {
    throw new Error('You appear to be offline. Weather data will update when the connection returns.');
  }

  const location = await getBrowserPosition();
  const data = openWeatherApiKey()
    ? await fetchOpenWeatherDirect(location)
    : await fetchOpenMeteoDirect(location);

  return {
    ...data,
    locationFallback: location.isFallback,
    fallbackLabel: location.label || '',
    healthAlerts: buildAlerts(data)
  };
}

async function fetchOpenWeatherDirect(location) {
  const apiKey = openWeatherApiKey();
  const params = new URLSearchParams({
    lat: String(location.lat),
    lon: String(location.lon),
    appid: apiKey,
    units: 'metric'
  });
  const [current, forecast] = await Promise.all([
    fetchJson(`${OPENWEATHER_BASE_URL}/weather?${params.toString()}`),
    fetchJson(`${OPENWEATHER_BASE_URL}/forecast?${params.toString()}`)
  ]);

  return normalizeOpenWeather(current, forecast);
}

async function fetchOpenMeteoDirect(location) {
  const params = new URLSearchParams({
    latitude: String(location.lat),
    longitude: String(location.lon),
    current: [
      'temperature_2m',
      'relative_humidity_2m',
      'apparent_temperature',
      'precipitation',
      'rain',
      'weather_code',
      'cloud_cover',
      'wind_speed_10m'
    ].join(','),
    hourly: 'precipitation_probability',
    forecast_hours: '1',
    wind_speed_unit: 'ms',
    timezone: 'auto'
  });
  const data = await fetchJson(`${OPEN_METEO_BASE_URL}?${params.toString()}`);

  return normalizeOpenMeteo(data);
}

async function fetchWeatherFromBackend(location) {
  if (import.meta.env.DEV && !import.meta.env.VITE_API_BASE_URL) {
    throw new Error('OpenWeather API key required. Paste a free OpenWeather key below to use weather without running the backend.');
  }

  const params = new URLSearchParams({
    lat: String(location.lat),
    lon: String(location.lon)
  });

  return fetchJson(`${apiBaseUrl()}/api/weather/health?${params.toString()}`);
}

async function fetchJson(url) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    const response = await fetch(url, {
      cache: 'no-store',
      signal: controller.signal
    });
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(data?.message || `Weather request failed with status ${response.status}.`);
    }

    return data;
  } catch (error) {
    if (error.name === 'AbortError') {
      throw new Error('Weather request took too long. Please try again in a moment.');
    }

    if (error instanceof TypeError) {
      throw new Error('Weather data is temporarily unavailable.');
    }

    throw error;
  } finally {
    window.clearTimeout(timeoutId);
  }
}

function normalizeOpenWeather(current, forecast) {
  const weather = Array.isArray(current?.weather) ? current.weather[0] : {};
  const nextForecast = Array.isArray(forecast?.list) ? forecast.list[0] : {};

  return {
    city: current?.name || 'Local area',
    country: current?.sys?.country || '',
    temperature: numberOrNull(current?.main?.temp),
    feelsLike: numberOrNull(current?.main?.feels_like),
    condition: weather?.main || 'Weather',
    description: capitalize(weather?.description || 'Current conditions'),
    icon: weather?.icon || '',
    humidity: numberOrNull(current?.main?.humidity),
    windSpeed: numberOrNull(current?.wind?.speed),
    cloudiness: numberOrNull(current?.clouds?.all),
    rainVolume: numberOrNull(current?.rain?.['1h']),
    rainProbability: Number.isFinite(Number(nextForecast?.pop)) ? Math.round(Number(nextForecast.pop) * 100) : null,
    observedAt: current?.dt ? new Date(current.dt * 1000).toISOString() : new Date().toISOString()
  };
}

function normalizeOpenMeteo(data) {
  const current = data?.current || {};
  const weatherCode = Number(current.weather_code);
  const rainProbability = Array.isArray(data?.hourly?.precipitation_probability)
    ? data.hourly.precipitation_probability[0]
    : null;

  return {
    city: 'Local area',
    country: '',
    temperature: numberOrNull(current.temperature_2m),
    feelsLike: numberOrNull(current.apparent_temperature),
    condition: weatherCodeLabel(weatherCode),
    description: weatherCodeLabel(weatherCode),
    icon: '',
    humidity: numberOrNull(current.relative_humidity_2m),
    windSpeed: numberOrNull(current.wind_speed_10m),
    cloudiness: numberOrNull(current.cloud_cover),
    rainVolume: numberOrNull(current.rain ?? current.precipitation),
    rainProbability: numberOrNull(rainProbability),
    observedAt: current.time || new Date().toISOString(),
    source: 'Open-Meteo fallback'
  };
}

function weatherCodeLabel(code) {
  if (code === 0) return 'Clear sky';
  if ([1, 2, 3].includes(code)) return 'Partly cloudy';
  if ([45, 48].includes(code)) return 'Fog';
  if ([51, 53, 55, 56, 57].includes(code)) return 'Drizzle';
  if ([61, 63, 65, 66, 67, 80, 81, 82].includes(code)) return 'Rain';
  if ([71, 73, 75, 77, 85, 86].includes(code)) return 'Snow';
  if ([95, 96, 99].includes(code)) return 'Thunderstorm';
  return 'Current conditions';
}

function numberOrNull(value) {
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

function capitalize(value) {
  if (!value) {
    return '';
  }

  return `${value.charAt(0).toUpperCase()}${value.slice(1)}`;
}
