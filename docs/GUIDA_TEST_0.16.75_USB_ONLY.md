# Guida test — Setup Catalogo USB-only `0.16.75-preview` (vc 116)

**LOCK:** `docs/LOCK_SETUP_USB_ONLY.md`  
**Template:** `docs/reference/ripristino_USB2.3.pdf`

## Preparazione

1. Installa APK `app-debug-0.16.75-preview-vc116.apk` (sovrascrivi la precedente).
2. PC Windows + cavo USB dati; Telefono sbloccato.
3. Eventuale CIFS Documents Provider già installata o da Play Store.

## Checklist LOCK (1:1)

| # | Procedura | Atteso | OK/KO |
|---|-----------|--------|-------|
| 1 | Avvio Setup Catalogo → PC | Nessuna combo BT (già associati / pairing); solo USB | |
| 2 | Dopo disponibilità → USB fisico → guida | Fasi 1–5 + poi 6/1 e 6/2; corrente evidenziata | |
| 3 | Fase 1 Pronto; Fasi 2–5 Fatto | Dopo tap: bottoni nascosti + «✓ FASE N COMPLETATA» | |
| 4 | Fase 4 / 5 | 4 = copia `DATI_CIFS.json` Desktop→Download; 5 = Importa + Fatto | |
| 5 | Fase 6/1 e 6/2 | 6/1 Installa/Apri + Fatto; 6/2 solo Copia + Attiva Catalogo | |
| 6 | Campi Host/User | Nessun campo editabile; solo Copia da JSON | |
| 7 | Disponibilità | Bullet + OK (senza spunte obbligatorie sui riquadri) | |

## Segnali di errore

- Import KO → messaggio con istruzioni, niente digitazione Host/User.
- CIFS non installata al Fatto di 6/1 → messaggio con istruzioni.
- Attiva Catalogo bloccato se manca JSON/password/conferma salvataggio CIFS.

## Riscontro

Inviare tabella `#` OK/KO + nota breve; non diagnosticare oltre.
