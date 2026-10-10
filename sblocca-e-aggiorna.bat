@echo off
chcp 65001 >nul
setlocal EnableExtensions

REM ============================================
REM  Sblocca se aggiorna-da-git.bat locale
REM  blocca Git, poi aggiorna il branch di lavoro.
REM  Usare UNA VOLTA se aggiorna-da-git.bat fallisce.
REM ============================================

set "BRANCH_LAVORO=cursor/m02-architettura-applicativa-e02a"

cd /d "%~dp0"

echo Cartella: %CD%
echo.

if exist "aggiorna-da-git.bat" (
  echo Rinomino aggiorna-da-git.bat in aggiorna-da-git-vecchio.bat ...
  if exist "aggiorna-da-git-vecchio.bat" del /f /q "aggiorna-da-git-vecchio.bat"
  ren "aggiorna-da-git.bat" "aggiorna-da-git-vecchio.bat"
)

echo.
echo Scarico da GitHub e passo al branch di lavoro...
git fetch origin
if errorlevel 1 (
  echo ERRORE: fetch non riuscito. Controlla internet / login GitHub.
  goto :end
)

git checkout "%BRANCH_LAVORO%" 2>nul
if errorlevel 1 (
  git checkout -b "%BRANCH_LAVORO%" "origin/%BRANCH_LAVORO%"
  if errorlevel 1 (
    echo ERRORE: impossibile passare a %BRANCH_LAVORO%.
    goto :end
  )
)

git reset --hard "origin/%BRANCH_LAVORO%"
if errorlevel 1 (
  echo ERRORE: aggiornamento non riuscito.
  goto :end
)

echo.
echo OK. Ora hai il codice aggiornato e il nuovo aggiorna-da-git.bat.
echo Prossimi passi: Sync in Android Studio, oppure compila-apk.bat.
echo Puoi cancellare aggiorna-da-git-vecchio.bat se vuoi.

:end
echo.
pause
endlocal
