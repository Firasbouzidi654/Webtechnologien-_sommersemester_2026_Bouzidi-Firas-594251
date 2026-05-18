const OPENFDA_LABEL_ENDPOINT = 'https://api.fda.gov/drug/label.json';
const REQUEST_TIMEOUT_MS = 12000;

function withTimeout(signal) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  if (signal) {
    signal.addEventListener('abort', () => controller.abort(), { once: true });
  }

  return { controller, timeoutId };
}

function firstArrayValue(value) {
  if (Array.isArray(value)) {
    return value.find((item) => Boolean(String(item || '').trim())) || '';
  }

  return String(value || '').trim();
}

function truncateText(value, maxLength = 520) {
  const text = firstArrayValue(value).replace(/\s+/g, ' ').trim();

  if (!text) {
    return '';
  }

  return text.length > maxLength ? `${text.slice(0, maxLength).trim()}...` : text;
}

function fieldFallback(record, fields, fallback = 'Not listed in this label.') {
  const value = fields
    .map((field) => truncateText(record?.[field]))
    .find(Boolean);

  return value || fallback;
}

function openFdaValue(record, field) {
  return firstArrayValue(record?.openfda?.[field]);
}

function normalizeMedication(record) {
  const brandName = openFdaValue(record, 'brand_name');
  const genericName = openFdaValue(record, 'generic_name');

  return {
    id: record?.id || record?.set_id || `${brandName}-${genericName}`,
    medicationName: brandName || genericName || 'Medication label',
    genericName: genericName || 'Not listed',
    description: fieldFallback(record, ['indications_and_usage', 'purpose', 'active_ingredient']),
    purpose: fieldFallback(record, ['purpose', 'indications_and_usage']),
    warnings: fieldFallback(record, [
      'boxed_warning',
      'warnings',
      'do_not_use',
      'ask_doctor',
      'ask_doctor_or_pharmacist',
      'stop_use'
    ]),
    sideEffects: fieldFallback(record, ['adverse_reactions', 'side_effects'], 'No adverse reaction field was listed in this label.'),
    dosage: fieldFallback(record, ['dosage_and_administration', 'directions'], 'No dosage directions were listed in this label.'),
    manufacturer: openFdaValue(record, 'manufacturer_name') || 'Manufacturer not listed'
  };
}

function buildMedicationSearch(searchTerm) {
  const safeTerm = String(searchTerm || '').trim().replace(/"/g, '');
  const terms = safeTerm.toLowerCase() === 'paracetamol'
    ? [safeTerm, 'acetaminophen']
    : [safeTerm];

  return terms
    .flatMap((term) => [
      `openfda.brand_name:"${term}"`,
      `openfda.generic_name:"${term}"`,
      `active_ingredient:"${term}"`,
      `purpose:"${term}"`
    ])
    .join(' OR ');
}

export async function searchMedicationInfo(searchTerm, { limit = 6, signal } = {}) {
  const query = String(searchTerm || '').trim();

  if (!query) {
    return [];
  }

  const { controller, timeoutId } = withTimeout(signal);
  const params = new URLSearchParams({
    search: buildMedicationSearch(query),
    limit: String(limit)
  });

  try {
    const response = await fetch(`${OPENFDA_LABEL_ENDPOINT}?${params.toString()}`, {
      cache: 'no-store',
      signal: controller.signal
    });

    if (response.status === 404) {
      return [];
    }

    if (!response.ok) {
      throw new Error(`OpenFDA request failed with status ${response.status}`);
    }

    const data = await response.json();

    return (data?.results || []).map(normalizeMedication);
  } finally {
    window.clearTimeout(timeoutId);
  }
}
