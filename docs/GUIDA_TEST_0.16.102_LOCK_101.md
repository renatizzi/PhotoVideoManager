# Guida test 0.16.102 — LOCK test 101 (#1–#5)

Build: `0.16.102-preview` (versionCode 143).  
LOCK: `docs/LOCK_SETUP_TEST_101_SEI_DISPOSITIVI.md`  
Dispositivo: S25 Ultra · Catalogo: **MyMedia**

| # | Passi | Atteso |
|---|-------|--------|
| 1 | Setup → Questo dispositivo → **Attiva Catalogo** (niente Seleziona cartella) | Cartella MyMedia creata (Documenti/MyMedia); attivazione OK; nessun falso «fase terminata» |
| 2 | Acquisisci → + | **Niente** voce «Su un altro telefono». Caso in `docs/GUIDA_ALTRO_DISPOSITIVO_MOBILE.md` |
| 3 | Guida CIFS (PC o disco rete) | Domain e Enable DFS: solo titolo + valore, **senza** quadrati selezione |
| 4 | Dopo **nuovo** CONFIGURA: CIFS → Users → profilo | Nomi come in Windows (Immagini, Documenti, …) sulle cartelle note |
| 5 | Acquisisci → + → Drive → scegli cartella | Messaggio «Aggiornamento elenco…»; Drive compare senza ritentare a caso |

## APK

`/opt/cursor/artifacts/MediaManager-0.16.102-preview.apk`
