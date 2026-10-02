@echo off
chcp 65001 >nul
setlocal EnableExtensions

REM ============================================
REM  Photo&VideoManager — aggiorna codice da Git
REM  Doppio clic su questo file, oppure:
REM  click destro → Esegui
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
  echo ERRORE: Git non trovato. Installa Git for Windows oppure apri
  echo         "Terminal" da Android Studio che ha gia Git.
  goto :end
)

echo === Branch attuale ===
git branch --show-current
echo.

echo === git fetch origin ===
git fetch origin
if errorlevel 1 (
  echo ERRORE durante fetch. Controlla la connessione / login GitHub.
  goto :end
)
echo.

echo === git pull (aggiorna il branch corrente) ===
git pull
if errorlevel 1 (
  echo ERRORE durante pull.
  echo Se Git chiede di scegliere un branch, in Android Studio:
  echo   Git → Branches → checkout di cursor/m02-architettura-applicativa-e02a
  echo poi riesegui questo file.
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
