ALTER TABLE children ADD COLUMN IF NOT EXISTS parent_id BIGINT;

ALTER TABLE children
    ADD CONSTRAINT fk_children_parent
    FOREIGN KEY (parent_id)
    REFERENCES users(id)
    ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_children_parent_id ON children(parent_id);
