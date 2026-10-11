@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================
REM  MediaManager — aggiorna codice da GitHub
REM  Doppio clic su questo file
REM ============================================
REM
REM Sceglie AUTOMATICAMENTE il branch origin/cursor/*
REM con versionCode piu' alto in app/build.gradle.kts.
REM Cosi' non resta bloccato su un solo branch di consegna
REM se la build nuova e' stata pushata altrove.
REM
REM BRANCH_FALLBACK: usato solo a parita' di versionCode
REM oppure se nessuno dei branch cursor/* ha un versionCode leggibile.
REM
set "BRANCH_FALLBACK=cursor/m02-architettura-applicativa-e02a"

cd /d "%~dp0"
if errorlevel 1 (
  echo ERRORE: impossibile entrare nella cartella del progetto.
  goto :end
)

echo.
echo === Cartella progetto ===
echo %CD%
echo.

where git >nul 2>&1
if errorlevel 1 (
  echo ERRORE: Git non trovato.
  echo Apri il Terminal di Android Studio oppure installa Git for Windows.
  goto :end
)

echo === Branch attuale ^(prima dell'aggiornamento^) ===
for /f "delims=" %%B in ('git branch --show-current 2^>nul') do set "BRANCH_ATTUALE=%%B"
if defined BRANCH_ATTUALE (
  echo !BRANCH_ATTUALE!
) else (
  echo ^(nessuno / detach^)
)
echo.

echo === Scarico aggiornamenti da GitHub ^(fetch^) ===
git fetch origin --prune
if errorlevel 1 (
  echo ERRORE durante il download. Controlla internet e l'accesso a GitHub.
  goto :end
)
echo.

REM Se esiste una copia locale non tracciata di questo .bat (creata a mano),
REM Git rifiuta l'aggiornamento. La spostiamo in .bak e poi continuiamo.
for /f "delims=" %%A in ('git status --porcelain -- "aggiorna-da-git.bat" 2^>nul') do (
  echo %%A | findstr /B /C:"??" >nul
  if not errorlevel 1 (
    echo.
    echo Trovato aggiorna-da-git.bat locale non ancora su Git.
    echo Lo rinomino in aggiorna-da-git.bat.bak per permettere l'aggiornamento...
    if exist "aggiorna-da-git.bat.bak" del /f /q "aggiorna-da-git.bat.bak" >nul 2>&1
    ren "aggiorna-da-git.bat" "aggiorna-da-git.bat.bak" >nul 2>&1
  )
)

REM --- Scelta automatica: branch cursor/* con versionCode massimo ---
set "BEST_BRANCH="
set "BEST_CODE=-1"
set "BEST_NAME="
set "CANDIDATI=0"

echo === Branch cursor/* su GitHub ^(versionCode^) ===
for /f "delims=" %%R in ('git branch -r --list "origin/cursor/*" 2^>nul') do (
  set "REF=%%R"
  set "REF=!REF: =!"
  if /I not "!REF!"=="" (
    echo !REF! | findstr /I /C:"origin/HEAD" >nul
    if errorlevel 1 (
      set /a CANDIDATI+=1
      set "CODE="
      set "VNAME="
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionCode"') do (
        if not defined CODE set "CODE=%%V"
      )
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionName"') do (
        if not defined VNAME set "VNAME=%%V"
      )
      if defined CODE (
        set "CODE=!CODE: =!"
        set "CODE=!CODE:"=!"
        set "VNAME=!VNAME: =!"
        set "VNAME=!VNAME:"=!"
        set "SHORT=!REF:origin/=!"
        echo   !SHORT!  code=!CODE!  name=!VNAME!
        set /a "CUR_CODE=!CODE!" 2>nul
        if !CUR_CODE! GTR !BEST_CODE! (
          set "BEST_CODE=!CUR_CODE!"
          set "BEST_BRANCH=!SHORT!"
          set "BEST_NAME=!VNAME!"
        ) else if !CUR_CODE! EQU !BEST_CODE! (
          REM A parita': preferisci il fallback di consegna telefono.
          if /I "!SHORT!"=="%BRANCH_FALLBACK%" (
            set "BEST_BRANCH=!SHORT!"
            set "BEST_NAME=!VNAME!"
          )
        )
      ) else (
        set "SHORT=!REF:origin/=!"
        echo   !SHORT!  ^(senza versionCode^)
      )
    )
  )
)
echo.

if not defined BEST_BRANCH (
  echo Nessun versionCode trovato nei branch cursor/*.
  echo Uso il fallback: %BRANCH_FALLBACK%
  set "BEST_BRANCH=%BRANCH_FALLBACK%"
  set "BEST_CODE=-1"
)

echo === Branch scelto per le prove telefono ===
echo !BEST_BRANCH!
if !BEST_CODE! GEQ 0 (
  echo versionCode=!BEST_CODE!  versionName=!BEST_NAME!
)
echo.
echo ^(Scelta = versionCode piu' alto tra origin/cursor/*; a parita' preferisce %BRANCH_FALLBACK%^)
echo.

git show-ref --verify --quiet "refs/remotes/origin/!BEST_BRANCH!"
if errorlevel 1 (
  echo ERRORE: su GitHub non trovo il branch:
  echo   origin/!BEST_BRANCH!
  echo Contatta chi sviluppa l'app.
  goto :end
)

echo === Passo al branch scelto ===
git checkout "!BEST_BRANCH!" 2>nul
if errorlevel 1 (
  echo Creo in locale il branch da GitHub...
  git checkout -b "!BEST_BRANCH!" "origin/!BEST_BRANCH!"
  if errorlevel 1 (
    echo ERRORE: impossibile passare al branch !BEST_BRANCH!.
    echo Se hai modifiche locali non salvate, apri Android Studio e chiedi aiuto.
    goto :end
  )
)
echo.

echo === Aggiorno il codice ^(allineo a origin^) ===
git pull --ff-only origin "!BEST_BRANCH!"
if errorlevel 1 (
  echo.
  echo Il pull "pulito" non e' riuscito. Provo un reset allineato a GitHub...
  echo ^(solo se non hai modifiche locali importanti^)
  git status -sb
  echo.
  git reset --hard "origin/!BEST_BRANCH!"
  if errorlevel 1 (
    echo.
    echo ERRORE: aggiornamento non riuscito.
    echo Prova questa soluzione:
    echo   1^) Doppio clic su sblocca-e-aggiorna.bat
    echo   2^) Oppure nel Terminal di Android Studio:
    echo        git fetch origin --prune
    echo        git checkout !BEST_BRANCH!
    echo        git reset --hard origin/!BEST_BRANCH!
    goto :end
  )
)
echo.

echo === Stato dopo l'aggiornamento ===
git status -sb
echo.
for /f "delims=" %%B in ('git branch --show-current 2^>nul') do echo Branch: %%B
for /f "delims=" %%C in ('git rev-parse --short HEAD 2^>nul') do echo Commit: %%C

set "VERSIONE="
set "VCODE="
for /f "tokens=2 delims==" %%V in ('findstr /C:"versionName" "app\build.gradle.kts" 2^>nul') do (
  set "VERSIONE=%%V"
)
for /f "tokens=2 delims==" %%V in ('findstr /C:"versionCode" "app\build.gradle.kts" 2^>nul') do (
  set "VCODE=%%V"
)
if defined VERSIONE (
  set "VERSIONE=!VERSIONE: =!"
  set "VERSIONE=!VERSIONE:"=!"
  set "VCODE=!VCODE: =!"
  set "VCODE=!VCODE:"=!"
  echo Versione app: !VERSIONE! ^(code !VCODE!^)
)
echo.

echo OK. Codice aggiornato al build piu' recente trovato su GitHub.
echo Ora:
echo   1^) In Android Studio: File → Sync Project with Gradle Files
echo   2^) Oppure doppio clic su compila-apk.bat per creare l'APK
echo   3^) Installa sul telefono e fai le prove
echo.

:end
echo.
pause
endlocal
