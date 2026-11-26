# Guida test 0.16.98 — ordine CIFS + Salva sbloccato

Build: `0.16.98-preview` (versionCode 139).

## Fix

1. **Ordine campi** allineato a CIFS Connection: Name → Storage → **Domain** (vuoto) → Host → Port → **Enable DFS** (deselezionato) → User → Password → Folder.
2. **Salva / ANR**: verifica scrittura su `Dispatchers.IO` (niente blocco UI).
3. **Gate sede**: con share = disco, la cartella Catalogo omonima tra i figli **non** è più rifiutata; si rifiuta solo l’elenco share (ADMIN$/IPC$).
4. **Attiva Catalogo**: dopo Salva fallito torna abilitato per un nuovo tentativo (pending azzerato).

## Cosa fare

1. Installa APK; riprendi Setup alla Fase CIFS.
2. Verifica ordine Domain / Enable DFS come in CIFS.
3. Attiva Catalogo → scegli la connessione `MyTEST` (o `MyTEST` / `MediaManager`) → Salva.
4. Se fallisce: Attiva Catalogo di nuovo (non resta grigio); niente ANR.

## Simulazioni agente

- `sim_pc_seat_gate.py` — drive root con figlio MyTEST accettato; ADMIN$ rifiutato
- Unit test `PcCatalogSeatGateTest` + compile
