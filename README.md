# TaskFlow

Task-Management-Webanwendung für die Lehrveranstaltung Software Quality.

**Team QualityForge:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu.

## Stand

Lauffähiges Java-/Maven-/Javalin-Gerüst mit zwei Beispielrouten. Noch keine Aufgabenverwaltung, SQLite-Persistenz, JSON-API oder vollständige Browseroberfläche. Qualitätsplanung und Sitzungsprotokoll liegen unter docs/.

- [Qualitätsplan](docs/Qualitaetsplan.md)
- [Fortschritt vom 27.10.2026](docs/Fortschritt_2026-10-27.md)

## Voraussetzungen und Start

- JDK 17 oder höher, Maven 3.9.x und Git.
- Den Ordner mit dieser pom.xml direkt in VS Code öffnen und den Maven-Import abwarten.
- java -version und mvn -version prüfen; Maven muss ein JDK verwenden.

```sh
mvn clean compile
mvn compile exec:java
```

Anwendung: http://localhost:7070/ . Stoppen mit Ctrl+C im ausführenden Terminal.

| Route | Aktuelles Ergebnis |
|---|---|
| GET / | HTTP 200, Willkommen bei TaskFlow! |
| GET /api/tasks | HTTP 200, Hier werden die Aufgaben angezeigt. |

Die zweite Route ist ein Text-Platzhalter; der geforderte JSON-Vertrag folgt später.

## Manuelle Gerüstprüfung

In einem zweiten Terminal bei laufender Anwendung:

```sh
curl -i http://localhost:7070/
curl -i http://localhost:7070/api/tasks
```

Beide Antworten müssen Status 200 und die oben genannten Texte liefern. Diese Smoke-Tests ersetzen keine Fachtests. Automatisierte Tests und Testabdeckung sind noch nicht eingerichtet.

Bei „Address already in use“ den bisherigen lokalen Server mit Ctrl+C stoppen. Bei Package-/Importfehlern den richtigen Projektordner öffnen und den Maven-Import prüfen. Warnungen von Maven unter neueren JDKs sind von tatsächlichen Buildfehlern zu unterscheiden; Kontrollbuild mit JDK 21 wurde erfolgreich durchgeführt.

## Struktur und Qualitätskonventionen

- src/main/java/com/taskflow/Main.java: Programmeinstieg und Beispielrouten.
- pom.xml: Java-Zielversion 17, Javalin, Logging, Compiler und Startkonfiguration.
- .vscode/settings.json: automatische Maven-Projektaktualisierung.
- docs/: Qualitätsplanung und Fortschrittsdokumentation.
- target/: generierte Dateien; durch .gitignore ausgeschlossen.

Geplant: getrennte HTTP-, Geschäftslogik- und Datenzugriffsschichten; SQLite, Jackson, JUnit 5, Mockito, JaCoCo. Mindestens 80 % Testabdeckung bei Abnahme, keine bekannten kritischen Defekte, verständliche Benutzerdokumentation.

## Repository und offene Abgabevoraussetzungen

Aktuelles Repository: https://github.com/Juma2016/TaskFlow . Das Übungsblatt verlangt GitLab. Noch ist kein GitLab-Ziel bekannt und keine Zustimmung zur Plattformabweichung dokumentiert. Bestehende Historie und GitHub-origin wurden erhalten. Ein späteres GitLab-Ziel kann als zusätzlicher Remote ergänzt werden.

Die Aufteilung der weiteren Arbeiten besprechen wir im Team.
