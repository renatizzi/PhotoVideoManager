# Guida test — 0.16.95-preview (processo Setup ripristinato)

**versionCode:** 136  
**APK:** `PhotoVideoManager-0.16.95-preview-vc136-debug.apk`

## Cosa e' stato riparato
1. Export Download **non** cancella piu' `DATI_CIFS` (causa file fantasma 0 byte / 1970 via MTP).
2. I 3 file script: overwrite in-place (niente delete+insert fragile).
3. Script: niente `icacls /T` (neanche su Users); fallback share se la radice disco e' rifiutata.
4. Maggiordomo non tocca `DATI_CIFS` / `no_pw`.

## Prima di riprovare (PC + telefono)
1. Chiudi Esplora file sul PC (tutte le finestre sul telefono).
2. Scollega e ricollega USB, oppure riavvia il telefono se `DATI_CIFS.json` fantasma resta.
3. In MediaManager: genera di nuovo i 3 file (Fase 1) e verifica in **Download del telefono** (app File) che esistano `CONFIGURA.cmd`, `sup.ps1`, `QRCoder.dll` con dimensione > 0.
4. Copia i 3 sul Desktop PC (overwrite), lancia CONFIGURA, attendi **Tutto pronto**.
5. Su Desktop PC deve comparire `DATI_CIFS.json` **non** a 0 byte.
6. Copia quel file in Download telefono (meglio: chiudi Esplora, usa copia unica; se compare fantasma, elimina da app File sul telefono prima).
