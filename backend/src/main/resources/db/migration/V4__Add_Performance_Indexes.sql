-- Indexes for faster queries on the children table
CREATE INDEX IF NOT EXISTS idx_children_name ON children(name);

-- Indexes for faster child-based lookups on related tables
CREATE INDEX IF NOT EXISTS idx_medications_child_id ON medications(child_id);
CREATE INDEX IF NOT EXISTS idx_emergency_contacts_child_id ON emergency_contacts(child_id);
CREATE INDEX IF NOT EXISTS idx_parent_notes_child_id ON parent_notes(child_id);
CREATE INDEX IF NOT EXISTS idx_medication_logs_medication_id ON medication_logs(medication_id);
