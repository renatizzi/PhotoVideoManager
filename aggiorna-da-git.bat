@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================
REM  MediaManager — aggiorna codice da GitHub
REM  Doppio clic su questo file
REM ============================================

REM Branch di lavoro da cui scaricare le build da provare sul telefono.
REM (deve esistere su GitHub; lo script ci porta automaticamente)
REM Branch di consegna per le prove telefono (allineato dopo ogni push).
set "BRANCH_LAVORO=cursor/m02-architettura-applicativa-e02a"

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

echo === Branch richiesto ===
echo %BRANCH_LAVORO%
echo.

echo === Branch attuale ^(prima dell'aggiornamento^) ===
for /f "delims=" %%B in ('git branch --show-current 2^>nul') do set "BRANCH_ATTUALE=%%B"
if defined BRANCH_ATTUALE (
  echo !BRANCH_ATTUALE!
) else (
  echo ^(nessuno / detach^)
)
echo.

echo === Scarico aggiornamenti da GitHub ^(fetch^) ===
git fetch origin
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

REM Passa sempre al branch di lavoro (anche se eravamo su un altro).
echo === Passo al branch di lavoro ===
git show-ref --verify --quiet "refs/remotes/origin/%BRANCH_LAVORO%"
if errorlevel 1 (
  echo ERRORE: su GitHub non trovo il branch:
  echo   origin/%BRANCH_LAVORO%
  echo Contatta chi sviluppa l'app: il nome del branch potrebbe essere cambiato.
  goto :end
)

git checkout "%BRANCH_LAVORO%" 2>nul
if errorlevel 1 (
  echo Creo in locale il branch di lavoro da GitHub...
  git checkout -b "%BRANCH_LAVORO%" "origin/%BRANCH_LAVORO%"
  if errorlevel 1 (
    echo ERRORE: impossibile passare al branch %BRANCH_LAVORO%.
    echo Se hai modifiche locali non salvate, apri Android Studio e chiedi aiuto.
    goto :end
  )
)
echo.

echo === Aggiorno il codice ^(pull^) ===
git pull --ff-only origin "%BRANCH_LAVORO%"
if errorlevel 1 (
  echo.
  echo Il pull "pulito" non e' riuscito. Provo un reset allineato a GitHub...
  echo ^(solo se non hai modifiche locali importanti^)
  git status -sb
  echo.
  git reset --hard "origin/%BRANCH_LAVORO%"
  if errorlevel 1 (
    echo.
    echo ERRORE: aggiornamento non riuscito.
    echo Prova questa soluzione:
    echo   1^) Rinomina aggiorna-da-git.bat in aggiorna-da-git-vecchio.bat
    echo   2^) Nel Terminal di Android Studio digita:
    echo        git fetch origin
    echo        git checkout %BRANCH_LAVORO%
    echo        git reset --hard origin/%BRANCH_LAVORO%
    echo   3^) Poi usa di nuovo aggiorna-da-git.bat
    goto :end
  )
)
echo.

echo === Stato dopo l'aggiornamento ===
git status -sb
echo.
for /f "delims=" %%B in ('git branch --show-current 2^>nul') do echo Branch: %%B
for /f "delims=" %%C in ('git rev-parse --short HEAD 2^>nul') do echo Commit: %%C

REM Mostra la versione dell'app letta da build.gradle.kts (se presente)
set "VERSIONE="
for /f "tokens=2 delims==" %%V in ('findstr /C:"versionName" "app\build.gradle.kts" 2^>nul') do (
  set "VERSIONE=%%V"
)
if defined VERSIONE (
  set "VERSIONE=!VERSIONE: =!"
  set "VERSIONE=!VERSIONE:"=!"
  echo Versione app: !VERSIONE!
)
echo.

echo OK. Codice aggiornato.
echo Ora:
echo   1^) In Android Studio: File → Sync Project with Gradle Files
echo   2^) Oppure doppio clic su compila-apk.bat per creare l'APK
echo   3^) Installa sul telefono e fai le prove
echo.

:end
echo.
pause
endlocal
