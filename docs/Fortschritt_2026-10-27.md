# Fortschrittsdokumentation – TaskFlow

| | |
|---|---|
| **Gruppe** | QualityForge |
| **Mitglieder** | Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu |
| **Datum** | 27.10.2026 |
| **Thema** | Qualitätsplanung und Projektaufbau |

## Durchgeführte Arbeiten

- Anforderungen gelesen und eine Webanwendung mit Java, Maven und Javalin ausgewählt.
- Das Projekt eingerichtet und auf GitHub hochgeladen.
- Stakeholder und fünf Qualitätsziele festgelegt: Korrektheit, Geschwindigkeit, Wiederherstellbarkeit, Verfügbarkeit und Bedienbarkeit.
- Für jedes Ziel Messgrößen, Zielwerte, Prüfungen und Maßnahmen beschrieben.
- Eine Startanleitung in der README ergänzt.

## Qualitätssicherung

| Art | Durchgeführte Maßnahmen |
|---|---|
| Konstruktiv | Java-Zielversion 17 und die Startklasse in Maven eingestellt; generierte Dateien über .gitignore ausgeschlossen. |
| Analytisch | Quellcode und Fehlermeldungen geprüft; Projekt kompiliert und beide Beispielrouten aufgerufen. |

## Tests

| Test | Durchführung | Ergebnis |
|---|---|---|
| Kompilierung | mvn clean compile mit JDK 21, Zielversion Java 17 | Erfolgreich: BUILD SUCCESS |
| Start der Anwendung | mvn compile exec:java | Server startet auf Port 7070 |
| Startseite | GET / | HTTP 200, Begrüßung wird angezeigt |
| Aufgabenroute | GET /api/tasks | HTTP 200, Beispieltext wird angezeigt |

Die Aufgabenroute liefert bisher nur einen Beispieltext. Aufgabenverwaltung und Datenbank sind noch nicht umgesetzt. Die dazugehörigen Tests folgen später.

## Probleme und Lösungen

| Problem | Lösung / Stand |
|---|---|
| VS Code hat einen Fehler bei der Paketstruktur angezeigt. | Den TaskFlow-Ordner direkt geöffnet und das Maven-Projekt importiert. |
| Unklar, wie wir die Qualitätsziele prüfen können. | Zu jedem Ziel eine Messgröße und eine konkrete Prüfung festgelegt. |
| Das Repository liegt auf GitHub, die Aufgabe nennt GitLab. | Noch klären, ob GitHub für die Abgabe akzeptiert wird. |

## Arbeitsaufteilung

| Name | Durchgeführte Arbeiten | Bemerkungen |
|---|---|---|
| Mario Shenouda | Projektaufbau, Fehlersuche, Tests und Dokumentation | |
| Ali Zolfaghari | — | Beitrag noch einzutragen |
| Maryam Taied | Vorschläge zu Recoverability, Availability, Testability und Adaptability | |
| Liyu Wu | Vorschläge zum Technologie-Stack |  |

## Reflexion

| Frage | Bisherige Beobachtung |
|---|---|
| Was lief gut? | Das Projekt startet und beide Beispielrouten funktionieren. |
| Was war schwierig? | Die Fehlermeldung in VS Code und die Formulierung messbarer Qualitätsziele. |
| Was haben wir gelernt? | Ein Qualitätsziel braucht einen konkreten Zielwert und eine passende Prüfung. |
| Was verbessern wir beim nächsten Mal? | Die Aufgaben im Team früher verteilen und die Ergebnisse gemeinsam prüfen. |

## Nächste Schritte

Das Aufgabenmodell erstellen und die Eingabeprüfung umsetzen. Danach folgen SQLite und die API für die Aufgabenverwaltung.
