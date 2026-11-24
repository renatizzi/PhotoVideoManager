# Guida test — 0.16.87-preview (etichetta PC, non telefono)

**versionCode:** 128  
**APK:** `PhotoVideoManager-0.16.87-preview-vc128-debug.apk`  
**Focus LOCK:** #6 / #8

## Causa (confermata screenshot Renato)

In Avvio con dispositivo **PC Windows**, il campo modello restava **S25 Ultra di RENATO** (telefono).  
Il riepilogo diventava `S25 Ultra \MyCatalog` invece di `PC Windows \MyCatalog` o `DELLRS \MyCatalog`.  
User CIFS `DELLRS\MyCatalog` e path `C:\MyCatalog` erano già corretti.

## Checklist

1. Installa `0.16.87-preview` (vc 128).
2. Avvio: nome `MyCatalog`, dispositivo **PC Windows**, disco **C:**.
3. Campo etichetta PC deve diventare **PC Windows** (non S25 Ultra). Dopo import DATI_CIFS può diventare **DELLRS**.
4. Fine setup → riepilogo: `PC Windows \MyCatalog` o `DELLRS \MyCatalog` + Online (non `S25 Ultra \…`).
5. Home / Acquisisci: sede/fonte classificata PC, non telefono/NAS.
