# Guida test 0.16.99 — set senza (1)/(2), CIFS ordine, tempi script

Build: `0.16.99-preview` (versionCode 140).

## 1. Nomi set CONFIGURA / sup / QRCoder

**Causa:** MediaStore, se riceve un `insert` con DISPLAY_NAME già presente, crea `name (1).ext`. La purge precedente non cancellava sempre il canonic prima dell’insert.

**Fix:** purge totale (canonic + `(n)`) → un solo insert; Maggiordomo PC = ReadAllBytes → delete all → WriteAllBytes.

**Verifica telefono:** Fase 1 due volte di fila → in Download solo `CONFIGURA.cmd`, `sup.ps1`, `QRCoder.dll` (nessun `(1)`).

## 2. Ordine CIFS

Name → Storage → Domain (vuoto, no Copia) → Host → Port → Enable DFS (off, no Copia) → User → Password → Folder → **«Non valorizzare i campi opzionali»** (unico messaggio).

## 3. Popup Acquisisci «Foto dal PC»

**Apri CIFS** resta: serve aprire la connessione già salvata. Non mostrato se Setup PC non è ancora fatto (`pc_need_setup`).

## 4. Tempi script

Sì, 0.16.96–97 erano più lenti per `icacls /T` su cartelle media (anche 60s × N). In 0.16.99: ACL media **senza** `/T`, timeout 15s → tempi vicini alle versioni pre-ACL-profilo.

## Stress in agente

- `sim_set_overwrite_exhaustive.py`
- `SetupSetFileNamesTest` + Maggiordomo/script unit tests
