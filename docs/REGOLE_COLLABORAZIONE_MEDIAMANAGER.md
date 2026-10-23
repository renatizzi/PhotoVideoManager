# Regole vincolanti di collaborazione e sviluppo — MediaManager

**Stato:** vincolanti e permanenti  
**Data adozione:** 23/10/2026  
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

## 9. Documenti collegati

| Documento | Ruolo |
|---|---|
| `AGENTS.md` | Ingresso obbligatorio per agenti Cursor |
| `docs/PROMPT_CONTINUITA_AGENTE.md` | Handoff / piano; punta a queste regole |
| `docs/PVM_Nota_Integrata.md` | Requisiti e decisioni di prodotto |
| `docs/BACKLOG_WIZARD_UX.md` | Backlog Setup Catalogo / Acquisisci |
| `docs/TERMINOLOGIA_UI.md` | Terminologia UI |
