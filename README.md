# TaskFlow

Wir entwickeln TaskFlow als Webanwendung für die Lehrveranstaltung Software Quality. Damit sollen Benutzer ihre Aufgaben anlegen, bearbeiten, filtern und löschen können.

**Team QualityForge:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu.

## Unser aktueller Stand

Datenbank (SQLite), Aufgabenlogik (`TaskService`), Eingabeprüfung (`TaskValidator`), JSON-Backup und die REST-API mit Javalin sind eingebaut und mit automatisierten Tests (JUnit 5, Mockito und JaCoCo) geprüft. Als Nächstes bauen wir die Browseroberfläche unter `src/main/resources/public/` weiter aus.

- [Unsere Qualitätsplanung](docs/Qualitaetsplan.md)
- [Unser Fortschritt vom 27.10.2026](docs/Fortschritt_2026-10-27.md)
- [Unser Walkthrough und Fortschritt vom 13.11.2026](docs/Fortschritt_2026-11-13.md)

## Projekt starten

Wir brauchen ein JDK ab Version 17, Maven 3.9.x und Git. In VS Code öffnen wir den Ordner mit der `pom.xml` und warten, bis VS Code das Maven-Projekt geladen hat.

Mit `java -version` und `mvn -version` prüfen wir die installierten Versionen. Danach starten wir im Projektordner:

```sh
mvn clean compile
mvn compile exec:java
```

Die Anwendung läuft unter `http://localhost:7070/`. Mit `Ctrl+C` im Terminal stoppen wir sie wieder.

## Verfügbare Routen (REST-API)

| Methode & Route | Beschreibung |
|---|---|
| `GET /` | Begrüßungstext („Willkommen bei TaskFlow!“) |
| `GET /ui/` | Statische Dateien für die Browseroberfläche (`src/main/resources/public/`) |
| `GET /api/tasks` | Alle Aufgaben als JSON abrufen (optional mit `?status=`, `?priority=`, `?dueFilter=`, `?keyword=`) |
| `GET /api/tasks/{id}` | Einzelne Aufgabe inkl. Status-Historie abrufen |
| `POST /api/tasks` | Neue Aufgabe erstellen (JSON-Body mit `title`, `description`, `priority`, `dueDate`) |
| `PUT /api/tasks/{id}` | Bestehende Aufgabe aktualisieren |
| `DELETE /api/tasks/{id}` | Aufgabe löschen |
| `GET /api/tasks/stats` | Statistik (Gesamtanzahl, Anzahl pro Status und überfällige Aufgaben) |
| `POST /api/tasks/backup` | Aktuelle Datenbasis als JSON-Datei mit Zeitstempel im Ordner `backups/` sichern |

## Tests und Testabdeckung ausführen

Mit folgendem Befehl führen wir alle Unit-, Repository-, Mockito- und API-Tests aus und erzeugen den JaCoCo-Bericht:

```sh
mvn test
```

Der Coverage-Bericht liegt danach unter `target/site/jacoco/index.html`. Aktuell erreichen wir über **90 % Testabdeckung** (Vorgabe: mindestens 80 % für die Geschäftslogik).

## Projekt- und Paketstruktur

| Pfad / Package | Inhalt |
|---|---|
| `src/main/java/com/taskflow/Main.java` | Startpunkt der Anwendung (initialisiert DB, Service und startet Javalin auf Port 7070). |
| `src/main/java/com/taskflow/api/TaskApp.java` | Konfiguration der Javalin-App, REST-Routen, Fehlerbehandlung und Static-Files. |
| `src/main/java/com/taskflow/service/TaskService.java` | Geschäftslogik für Aufgaben, Filterung, Statistik und Backup. |
| `src/main/java/com/taskflow/validation/TaskValidator.java` | Prüfung der Pflichtfelder (`title`, `priority`). |
| `src/main/java/com/taskflow/dto/CreateTaskRequest.java` | Request-Objekt für das Erstellen und Aktualisieren von Aufgaben. |
| `src/main/java/com/taskflow/db/` | `DatabaseManager`, `TaskRepository` und `SqliteTaskRepository` für den SQLite-Datenzugriff und JSON-Backups. |
| `src/main/java/com/taskflow/model/` | Datenmodelle (`Task`, `Priority`, `TaskStatus`, `TaskStatusHistory`, `TaskFilterCriteria`, `TaskStatistics`). |
| `src/main/resources/schema.sql` | SQLite-Tabellen (`tasks`, `task_status_history`) und Indizes. |
| `src/main/resources/public/` | Ordner für die Web-Oberfläche (HTML/CSS/JS). |
| `src/test/java/com/taskflow/` | Automatisierte Tests (`TaskApiTest`, `TaskServiceTest`, `TaskServiceMockitoTest`, `TaskValidatorTest`, `SqliteTaskRepositoryTest`). |
| `docs/` | Qualitätsplan und Fortschrittsberichte. |
