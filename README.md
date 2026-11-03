# Uptime

Uptime is an Android app for checking website availability and response latency. It lets you maintain a watchlist, run one-off checks, and review recorded incidents. The app is branded **SitePulse** in its interface and project metadata.

## Features

- Monitor websites from a saved watchlist
- Run instant checks for URLs without adding them to the watchlist
- View status, HTTP response code, and response latency
- Review incident and per-site check history
- Search and filter sites by name, URL, and status
- Configure automatic refresh intervals
- Store monitored sites and check logs locally with Room

## Tech stack

- Kotlin and Android
- Jetpack Compose and Material 3
- Room
- OkHttp and Retrofit
- Kotlin Coroutines and Flow
- Gradle Kotlin DSL

## Project structure

```text
app/src/main/java/com/example/
├── data/       # Room database, entities, DAO, and repository
├── network/    # Website health-check implementation and results
├── ui/         # ViewModel, Compose screens, components, and theme
└── MainActivity.kt
```

## Requirements

- Android Studio with Android SDK support for API 36
- JDK 11 or newer
- Android device or emulator running Android 7.0 (API 24) or newer

## Build and run

1. Clone the repository and open it in Android Studio:

   ```bash
   git clone https://github.com/deborshi-web/Uptime.git
   cd Uptime
   ```

2. Allow Gradle to sync, then run the `app` configuration on an emulator or Android device.

To build a debug APK from the command line:

```bash
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`.

## Configuration

The repository includes `.env.example` for environment-specific secrets. Copy it to `.env` if your setup requires these values, and provide secrets through your local environment or secret manager. Do not commit real API keys or credentials. The app's current website-check functionality does not require a Gemini API key.

## Website check behavior

Checks use HTTP requests and follow redirects. URLs without a scheme are checked over HTTPS by default. The checker records response information and timing data; successful responses taking longer than 2.5 seconds are marked degraded. HTTP 5xx responses and network errors are marked down. Other non-success HTTP responses may be classified as degraded even when the server is reachable.

## License

No license is currently specified in this repository. Contact the repository owner before redistributing or using the project under particular licensing terms.
