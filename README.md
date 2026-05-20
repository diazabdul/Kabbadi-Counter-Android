# Kabaddi Kounter

Kabaddi Kounter is an Android app for recording Kabaddi match scores, managing local match history, exporting data to JSON, and testing backend connectivity.

This project was developed for the IF5230 (Teknologi & Aplikasi Piranti Bergerak) course.

## Features
- Create a new match with Team A and Team B names.
- Update scores using +1 and +2 controls.
- Save draft score progress and finish matches.
- View, load, and delete saved match history.
- Clear all local history.
- Export all local history to a JSON document.
- Switch theme mode: Light, Dark, or Follow System.
- Test backend API response from an in-app backend test screen.

## Tech Stack
- Kotlin
- Jetpack Compose (Material 3)
- MVVM (ViewModel + StateFlow)
- Room (local persistence)
- Retrofit + OkHttp + Gson (remote API)
- Firebase Analytics

## Project Structure
- `app/src/main/java/com/example/kabaddikounter/ui`: Compose UI screens and theme.
- `app/src/main/java/com/example/kabaddikounter/viewModels`: UI/business state and actions.
- `app/src/main/java/com/example/kabaddikounter/data`: Room entities, DAO, database, repositories.
- `app/src/main/java/com/example/kabaddikounter/data/remote`: API client, service interfaces, DTOs.

## Requirements
- Android Studio (recent stable version)
- Android SDK with API 24+ (minSdk 24)
- JDK 17

## Setup
1. Clone this repository.
2. Open project in Android Studio.
3. Configure `local.properties` in project root (you can copy from `example.local.properties`).
4. Set `BACKEND_BASE_URL` in `local.properties` (must end with `/`).
5. Sync Gradle and run the app.

Example:
```properties
BACKEND_BASE_URL=https://android.bersamahotspot.com/api/
```

If `BACKEND_BASE_URL` is not set, app will fallback to:
```text
http://10.0.2.2:8000/api/
```

## Notes
- Local database currently uses destructive migration fallback when schema changes.
- Backend test endpoint used by app: `GET match` (resolved against `BASE_URL`).