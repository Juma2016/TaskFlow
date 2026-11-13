# Fortschrittsdokumentation & Code-Walkthrough – TaskFlow

|                |                                                             |
| -------------- | ----------------------------------------------------------- |
| **Gruppe**     | QualityForge                                                |
| **Mitglieder** | Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu       |
| **Datum**      | 13.11.2026                                                  |
| **Thema**      | Integration, Architektur-Walkthrough, Bugfixing und Testing |

## Was haben wir gemacht?

- Liyu hat das Datenmodell, das SQLite-Schema (`schema.sql`) und das `SqliteTaskRepository` (inkl. Filterung, Status-Historie und JSON-Backup) umgesetzt.
- Ali hat im Backend-Branch den `TaskService`, die Eingabeprüfung (`TaskValidator`), das DTO (`CreateTaskRequest`), die REST-Routen und die ersten Unit- und API-Tests implementiert.
- Mario hat die Branches zusammengeführt, einen Code- und Struktur-Walkthrough durchgeführt, die Paketstruktur aufgeräumt (`TaskApp`), kleine Bugs behoben sowie die fehlenden Tests (DB, Mockito, Geschwindigkeit) und JaCoCo ergänzt.

## Ergebnisse unseres Walkthroughs (Was haben wir gefunden und behoben?)

Beim gemeinsamen Prüfen des Codes und der Anforderungen sind uns folgende Punkte aufgefallen, die wir direkt behoben haben:

| Gefundenes Problem | Ursache | Unsere Lösung |
|---|---|---|
| Build-Fehler bei Jackson in `pom.xml` | Im Backend-Branch war die Version `2.22.4` eingetragen, die in Maven Central nicht existiert. | Wir haben die Version wieder auf `2.16.1` gesetzt und Mockito sowie JaCoCo ergänzt. |
| Doppelte Routen-Definition in `Main` und `TaskApiTest` | Alle Javalin-Routen standen direkt in `Main.java` und waren im Test kopiert. | Wir haben die Routen in `com.taskflow.api.TaskApp` ausgelagert. Jetzt nutzen `Main` und `TaskApiTest` exakt denselben Code. |
| Überfällige Aufgaben fehlten in `/api/tasks/stats` | `SqliteTaskRepository` hat `overdueCount` berechnet, aber `TaskService.getTaskStats()` hat den Wert nicht in die Map aufgenommen. | Wir geben jetzt `"overdue"` in `TaskService.getTaskStats()` mit zurück. |
| Aufgaben von „heute“ wurden als „überfällig“ gezählt | Das Fälligkeitsdatum wird mit `00:00` Uhr gespeichert, in der DB wurde aber mit `LocalDateTime.now()` verglichen. | Wir vergleichen bei `OVERDUE` und in der Statistik jetzt mit dem Tagesbeginn (`LocalDate.now().atStartOfDay()`). |
| JSON-Backup war über die API noch nicht erreichbar | Die Backup-Methode war nur im Repository vorhanden. | Wir haben `exportBackup` im `TaskService` und die Route `POST /api/tasks/backup` ergänzt. |
| Ordner für das Frontend fehlte | Für die Web-Oberfläche gab es noch keinen festen Ablageort. | Wir haben `src/main/resources/public/` angelegt und in `TaskApp` unter `/ui` eingebunden. |

## Was haben wir getestet?

Wir haben insgesamt **37 automatisierte Tests** mit JUnit 5 und Mockito ausgeführt:

| Testklasse | Was wird geprüft? | Ergebnis |
|---|---|---|
| `TaskValidatorTest` (5 Tests) | Pflichtfelder (`title`, `priority`), leere Eingaben und `null`-Werte. | 5 / 5 bestanden |
| `TaskServiceTest` (12 Tests) | Erstellen, Lesen, Aktualisieren, Löschen, UUID-Vergabe und Statistik mit echter SQLite-Testdatenbank. | 12 / 12 bestanden |
| `TaskServiceMockitoTest` (5 Tests) | Isolierte Prüfung der Geschäftslogik im `TaskService` mit gemocktem `TaskRepository` (Mockito). | 5 / 5 bestanden |
| `SqliteTaskRepositoryTest` (4 Tests) | Speichern mit Status-Historie, kombinierte Filter (`OVERDUE`, `TODAY`, `THIS_WEEK`, Suchbegriff), JSON-Backup & Restore sowie Filter-Geschwindigkeit ($\le 100\text{ ms}$). | 4 / 4 bestanden |
| `TaskApiTest` (11 Tests) | Alle REST-Endpunkte (`GET`, `POST`, `PUT`, `DELETE`, Filter-Parameter, `/api/tasks/stats`, `/api/tasks/backup`) inkl. Fehlercodes `400` und `404`. | 11 / 11 bestanden |

### Gemessene Testabdeckung (JaCoCo)

- **Geschäftslogik (`TaskService`):** **100 %** Line Coverage (Vorgabe: $\ge 80\,\%$)
- **Validierung & Modelle (`TaskValidator`, `model`, `dto`):** **> 90 %** Line Coverage
- **REST-API (`TaskApp`):** **98 %** Line Coverage
- **Datenbank (`SqliteTaskRepository`, `DatabaseManager`):** **88 %** Line Coverage
- **Gesamtprojekt:** **ca. 90 %** Line Coverage

## Wer hat woran gearbeitet?

| Name | Bisherige Arbeiten | Nächste Schritte |
|---|---|---|
| Mario Shenouda | Projekt- & Paketstruktur (`TaskApp`), Merge, Code-Walkthrough, Bugfixes, Mockito-/DB-/Speed-Tests, JaCoCo-Setup und Dokumentation | Endabnahme-Tests mit dem fertigen Frontend |
| Ali Zolfaghari | Implementierung von `TaskService`, `TaskValidator`, `CreateTaskRequest`, REST-Endpunkten sowie `TaskServiceTest`, `TaskValidatorTest` und `TaskApiTest` | Unterstützung bei API-Fragen fürs Frontend |
| Liyu Wu | SQLite-Datenbankschema (`schema.sql`), `DatabaseManager`, `SqliteTaskRepository` (inkl. Filter, Statistik, Status-Historie und JSON-Backup) | Unterstützung bei Datenbankfragen |
| Maryam Taied | Vorbereitung der Web-Oberfläche | Umsetzung des Frontends unter `src/main/resources/public/` |
