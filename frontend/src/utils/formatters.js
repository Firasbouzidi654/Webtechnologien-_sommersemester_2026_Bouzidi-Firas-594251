export function formatDistanceMeters(distance) {
  if (distance < 1000) {
    return `${distance} m`;
  }
  return `${(distance / 1000).toFixed(1)} km`;
}

export function estimateDriveTimeMinutes(distance, speedKmh = 40) {
  const minutes = Math.max(1, Math.round((distance / 1000 / speedKmh) * 60));
  return `${minutes} min`;
}

export function formatDateTime(value) {
  if (!value) {
    return 'Unknown time';
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit'
  });
}
