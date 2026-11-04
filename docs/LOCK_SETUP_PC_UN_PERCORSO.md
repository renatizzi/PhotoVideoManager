# LOCK — Setup Catalogo PC (un percorso)

**Stato:** in attesa OK Renato (dopo convalida workflow 04/11)  
**Data:** 2026-11-04  
**Richiesta / test di riferimento:** chat 04/11 — workflow convalidato; account locale ad hoc; cartella fissa MyMedia  
**Approvazione Renato:** in attesa su questa tabella  
**Workflow:** `docs/WORKFLOW_SETUP_PC_UN_PERCORSO.md`

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Doppio binario confonde / si blocca comunque | Nessuna scelta RAPIDA/GUIDATA. Un solo percorso lineare S0→M1→…→F. Help solo sul passo corrente | Secondo wizard parallelo |
| 2 | Checklist senza prova inutili | Solo macro-passi; OK gated da checkpoint (file in Download; Host/User; pick+verify sede) | Spunte campo-per-campo CIFS senza prova |
| 3 | Difficoltà credenziali (quale account/username) | Script crea **account locale dedicato fisso** `MyMedia`; CIFS User = quel account (es. `NOMEPC\MyMedia`); Password solo Copia dall’app; niente Microsoft/PIN | Chiedere all’utente di inventare account o usare Gmail |
| 4 | Semplificare path Catalogo | Cartella **non editabile**: `C:\MyMedia`; share **MyMedia**. In S0 niente campo percorso | Path libero / scelta lettera disco in v1 |
| 5 | Catalogo prima, sorgenti dopo | Questo LOCK = solo **sede Catalogo** su PC. Nessun setup sorgenti in questo filo | Acquisisci + multi-dispositivo (tranche dopo) |
| 6 | CIFS resta il ponte | Passo M4/M5: CIFS Documents Provider + Attiva collegamento + riga `MyMedia` + verifica R/W | File Explorer+; SMB1 |

## Note operative (agente)

- Prima del codice: se Stato ≠ `approvato`, chiedere OK su questa tabella.
- FIX: solo righe `#N`; preferire commit `fix(LOCK-N): …`.
- Allineare default script/app da `MediaManager` / `MediaManagerCatalog` / `C:\MediaManager\Catalogo` → utente/share/cartella **`MyMedia`** / **`C:\MyMedia`**.
- PROVA: guida test con gli stessi `#N`.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
