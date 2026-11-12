# Qualitätsplanung – TaskFlow

**Gruppe:** QualityForge

**Mitglieder:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu

**Datum:** 27.10.2026

## 1. Was wir entwickeln

Wir entwickeln TaskFlow als Webanwendung mit Java, Maven und Javalin. Die Benutzer öffnen sie im Browser. Wir wollen Aufgaben in SQLite speichern und über eine REST-API als JSON abrufen und ändern.

Mit TaskFlow sollen Benutzer Aufgaben anlegen, bearbeiten und nach einer Bestätigung löschen können. Zu einer Aufgabe gehören Titel, Priorität und Status. Beschreibung und Fälligkeitsdatum sind optional. Die Anwendung vergibt ID und Erstellungszeit automatisch und hält Statusänderungen mit Zeitstempel fest. Dazu kommen Suche, Filter, Details, Statistiken und Hinweise auf fällige Aufgaben. Über ein JSON-Backup sollen Benutzer ihre Daten zusätzlich sichern können.

Wir orientieren unsere fünf Qualitätsziele an ISO 25010:2023.

## 2. Wer hat welche Erwartungen?

| Stakeholder | Was ist ihnen wichtig? |
|---|---|
| Benutzer / Mitarbeiter | Sie möchten Aufgaben einfach und schnell verwalten. Ihre gespeicherten Daten sollen erhalten bleiben. |
| Auftraggeber / Firmenkunde | Die geforderten Funktionen sollen richtig funktionieren. Die Firma möchte sich im Arbeitsalltag auf TaskFlow verlassen können. |
| Entwicklerteam | Wir möchten den Code verstehen, Fehler finden und neue Funktionen ergänzen können. |
| Administratoren | Sie möchten den Betrieb überwachen und die Anwendung nach einem Ausfall wieder starten können. |

## 3. Unsere fünf Qualitätsziele

Die Prüfungen in der Tabelle planen wir für die weitere Entwicklung. Wir haben sie noch nicht durchgeführt. Die Anzahl der Durchläufe und Testpersonen legen wir für unser Projekt selbst fest.

| Ziel / ISO-Kriterium | Was messen wir? | Welchen Wert wollen wir erreichen? | Wie prüfen wir das? | Was bauen wir dafür ein? |
|---|---|---|---|---|
| **Q1 – Korrekte Funktionen** / Functional suitability: Functional correctness | Anteil bestandener Pflicht-Testfälle (%), Anzahl kritischer Fehler | 100 % der Pflicht-Testfälle bestanden, keine bekannten kritischen Fehler | Wir prüfen Anlegen, Bearbeiten, Löschen, Statusverlauf, Suche, Filter, Details, Statistiken, Speicherung, Backup und API. Dazu testen wir falsche Eingaben und unveränderliche IDs. | Wir prüfen Pflichtfelder, verwenden feste Status- und Prioritätswerte und schützen ID und Erstellungszeit vor Änderungen. |
| **Q2 – Geschwindigkeit** / Performance efficiency: Time behaviour und Capacity | Startzeit (s), Filterzeit (ms) bei **10.000 Aufgaben** | Start ≤ 2 s, jeder gemessene Filter ≤ 100 ms | Wir erzeugen 10.000 Aufgaben mit einem Testskript in SQLite. Auf demselben Rechner messen wir fünf Starts und zehn Aufrufe je Filter. Wir notieren auch den langsamsten Aufruf. | Wir legen passende Datenbankindizes an und laden nur die Daten, die wir brauchen. |
| **Q3 – Wiederherstellbarkeit** / Reliability: Recoverability | Anzahl verlorener Änderungen, deren Speicherung die Anwendung schon bestätigt hat | 0 verlorene bestätigte Änderungen in unseren Tests | Wir speichern und ändern Aufgaben, beenden den Prozess unerwartet und starten neu. Wir wiederholen das fünfmal und vergleichen die Daten. | Wir nutzen SQLite-Transaktionen und planen JSON-Backups. Wir bestätigen eine Änderung erst nach dem Speichern. |
| **Q4 – Verfügbarkeit und Stabilität** / Reliability: Availability und Fault tolerance | Anteil korrekter Antworten (%), Anzahl Prozessabstürze | ≥ 99 % korrekte Antworten und 0 Abstürze im lokalen Testlauf | Wir lassen die Anwendung **60 Minuten lokal** laufen und schicken jede Sekunde eine Anfrage. Zusätzlich senden wir ungültige Anfragen und prüfen, ob sie passende Fehlermeldungen liefern, ohne den Server zu stoppen. | Wir fangen Fehler gezielt ab, prüfen Eingaben und schreiben Fehler ins Log. Für einen späteren Serverbetrieb planen wir Überwachung und automatischen Neustart. |
| **Q5 – Einfache Bedienung** / Interaction capability: Operability und User error protection | Anzahl ohne Hilfe erledigter Versuche, Daten nach dem Abbrechen einer Löschung | Mindestens 14 von 15 Versuchen ohne Hilfe, keine gelöschten Daten beim Abbrechen | Fünf Personen legen je eine Aufgabe an, bearbeiten und filtern sie. Zusätzlich prüfen wir Löschen, Abbrechen und Fehlermeldungen. | Wir beschriften Felder klar, geben verständliche Hinweise und fragen vor dem Löschen nach einer Bestätigung. |

### So halten wir die Tests nachvollziehbar

Wir notieren die getestete Codeversion, das JDK, das Betriebssystem, den Rechner und die Testdaten. Beim Start messen wir vom Prozessstart bis zur ersten erfolgreichen HTTP-Antwort. Die Maven-Buildzeit zählt nicht dazu.

**Zu Q2:** Die Anforderungen nennen mindestens 100.000 Aufgaben. Wir beginnen mit 10.000, weil wir diesen Test im Projekt leicht wiederholen und vorführen können. Das ersetzt den Nachweis für 100.000 Aufgaben noch nicht. Für die Abgabe müssen wir auch mit 100.000 testen oder eine Änderung mit der Lehrperson vereinbaren. Die Grenzen von 2 Sekunden und 100 Millisekunden behalten wir bei.

**Zu Q4:** Der lokale Lauf dient im Studienprojekt als praxisnaher Ersatz für eine 30-Tage-Servermessung. Bei einer Anfrage pro Sekunde erwarten wir 3.600 reguläre Anfragen. Timeouts und unerwartete Antworten zählen als Fehler. Wenn wir zum Beispiel absichtlich ungültige Daten senden, erwarten wir einen passenden 4xx-Status und keinen 500-Fehler. Diese zusätzlichen Prüfungen werten wir separat aus und prüfen auch danach, ob die Anwendung noch antwortet.

Die ursprüngliche Vorgabe lautet über 99 Prozent Verfügbarkeit. Mit unserem lokalen Ziel von mindestens 99 Prozent korrekten Antworten prüfen wir die Stabilität unter den Testbedingungen. Eine Monatsverfügbarkeit weisen wir damit nicht nach. Ob dieser Ersatz für die Abgabe reicht, klären wir mit der Lehrperson.

## 4. Wie wir die Qualität sichern

**Konstruktiv:** Wir trennen Routen, Aufgabenlogik und Datenzugriff. Wir prüfen Eingaben, nutzen Transaktionen und halten uns an gemeinsame Regeln für Namen und Formatierung. So wollen wir Fehler schon beim Programmieren vermeiden.

**Analytisch:** Wir schauen Änderungen gemeinsam durch und testen die Funktionen mit JUnit 5. Mit Mockito können wir bei Bedarf den Datenzugriff im Test ersetzen. Zusätzlich prüfen wir API und Oberfläche, führen die Tests aus der Tabelle durch und halten die Ergebnisse fest. Mit JaCoCo messen wir die Testabdeckung. Für die Abgabe brauchen wir mindestens 80 Prozent. Außerdem testen wir die Oberfläche in Firefox, Chrome und Safari.

## 5. Unser aktueller Stand

Wir können das Grundgerüst kompilieren und starten. Die beiden Beispielrouten liefern die erwarteten Antworten. Aufgabenverwaltung, Datenbank und Oberfläche bauen wir noch ein. Danach führen wir die geplanten Tests durch. Für die Abgabe erstellen wir auch eine Anleitung und prüfen, dass keine kritischen Fehler offen sind.
