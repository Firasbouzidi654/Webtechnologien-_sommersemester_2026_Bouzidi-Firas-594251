ALTER TABLE medications ADD COLUMN IF NOT EXISTS day_of_week VARCHAR(10);

UPDATE medications
SET frequency = 'SPECIFIC_DAY', day_of_week = 'MONDAY'
WHERE frequency = 'WEEKDAYS_ONLY';
