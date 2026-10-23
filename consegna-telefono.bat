@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================================
REM  MediaManager — UNICO script per le prove sul telefono
REM
REM  Doppio clic su QUESTO file. Fa tutto in sequenza:
REM    1) scarica da GitHub l'ultima build di consegna
REM    2) prepara Java/SDK e compila l'APK di test
REM    3) apre la cartella dell'APK (pronto da copiare/inviare)
REM
REM  Non usare altri .bat per aggiornare o compilare.
REM  Log errori: build-consegna.log (nella cartella del progetto)
REM ============================================================

cd /d "%~dp0"
if errorlevel 1 (
  echo ERRORE: impossibile entrare nella cartella del progetto.
  goto :end
)

echo.
echo ========================================
echo  MediaManager — consegna sul telefono
echo ========================================
echo Cartella: %CD%
echo.

where git >nul 2>&1
if errorlevel 1 (
  echo ERRORE: Git non trovato. Installa Git for Windows oppure
  echo apri il Terminal di Android Studio e riprova.
  goto :end
)

if not exist "gradlew.bat" (
  echo ERRORE: gradlew.bat non trovato. Apri il progetto in Android Studio
  echo almeno una volta ^(Sync^), poi riprova.
  goto :end
)

REM --- Java / SDK ---
call :ensure_java
if errorlevel 1 goto :end
call :ensure_android_sdk
if errorlevel 1 goto :end

echo === 1/3  Aggiornamento da GitHub ===
git fetch origin --prune
if errorlevel 1 (
  echo ERRORE: download da GitHub non riuscito. Controlla internet / accesso.
  goto :end
)

REM Se questo .bat locale non tracciato blocca git: backup temporaneo
for /f "delims=" %%A in ('git status --porcelain -- "consegna-telefono.bat" 2^>nul') do (
  echo %%A | findstr /B /C:"??" >nul
  if not errorlevel 1 (
    if exist "consegna-telefono.bat.bak" del /f /q "consegna-telefono.bat.bak" >nul 2>&1
    ren "consegna-telefono.bat" "consegna-telefono.bat.bak" >nul 2>&1
  )
)

call :scegli_branch_consegna
if not defined BEST_BRANCH (
  echo ERRORE: nessun branch di consegna trovato su GitHub.
  goto :end
)

echo Branch scelto: !BEST_BRANCH!
echo Versione:      !BEST_NAME! ^(code !BEST_CODE!^)
echo.
echo ^(Regola: versionCode piu' alto; a parita' versionName; poi commit piu' recente^)
echo.

git show-ref --verify --quiet "refs/remotes/origin/!BEST_BRANCH!"
if errorlevel 1 (
  echo ERRORE: non trovo origin/!BEST_BRANCH!
  goto :end
)

git checkout "!BEST_BRANCH!" 2>nul
if errorlevel 1 (
  git checkout -b "!BEST_BRANCH!" "origin/!BEST_BRANCH!"
  if errorlevel 1 (
    echo ERRORE: impossibile passare al branch !BEST_BRANCH!.
    echo Chiudi Android Studio e file aperti nel progetto, poi riprova.
    goto :end
  )
)

git pull --ff-only origin "!BEST_BRANCH!"
if errorlevel 1 (
  echo Allineamento forzato a GitHub...
  git reset --hard "origin/!BEST_BRANCH!"
  if errorlevel 1 (
    echo ERRORE: aggiornamento non riuscito.
    goto :end
  )
)

REM Se avevamo rinominato il bat, ripristina dalla versione del branch
if exist "consegna-telefono.bat.bak" (
  if not exist "consegna-telefono.bat" (
    git show "origin/!BEST_BRANCH!:consegna-telefono.bat" > "consegna-telefono.bat" 2>nul
  )
  del /f /q "consegna-telefono.bat.bak" >nul 2>&1
)

set "VERSIONE="
set "VCODE="
for /f "tokens=2 delims==" %%V in ('findstr /C:"versionName" "app\build.gradle.kts" 2^>nul') do set "VERSIONE=%%V"
for /f "tokens=2 delims==" %%V in ('findstr /C:"versionCode" "app\build.gradle.kts" 2^>nul') do set "VCODE=%%V"
set "VERSIONE=!VERSIONE: =!"
set "VERSIONE=!VERSIONE:"=!"
set "VCODE=!VCODE: =!"
set "VCODE=!VCODE:"=!"
echo OK. Codice aggiornato: !VERSIONE! ^(code !VCODE!^)
for /f "delims=" %%C in ('git rev-parse --short HEAD 2^>nul') do echo Commit: %%C
echo.

echo === 2/3  Compilazione APK ===
if defined JAVA_HOME (
  echo JAVA_HOME=%JAVA_HOME%
) else (
  echo Java: PATH di sistema
)
if defined ANDROID_HOME echo ANDROID_HOME=%ANDROID_HOME%
echo Log: %CD%\build-consegna.log
echo.

call :compile_once
if errorlevel 1 (
  echo.
  echo Prima compilazione fallita. Pulisco e riprovo una volta...
  echo.
  call gradlew.bat --stop >nul 2>&1
  if exist "app\build" rd /s /q "app\build" 2>nul
  if exist "build" rd /s /q "build" 2>nul
  timeout /t 2 /nobreak >nul
  call :compile_once
  if errorlevel 1 (
    echo.
    echo ERRORE: compilazione fallita anche al secondo tentativo.
    echo Apri e invia SOLO il file: build-consegna.log
    echo ^(ultime righe con "e:" o "FAILED"^)
    if exist "build-consegna.log" start "" notepad.exe "build-consegna.log"
    goto :end
  )
)

set "APK_DIR=%CD%\app\build\outputs\apk\debug"
set "APK=%APK_DIR%\app-debug.apk"
if not exist "%APK%" (
  echo ERRORE: APK non trovato in
  echo %APK%
  goto :end
)

echo.
echo === 3/3  APK pronto ===
echo Versione: !VERSIONE! ^(code !VCODE!^)
echo File:     %APK%
echo.
echo Copia o invia app-debug.apk sul telefono ^(USB, Drive, Telegram a te stesso^),
echo poi aprilo sul telefono e conferma Installazione.
echo.
echo Apro la cartella dell'APK...
start "" explorer.exe "%APK_DIR%"

:end
echo.
pause
endlocal
exit /b 0

REM ============================================================
:compile_once
REM Scrive tutto il log Gradle in build-consegna.log
call gradlew.bat --no-daemon :app:assembleDebug > "build-consegna.log" 2>&1
set "ERR=!errorlevel!"
if not "!ERR!"=="0" (
  echo --- Ultime righe del log ---
  powershell -NoProfile -Command "Get-Content -Path 'build-consegna.log' -Tail 40 -ErrorAction SilentlyContinue"
  exit /b 1
)
echo Compilazione OK.
exit /b 0

REM ============================================================
:scegli_branch_consegna
set "BEST_BRANCH="
set "BEST_CODE=-1"
set "BEST_NAME="
set "BEST_EPOCH=0"
set "FALLBACK=cursor/m02-architettura-applicativa-e02a"
for /f "delims=" %%R in ('git branch -r --list "origin/cursor/*" 2^>nul') do (
  set "REF=%%R"
  set "REF=!REF: =!"
  if /I not "!REF!"=="" (
    echo !REF! | findstr /I /C:"origin/HEAD" >nul
    if errorlevel 1 (
      set "CODE="
      set "VNAME="
      set "EPOCH=0"
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionCode"') do (
        if not defined CODE set "CODE=%%V"
      )
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionName"') do (
        if not defined VNAME set "VNAME=%%V"
      )
      for /f "delims=" %%E in ('git log -1 --format^=%%ct "!REF!" 2^>nul') do set "EPOCH=%%E"
      if defined CODE (
        set "CODE=!CODE: =!"
        set "CODE=!CODE:"=!"
        set "VNAME=!VNAME: =!"
        set "VNAME=!VNAME:"=!"
        if not defined EPOCH set "EPOCH=0"
        set "SHORT=!REF:origin/=!"
        set /a "CUR=!CODE!" 2>nul
        set "TAKE="
        if !CUR! GTR !BEST_CODE! set "TAKE=1"
        if not defined TAKE if !CUR! EQU !BEST_CODE! (
          if /I "!VNAME!" GTR "!BEST_NAME!" (
            set "TAKE=1"
          ) else if /I "!VNAME!"=="!BEST_NAME!" (
            if !EPOCH! GTR !BEST_EPOCH! set "TAKE=1"
          )
        )
        if defined TAKE (
          set "BEST_CODE=!CUR!"
          set "BEST_BRANCH=!SHORT!"
          set "BEST_NAME=!VNAME!"
          set "BEST_EPOCH=!EPOCH!"
        )
      )
    )
  )
)
if not defined BEST_BRANCH set "BEST_BRANCH=!FALLBACK!"
exit /b 0

REM ============================================================
:ensure_java
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" exit /b 0
set "CAND="
if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\java.exe" set "CAND=%LOCALAPPDATA%\Programs\Android Studio\jbr"
if not defined CAND if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\java.exe" set "CAND=%ProgramFiles%\Android\Android Studio\jbr"
if not defined CAND if exist "%ProgramFiles(x86)%\Android\Android Studio\jbr\bin\java.exe" set "CAND=%ProgramFiles(x86)%\Android\Android Studio\jbr"
if not defined CAND (
  for /d %%D in ("%LOCALAPPDATA%\Programs\Android Studio*") do (
    if exist "%%~D\jbr\bin\java.exe" (
      set "CAND=%%~D\jbr"
      goto :java_found
    )
  )
)
:java_found
if defined CAND (
  set "JAVA_HOME=%CAND%"
  exit /b 0
)
where java >nul 2>&1
if not errorlevel 1 exit /b 0
echo ERRORE: Java non trovato. Apri Android Studio una volta ^(Sync^) e riprova.
echo Oppure: Build → Build APK^(s^) da Android Studio.
exit /b 1

REM ============================================================
:ensure_android_sdk
set "SDK_FOUND="
if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools" set "SDK_FOUND=%ANDROID_HOME%"
if not defined SDK_FOUND if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\platform-tools" set "SDK_FOUND=%ANDROID_SDK_ROOT%"
if not defined SDK_FOUND if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools" set "SDK_FOUND=%LOCALAPPDATA%\Android\Sdk"
if not defined SDK_FOUND if exist "%USERPROFILE%\AppData\Local\Android\Sdk\platform-tools" set "SDK_FOUND=%USERPROFILE%\AppData\Local\Android\Sdk"

if not defined SDK_FOUND if exist "local.properties" (
  for /f "usebackq tokens=1,* delims==" %%A in ("local.properties") do (
    if /I "%%A"=="sdk.dir" set "SDK_RAW=%%B"
  )
)
if not defined SDK_FOUND if defined SDK_RAW (
  set "SDK_RAW=!SDK_RAW:\\=\!"
  set "SDK_RAW=!SDK_RAW:/=\!"
  if exist "!SDK_RAW!\platform-tools" set "SDK_FOUND=!SDK_RAW!"
)

if not defined SDK_FOUND (
  echo ERRORE: Android SDK non trovato.
  echo Apri Android Studio una volta ^(Sync del progetto^), poi riesegui questo .bat.
  exit /b 1
)

set "ANDROID_HOME=!SDK_FOUND!"
set "ANDROID_SDK_ROOT=!SDK_FOUND!"

REM Gradle legge sdk.dir da local.properties (obbligatorio su molti PC Windows).
set "SDK_PROP=!SDK_FOUND:\=/!"
(
  echo ## Generato da consegna-telefono.bat — non committare
  echo sdk.dir=!SDK_PROP!
) > "local.properties"
echo SDK: !SDK_FOUND!
echo local.properties aggiornato.
exit /b 0
