# Guida test — 0.16.90-preview (sede PC: path + NB CIFS)

**versionCode:** 131  
**APK:** `PhotoVideoManager-0.16.90-preview-vc131-debug.apk`

## Cosa verificare

1. **Avvio (PC Windows):** sotto il percorso `C:\Nome` compare  
   `NB — … usa l’app CIFS dove vedrai «Nome» e non la lettera del disco.`  
   e la nota sulla sottocartella `MediaManager`.
2. **Dopo Attiva / riepilogo / Acquisisci (riga Catalogo):**  
   `PC Windows C:\Nome\MediaManager — Online` (non `S25 Ultra \Nome`).
3. **Guida stampabile / template REV4:** stessi messaggi in Avvio e Fase 7.
4. Su Esplora file PC: `C:\Nome\MediaManager\` con `catalog_hub.json` è atteso.
