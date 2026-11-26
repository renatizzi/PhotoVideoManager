# Guida test 0.16.100 — scrub post-import, DATI_CIFS fresco, Domain/DFS

Build: `0.16.100-preview` (versionCode 141).

## Cosa cambia (esito 26/11 Renato)

1. **Nomi `(1)`**: dopo **Import** riuscito di `DATI_CIFS`, l’app cancella da Download del telefono tutto il set (`CONFIGURA` / `sup` / `QRCoder`) **e** tutte le varianti `DATI_CIFS`, sia `stem (n).ext` sia `name.ext (n)`.
2. **DATI_CIFS stale MTP**: in ricerca bundle si preferisce il file **più recente** (non il solo nome canonico). Così se Windows lascia il vecchio `DATI_CIFS.json` e scrive `DATI_CIFS.json (1)`, si importa quello nuovo.
3. **Domain / Enable DFS**: stesso allineamento delle altre righe (slot checkbox + riquadro valore), **senza** bottone Copia.
4. CIFS su disco esterno: già OK in 0.16.99 — nessuna regressione attesa.

## Checklist rapida

1. Esporta set → sul PC (o via MTP) possono comparire `(1)`; Maggiordomo e purge Android accettano entrambi i pattern.
2. Completa CONFIGURA → copia `DATI_CIFS.json` in Download telefono (anche se Windows mostra sync in sospeso / lascia la copia vecchia).
3. In wizard: **Importa** → esito OK → verifica in app File che **non** restino `CONFIGURA*`, `sup*`, `QRCoder*`, `DATI_CIFS*` in Download.
4. Guida CIFS: Domain ha riquadro vuoto allineato; Enable DFS checkbox OFF + riquadro “Non attivo”; **nessun** Copia su quelle due righe.
5. Opzionale: ripeti CIFS su share disco esterno (regressione).

## APK

`/opt/cursor/artifacts/MediaManager-0.16.100-preview.apk`
