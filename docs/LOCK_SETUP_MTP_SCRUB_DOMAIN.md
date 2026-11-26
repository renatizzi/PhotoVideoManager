# LOCK — Setup MTP scrub + Domain/DFS

**Stato:** in attesa OK Renato  
**Data:** 2026-11-26  
**Richiesta / test di riferimento:** esito telefono 0.16.99-preview (26/11/2026, S25 Ultra + disco esterno)  
**Approvazione Renato:** in attesa

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Restano nomi `CONFIGURA.cmd (1)` / `sup.ps1 (1)` / `QRCoder.dll (1)` (e analoghi) | Dopo Import DATI_CIFS riuscito, in Download del telefono non restano file del set né varianti `(n)` | Maggiordomo PC oltre i pattern già previsti |
| 2 | Copia/incolla `DATI_CIFS`: Windows aggiornato, telefono resta versione precedente | Import usa il bundle più recente (anche `DATI_CIFS.json (n)` / `DATI_CIFS (n).json`); dopo OK i `DATI_CIFS*` spariscono da Download | Fix driver MTP Windows |
| 3 | Manca riquadro Domain; Enable DFS non allineato | Domain e DFS: riquadri allineati alle altre righe; **nessun** bottone Copia su Domain e DFS | Altri campi CIFS |
| 4 | Test CIFS su disco esterno OK | Nessuna regressione CIFS disco esterno | — |

## Note operative (agente)

- FIX in `0.16.100-preview` (versionCode 141): scrub post-import, prefer newest bundle, Domain/DFS UI.
- PROVA: `docs/GUIDA_TEST_0.16.100_MTP_SCRUB_DOMAIN.md` con gli stessi `#N`.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
