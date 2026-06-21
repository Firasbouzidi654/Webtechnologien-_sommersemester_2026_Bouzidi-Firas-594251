export async function getGermanPublicHolidays() {
  const year = new Date().getFullYear();
  const response = await fetch(`https://date.nager.at/api/v3/PublicHolidays/${year}/DE`);
  if (!response.ok) throw new Error('Public holidays are unavailable.');
  const holidays = await response.json();
  return holidays.slice(0, 3);
}
