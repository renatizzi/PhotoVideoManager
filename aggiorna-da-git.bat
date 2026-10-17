@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================
REM  MediaManager — aggiorna codice da GitHub
REM  Doppio clic su questo file
REM ============================================
REM
REM Sceglie AUTOMATICAMENTE il branch origin/cursor/*
REM di consegna telefono (vedi pvm-scegli-branch-consegna.bat):
REM   1) versionCode piu' alto
REM   2) a parita': versionName piu' alto (0.16.28 > 0.16.27)
REM   3) a parita': commit remoto piu' recente
REM   4) a parita': branch di fallback storico
REM

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

if not exist "%~dp0pvm-scegli-branch-consegna.bat" (
  echo ERRORE: manca pvm-scegli-branch-consegna.bat
  echo Esegui sblocca-e-aggiorna.bat oppure riscarica i file da GitHub.
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

echo === Branch cursor/* su GitHub ^(versionCode / versionName^) ===
for /f "delims=" %%R in ('git branch -r --list "origin/cursor/*" 2^>nul') do (
  set "REF=%%R"
  set "REF=!REF: =!"
  if /I not "!REF!"=="" (
    echo !REF! | findstr /I /C:"origin/HEAD" >nul
    if errorlevel 1 (
      set "CODE="
      set "VNAME="
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionCode"') do (
        if not defined CODE set "CODE=%%V"
      )
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionName"') do (
        if not defined VNAME set "VNAME=%%V"
      )
      set "SHORT=!REF:origin/=!"
      if defined CODE (
        set "CODE=!CODE: =!"
        set "CODE=!CODE:"=!"
        set "VNAME=!VNAME: =!"
        set "VNAME=!VNAME:"=!"
        echo   !SHORT!  code=!CODE!  name=!VNAME!
      ) else (
        echo   !SHORT!  ^(senza versionCode^)
      )
    )
  )
)
echo.

call "%~dp0pvm-scegli-branch-consegna.bat"
set "BEST_BRANCH=!PVM_BEST_BRANCH!"
set "BEST_CODE=!PVM_BEST_CODE!"
set "BEST_NAME=!PVM_BEST_NAME!"

echo === Branch scelto per le prove telefono ===
echo !BEST_BRANCH!
if !BEST_CODE! GEQ 0 (
  echo versionCode=!BEST_CODE!  versionName=!BEST_NAME!
)
echo.
echo ^(Regola: code alto; a parita' versionName; poi commit piu' recente^)
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

echo OK. Codice aggiornato al build di consegna trovato su GitHub.
echo Ora:
echo   1^) In Android Studio: File → Sync Project with Gradle Files
echo   2^) Oppure doppio clic su compila-apk.bat per creare l'APK
echo   3^) Installa sul telefono e fai le prove
echo.

:end
echo.
pause
endlocal
