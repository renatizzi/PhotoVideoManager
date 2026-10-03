@echo off
chcp 65001 >nul
setlocal EnableExtensions

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

echo === Compilazione APK debug ===
echo (puo richiedere alcuni minuti la prima volta)
echo.
call gradlew.bat :app:assembleDebug
if errorlevel 1 (
  echo.
  echo ERRORE: compilazione fallita.
  echo Se manca l'SDK Android, apri il progetto in Android Studio,
  echo fai Sync, poi riesegui questo file.
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
