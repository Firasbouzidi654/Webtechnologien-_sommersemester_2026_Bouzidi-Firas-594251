-- Add optional photo URL / Base64 column to children table
ALTER TABLE children ADD COLUMN IF NOT EXISTS photo_url TEXT;
