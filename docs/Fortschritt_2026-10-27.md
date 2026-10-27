# Fortschrittsdokumentation – Software Quality

**Gruppe:** QualityForge

**Mitglieder:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu

**Einheit:** Qualitätsplanung und Anwendungsgerüst

**Datum:** 27.10.2026

## Welche Arbeiten wurden durchgeführt?

- Anforderungen gelesen und die Webvariante mit Java, Maven und Javalin ausgewählt.
- Anwendungsgerüst eingerichtet und auf GitHub gespeichert.
- Das Problem mit dem Java-Projektimport in VS Code untersucht und den richtigen Projektordner geöffnet.
- Stakeholder beschrieben und fünf Qualitätsziele mit Messgrößen, Zielwerten und Maßnahmen ausgearbeitet.
- Die Qualitätsplanung gekürzt und auf die fünf Ziele Korrektheit, Geschwindigkeit, Wiederherstellbarkeit, Verfügbarkeit und Bedienbarkeit konzentriert.
- Eine README mit Startanleitung erstellt.

## Qualitätssicherungsmaßnahmen

**Konstruktiv:** Java-Zielversion 17 in Maven eingestellt, die Startklasse festgelegt und generierte Dateien aus Git ausgeschlossen. In der Planung haben wir zum Beispiel Eingabeprüfungen und Datenbanktransaktionen vorgesehen.

**Analytisch:** Quellcode, Maven-Ausgaben und VS-Code-Protokoll geprüft. Die Anwendung kompiliert und die beiden Beispielrouten getestet.

## Testing

| Was? | Wie? | Ergebnis |
|---|---|---|
| Kompilierung | mvn clean compile mit JDK 21 und Java-Zielversion 17 | BUILD SUCCESS |
| Start | mvn compile exec:java | Server startet auf Port 7070 |
| Startseite | GET / | HTTP 200 und Begrüßung |
| Beispielroute | GET /api/tasks | HTTP 200 und Platzhaltertext |

Die Aufgabenverwaltung ist noch nicht umgesetzt. Deshalb gibt es noch keine Fachtests oder gemessene Testabdeckung. Die Tests aus der Qualitätsplanung folgen während der Entwicklung.

## Probleme und Lösungen

VS Code hat die Paketstruktur zunächst falsch erkannt. Nach dem Öffnen des TaskFlow-Ordners wurde das Maven-Projekt importiert. Die Javalin-7-Routen mussten nicht geändert werden.

Offen ist die Plattform für die Abgabe: Unser Repository liegt auf GitHub, im Übungsblatt steht GitLab.

## Arbeitsaufteilung

| Name | Bisher dokumentierter Beitrag | Noch zu ergänzen |
|---|---|---|
| Mario Shenouda | Projektaufbau, Fehlersuche, Prüfungen und Dokumentation | Teamreview |
| Ali Zolfaghari | Noch nicht festgehalten | Eigenen Beitrag ergänzen |
| Maryam Taied | Vorschläge zu Recoverability, Availability, Testability und Adaptability | Weitere Beiträge ergänzen |
| Liyu Wu | Noch nicht festgehalten | Eigenen Beitrag ergänzen |

## Gruppenreflexion – noch im Team zu besprechen

- **Was lief gut?** Das Gerüst startet und die Beispielrouten funktionieren.
- **Was war schwierig?** Die Fehlermeldung in VS Code und die Auswahl messbarer Qualitätsziele.
- **Was haben wir gelernt?** Zu jedem Qualitätsziel gehören ein Zielwert und eine passende Prüfung.
- **Was machen wir als Nächstes?** Beiträge im Team ergänzen, GitLab klären und mit dem Aufgabenmodell beginnen.
