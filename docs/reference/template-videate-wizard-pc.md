# Template videate v2 — Setup Catalogo · ramo PC

**Stato:** revisione UIX (nessun codice).  
**Risposta alle raccomandazioni Renato 18/10.**  
**Titolo fisso ogni schermata:** `Setup Catalogo personale`  
**Sottotitolo:** `Passo N di M — <descrizione>`  

**Termine scelto al posto di «Sede»:** **Posizione del Catalogo**  
(Alternativa scartata: «posto» troppo generico; «Dispositivo/cartella» lungo in UI. «Posizione del Catalogo» resta chiaro in titoli e riepiloghi.)

**Pattern di interlocuzione:** ogni schermata ha **una domanda chiusa** (o un checkpoint a scelte). Le istruzioni fuori-app sono sotto la domanda, più dettagliate. Niente «Mostra aiuto» separato: si usa **Non riesco** / risposte «No…» che aprono il **checkpoint** (gruppi di problemi + soluzione).

**Dichiarazioni «Ho fatto X»:** la Nota impone che i passi manuali siano chiari, ma **non** prevede un modo verificabile dall’app per USB/UAC/script (no deep-link, no import CIFS, no lettura automatica del JSON/QR in questa fase). Quindi:
- **non** usiamo bottoni del tipo «Ho copiato i file»;
- usiamo **domande chiuse su ciò che l’utente vede** (proxy osservabile);
- avanzamento solo se la risposta indica esito utile; altrimenti checkpoint.

**Riepiloghi dati:** solo dove servono (CIFS; esito finale). Non in B1.

---

## Sequenza PC

```
E0 → S0 → B1 → B2 → [CK-USB] → B3a → B3b → B3c → [CK-SCRIPT]
   → B4 → B5a → B5b → [CK-CIFS] → B6 → [CK-SAF] → F
```

`CK-*` = checkpoint (solo se risposta negativa / Non riesco).

---

## Chrome comune

| Elemento | Contenuto |
|---|---|
| Titolo | Setup Catalogo personale |
| Sottotitolo | Passo N di M — … |
| Corpo | Domanda (evidenziata) → risposte chiuse → istruzioni (se serve) |
| Primario | La risposta affermativa utile / Avanti contestuale |
| Secondario | Altre risposte chiuse |
| Non riesco | Apre checkpoint del passo (mai un generico «mostra aiuto») |
| Indietro | Sempre |

---

## E0 — Ingresso

**Sottotitolo:** Inizio  
**Domanda:** Cosa vuoi fare con la Posizione del Catalogo?  
**Risposte:**
- Riprendere il percorso interrotto *(solo se bozza)*  
- Creare una nuova Posizione del Catalogo  
- Modificare la Posizione del Catalogo attuale *(solo se già configurata)*  
- Annulla  

**Prevenzione errori:** sede precedente resta attiva fino a verifica OK della nuova (Nota §7) — messaggio informativo sotto Modifica: «La Posizione attuale resta valida finché la nuova non è verificata.»

---

## S0 — Destinazione

**Sottotitolo:** Passo 1 di … — Dove tenere il Catalogo  
**Domanda:** Dove vuoi tenere il Catalogo?  
**Risposte (un tocco = avanza):**
- Su questo telefono o tablet — *Nessuna rete necessaria*  
- Su un PC o notebook — *Deve essere acceso; altrimenti userai il cavo USB*  
- Su Google Drive (o cloud) — *come oggi (stub)*  
- Su un disco di rete o NAS — *come oggi (stub)*  

---

## B1 — Prepara file

**Sottotitolo:** Passo 2 di 10 — Prepara i file per il PC  
**Domanda:** Il PC è acceso, sbloccato e non in sospensione?  
**Risposte:**
- Sì, prepara i file → genera script in Download, vai a B2  
- No → messaggio Nota 5.6 («Riattivalo e riprova») senza avanzare  
- Il PC ha già la cartella condivisa → ramo manuale leggero (come oggi secondario)  

**Istruzioni (brevi):** L’app prepara due file in Download sul telefono. Non serve scegliere cartella o nome condivisione: usa i valori già previsti.  
**▸ Modifica dettagli** (opzionale, collassato): path / share — default `C:\MediaManager\Catalogo`, `MediaManagerCatalog`.  
**Niente riepilogo dati a schermo pieno.**

**Prevenzione:** se export fallisce → Non riesco / Riprova (non avanzare a B2).

---

## B2 — Copia USB (domanda chiusa, non «Ho copiato»)

**Sottotitolo:** Passo 3 di 10 — Copia i file sul Desktop del PC  

**Istruzioni dettagliate (Nota 5.3 + nomi attuali):**
1. Usa un cavo USB che trasferisce file (non solo ricarica).  
2. Collega telefono e PC; entrambi accesi e sbloccati.  
3. Sul telefono apri la notifica USB e scegli **Trasferimento file** / MTP / File — non «Solo ricarica». Se chiede accesso ai dati, consenti.  
4. Sul PC: Esplora file → Questo PC → telefono → memoria interna → **Download**.  
5. Seleziona **entrambi** i file e copiali sul **Desktop** (stessa cartella):  
   - `AVVIA_Configurazione_Catalogo` ← quello da avviare dopo  
   - `_non_aprire_supporto_setup` ← non aprirlo  
6. Controlla che siano entrambi sul Desktop prima di continuare.

**Domanda:** Sul Desktop del PC vedi entrambi i file?  
**Risposte:**
- Sì, entrambi → B3a  
- Ne vedo solo uno → CK-USB (gruppo «File incompleti»)  
- Non vedo il telefono / la cartella Download → CK-USB  
- Non riesco → CK-USB  

---

## CK-USB — Checkpoint copia

**Sottotitolo:** Problema in copia file  
**Domanda:** Quale situazione descrive meglio il problema?  

| Gruppo | Soluzione proposta |
|---|---|
| Il telefono non compare sul PC | Controlla Trasferimento file (non Solo ricarica); sblocca; ricollega; altra porta/cavo dati; attendi driver |
| Compare ma non trovo Download | Memoria interna / File condivisi → Download; non usare «Solo ricarica» |
| Manca uno dei due file | Torna in Download sul telefono; se manca, Indietro e «Prepara i file» di nuovo |
| Non ho un cavo dati | **Quick Share** (solo qui): Wi‑Fi+Bluetooth, invia i due file, spostali insieme sul Desktop |
| Altro | Ripeti i passi 1–6 di B2 |

**Azioni:** Ho risolto — ricontrolla (torna a B2 con la stessa domanda) · Indietro  

---

## B3a — Script: trova il file

**Sottotitolo:** Passo 4 di 10 — Avvia lo script sul PC (1/3)  

**Istruzioni:** Sul Desktop del PC cerca il file `AVVIA_Configurazione_Catalogo`. L’altro file deve restare accanto; non aprirlo.  

**Domanda:** Hai trovato AVVIA_Configurazione_Catalogo sul Desktop?  
**Risposte:** Sì → B3b · No → CK-USB/CK-SCRIPT gruppo file · Non riesco → CK-SCRIPT  

---

## B3b — Script: doppio clic e UAC

**Sottotitolo:** Passo 5 di 10 — Avvia lo script sul PC (2/3)  

**Istruzioni:**
1. Doppio clic su `AVVIA_Configurazione_Catalogo`.  
2. Se compare SmartScreen: **Ulteriori informazioni** → **Esegui comunque**.  
3. Se compare Controllo account utente: conferma **Sì**.  
4. Si apre una finestra nera/blu «MediaManager - configurazione Catalogo».  

**Avviso:** Da qui possono passare **alcuni minuti** senza nuovi messaggi. Non chiudere la finestra.  

**Domanda:** Cosa è successo dopo il doppio clic?  
**Risposte:**
- È partita la finestra dello script → B3c  
- SmartScreen / UAC e non so cosa fare → CK-SCRIPT (gruppo Windows)  
- Non parte nulla → CK-SCRIPT  
- Non riesco → CK-SCRIPT  

---

## B3c — Script: esito

**Sottotitolo:** Passo 6 di 10 — Avvia lo script sul PC (3/3)  

**Istruzioni:** Guarda l’ultima riga della finestra dello script. Serve il messaggio **Tutto pronto**. Se vedi un errore o un codice, non continuare: usa Non riesco.  

**Domanda:** Cosa mostra ora la finestra dello script?  
**Risposte:**
- **Tutto pronto** → B4 (pulizia Download lato app come oggi)  
- Ancora in lavorazione / passi in corso → resta qui + ripeti l’avviso attesa; «Ricontrolla»  
- Messaggio di errore / Configurazione NON completata → CK-SCRIPT  
- Ho chiuso la finestra per sbaglio → CK-SCRIPT (rilancia AVVIA_…)  
- Non riesco → CK-SCRIPT  

---

## CK-SCRIPT — Checkpoint script

**Domanda:** Quale situazione descrive meglio il problema?  

| Gruppo | Soluzione |
|---|---|
| SmartScreen / Windows ha bloccato | Ulteriori informazioni → Esegui comunque; poi Sì a UAC |
| Errore nello script / codice 10–60 | Leggi il messaggio; correggi (cartella, utente, share, NTFS, rete); rilancia AVVIA_… (script rilanciabile) |
| PC in sospensione / rete | Riattiva il PC; stessa Wi‑Fi; rilancia |
| Finestra chiusa a metà | Rilancia AVVIA_… dal Desktop (i passi già fatti vengono riusati) |
| Non trovo più i file | Torna a B2 / Prepara di nuovo |

---

## B4 — Ponte verso CIFS (senza password, senza riepilogo pesante)

**Sottotitolo:** Passo 7 di 10 — Collegamento dal telefono  

**Domanda:** Sul telefono hai già l’app **CIFS Documents Provider**?  
**Risposte:**
- Sì → B5a  
- No → Installa (store) poi torna qui; ripresa su B4  
- Non riesco → CK-CIFS (gruppo provider)  

**Testo breve:** Ora colleghiamo il telefono alla cartella del PC. La password la vedi solo al passo successivo, quando la digiti in CIFS.  
**Niente scheda dati completa qui.**

---

## B5a — CIFS: come inserire i dati

**Sottotitolo:** Passo 8 di 10 — Configura CIFS (1/2)  

**Domanda:** In CIFS, come vuoi creare la connessione?  
**Risposte:**
- **Inserimento manuale (consigliato)** → B5b  
- Provare la scansione automatica → avviso: *spesso la condivisione MediaManager non compare nell’elenco trovato; se non la vedi, usa inserimento manuale* → poi B5b  
- Non riesco → CK-CIFS  

**Perché manuale:** in prova sul dispositivo l’URL/condivisione necessaria **può non apparire** nella lista della scansione; l’inserimento manuale è il percorso affidabile (allineato a Nota: niente scansione 445 descritta come già disponibile).

---

## B5b — CIFS: scheda nel formato provider + password

**Sottotitolo:** Passo 8 di 10 — Configura CIFS (2/2)  

**Istruzioni:**
1. Tocca **Apri CIFS Documents Provider**.  
2. Tocca **+** / Aggiungi connessione.  
3. Compila i campi **esattamente** come sotto (stesse etichette del form CIFS). Usa **Copia** sul singolo campo se ti aiuta a incollare.  
4. Tocca Verifica (in CIFS), poi Salva.  
5. Torna a MediaManager con il tasto **Indietro** del sistema (il ritorno **non è automatico**).  

### Mock form CIFS (precompilato — da riflettere in UI come scheda)

```
┌─ Nuova connessione (CIFS Documents Provider) ─┐
│ Nome / Connection name   MediaManagerCatalog  │  [Copia]
│ Host                     <IP-del-PC>          │  [Copia]
│ Port                     445                  │  [Copia]
│ Domain / Workgroup       (lascia vuoto)       │
│ Folder / Shared folder   MediaManagerCatalog  │  [Copia]
│ Username                 MediaManager         │  [Copia]
│ Password                 ••••••••••••  [Mostra][Copia]
│ Storage / SMB            SMB2,3 (jCIFS NG)    │
└───────────────────────────────────────────────┘
```

**Password (Nota 5.5):**
- Visibile **solo in questo passo**, con Mostra/Nascondi.  
- Copia password consentita qui (azione manuale in CIFS).  
- Mai in riepilogo finale / log / preferenze.  

**Host:** se l’IP non è noto, testo guida: «Sul PC, nella finestra Tutto pronto, usa l’IP indicato; oppure Impostazioni Windows → rete.»

**Domanda:** Hai salvato la connessione in CIFS e sei tornato qui?  
**Risposte:**
- Sì → B6  
- La verifica in CIFS è fallita → CK-CIFS  
- Non riesco → CK-CIFS  

---

## CK-CIFS — Checkpoint collegamento

**Domanda:** Quale situazione descrive meglio il problema?  

| Gruppo | Soluzione |
|---|---|
| CIFS non installato | Installa CIFS Documents Provider; torna al passo 7 |
| Scansione senza risultati / URL assente | Usa **inserimento manuale** con Host=IP, Folder=MediaManagerCatalog, Porta=445 |
| Autenticazione rifiutata | Utente `MediaManager` (non Microsoft/Gmail); ricopia password; PC acceso |
| Host non raggiungibile | Stessa Wi‑Fi; PC non in sospensione; controlla IP dello script |
| Connessione non salvata / non visibile | In CIFS: Salva; riprova Apri CIFS |
| Verifica CIFS ok ma poi non vedo cartelle | Controlla Folder=MediaManagerCatalog; Storage SMB2,3 |

---

## B6 — Scelta cartella (SAF)

**Sottotitolo:** Passo 9 di 10 — Scegli la cartella  

**Istruzioni:** Tocca Scegli cartella → nel selettore apri **CIFS Documents Provider** (o Tutte le cartelle → CIFS) → apri la connessione appena salvata → seleziona la cartella del Catalogo → **Usa questa cartella** → Consenti.  

**Domanda:** Hai concesso l’accesso alla cartella?  
*(In pratica l’app avanza con il risultato SAF: successo → verifica scrittura; annulla → resta qui.)*  

**Se verifica scrittura fallisce → CK-SAF** (non salvare Posizione come valida).

---

## CK-SAF — Checkpoint verifica

| Gruppo | Soluzione |
|---|---|
| Consenso negato / selettore chiuso | Ripeti Scegli cartella e Consenti |
| Cartella sbagliata (non CIFS) | Seleziona la connessione CIFS appena creata |
| Non scrivibile / Accesso negato | PC acceso; permessi; Torna a CK-CIFS / rilancia script se serve |
| Non raggiungibile | PC non in sospensione; stessa rete |

---

## F — Esito

**Sottotitolo:** Passo 10 di 10 — Configurazione completata  

**Solo se verifica ok.**  
**Riepilogo utile (senza password):** Posizione del Catalogo = PC · cartella scelta · condivisione · utente.  
**Reminder:** password provvisoria da cambiare sul PC.  
**Domanda/azione:** Torna a Configura / Torna ad Acquisisci (in base all’origine).

---

## Mappa rispetto ai template v1

| v1 | v2 |
|---|---|
| Titoli vari | Titolo fisso + sottotitolo passo |
| «Ho copiato / Ho visto Tutto pronto» | Domande chiuse su ciò che si vede |
| «Mostra aiuto» | Solo Non riesco / No → checkpoint |
| Riepilogo ovunque | Solo CIFS + esito |
| Script in 1 schermata | B3a/b/c passo-passo + attesa |
| CIFS generico | Form etichette CIFS + manuale vs scan |
| Sede | Posizione del Catalogo |

---

## Limiti onesti (da Nota, non simulare)

- Nessuna verifica automatica della copia USB o dell’UAC.  
- Nessuna lettura automatica di `MediaManager_setup_result.json` / QR in questa revisione.  
- Nessun ritorno automatico da CIFS.  
- Quick Share solo nel checkpoint USB.
