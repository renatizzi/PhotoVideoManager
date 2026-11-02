# Guida test 0.16.58 — Nota template + bug 1–5

APK: `0.16.58-preview` (versionCode 99). Template di riferimento: PDF Nota + `PVM_Template_Statici_Nota_01657`.

## Checklist rapida

1. **Riga fissa Catalogo** — 2 righe; 2ª riga corsivo blu **non** bold; path con `\`; SD solo Secondaria.
2. **Dashboard senza Catalogo** — nessun messaggio assenza; scorciatoie dim; solo **Acquisisci** attivo → Setup.
3. **Setup ×3** — MyMedia/MyDevice; Annulla/Continua; cartella Annulla/Salva; riepilogo «Catalogo censito correttamente» + Continua.
4. **Acquisisci** — testi letterali; contatori solo se N>0; hint [+]/[Conferma]; elenco titolo solo con cartelle; dialog eliminazione letterale.
5. **NOVITÀ** — pick di Pictures (con familiari/vacanze) → **una riga per sottocartella con media**, non solo root.
6. **Bug 1** — dettaglio/censimento della riga mostra i media del sottoalbero.
7. **Bug 2** — dopo [+] l’elenco si aggiorna subito.
8. **Bug 3** — ciclo vuoto → ✓ → X → dialog; NO → vuoto; poi di nuovo ✓ possibile.
9. **Bug 4** — Dettaglio ha **Indietro**.
10. **Bug 5** — in Setup Avvio, tastiera non copre i campi Nome/Modello (scroll/IME).

## Fuori scope

- Bug 6 riprogettazione completa Sorgenti/Cloud/PC (locale: [+] → SAF diretto).
- Uniformare stile bottoni / microfono: BACKLOG.
