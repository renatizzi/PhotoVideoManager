# Prompt di continuità — allineamento testuale della filiera Setup Catalogo

**Data:** 19/10/2026
**Uso:** primo messaggio di una **nuova** sessione, un solo agente.
**Branch di partenza:** `cursor/prompt-continuita-allineamento-5311`
(è la 0.16.35 del Wizard, più questo file). Non partire da `main`.

---

## Problema da non ripetere

Nelle sessioni precedenti la chiusura descriveva un risultato che sul telefono non c’era.

È successo in tre modi:

1. La risposta diceva che i testi delle pagine seguivano la proposta, e le pagine mostravano ancora la versione precedente (0.16.34).
2. La risposta diceva «tutta la filiera è allineata» (0.16.35) dopo aver seguito un markdown scritto dall’agente (`docs/reference/template-videate-wizard-pc.md`, «Passo N di 10» e pulsanti Sì/No), non la proposta in PPT.
3. Lo script sul PC ha compilato una versione più vecchia (0.16.27 mentre il lavoro era 0.16.28) perché due branch avevano lo stesso numero di versione.

Regole di questa sessione:

- La specifica dei testi e della struttura pagine è il PPT di Renato. Il markdown `template-videate-wizard-pc.md` è la riscrittura che ha prodotto il disallineamento: si legge solo per vedere dove il codice è divergito, non per decidere come deve essere la pagina.
- Prima di scrivere «fatto» o «allineato», confronta tu ogni pagina visibile con la slide corrispondente. Se una pagina è diversa, elenchila come non fatta. Non chiudere con una frase unica di successo.
- A Renato non chiedere di confrontare le frasi. Il controllo frase per frase è tuo. A lui restano pochi controlli sul telefono, in linguaggio semplice, con il numero di versione che deve leggere nell’app.
- Non citare merge, PR, branch o GitHub nelle istruzioni per Renato.

---

## Stato del progetto

| Voce | Valore |
|---|---|
| Repo | `renatizzi/photovideomanager` (app: **MediaManager**) |
| Codice da cui partire | branch `cursor/prompt-continuita-allineamento-5311`, commit di base `3748b9d` |
| Versione in quel commit | **0.16.35-preview**, versionCode **76** |
| PR aperta sul Wizard | https://github.com/renatizzi/PhotoVideoManager/pull/16 (bozza) |
| Filiera già provata da Renato | fino alla **0.16.33** lo script PC → CIFS → cartella Catalogo funzionava. La 0.16.34 e la 0.16.35 non sono state convalidate da lui come testi allineati alla proposta. |
| Cosa non riaprire | architettura della Posizione del Catalogo, SMB nativo, sync multi-apparecchio. Il pezzo multi-apparecchio resta fermo. |

Sul telefono, oggi, la 0.16.35 ha titolo «Setup Catalogo personale» e una scheda CIFS con Copia per campo. Il resto della filiera PC non è la proposta: sono micro-pagine con pulsanti.

---

## Documenti da leggere prima di modificare il codice

Leggili in questo ordine. Il primo comanda i testi.

1. **`docs/reference/revisione_guida.pptx`** — proposta di Renato. 17 slide. Questa è la fonte.
2. **`docs/reference/valutazione-revisione-guida-pptx.md`** — solo le decisioni già chiuse con Renato il 19/10 (elenco sotto). Il resto di quel file è il parere dell’agente, non una nuova specifica.
3. **`docs/BACKLOG_WIZARD_UX.md`** — per non rifare lavori già chiusi. Aggiornalo a fine intervento, senza dichiarare chiuso ciò che non hai verificato sul testo delle pagine.
4. `docs/reference/Nota_operativa_Wizard_Catalogo_Cursor_e1d1.pdf` e `docs/PVM_Nota_Integrata.md` — vincoli già consolidati (niente SMB nativo, ritorno da CIFS manuale, linguaggio semplice). Non sostituiscono il PPT sulla forma delle pagine.
5. `docs/reference/template-videate-wizard-pc.md` — **non è la specifica.** È il documento che ha trasformato la proposta in 10 passi con pulsanti.

---

## Come è fatta una pagina (slide 3 del PPT, vale per tutte)

Ogni videata di Setup Catalogo personale:

- **Titolo fisso:** Setup Catalogo personale
- **Sottotitolo:** numero del passo + titolo di quel passo
- **Introduzione:** breve descrizione
- **Domanda, se c’è:** una sola, con le risposte chiuse dentro un **menu a tendina** (combo). Le voci del menu non diventano pulsanti uno sotto l’altro sulla pagina.
- **Checklist, se c’è:** le istruzioni operative sono riquadri da spuntare. **OK** si abilita solo quando sono tutti spuntati.
- **Help:** previsto, da sviluppare dopo. In questo intervento non si costruisce il repository what/if. Sulle pagine operative non si elencano i problemi (niente fila di pulsanti «Non riesco», «Ne vedo solo uno», «La verifica è fallita»).

Slide 2 (pagina Configura nello stile di Utility) resta fuori da questo intervento.

---

## Filiera da allineare (PPT, ramo PC)

Numerazione del PPT, non quella «Passo N di 10».

| Passo | Slide | Pagina | Forma |
|---|---|---|---|
| 0 | 4 | Dove tenere il Catalogo | Menu a tendina «Seleziona il dispositivo»: Su questo dispositivo; Su un PC/Notebook Windows; Su Cloud (Google Drive); Su disco di rete o NAS. Sotto, i campi si adattano alla voce scelta. Poi OK. |
| 1 | 5 | Verifica della disponibilità di quanto necessario | Checklist (telefono/tablet con MediaManager, PC Windows 10 o superiore, cavo USB dati). OK solo se tutto spuntato. |
| 2 | 5 | Verifica preliminare, preparazione dei file e collegamento | Checklist del collegamento USB e del passaggio da «Solo ricarica» a «Trasferimento di file». OK solo se tutto spuntato. |
| 3 | 6 | Copia manuale dei file di configurazione | Checklist: Download del telefono → Desktop, entrambi i file, verifica che ci siano. OK solo se tutto spuntato. |
| 4 | 8 e 9 | Generazione password e dati per la cartella condivisa | Checklist di avvio dello script e attesa di «Tutto pronto». I dati di riepilogo della slide 9 si mostrano dove servono al passo CIFS, non come pagina di errori. |
| 5 | 13 | Accedere alla cartella mediante CIFS | Scheda con le stesse etichette del form CIFS, Copia sul singolo campo, password con Mostra e Copia. Poi la scelta della cartella. |

Dopo il passo 0, se il dispositivo non è il PC, non inventare una filiera parallela. Cloud e NAS restano il percorso breve già presente (stub): si possono scegliere, senza promettere gli stessi passi del PC.

---

## Decisioni di Renato che modificano singole frasi del PPT

Rispetta queste anche se la slide dice altro. Non riaprirle.

- **Campi sotto il dispositivo (slide 4).** Si adattano alla voce del menu: PC → lettera del disco e percorso (controllo sintassi, in questa fase il disco è C:); telefono/tablet → Memoria principale e Secondaria (microSD); Cloud/NAS → niente percorso da PC.
- **Avvio script (slide 8).** Doppio clic sul file e conferma della finestra di Windows (UAC). Non scrivere «tasto destro → Esegui come amministratore».
- **Password (slide 9).** Non usare la password fissa `mediamanager` e non chiedere di copiarla a mano dalla finestra del PC. Al passo CIFS l’app mostra la password già generata, con Mostra/Nascondi e Copia. Non compare nel riepilogo finale.
- **Quando nascono i file.** Non c’è una pagina separata «Prepara i file». I due file vanno in Download quando Renato conferma con OK il passo 2 (checklist di collegamento). Senza quello OK non si va avanti.
- **Nomi dei file (slide 6).** Nei testi e nei file generati i nomi sono **CONFIGURA** e **sup**. Le slide successive citano ancora il nome lungo: usa i nomi nuovi. Cambia solo il nome del file prodotto e le frasi che l’utente legge. Non riscrivere la logica dello script.
- **CIFS.** Scheda compilata a mano nell’app CIFS, campo per campo, con Copia. Nessun riempimento automatico del form CIFS. Il ritorno da CIFS a MediaManager è con il tasto Indietro del telefono.
- **Checklist.** Spuntare un riquadro non è una verifica automatica. L’unica prova reale resta l’apertura della cartella a fine percorso. Non scrivere che l’app «ha controllato» copia o script.
- **Termine.** In interfaccia si dice **Posizione del Catalogo**, non «Sede».

---

## Cosa c’è nel codice 0.16.35 e va sostituito

File: `CatalogSeatWizardScreen.kt`, `WizardStepChrome.kt`, `strings.xml`.

- La scelta dispositivo (`IntroStep`) è quattro pulsanti a tutta larghezza. Un tap sceglie e cambia pagina subito. Nel PPT è un menu a tendina, i campi sotto compaiono dopo la scelta, si prosegue con OK. Esiste già l’etichetta `hub_combo_label` («Seleziona il dispositivo») e non è usata come menu.
- Le domande dei passi copia, script e CIFS sono pulsanti affiancati in verticale («Sì, entrambi», «Ne vedo solo uno», «Non riesco», «Tutto pronto», «Ancora in lavorazione»…). Nel PPT quelle istruzioni sono checklist, e le risposte chiuse sono una combo.
- Il sottotitolo è «Passo N di 10», preso dal markdown dell’agente. I passi del PPT sono 0 e poi 1–5.
- Il pulsante «Il PC ha già la cartella condivisa» apre ancora il testo vecchio (tasto destro → Condivisione, «Ho creato la cartella»), numerato come passo 2. Non fa parte della proposta: toglilo da questa filiera.
- «Non riesco», da qualunque passo, apre un unico paragrafo sul collegamento CIFS e da lì riporta verso CIFS. Non è l’help della proposta.
- I file si chiamano ancora `AVVIA_Configurazione_Catalogo` e `_non_aprire_supporto_setup`.

La scheda CIFS con etichette Nome, Host, Port, Domain, Folder, Username, Password, Storage e il pulsante Copia può restare, se le etichette coincidono con il form CIFS e con la slide 13. Non diluirla in un paragrafo.

---

## Primo compito di questa sessione (solo questo)

Allineare testo e struttura di **tutta** la filiera Setup Catalogo (passi 0–5 sopra) al PPT, con le decisioni chiuse.

In particolare: dove la proposta ha un menu a tendina, in pagina si vede il menu chiuso. Le opzioni non sono riportate come pulsanti sulla stessa pagina.

Fuori da questo compito, e quindi da non iniziare nello stesso intervento:

- repository di help contestuale what/if (slide 7, 10–12, 14–16);
- pagina Configura nello stile di Utility (slide 2);
- completamento dei rami Cloud e NAS;
- stampa o PDF della scheda;
- qualsiasi modifica allo script Windows che non sia il nome dei due file e la frase di attesa già prevista.

A fine intervento:

1. Alza `versionCode` sopra 76 e il nome versione sopra 0.16.35, in modo che sia il più alto di tutti i branch, così lo script di aggiornamento sul PC non ricompila una versione vecchia.
2. Aggiorna il test che oggi controlla solo «Passo N di 10»: deve fallire se la scelta dispositivo non è un menu a tendina e se i sottotitoli della filiera PC non sono quelli dei passi 0–5.
3. Scrivi a Renato, in linguaggio semplice, la versione da leggere sul telefono e questi controlli:
   - il titolo resta «Setup Catalogo personale»;
   - al passo del dispositivo c’è un menu a tendina, e le quattro voci non sono quattro pulsanti;
   - scelto il PC, le pagine successive sono checklist con OK, non una pila di pulsanti Sì/No;
   - i due file citati si chiamano CONFIGURA e sup;
   - al passo CIFS la scheda ha Copia su ogni dato, password inclusa.
4. Se un punto dell’elenco non è vero nel codice, non consegnare la prova: sistemalo prima, oppure dì quale punto è rimasto fuori e non descrivere il resto come completo.
