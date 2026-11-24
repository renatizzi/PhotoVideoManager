# Guida test — 0.16.84-preview (LOCK test telefono USB REV4)

**versionCode:** 125  
**LOCK:** `docs/LOCK_SETUP_USB_REV4_TEST_TELEFONO.md` (checkbox 1:1)

## Preparazione
1. Installa APK `PhotoVideoManager-0.16.84-preview-vc125-debug.apk`.
2. Se in Download ci sono `DATI_CIFS*.json` vecchi, eliminali o rinominali prima della prova.

## Passi / atteso

| # | Passo | Atteso |
|---|-------|--------|
| 7 | Avvio: nome Catalogo es. `FotoCasa`; dispositivo PC Windows; disco interno o esterno | Anteprima percorso `C:\FotoCasa` (o `D:\…`); Continua OK |
| 1 | Dopo Fase 1 OK | Messaggio: «I file sono stati creati e sono pronti per essere copiati sul Desktop del PC» |
| 2 | Fase 4–5: copia DATI_CIFS.json in Download TEL; Importa | Selettore da Download TEL; import OK anche con `(1)`; Fatto abilitato |
| 3 | Fase 6 con CIFS già installata | Pulsante «Apri CIFS…» attivo e apre l’app |
| 4 | Fase 7 | Guida inizia con «Aperto CIFS…» (non «Apri CIFS») |
| 5 | Campi CIFS Name / Folder / User | Name e Folder = nome Avvio; User = `COMPUTERNAME\nomeAvvio` (niente MyMedia parallelo) |
| 6+8 | Dopo Attiva Catalogo | Sede/fonte etichettata «PC Windows»; Catalogo raggiungibile |

## Riscontro
Per ogni `#` del LOCK: OK/KO + nota breve.
