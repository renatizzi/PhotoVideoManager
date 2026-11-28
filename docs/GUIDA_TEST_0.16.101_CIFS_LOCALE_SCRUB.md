# Guida test 0.16.101 — nomi CIFS localizzati + scrub DATI_CIFS

Build: `0.16.101-preview` (versionCode 142).

## LOCK

`docs/LOCK_CIFS_LOCALE_DATI_SCRUB.md`

## Checklist (stessi `#N`)

1. **Nomi cartelle CIFS**  
   - Ripeti CONFIGURA su un PC con Windows in italiano (o usa la share già configurata **dopo** nuovo script).  
   - In CIFS apri `Users` → profilo.  
   - **Atteso:** vedi anche `Immagini` / `Documenti` / … (junction), non solo `Pictures` / `Documents`.  
   - Apri `Immagini`: stesso contenuto di Pictures.

2. **Scrub DATI_CIFS**  
   - Importa `DATI_CIFS` (anche se resta una copia vecchia / estensione strana in Download).  
   - Tocca **Fatto** (fine Fase 5).  
   - **Atteso:** in Download del telefono non restano file `DATI_CIFS*` (né `(1)`, né senza `.json`).

## APK

`/opt/cursor/artifacts/MediaManager-0.16.101-preview.apk`

## Nota

Il punto 1 richiede un nuovo passaggio di CONFIGURA (crea gli alias). Su share già fatte senza ri-script, gli alias non ci sono ancora.
