@echo off
chcp 65001 >nul
setlocal EnableExtensions

REM ============================================
REM  Sblocca il pull se aggiorna-da-git.bat
REM  e stato creato a mano e blocca Git.
REM  Usare UNA VOLTA, poi usare aggiorna-da-git.bat
REM ============================================

cd /d "%~dp0"

echo Cartella: %CD%
echo.

if exist "aggiorna-da-git.bat" (
  echo Rinomino aggiorna-da-git.bat in aggiorna-da-git-vecchio.bat ...
  if exist "aggiorna-da-git-vecchio.bat" del /f /q "aggiorna-da-git-vecchio.bat"
  ren "aggiorna-da-git.bat" "aggiorna-da-git-vecchio.bat"
)

echo.
echo Eseguo git fetch + git pull ...
git fetch origin
git pull
if errorlevel 1 (
  echo ERRORE: pull non riuscito. Controlla il messaggio sopra.
  goto :end
)

echo.
echo OK. Ora dovresti avere il nuovo aggiorna-da-git.bat da GitHub.
echo Prossimi passi in Android Studio: Sync Project with Gradle Files, poi Run.
echo Puoi cancellare aggiorna-da-git-vecchio.bat se vuoi.

:end
echo.
pause
endlocal
