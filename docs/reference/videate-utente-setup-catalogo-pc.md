# Videate utente — Setup Catalogo personale (ramo PC)

**Stato:** allineato a `docs/LOCK_SETUP_BT_BUNDLE_CIFS.md` (OK Renato 06/11/2026 — Bluetooth al posto di USB; form CIFS intero solo-Copia).  
**Fonte testi/struttura:** PPT V2 + decisioni 19–20/10 + LOCK BT 06/11.  
**Fuori da questo documento:** help/errori what-if; pagina Configura stile Utility; rami Cloud/NAS/locale oltre allo stub sul passo 0.

Questo file descrive **solo ciò che l’utente vede** nelle videate. Non è il vecchio `template-videate-wizard-pc.md`.  
In caso di conflitto con testi USB/MTP o Host da digitare: **prevale la LOCK**.

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
Validazione sintassi (es. `\` e non `/`; lettera disco con `:`). In questa fase il disco è `C:`.

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

1. Verifica della disponibilità di quanto necessario (Bluetooth)  
2. Collegamento Bluetooth, preparazione e invio dei file di configurazione  
3. File di configurazione sul Desktop del PC  
4. Generazione password e cartella condivisa («Tutto pronto»)  
4b. Scarica sul telefono i dati CIFS (bundle)  
5. Accedere alla cartella del Catalogo mediante CIFS (scheda intera, solo Copia)  

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
- [ ] Bluetooth attivo sul telefono e sul PC  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 2 — Collegamento Bluetooth e preparazione dei file

**Sottotitolo:** 2 – Collegamento Bluetooth, preparazione e invio dei file  

**Introduzione:**  
Autorizza il collegamento Bluetooth tra telefono e PC, poi prepara i file di configurazione.

Testo di supporto:

Sul PC: Impostazioni → Bluetooth e dispositivi → «Invia o ricevi file tramite Bluetooth» → **Ricevi file**. Sul telefono completa l’associazione se richiesta.

**Checklist** (OK solo se tutte spuntate):

- [ ] Telefono e PC sono accesi, sbloccati e con Bluetooth attivo  
- [ ] Ho associato (paired) telefono e PC  
- [ ] Sul PC ho avviato «Ricevi file» e sto attendendo  

**Comportamento sull’OK:** l’app genera **CONFIGURA** e **sup** e avvia l’invio via Bluetooth. Senza questo OK non si avanza e i file non vengono creati/inviati.

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 3 — File di configurazione sul Desktop

**Sottotitolo:** 3 – File di configurazione sul Desktop  

**Introduzione:**  
Dopo la ricezione Bluetooth, i file devono stare sul Desktop del PC.

**Checklist** (OK solo se tutte spuntate):

- [ ] Ho accettato i file **CONFIGURA** e **sup** sul PC  
- [ ] Entrambi i file sono sul Desktop del PC  
- [ ] Non apro il file **sup** (si usa solo tramite CONFIGURA)  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 4 — Generazione password e cartella condivisa

**Sottotitolo:** 4 – Generazione password e altri dati necessari per configurare la cartella condivisa  

**Introduzione:**  
È tutto pronto per lanciare la routine che crea la cartella dedicata al Catalogo personale.

**Istruzione di avvio:**  
Sul Desktop, **doppio clic** su **CONFIGURA**. Se compare il Controllo account utente, **conferma**. Non usare «Esegui come amministratore» dal tasto destro. Non aprire **sup**.

A «Tutto pronto» lo script lascia sul Desktop anche il file **`MediaManager_cifs_bundle.json`**. Non trascrivere i dati dalla finestra: li scaricherai al passo successivo.

**Checklist** (OK solo se tutte spuntate):

- [ ] Sul Desktop ho fatto doppio clic su CONFIGURA  
- [ ] Ho confermato il Controllo account utente, se è comparso  
- [ ] Ho atteso senza interrompere finché è comparso «Tutto pronto»  
- [ ] Sul Desktop vedo anche `MediaManager_cifs_bundle.json`  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Passo 4b — Scarica i dati CIFS sul telefono

**Sottotitolo:** 4b – Scarica i dati per CIFS  

**Introduzione:**  
Dal telefono richiedi il file con tutti i dati già pronti per CIFS. Sul PC non serve alcun automatismo nello script: basta inviare una volta il file bundle già sul Desktop.

**Checklist** (OK solo se tutte spuntate):

- [ ] In MediaManager ho toccato **Scarica dati CIFS**  
- [ ] Sul PC ho inviato via Bluetooth il file `MediaManager_cifs_bundle.json` (Desktop)  
- [ ] Sul telefono ho accettato il file e l’app conferma «Dati CIFS pronti»  

Se l’app segnala bundle incompleto: non si avanza; ripetere invio o rieseguire lo script.  
**Niente** digitazione Host, **niente** ricomposizione User/Password a pezzi.

**Chiusura:** Quando pronto clicca su OK → **OK** *(OK abilitato solo con bundle completo)*

---

## Passo 5 — Accedere alla cartella del Catalogo mediante CIFS

*Tutta la configurazione CIFS sta qui. Nessuna pagina «coda» dopo questo passo.*  
*La scheda si apre solo con bundle completo (passo 4b).*

**Sottotitolo:** 5 – Accedere alla cartella del Catalogo mediante CIFS  

**Introduzione:**  
Configura CIFS Documents Provider (Google Play) usando **solo Copia** dalla scheda qui sotto. I valori sono già definitivi: non modificarli e non integrarli a mano.

### Scheda dati *(speculare al form CIFS «Connection» — tutti i campi dati pieni + Copia)*

| Campo form CIFS | Valore in MediaManager | Azione utente |
|---|---|---|
| **ID** | (generato da CIFS) | Non modificare |
| **Name** | da bundle + **Copia** | Incolla |
| **Storage** | `SMB2,3 (jCIFS NG)` + **Copia** | Incolla / conferma |
| **Domain** | *(vuoto)* | Lascia vuoto |
| **Host** | IPv4 da bundle + **Copia** *(sempre valorizzato)* | Incolla — **non** lente di scansione |
| **Port** | `445` + **Copia** | Incolla |
| **Folder** | nome share da bundle + **Copia** *(obbligatorio, mai vuoto)* | Incolla |
| **Enable DFS** | Non attivo | Lascia deselezionato |
| **User** | `NOMEPC\MediaManager` (già qualificato) + **Copia** | Incolla così com’è |
| **Password** | da bundle + **Mostra/Nascondi** + **Copia** | Incolla |
| **Read only** *(se presente in CIFS)* | deve restare OFF | Lascia disattivato |

Poi in CIFS: **Save**. Torna a MediaManager con Indietro (il ritorno non è automatico).

### Checklist operativa del passo 5

OK solo se tutte spuntate.

- [ ] Ho installato / aperto CIFS Documents Provider  
- [ ] Ho creato una nuova Connection e lasciato **ID** com’è  
- [ ] Ho copiato e incollato **Name**  
- [ ] Ho copiato / impostato **Storage** su SMB2,3 (jCIFS NG)  
- [ ] Ho lasciato **Domain** vuoto  
- [ ] Ho copiato e incollato **Host**  
- [ ] Ho copiato e incollato **Port**  
- [ ] Ho copiato e incollato **Folder**  
- [ ] Ho lasciato **Enable DFS** deselezionato  
- [ ] Ho lasciato **Read only** disattivato *(se il campo c’è)*  
- [ ] Ho copiato e incollato **User**  
- [ ] Ho copiato e incollato **Password**  
- [ ] In CIFS ho toccato **Save** e sono tornato a MediaManager con Indietro  
- [ ] **Esito:** connessione CIFS salvata e configurazione andata a buon fine *(autodichiarazione)*  

**Chiusura:** Quando pronto clicca su OK → **OK**

---

## Cosa non compare in queste videate

| Elemento | Motivo |
|---|---|
| Help / «Non riesco» / checkpoint errori | Da sviluppare dopo consolidamento UI |
| Pagina Configura stile Utility | Lavoro separato (slide 2 PPT) |
| Elenco passi su Cloud / NAS / questo dispositivo | Procedure non ancora definite (stub sul passo 0) |
| «Prendi nota» / trascrizione da finestra PC | Sostituito da bundle + scheda CIFS solo-Copia |
| Cavo USB / MTP come percorso felice | Sostituito da Bluetooth (USB solo piano B in LOCK) |
| Host vuoto o da digitare | Vietato dalla LOCK |
| Password fissa `mediamanager` | Non adottata |
| Pagina coda dopo il passo 5 | Tutto ciò che riguarda CIFS sta nel passo 5 |

---

## Decisioni recepite — sintesi

1. Percorso: oggi unico `C:\…`; a regime controllo che ridisegna il riquadro per tipo dispositivo.  
2. Elenco passi solo sul passo 0 dopo scelta PC; altri dispositivi → loro elenco (da definire).  
3. Passo 0 senza numero nel sottotitolo.  
4. Avvio CONFIGURA: **doppio clic + UAC**.  
5. Trasporto file: **Bluetooth** (andata script, ritorno bundle scaricato dal telefono).  
6. Form CIFS: **interamente compilato** dal bundle; **solo Copia**; zero integrazioni/modifiche sui dati.  
7. **Folder** obbligatorio nella scheda (pin share).  
8. Nessuna coda dopo CIFS; checklist per ogni copia/incolla + esito.
