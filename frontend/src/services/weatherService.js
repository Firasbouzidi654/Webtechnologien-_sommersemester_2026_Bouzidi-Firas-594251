// External API 1: Open-Meteo. Berlin is used as a fixed, simple demo location.
export async function loadWeatherHealthData() {
  const url = 'https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.405&current=temperature_2m,weather_code';
  const response = await fetch(url);
  if (!response.ok) throw new Error('Weather is unavailable.');

  const current = (await response.json()).current;
  const description = current.weather_code === 0 ? 'Clear sky' : 'Current conditions in Berlin';
  return {
    city: 'Berlin',
    country: 'DE',
    temperature: current.temperature_2m,
    feelsLike: current.temperature_2m,
    condition: description,
    description,
    humidity: null,
    windSpeed: null,
    rainProbability: null,
    healthAlerts: []
  };
}

// Kept for the existing weather card UI. Open-Meteo does not need an API key.
export function getStoredOpenWeatherApiKey() {
  return '';
}

export function saveOpenWeatherApiKey() {
  return '';
}
