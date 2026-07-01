DELETE FROM medications
WHERE child_id IS NULL
   OR NOT EXISTS (
       SELECT 1
       FROM children
       WHERE children.id = medications.child_id
   );

ALTER TABLE medications ALTER COLUMN child_id SET NOT NULL;

ALTER TABLE medications
    ADD CONSTRAINT fk_medications_child
    FOREIGN KEY (child_id)
    REFERENCES children(id)
    ON DELETE CASCADE;
