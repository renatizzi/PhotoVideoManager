# LOCK — Setup USB REV4 (esito test telefono)

**Stato:** approvato  
**Data:** 2026-11-24  
**Richiesta / test di riferimento:** prova telefono su `0.16.82-preview` (vc 123), esito 8 punti + chiarimenti Renato  
**Approvazione Renato:** 24/11/2026 («Procedi con i fix mirati» + decisioni sotto)  
**Baseline:** `docs/LOCK_SETUP_USB_REV4.md` — **#5/#7 aggiornano** `LOCK_SETUP_PC_UN_PERCORSO` (nome unico Catalogo=CIFS; scelta disco PC)

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Fase 2: messaggio di conferma | Testo esatto: «I file sono stati creati e sono pronti per essere copiati sul Desktop del PC» | Altri testi Fase 2 |
| 2 | BUG GRAVE Fase 5: selettore non mostra PC/file nuovo; file vecchio invalido | Import del JSON sessione corrente OK anche se rinominato `(1)`; guida chiara: scegliere da Download TEL | Redesign SAF completo |
| 3 | Fase 6: Installa/Apri resta indisponibile se app presente | Pulsante sempre attivo: installa se assente, apre se presente | Cambio package CIFS |
| 4 | Fase 7: «Apri CIFS» → già aperto in Fase 6 | Testo guida: «Aperto CIFS…» (o equivalente) | Nuova sequenza Copia |
| 5 | Nome Catalogo = stesso in MediaManager e CIFS | Nome digitato in Avvio = Name/Folder/User CIFS = share + account Windows (un solo nome; niente MyMedia parallelo) | Nomi multipli paralleli |
| 6 | Qualificata «Disco di rete / NAS» invece di «PC Windows» | Sede/fonte da setup PC etichettata «PC Windows» | Cambiare protocollo SMB di CIFS |
| 7 | Scelta disco interno/esterno essenziale | UI in Avvio (con PC Windows) + guida: disco interno C: o esterno (lettera); path `{letter}:\{NomeCatalogo}` | Path libero arbitrario oltre lettera+nome |
| 8 | In CIFS il PC non si usa per bug 6 | Dopo Attiva, Catalogo raggiungibile e classificato PC Windows | Diagnosi Wi‑Fi oltre setup |

## Decisioni recepite (24/11)

- **#3:** apertura CIFS avviene in Fase 6; bottone sempre attivo.
- **#5/#6/#8:** un solo nome (Catalogo = share = account Windows = Name/Folder/User CIFS); niente dualismo MyMedia fisso vs nome utente (conferma Renato 24/11 sera).
- **#6:** etichetta dispositivo «PC Windows» (non Storage SMB).
- **#7:** rientra in scope; UI + codice + guida.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | KO → ritest 0.16.89 | CIFS non distingue PC/NAS; fix etichetta MediaManager (URI opaco + share noto) |
| 7 | | |
| 8 | KO → ritest 0.16.89 | collegato a #6 |

**Nota (25/11):** Screenshot CIFS MyCatalog coerenti col PC. CIFS = solo SMB. Fix `0.16.89`: MediaManager etichetta **PC Windows** per sede/fonte Catalogo anche con Shared URI UUID.
