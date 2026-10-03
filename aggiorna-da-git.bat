@echo off
chcp 65001 >nul
setlocal EnableExtensions

REM ============================================
REM  MediaManager — aggiorna codice da Git
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

where git >nul 2>&1
if errorlevel 1 (
  echo ERRORE: Git non trovato. Usa il Terminal di Android Studio
  echo         oppure installa Git for Windows.
  goto :end
)

echo === Branch attuale ===
git branch --show-current
echo.

echo === git fetch origin ===
git fetch origin
if errorlevel 1 (
  echo ERRORE durante fetch. Controlla connessione / login GitHub.
  goto :end
)
echo.

REM Se esiste una copia locale non tracciata di questo .bat (creata a mano),
REM Git rifiuta il pull. La spostiamo in .bak e poi aggiorniamo.
for /f "delims=" %%A in ('git status --porcelain -- "aggiorna-da-git.bat" 2^>nul') do (
  echo %%A | findstr /B /C:"??" >nul
  if not errorlevel 1 (
    echo.
    echo Trovato aggiorna-da-git.bat locale non ancora su Git.
    echo Lo rinomino in aggiorna-da-git.bat.bak per permettere il pull...
    if exist "aggiorna-da-git.bat.bak" del /f /q "aggiorna-da-git.bat.bak" >nul 2>&1
    ren "aggiorna-da-git.bat" "aggiorna-da-git.bat.bak" >nul 2>&1
  )
)

echo === git pull (aggiorna il branch corrente) ===
git pull
if errorlevel 1 (
  echo.
  echo ERRORE durante pull.
  echo Prova questa soluzione manuale in Esplora file:
  echo   1^) Rinomina aggiorna-da-git.bat in aggiorna-da-git-vecchio.bat
  echo   2^) Nel Terminal di Android Studio digita: git pull
  echo   3^) Poi usa il nuovo aggiorna-da-git.bat scaricato da GitHub
  goto :end
)
echo.

echo === Stato ===
git status -sb
echo.
echo OK. Ora in Android Studio: File → Sync Project with Gradle Files, poi Run.
echo.

:end
echo.
pause
endlocal
