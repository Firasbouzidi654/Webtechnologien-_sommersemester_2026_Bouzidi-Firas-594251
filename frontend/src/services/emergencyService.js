import { formatDistanceMeters } from '../utils/formatters';

const AMENITY_PRIORITY = {
  hospital: 1,
  police: 2,
  pharmacy: 3,
  poi: 99
};

const AMENITY_LABEL = {
  hospital: 'Hospital',
  police: 'Police station',
  pharmacy: 'Pharmacy',
  poi: 'Emergency support'
};

const AMENITY_ICON = {
  hospital: '🏥',
  police: '🚓',
  pharmacy: '💊',
  poi: '📍'
};

export function poiTypeLabel(type) {
  return AMENITY_LABEL[type] || AMENITY_LABEL.poi;
}

export function poiTypeIcon(type) {
  return AMENITY_ICON[type] || AMENITY_ICON.poi;
}

export function buildEmergencyRouteLink(from, to) {
  if (!from || !to) {
    return '#';
  }

  return `https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route=${from.lat},${from.lng};${to.lat},${to.lng}`;
}

function getAmenityPriority(amenity) {
  return AMENITY_PRIORITY[amenity] || AMENITY_PRIORITY.poi;
}

function haversineDistance(lat1, lon1, lat2, lon2) {
  const toRad = (value) => (value * Math.PI) / 180;
  const R = 6371000;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

export async function fetchNearbyEmergencyPOIs(lat, lng, radius = 3000) {
  const query = `[out:json];(node(around:${radius},${lat},${lng})[amenity~"^(hospital|pharmacy|police)$"];way(around:${radius},${lat},${lng})[amenity~"^(hospital|pharmacy|police)$"];);out center 20;`;
  const url = `https://overpass-api.de/api/interpreter?data=${encodeURIComponent(query)}`;

  try {
    const response = await fetch(url, { cache: 'no-store' });
    const data = await response.json();
    const elements = data.elements || [];

    return elements
      .map((element) => {
        const coordinate = element.type === 'node'
          ? { lat: element.lat, lng: element.lon }
          : { lat: element.center?.lat, lng: element.center?.lon };

        return {
          id: element.id,
          name: element.tags?.name || poiTypeLabel(element.tags?.amenity),
          type: element.tags?.amenity || 'poi',
          lat: coordinate.lat,
          lng: coordinate.lng,
          tags: element.tags || {}
        };
      })
      .filter((poi) => poi.lat && poi.lng)
      .map((poi) => ({
        ...poi,
        distance: Math.round(haversineDistance(lat, lng, poi.lat, poi.lng)),
        icon: poiTypeIcon(poi.type),
        label: poiTypeLabel(poi.type)
      }))
      .sort((a, b) => {
        const priority = getAmenityPriority(a.type) - getAmenityPriority(b.type);
        if (priority !== 0) {
          return priority;
        }
        return a.distance - b.distance;
      })
      .slice(0, 8);
  } catch (error) {
    console.warn('Unable to fetch nearby emergency POIs', error);
    return [];
  }
}
