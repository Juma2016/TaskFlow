# TaskFlow

Wir entwickeln TaskFlow als Webanwendung für die Lehrveranstaltung Software Quality. Damit sollen Benutzer ihre Aufgaben anlegen, bearbeiten, filtern und löschen können.

**Team QualityForge:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu.

## Unser aktueller Stand

Wir haben das Grundgerüst mit Java, Maven und Javalin eingerichtet. Die Anwendung startet und zwei Beispielrouten funktionieren. Aufgabenverwaltung, SQLite-Datenbank und Browseroberfläche bauen wir als Nächstes ein.

- [Unsere Qualitätsplanung](docs/Qualitaetsplan.md)
- [Unser Fortschritt vom 27.10.2026](docs/Fortschritt_2026-10-27.md)

## Projekt starten

Wir brauchen ein JDK ab Version 17, Maven 3.9.x und Git. In VS Code öffnen wir den Ordner mit der pom.xml und warten, bis VS Code das Maven-Projekt geladen hat.

Mit `java -version` und `mvn -version` prüfen wir die installierten Versionen. Danach starten wir im Projektordner:

```sh
mvn clean compile
mvn compile exec:java
```

Die Anwendung läuft unter http://localhost:7070/. Mit Ctrl+C im Terminal stoppen wir sie wieder.

| Route | Was kommt zurück? |
|---|---|
| GET / | HTTP 200 und „Willkommen bei TaskFlow!“ |
| GET /api/tasks | HTTP 200 und „Hier werden die Aufgaben angezeigt.“ |

Die Aufgabenroute gibt aktuell nur einen Beispieltext zurück. Später kommt hier JSON mit den Aufgaben zurück.

## Was wir bisher geprüft haben

Bei laufender Anwendung rufen wir die beiden Routen in einem zweiten Terminal auf:

```sh
curl -i http://localhost:7070/
curl -i http://localhost:7070/api/tasks
```

Beide Aufrufe haben HTTP 200 und die erwarteten Texte geliefert. Damit haben wir den Start und die Beispielrouten geprüft. Automatische Tests für die Aufgabenverwaltung fehlen noch.

Bei „Address already in use“ läuft meistens noch eine Instanz. Wir stoppen sie im zugehörigen Terminal mit Ctrl+C. Bei Fehlern mit Paketen oder Imports prüfen wir, ob wir den richtigen Projektordner geöffnet haben und VS Code das Maven-Projekt geladen hat.

Unter neueren JDK-Versionen zeigt Maven ein paar Warnungen an. Mit JDK 21 lief der Build problemlos durch und erzeugte Code für Java 17.

## Wo liegt was?

| Datei / Ordner | Inhalt |
|---|---|
| src/main/java/com/taskflow/Main.java | Hier starten wir die Anwendung und legen die Beispielrouten an. |
| pom.xml | Hier stehen Java-Zielversion 17, Abhängigkeiten und die Einstellungen zum Kompilieren und Starten. |
| .vscode/settings.json | Damit aktualisiert VS Code die Maven-Einstellungen automatisch. |
| docs/ | Hier sammeln wir Qualitätsplan und Fortschrittsbericht. |
| target/ | Hier legt Maven erzeugte Dateien ab. Wir schließen den Ordner über .gitignore aus. |

## Wie wir weiterarbeiten

Wir trennen die HTTP-Routen, die Aufgabenlogik und den Datenzugriff, damit wir die Teile einzeln testen können. Wir planen SQLite für die Speicherung, Jackson für JSON und JUnit 5 mit Mockito für die Tests. Mit JaCoCo messen wir später die Testabdeckung. Für die Abgabe brauchen wir mindestens 80 Prozent, keine bekannten kritischen Fehler und eine verständliche Anleitung.

Im Qualitätsplan beschreiben wir fünf Ziele und unsere geplanten Tests. Für den ersten Geschwindigkeitstest nutzen wir 10.000 Aufgaben. Die Vorgabe von 100.000 Aufgaben bleibt offen, bis wir auch diese Datenmenge geprüft haben. Die Stabilität prüfen wir zunächst lokal über 60 Minuten. Daraus können wir noch keine Verfügbarkeit über einen ganzen Monat ableiten.

## Repository und Team

Wir arbeiten aktuell im bestehenden [GitHub-Repo](https://github.com/Juma2016/TaskFlow) weiter. Wir klären noch ab, ob wir für die Abgabe auf GitLab wechseln müssen.

Die nächsten Aufgaben verteilen wir gemeinsam im Team.
