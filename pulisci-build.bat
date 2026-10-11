@echo off
chcp 65001 >nul
setlocal EnableExtensions

REM ============================================
REM  MediaManager — pulisce cartelle build
REM  Usa se Sync/Run fallisce con errori su
REM  mergeDebugResources / packageDebugResources
REM  o FileSystemException su Windows
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
echo Chiudi eventuali finestre di compilazione e aspetta 2 secondi...
timeout /t 2 /nobreak >nul

echo Elimino app\build ...
if exist "app\build" rd /s /q "app\build" 2>nul
if exist "app\build" (
  echo ATTENZIONE: app\build ancora presente ^(file bloccato^).
  echo Chiudi Android Studio, antivirus in pausa sul progetto, poi riesegui.
  goto :end
)

echo Elimino build\ ^(root^) ...
if exist "build" rd /s /q "build" 2>nul

echo Elimino .gradle\caches di progetto se presenti lock...
if exist ".gradle" (
  REM non cancelliamo tutto .gradle: solo file lock comuni
  del /f /q ".gradle\*.lock" >nul 2>&1
)

echo.
echo OK. Cartelle build ripulite.
echo Ora in Android Studio:
echo   1^) File → Sync Project with Gradle Files
echo   2^) Build → Rebuild Project  ^(oppure Run sul telefono^)
echo.

:end
echo.
pause
endlocal
