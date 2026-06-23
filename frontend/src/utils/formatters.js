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
