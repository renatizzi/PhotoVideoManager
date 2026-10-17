@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

REM ============================================
REM  Sblocca se aggiorna-da-git.bat locale
REM  blocca Git, poi lancia aggiorna-da-git.bat
REM  (stessa regola di scelta branch di consegna).
REM ============================================

cd /d "%~dp0"

echo Cartella: %CD%
echo.

where git >nul 2>&1
if errorlevel 1 (
  echo ERRORE: Git non trovato.
  goto :end
)

if exist "aggiorna-da-git.bat" (
  echo Rinomino aggiorna-da-git.bat in aggiorna-da-git-vecchio.bat ...
  if exist "aggiorna-da-git-vecchio.bat" del /f /q "aggiorna-da-git-vecchio.bat"
  ren "aggiorna-da-git.bat" "aggiorna-da-git-vecchio.bat"
)

echo.
echo Scarico da GitHub i bat aggiornati ^(branch di consegna^)...
git fetch origin --prune
if errorlevel 1 (
  echo ERRORE: fetch non riuscito. Controlla internet / login GitHub.
  goto :end
)

REM Prima recupera l'helper di scelta ^(se manca in locale o e' vecchio^)
REM Usa un passaggio a due fasi: scegli branch con logica inline minima sul code+name,
REM poi riscrivi helper + aggiorna-da-git dal branch vincente.

set "BEST_BRANCH="
set "BEST_CODE=-1"
set "BEST_NAME="
set "BEST_EPOCH=0"
for /f "delims=" %%R in ('git branch -r --list "origin/cursor/*" 2^>nul') do (
  set "REF=%%R"
  set "REF=!REF: =!"
  if /I not "!REF!"=="" (
    echo !REF! | findstr /I /C:"origin/HEAD" >nul
    if errorlevel 1 (
      set "CODE="
      set "VNAME="
      set "EPOCH=0"
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionCode"') do (
        if not defined CODE set "CODE=%%V"
      )
      for /f "tokens=2 delims==" %%V in ('git show "!REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionName"') do (
        if not defined VNAME set "VNAME=%%V"
      )
      for /f "delims=" %%E in ('git log -1 --format^=%%ct "!REF!" 2^>nul') do set "EPOCH=%%E"
      if defined CODE (
        set "CODE=!CODE: =!"
        set "CODE=!CODE:"=!"
        set "VNAME=!VNAME: =!"
        set "VNAME=!VNAME:"=!"
        if not defined EPOCH set "EPOCH=0"
        set "SHORT=!REF:origin/=!"
        set /a "CUR_CODE=!CODE!" 2>nul
        set "TAKE="
        if !CUR_CODE! GTR !BEST_CODE! set "TAKE=1"
        if not defined TAKE if !CUR_CODE! EQU !BEST_CODE! (
          if /I "!VNAME!" GTR "!BEST_NAME!" (
            set "TAKE=1"
          ) else if /I "!VNAME!"=="!BEST_NAME!" (
            if !EPOCH! GTR !BEST_EPOCH! set "TAKE=1"
          )
        )
        if defined TAKE (
          set "BEST_CODE=!CUR_CODE!"
          set "BEST_BRANCH=!SHORT!"
          set "BEST_NAME=!VNAME!"
          set "BEST_EPOCH=!EPOCH!"
        )
      )
    )
  )
)

if not defined BEST_BRANCH set "BEST_BRANCH=cursor/m02-architettura-applicativa-e02a"

echo Branch sorgente bat: !BEST_BRANCH! ^(code !BEST_CODE!  name !BEST_NAME!^)

git show "origin/!BEST_BRANCH!:pvm-scegli-branch-consegna.bat" > "pvm-scegli-branch-consegna.bat" 2>nul
if errorlevel 1 (
  echo AVVISO: helper pvm-scegli-branch-consegna.bat non sul branch; continuo con aggiorna-da-git.bat
)

git show "origin/!BEST_BRANCH!:aggiorna-da-git.bat" > "aggiorna-da-git.bat" 2>nul
if errorlevel 1 (
  echo ERRORE: non trovo aggiorna-da-git.bat su origin/!BEST_BRANCH!
  if exist "aggiorna-da-git-vecchio.bat" (
    echo Ripristino il bat precedente...
    ren "aggiorna-da-git-vecchio.bat" "aggiorna-da-git.bat"
  )
  goto :end
)

git show "origin/!BEST_BRANCH!:compila-apk.bat" > "compila-apk.bat" 2>nul
if errorlevel 1 (
  echo AVVISO: non ho potuto aggiornare compila-apk.bat da origin/!BEST_BRANCH!
)

echo.
echo OK. Bat di consegna recuperati.
echo Ora eseguo aggiorna-da-git.bat per allineare tutto il progetto...
echo.
call "%~dp0aggiorna-da-git.bat"
goto :eof

:end
echo.
pause
endlocal
