# LOCK — Setup USB 0.16.75 errori (duplicati / stile / funzioni)

**Stato:** approvato (sviluppo)  
**Data:** 2026-11-22  
**Richiesta / test di riferimento:** Renato su 0.16.75 — «piena di errori (duplicati, stile, funzioni)»; attenersi a ripristino USB2.3; niente APK senza verifica dettagliata in ambiente  
**Approvazione Renato:** 22/11/2026 (richiesta esplicita di correzione)

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Duplicati | Una sola area status/errore; un solo Importa in Fase 5; Installa/Apri solo in 6/1; niente copy BT+USB contrastanti | Refactor globale stringhe non-setup |
| 2 | Stile non allineato al template / versioni precedenti | Guida a card accordion (come Analog); strip 6 fasi; testi ✓ come USB2.3; campi 6/2 = Name/Storage/Host/Port/Username/Password/Folder con Copia; niente Host/User editabili | Redesign brand/tema app |
| 3 | Funzioni / checkpoint | Pronto: avanza solo se script in Download OK; Fasi 2+3 e 4+5 entrambe visibili sulla pagina; Fase 5 Fatto solo con import OK; 6/1 Fatto solo se CIFS installata; niente step BT in percorso vivo | Nuove feature fuori USB2.3 |
| 4 | Residui Bluetooth in UI setup | Intro/disponibilità/guide senza pairing/BT; percorso fisso USB | Rimuovere tutto il codice legacy non raggiunto (opz.) |
| 5 | Verifica prima della prova | Unit test + assembleDebug + controlli simulabili OK in ambiente; **nessuna richiesta prova telefono** finché non verdi | — |

## Note operative

- Template: `docs/reference/ripristino_USB2.3.pdf`
- Guida test aggiornata solo a gate OK: `docs/GUIDA_TEST_0.16.76_USB_ONLY.md`
- Nuova APK solo dopo riga #5 soddisfatta in ambiente agente (REGOLE §9 / AGENTS.md).

## Esito test (dopo prova telefono)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
