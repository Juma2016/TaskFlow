# TaskFlow – Qualitätsplan

**Gruppe:** QualityForge

**Mitglieder:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu

**Stand:** 27.10.2026 | **Status:** Planung für das Projekt; Qualitätsziele noch nicht als erfüllt nachgewiesen.

Die Namen wurden vom Auftraggeber genannt; die Schreibweise wurde vereinheitlicht. Lokal ist Mario Shenouda als Commit-Autor nachweisbar. Die Identitäten der übrigen Mitglieder sind anhand der verfügbaren Repository-Metadaten nicht unabhängig bestätigt.

## 1. Grundlagen und Umfang dieser Einheit

Grundlagen: „Qualitätsmanagement Übung“, Folien 2–7, sowie „TaskFlow – Anforderungsspezifikation“, Abschnitte 1–5. Das Fortschrittsprotokoll folgt der bereitgestellten Screenshot-Vorlage.

Die Einheit umfasst Qualitätsplanung, Repository und eingechecktes Anwendungsgerüst. Vollständige Aufgabenverwaltung, Datenbank, Benutzeroberfläche und automatisierte Fachtests werden in späteren Einheiten umgesetzt.

**Modell:** ISO/IEC 25010:2023, entsprechend den neun Merkmalen der Grafik auf Folie 7. Die Grafik bezeichnet das erste Merkmal als „Functional Stability“; hier wird die normative Bezeichnung „Functional suitability“ verwendet. Benutzerfreundlichkeit wird unter Interaction capability, Browseranpassung unter Flexibility eingeordnet. Referenz: https://www.iso.org/standard/78176.html .

## 2. Implementierungsentscheidung

**Variante 1: Webanwendung.** Java 17 oder höher, Maven, eingebetteter Javalin-Server, Browseroberfläche. SQLite und Jackson sind für die Umsetzung vorgesehen; JUnit 5, Mockito und JaCoCo für die Tests bzw. Abdeckungsmessung. Aktuell enthält das Gerüst Javalin und Logging, noch keine Datenbank oder Geschäftslogik.

Begründung: zentraler Betrieb und Updates, Zugriff ohne lokale Desktopinstallation, gemeinsame Nutzung der REST-API durch Oberfläche und weitere Clients. Gegenüber Swing entstehen Anforderungen an Serverbetrieb, Verfügbarkeit, Browserkompatibilität und eine geklärte Zugriffstrennung.

Geplante Schichten: Präsentation/HTTP → Geschäftslogik → Repository → SQLite. Fachlogik darf weder vom HTTP-Kontext noch von konkreten SQL-Abfragen abhängen. Das aktuelle Main.java ist nur der Einstiegspunkt des Gerüsts.

## 3. Anforderungsanalyse und Nachverfolgbarkeit

Die folgenden IDs sind projektinterne Referenzen, keine Nummern aus dem Original.

| ID | Quelle | Anforderung und Prüfung | Geplante Priorität |
|---|---|---|---|
| F1 | 2.1 | Aufgaben erstellen: Titel, optionale Beschreibung, Priorität, optionales Datum, eindeutige ID, Erstellungszeit; Feld- und Eindeutigkeitstests | Muss |
| F2 | 2.1 | Attribute ändern; ID/Erstellungszeit unveränderlich; Löschen nur nach Bestätigung; Negativtests | Muss |
| F3 | 2.1 | OFFEN/IN_BEARBEITUNG/ABGESCHLOSSEN und Zeitstempel jeder Statusänderung; Historientests | Muss |
| F4 | 2.2 | Liste nach Priorität, dann Fälligkeit; Filter Status/Priorität/überfällig/heute/diese Woche und Textsuche; Grenzwerttests | Muss |
| F5 | 2.2 | Detailansicht samt Statushistorie und Statistik pro Status/überfällig; Vergleich mit Referenzdaten | Muss/Soll laut Wortlaut |
| F6 | 2.3 | SQLite: Laden beim Start, Speicherung jeder Änderung; Neustart- und Abbruchtests | Muss |
| F7 | 2.3 | JSON-Backup in separate Datei mit Zeitstempel; Export auf Vollständigkeit prüfen | Soll |
| F8 | 2.4 | GET /api/tasks, GET /api/tasks/{id}, POST /api/tasks, PUT /api/tasks/{id}, DELETE /api/tasks/{id}, GET /api/tasks/stats; JSON und passende HTTP-Codes | Muss |
| F9 | 2.5 | Überfällige/heute fällige Aufgaben beim Start erkennen und hervorheben | Soll |
| F10 | 2.5–2.6 | Desktop-Benachrichtigung und Benutzerverwaltung | Optional |
| N1 | 3.1 | Verständliche Oberfläche und hilfreiche Fehlermeldungen | Qualitätsziel Q5 |
| N2 | 3.2 | Mindestens 100.000 Aufgaben, Start ≤2 s, Filter ≤100 ms | Q2 |
| N3 | 3.3 | Ungültige Eingaben ohne Absturz, kein Verlust bestätigter Änderungen, Verfügbarkeit >99 % | Q3/Q4 |
| N4 | 3.4, 5 | Schichtentrennung und mindestens 80 % Testabdeckung bei Abnahme | Q6 |
| N5 | 3.6 | Webanwendung auf Firefox, Chrome, Safari | Q7 |

Bei Abnahme werden alle nicht optionalen funktionalen Anforderungen als verpflichtende Checkliste behandelt, auch die mit „soll“ formulierten. Abweichungen sind mit dem Auftraggeber zu klären.

### Mengengerüst und Risiken

4.500 Benutzer × 115 neue Aufgaben/Monat = **517.500 neue Aufgaben/Monat**. Bei konstant 4.500 Benutzern über zwölf Monate wären das **6.210.000 neue Aufgaben/Jahr**; dies ist eine Szenariorechnung, keine Prognose des ersten Jahres, da 4.500 erst nach dem ersten Jahr erwartet werden. Die Vorgabe von 100.000 Aufgaben ist daher nur die Mindesttestgröße; Archivierung und größere Datensätze sind später zu prüfen.

Bei voller angenommener Nutzerzahl: 4.500 × 35 EUR = 157.500 EUR monatlicher Umsatz; abzüglich 3.500 EUR Betriebskosten = 154.000 EUR vor Entwicklung, Steuern und weiteren Kosten. Das ist keine Gewinnberechnung. Aus Benutzerzahlen folgt keine Gleichzeitigkeit; ein realistisches Lastprofil muss vereinbart werden.

## 4. Stakeholder und Views

| Stakeholder | Sicht und Erwartungen | Zugeordnete Ziele |
|---|---|---|
| Mitarbeiter als Endnutzer | Aufgaben korrekt und schnell verwalten; klare Hinweise; keine verlorenen Änderungen | Q1–Q5, Q7 |
| Firmenkunde/Teamleitung als Auftraggeber | Verlässliches Arbeitsmittel, verfügbare Planung und nachvollziehbare Abnahme | Q1–Q4, Q8 |
| Startup-Geschäftsführung/Produktverantwortung | Betriebskosten, Wachstum, Kundenzufriedenheit und priorisierte Lieferung | Q2, Q4, Q6, Q8 |
| Betrieb/Administration | Überwachung, Backups, Wiederanlauf, sichere Konfiguration | Q3, Q4, Q8 |
| Entwicklung und Qualitätssicherung | Modulare Änderungen, reproduzierbarer Build, prüfbare Anforderungen | Q1, Q6, Q7 |

## 5. Qualitätsziele, Indikatoren und Messplan

Werte aus den Anforderungen sind als „Vorgabe“ markiert. Weitere Werte sind Planungsvorschläge und noch mit dem Team abzustimmen. Sämtliche Messungen benötigen einen protokollierten Build-Stand, JDK, Betriebssystem, Hardware und Datensatz. Bisher sind nur die Gerüstprüfungen im Fortschrittsprotokoll ausgeführt.

| Ziel / ISO-Kriterium | Indikator und Messgröße | Zielwert | Messverfahren / Nachweis | Konstruktive Maßnahmen |
|---|---|---|---|---|
| Q1 Fachliche Korrektheit – Functional suitability: completeness/correctness | Erfolgreiche Pflicht-Abnahmefälle / alle Pflichtfälle ×100; Anzahl kritischer Defekte | 100 % Pflichtfälle bestanden, 0 bekannte kritische Bugs (Vorgabe Abnahme) | JUnit-Fachtests, API-Integrationstests, manuelle UI-Abnahme; F1–F9 rückverfolgbar | Zentrale Validierung, Status-Enum, unveränderliche ID/Erstellungszeit, parametrisierte SQL-Abfragen, Akzeptanzfälle vor Umsetzung |
| Q2 Reaktionszeit und Kapazität – Performance efficiency: time behaviour/capacity | Startdauer in s; maximale Filterdauer in ms; Anzahl Datensätze | Start ≤2 s; jeder gemessene Filter ≤100 ms bei ≥100.000 Aufgaben (Vorgabe) | 10 separate JVM-Starts vom Prozessstart bis HTTP-Bereitschaft; persistierte 100.000 Datensätze; je Filtertyp 100 Messungen nach 10 Aufwärmläufen; Maximum und p95 berichten | SQL-Indizes nach Abfrageprofil, Transaktionen, Pagination, keine vollständigen Datenladungen je Anfrage; Kapazität separat über 100.000 hinaus untersuchen |
| Q3 Datenintegrität – Reliability: fault tolerance/recoverability; Security: integrity | Verlorene bestätigte Änderungen; Integritätsfehler; ungültige Anfragen mit Prozessabsturz | 0 in definierten Tests (Vorgabe: keine Datenverluste/Abstürze) | 20 kontrollierte Prozessabbrüche um Schreibvorgänge; Neustart, SQLite-Integritätsprüfung und Vergleich bestätigter IDs; ungültige Felder/IDs/JSON testen | Atomare SQLite-Transaktionen, geeignete Durability-Einstellungen, Constraints, zentrale Fehlerbehandlung, Backup mit Zeitstempel; nicht bestätigte Transaktionen dürfen zurückrollen |
| Q4 Verfügbarkeit – Reliability: availability | Erfolgreiche Bereitschaftsproben / alle geplanten Proben ×100; Ausfallzeit min/Monat | >99 % (Vorgabe); bei 30 Tagen also <432 min Ausfall | Nach Deployment alle 60 s überwachen, Timeouts als Fehler; Monatsbericht, Wartungszeiten zunächst eingeschlossen | Prozessüberwachung mit Neustart, strukturierte Logs, persistentes Datenverzeichnis, Wiederanlaufanleitung; lokale Tests beweisen keine Monatsverfügbarkeit |
| Q5 Bedienbarkeit – Interaction capability: operability/user error protection/self-descriptiveness | Erfolgsquote ohne Hilfe; erfolgreiche Löschabbrüche; hilfreiche Validierungshinweise | ≥90 % der 15 Kernaufgaben ohne Hilfe; 100 % der Löschabbrüche erhalten Daten (Vorschlag); klare Fehlertexte (Vorgabe) | 5 Testpersonen mit je Anlegen/Bearbeiten/Filtern; separate Tests für Löschen/Abbrechen und ungültige Eingaben; Protokoll | Beschriftete Felder, konsistente Prioritäts-/Statuswerte, Bestätigungsdialog, konkrete Fehlermeldungen, Tastaturbedienung |
| Q6 Wartbarkeit – Maintainability: modularity/testability | JaCoCo-Zeilenabdeckung Fachlogik und Gesamtprojekt; verbotene Schichtenabhängigkeiten | ≥80 % Fachlogik und ≥80 % Gesamt-Zeilenabdeckung bei Abnahme; 0 verbotene Abhängigkeiten (80 % Vorgabe, Definition/Verschärfung Vorschlag) | JUnit 5/Mockito, JaCoCo-Bericht; Review: Fachlogik importiert weder Javalin noch JDBC; keine Ausnahmen durch pauschalen Ausschluss produktiver Klassen | Getrennte Pakete/Interfaces, Dependency Injection, kleine Klassen, gemeinsame Coding-Standards, Reviews und später Build-Prüfung |
| Q7 Browserfähigkeit – Flexibility: adaptability; Compatibility: interoperability | Bestandene Kernabläufe je Browser; API-Antworten gemäß JSON-/HTTP-Vertrag | 100 % Kernabläufe in Firefox, Chrome, Safari (Browser Vorgabe, Quote Vorschlag) | Bei Abnahme jeweilige stabile Browserversion protokollieren; Anlegen/Bearbeiten/Filtern/Löschen/Details prüfen; API-Vertragstests | Standard-HTML/CSS/JS, gemeinsame JSON-DTOs, keine browserspezifischen APIs ohne Fallback |
| Q8 Schutz vor Fehlzugriffen – Security: confidentiality/integrity; Safety: operational constraint | Ungeklärte Zugriffskonzepte; Injection-Testfehler; versehentlich veröffentlichte Geheimnisse | Vor externem Betrieb 0 offene kritische Risiken, 0 erfolgreiche SQL-Injections (Vorschlag) | Threat-Review, Negativtests, Review vor Push/Deployment; Betriebsfreigabe dokumentieren | Prepared Statements, keine Zugangsdaten im Git, lokale Demo; vor Firmenbetrieb Zugriffstrennung und HTTPS klären. Optionale Anmeldung bedeutet nicht automatisch Schutz persönlicher Aufgaben |

Die Startzeit des Maven-Builds zählt nicht zur Anwendungsstartzeit. Der heutige Konsolenwert von Javalin ist kein vollständiger Performance-Nachweis. Fehlende Fälligkeitsdaten, Zeitzone und Wochenbeginn müssen vor Filtertests festgelegt werden.

## 6. Konstruktive und analytische Qualitätssicherung

**Konstruktiv:** abgestimmte Coding-Standards, Schichtentrennung, Validierung, Transaktionen, Indizes, gemeinsame API-Verträge, reproduzierbarer Maven-Build und versionierte Dokumentation.

**Analytisch:** Anforderungs- und Code-Reviews, Unit-/Integrationstests, Browser- und Benutzertests, Last-/Abbruchtests, Abdeckungsmessung und später Betriebsmonitoring. Diese Verfahren sind geplant; ein erfolgreiches Gerüst ist kein Nachweis für fachliche Vollständigkeit.

Coding-Standards: Java-Konventionen, aussagekräftige englische Bezeichner, vier Leerzeichen, keine Geheimnisse im Repository, parametrisierte SQL-Abfragen, verständliche Fehlermeldungen, keine unbehandelten technischen Details in UI-Antworten. Änderungen klein halten; fachliche Änderungen mit passenden Tests prüfen. Rollen im Team werden noch gemeinsam verteilt.

## 7. Offene Entscheidungen und nächste Schritte

1. **Repository:** Aufgabenblatt fordert GitLab; aktuell existiert https://github.com/Juma2016/TaskFlow . GitLab-Zieladresse oder ausdrückliche Akzeptanz von GitHub durch die Lehrperson fehlt. Bestehendes Repository und Historie bleiben erhalten.
2. Team prüft Plan, Namensschreibung, Rollen und Reflexion; keine gemeinsame Freigabe behauptet.
3. Zugriffstrennung: persönliche Aufgaben versus optionaler Anmeldung klären; bis dahin nur lokale Demo.
4. Eindeutige Regeln für leere Titel, Prioritätsreihenfolge, fehlende Termine, Zeitzone, Wochenbeginn, HTTP-Fehler und Löschbestätigung festlegen. Vorschlag: HOCH vor MITTEL vor NIEDRIG, innerhalb einer Priorität frühester Termin zuerst, ohne Termin zuletzt.
5. Synchronisationskonflikte, Datenaufbewahrung, gleichzeitige Nutzer und Serverausstattung mit Auftraggeber abstimmen.
6. Nächste Implementierung: Task-Modell/Validierung und Fachtests, danach SQLite, JSON-API, Browseroberfläche. GET /api/tasks/stats vor der dynamischen ID-Route berücksichtigen.

## 8. Abnahmecheckliste für das Gesamtprojekt

- [ ] Alle nicht optionalen Funktionen umgesetzt und getestet, inklusive Backup und Fälligkeitshinweisen.
- [ ] Q1–Q8 geprüft; verbindliche Zielwerte erfüllt, vorgeschlagene Werte abgestimmt.
- [ ] Testabdeckung mindestens 80 %, keine bekannten kritischen Bugs.
- [ ] Benutzer- und Betriebsdokumentation vorhanden; Coding-Standards eingehalten.
- [ ] Teamreview und Lehrperson klären GitLab/GitHub-Abweichung.
