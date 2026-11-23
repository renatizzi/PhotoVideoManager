# Guida test — 0.16.82-preview (USB REV4)

**versionCode:** 123  
**Obiettivo:** verificare bullet ✔ uniformi + etichette fase «Fase gestita dall’app» / «Fase gestita manualmente».

## Preparazione
1. Installa APK `PhotoVideoManager-0.16.82-preview-vc123-debug.apk`.
2. Apri Setup Catalogo personale (percorso USB).

## Passi
1. **Disponibilità / USB fisico:** le liste usano ✔ (non pallino), rientro uniforme.
2. **Fase 1 e 5:** primo bullet = «Fase gestita dall’app».
3. **Fasi 2, 3, 4, 6, 7:** primo bullet = «Fase gestita manualmente» (niente «dall’utente» / «dopo conferma…»).
4. **Fasi con CTA Fatto (2–5):** non compare «Quando pronto clicca su OK».
5. **Fase 1 e 6:** compare «Quando pronto clicca su OK» con CTA OK.
6. Annulla (con conferma) da una fase intermedia → torna ad Avvio con nome/dispositivo conservati.

## Atteso
Checkbox LOCK #7 e #8 soddisfatti; nessun testo Bluetooth; sette fasi separate.
