# Guida test 0.16.61 — Fix Tab S9 (16 file / dettaglio / Ripristina)

Versione: `0.16.61-preview`  
Ambito: solo i bug della diagnosi `docs/NOTA_TER_DIAGNOSI_TAB_S9.md` (nessuna filiera).

## Obiettivo

Verificare che con 4 cartelle collegate (2 root + 2 sottocartelle) e 11 file (5 foto + 6 video) non si torni a 16 elementi, e che dettaglio / Ripristina rispettino lo standard Catalogo.

## Preparazione

1. Installare `0.16.61-preview` su Tab S9.
2. Catalogo vuoto (o ripartire da zero sulle fonti di test).
3. Struttura come nel test del 3 nov:
   - Main: `Test1` (3 video) + `Duplicati` (1 foto + 2 video)
   - SD: `Test2` (2 foto + 1 video) + `Duplicati` (2 foto)
   - Totale: 4 cartelle, 11 file; 5 duplicati da eliminare; dopo pulizia 6 in Catalogo.

## Procedura

1. **Acquisisci** → collega le 4 cartelle (root con media + sottocartelle Duplicati).
2. Apri **Dettaglio** su `Test2` (prima di Conferma):
   - conteggio atteso: **3** (2 foto + 1 video della root; non i file di `Duplicati` già collegata);
   - ogni riga: **miniatura** + nome + tipo (standard Catalogo).
3. **Conferma** → **Importa**: riepilogo **Originali 11** (non 16); foto 5 · video 6.
4. Importa → **Pulisci** i 5 duplicati → in Catalogo restano **6** (2 foto + 4 video).
5. Da Pulisci premi **Indietro**: Aggiorna mostra **6** (non ancora 16).
6. Torna ad **Acquisisci**: 6 elementi già in Catalogo; **4** cartelle collegate (niente duplicati di riga).
7. Controlla `pietro.mov`: trattata come **video** con **miniatura** (frame), non placeholder generico.
8. **Utility → Ripristina → Cestino** (e sezione esclusi in Importa se presente):
   - riga = riquadro ✓ + thumb + nome + meta;
   - un solo bottone **[Ripristina]** (selezione + azione), niente Elimina per riga.

## Risultato atteso

Allineato alla matrice: 4 cartelle · 11 in Importa · 5 nel cestino · 6 dopo pulizia; UI dettaglio/Ripristina standard.

## Segnali di errore

- Importa o Conferma con **16** (o N ≠ 11).
- Indietro da Pulisci ancora a 16.
- Dettaglio senza miniature / solo testo.
- `pietro.mov` senza frame.
- Ripristina con più bottoni per riga o senza riquadro/thumb.

## Riscontro da inviare

Screenshot di: Acquisisci (4 cartelle), Dettaglio Test2, Importa (riepilogo 11), Aggiorna dopo Indietro da Pulisci (6), una riga `pietro.mov`, Cestino/esclusi.
