const AMENITY_LABELS = {
  hospital: 'Hospital',
  police: 'Police station',
  pharmacy: 'Pharmacy'
};

function distanceInMeters(lat1, lng1, lat2, lng2) {
  const toRadians = (value) => (value * Math.PI) / 180;
  const radius = 6371000;
  const deltaLat = toRadians(lat2 - lat1);
  const deltaLng = toRadians(lng2 - lng1);
  const value =
    Math.sin(deltaLat / 2) ** 2 +
    Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) * Math.sin(deltaLng / 2) ** 2;

  return radius * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
}

export function buildEmergencyRouteLink(from, to) {
  if (!from || !to) {
    return '#';
  }

  return `https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=${from.lat},${from.lng};${to.lat},${to.lng}`;
}

export async function fetchNearbyEmergencyPOIs(lat, lng, radius = 3000) {
  const query = `[out:json];(node(around:${radius},${lat},${lng})[amenity~"^(hospital|pharmacy|police)$"];way(around:${radius},${lat},${lng})[amenity~"^(hospital|pharmacy|police)$"];);out center 20;`;
  const response = await fetch(`https://overpass-api.de/api/interpreter?data=${encodeURIComponent(query)}`, {
    cache: 'no-store'
  });

  if (!response.ok) {
    throw new Error('Nearby support search failed.');
  }

  const data = await response.json();
  const elements = Array.isArray(data.elements) ? data.elements : [];

  return elements
    .map((element) => {
      const coordinates = element.type === 'node'
        ? { lat: element.lat, lng: element.lon }
        : { lat: element.center?.lat, lng: element.center?.lon };
      const type = element.tags?.amenity;

      return {
        id: element.id,
        name: element.tags?.name || AMENITY_LABELS[type] || 'Emergency support',
        type,
        label: AMENITY_LABELS[type] || 'Emergency support',
        ...coordinates
      };
    })
    .filter((poi) => Number.isFinite(poi.lat) && Number.isFinite(poi.lng))
    .map((poi) => ({ ...poi, distance: Math.round(distanceInMeters(lat, lng, poi.lat, poi.lng)) }))
    .sort((first, second) => first.distance - second.distance)
    .slice(0, 8);
}
