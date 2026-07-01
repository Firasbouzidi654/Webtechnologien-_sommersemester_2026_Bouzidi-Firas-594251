ALTER TABLE medications ADD COLUMN IF NOT EXISTS scheduled_date VARCHAR(10);

UPDATE medications
SET scheduled_date = COALESCE(scheduled_date, start_date, CAST(CURRENT_DATE AS VARCHAR))
WHERE scheduled_date IS NULL;

ALTER TABLE medications DROP COLUMN IF EXISTS frequency;
ALTER TABLE medications DROP COLUMN IF EXISTS interval_days;
ALTER TABLE medications DROP COLUMN IF EXISTS day_of_week;
ALTER TABLE medications DROP COLUMN IF EXISTS start_date;
