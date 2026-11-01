# Guida prova telefono — 0.16.54 Setup Catalogo TO-BE

**Versione:** `0.16.54-preview` · **versionCode:** `95`  
**Branch:** `cursor/setup-catalogo-tobe-integ-b602`  
**Base:** 0.16.53 (F1–F4 sede Catalogo)

## Cosa verificare

### Riga Catalogo
1. **Dashboard / Acquisisci / Aggiorna / Componi:** riga `Catalogo: <nome>` + dettaglio dispositivo/path + **Online / Offline / Non verificabile**.
2. **Organizza / Utility:** nessuna riga Catalogo.
3. Senza Catalogo: «Catalogo Personale non presente: clicca qui per crearlo» → apre Setup.

### Acquisisci
1. Sottotitolo italic + descrizione TO-BE.
2. Senza Catalogo: solo invito (niente +, CONFERMA, elenco).
3. Con Catalogo: refresh **solo icona**, niente «Tutti», + e CONFERMA come prima.

### Setup Catalogo — Avvio procedura
1. Landing unica: Nome (default `MediaManagerCatalog`), Dispositivo, Modello → **CONTINUA** (niente Imposta/Modifica).
2. **Locale:** Scelta della cartella → pick → esito «Configurazione completata correttamente».
3. **PC / NAS:** combo **Procedura rapida** (default) / **Procedura guidata** + **Avvia**.
4. RAPIDA → CIFS (Host a mano) → pick → Salva/esito.
5. GUIDATA PC → passi 1–4 poi CIFS; GUIDATA NAS → preparazione NAS.

### Componi
1. Switch **ALBUM** (default) / **RACCOLTA** (grigio finché non selezionato).
2. Un solo **+** contestuale al modo; elenco filtrato.

### Reinstall / backup
1. `allowBackup=false` + regole che escludono `pvm_catalog_seat`: dopo reinstall non deve riapparire un NAS/PC vecchio senza setup.

## Fuori scope (backlog)
- Host LAN scan, GUIDATA Sorgenti, Esc dialog completo, Cloud account, Configura reorg, font/stile UI.
