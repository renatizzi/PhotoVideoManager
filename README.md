# MediaManager (Photo&VideoManager)

Applicazione Android local-first per la gestione dell’archivio personale e familiare di foto e video.

Nome commerciale: **MediaManager** (coerente con la linea *…Manager*, es. BoxManager).  
Identificativi tecnici invariati: `applicationId` / package `com.renatizzi.photovideomanager`.

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

1. Doppio clic su `aggiorna-da-git.bat`.
2. Attendi il messaggio OK, poi chiudi la finestra.
3. In Android Studio: **File → Sync Project with Gradle Files**, poi **Run**.

Non incollare nel terminale le frasi delle istruzioni (es. «Torna in Home…»): non sono comandi.

## APK sul telefono (senza cavo / senza Run da Studio)

1. Doppio clic su `compila-apk.bat` (dopo almeno un Sync del progetto in Studio).
2. Si apre la cartella con `app-debug.apk`.
3. Copia quel file sul telefono (USB, Google Drive, email/Telegram a te stesso).
4. Sul telefono apri il file → **Installa**.
5. Se Android chiede il permesso: consenti l’installazione dall’app usata per aprire il file.

In alternativa da Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)** → link *locate*.

## Documentazione

- `docs/ARCHITECTURE.md` — struttura M02
- Baseline requisiti: Nota Integrata v5.1 (fuori repo)
