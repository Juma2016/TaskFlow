# Qualitätsplanung – TaskFlow

**Gruppe:** QualityForge

**Mitglieder:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu

**Datum:** 27.10.2026

## 1. Unsere Anwendung

Wir entwickeln TaskFlow als Webanwendung mit Java, Maven und Javalin. Die Benutzer können die Anwendung im Browser öffnen. Für die Speicherung ist SQLite vorgesehen.

TaskFlow soll Aufgaben erstellen, bearbeiten und löschen können. Aufgaben haben zum Beispiel einen Titel, eine Priorität, einen Status und ein Fälligkeitsdatum. Dazu kommen eine Suche, Filter und eine Statistik. Die Daten sollen gespeichert bleiben, wenn die Anwendung geschlossen wird. Über eine REST-API sollen Aufgaben als JSON abgerufen und geändert werden können.

Für unsere Qualitätsplanung verwenden wir das Qualitätsmodell ISO 25010 aus der Übungsunterlage. Wir haben fünf Ziele ausgewählt.

## 2. Stakeholder

| Stakeholder | Was ist dieser Gruppe wichtig? |
|---|---|
| Benutzer / Mitarbeiter | Sie möchten ihre Aufgaben einfach verwalten. Die Anwendung soll schnell und erreichbar sein. Ihre gespeicherten Aufgaben dürfen nicht verloren gehen. |
| Auftraggeber / Firmenkunde | Die geforderten Funktionen sollen richtig funktionieren. TaskFlow soll ein zuverlässiges Arbeitsmittel sein. |
| Entwicklerteam | Wir möchten den Code verstehen, Fehler finden und die Anwendung später erweitern können. |
| Administratoren | Sie kümmern sich um den Server, die Überwachung und die Wiederherstellung nach einem Ausfall. |

## 3. Unsere fünf Qualitätsziele

Die Zahlen für Geschwindigkeit und Verfügbarkeit stammen aus den Anforderungen. Die Anzahl der Testdurchläufe und Testpersonen sind unsere Vorschläge. Die Tests sind geplant und noch nicht durchgeführt.

### Q1 – Fachliche Korrektheit

**ISO-Kriterium:** Functional suitability – Functional correctness

Die Aufgabenverwaltung soll die erwarteten Ergebnisse liefern.

- **Indikator / Messgröße:** Anteil der bestandenen Testfälle in Prozent und Anzahl kritischer Fehler.
- **Zielwert:** Alle festgelegten Pflicht-Testfälle bestehen; keine bekannten kritischen Fehler.
- **Prüfung:** Wir testen zum Beispiel das Erstellen, Bearbeiten, Löschen und Filtern von Aufgaben. Dabei prüfen wir auch falsche Eingaben.
- **Konstruktive Maßnahmen:** Pflichtfelder prüfen, feste Werte für Priorität und Status verwenden und ID sowie Erstellungszeit vor Änderungen schützen.

### Q2 – Geschwindigkeit

**ISO-Kriterium:** Performance efficiency – Time behaviour und Capacity

TaskFlow soll auch mit vielen Aufgaben schnell reagieren.

- **Indikator / Messgröße:** Startzeit in Sekunden und Filterzeit in Millisekunden bei 100.000 gespeicherten Aufgaben.
- **Zielwert:** Start höchstens 2 Sekunden, Filter höchstens 100 Millisekunden.
- **Prüfung:** Wir messen fünf Starts und zehn Durchläufe pro Filter auf demselben Rechner. Wir notieren die Zeiten und prüfen, ob ein Wert die Grenze überschreitet. Die Maven-Buildzeit zählt nicht zur Startzeit.
- **Konstruktive Maßnahmen:** Passende Datenbankindizes anlegen und nur die benötigten Daten laden.

### Q3 – Wiederherstellbarkeit

**ISO-Kriterium:** Reliability – Recoverability

Nach einem Absturz sollen bereits gespeicherte Aufgaben noch vorhanden sein.

- **Indikator / Messgröße:** Anzahl verlorener, bereits als gespeichert bestätigter Änderungen nach einem Neustart.
- **Zielwert:** Keine verlorenen bestätigten Änderungen in unseren Tests.
- **Prüfung:** Wir speichern und ändern Aufgaben, beenden den Prozess unerwartet und starten die Anwendung neu. Wir wiederholen das fünfmal und vergleichen die Daten.
- **Konstruktive Maßnahmen:** SQLite-Transaktionen verwenden und eine Backup-Funktion mit JSON-Dateien vorsehen.

### Q4 – Verfügbarkeit

**ISO-Kriterium:** Reliability – Availability

Die Benutzer sollen TaskFlow erreichen können, wenn sie ihre Aufgaben verwalten möchten.

- **Indikator / Messgröße:** Anteil erfolgreicher Erreichbarkeitsprüfungen in Prozent und dokumentierte Ausfallzeit.
- **Zielwert:** Verfügbarkeit über 99 Prozent.
- **Prüfung:** Nach dem Serverbetrieb prüfen wir jede Minute, ob die Anwendung antwortet, und werten die Ergebnisse über einen Monat aus. Bei 30 Tagen entspricht die Vorgabe weniger als 432 Minuten Ausfallzeit. Die Minutenprüfungen liefern eine Schätzung.
- **Konstruktive Maßnahmen:** Server überwachen, Fehler protokollieren und einen automatischen Neustart bei einem Prozessausfall einrichten.

### Q5 – Einfache Bedienung

**ISO-Kriterium:** Interaction capability – Operability und User error protection

Benutzer sollen die wichtigsten Funktionen ohne Erklärung bedienen können.

- **Indikator / Messgröße:** Anzahl der ohne Hilfe erledigten Aufgaben und Ergebnis beim Abbrechen einer Löschung.
- **Zielwert:** Mindestens 14 von 15 Versuchen gelingen ohne Hilfe. Beim Abbrechen einer Löschung bleiben die Daten erhalten. Das sind unsere vorgeschlagenen Zielwerte.
- **Prüfung:** Fünf Personen erstellen, bearbeiten und filtern jeweils eine Aufgabe. Zusätzlich testen wir das Löschen und Abbrechen sowie Fehlermeldungen bei falschen Eingaben.
- **Konstruktive Maßnahmen:** Felder klar beschriften, verständliche Fehlermeldungen anzeigen und vor dem Löschen eine Bestätigung verlangen.

## 4. Stand und nächste Schritte

Das Anwendungsgerüst lässt sich kompilieren und starten. Zwei Beispielrouten wurden erfolgreich geprüft. Aufgabenverwaltung, Datenbank und Oberfläche müssen noch umgesetzt werden. Danach führen wir die geplanten Prüfungen durch und halten die Ergebnisse fest.

Die übrigen Anforderungen, zum Beispiel mindestens 80 Prozent Testabdeckung und die Unterstützung von Firefox, Chrome und Safari, bleiben Teil des Projekts. In dieser Planung beschreiben wir die fünf ausgewählten Ziele genauer.

Das Repository liegt aktuell auf GitHub. Da das Übungsblatt GitLab nennt, müssen wir noch klären, ob GitHub für die Abgabe akzeptiert wird.

## Grundlagen

- Anforderungen_Taskflow.pdf, insbesondere Abschnitte 2 und 3.
- Qualitätsmanagement Übung, insbesondere Folien 4 und 5 sowie das ISO-25010-Modell auf Folie 7.
