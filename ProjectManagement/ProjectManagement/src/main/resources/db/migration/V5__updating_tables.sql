ALTER TABLE users
DROP COLUMN if exists role_id;

ALTER TABLE projects
DROP CONSTRAINT if exists idx_owner_id;

ALTER TABLE tasks
add column estimated_effort_hours NUMERIC(6,2) default 0.0;