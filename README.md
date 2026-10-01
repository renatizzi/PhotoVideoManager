# Photo&VideoManager

Applicazione Android local-first per la gestione dell’archivio personale e familiare di foto e video.

## Stack

- Kotlin, Jetpack Compose, Navigation
- Clean Architecture: Domain / Application / Adapter / Port
- Room (Catalogo logico)
- minSdk 26 · targetSdk 35 · applicationId `com.renatizzi.photovideomanager`

## Build

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

## Documentazione

- `docs/ARCHITECTURE.md` — struttura M02
- Baseline requisiti: Nota Integrata v5.1 (fuori repo)
