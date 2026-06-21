// External API 3: public medicine labels from openFDA.
export async function searchMedicationInfo(searchTerm, { limit = 1 } = {}) {
  if (!searchTerm) return [];

  const query = `openfda.brand_name:"${searchTerm.replace(/"/g, '')}"`;
  const url = `https://api.fda.gov/drug/label.json?search=${encodeURIComponent(query)}&limit=${limit}`;
  const response = await fetch(url);
  if (response.status === 404) return [];
  if (!response.ok) throw new Error('Medication information is unavailable.');

  const results = (await response.json()).results || [];
  return results.map((item) => ({
    medicationName: item.openfda?.brand_name?.[0] || item.openfda?.generic_name?.[0] || searchTerm,
    purpose: item.purpose?.[0] || item.indications_and_usage?.[0] || 'No public label information found.'
  }));
}
