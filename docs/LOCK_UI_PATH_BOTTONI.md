# LOCK — Path uniforme + bottoni azione (3 nov)

**Stato:** approvato (Renato: elenco 1–2 da sistemare)  
**Data:** 2026-11-03  
**Richiesta / test di riferimento:** screenshot 0.16.63 S25 — path instabile; Sposta/Ripristina in fondo lista  
**Approvazione Renato:** 2026-11-03 (richiesta di fix)

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Path instabile: ripetizione cartella, sottocartella mancante, cartella vicino al nome | Formato uniforme come Acquisisci OK: **titolo = dispositivo** (`Google Drive` / `S25 Ultra` / `Disco di rete / NAS` **senza** `· host`); **meta = `\cartella - Online`**. In Dettaglio/Aggiorna/Pulisci: nome file + meta `Dispositivo \cartella - Online`. Niente `· test3_SDA1` nel titolo; niente `\MediaManager(sda1)\test3_SDA1` ridondante | Filiera PC/S9 |
| 2 | [Sposta nel cestino] e [Ripristina] in fondo lista → servono scroll | Stesso schema di CONFERMA/Pulisci: **bottoni azione sopra la lista scrollabile** (sempre visibili senza arrivare in fondo). Nessun redesign invasivo | Altri layout |

## Esito test

| # | OK / KO | Nota |
|---|---------|------|
| 1 | | |
| 2 | | |
