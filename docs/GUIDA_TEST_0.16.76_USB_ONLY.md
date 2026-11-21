# Guida test — Setup Catalogo USB-only `0.16.76-preview` (vc 117)

**LOCK:** `docs/LOCK_SETUP_USB75_FIX.md`  
**Template:** `docs/reference/ripristino_USB2.3.pdf`  
**Gate ambiente:** unit test hub/data + assembleDebug OK prima di questa prova.

## Preparazione

1. Installa APK `app-debug-0.16.76-preview-vc117.apk` (sostituisce 0.16.75).
2. PC Windows + cavo USB dati; Telefono sbloccato.

## Checklist LOCK (1:1)

| # | Procedura | Atteso | OK/KO |
|---|-----------|--------|-------|
| 1 | Scorri setup PC: disponibilità → USB → fasi → 6/1 → 6/2 | Niente doppio Importa; Installa/Apri solo in 6/1; un solo blocco status/errore in fondo | |
| 2 | Confronta layout con USB2.3 | Card fasi stile accordion; strip 6; ✓ bullet; 6/2 solo Name/Storage/Host/Port/Username/Password/Folder + Copia | |
| 3 | Pronto senza spazio/permessi simulati KO; poi OK | Pronto non avanza se Download KO; con OK passa a Fase 2; Fatto 5 solo dopo Import; Fatto 6/1 solo se CIFS presente | |
| 4 | Intro + disponibilità | Nessun testo pairing/Bluetooth come percorso setup | |
| 5 | (agente) | Già verificato in ambiente; prova telefono solo dopo questo file | |

## Segnali di errore

- Pronto → messaggio Download se creazione fallisce.
- Fase 5 Fatto disabilitato finché import KO.
- 6/1 Fatto → messaggio se CIFS assente.
- 6/2 senza JSON → Indietro / reimport, niente Host/User digitabili.

## Riscontro

Tabella `#` OK/KO + nota breve.
