# Annotazioni Renato su bozza pagine (testo PPT V2)

**Passo 1:** eliminare l’asterisco dopo «superiore».

**Passo 2 (REVISIONE3):** ordine Sbloccati → Collega → USB + 4 bullet.

**Passo 3:** pagina separata (copia CONFIGURA/Sup).  
- Sopra i bottoni: «Quando pronto clicca su OK».  
- **Non** mostrare sotto i bottoni lo status «File CONFIGURA e sup salvati…».

**Passo 4:** checklist CONFIGURA / UAC / Tutto pronto (finestra resta aperta; niente copia JSON sul telefono).

**Passo 5 (revisione4 + CIFS 0.16.46):**  
- **User/Host:** memorizzati a «Tutto pronto» sul PC e inviati in Wi‑Fi all’app (form già compilato).  
- **Visibilità share:** Folder vuoto → elenco share; aprire `MediaManagerCatalog`.  
- Niente passo «copia file esito» per il cliente.

**KO 0.16.45 (Renato 23/10):** finestra chiusa; copia JSON inutile/complicata; CIFS non testabile. Superato da 0.16.46.

**P0 Acquisisci (Renato 22/10):** su S25 solo telefono/Drive/CIFS — manca censimento altri dispositivi/fonti. Vedi `docs/BACKLOG_WIZARD_UX.md`.

**KO 0.16.46 (Renato 23/10):** UDP non ha riempito Host/User; messaggio PC ancora citava JSON (`MediaManager_setup_result.json` — file tecnico interno, non per il cliente).  
**0.16.47:** TCP+UDP dal PC, scansione HTTP in app già dal Passo 4; messaggi Tutto pronto senza path JSON/log.

**Stato:** `0.16.47-preview`
