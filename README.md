# IniTool – DSA Initiativ-Tracker

Eine Android-App zur Verwaltung der Initiativreihenfolge im Pen-&-Paper-Rollenspiel **Das Schwarze Auge (DSA)**.

---

## Features

- **Gegner verwalten** – Name, Initiativwert, -4/-8-Aktionen, optionaler 2W6-Wurf beim Start des Kampfes
- **Spieler verwalten** – persistente Speicherung aller Spielercharaktere (Room/SQLite)
- **Encounter-System** – Gegner-Gruppen als wiederverwendbare Encounters speichern und laden
- **Kampfansicht** – automatisch sortierte Liste aller Teilnehmer nach Initiative (absteigend)
  - Zufälliger W6- oder 2W6-Wurf wird zum Basis-INI der Gegner addiert
  - -4/-8-Einträge werden automatisch berechnet und ausgeblendet, wenn INI < 1
  - Navigierbarer Markierungszeiger (hoch/runter) mit Auto-Scroll
  - +1/−1-Buttons zum Anpassen der Initiative während des Kampfes


---

## Technologie-Stack

| Bereich         | Technologie                              |
|-----------------|------------------------------------------|
| Sprache         | Kotlin                                   |
| UI              | Jetpack Compose (Material 3)             |
| Navigation      | Navigation Compose                       |
| Datenhaltung    | Room (SQLite), `initool.db`              |
| Architektur     | MVVM (ViewModel + StateFlow)             |
| Build           | Gradle KTS, KSP                          |
| Min SDK         | 26 (Android 8.0)                         |
| Target/Compile  | SDK 35, Java 17                          |

---

## App-Ablauf

```
SplashScreen  →  GegnerListScreen  →  PlayerListScreen  →  KampfScreen
```

1. **SplashScreen** – Zeigt das Fanprojekt-Logo und den DSA-Markenrechtshinweis
2. **GegnerListScreen** – Gegner hinzufügen, bearbeiten, duplizieren und entfernen; Encounter auswählen oder neu anlegen
3. **PlayerListScreen** – Spieler dauerhaft verwalten (werden in SQLite gespeichert)
4. **KampfScreen** – Zusammengeführte, nach Initiative sortierte Kampfliste mit Steuerung

---

## Build

```bash
# Debug-APK erstellen
./gradlew assembleDebug

# Release-APK erstellen
./gradlew assembleRelease

# Nur Room-Annotation-Processing ausführen
./gradlew kspDebugKotlin
```

Die erstellte APK liegt unter `app/build/outputs/apk/`.

---

## Rechtlicher Hinweis

DAS SCHWARZE AUGE, AVENTURIEN, DERE, MYRANOR, THARUN, UTHURIA und RIESLAND sind eingetragene Marken der Significant Fantasy Medienrechte GbR. Ohne vorherige schriftliche Genehmigung der Ulisses Medien und Spiel Distribution GmbH ist eine Verwendung der genannten Markenzeichen nicht gestattet.

Diese App enthält nicht-offizielle Informationen zum Rollenspiel Das Schwarze Auge und zu Aventurien. Diese Informationen können im Widerspruch zu offiziell publizierten Texten stehen.
![DSA Fanware](app/src/main/res/drawable/fan_projekt_logo.png)
