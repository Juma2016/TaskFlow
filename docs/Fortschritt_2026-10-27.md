# Fortschrittsdokumentation – Software Quality

**Gruppe:** QualityForge

**Mitglieder:** Mario Shenouda, Ali Zolfaghari, Maryam Taied, Liyu Wu

**Einheit:** Qualitätsplanung / Anwendungsgerüst (offizielle Einheitsnummer nicht bekannt)

**Datum:** 27.10.2026

## Welche Arbeiten wurden durchgeführt?

- Anforderungen und Übungsauftrag analysiert; Webvariante mit Java/Maven/Javalin gewählt.
- Vorhandenes Repository erhalten; Maven-Gerüst mit Java-17-Ziel, Logging und zwei Beispielrouten eingerichtet und auf GitHub eingecheckt.
- VS-Code-Diagnose ausgewertet: Main.java wurde außerhalb des korrekt importierten Maven-Projekts interpretiert. Tatsächlichen Projektordner geöffnet und automatischen Maven-Abgleich konfiguriert.
- Qualitätsplan mit fünf Stakeholder-Views, acht messbaren Zielen, ISO/IEC-25010:2023-Zuordnung, Prüfverfahren und Maßnahmen erstellt.
- Anforderungszuordnung, Mengengerüst, Risiken, Coding-Standards und spätere Abnahme dokumentiert.
- README um Einrichtung, Betrieb, Grenzen des Gerüsts und Dokumentationslinks ergänzt.

## Welche Qualitätssicherungsmaßnahmen wurden angewandt?

### Konstruktive Maßnahmen

Maven-Konfiguration mit festem Compiler-Plugin und release 17; definierter Main-Einstieg für exec:java; target/ aus Git ausgeschlossen; VS-Code-Maven-Import konfiguriert. Qualitätsziele und Architektur vor der fachlichen Implementierung beschrieben.

### Analytische Maßnahmen

Quelltext und pom.xml geprüft; Java-Sprachserverprotokoll und Maven-Ausgaben untersucht; Dokumente mit Aufgabenblatt abgeglichen; sauberen Compile und HTTP-Smoke-Test durchgeführt. Korrekte Javalin-7-API bestätigt; keine unbegründete Umstellung auf ältere Versionen.

## Testing – was wurde getestet, wie, Ergebnis?

| Prüfung | Vorgehen | Beobachtetes Ergebnis | Grenze |
|---|---|---|---|
| Maven-Kompilierung | mvn clean compile, zusätzlich mit installiertem JDK 21 und release 17 | BUILD SUCCESS, eine Java-Quelldatei kompiliert | Kein Lauf auf eigenständigem JDK 17; keine Fachtests vorhanden |
| Anwendung starten | mvn compile exec:java | Javalin 7.2.4 startete auf Port 7070 | Lokaler Start, keine Produktionsabnahme |
| GET / | HTTP-Anfrage an localhost:7070 | 200; Willkommen bei TaskFlow! | Nur Beispielroute |
| GET /api/tasks | HTTP-Anfrage an localhost:7070/api/tasks | 200; Hier werden die Aufgaben angezeigt. | Text-Platzhalter, noch kein JSON/keine Aufgabenverwaltung |
| VS-Code-Projektimport | Java-Sprachserverprotokoll nach Öffnen des TaskFlow-Ordners geprüft | Maven-Projekt importiert; Build-Jobs beendet | Keine vollständige Bestätigung aller Problems-Einträge über UI |
| Repository | Commit a13a667 und Push nach origin/main | Gerüst auf GitHub vorhanden | GitLab-Anforderung bleibt offen |

Keine automatisierten Unit-/Integrationstests, Abdeckungsmessung, Lasttests oder Datenbanktests ausgeführt. Dafür existiert derzeit noch keine Fachimplementierung. Die ≥80-%-Abdeckung ist ein späteres Abnahmekriterium und heute nicht nachgewiesen.

## Probleme und Lösungen

| Problem | Ursache/Beobachtung | Lösung oder Restpunkt |
|---|---|---|
| Package com.taskflow stimmt laut Editor nicht | Editor erwartete main.java.com.taskflow; falscher Quellwurzelkontext | TaskFlow als Maven-Projekt geöffnet; Paketdeklaration beibehalten |
| Verdacht auf inkompatible Javalin-API | config.routes ist tatsächlich API von Javalin 7 | Version 7.2.4 und vorhandene Routen beibehalten; Compile/Start bestätigt |
| Warnungen bei Maven mit JDK 25 | Native-access/Unsafe-Warnungen aus Maven-Abhängigkeiten | Kein Compilefehler; Kontrollbuild mit installiertem JDK 21 erfolgreich |
| GitHub statt GitLab | Bestehender origin ist GitHub | Noch GitLab-Ziel oder Zustimmung zur Abweichung notwendig |
| Belastungsanforderung vs. Mengengerüst | 517.500 neue Aufgaben pro Monat bei voller Nutzerzahl | Größeres Lastszenario/Archivierung zur späteren Klärung dokumentiert |

## Arbeitsaufteilung

Es werden keine unbekannten individuellen Beiträge erfunden. Technisches Gerüst, Diagnose, Prüfungen und Dokumentationsentwurf wurden in Marios Arbeitssitzung mit KI-Unterstützung bearbeitet. Weitere tatsächliche Beiträge sind vom Team nachzutragen.

| Name | Durchgeführte Arbeiten | Bemerkungen |
|---|---|---|
| Mario Shenouda | Projektaufbau, Diagnose, Build-/HTTP-Prüfungen und Dokumentationsentwurf in dieser Arbeitssitzung | KI-Unterstützung; Teamreview ausstehend |
| Ali Zolfaghari | Noch nicht dokumentiert | Tatsächlichen Beitrag und Rolle ergänzen |
| Maryam Taied | Noch nicht dokumentiert | Tatsächlichen Beitrag und Rolle ergänzen |
| Liyu Wu | Noch nicht dokumentiert | Tatsächlichen Beitrag und Rolle ergänzen |

## Gruppenreflexion

**Entwurf auf Grundlage der Sitzung; noch keine gemeinsam bestätigte Teamreflexion.**

- **Was lief gut?** Kleines lauffähiges Gerüst; Kompilierung und beide Beispielrouten überprüft; messbare Qualitätsziele mit Anforderungen verbunden.
- **Was war herausfordernd?** Editor-Konfiguration wirkte wie ein Java-Fehler; Repository-Plattform und Mengengerüst passen noch nicht vollständig zum Auftrag.
- **Was haben wir gelernt?** Maven-Build und Editor-Diagnostik getrennt prüfen; ISO-Kriterien brauchen konkrete Messverfahren/Zielwerte; ein laufender Server ist noch kein fertiges Produkt.
- **Verbesserungen für die nächste Einheit:** Rollen gemeinsam festlegen, Plan reviewen, GitLab klären, Task-Modell mit Akzeptanzfällen entwickeln und erst danach Datenbank/API erweitern.

## Stand des heutigen Auftrags

- [x] Implementierungsvariante ausgewählt und begründet.
- [x] Anforderungen analysiert, ≥5 Qualitätsziele und ≥3 Stakeholder dokumentiert.
- [x] Kriterien, Indikatoren, Messgrößen, Zielwerte und konstruktive Maßnahmen festgelegt.
- [x] Anwendungsgerüst erstellt, geprüft und eingecheckt.
- [x] Fortschrittsdokumentation als prüfbarer Entwurf erstellt.
- [ ] Individuelle Beiträge, Namensschreibung und Reflexion durch Team bestätigen.
- [ ] GitLab-Repository einrichten oder Akzeptanz des GitHub-Repositories bestätigen lassen.
