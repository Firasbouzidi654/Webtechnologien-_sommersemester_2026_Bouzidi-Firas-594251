const OPEN_FDA_LABEL_URL = 'https://api.fda.gov/drug/label.json';

function firstLabelValue(item, fields, fallback) {
  for (const field of fields) {
    const value = item[field]?.[0];
    if (typeof value === 'string' && value.trim()) {
      return value;
    }
  }

  return fallback;
}

export async function searchMedicationInfo(searchTerm, { limit = 5, signal } = {}) {
  if (!searchTerm?.trim()) return [];

  const cleanTerm = searchTerm.trim().replace(/"/g, '');

  const query = `(openfda.brand_name:"${cleanTerm}" OR openfda.generic_name:"${cleanTerm}")`;
  const params = new URLSearchParams({
    search: query,
    limit: String(limit)
  });
  const response = await fetch(`${OPEN_FDA_LABEL_URL}?${params.toString()}`, { signal });

  if (response.status === 404) return [];
  if (!response.ok) {
    throw new Error('OpenFDA request failed.');
  }

  let data;
  try {
    data = await response.json();
  } catch {
    throw new Error('OpenFDA returned an invalid response.');
  }

  const results = Array.isArray(data.results) ? data.results : [];

  return results.map((item, index) => ({
    id: item.id || item.set_id || item.spl_id || `${cleanTerm}-${index}`,
    medicationName:
      item.openfda?.brand_name?.[0] ||
      item.openfda?.generic_name?.[0] ||
      cleanTerm,

    genericName: item.openfda?.generic_name?.[0] || 'Not listed',

    manufacturer: item.openfda?.manufacturer_name?.[0] || 'Manufacturer not listed',

    purpose: firstLabelValue(item, ['purpose', 'indications_and_usage'], 'No public label information found.'),

    warnings: firstLabelValue(item, ['warnings', 'boxed_warning'], 'Not listed in this label.'),

    sideEffects: firstLabelValue(item, ['adverse_reactions'], 'Not listed in this label.'),

    dosage: firstLabelValue(item, ['dosage_and_administration'], 'Not listed in this label.')
  }));
}
