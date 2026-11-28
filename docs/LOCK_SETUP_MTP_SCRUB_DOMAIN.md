# LOCK — Setup MTP scrub + Domain/DFS

**Stato:** chiuso  
**Data:** 2026-11-26  
**Richiesta / test di riferimento:** esito telefono 0.16.99-preview (26/11/2026, S25 Ultra + disco esterno)  
**Approvazione Renato:** OK 27/11/2026 («Questa versione sembra funzionare»)

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Restano nomi `CONFIGURA.cmd (1)` / `sup.ps1 (1)` / `QRCoder.dll (1)` (e analoghi) | Dopo Import DATI_CIFS riuscito, in Download del telefono non restano file del set né varianti `(n)` | Maggiordomo PC oltre i pattern già previsti |
| 2 | Copia/incolla `DATI_CIFS`: Windows aggiornato, telefono resta versione precedente | Import usa il bundle più recente (anche `DATI_CIFS.json (n)` / `DATI_CIFS (n).json`); dopo OK i `DATI_CIFS*` spariscono da Download | Fix driver MTP Windows |
| 3 | Manca riquadro Domain; Enable DFS non allineato | Domain e DFS: riquadri allineati alle altre righe; **nessun** bottone Copia su Domain e DFS | Altri campi CIFS |
| 4 | Test CIFS su disco esterno OK | Nessuna regressione CIFS disco esterno | — |

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | OK | versione funziona (27/11) |
| 2 | OK | versione funziona (27/11) |
| 3 | OK | versione funziona (27/11) |
| 4 | OK | già OK in test 26/11 |
