# Guida test 0.16.63 — LOCK S25 path / etichette

Versione: `0.16.63-preview`  
LOCK: `docs/LOCK_S25_PATH_ETICHETTE.md`

## Obiettivo

Chiudere `#1`–`#4` del LOCK (non `#5` PC/S9 da S25).

## Preparazione

1. Installare `0.16.63-preview` su S25 Ultra (o Tab S9).
2. Tenere collegate almeno: cartella locale, Google Drive, share di rete tipo `MediaManager(sda1)` / `test3_SDA1`.

## Procedura

1. **#1 Acquisisci / Aggiorna / Dettaglio:** la riga rete mostra **Disco di rete / NAS** (eventualmente `· test3_SDA1`), **non** «PC / notebook».
2. **#2 Pulisci** (con duplicati): ogni riga ha dispositivo + `\path` + Online (niente solo `- 📶 Online`).
3. **#3 Dettaglio** cartella Drive: path leggibile (es. `\test2_drive`), **mai** `\acc=1;doc=encoded=…`.
4. **#4** Meta uniforme su Aggiorna/Dettaglio: dispositivo + path cartella + stato; senza host ripetuto nel path.

## Fuori prova

`#5` vedere PC e Tab S9 dal S25 — non in questo build.

## Riscontro

Screenshot Acquisisci (etichetta rete), Pulisci (meta completa), Dettaglio Drive (path pulito), Aggiorna (meta uniforme).
