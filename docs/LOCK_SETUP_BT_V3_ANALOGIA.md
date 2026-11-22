# LOCK — Setup Catalogo BT v3 + analogia altri dispositivi

**Stato:** approvato  
**Data:** 2026-11-16  
**Richiesta / test di riferimento:** OK sviluppo Renato 16/11 — template v3 + prerequisiti + guida da presentazione 6/11; analogia locale/NAS/cloud senza inventare flussi non condivisi  
**Approvazione Renato:** 16/11/2026 (OK allo sviluppo)

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Template v3: 3 rami associazione PC (già associati / pairing / USB), senza A/B/C | Combo con le 3 voci letterali; OK apre guida del ramo | Nuove tecnologie / mirroring obbligatorio |
| 2 | Guida: 4 fasi accordion + conferma lineare 1×1 + CONFERMA; topbar MediaManager\|Setup | Una videata ramo; messaggi conferma letterali senza citare BT/USB; stampa = icona | Pagina riepilogo monolitica obbligatoria |
| 3 | Contenuto guida da presentazione 6/11 + Ottimizzazione (pairing da Telefono; già associati; dove finiscono i file; tornare all’app; solo DATI_CIFS.json; overwrite Desktop) | Testi espansione allineati a quelle fonti | Inventare passi non presenti in template/PPT |
| 4 | Prerequisiti: «Telefono» include Tablet Android; BT non già impegnato su PC e Telefono | Note esplicite nella videata «ciò che occorre»; UI continua a dire «Telefono» | Rinominare tutte le pagine in «Telefono/Tablet» |
| 5 | Analogia altri casi (locale / NAS / cloud) | Stesso scheletro UX (guida a fasi + conferma) **solo** con contenuti già documentati (SAF locale, Drive SAF, CIFS+cartella NAS); niente procedura BT inventata | Nuovi protocolli, account cloud oltre SAF, RAPIDA finta |
| 6 | File DATI_CIFS.json / no_pw.json; overwrite Desktop (BL-BT-01/02) | App e messaggi usano DATI_CIFS.json; script overwrite | Cambiare form CIFS di terze parti |

## Note operative (agente)

- Versione app: `0.16.73-preview` / `versionCode` 114.
- Specifica UI PC: `docs/reference/PVM_Template_Statici_Ottimizzazione_Setup_PC_BT_v3.pdf`.
- Fonti guida: Ottimizzazione + Difficoltà_BT 6/11 + Ultime modifiche.
- FIX: solo righe `#N`; fuori tabella = blocco totale.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
