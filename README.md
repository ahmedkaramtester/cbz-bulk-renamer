# CBZ Bulk Renamer

A fully offline Android application for bulk renaming `.cbz` (Comic Book Zip) files using the Storage Access Framework.

## Features

- **Offline**: No network dependency
- **Safe file operations**: Uses Android Storage Access Framework (SAF) for secure access
- **Bulk rename**: Remove unwanted suffixes from CBZ filenames
- **Preview**: See proposed changes before applying them
- **Collision detection**: Prevents overwriting files with duplicate target names
- **Undo support**: Restore original filenames from the last operation
- **Modern UI**: Built with Jetpack Compose and Material 3
- **MVVM Architecture**: Clean, maintainable code structure
- **Coroutines**: Smooth, responsive UI with background processing

## Rename Rule

The app removes everything from the first underscore (`_`) to the file extension.

**Examples:**
- `chapter 210_6d22c8.cbz` → `chapter 210.cbz`
- `chapter 1_ab12cd.cbz` → `chapter 1.cbz`
- `volume 5.2_xyz789.cbz` → `volume 5.2.cbz`

## Requirements

- Android 8.0 (API 26) or higher
- Kotlin 1.9.20+
- Android Gradle Plugin 8.2.0+
- Java 11 or higher

## Building the Project

### Prerequisites

1. Install Android Studio (latest stable version)
2. Install Android SDK with:
   - Android SDK Platform 35 (or latest available)
   - Android SDK Build Tools 35.0.0 (or latest available)
   - Android Emulator (optional, for testing)

### Build Steps

1. Clone or open the project in Android Studio
2. Let Android Studio sync Gradle files (File > Sync Now)
3. Build the project:
   ```bash
   ./gradlew build
   ```
4. Build and run the app:
   ```bash
   ./gradlew installDebug
   ```
   Or use Android Studio: Run > Run 'app'

### Build Output

After successful build, the debug APK is located at:
```
app/build/outputs/apk/debug/app-debug.apk
```

For release build:
```bash
./gradlew build --build-type release
```

## Project Structure

```
app/
├── src/main/
│   ├── java/com/example/cbzbulkrenamer/
│   │   ├── MainActivity.kt           # Main activity and Compose UI
│   │   ├── ui/
│   │   │   ├── theme/                # Material 3 theme setup
│   │   │   └── screens/              # Compose screens
│   │   ├── viewmodel/
│   │   │   └── MainViewModel.kt      # MVVM ViewModel
│   │   ├── data/
│   │   │   ├── models/               # Data classes
│   │   │   └── repository/           # Repository pattern
│   │   ├── domain/
│   │   │   ├── FileScanner.kt        # CBZ file scanning logic
│   │   │   ├── RenameEngine.kt       # Rename operation logic
│   │   │   └── UndoManager.kt        # Undo/restore logic
│   │   └── util/
│   │       ├── StorageUtils.kt       # SAF utilities
│   │       └── Extensions.kt         # Kotlin extensions
│   ├── res/
│   │   ├── values/
│   │   │   └── strings.xml           # String resources
│   │   └── drawable/                 # Icons and drawables
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Usage

1. **Select Folder**: Tap "Select Folder" to choose a directory containing CBZ files
2. **View Preview**: See original and proposed new filenames
3. **Rename Files**: Tap "Rename Files" to execute the rename operation
4. **Monitor Progress**: Watch the progress indicator and result summary
5. **Undo if Needed**: Use "Undo Last Operation" to restore original names

## Architecture

- **MainActivity**: Hosts Compose UI and handles user interactions
- **MainViewModel**: Manages app state, folder selection, file scanning, and rename operations
- **FileScanner**: Scans selected directory for CBZ files
- **RenameEngine**: Implements rename logic and collision detection
- **UndoManager**: Stores and manages undo data for the last operation
- **Repository**: Manages persistence and data access (DataStore for local storage)

## Permissions

The app uses:
- `READ_EXTERNAL_STORAGE` (inherited via SAF)
- `WRITE_EXTERNAL_STORAGE` (inherited via SAF)

No broad storage permissions required. All access is mediated through the Storage Access Framework.

## Error Handling

The app gracefully handles:
- Permission revocation
- Missing or deleted files
- Duplicate target filenames
- URI invalidation
- Rename failures

All errors are reported to the user with clear, actionable messages.

## Performance

- File scanning and renaming run on background coroutines
- UI remains responsive during all operations
- Supports efficient handling of hundreds of files
- ProgressFlow updates provide real-time feedback

## License

MIT License
