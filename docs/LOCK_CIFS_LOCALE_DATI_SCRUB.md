# LOCK — CIFS nomi cartelle locali + scrub DATI_CIFS

**Stato:** in attesa OK Renato (implementazione avviata su esito chiaro 27/11)  
**Data:** 2026-11-27  
**Richiesta / test di riferimento:** esito telefono 0.16.100-preview (27/11/2026)  
**Approvazione Renato:** in attesa

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Via CIFS le cartelle sono in inglese (es. Pictures) mentre su Windows si vedono in italiano (es. Immagini) | Aprendo la share CIFS, accanto alle cartelle note del profilo compaiono anche i nomi localizzati come in Esplora risorse (es. Immagini → stessa cartella di Pictures) | Rinominare/eliminare le cartelle inglesi di sistema; UI CX File Explorer |
| 2 | Difficile eliminare a mano la versione vecchia di DATI_CIFS sul telefono (forse per estensione) | Quando la sessione non lo usa più (dopo Import OK e di nuovo a fine guida CIFS), ogni `DATI_CIFS*` in Download sparisce da solo | Fix file manager Samsung / MTP Windows |

## Note operative (agente)

- FIX previsto in 0.16.101: junction localizzati nello script PC (#1); scrub DATI_CIFS più aggressivo + secondo passaggio a fine sessione (#2).
- PROVA: guida test con gli stessi `#N`.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
