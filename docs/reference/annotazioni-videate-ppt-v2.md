# Annotazioni Renato su bozza pagine (testo PPT V2)

**Passo 1:** eliminare l’asterisco dopo «superiore».

**Passo 2 (REVISIONE3):** ordine Sbloccati → Collega → USB + 4 bullet.

**Passo 3:** pagina separata (copia CONFIGURA/Sup).  
- Sopra i bottoni: «Quando pronto clicca su OK».  
- **Non** mostrare sotto i bottoni lo status «File CONFIGURA e sup salvati…».

**Passo 4:** checklist CONFIGURA / UAC / Tutto pronto.

**Passo 5 (revisione4 + annotazione 22/10):**  
- Eliminati: «ricontrolla», bullet «Clicca qui» / «Consenti…», status sotto Indietro.  
- Unico blocco + bottone **Attiva collegamento** (ex Scegli cartella).  
- SI → Acquisisci; NO → stub. Routine/IP/attesa differiti.

**Disco esterno PC:** percorso Windows su qualsiasi lettera (non solo C:); default resta `C:\MediaManager\Catalogo`.

**Da verificare (test / follow-up):** stesso disco esterno configurato su un PC (stessa lettera + stesso percorso) collegato poi a un **altro** PC — esito SMB/share/CIFS/MediaManager (forse nessun problema; va confermato sul campo).

**Test filiera 0.16.43 — KO (Renato 22/10):** Dell + MiniPC Geekom, stesso esito. Cartella creata ok su SSD esterno (`G:`). Accesso CIFS / File Manager+ «Impossibile accedere»; MediaManager «Stato: non raggiungibile».  
**Incongruenza credenziali:** script mostra `Utente SMB: MINI_IT13\MediaManager`; Copia in app mette solo `MediaManager`. Ipotesi: formato User/Domain per jCIFS (come in passato). Approfondimento richiesto prima di fix.

**Stato:** `0.16.43-preview` (filiera PC non consolidata finché KO non risolto)
