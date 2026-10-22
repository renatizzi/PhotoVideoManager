# Videate utente — Setup Catalogo personale (ramo PC)

**Stato:** consolidato 20/10/2026 dopo risposte Renato alle Q1–Q7 su `revisione_guida_V2.pptx`.  
**Fonte testi/struttura:** PPT V2 + decisioni 19/10 + chiarimenti 20/10.  
**Fuori da questo documento:** help/errori what-if; pagina Configura stile Utility; rami Cloud/NAS/locale oltre allo stub sul passo 0.

Questo file descrive **solo ciò che l’utente vede** nelle videate. Non è il vecchio `template-videate-wizard-pc.md`.

---

## Chrome comune (tutte le videate)

| Elemento | Contenuto |
|---|---|
| **Titolo** | Setup Catalogo personale *(fisso)* |
| **Sottotitolo** | Numero passo + titolo del passo *(eccezione: passo 0 senza numero — vedi sotto)* |
| **Introduzione** | Breve descrizione della pagina |
| **Domanda** *(se c’è)* | Una sola; risposte chiuse in **menu a tendina** (combo chiuso in pagina; le voci non diventano pulsanti uno sotto l’altro) |
| **Checklist** *(se c’è)* | Istruzioni operative a riquadri; **OK** abilitato solo quando tutti i riquadri sono spuntati |
| **Help** | Previsto a regime; **non figurare** in queste videate in questa fase |

Non elencare in pagina file di problemi («Non riesco», «Ne vedo solo uno», …).

---

## Passo 0 — Dove tenere il Catalogo

*Passo comune a tutti i dispositivi. Sottotitolo **senza numero**.*

**Sottotitolo:** Dove tenere il Catalogo  

**Introduzione:**  
Inizia a scegliere in quale dispositivo e cartella vuoi tenere il Catalogo Personale:

**Menu a tendina** — etichetta: `Seleziona il dispositivo`  
Voci (menu chiuso in pagina):

1. Su questo dispositivo *(default)*  
2. Su un PC/Notebook Windows  
3. Su Cloud (Google Drive)  
4. Su disco di rete o NAS  

### Riquadro percorso / volume *(condizionato al dispositivo)*

**Ora (fase corrente):** un solo percorso unico editabile, valorizzato di default con:

`C:\MediaManager\Catalogo`

Sotto il riquadro: `Valori modificabili`  
Validazione sintassi (es. `\` e non `/`; lettera disco con `:`). Amessi disco interno **e** dischi esterni collegati al PC (`C:\…`, `E:\…`, …). Default: `C:\MediaManager\Catalogo`.

**A regime:** lo stesso controllo **ridisegna** riquadro e valori di input in base al dispositivo:

| Dispositivo | Contenuto del riquadro |
|---|---|
| Su questo dispositivo | Memoria principale / Secondaria (microSD) |
| Su un PC/Notebook Windows | Lettera seguita da `:` + percorso cartella (es. `C:\MediaManager\Catalogo`) |
| Su Cloud (Google Drive) | Sigla che identifica il cloud |
| Su disco di rete o NAS | Lettera seguita da `:` |

### Blocco variabile dopo la scelta del dispositivo

Compare **sotto** il menu «Seleziona il dispositivo», **solo dopo** la scelta.

#### Se «Su un PC/Notebook Windows»

**Descrizione:**  
Da questa pagina si attiva la procedura guidata per la creazione o modifica della cartella condivisa dove tenere il Catalogo. Per motivi di sicurezza ci sono delle operazioni che puoi fare solo tu.

**Se hai scelto un PC o Notebook Windows, la procedura prevede:**

1. Verifica della disponibilità di quanto necessario  
2. Verifica preliminare, preparazione dei file e collegamento  
3. Copia manuale dei file di configurazione  
4. Generazione password e altri dati necessari per configurare la cartella condivisa  
5. Accedere alla cartella del Catalogo mediante CIFS  

Più il riquadro percorso con default `C:\MediaManager\Catalogo` e la riga `Valori modificabili`.

#### Se «Su questo dispositivo» / Cloud / disco di rete o NAS

Al posto dell’elenco 1–5 PC: **i passi previsti dalla specifica procedura di quel dispositivo** *(non ancora definiti in questo documento — stub)*.

**Chiusura pagina:** Quando pronto clicca su OK → **OK**

---

## Passo 1 — Verifica della disponibilità di quanto necessario

*(ramo PC, dopo OK del passo 0 con PC selezionato)*

**Sottotitolo:** 1 – Verifica della disponibilità di quanto necessario  

**Introduzione:**  
Prima di avviare la procedura verifica e conferma di avere a disposizione i seguenti strumenti:

**Checklist** (OK solo se tutte spuntate):

- [ ] Telefono Android o tablet dove è stata installata l’app MediaManager  
- [ ] PC/Notebook Windows v. 10 o superiore  
- [ ] Cavo USB adatto al trasferimento dei dati  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 2 — Verifica preliminare, preparazione dei file e collegamento

**Sottotitolo:** 2 – Verifica preliminare, preparazione dei file e collegamento  

**Introduzione:**  
Esegui ora le seguenti verifiche preliminari:

Testo di supporto (come nel PPT, non come fila di pulsanti problema):

Il cavetto USB è spesso configurato solo per ricaricare. Per trasferire i file: apri le notifiche scorrendo dal bordo superiore, tocca «Ricarica questo dispositivo via USB» (o simile), scegli «Trasferimento di file» (o simile), poi consenti l’accesso ai dati del telefono.

**Checklist** (OK solo se tutte spuntate):

- [ ] Collega il telefono al PC/Notebook con un cavetto USB adatto al trasferimento dei dati (meglio se quello fornito in dotazione)  
- [ ] Verifica che il PC e il telefono siano accesi e sbloccati  
- [ ] Modifica sul telefono la modalità di collegamento USB da «Solo ricarica» a «Trasferimento di file»  
- [ ] Consenti l’accesso ai dati del telefono  

**Comportamento sull’OK:** l’app crea in Download i file **CONFIGURA** e **sup**. Senza questo OK non si avanza e i file non vengono creati.

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 3 — Copia manuale dei file di configurazione

**Sottotitolo:** 3 – Copia manuale dei file di configurazione  

**Introduzione:**  
Dopo aver collegato il telefono al PC/Notebook esegui le seguenti operazioni:

Testo di supporto (template, nomi nuovi):

Dal PC: Esplora file → Questo PC → nome del telefono → Memoria interna (o Telefono) → Download. Seleziona i file **CONFIGURA** e **sup**, copiali (Ctrl+C) e incollali sul Desktop (Ctrl+V).

**Checklist** (OK solo se tutte spuntate):

- [ ] Accedi dal PC alla cartella Download del telefono  
- [ ] Copia i due file CONFIGURA e sup dalla cartella Download sul Desktop del PC  
- [ ] Verifica che entrambi i file siano presenti sul Desktop  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 4 — Generazione password e altri dati per la cartella condivisa

**Sottotitolo:** 4 – Generazione password e altri dati necessari per configurare la cartella condivisa  

**Introduzione:**  
È tutto pronto per lanciare la routine che ti consentirà di creare la cartella dedicata al Catalogo personale. Ecco le istruzioni:

**Istruzione di avvio (cosa l’utente deve fare davvero):**  
Sul Desktop, **doppio clic** sul file **CONFIGURA**. Se Windows mostra la finestra di Controllo account utente, **conferma**. Non usare «Esegui come amministratore» dal tasto destro: lo script chiede già l’elevazione da solo. Non aprire il file **sup**.

*(«Prendi nota» dei dati dalla finestra PC: **omesso** — i dati per CIFS sono nella scheda del passo 5, con Copia.)*

**Checklist** (OK solo se tutte spuntate):

- [ ] Sul Desktop del PC/Notebook ho fatto doppio clic su CONFIGURA  
- [ ] Ho confermato la finestra di Windows (Controllo account utente), se è comparsa  
- [ ] Ho atteso senza interrompere finché la routine ha mostrato «Tutto pronto»  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 5 — Accedere alla cartella del Catalogo mediante CIFS

*Tutta la configurazione CIFS sta qui. Nessuna pagina «coda» dopo questo passo.*

**Sottotitolo:** 5 – Accedere alla cartella del Catalogo mediante CIFS  

**Introduzione:**  
L’ultimo passo della procedura prevede la configurazione dell’app denominata CIFS Documents Provider (scaricabile gratuitamente su Google Play).

Ecco i dati da inserire: con il bottone «Copia» puoi incollarli uno alla volta sul form di CIFS.

### Scheda dati *(stesso ordine e stessi nomi del form CIFS «Connection» — screenshot in PPT V2)*

Valori noti all’app dove disponibili; istruzione esplicita anche per campi vuoti o da lasciare al default CIFS.

| Campo form CIFS (nome originale) | Cosa mostra / fa MediaManager | Azione utente |
|---|---|---|
| **ID** | Valore generato da CIFS (UUID) | Non modificare |
| **Name** | Es. `MediaManagerCatalog` + **Copia** | Incolla in Name |
| **Storage** | `SMB2,3 (jCIFS NG)` + **Copia** (o conferma se già selezionato) | Seleziona / conferma questa voce |
| **Domain** | *(lascia vuoto)* | Lascia vuoto |
| **Host** | IP del PC (se noto all’app) + **Copia**; altrimenti istruzione a usare l’IP dalla finestra «Tutto pronto» sul PC | Incolla l’IP in Host *(non usare la lente di scansione come percorso principale)* |
| **Port** | `445` + **Copia** | Incolla / conferma 445 |
| **Enable DFS (Distributed File System)** | Non attivo | Lascia deselezionato |
| **User** | `MediaManager` (+ eventuale formato `NOMEPC\MediaManager` se serve) + **Copia** | Incolla in User |
| **Password** | Password generata dall’app + **Mostra/Nascondi** + **Copia** | Incolla in Password *(non trascritta a mano dal PC)* |

Poi in CIFS: **Save**. Torna a MediaManager con il tasto Indietro del telefono (il ritorno non è automatico).

### Checklist operativa del passo 5

Criterio: **un riquadro per ogni dato da copiare/incollare** (e per le azioni che non sono un campo), più **ultimo riquadro = esito** della configurazione. OK solo se tutte spuntate.

- [ ] Ho installato / aperto CIFS Documents Provider  
- [ ] Ho creato una nuova Connection e lasciato **ID** com’è  
- [ ] Ho copiato e incollato **Name**  
- [ ] Ho impostato **Storage** su SMB2,3 (jCIFS NG)  
- [ ] Ho lasciato **Domain** vuoto  
- [ ] Ho copiato e incollato **Host** (IP del PC)  
- [ ] Ho impostato **Port** a 445  
- [ ] Ho lasciato **Enable DFS** deselezionato  
- [ ] Ho copiato e incollato **User**  
- [ ] Ho copiato e incollato **Password** (dopo averla mostrata se serve)  
- [ ] In CIFS ho toccato **Save** e sono tornato a MediaManager con Indietro  
- [ ] **Esito:** la connessione in CIFS è salvata e la configurazione è andata a buon fine *(autodichiarazione; non è una verifica automatica dell’app)*  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Cosa non compare in queste videate

| Elemento | Motivo |
|---|---|
| Help / «Non riesco» / checkpoint errori | Da sviluppare dopo consolidamento UI |
| Pagina Configura stile Utility | Lavoro separato (slide 2 PPT) |
| Elenco passi 1–5 su Cloud / NAS / questo dispositivo | Procedure non ancora definite (stub sul passo 0) |
| «Prendi nota» / printscreen dei dati a fine script | Sostituito dalla scheda CIFS + Copia al passo 5 |
| Password fissa `mediamanager` | Non adottata |
| Pagina separata dopo il passo 5 («Scegli cartella» come coda) | Tutto ciò che riguarda CIFS sta nel passo 5 |

---

## Decisioni recepite (20/10) — sintesi

1. Percorso: oggi unico `C:\…`; a regime controllo che ridisegna il riquadro per tipo dispositivo.  
2. Elenco 1–5 solo sul passo 0 dopo scelta PC; altri dispositivi → loro elenco (da definire).  
3. Passo 0 senza numero nel sottotitolo; la numerazione dipende dalla procedura del dispositivo.  
4. Avvio CONFIGURA: **doppio clic + UAC** (autoelevazione; niente «Esegui come amministratore»).  
5. «Prendi nota» omesso se la scheda CIFS è completa (come sopra).  
6. Campi CIFS = ordine/nomi dello screenshot form Connection nel PPT V2.  
7. Nessuna coda dopo CIFS; checklist per ogni copia/incolla + ultimo punto sull’esito.
