# Guida test — 0.16.73-preview (Setup BT v3 + analogia)

**Obiettivo:** verificare i 3 rami PC (già associati / pairing / USB), prerequisiti, import `DATI_CIFS.json`, e le guide analoghe locale / Cloud / NAS.  
**LOCK:** `docs/LOCK_SETUP_BT_V3_ANALOGIA.md`  
**versionCode:** 114

## Preparazione

1. Installare APK `0.16.73-preview` (artifact o CI Consegna APK).  
2. PC Windows 10+ e Telefono/Tablet Android con BT (o cavo USB).  
3. CIFS Documents Provider installabile.  
4. BT libero su entrambi (nessun altro trasferimento esclusivo in corso).

## Procedura PC — ramo già associati (#1–#4, #6)

1. Setup Catalogo → dispositivo **PC Windows** → Continua.  
2. Prerequisiti: leggi note «Telefono = anche Tablet» e «BT non impegnato»; spunta e OK.  
3. Modalità: **Telefono e PC sono già associati…** → OK.  
4. Guida: espandi le 4 fasi; fase 1 OK deve aprire Condividi/BT; conferma le 4 fasi una alla volta; CONFERMA.  
5. Importa solo `DATI_CIFS.json` (ignora `no_pw.json`) → OK → CIFS solo Copia → Attiva collegamento.

**Atteso:** topbar/sottotitoli senza A/B/C; messaggi conferma senza citare BT/USB; Desktop con unica versione file.

## Procedura PC — pairing (#1–#3)

Come sopra scegliendo **NON sono ancora associati**; fase 1 deve spiegare pairing **dal Telefono**.

## Procedura PC — USB (#1)

Come sopra scegliendo **cavo USB**; titoli fasi con USB; stesso import `DATI_CIFS.json`.

## Analogia altri target (#5)

1. **Questo dispositivo:** guida 2 fasi → Seleziona Cartella → CONFERMA.  
2. **Cloud:** guida 2 fasi (SAF Drive) → cartella → CONFERMA.  
3. **Disco di rete:** guida 2 fasi (CIFS + cartella) → CONFERMA.  
**Atteso:** stesso scheletro accordion/conferma; nessun pairing BT inventato su Cloud/NAS.

## Segnali di errore

- Import di `no_pw.json` / result senza password → rifiuto.  
- CONFERMA guida disabilitata se manca una fase.  
- Dopo Invio/Ricevi BT resta sulla pagina di sistema → torna a MediaManager.

## Riscontro

| # LOCK | OK / KO | Nota |
|-------|---------|------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
