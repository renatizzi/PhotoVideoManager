# Guida test — 0.16.91-preview (Acquisisci: PC non NAS)

**versionCode:** 132  
**APK:** `PhotoVideoManager-0.16.91-preview-vc132-debug.apk`

## Verifica

1. Avvio PC: sotto la lettera disco c’è **solo** il NB CIFS (niente seconda riga su MediaManager).
2. Dopo Attiva, in Acquisisci la cartella sede non è più `Disco di rete / NAS \MediaManager` ma  
   `PC Windows` + `C:\MyC\MediaManager` (o il nome Catalogo usato).
3. Per aggiungere altre cartelle foto del PC: in CIFS crea/condividi quella cartella (o un’altra sotto lo share), poi **+** in Acquisisci. Lo share Catalogo non espone tutto `C:` — solo la cartella condivisa.
