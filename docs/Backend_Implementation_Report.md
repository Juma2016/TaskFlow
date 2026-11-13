# TaskFlow – Backend Implementation Report

**Branch:** `feature/backend-api`  
**Latest integration commit:** `2ee908b`  
**Commit message:**
`feat(backend): integrate SQLite repository and task filtering`  
**Stack:** Java 17, Maven, Javalin 7.2.4, SQLite JDBC 3.45.1.0, Jackson,
JUnit 5  
**Status:** Backend core implemented, locally tested, committed, and
pushed.

------------------------------------------------------------------------

## 1. Executive Summary

This report documents the backend work completed on
`feature/backend-api`, including the integration of Liyu’s SQLite
implementation.

TaskFlow provides a REST API for creating, retrieving, updating, and
deleting tasks. The original in-memory task storage was replaced with a
SQLite-backed repository. The integrated database layer also supports
task status history, filtering, sorting, statistics, and JSON
backup/restore at repository level.

The database code was integrated selectively rather than merging the
entire database branch. This preserved the existing backend
implementation and its tests while adapting the service and application
startup to use the repository.

## 2. Technology Stack

| Technology       | Version / Purpose                   |
|------------------|-------------------------------------|
| Java             | 17                                  |
| Maven            | Build and dependency management     |
| Javalin          | 7.2.4 – HTTP server and REST routes |
| SQLite JDBC      | 3.45.1.0 – database connectivity    |
| Jackson Databind | 2.22.4 in commit `2ee908b`          |
| Jackson JSR-310  | 2.22.4 – Java date/time support     |
| SLF4J Simple     | 2.0.17 – logging                    |
| JUnit Jupiter    | 5.10.2 – automated tests            |

**Dependency coordination note:** After this commit, a teammate reported
that Jackson `2.22.4` was not available from Maven and proposed changing
it to `2.16.1`. That proposal is a subsequent integration change, not
part of commit `2ee908b`. The team should verify and agree on the final
version.

## 3. Project Structure

Backend-related structure after the SQLite integration:

``` text
src/
├── main/
│   ├── java/com/taskflow/
│   │   ├── Main.java
│   │   ├── db/
│   │   │   ├── DatabaseManager.java
│   │   │   ├── TaskRepository.java
│   │   │   └── SqliteTaskRepository.java
│   │   ├── dto/
│   │   │   └── CreateTaskRequest.java
│   │   ├── model/
│   │   │   ├── Priority.java
│   │   │   ├── Task.java
│   │   │   ├── TaskFilterCriteria.java
│   │   │   ├── TaskStatistics.java
│   │   │   ├── TaskStatus.java
│   │   │   └── TaskStatusHistory.java
│   │   ├── service/
│   │   │   └── TaskService.java
│   │   └── validation/
│   │       └── TaskValidator.java
│   └── resources/
│       └── schema.sql
└── test/java/com/taskflow/
    ├── api/TaskApiTest.java
    ├── service/TaskServiceTest.java
    └── validation/TaskValidatorTest.java
```

### 3.1 Component responsibilities

| Component              | Responsibility                                                              |
|------------------------|-----------------------------------------------------------------------------|
| `Main`                 | Initializes database, repository, service, Javalin and REST routes          |
| `DatabaseManager`      | Opens SQLite connections and initializes schema                             |
| `TaskRepository`       | Persistence abstraction used by the service                                 |
| `SqliteTaskRepository` | SQLite implementation: CRUD, filtering, statistics, history, backup/restore |
| `TaskService`          | Application-level task operations; delegates persistence to repository      |
| `Task`                 | Task domain model, including UUID, dates, status and history                |
| `TaskFilterCriteria`   | Optional status, priority, due-date and keyword criteria                    |
| `TaskStatistics`       | Aggregated task counts, including overdue count                             |
| `TaskStatusHistory`    | One task status-change record                                               |
| `CreateTaskRequest`    | HTTP request DTO for create/update                                          |
| `TaskValidator`        | Validates incoming task data                                                |
| `schema.sql`           | Tables, constraints and indexes                                             |

## 4. Architecture and Design

### 4.1 Request and data flow

``` text
HTTP Client
    |
    v
Javalin REST Route (Main.java)
    |
    v
TaskValidator (create/update)
    |
    v
TaskService
    |
    v
TaskRepository interface
    |
    v
SqliteTaskRepository
    |
    v
DatabaseManager
    |
    v
SQLite database
```

The service depends on the `TaskRepository` interface rather than on
SQLite implementation details. `Main` constructs `SqliteTaskRepository`
and injects it into `TaskService`.

### 4.2 Repository Pattern

The repository interface defines the persistence operations:

- `save(Task task)`
- `findById(String id)`
- `findAll(TaskFilterCriteria criteria)`
- `update(Task task)`
- `deleteById(String id)`
- `getStatistics()`
- `exportBackupToJson(Path directory)`
- `restoreFromJson(Path backupFile)`

This separates application logic from SQL and database access.

### 4.3 UUID identifiers

Task identifiers are strings containing UUID values. New tasks receive
UUIDs, which remain stable when tasks are stored and retrieved from
SQLite and accessed through the REST API.

### 4.4 Date/time representation

The `Task` model uses `LocalDateTime` for due dates and creation
timestamps. `CreateTaskRequest` accepts a `LocalDate` due date;
`TaskService` converts it to midnight using `atStartOfDay()`.

A date-boundary issue was identified during team review: the original
overdue SQL compares the stored due date with `LocalDateTime.now()`.
Since date-only due dates are stored at midnight, a task due today may
incorrectly be considered overdue later that day. The proposed
correction is to compare with the beginning of the current day. This
correction must be confirmed in the integrated branch and covered by a
test.

## 5. Database Design

The schema is defined in `src/main/resources/schema.sql`.

### 5.1 `tasks` table

| Column        | Purpose                                                        |
|---------------|----------------------------------------------------------------|
| `id`          | UUID string primary key                                        |
| `title`       | Required task title                                            |
| `description` | Optional description                                           |
| `priority`    | `NIEDRIG`, `MITTEL`, or `HOCH`                                 |
| `status`      | `OFFEN`, `IN_BEARBEITUNG`, or `ABGESCHLOSSEN`; default `OFFEN` |
| `due_date`    | Optional due date/time stored as text                          |
| `created_at`  | Creation timestamp                                             |

The schema constrains priority and status values using SQLite `CHECK`
constraints.

### 5.2 `task_status_history` table

| Column       | Purpose                                      |
|--------------|----------------------------------------------|
| `id`         | Auto-increment history record ID             |
| `task_id`    | Foreign key to the task                      |
| `old_status` | Previous status; nullable for initial record |
| `new_status` | New status                                   |
| `changed_at` | Timestamp of the change                      |

The foreign key references `tasks(id)` with `ON DELETE CASCADE`. With
foreign-key enforcement enabled, deleting a task also deletes its
history records.

### 5.3 Indexes and connection settings

The schema defines indexes on task status, due date, priority plus due
date, and history task ID. `DatabaseManager` configures SQLite
connections with foreign-key enforcement, WAL mode, and a busy timeout.

The default database URL is `jdbc:sqlite:taskflow.db`. The local
database file is a development artifact and should not be committed.

## 6. REST API

The routes were registered in `Main.java`. The application was run
locally at:

``` text
http://localhost:7070
```

### 6.1 Endpoint summary

| Method   | Endpoint           | Purpose                      |
|----------|--------------------|------------------------------|
| `GET`    | `/`                | Welcome message              |
| `GET`    | `/api/tasks`       | List tasks; optional filters |
| `GET`    | `/api/tasks/stats` | Task statistics              |
| `GET`    | `/api/tasks/{id}`  | Retrieve one task            |
| `POST`   | `/api/tasks`       | Create a task                |
| `PUT`    | `/api/tasks/{id}`  | Update a task                |
| `DELETE` | `/api/tasks/{id}`  | Delete a task                |

The statistics route is registered before `/{id}` to ensure
`/api/tasks/stats` is handled as the statistics route.

### 6.2 Create task

Example:

``` http
POST /api/tasks
Content-Type: application/json
```

``` json
{
  "title": "Java Backend Task",
  "description": "Implement REST API",
  "priority": "HOCH",
  "dueDate": "2026-11-20"
}
```

The route deserializes `CreateTaskRequest`, validates it using
`TaskValidator`, calls `TaskService`, and returns the created task with
HTTP `201 Created`.

### 6.3 Read, update and delete

- `GET /api/tasks/{id}` returns the task or HTTP `404` with
  `{"error":"Task not found."}`.
- `PUT /api/tasks/{id}` validates the request and updates the task; a
  missing task returns HTTP `404`.
- `DELETE /api/tasks/{id}` returns HTTP `204 No Content` when
  successful; a missing task returns HTTP `404`.

### 6.4 Error handling

`Main` registers an exception handler for `IllegalArgumentException`.
Invalid input is returned as HTTP `400 Bad Request` with a JSON error
message.

## 7. Filtering, Search and Sorting

`TaskFilterCriteria` contains optional `status`, `priority`,
`dueFilter`, and `keyword` fields. `DueFilter` defines `OVERDUE`,
`TODAY`, and `THIS_WEEK`.

The GET `/api/tasks` route reads query parameters and passes the
criteria through `TaskService` to the repository.

| Query parameter | Example             | Purpose                      |
|-----------------|---------------------|------------------------------|
| `status`        | `status=OFFEN`      | Filter by status             |
| `priority`      | `priority=HOCH`     | Filter by priority           |
| `keyword`       | `keyword=SQLite`    | Search title and description |
| `dueFilter`     | `dueFilter=OVERDUE` | Filter by due-date category  |

Examples:

``` http
GET /api/tasks?status=OFFEN
GET /api/tasks?priority=HOCH
GET /api/tasks?keyword=SQLite
GET /api/tasks?dueFilter=TODAY
GET /api/tasks?status=OFFEN&priority=HOCH&keyword=backend
```

Multiple criteria are combined with SQL `AND` conditions. Invalid enum
values raise `IllegalArgumentException`, which the route exception
handler maps to HTTP 400.

### 7.1 SQL safety and keyword search

The repository builds the query dynamically but binds values using
`PreparedStatement` parameters. User-supplied values are not
concatenated into SQL. Keyword search checks both title and description
using `LOWER(...) LIKE ?`, with the keyword trimmed and converted to
lowercase.

### 7.2 Due-date filters

- `OVERDUE`: non-null due date earlier than the comparison timestamp,
  excluding completed tasks.
- `TODAY`: due date between the beginning and end of the current day.
- `THIS_WEEK`: due date between Monday and Sunday of the current week.

**Known issue:** the original overdue comparison uses the current time
rather than the start of today. This can incorrectly mark tasks due
today as overdue. The team identified the fix but it must be verified in
the final integrated branch.

### 7.3 Default sorting

Tasks are ordered by priority (`HOCH`, `MITTEL`, `NIEDRIG`), then tasks
with due dates before tasks without due dates, and finally by due date
ascending.

## 8. Task Status History

The repository records an initial history entry when a task is created:
`oldStatus = null`, `newStatus = OFFEN`.

When a task’s status changes, the repository records the old status, new
status, and timestamp. `findById` returns the task with its history.

Manual testing confirmed these records after changing a task from
`OFFEN` to `IN_BEARBEITUNG`:

| History ID | Old status | New status       |
|------------|------------|------------------|
| 1          | `null`     | `OFFEN`          |
| 2          | `OFFEN`    | `IN_BEARBEITUNG` |

The schema’s cascade rule is intended to remove the history records when
their task is deleted.

## 9. Task Statistics

`SqliteTaskRepository` returns a `TaskStatistics` object containing
total count, counts by status, and overdue count. `TaskService` maps
status counts into the response for `/api/tasks/stats`.

Manual testing returned:

``` json
{
  "total": 1,
  "OFFEN": 1,
  "IN_BEARBEITUNG": 0,
  "ABGESCHLOSSEN": 0
}
```

After updating the task to `IN_BEARBEITUNG`, the status counts changed
as expected.

**Follow-up identified by the team:** `TaskStatistics` already has
`overdueCount`, but the service response at the time of review did not
expose it. The teammate planned to add it to `/api/tasks/stats`. Confirm
the final response and tests after integration.

## 10. JSON Backup and Restore

The repository interface and SQLite implementation include:

- `exportBackupToJson(Path directory)`
- `restoreFromJson(Path backupFile)`

These methods are implemented at repository level in the imported
database code. At the time of commit `2ee908b`, backup/restore had not
been verified end-to-end through HTTP, and no backup REST route had been
added in `Main`. A teammate proposed wiring the repository operation
through `TaskService` and exposing an API endpoint for the frontend.

Therefore, repository support should not be confused with a completed
and tested frontend-accessible backup API.

## 11. Automated Tests

JUnit Jupiter 5.10.2 is used.

| Test class          |  Tests |
|---------------------|-------:|
| `TaskServiceTest`   |     12 |
| `TaskValidatorTest` |      5 |
| `TaskApiTest`       |      8 |
| **Total**           | **25** |

The complete Maven run reported:

``` text
Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

`TaskServiceTest` uses a temporary SQLite database. The API tests also
create a service backed by a temporary database and start Javalin on an
available port.

### 11.1 Coverage status

The 25 passing tests confirm the current test suite passes. They do not
prove 80% code coverage. Mockito tests, database-specific tests,
performance tests, and JaCoCo reporting were identified as follow-up
work. The 80% target must not be claimed until a JaCoCo report confirms
it.

## 12. Manual Verification

The application was started using:

``` powershell
mvn exec:java
```

Javalin started successfully and listened on port `7070`.

| Manual check              | Result        | Observation                                               |
|---------------------------|---------------|-----------------------------------------------------------|
| Start application         | Passed        | Javalin listened on port 7070                             |
| GET empty task list       | Passed        | Empty result                                              |
| POST create task          | Passed        | Task and UUID returned                                    |
| GET task list             | Passed        | Created task returned                                     |
| Persistence after restart | Passed        | Same task and UUID remained after restart                 |
| GET statistics            | Passed        | Counts matched stored task                                |
| PUT update                | Passed        | Status changed from `OFFEN` to `IN_BEARBEITUNG`           |
| Status history            | Passed        | Initial and status-change entries returned                |
| Statistics after update   | Passed        | Counts reflected new status                               |
| DELETE task               | Passed        | Task removed; total count became zero                     |
| GET deleted task          | Passed        | `Task not found.` response                                |
| Priority filter           | Passed        | `priority=HOCH` returned only the high-priority test task |
| Keyword filter            | Not confirmed | A test request was prepared, but no result was recorded   |
| Status filter             | Not confirmed | Implemented; no manual result recorded                    |
| Due-date filters          | Not confirmed | Implemented; no manual result recorded                    |
| Backup/restore            | Not confirmed | No end-to-end result recorded                             |

### 12.1 Filter test data

Three test tasks were created:

| Title             | Description                   | Priority  | Status  | Due date   |
|-------------------|-------------------------------|-----------|---------|------------|
| Java Backend Task | Implement REST API            | `HOCH`    | `OFFEN` | 2026-11-20 |
| Database Design   | Design SQLite database        | `MITTEL`  | `OFFEN` | 2026-11-25 |
| Project Meeting   | Prepare project documentation | `NIEDRIG` | `OFFEN` | 2026-11-30 |

The following request was manually tested and returned only
`Java Backend Task`:

``` powershell
Invoke-RestMethod `
    -Uri "http://localhost:7070/api/tasks?priority=HOCH" `
    -Method Get
```

These records were stored in the local `taskflow.db` and were not
committed.

## 13. Git and Team Integration

### 13.1 Branch and commits

Branch:

``` text
feature/backend-api
```

Key commits:

| Commit    | Description                                                     |
|-----------|-----------------------------------------------------------------|
| `f28d6f5` | `refactor(backend): replace Long IDs with UUIDs`                |
| `2ee908b` | `feat(backend): integrate SQLite repository and task filtering` |

The integration commit changed 17 files, with 894 insertions and 126
deletions. It was pushed successfully to `origin/feature/backend-api`,
advancing the remote branch from `f28d6f5` to `2ee908b`.

### 13.2 Selective database integration

Rather than merging the entire database branch, selected database,
model, and schema files were brought into `feature/backend-api`. The
backend service and application startup were adapted to use the
repository. The old enum files were moved from `com.taskflow.enums` to
`com.taskflow.model`.

### 13.3 Local files intentionally not committed

- `.idea/` – local IntelliJ settings;
- `taskflow.iml` – local IntelliJ project file;
- `taskflow.db` – local development database;
- `docs/Backend_Implementation_Report.md` – report file, unless the team
  explicitly chooses to commit it.

## 14. Follow-up Work from Team Review

The following tasks were raised by a teammate after reviewing the
branches. They are follow-up integration work and should not be
described as completed in commit `2ee908b`.

1.  Verify and agree on the Jackson version; the teammate proposed
    changing `2.22.4` to `2.16.1`.
2.  Move route registration from `Main.java` into a dedicated API/routes
    class.
3.  Update API tests to use the real route registration rather than
    duplicating route definitions.
4.  Include `overdueCount` in the `/api/tasks/stats` response.
5.  Fix the overdue date boundary by comparing against the start of the
    current day.
6.  Connect JSON backup through `TaskService` and add a backup API route
    for frontend use.
7.  Create the agreed `public` directory for frontend files.
8.  Add Mockito and appropriate mock-based tests.
9.  Add JaCoCo and verify at least 80% test coverage.
10. Add database-specific tests, performance/speed tests, and tests for
    filters and backup/restore.
11. Update README and project documentation after integration.

## 15. Conclusion

The backend core and SQLite integration were implemented, tested,
committed, and pushed to `feature/backend-api`.

Successfully demonstrated capabilities: - REST CRUD operations; - SQLite
persistence across application restarts; - repository-based persistence
architecture; - task status history; - task statistics by status; -
priority filtering; - 25 automated tests passing with zero failures.

The branch was ready for team integration. However, the complete project
backend should only be considered finished after the follow-up items
have been integrated and verified. In particular, the JaCoCo 80% target,
backup API, overdue statistics field, overdue date correction, and
remaining filter tests still require confirmation.

## Appendix A – Useful Commands

Run tests:

``` powershell
mvn clean test
```

Start the application:

``` powershell
mvn exec:java
```

Check Git status:

``` powershell
git status
```

Push the backend branch:

``` powershell
git push origin feature/backend-api
```

## Appendix B – Example API Requests

Get all tasks:

``` powershell
Invoke-RestMethod -Uri "http://localhost:7070/api/tasks" -Method Get
```

Filter by priority:

``` powershell
Invoke-RestMethod -Uri "http://localhost:7070/api/tasks?priority=HOCH" -Method Get
```

Search by keyword:

``` powershell
Invoke-RestMethod -Uri "http://localhost:7070/api/tasks?keyword=SQLite" -Method Get
```

Filter by status:

``` powershell
Invoke-RestMethod -Uri "http://localhost:7070/api/tasks?status=OFFEN" -Method Get
```

Filter overdue tasks:

``` powershell
Invoke-RestMethod -Uri "http://localhost:7070/api/tasks?dueFilter=OVERDUE" -Method Get
```

Get statistics:

``` powershell
Invoke-RestMethod -Uri "http://localhost:7070/api/tasks/stats" -Method Get
```
