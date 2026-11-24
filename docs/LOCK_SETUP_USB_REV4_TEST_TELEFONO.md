# LOCK — Setup USB REV4 (esito test telefono)

**Stato:** bozza — in attesa OK Renato  
**Data:** 2026-11-24  
**Richiesta / test di riferimento:** prova telefono su `0.16.82-preview` (vc 123), esito 8 punti (24/11/2026)  
**Approvazione Renato:** in attesa  
**Baseline:** `docs/LOCK_SETUP_USB_REV4.md` + decisioni precedenti `LOCK_SETUP_PC_UN_PERCORSO` / etichette PC vs NAS

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Fase 2: modificare il messaggio di conferma | Testo esatto: «I file sono stati creati e sono pronti per essere copiati sul Desktop del PC» | Altri testi Fase 2 non citati |
| 2 | BUG GRAVE Fase 5: dopo copia corretta di DATI_CIFS.json in Download TEL, nel selettore non si vede né il PC né il file nuovo; resta un file vecchio non riconosciuto; secondo tentativo bloccato | Import riuscito del `DATI_CIFS.json` della sessione corrente; Fatto abilitato dopo import OK; niente blocco su file vecchio/token sbagliato | Redesign completo SAF; Bluetooth |
| 3 | BUG Fase 6: Installa/Apri CIFS resta indisponibile («app già presente») e non si può aprire l’app | Con CIFS già installata il pulsante resta utilizzabile e apre l’app (etichetta Apri); con non installata apre store/install | Cambiare package CIFS |
| 4 | Fase 7: dove dice «Apri CIFS» sostituire con formulazione «Aperto CIFS…» (già aperto in Fase 6) | Primo passo guida Fase 7 non invita più ad «Apri CIFS»; parla di CIFS già aperto | Nuova sequenza campi Copia |
| 5 | BUG GRAVE: nome Catalogo resta «MyMedia» e non recepisce il nome dato dall’utente | Il nome digitato in Avvio compare come nome Catalogo in MediaManager (riepilogo / sede) | Rinominare anche lo share Windows (vedi decisione su conflitto con LOCK un percorso) |
| 6 | BUG GRAVE: cartella qualificata come smb e non come PC Windows (già sistemato in passato) | Dopo Attiva Catalogo la sede/fonte è etichettata «PC Windows», non come NAS/smb generico | Cambiare protocollo CIFS (SMB resta il trasporto tecnico) |
| 7 | BUG GRAVE: non è chiaro come selezionare disco interno/esterno dove installare il Catalogo — serve integrazione alla Guida | Guida (stampabile + in-app dove previsto) spiega dove/come scegliere il disco; comportamento allineato a quella spiegazione | Nuovo wizard multi-path senza decisione esplicita |
| 8 | BUG GRAVE: in CIFS il PC non viene visto a causa del bug 6 → Catalogo inutilizzabile | Dopo Fase 7 + Attiva, la cartella Catalogo sul PC è raggiungibile da MediaManager via CIFS | Diagnosticare rete Wi‑Fi del telefono oltre al setup |

## Note operative (agente)

- Non patchare finché Stato ≠ `approvato`.
- `#2`, `#5`, `#6`, `#7`, `#8` hanno dipendenze reciproche: fix solo dopo chiarimenti sotto.
- `#3` e `#4` e `#1` sono isolabili; `#3` è regressione chiara da commit testi REV4.
- Preferire commit `fix(LOCK-N): …`.

## Domande aperte (bloccanti per alcune righe)

Vedi chat: chiarimenti su `#5` (quale etichetta), `#6` (smb vs Storage), `#7` (disco PC vs telefono), `#2` (evidenza file/errore).

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
| 7 | | |
| 8 | | |
