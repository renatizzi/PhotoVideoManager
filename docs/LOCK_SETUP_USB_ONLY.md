# LOCK — Setup Catalogo USB-only (ripristino USB2.3)

**Stato:** approvato (sviluppo)  
**Data:** 2026-11-21  
**Richiesta / test di riferimento:** ripristino USB2.3.pdf · decisione USB-only · Fase 6 in due step · campi CIFS sola Copia  
**Approvazione Renato:** 21/11/2026 («Se non trovi altre cose che non vanno passa allo sviluppo»)  
**Versione target:** `0.16.75-preview` / `versionCode` 116

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Solo USB, niente BT setup | Nessuna combo già associati/pairing; percorso fisso USB | Ripristino ramo BT |
| 2 | 6 fasi autoconsistenti (pag. 11) | Fasi 1→6 evidenziate; una fase (o coppia 2–3 / 4–5) alla volta | Accorpamento a 4 fasi |
| 3 | Pronto vs Fatto intenzionali | Fase 1 = Pronto (crea script); fasi manuali = Fatto; dopo tap bottoni nascosti + «✓ FASE N COMPLETATA» | Autodichiarazioni «Confermo di aver…» |
| 4 | Fase 4/5 corrette (USB2.3) | Fase 4 = copia DATI_CIFS.json Desktop→Download TEL; Fase 5 = Import + checkpoint | Testo fasi 2–3 sotto etichetta 4–5 |
| 5 | Fase 6 in due step | 6/1 = spiegazioni + Installa/Apri CIFS + Fatto (checkpoint app); 6/2 = solo Copia campi + Attiva Catalogo | Un’unica pagina CIFS con edit |
| 6 | Niente campi Host/User editabili | Solo bottoni Copia da DATI_CIFS.json; se manca/invalido → messaggio e re-import | Digita IP/user a mano |
| 7 | Disponibilità a bullet | Bullet + OK totale (niente riquadri di selezione obbligatori) | Combo BT |

## Note operative (agente)

- Template: `docs/reference/ripristino_USB2.3.pdf`
- Guida integrale: `docs/reference/Guida_Setup_Catalogo_USB_ONLY.pdf`
- Guida test: `docs/GUIDA_TEST_0.16.75_USB_ONLY.md`
- FIX: solo righe `#N`; commit preferiti `fix(LOCK-N): …` o `feat(usb-only): …` coerenti.
- PROVA telefono: solo dopo unit test + assembleDebug + APK in artifact (REGOLE §9 / AGENTS.md).

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
