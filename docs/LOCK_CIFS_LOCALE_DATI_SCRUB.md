# LOCK — CIFS nomi cartelle locali + scrub DATI_CIFS

**Stato:** chiuso (parziale → assorbito)  
**Data:** 2026-11-27  
**Richiesta / test di riferimento:** esito telefono 0.16.100-preview (27/11/2026)  
**Approvazione Renato:** prova 0.16.101 del 28/11/2026

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Via CIFS le cartelle sono in inglese (es. Pictures) mentre su Windows si vedono in italiano (es. Immagini) | Aprendo la share CIFS, accanto alle cartelle note del profilo compaiono anche i nomi localizzati come in Esplora risorse | Rinominare/eliminare le cartelle inglesi di sistema; UI CX File Explorer |
| 2 | Difficile eliminare a mano la versione vecchia di DATI_CIFS sul telefono | Quando la sessione non lo usa più, ogni `DATI_CIFS*` in Download sparisce da solo | Fix file manager Samsung / MTP Windows |

## Esito test (dopo prova 0.16.101)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | KO residuo | «cartelle quasi tutte ancora in inglese» → assorbito in `LOCK_SETUP_TEST_101_SEI_DISPOSITIVI.md` #4 (uniformare **tutte**) |
| 2 | OK (non ripreso nel KO) | Non segnalato come problema nel test 101 multi-dispositivo |

**Segue:** `docs/LOCK_SETUP_TEST_101_SEI_DISPOSITIVI.md`
