@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================
REM  MediaManager — genera APK di test (debug)
REM  Doppio clic su questo file
REM ============================================

cd /d "%~dp0"
if errorlevel 1 (
  echo ERRORE: impossibile entrare nella cartella del progetto.
  goto :end
)

echo.
echo === Cartella progetto ===
echo %CD%
echo.

if not exist "gradlew.bat" (
  echo ERRORE: gradlew.bat non trovato. Apri questa cartella in Android Studio
  echo         almeno una volta e fai Sync, poi riprova.
  goto :end
)

REM --- Java: usa JAVA_HOME se valido, altrimenti cerca il JBR di Android Studio ---
call :ensure_java
if errorlevel 1 goto :end

REM --- Android SDK: da ANDROID_HOME / ANDROID_SDK_ROOT / local.properties ---
call :ensure_android_sdk

echo === Java usato ===
if defined JAVA_HOME (
  echo JAVA_HOME=%JAVA_HOME%
  "%JAVA_HOME%\bin\java.exe" -version 2>&1
) else (
  echo JAVA_HOME non impostato — uso java dal PATH
  java -version 2>&1
)
echo.

echo === Compilazione APK debug ===
echo (puo richiedere alcuni minuti la prima volta)
echo.
call gradlew.bat :app:assembleDebug
if errorlevel 1 (
  echo.
  echo ERRORE: compilazione fallita.
  echo.
  echo Se vedi FileSystemException / mergeDebugResources / packageDebugResources:
  echo   1^) Chiudi Android Studio
  echo   2^) Doppio clic su pulisci-build.bat
  echo   3^) Riapri Studio → Sync → Run
  echo.
  echo Altrimenti:
  echo   1^) Apri il progetto in Android Studio
  echo   2^) File → Sync Project with Gradle Files
  echo   3^) Build → Build Bundle^(s^) / APK^(s^) → Build APK^(s^)
  goto :end
)

set "APK=%CD%\app\build\outputs\apk\debug\app-debug.apk"
if not exist "%APK%" (
  echo ERRORE: APK non trovato in
  echo %APK%
  goto :end
)

echo.
echo OK. APK creato:
echo %APK%
echo.
echo === Come installarlo sul telefono (senza cavo Studio) ===
echo 1^) Copia app-debug.apk sul telefono ^(USB, Drive, email, Telegram a te stesso^)
echo 2^) Sul telefono apri il file e conferma Installazione
echo 3^) Se Android blocca: Impostazioni → Sicurezza → consenti
echo    installazione da quella app ^(File / Chrome / Drive...^)
echo.
echo Apro la cartella dell'APK...
start "" explorer.exe "%CD%\app\build\outputs\apk\debug"

:end
echo.
pause
endlocal
exit /b 0

REM ------------------------------------------------------------
:ensure_java
if defined JAVA_HOME (
  if exist "%JAVA_HOME%\bin\java.exe" (
    exit /b 0
  )
  echo JAVA_HOME attuale non valido: %JAVA_HOME%
  echo Cerco Java di Android Studio...
)

REM Percorsi tipici del JBR incluso in Android Studio (Windows)
set "CAND="
if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe" set "CAND=%LOCALAPPDATA%\Programs\Android Studio\jbr"
if not defined CAND if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "CAND=%ProgramFiles%\Android\Android Studio\jbr"
if not defined CAND if exist "%ProgramFiles(x86)%\Android\Android Studio\jbr\bin\java.exe" set "CAND=%ProgramFiles(x86)%\Android\Android Studio\jbr"
if not defined CAND if exist "%USERPROFILE%\AppData\Local\Programs\Android Studio\jbr\bin\java.exe" set "CAND=%USERPROFILE%\AppData\Local\Programs\Android Studio\jbr"

REM Installazioni Toolbox / versioni numerate (prende la prima trovata)
if not defined CAND (
  for /d %%D in ("%LOCALAPPDATA%\Programs\Android Studio*") do (
    if exist "%%~D\jbr\bin\java.exe" (
      set "CAND=%%~D\jbr"
      goto :found_jbr
    )
  )
)
if not defined CAND (
  for /d %%D in ("%ProgramFiles%\Android\Android Studio*") do (
    if exist "%%~D\jbr\bin\java.exe" (
      set "CAND=%%~D\jbr"
      goto :found_jbr
    )
  )
)

:found_jbr
if defined CAND (
  set "JAVA_HOME=%CAND%"
  echo Trovato Java di Android Studio:
  echo %JAVA_HOME%
  echo.
  exit /b 0
)

REM Ultimo tentativo: java nel PATH di sistema
where java >nul 2>&1
if not errorlevel 1 (
  echo Trovato "java" nel PATH ^(senza JAVA_HOME^). Provo comunque...
  echo.
  exit /b 0
)

echo.
echo ERRORE: Java non trovato.
echo.
echo Android Studio include gia' un Java ^(cartella jbr^), ma questo script
echo non e' riuscito a trovarlo automaticamente.
echo.
echo Soluzione immediata ^(senza configurare nulla^):
echo   In Android Studio: Build → Build Bundle^(s^) / APK^(s^) → Build APK^(s^)
echo   poi clicca "locate" e copia app-debug.apk sul telefono.
echo.
echo Oppure: apri Android Studio → Settings → Build, Execution, Deployment
echo → Build Tools → Gradle → Gradle JDK ^(copia il percorso^) e dimmelo:
echo posso aggiungere quel percorso allo script.
echo.
exit /b 1

REM ------------------------------------------------------------
:ensure_android_sdk
if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools" exit /b 0
if defined ANDROID_SDK_ROOT (
  if exist "%ANDROID_SDK_ROOT%\platform-tools" (
    set "ANDROID_HOME=%ANDROID_SDK_ROOT%"
    exit /b 0
  )
)

REM local.properties generato da Android Studio dopo Sync
if exist "local.properties" (
  for /f "usebackq tokens=1,* delims==" %%A in ("local.properties") do (
    if /I "%%A"=="sdk.dir" (
      set "SDK_RAW=%%B"
    )
  )
)

if defined SDK_RAW (
  REM sdk.dir usa slash / e escape \\ — normalizziamo
  set "SDK_RAW=!SDK_RAW:\\=\!"
  set "SDK_RAW=!SDK_RAW:/=\!"
  if exist "!SDK_RAW!\platform-tools" (
    set "ANDROID_HOME=!SDK_RAW!"
    echo SDK Android da local.properties:
    echo !ANDROID_HOME!
    echo.
    exit /b 0
  )
)

if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools" (
  set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"
  echo SDK Android trovato in:
  echo %ANDROID_HOME%
  echo.
  exit /b 0
)

echo Avviso: ANDROID_HOME non impostato. Se la build fallisce,
echo apri il progetto in Android Studio e fai Sync una volta.
echo.
exit /b 0
