# Prompt di continuità — Wizard sede Catalogo (W2 PC + CIFS)

**Data:** 17/10/2026  
**Uso:** incolla questo documento come **primo messaggio** in una **nuova** sessione Cursor (un solo agente).  
**Allegati da fornire nella nuova sessione (non obbligatori in questa chat):**  
1. proposta di revisione del workflow Wizard (ChatGPT / Renato)  
2. rappresentazione grafica del flusso  

---

## Ruolo

Sei l’agente unico su **Photo&VideoManager** (UI: **MediaManager**).  
**Priorità ora:** esaminare la proposta di revisione del Wizard e mettere ordine al workflow.  
**Non sviluppare codice** finché Renato non convalida la proposta (o parti esplicite di essa).

---

## 1. Dove siamo (tecnico)

| Voce | Valore |
|---|---|
| Repo | `renatizzi/PhotoVideoManager` |
| Branch lavoro W2 | `cursor/w2-routine-pc-windows-c499` (base tipica: `cursor/bl17-catalogo-h1-c499` / H1) |
| Ultima build nota | **0.16.26-preview** (`versionCode` 67) |
| Esito test Renato | **Filiera OK** (script PC → CIFS → sede Catalogo) |

### Cosa funziona già (non rimettere in discussione senza motivo)
- Script Windows idempotente (account `MediaManager`, share, NTFS, UAC, ASCII+.ps1 con BOM, .cmd senza BOM/CRLF).
- Trasferimento file via USB/MTP (Download sul telefono → Desktop PC).
- Stesso modello **BoxManager**: CIFS Documents Provider (`com.wa2c.android.cifsdocumentsprovider`) + SAF `OpenDocumentTree` (niente SMB nativo in-app).
- Codice rilevante: `NetworkDriveAssistant`, `WindowsCatalogSetupScript`, `CatalogSeatWizard*`, `docs/PVM_BoxManager_CIFS_Analisi.md`.

### Feedback Renato (17/10 — motivo della pausa sviluppo)
1. Guida passo-passo della proposta **non ancora recepita** nel Wizard.  
2. Controllo del processo ancora **troppo frammentato** (troppi micro-passi / schermate).  
3. Credenziali salvate ma **non è chiaro come usarle**.  
4. Riepilogo credenziali nei prompt **poco fruibile**.  
5. Password temporanea **troppo complessa** / difficile da trascrivere.

**Decisione:** stop sviluppo feature; prima **riordinare** questi aspetti con una proposta condivisa.

---

## 2. Cosa fare in questa nuova sessione

### All’avvio
1. Fare checkout / usare il branch `cursor/w2-routine-pc-windows-c499` (o quello indicato da Renato se aggiornato).  
2. Leggere: questo prompt; `docs/PVM_BoxManager_CIFS_Analisi.md`; parti Wizard in `docs/PVM_Nota_Integrata.md` / interlocuzione §7.26–§7.27 se presenti.  
3. **Leggere gli allegati** (proposta + grafico) prima di qualsiasi modifica codice.  
4. Dare **feedback** sulla proposta (fattibilità, riuso codice, punti da non toccare, ordine di intervento).  
5. Solo dopo OK Renato: implementare le modifiche di UX/workflow.

### Non fare
- Non ripartire da `main` / `bl03` senza istruzione.  
- Non reintrodurre SMB nativo.  
- Non “migliorare” lo script PC se la proposta riguarda solo il Wizard, salvo richiesta esplicita.  
- Non inventare requisiti oltre proposta + feedback Renato.

---

## 3. Messaggio tipo da incollare a Cursor (nuova chat)

```
Continua Photo&VideoManager dal prompt docs/PROMPT_CONTINUITA_WIZARD_W2.md
Branch: cursor/w2-routine-pc-windows-c499 — build 0.16.26.
Filiera PC+CIFS OK in test; sviluppo in pausa.
Allegati: proposta revisione Wizard + rappresentazione grafica.
Compito: esamina la proposta, dai feedback; non sviluppare fino a mia convalida.
```

Poi allega i due documenti.

---

## 4. Contesto prodotto (una riga)

Catalogo personale unico (H1): sede scelta col Wizard; su PC la cartella condivisa si rende visibile al tablet con **CIFS** (come BoxManager), poi MediaManager riprende Acquisisci / Aggiorna Catalogo.

Fine prompt — Wizard W2 continuità.
