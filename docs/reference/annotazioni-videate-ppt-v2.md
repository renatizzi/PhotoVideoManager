# Annotazioni Renato su bozza pagine (testo PPT V2)

**Passo 1:** eliminare l’asterisco dopo «superiore».

**Passo 2 (REVISIONE3):** ordine Sbloccati → Collega → USB + 4 bullet.

**Passo 3:** pagina separata (copia CONFIGURA/Sup).  
- Sopra i bottoni: «Quando pronto clicca su OK».  
- **Non** mostrare sotto i bottoni lo status «File CONFIGURA e sup salvati…».

**Passo 4:** checklist CONFIGURA / UAC / Tutto pronto.

**Passo 5 (revisione4 + annotazione 22/10 + fix CIFS 0.16.44):**  
- Eliminati: «ricontrolla», bullet «Clicca qui» / «Consenti…», status sotto Indietro.  
- Unico blocco + bottone **Attiva collegamento** (ex Scegli cartella).  
- SI → Acquisisci; NO → stub. Routine/IP/attesa differiti.  
- **User CIFS:** Copia in formato `NOMEPC\MediaManager` (come «Utente SMB» in Tutto pronto; sostituire NOMEPC se ancora segnaposto).  
- **Folder CIFS:** lasciare vuoto + Conferma (non il path Windows).  
- **Attiva collegamento:** aprire la share `MediaManagerCatalog`, poi «Usa questa cartella».

**Disco esterno PC:** percorso Windows su qualsiasi lettera (non solo C:); default resta `C:\MediaManager\Catalogo`.

**Da verificare (test / follow-up):** stesso disco esterno configurato su un PC (stessa lettera + stesso percorso) collegato poi a un **altro** PC — esito SMB/share/CIFS/MediaManager (forse nessun problema; va confermato sul campo).

**Test filiera 0.16.43 — KO poi OK manuale (Renato 22/10):** con User `MINI_IT13\MediaManager` + Folder vuoto + aprire `MediaManagerCatalog` → **Stato: verificato**. Fix app in `0.16.44-preview` (Copia User/Folder + testo Attiva collegamento).

**Stato:** `0.16.44-preview` (fix CIFS; da riconvalidare sul campo con Copia senza correzioni manuali)
