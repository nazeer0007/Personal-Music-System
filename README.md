# 🎵 TuneFlow: Personal Music System
> An Object-Oriented Music Management & Playback Application built with **Java (OOP)**, **JavaFX**, and **MySQL**.

---

## 🚀 Quick Start (How to Run)

### 1. Ensure MySQL is Running
- Open **XAMPP Control Panel** and start **MySQL** (or ensure MySQL Server is active on port `3306`).
- Default credentials used:
  - **Host:** `localhost:3306`
  - **Database:** `tuneflow` (automatically created and seeded)
  - **User:** `root`
  - **Password:** *(blank / empty)*

### 2. Run the Application
You can run either the **JavaFX Desktop GUI** or the **Console Terminal CLI**:

#### Option A: JavaFX Desktop GUI 🖥️
Double-click:
```cmd
run-gui.bat
```
*(or run from terminal: `java --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.media,javafx.fxml -cp "lib\*;bin" TuneFlowApp`)*

#### Option B: Console CLI Application 📟
Double-click:
```cmd
run-console.bat
```
*(or run from terminal: `java --module-path "javafx-sdk-23.0.1\lib" --add-modules javafx.controls,javafx.media,javafx.fxml -cp "lib\*;bin" Main`)*

#### Option C: Web Frontend Prototype 🌐
Double-click or open `index.html` in any web browser.

---

## 🛠️ Recompiling from Source

If you make any changes to the code in `src/`, run:
```cmd
compile.bat
```

---

## 📋 Features

1. **View All Songs:** Browse songs with artist, album, genre, duration, and favorite status.
2. **Search Songs:** Search across titles, artists, albums, or genres in real-time.
3. **Create Playlists:** Create customized playlists stored in MySQL.
4. **Add Songs to Playlists:** Dynamically assign songs to playlists.
5. **View Playlists:** View curated tracks, track count, and total duration.
6. **Music Playback:** Real audio playback (MP3/WAV) with JavaFX `MediaPlayer` or simulation with time progress, pause, resume, seek, and volume control.
7. **Favorites:** Quickly toggle favorite tracks.
8. **Add Music:** Browse and add local audio files via the GUI file chooser into MySQL.

---

## 🏛️ OOP Concepts Demonstrated

- **Encapsulation:** Private fields and validated getters/setters across all model classes.
- **Abstraction:** Abstract class `Media` provides base media attributes and forces implementation of `displayDetails()`.
- **Inheritance:** `Song` extends `Media`.
- **Interfaces:** `Playable` defines playback contracts (`play()`, `pause()`, `stop()`, `isPlaying()`).
- **Polymorphism:** `Song` implements `Playable` methods and overrides `Media` behavior.
- **Composition:** `Playlist` and `Artist` contain collections of `Song` objects (`has-a` relationship).

---

## 📄 Documentation
For the complete project report with architectural diagrams and module descriptions, see:
- [PROJECT_REPORT.md](PROJECT_REPORT.md)
