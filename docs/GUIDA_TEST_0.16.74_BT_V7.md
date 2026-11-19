# Guida test — 0.16.74-preview (Setup BT v7)

**APK:** `0.16.74-preview` · `versionCode` 115  
**Obiettivo:** porte/CTA, stampa PDF REV9, ascolto DATI_CIFS, maggiordomo overwrite.  
**Prerequisito agente:** unit test + `assembleDebug` OK in Cloud prima della prova telefono.

## Preparazione
1. Installa APK debug 0.16.74 (`versionCode` 115).
2. PC Windows + Telefono; scegli un solo metodo (BT o USB) e mantienilo.

## Passi
1. Setup Catalogo → PC Windows → scegli ramo (già associati / pairing / USB).
2. Guida: vedi **una sola fase**, lampade, flowchart a 4. Tap su «qui 🖨» → si apre il **PDF** REV9 (non share di testo).
3. Fase 1: CTA **Invia set di configurazione** (BT) oppure istruzioni USB. Nessun «Confermo di aver…» / «Continua» / «Puoi passare…».
4. Dopo invio: CTA **Apri istruzioni script** → stato «In attesa dei dati dal PC».
5. Sul PC: avvia CONFIGURA (Desktop o cartella di ricezione). Verifica che non restino `CONFIGURA (1)` / `DATI_CIFS (1)`.
6. Quando l’app rileva `DATI_CIFS.json`: lampada verde + **Usa questi dati**. Fallback: «Non arriva?» → Importa.
7. **Vai a configurazione CIFS** → Copia campi → **Attiva Catalogo**.

## Atteso
- Stampa = visualizzatore PDF.
- Avanzamento solo con CTA d’azione / artefatto.
- File `(1)/(2)` non accettati nel happy path.
- Catalogo attivato a fine procedura.

## Riscontro
| # | OK/KO | Nota |
|---|-------|------|
| 1 PDF stampa | | |
| 2 Porte/CTA | | |
| 3 Ascolto/import | | |
| 4 Overwrite PC | | |
| 5 Attiva Catalogo | | |
