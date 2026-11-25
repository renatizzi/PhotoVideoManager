# Guida test — 0.16.93-preview (script sbloccato + overwrite Download)

**versionCode:** 134  
**APK:** `PhotoVideoManager-0.16.93-preview-vc134-debug.apk`

## Bugfixati
1. CONFIGURA non deve più restare fermo su `icacls` di tutto `C:\` (pagefile/Windows). ACL solo su cartelle dati.
2. I 3 file (`CONFIGURA.cmd`, `sup.ps1`, `QRCoder.dll`) in Download telefono (e Maggiordomo su PC) **sostituiscono** le versioni precedenti: niente `CONFIGURA (1).cmd`.

## Cosa fare
1. Installa APK; in Avvio genera di nuovo i file (controlla Download: solo 3 nomi senza `(1)`).
2. Copia sul Desktop PC (overwrite) e lancia CONFIGURA.
3. Deve arrivare a **Tutto pronto** senza lista errori pagefile/Windows.
4. In CIFS: share `Mytest` = disco; Attiva su `Mytest\MediaManager`.
