# Guida test 0.16.65 — Interscambio Wi‑Fi semplice (Acquisisci +)

**Versione:** `0.16.65-preview` · **versionCode:** `106`  
**LOCK:** `docs/LOCK_INTERSCAMBIO_WIFI_SEMPLICE.md`  
**Base:** 0.16.64 OK path/bottoni

## Installazione

APK da artifact / `consegna-telefono.bat` (versionCode 106).

## Checklist LOCK

| # | Cosa fare | Cosa deve vedere | OK/KO |
|---|-----------|------------------|-------|
| 1 | Acquisisci → **+** → PC → **Sì, è già pronta** | Tip breve + pulsante «Apri CIFS / Disco di rete» + «Scegli cartella». **Niente** USB/script | |
| 1b | Stesso + → PC → **No, va creata** | Guida breve 3 passi (crea share sul PC) + Apri CIFS + Scegli cartella. **Non** apre wizard sede Catalogo | |
| 2 | Linguaggio | Domanda «Da dove vuoi prendere le foto?» con 5 voci chiare | |
| 3 | + → **Disco di rete / NAS** | **Nessuna** domanda «già pronta?». Tip NAS (share già sul NAS) + Apri CIFS + Scegli cartella | |
| 3b | Dopo pick PC o NAS | Fonte in elenco Acquisisci; uso = Conferma come sempre | |
| 4 | Non rifare sede Catalogo | Aggiungere fonte **non** obbliga Setup Catalogo / GUIDATA USB | |
| 5 | + → **Un altro telefono** | Solo testo onesto (cartella condivisa / Drive). Niente pick, niente CIFS, niente discovery | |
| 6 | Dopo collegamento riuscito | Dispositivo/cartella visibile in Acquisisci; se offline, stato non in rete senza crash | |

## Fuori prova

Scan Wi‑Fi, peer telefono↔telefono, recupero auto IP, bottone rientro Cestino (BL-UI-13).
