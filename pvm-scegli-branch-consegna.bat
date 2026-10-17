@echo off
REM ============================================================
REM  Helper interno — NON doppio-cliccare.
REM  Sceglie il branch di consegna telefono tra origin/cursor/*.
REM
REM  Ordine di priorita' (evita il caso 0.16.27 vs 0.16.28
REM  con lo stesso versionCode):
REM    1) versionCode piu' alto
REM    2) a parita': versionName piu' alto (es. 0.16.28 > 0.16.27)
REM    3) a parita': commit remoto piu' recente
REM    4) a parita': BRANCH_FALLBACK se presente
REM
REM  In uscita imposta (nell'ambiente del chiamante):
REM    PVM_BEST_BRANCH  PVM_BEST_CODE  PVM_BEST_NAME  PVM_BEST_EPOCH
REM  Richiede: git fetch gia' fatto; EnableDelayedExpansion nel chiamante.
REM ============================================================

set "PVM_FALLBACK=cursor/m02-architettura-applicativa-e02a"
set "PVM_BEST_BRANCH="
set "PVM_BEST_CODE=-1"
set "PVM_BEST_NAME="
set "PVM_BEST_EPOCH=0"

for /f "delims=" %%R in ('git branch -r --list "origin/cursor/*" 2^>nul') do (
  set "PVM_REF=%%R"
  set "PVM_REF=!PVM_REF: =!"
  if /I not "!PVM_REF!"=="" (
    echo !PVM_REF! | findstr /I /C:"origin/HEAD" >nul
    if errorlevel 1 (
      set "PVM_CODE="
      set "PVM_VNAME="
      set "PVM_EPOCH=0"
      for /f "tokens=2 delims==" %%V in ('git show "!PVM_REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionCode"') do (
        if not defined PVM_CODE set "PVM_CODE=%%V"
      )
      for /f "tokens=2 delims==" %%V in ('git show "!PVM_REF!:app/build.gradle.kts" 2^>nul ^| findstr /C:"versionName"') do (
        if not defined PVM_VNAME set "PVM_VNAME=%%V"
      )
      for /f "delims=" %%E in ('git log -1 --format^=%%ct "!PVM_REF!" 2^>nul') do set "PVM_EPOCH=%%E"
      if defined PVM_CODE (
        set "PVM_CODE=!PVM_CODE: =!"
        set "PVM_CODE=!PVM_CODE:"=!"
        set "PVM_VNAME=!PVM_VNAME: =!"
        set "PVM_VNAME=!PVM_VNAME:"=!"
        if not defined PVM_EPOCH set "PVM_EPOCH=0"
        set "PVM_SHORT=!PVM_REF:origin/=!"
        set /a "PVM_CUR=!PVM_CODE!" 2>nul
        set "PVM_TAKE="
        if !PVM_CUR! GTR !PVM_BEST_CODE! set "PVM_TAKE=1"
        if not defined PVM_TAKE if !PVM_CUR! EQU !PVM_BEST_CODE! (
          if /I "!PVM_VNAME!" GTR "!PVM_BEST_NAME!" (
            set "PVM_TAKE=1"
          ) else if /I "!PVM_VNAME!"=="!PVM_BEST_NAME!" (
            if !PVM_EPOCH! GTR !PVM_BEST_EPOCH! set "PVM_TAKE=1"
            if not defined PVM_TAKE if /I "!PVM_SHORT!"=="!PVM_FALLBACK!" set "PVM_TAKE=1"
          )
        )
        if defined PVM_TAKE (
          set "PVM_BEST_CODE=!PVM_CUR!"
          set "PVM_BEST_BRANCH=!PVM_SHORT!"
          set "PVM_BEST_NAME=!PVM_VNAME!"
          set "PVM_BEST_EPOCH=!PVM_EPOCH!"
        )
      )
    )
  )
)

if not defined PVM_BEST_BRANCH (
  set "PVM_BEST_BRANCH=!PVM_FALLBACK!"
  set "PVM_BEST_CODE=-1"
  set "PVM_BEST_NAME="
  set "PVM_BEST_EPOCH=0"
)

exit /b 0
