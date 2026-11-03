# Regole vincolanti di collaborazione e sviluppo — MediaManager

**Stato:** vincolanti e permanenti  
**Data adozione:** 23/10/2026 (aggiornamento §11 Protocollo LOCK: 3/11/2026)  
**Applicazione:** tutte le sessioni presenti e future (Cursor Cloud / Composer / agenti).  
**Riferimento di avvio sessione:** anche `AGENTS.md` (radice del repository).

Queste disposizioni riducono ripetizioni, interpretazioni errate e consumo di lavoro inutile. Hanno carattere vincolante e vanno **applicate**, non solo dichiarate comprese.

---

## 1. Scopo e validità

Valgono per ogni attività sul progetto MediaManager (Photo&VideoManager).  
Obiettivo: sviluppo ordinato, coerente con requisiti e piano condiviso.

## 2. Comunicazione semplice e comprensibile

- Linguaggio semplice, diretto, comprensibile a chi non sviluppa.
- Evitare tecnicismi e dettagli di implementazione non necessari; se un termine tecnico è indispensabile, spiegarlo brevemente.
- Istruzioni operative: **cosa** fare, **dove**, **quale risultato** osservare.
- Non dare per scontato procedure o strumenti non spiegati.

## 3. Divieto di ricicli e chiarimenti preventivi

Vietato avviare implementazioni su interpretazioni incerte, incomplete o discordanti.

Prima di procedere:

1. Individuare il risultato concreto atteso dalla richiesta corrente.
2. Verificare coerenza con requisiti approvati, documentazione e decisioni consolidate.
3. Individuare ambiguità, contraddizioni, dipendenze o conseguenze non previste.
4. Se un dubbio può influire sul risultato: **fermarsi e chiedere chiarimento** prima di modificare il codice.

Non:

- riproporre analisi sullo stesso problema quando gli elementi bastano;
- ripetere verifiche già fatte senza motivo concreto;
- riaprire decisioni già approvate;
- trasformare una richiesta circoscritta in revisione generale;
- introdurre miglioramenti facoltativi o cambi di architettura non richiesti;
- chiedere di ripetere informazioni già in documentazione o conversazione.

Se la richiesta è chiara e coerente: procedere senza conferme superflue.

## 4. Controllo preliminare obbligatorio prima di ogni implementazione

Verifica interna (non una lunga conversazione) su: richiesta e risultato atteso; requisiti e decisioni; piano e posizione dell’attività; documentazione ufficiale e piattaforme/librerie; codice esistente e ciò che deve restare invariato; istruzioni contingenti; rischi di regressione.

Evidenziare a Renato solo i punti che richiedono una sua decisione. Altrimenti procedere.  
A fine implementazione: verifica in ambiente di sviluppo (coerenza requisiti, no regressioni ragionevoli).

## 5. Sviluppo unitario — senza frammentare i test sul telefono

- Completare le attività del piano prima di chiedere verifiche manuali sul telefono.
- Evitare richieste frequenti di installazione/test intermedi.
- Usare al massimo controlli interni: codice, compilazione, test automatici.
- Accorpare i test manuali in **una sessione finale** per il gruppo di attività, salvo blocco tecnico reale.
- Test anticipato sul telefono solo se indispensabile; spiegare perché e limitare la prova al minimo.

## 6. Istruzioni obbligatorie per ogni test sul telefono

Ogni richiesta di test manuale deve contenere:

1. Obiettivo  
2. Preparazione  
3. Procedura numerata  
4. Risultato atteso  
5. Segnali di errore  
6. Riscontro da inviare  

Vietate istruzioni vaghe («prova il wizard», «verifica che funzioni»).  
In caso di fallimento: chiedere solo il riscontro necessario, senza chiedere a Renato di diagnosticare.

## 7. Prevenzione delle deviazioni dal piano

All’inizio di un’attività significativa: riepilogo breve (risultato, modifiche previste, cosa non alterare, verifiche interne, eventuale test finale).  
Se chiaro e senza conflitti: procedere senza approvazione aggiuntiva, salvo richiesta esplicita di fermarsi.  
Con contraddizione o requisito mancante: non implementare la parte interessata prima del chiarimento.  
Non modificare funzioni o interfacce estranee alla richiesta.

## 8. Continuità nelle sessioni future

- Consultare questo documento e `AGENTS.md` all’avvio.
- Verificare stato reale e decisioni già prese.
- Riprendere dal punto raggiunto, senza riaprire analisi concluse.
- Applicare gli stessi criteri di comunicazione, verifica e test.

## 9. Gate di consegna — niente prove telefono a vuoto (anti-spreco crediti)

Vietato chiedere a Renato installazione o prova sul telefono se manca anche uno solo di questi punti:

1. `versionCode` **univoco e più alto** di tutti i branch `cursor/*` (così `consegna-telefono.bat` prende questo codice).
2. Compilazione interna OK: `:app:assembleDebug` (e test automatici pertinenti).
3. APK pronto in `/opt/cursor/artifacts/` **oppure** artifact CI «Consegna APK».
4. Per bug di rete/recupero dati: prova simulata in ambiente agente (socket/unit) **prima** del telefono; non chiedere «riprova e spera».

Se `consegna-telefono.bat` fallisce: chiedere **solo** il file `build-consegna.log` (o le righe `FAILED` / `e:`), non un nuovo ciclo di ipotesi senza log.

Dopo un KO telefono sullo **stesso** difetto: non rilasciare un’altra build «di speranza»; isolare la causa in ambiente agente, poi una sola build correttiva.

## 10. Documenti collegati

| Documento | Ruolo |
|---|---|
| `AGENTS.md` | Ingresso obbligatorio per agenti Cursor |
| `docs/PROMPT_CONTINUITA_AGENTE.md` | Handoff / piano; punta a queste regole |
| `docs/PVM_Nota_Integrata.md` | Requisiti e decisioni di prodotto |
| `docs/BACKLOG_WIZARD_UX.md` | Backlog Setup Catalogo / Acquisisci |
| `docs/TERMINOLOGIA_UI.md` | Terminologia UI |
| `docs/LOCK_TEMPLATE.md` | Modello obbligatorio protocollo LOCK (§11) |
| `consegna-telefono.bat` | Unico percorso PC → APK di prova |

## 11. Protocollo LOCK → FIX → PROVA (anti-riciclo, adottato 3/11/2026)

Obiettivo: quando Renato dà un elenco chiaro di punti da sistemare, l’agente **non** amplia, non inventa UI/flussi e non apre un nuovo ciclo di «migliorie».

Decisione Renato 3/11/2026: **sì** a (1) LOCK obbligatorio prima del codice su elenco bug, (2) fuori tabella = blocco totale, (3) nuova APK solo a LOCK chiuso in test.

### 11.1 LOCK (obbligatorio prima del codice)

Se la richiesta contiene un elenco numerato di bug / disallineamenti / osservazioni da test (anche con screenshot):

1. L’agente crea o aggiorna un file `docs/LOCK_<argomento>.md` usando `docs/LOCK_TEMPLATE.md`.
2. Il LOCK è una tabella con **sole** colonne ammesse:
   - **#** — numero punto
   - **Osservazione** — testo di Renato (o parafrasi fedele + riferimento screenshot)
   - **Risultato atteso** — misurabile (conteggi, testi, elementi UI presenti/assenti)
   - **Fuori scope** — cosa non toccare in questo giro
3. Vietato aggiungere righe «idea agente», «nice to have», allineamenti estetici non chiesti.
4. Se manca un LOCK e la richiesta è un elenco di punti: l’agente prepara la bozza LOCK e **attende OK esplicito di Renato** prima di modificare codice (eccezione: typo / stringa letterale già indicata parola per parola).
5. Se il LOCK è già presente e approvato (o la richiesta indica di usare un LOCK esistente): si passa al FIX senza ripresentare analisi.

### 11.2 Ambito chiuso (fuori tabella = blocco totale)

- È **proibito** modificare UI, flussi, architettura o file non necessari a chiudere una riga `#N` del LOCK attivo.
- «Allineare», «migliorare», «di passaggio», filiera, refactor non richiesti = **vietati** finché Renato non aggiunge la riga al LOCK.
- In dubbio su una riga: **fermati e chiedi** (una domanda secca); non interpretare né patchare «per sicurezza».
- Questo §11 restringe il §7: «se chiaro, procedi» vale **solo entro le righe del LOCK**, non sul prodotto intero.

### 11.3 FIX (una riga = un intervento)

- Ogni `#N` ha causa in codice e fix mirato; preferire commit `fix(LOCK-N): …`.
- Diff che tocca aree estranee alla causa di `#N`: da scartare o non consegnare in questo giro.
- Non riaprire decisioni già in Nota / backlog / LOCK precedenti.

### 11.4 PROVA (una sola, contro il LOCK)

- Guida test = checkbox **1:1** sulle righe del LOCK (stessi numeri `#N`).
- Nuova versione APK (`versionName` / `versionCode`) **solo** se:
  1. compilazione e test automatici pertinenti OK;
  2. ogni riga del LOCK ha criterio OK/KO nella guida;
  3. la guida **non** introduce punti extra fuori LOCK.
- Vale anche il §9 (gate consegna). Dopo KO telefono sullo stesso `#N`: niente build di speranza; solo ripresa di `#N`.

### 11.5 Chiusura

Il LOCK si considera chiuso quando Renato conferma OK su tutte le righe (o indica esplicitamente quali restano aperte). Solo allora si possono aprire nuovi punti o un nuovo LOCK.
