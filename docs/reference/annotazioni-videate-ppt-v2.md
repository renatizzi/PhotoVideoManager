# Annotazioni Renato su bozza pagine (testo PPT V2)

**Passo 1:** eliminare l’asterisco dopo «superiore».

**Passo 2 (REVISIONE3):** ordine Sbloccati → Collega → USB + 4 bullet.

**Passo 3:** pagina separata (copia CONFIGURA/Sup).  
- Sopra i bottoni: «Quando pronto clicca su OK».  
- **Non** mostrare sotto i bottoni lo status «File CONFIGURA e sup salvati…».

**Passo 4:** checklist CONFIGURA / UAC / Tutto pronto / copia `MediaManager_setup_result.json` in Download.

**Passo 5 (revisione4 + CIFS 0.16.44/45):**  
- Eliminati: «ricontrolla», bullet «Clicca qui» / «Consenti…», status sotto Indietro.  
- Unico blocco + bottone **Attiva collegamento** (ex Scegli cartella).  
- SI → Acquisisci; NO → stub.  
- **Visibilità share:** Folder vuoto → elenco share; aprire `MediaManagerCatalog`.  
- **User/Host:** da import esito JSON in Download (fallback `NOMEPC\…` + IP da Tutto pronto).  
- Script: messaggio attesa + cleanup CONFIGURA dal Desktop.

**Disco esterno PC:** percorso Windows su qualsiasi lettera (non solo C:); default resta `C:\MediaManager\Catalogo`.

**Da verificare (test / follow-up):** stesso disco esterno configurato su un PC (stessa lettera + stesso percorso) collegato poi a un **altro** PC — esito SMB/share/CIFS/MediaManager (forse nessun problema; va confermato sul campo).

**P0 Acquisisci (Renato 22/10):** su S25 solo telefono/Drive/CIFS — manca censimento altri dispositivi/fonti. Vedi `docs/BACKLOG_WIZARD_UX.md`.

**Test filiera 0.16.43 — KO poi OK manuale (Renato 22/10):** User `MINI_IT13\MediaManager` + Folder vuoto + `MediaManagerCatalog` → verificato. Fix app 0.16.44/45.

**Stato:** `0.16.45-preview` (visibilità share + import esito; da riconvalidare sul campo)
