@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================
REM  Sblocca se aggiorna-da-git.bat locale
REM  blocca Git, poi lancia aggiorna-da-git.bat
REM  (che sceglie da solo il branch con versionCode
REM  piu' alto tra origin/cursor/*).
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
echo Scarico da GitHub il bat aggiornato ^(da m02 o dal branch piu' recente^)...
git fetch origin --prune
if errorlevel 1 (
  echo ERRORE: fetch non riuscito. Controlla internet / login GitHub.
  goto :end
)

REM Recupera aggiorna-da-git.bat dal branch con versionCode massimo
set "BEST_BRANCH="
set "BEST_CODE=-1"
for /f "delims=" %%R in ('git branch -r --list "origin/cursor/*" 2^>nul') do (
  set "REF=%%R"
  set "REF=!REF: =!"
  if /I not "!REF!"=="" (
    echo !REF! | findstr /I /C:"origin/HEAD" >nul
    if errorlevel 1 (
      set "CODE="
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionCode"') do (
        if not defined CODE set "CODE=%%V"
      )
      if defined CODE (
        set "CODE=!CODE: =!"
        set "CODE=!CODE:"=!"
        set "SHORT=!REF:origin/=!"
        set /a "CUR_CODE=!CODE!" 2>nul
        if !CUR_CODE! GTR !BEST_CODE! (
          set "BEST_CODE=!CUR_CODE!"
          set "BEST_BRANCH=!SHORT!"
        )
      )
    )
  )
)

if not defined BEST_BRANCH set "BEST_BRANCH=cursor/m02-architettura-applicativa-e02a"

echo Branch sorgente bat: !BEST_BRANCH! ^(code !BEST_CODE!^)
git show "origin/!BEST_BRANCH!:aggiorna-da-git.bat" > "aggiorna-da-git.bat" 2>nul
if errorlevel 1 (
  echo ERRORE: non trovo aggiorna-da-git.bat su origin/!BEST_BRANCH!
  if exist "aggiorna-da-git-vecchio.bat" (
    echo Ripristino il bat precedente...
    ren "aggiorna-da-git-vecchio.bat" "aggiorna-da-git.bat"
  )
  goto :end
)

echo.
echo OK. Nuovo aggiorna-da-git.bat recuperato.
echo Ora lo eseguo per allineare tutto il progetto...
echo.
call "%~dp0aggiorna-da-git.bat"
goto :eof

:end
echo.
pause
endlocal
