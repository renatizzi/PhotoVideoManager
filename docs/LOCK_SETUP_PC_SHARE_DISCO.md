# LOCK — Setup PC: share = radice disco

**Stato:** approvato (Renato 25/11/2026 — «sceglila tu», stesso titolare dispositivi)  
**Branch:** `cursor/setup-usb-rev4-7fasi-b602`

| # | Decisione | Risultato atteso |
|---|-----------|------------------|
| 1 | Cartella Catalogo | Resta `X:\NomeCatalogo` (+ `MediaManager` hub) |
| 2 | Condivisione SMB | Punta a **`X:\`** (radice disco Avvio), nome share = **NomeCatalogo** |
| 3 | CIFS | Una connessione = disco intero; Attiva su `Nome\MediaManager`; Acquisisci **+** su qualsiasi cartella del disco |
| 4 | Niente seconda procedura CIFS per cartella foto | «No, va creata» in Acquisisci non chiede di compilare Host/User: guida a Setup (se manca) o a Scegli cartella |
| 5 | Foto | Restano dove sono (raccolta logica); non obbligatorio spostarle sotto Catalogo |

## Fuori scope
- Share di un secondo disco senza nuovo Setup
- Account Windows diverso dal dedicato NomeCatalogo
