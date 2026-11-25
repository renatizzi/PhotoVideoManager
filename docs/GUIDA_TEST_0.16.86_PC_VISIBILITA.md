# Guida test — 0.16.86-preview (PC Windows visibile)

**versionCode:** 127  
**APK:** `PhotoVideoManager-0.16.86-preview-vc127-debug.apk`  
**Focus LOCK:** #6 / #8 (etichetta «PC Windows» dopo CIFS con nome custom, es. MyCatalog)

## Cosa cambia

- Share Catalogo PC (es. `MyCatalog`) **persistito** e ripristinato al cold-start → Acquisisci non riclassifica come «Disco di rete / NAS».
- Sede Catalogo `PC_NOTEBOOK`: stessa tree SAF (o stesso share SMB) forzata a «PC Windows».
- Import `DATI_CIFS.json`: se mancano `folder`/`name`, seguono lo `share` (non più default MyMedia).
- Host SMB: porta `:445` tolta dall’hint host.

## Checklist telefono

1. Installa APK `0.16.86-preview` (vc 127).
2. Completa Setup PC (o riparti da Attiva se già fatto) con nome Catalogo custom (es. `MyCatalog`).
3. In CIFS verifica: User `PC\MyCatalog`, Folder `MyCatalog`, URI `smb://IP:445/MyCatalog/`.
4. Attiva collegamento → in Dashboard/Acquisisci la fonte deve essere **PC Windows**, non «Disco di rete / NAS».
5. Forza chiusura app e riapri: l’etichetta resta **PC Windows**.
