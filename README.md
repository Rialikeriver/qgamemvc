# Quantum Millionaire
A JavaFX MVC Application with Persistent Profiles, Admin Tools, and Dynamic Gameplay

Quantum Millionaire is a complete JavaFX application inspired by "Who Wants to Be a Millionaire," extended with quantum-themed lifelines, persistent player profiles, a full admin question editor, and a clean, maintainable MVC architecture. The project is designed for clarity, extensibility, and long-term maintainability, with strict separation of concerns and fully documented subsystems.

---

## 1. Project Overview

Quantum Millionaire is built around four major subsystems:

1. Core gameplay (MVC)
2. Profile and statistics persistence
3. Question loading and usage tracking
4. Media assets and UI styling

Each subsystem is isolated into its own package, with clear responsibilities and no cross-contamination of logic. All UI is built in JavaFX, all persistence uses JSON, and all game logic is deterministic and testable.

---

## 2. Architecture Summary

The project follows a strict Model–View–Controller pattern:

- **Models** contain state and deterministic logic only.
- **Views** contain layout, styling, and UI components only.
- **Controllers** handle user actions, update models, and refresh views.

This ensures clean separation of concerns and makes the system easy to extend or debug.

---

## 3. Package Breakdown

### 3.1 Pack_1 (Core MVC Gameplay)

This is the main application package. It contains:

- **Models**
  - `QModel` — game state, prize ladder, lifeline logic
  - `Question` / `Answer` — question structures, correctness flags, tiering

- **Views**
  - `QView` — main gameplay UI
  - `PlayerMenuView` — profile selection and theme selection
  - `ProfileSelectionView` — user profile management
  - `QModeSelectionView` — admin question editor UI
  - Overlay views (win/lose screens, popups)

- **Controllers**
  - `QController` — gameplay flow, lifelines, timer, ladder progression
  - `ProfileController` — profile creation, deletion, selection
  - `QModeSelectionController` — admin question editing and persistence

- **Application Entry Point**
  - `QMillionaireMVC` — initializes the application, loads profiles, sets up screens

- **Utility Classes**
  - `QSplash` — startup splash screen
  - `QMillionaireException` — custom exception type
  - Overlay helpers and UI utilities

This package contains all gameplay logic and UI, but no persistence or file I/O.

---

### 3.2 Pack_1.profile (User Profiles and Statistics)

This subsystem manages:

- Player accounts
- Session tracking
- Win/loss statistics
- Lifeline usage history
- JSON-based persistence

Key classes:

- `User` — persistent player data
- `Session` — currently active user
- `JsonUserStore` — loads/saves user profiles
- `JsonStatsStore` — loads/saves global statistics
- `UserManager` — unified API for creating, deleting, and updating profiles

This package is UI-agnostic and interacts with controllers only through clean APIs.

---

### 3.3 Database (Question Loading and Usage Tracking)

This package provides the data-access layer for question management.

- `QuestionLoader`
  - Loads question sets from JSON files on the classpath
  - Supports default and alternate language datasets
  - Used by gameplay and admin tools

- `SaveManager`
  - Tracks which questions have been used
  - Ensures each tier always has unused entries
  - Auto-repairs corrupted or exhausted tiers
  - Stores usage in `SaveFile.json`

This package contains no UI or game logic; it serves purely as a data-access layer.

---

### 3.4 assets (Media Resources)

This package contains all static media used by the application:

- Background images
- UI concept art
- Video clips
- Audio (if added later)

Assets are loaded using JavaFX resource lookups such as:

getClass().getResource("/assets/filename.ext");

Although the package contains no Java classes, it serves as a logical namespace for organizing all media resources.

---

## 4. Gameplay Features

- 15-tier money ladder with dynamic highlighting
- Quantum-themed lifelines:
  - Superposition (50/50 variant)
  - Entanglement (swap question)
  - Interference (time freeze or reveal mechanic)
- Timer-based question answering
- Full-screen overlays for win/lose states
- Persistent player profiles and statistics
- Admin panel for adding, editing, and deleting questions
- JSON-based question bank with tiered difficulty
- Automatic question usage tracking and tier repair

---

## 5. Admin Tools

The admin panel allows:

- Adding new questions
- Editing existing questions
- Deleting questions
- Refreshing the question list
- Editing tiers, answers, and correctness flags
- Auto-generating question IDs based on tier

The editor uses Swing dialogs for simplicity, while the rest of the UI uses JavaFX.

---

## 6. Persistence Details

### 6.1 Question Bank

Questions are stored in JSON format:

[
{
"id": "T01-Q123",
"tier": 1,
"question": "What is the capital of France?",
"answers": [
{"label": "A", "text": "Paris", "correct": true},
{"label": "B", "text": "London", "correct": false},
...
]
}
]


### 6.2 Save File

`SaveManager` stores question usage in:

SaveFile.json


Mapping question IDs to boolean usage flags.

### 6.3 Profiles

Profiles and statistics are stored in:



users.json
stats.json

Both are human-readable and safe to edit manually.

---

## 7. Running the Application

1. Install Java 17+ and JavaFX.
2. Open the project in your IDE (IntelliJ recommended).
3. Ensure the `assets` folder is marked as a Resources Root if required.
4. Run the main class:

Pack_1.QMillionaireMVC

---

## 8. Extending the Project

You can safely extend the system by:

- Adding new lifelines (modify `QModel` and `QController`)
- Adding new UI themes (CSS + `applyTheme` in `QView`)
- Adding new question sets (JSON files in assets)
- Adding new overlays (extend `QView.showOverlay`)
- Adding new admin tools (extend `QModeSelectionView` and controller)

The strict MVC structure ensures extensions remain clean and maintainable.

---

## 9. Notes

- All JSON files are human-readable.
- The admin panel allows live editing of the question bank.
- The SaveManager ensures tiers never run out of questions.
- The project is fully documented with class-level and package-level Javadoc.
- The assets package contains all media resources and is loaded via the classpath.

