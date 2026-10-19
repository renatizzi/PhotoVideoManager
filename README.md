# MediaManager (Photo&VideoManager)

Applicazione Android local-first per la gestione dell’archivio personale e familiare di foto e video.

Nome commerciale: **MediaManager** (coerente con la linea *…Manager*, es. BoxManager).  
Identificativi tecnici invariati: `applicationId` / package `com.renatizzi.photovideomanager`.

## Stack

- Kotlin, Jetpack Compose, Navigation
- Clean Architecture: Domain / Application / Adapter / Port
- Room (Catalogo logico)
- minSdk 26 · targetSdk 35 · applicationId `com.renatizzi.photovideomanager`

## Build (Linux / CI)

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

## Prove sul telefono (Windows) — un solo script

Nella cartella del progetto usa solo `consegna-telefono.bat`.

1. Apri il progetto **almeno una volta** in Android Studio e fai **Sync** (così esistono SDK e `local.properties`).
2. Doppio clic su `consegna-telefono.bat`.
   - Scarica da GitHub l’ultima build di consegna
   - Compila l’APK di test
   - Apre la cartella con `app-debug.apk`
3. Copia quel file sul telefono (USB, Google Drive, email/Telegram a te stesso).
4. Sul telefono apri il file → **Installa**.

Lo script cerca da solo il Java di Android Studio (cartella `jbr`): **non serve** impostare `JAVA_HOME` a mano.

Se dice ancora “Java non trovato”: in Android Studio usa  
**Build → Build Bundle(s) / APK(s) → Build APK(s)** → link *locate* (stesso risultato, senza terminale).

Non incollare nel terminale le frasi delle istruzioni (es. «Torna in Home…»): non sono comandi.

## Se Sync/Run fallisce su Windows (FileSystemException / mergeDebugResources)

Spesso è la cartella `app\build` bloccata o sporca (antivirus, OneDrive, Studio ancora in compilazione).

1. Chiudi Android Studio.
2. Doppio clic su `pulisci-build.bat`.
3. Riesegui `consegna-telefono.bat` (oppure in Studio: Sync → Run).

In alternativa da Studio: **Build → Clean Project**, poi **Rebuild Project**.
