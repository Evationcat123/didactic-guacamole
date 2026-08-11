# Circle Day Planner

Circle Day Planner ist ein moderner visueller Tagesplaner für Android. Ein kompletter Tag von 00:00 bis 24:00 Uhr wird als Kreis dargestellt. Termine erscheinen als farbige Zeitsegmente, während ein aktueller Uhrzeiger den Fortschritt des Tages visualisiert.

## Funktionen

- 24-Stunden-Kreis im Stil einer modernen analogen Uhr
- echte aktuelle Uhrzeit und beweglicher Zeitzeiger
- Termine als farbige Segmente
- Termin erstellen, bearbeiten und löschen
- Termine per Drag-Geste auf dem Kreis verschieben
- Start- und Endzeit direkt im Editor ändern
- Kategorien, Farben und Notizen
- Erinnerungen 10 Minuten vor einem Termin per lokaler Android-Benachrichtigung
- persistente lokale Speicherung ohne Cloud und ohne Pflicht-Internet
- Heute, Morgen, Gestern und weitere Tage per Wischen
- Kalender-/Datumsauswahl
- Dark/Light/System-Theme-Auswahl
- Designoptionen für Kreis, Segmente, Zeiger und Markierungen
- Android-Home-Screen-Widget mit Tagesfortschritt und nächstem Termin
- Material-3-Oberfläche
- GitHub Actions Workflow für manuell gestartete Debug-APK-Builds

## Screenshots

> Platzhalter: Nach dem ersten Build können hier eigene Screenshots ergänzt werden.
>
> `![Hauptansicht](docs/screenshots/home.png)`
>
> `![Termin-Editor](docs/screenshots/editor.png)`

## Technischer Aufbau

- Java
- Android SDK
- Android Gradle Plugin 8.11.1
- Gradle 8.13
- Material 3 / Material Components
- AndroidX
- lokale SharedPreferences-basierte Datenspeicherung mit JSON, bewusst ohne externe Datenbank oder Cloud-Service

AGP 8.11 unterstützt API 36 und nutzt JDK 17; das Projekt baut auf API 35, um die benötigten Plattform- und Build-Tools im GitHub-Workflow klar festzulegen. Der Workflow installiert Java 17, Android SDK Platform 35 und Build Tools 35.0.0. Die verwendeten GitHub-Actions sind auf aktuelle Node-24-fähige Versionen gesetzt. Vor dem APK-Build läuft zusätzlich Android Lint, damit typische Fehler früh auffallen.

## Installation in Android Studio

1. ZIP-Archiv entpacken.
2. Den Inhalt direkt als Projekt in Android Studio öffnen.
3. Android Studio die Gradle-Abhängigkeiten synchronisieren lassen.
4. Auf einem Emulator oder Android-Gerät mit Android 8.0 (API 26) oder höher ausführen.

## GitHub-Upload

Das ZIP ist so aufgebaut, dass der Repository-Inhalt direkt auf der obersten Ebene liegt. Es gibt keinen zusätzlichen verschachtelten `CircleDayPlanner/CircleDayPlanner`-Ordner.

1. Neues GitHub-Repository erstellen.
2. Die Dateien aus dem ZIP direkt in das Repository hochladen.
3. Commit auf `main` oder `master` erstellen.
4. Unter **Actions** erscheint der Workflow **Build APK** automatisch.

## APK über GitHub Actions erzeugen

Der Workflow startet **nicht mehr automatisch bei Pushes**. Er wird bewusst nur manuell über **Actions → Build APK → Run workflow** gestartet. Damit bleibt der CI-Build unter deiner Kontrolle und erzeugt keine unerwarteten Workflows.

Der Build verwendet:

- `actions/checkout@v6`
- `actions/setup-java@v5.6.0` mit Temurin 17
- `android-actions/setup-android@v4.0.1`
- `gradle/actions/setup-gradle@v6.2.0` mit Gradle 8.13
- `actions/upload-artifact@v6`

Der Build-Schritt ist:

```text
gradle --no-daemon --stacktrace :app:assembleDebug
```

Die erzeugte Datei liegt hier:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### APK herunterladen

1. GitHub öffnen.
2. **Actions** auswählen.
3. Den erfolgreichen **Build APK**-Lauf öffnen.
4. Ganz unten bei **Artifacts** `CircleDayPlanner-debug-apk` auswählen.
5. Das Artifact herunterladen und entpacken.
6. Die enthaltene `app-debug.apk` auf dem Android-Gerät installieren.

## Verwendung

Die Startseite zeigt den gewählten Tag als großen Kreis. Jede farbige Fläche entspricht einem Zeitblock. Tippe auf einen Block, um ihn zu bearbeiten. Ziehe einen Block entlang des Kreises, um ihn zeitlich zu verschieben. Wische horizontal über einen freien Bereich des Kreises, um zum vorherigen oder nächsten Tag zu wechseln.

Über **＋ Termin** kannst du neue Tagesblöcke erstellen. Start- und Endzeit werden in 15-Minuten-Schritten gewählt. Erinnerungen werden lokal als Android-Benachrichtigung geplant.

Die Kalender-Schaltfläche oben öffnet die Datumsauswahl. Die Einstellungen erreichst du über das Menü oben rechts.

## Daten & Datenschutz

Die Termin-Daten werden lokal auf dem Gerät gespeichert. Für den normalen Betrieb ist kein Konto und keine Internetverbindung erforderlich. Erinnerungen werden über Androids lokale Alarm-/Benachrichtigungs-APIs verarbeitet. Erinnerungen werden bewusst nur beim Speichern bzw. Verschieben eines Termins geplant; es gibt keinen Boot-Trigger, der Termine nach einem Neustart automatisch neu einplant.

## Lizenz

Dieses Projekt wird standardmäßig unter der MIT-Lizenz bereitgestellt. Siehe `LICENSE`.
