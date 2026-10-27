# Fortschrittsdokumentation – TaskFlow

|                |                                                       |
| -------------- | ----------------------------------------------------- |
| **Gruppe**     | QualityForge                                          |
| **Mitglieder** | Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu |
| **Datum**      | 27.10.2026                                            |
| **Thema**      | Qualitätsplanung und Projektaufbau                    |

## Was haben wir gemacht?

- Wir haben die Anforderungen gelesen und uns für eine Webanwendung mit Java, Maven und Javalin entschieden.
- Wir haben das Projekt eingerichtet und auf GitHub hochgeladen.
- Wir haben die Stakeholder und fünf Qualitätsziele beschrieben: Korrektheit, Geschwindigkeit, Wiederherstellbarkeit, Verfügbarkeit und Bedienbarkeit.
- Zu jedem Ziel haben wir Messgrößen, Zielwerte, Prüfungen und Maßnahmen festgelegt.
- Wir haben die geplanten Tests auf einen ersten Lauf mit 10.000 Aufgaben und einen lokalen Stabilitätstest über 60 Minuten angepasst. Die ursprünglichen Vorgaben haben wir im Plan festgehalten.
- Wir haben die Startanleitung in der README ergänzt.

## Wie haben wir die Qualität gesichert?

| Art         | Was haben wir gemacht?                                                                                                                |
| ----------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| Konstruktiv | Wir haben Java 17 als Zielversion und die Startklasse in Maven eingestellt. Über .gitignore schließen wir erzeugte Dateien aus.       |
| Analytisch  | Wir haben den Quellcode und die Fehlermeldungen geprüft. Danach haben wir das Projekt kompiliert und beide Beispielrouten aufgerufen. |

## Was haben wir getestet?

| Test              | Wie sind wir vorgegangen?                                                  | Ergebnis                                 |
| ----------------- | -------------------------------------------------------------------------- | ---------------------------------------- |
| Kompilieren       | Wir haben mvn clean compile mit JDK 21 und Zielversion Java 17 ausgeführt. | BUILD SUCCESS                            |
| Anwendung starten | Wir haben mvn compile exec:java ausgeführt.                                | Der Server startet auf Port 7070.        |
| Startseite        | Wir haben GET / aufgerufen.                                                | HTTP 200 und die erwartete Begrüßung.    |
| Aufgabenroute     | Wir haben GET /api/tasks aufgerufen.                                       | HTTP 200 und der erwartete Beispieltext. |

Die Aufgabenroute gibt aktuell nur einen Beispieltext zurück. Später kommt hier JSON zurück. Aufgabenverwaltung und Datenbank fehlen noch. Deshalb haben wir die Tests mit 10.000 Aufgaben und den 60-Minuten-Lauf noch nicht ausgeführt. Auch die Testabdeckung haben wir noch nicht gemessen.

## Welche Probleme gab es?

| Problem                                                              | Wie haben wir es gelöst oder was ist noch offen?                                                                               |
| -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| VS Code hat einen Fehler bei der Paketstruktur angezeigt.            | Wir haben den TaskFlow-Ordner direkt geöffnet. Danach hat VS Code das Maven-Projekt geladen.                                   |
| Es war unklar, wie wir die Qualitätsziele prüfen können.             | Wir haben zu jedem Ziel eine Messgröße, einen Zielwert und einen konkreten Test ergänzt.                                       |
| Unser erster Geschwindigkeitstest nutzt weniger Daten als gefordert. | Wir beginnen mit 10.000 Aufgaben. Die geforderten 100.000 müssen wir später ebenfalls prüfen oder mit der Lehrperson abklären. |
| Wir betreiben noch keinen Server über einen ganzen Monat.            | Wir planen zunächst einen lokalen Lauf über 60 Minuten. Ob das für die Abgabe reicht, klären wir noch ab.                      |
| Wir nutzen GitHub, die Aufgabe nennt GitLab.                         | Wir arbeiten im bestehenden Repo weiter und klären noch ab, ob wir für die Abgabe wechseln müssen.                             |

## Wer hat woran gearbeitet?

| Name           | Bisherige Arbeiten                                                       | Noch offen             |
| -------------- | ------------------------------------------------------------------------ | ---------------------- |
| Mario Shenouda | Projektaufbau, Fehlersuche, Tests und Dokumentation                      |                        |
| Ali Zolfaghari | Einarbeitung in das Projekt und Abstimmung der Aufgabenverteilung.       | Beitrag noch eintragen |
| Maryam Taied   | Vorschläge zu Recoverability, Availability, Testability und Adaptability |                        |
| Liyu Wu        | Vorschläge zum Technologie-Stack                                         |                        |

## Was nehmen wir aus der Einheit mit?

| Frage                               | Unsere bisherigen Beobachtungen                                                                     |
| ----------------------------------- | --------------------------------------------------------------------------------------------------- |
| Was lief gut?                       | Das Projekt startet und beide Beispielrouten funktionieren.                                         |
| Was war schwierig?                  | Die Fehlermeldung in VS Code und die Frage, wie wir Qualitätsziele messbar machen.                  |
| Was haben wir gelernt?              | Ein Qualitätsziel braucht einen konkreten Zielwert und einen Test, den wir auch durchführen können. |
| Was machen wir nächstes Mal besser? | Wir verteilen die Aufgaben früher und schauen die Ergebnisse gemeinsam durch.                       |

## Was kommt als Nächstes?

Wir erstellen das Aufgabenmodell und prüfen die Eingaben. Danach bauen wir SQLite und die API für die Aufgabenverwaltung ein. Parallel ergänzen wir automatische Tests.
