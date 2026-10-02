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

## Aggiornare il codice sul PC (Windows)

Nella cartella del progetto c’è lo script `aggiorna-da-git.bat`.

1. Doppio clic su `aggiorna-da-git.bat` (oppure, nel terminale di Android Studio: `.\aggiorna-da-git.bat`).
2. Attendi il messaggio OK, poi chiudi la finestra.
3. In Android Studio: **File → Sync Project with Gradle Files**, poi **Run**.

Non incollare nel terminale le frasi delle istruzioni (es. «Torna in Home…»): non sono comandi.

## Documentazione

- `docs/ARCHITECTURE.md` — struttura M02
- Baseline requisiti: Nota Integrata v5.1 (fuori repo)
