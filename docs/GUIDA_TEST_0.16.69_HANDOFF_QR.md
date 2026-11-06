# Guida test 0.16.69 — handoff QR PC → app

**Obiettivo:** portare IP + User da «Tutto pronto» a MediaManager **senza** affidarsi alla Wi‑Fi (LAN).

## Preparazione
1. Installa `PhotoVideoManager-0.16.69-preview.apk`.
2. Copia sul PC **insieme**: `CONFIGURA.cmd`, `sup.ps1`, `QRCoder.dll` (dalla stessa export/Download).
3. Esegui CONFIGURA fino a **Tutto pronto**.
4. Sul Desktop devono comparire `MediaManager_setup_handoff.png` (QR) e `.txt`.

## Passi
1. In MediaManager vai al passo CIFS.
2. Tocca **Scansiona QR dal PC** → concedi fotocamera → inquadra il QR.
3. Atteso: messaggio dati ricevuti; campo Host = IP; User = `NOMEPC\MyMedia`.
4. **Copia** Host / User / Password in CIFS; Folder = `MyMedia`; Read only OFF.
5. Attiva collegamento → Usa questa cartella sulla connessione.

## Se il QR non si legge
Apri `MediaManager_setup_handoff.txt`, copia la riga `pvmsetup:2;…`, Incolla in app → Applica codice incollato.
