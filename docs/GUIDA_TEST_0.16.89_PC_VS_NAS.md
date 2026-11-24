# Guida test — 0.16.89-preview (PC Windows vs NAS)

**versionCode:** 130  
**APK:** `PhotoVideoManager-0.16.89-preview-vc130-debug.apk`  
**Focus LOCK:** #6 / #8 — stesso flusso CIFS (nessun cambio di processo)

## Premessa (verifica screenshot CIFS)

CIFS Documents Provider **non** distingue PC da NAS: entrambe le voci sono SMB.
Nei tuoi screenshot `MyCatalog` (`smb://192.168.68.72:445/MyCatalog/`, User `DELLRS\MyCatalog`) è coerente col PC; `SSD_SHARED` è un altro host. Il riconoscimento «PC Windows» lo fa **solo MediaManager** dopo Attiva (share noto + sede `PC_NOTEBOOK`), anche se lo Shared URI è un UUID opaco.

## Checklist

1. Installa `0.16.89-preview` (vc 130).
2. Completa setup PC + CIFS come oggi (senza nuovi passi).
3. Attiva collegamento sulla cartella MyCatalog.
4. In **Acquisisci** (e dove compare la fonte/sede di rete): etichetta **PC Windows**, non «Disco di rete / NAS».
5. `SSD_SHARED` / altri share restano NAS se non sono il Catalogo.
