# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew kspDebugKotlin         # Run Room annotation processing only
```

No test suite is configured.

## Project Overview

IniTool is an Android app for tracking initiative order in the tabletop RPG "Das Schwarze Auge" (DSA). It lets users manage players and enemies (Gegner), then generates a sorted combat (Kampf) view based on initiative values.

## Architecture

- **Language/UI:** Kotlin with Jetpack Compose (Material3), no XML layouts
- **Navigation:** Jetpack Navigation Compose with three routes: `gegner` → `spieler` → `kampf`
- **Build:** Gradle KTS, KSP for Room annotation processing, Java 17 target, minSdk 26

### Data Layer

- **Player** is a Room `@Entity` persisted to SQLite (`initool.db`). `PlayerDao` provides Flow-based queries.
- **Encounter** is a Room `@Entity` for grouping Gegner. `EncounterDao` provides Flow-based queries.
- **Gegner** is a Room `@Entity` with ForeignKey on Encounter (cascade delete). Without selected Encounter, Gegner are held in-memory only.
- **AppDatabase** uses a singleton pattern via `getInstance()`. Current schema version: 2.

### ViewModel Layer

- `PlayerViewModel` takes `PlayerDao` via `PlayerViewModelFactory`; uses Room Flow → `StateFlow`.
- `GegnerViewModel` takes `EncounterDao` + `GegnerDao` via `GegnerViewModelFactory`; switches between DB-backed (Encounter selected) and in-memory mode.
- Both ViewModels share `SortColumn`/`SortState` types for sortable table columns.

### Screen Flow

1. **GegnerListScreen** (start destination) — add/edit/duplicate/remove enemies with name, ini, -4/-8 flags, 2W6 toggle
2. **PlayerListScreen** — add/edit/remove players with name, ini, -4/-8 flags (persisted via Room)
3. **KampfScreen** — merges players + enemies into `KampfEntry` list sorted by initiative descending. Enemies get a random W6 (or 2W6) roll added to their base ini. Features:
   - Highlight marker navigable via up/down buttons (up stops at top, down wraps around)
   - Auto-scroll (4 rows) when marker leaves visible area
   - +1/−1 buttons on base entries to adjust initiative (affects -4/-8 sub-entries too)
   - -4/-8 sub-entries are hidden when their calculated ini < 1

## Compose TextField Pattern

TextFields die an async State (Room Flow → StateFlow, flatMapLatest/stateIn) gebunden sind, müssen einen lokalen `remember`-State verwenden, um Cursor-Sprünge zu vermeiden:

```kotlin
var nameText by remember(entity.id) { mutableStateOf(entity.name) }
OutlinedTextField(
    value = nameText,
    onValueChange = {
        nameText = it
        onNameChange(it)
    }
)
```

## Room Database Migrations

**NIEMALS `fallbackToDestructiveMigration()` verwenden.** Das löscht alle bestehenden Nutzerdaten.

Bei Schema-Änderungen (neue Tabellen, neue Spalten, etc.):
1. DB-Version in `@Database(version = ...)` hochzählen
2. Explizite `Migration(oldVersion, newVersion)` schreiben mit den nötigen SQL-Statements
3. Migration via `.addMigrations(...)` registrieren
4. Bestehende Tabellen dürfen nicht verändert oder gelöscht werden, es sei denn explizit gewünscht

## Language

The app UI and code comments are in German. Maintain this convention.
