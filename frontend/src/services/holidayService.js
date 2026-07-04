const HOLIDAY_LIMIT = 3;

function todayDateKey() {
  const date = new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

async function fetchGermanPublicHolidays(year) {
  const response = await fetch(`https://date.nager.at/api/v3/PublicHolidays/${year}/DE`);
  if (!response.ok) throw new Error('Public holidays are unavailable.');
  return response.json();
}

function upcomingHolidays(holidays, dateKey) {
  return holidays
    .filter((holiday) => holiday.date >= dateKey)
    .sort((first, second) => first.date.localeCompare(second.date));
}

export async function getGermanPublicHolidays() {
  const today = todayDateKey();
  const year = Number(today.slice(0, 4));
  const currentYearHolidays = await fetchGermanPublicHolidays(year);
  let holidays = upcomingHolidays(currentYearHolidays, today);

  // Show only future holidays; when this year is exhausted, continue with next year.
  if (holidays.length === 0) {
    holidays = upcomingHolidays(await fetchGermanPublicHolidays(year + 1), `${year + 1}-01-01`);
  }

  return holidays.slice(0, HOLIDAY_LIMIT);
}
