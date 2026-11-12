-- Enable foreign key checks and WAL mode to improve concurrent read/write performance
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- 1. Task entity table (tasks)
CREATE TABLE IF NOT EXISTS tasks (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT,
    priority TEXT NOT NULL CHECK(priority IN ('NIEDRIG', 'MITTEL', 'HOCH')),
    status TEXT NOT NULL DEFAULT 'OFFEN' CHECK(status IN ('OFFEN', 'IN_BEARBEITUNG', 'ABGESCHLOSSEN')),
    due_date TEXT,
    created_at TEXT NOT NULL
);

-- 2. Task status history table (task_status_history)
CREATE TABLE IF NOT EXISTS task_status_history (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    task_id TEXT NOT NULL,
    old_status TEXT,
    new_status TEXT NOT NULL,
    changed_at TEXT NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- 3. B-Tree indexes for efficient retrieval and sorting of 100,000 records
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_priority_due ON tasks(priority, due_date);
CREATE INDEX IF NOT EXISTS idx_history_task_id ON task_status_history(task_id);