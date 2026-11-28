# Guida prova — LOCK test 0.16.101 (sei percorsi)

**LOCK:** `docs/LOCK_SETUP_TEST_101_SEI_DISPOSITIVI.md`  
**Dispositivo prova:** S25 Ultra · Catalogo: **MyMedia**  
**APK:** `0.16.102-preview` — `/opt/cursor/artifacts/MediaManager-0.16.102-preview.apk`  
Dettaglio: `docs/GUIDA_TEST_0.16.102_LOCK_101.md`

Checkbox 1:1 sulle righe LOCK.

| # | Preparazione | Passi | Atteso | Riscontro |
|---|--------------|-------|--------|-----------|
| 1 | Catalogo non configurato; nome MyMedia | Setup → Questo dispositivo → **Attiva Catalogo** | Cartella creata; niente Seleziona cartella; attivazione OK | ☐ |
| 2 | — | Acquisisci → + | Voce «altro telefono» **assente**; caso in guida | ☐ |
| 3 | Share PC/disco rete | Scheda CIFS | Domain/DFS senza quadrati selezione | ☐ |
| 4 | Nuovo CONFIGURA | CIFS → profilo | Nomi come in Windows | ☐ |
| 5 | Account Google | + → Drive → scegli | Attesa visibile; Drive in elenco | ☐ |
