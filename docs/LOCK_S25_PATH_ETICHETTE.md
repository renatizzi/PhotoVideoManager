# LOCK — S25 path / etichette rete (3 nov)

**Stato:** approvato (Renato: «Sistema le cose… stessa regola»)  
**Data:** 2026-11-03  
**Richiesta / test di riferimento:** prove S25 Ultra su 0.16.62 + screenshot Aggiorna / Pulisci / Dettaglio / Acquisisci  
**Approvazione Renato:** 2026-11-03 — sistemare i punti sotto; PC/S9 da S25 in seguito

## Contesto

Conteggi Catalogo/duplicati OK sul nuovo dispositivo. Restano etichette/path UI.

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Assegnazione «PC / notebook» al disco di rete (`test3_SDA1` / `MediaManager(sda1)`) | Dispositivo = **Disco di rete / NAS** (con host se noto), non «PC / notebook» | Filiera multi-dispositivo |
| 2 | In Pulisci path assente: solo `- 📶 Online` | Ogni riga duplicato: dispositivo + `\path` + Online (come Catalogo) | — |
| 3 | In Dettaglio Google Drive path illeggibile (`\acc=1;doc=encoded=…`) | Path = cartella leggibile (es. `\test2_drive` / sottocartella), mai stringa encoded | — |
| 4 | Path in Dettaglio/Aggiorna poco chiaro o incompleto (ripetizioni / pezzi mancanti) | Meta uniforme: dispositivo + `\path` + stato; path senza host ridondante né pezzi vuoti | — |
| 5 | Dal S25 non vedo il PC né il Tab S9 | *(non in questo LOCK)* | Visibilità altri dispositivi / filiera — «poi vediamo» |

## Note operative (agente)

- FIX solo `#1`–`#4`.
- `#5` non implementare finché Renato non apre un LOCK dedicato.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
