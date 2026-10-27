# Qualitätsplanung – TaskFlow

**Gruppe:** QualityForge

**Mitglieder:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu

**Datum:** 27.10.2026

## 1. Unsere Anwendung

Wir entwickeln TaskFlow als Webanwendung mit Java, Maven und Javalin. Die Benutzer können die Anwendung im Browser öffnen. Für die Speicherung ist SQLite vorgesehen.

TaskFlow soll Aufgaben erstellen, bearbeiten und löschen können. Aufgaben haben zum Beispiel einen Titel, eine Priorität, einen Status und ein Fälligkeitsdatum. Dazu kommen eine Suche, Filter und eine Statistik. Die Daten sollen gespeichert bleiben, wenn die Anwendung geschlossen wird. Über eine REST-API sollen Aufgaben als JSON abgerufen und geändert werden können.

Unsere fünf Qualitätsziele orientieren sich an ISO 25010.

## 2. Stakeholder

| Stakeholder | Was ist dieser Gruppe wichtig? |
|---|---|
| Benutzer / Mitarbeiter | Sie möchten ihre Aufgaben einfach verwalten. Die Anwendung soll schnell und erreichbar sein. Ihre gespeicherten Aufgaben dürfen nicht verloren gehen. |
| Auftraggeber / Firmenkunde | Die geforderten Funktionen sollen richtig funktionieren. TaskFlow soll ein zuverlässiges Arbeitsmittel sein. |
| Entwicklerteam | Wir möchten den Code verstehen, Fehler finden und die Anwendung später erweitern können. |
| Administratoren | Sie kümmern sich um den Server, die Überwachung und die Wiederherstellung nach einem Ausfall. |

## 3. Unsere fünf Qualitätsziele

Die folgenden Prüfungen führen wir während der Entwicklung durch.

| Qualitätsziel / ISO-Kriterium | Indikator / Messgröße | Zielwert | Wie prüfen wir das? | Konstruktive Maßnahmen |
|---|---|---|---|---|
| **Q1 – Fachliche Korrektheit** / Functional suitability: Functional correctness | Bestandene Pflicht-Testfälle (%); kritische Fehler | 100 % bestanden; keine bekannten kritischen Fehler | Erstellen, Bearbeiten, Löschen und Filtern testen; auch falsche Eingaben prüfen | Pflichtfelder prüfen; feste Status- und Prioritätswerte; ID und Erstellungszeit schützen |
| **Q2 – Geschwindigkeit** / Performance efficiency: Time behaviour, Capacity | Startzeit (s); Filterzeit (ms) bei 100.000 Aufgaben | Start ≤ 2 s; Filter ≤ 100 ms | Fünf Starts und zehn Durchläufe je Filter auf demselben Rechner messen | Datenbankindizes verwenden; nur benötigte Daten laden |
| **Q3 – Wiederherstellbarkeit** / Reliability: Recoverability | Verlorene bestätigte Änderungen nach einem Neustart (Anzahl) | 0 verlorene bestätigte Änderungen in den Tests | Aufgaben speichern, Prozess unerwartet beenden und neu starten; fünfmal wiederholen und Daten vergleichen | SQLite-Transaktionen; JSON-Backups |
| **Q4 – Verfügbarkeit** / Reliability: Availability | Erfolgreiche Erreichbarkeitsprüfungen (%); Ausfallzeit (min) | Über 99 %; bei 30 Tagen weniger als 432 min Ausfall | Nach Bereitstellung auf dem Server jede Minute prüfen; Ergebnisse einen Monat lang erfassen | Server überwachen; Fehler protokollieren; automatischer Neustart |
| **Q5 – Einfache Bedienung** / Interaction capability: Operability, User error protection | Ohne Hilfe erledigte Versuche (Anzahl); Daten nach Löschabbruch | Mindestens 14 von 15 Versuchen ohne Hilfe; kein Datenverlust beim Abbrechen | Fünf Personen erstellen, bearbeiten und filtern je eine Aufgabe; Löschen, Abbrechen und Fehlermeldungen zusätzlich prüfen | Klare Beschriftungen; verständliche Fehlermeldungen; Bestätigung vor dem Löschen |

Die Maven-Buildzeit zählt nicht zur Startzeit. Die regelmäßigen Erreichbarkeitsprüfungen schätzen die Verfügbarkeit. Für die Messungen halten wir Rechner, Softwareversion und Testdaten fest.

## 4. Stand und nächste Schritte

Das Anwendungsgerüst lässt sich kompilieren und starten. Zwei Beispielrouten wurden erfolgreich geprüft. Aufgabenverwaltung, Datenbank und Oberfläche müssen noch umgesetzt werden. Danach führen wir die geplanten Prüfungen durch und halten die Ergebnisse fest.

Zusätzlich berücksichtigen wir mindestens 80 Prozent Testabdeckung und die Unterstützung von Firefox, Chrome und Safari.
