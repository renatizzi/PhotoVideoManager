# Proposta unica — Razionalizzazione configurazione Catalogo e UX sede

**Stato:** PROPOSTA IN ATTESA DI APPROVAZIONE ESPLICITA DI RENATO — **nessuna implementazione autorizzata**  
**Data:** 25/10/2026 (Europe/Rome)  
**Autore:** Cursor (analisi AS-IS + proposta TO-BE)  
**Perimetro:** A–D del prompt (sede Catalogo, identificazione UI, CIFS/IP, razionalizzazione passi).  
**Fuori perimetro:** riprogettazione Acquisisci/Importa/Aggiorna/Pulisci/Ricerca (salvo integrazioni minime per la sede).

---

## Premessa metodologica

- Questa consegna **non** autorizza sviluppo, branch di codice, né layout sperimentali.
- Dove documenti e codice divergono, le discordanze sono **segnalate**, non risolte arbitrariamente.
- Distinzione obbligatoria: schermata disegnata ≠ voce UI ≠ implementato ≠ verificato in prova telefono.

---

## A. Stato AS-IS verificato

### A.1 Branch e versioni esaminati

| Riferimento | versionName / versionCode | Contenuto rilevante |
|---|---|---|
| `cursor/prompt-continuita-allineamento-5311` (base) | **0.16.37-preview** / 78 | Wizard PC PPT 0–5, shell, Organizza, Acquisisci “classico” |
| `cursor/cifs-user-folder-attiva-b602` (PR parallela) | **0.16.52-preview** / 93 | + gruppi fonti W1–W4, Catalogo di riferimento in Acquisisci, filiera IP CIFS (poi parcheggiata) |
| `cursor/docs-allineamento-nota-albero-b602` | 0.16.37 (solo docs) | Export Nota v5.3 + albero navigazione |
| `docs/PVM_Nota_Integrata.md` | **v5.3** (header 10/10/2026; §7.1 ancora cita 0.16.26) | Requisiti H1 / W1–W4; **non aggiornata** allo stato 0.16.52 |
| Videate utente (PDF caricato, 6 slide image-only) | UI fotografata **0.16.52-preview** | Dashboard, Acquisisci, Aggiorna, Setup, Organizza, Utility |

**Discordanza #1:** la Nota Integrata “corrente” in repo è ancora **v5.3 / ottobre**, mentre l’app sul tablet di prova è **0.16.52** su branch parallelo non unificato alla base 0.16.37.  
**Discordanza #2:** l’albero navigazione esportato descrive 0.16.37 + nota delta 0.16.52; le videate PDF documentano il delta già a schermo.  
**Discordanza #3:** §7.1 Nota parla di ingresso «Imposta Catalogo condiviso»; in UI compare **Setup Catalogo personale** / **Posizione del Catalogo**.

### A.2 Flusso AS-IS (sintesi)

```
[Configura] ──► Setup Catalogo personale ──┐
[Acquisisci] ─► (se non configurato / link)┤
[Aggiorna Catalogo] ─► wizard / update ────┤
                                           ▼
                              S0 Inizio (Crea | Modifica)
                                           ▼
                              Passo 0 — Dove tenere il Catalogo
                              (combo W1 | W2 | W3 | W4)
                     ┌─────────┼──────────┼──────────┐
                     ▼         ▼          ▼          ▼
                   W1        W2 PC      W3 Cloud   W4 NAS
                 conferma   Passi 1–5    guida      guida
                 + pick     USB+script   + pick     + pick
                            + CIFS+IP
                            + pick SAF
                                           ▼
                                        DONE / sede salvata
                                           │
[Acquisisci 0.16.52] Catalogo di riferimento + fonti (gruppi)
[Importa] → [Aggiorna] → [Pulisci] / [Ricerca]  (preservati)
```

### A.3 Funzioni: implementate / parziali / non presenti

| Area | Stato verificato sul codice | Prova telefono (da annotazioni / videate) |
|---|---|---|
| Shell Home / Organizza / Componi / Utility / Config | Implementato (layout congelato) | OK videate |
| Acquisisci → Importa → Aggiorna → Pulisci → Ricerca | Implementato | OK funzionale (pregresse) |
| Wizard S0 + Passo 0 W1–W4 | Implementato (combo 4 voci) | OK videate 0.16.52 |
| W2 Passi 1–5 (checklist USB, CONFIGURA/sup, CIFS) | Implementato | Procedura lunga; usata |
| Recupero automatico IP/User CIFS (`SetupLanAnnounce`) | **Implementato in codice** su branch 0.16.4x–52 | **KO ripetuti → PARCHEGGIATO 23/10** (IP manuale accettato) |
| Catalogo di riferimento in **Acquisisci** | Solo **0.16.52** | Presente in videate (es. «MediaManager · Su disco di rete/NAS · Raggiungibile») |
| Sede Catalogo in **Dashboard** | **Assente** | Videate: solo KPI, nessuna sede |
| Sede Catalogo in hub **Organizza** | **Assente** | Videate: solo Acquisisci / Aggiorna |
| Sede Catalogo in **Aggiorna** | **Assente** (solo conteggi) | Videate |
| W3 Cloud / W4 NAS | **Parziali** (guida + OpenDocumentTree; non filiera completa) | Stub accettati Nota |
| Scansione Wi‑Fi dispositivi | **Non** come prodotto fonti | HTTP scan subnet **solo** per annuncio setup (vedi §D) — distinto da censimento LAN |

### A.4 Problemi UX/tecnici rilevanti (AS-IS)

1. **Ingressi multipli** alla stessa funzione (Configura, Acquisisci, Aggiorna Catalogo) senza un “luogo” unico che mostri sempre sede+stato.  
2. **Dashboard e Organizza** non identificano la sede → l’utente perde il contesto fuori da Acquisisci/Wizard.  
3. Confusione storica **alias tablet ↔ nome Posizione** (mitigata in 0.16.52 su Acquisisci, non su Home/Organizza).  
4. Ramo **W2** molto lungo (5 passi + app CIFS esterna); utile, ma pesante se la share esiste già.  
5. **IP auto**: codice presente ma **non affidabile in prova**; Riprova nascosto durante `recoveringPcData` (KO 0.16.49).  
6. Base 0.16.37 e PR 0.16.52 **non allineati**: rischio di sviluppare sulla base sbagliata.

---

## B. Flow chart TO-BE

### B.1 Principi TO-BE (vincolanti rispetto al prompt)

- Un solo concetto: **Posizione del Catalogo** (sede H1).  
- Quattro rami W1–W4 sempre di primo livello.  
- Nessuna scansione Wi‑Fi per censire dispositivi.  
- Nessuna scelta “rapida vs guidata” dove il ramo basta a determinare il percorso.  
- Scorciatoia ammissibile **solo** se punta alle **stesse** verifiche del percorso lungo (non seconda implementazione).  
- Acquisisci/Importa/Aggiorna/Pulisci/Ricerca **invariati** nel merito; solo aggiunta/riuso del blocco sede.

### B.2 Percorso unico

```
┌─────────────────────────────────────────────────────────────┐
│  INGRESSO UNICO — «Posizione del Catalogo»                  │
│  (stesso destino da: Configura · Organizza · Acquisisci ·   │
│   Dashboard tap sulla riga sede)                            │
│                                                             │
│  Mostra: etichetta sede · tipo W1–W4 · stato* · azioni      │
│  Azioni: Imposta nuova · Modifica · (Aggiorna Catalogo se   │
│          già configurata)                                   │
└───────────────────────────┬─────────────────────────────────┘
                            │ Imposta / Modifica
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  SCELTA SEDE — Passo 0 (invariato nel merito)               │
│  1 Questo telefono/tablet                                   │
│  2 PC/notebook Windows                                      │
│  3 Cloud (Google Drive)                                     │
│  4 Disco di rete / NAS                                      │
│  + campi percorso condizionati al tipo (come oggi / Nota)   │
└───────┬──────────┬──────────┬──────────┬────────────────────┘
        │          │          │          │
        ▼          ▼          ▼          ▼
     [W1]       [W2]       [W3]       [W4]
   Conferma   Procedura   Guida +    Guida +
   sede +     condizionale pick SAF  pick SAF
   pick       PC (B.3)    cartella   cartella
   locale     ──┬──
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│  VERIFICA E CONFERMA (comune)                               │
│  - probe accessibilità / scrittura dove possibile           │
│  - persistenza CatalogSeatStore                             │
│  - stati: raggiungibile | non raggiungibile | non           │
│    verificabile | non configurata                           │
│  - MAI «verificata» solo perché la cartella è stata scelta  │
└───────────────────────────┬─────────────────────────────────┘
                            ▼
                    Sede attiva + ritorno al chiamante
```

\*Stato mostrato solo se verificabile in quel momento; altrimenti «non verificabile».

### B.3 Ramo W2 (PC) — TO-BE condizionale senza menu “modalità”

Dopo Passo 0 = PC, **un solo percorso guidato** (non chiedere rapida/guidata).  
All’interno, **una domanda chiusa di contesto** (non è una seconda modalità prodotto):

> «La cartella condivisa del Catalogo è già pronta e raggiungibile da questo tablet?»  
> · **Sì** → vai a collegamento CIFS / scelta cartella + **stesse verifiche finali**  
> · **No** → sequenza preparatoria (Passi 1–4 attuali, eventualmente condensati nei testi) → CIFS → pick → verifica  

Motivazione: evita di rifare USB/script quando GeeKom/DELL hanno già la share; non crea un secondo motore.

### B.4 Dove compare la sede (TO-BE)

| Schermata | Cosa mostrare |
|---|---|
| **Dashboard** | Riga sotto sottotitolo: «Catalogo: {etichetta} · {stato}» (tap → ingresso unico) |
| **Organizza (hub)** | Stessa riga sopra Acquisisci/Aggiorna |
| **Acquisisci** | Blocco già introdotto in 0.16.52 («Catalogo di riferimento»), allineato etichette/stati comuni |
| **Aggiorna** | Riga compatta sede (non ripete l’intero blocco Acquisisci) |
| **Ingresso unico / S0** | Sede attuale + stato + Crea/Modifica (come oggi, arricchito) |

Componenti: riuso tipografia/bottoni esistenti; **non** ridisegnare Dashboard KPI.

### B.5 Errori e ritorni

- Indietro resta lineare nel ramo.  
- Fallimento verifica → stato esplicito + resta in S0/ingressoo (sede precedente resta valida finché la nuova non è verificata — già in Nota/S0).  
- CIFS: Host sempre editabile; fallimento IP auto → messaggio chiaro + inserimento manuale (mai blocco senza via d’uscita).  
- Annulla da ingresso unico → nessuna modifica.

---

## C. Matrice AS-IS / TO-BE

| Schermata / passo | AS-IS | TO-BE | Motivazione | Impatti / dipendenze |
|---|---|---|---|---|
| Dashboard | KPI senza sede | + riga sede+stato (tap) | Identificazione permanente | `HomeScreen`, snapshot/seat da `CatalogFacade` |
| Organizza hub | Solo 2 tile | + riga sede+stato | Contesto Acquisisci/Aggiorna | `MacroHubScreen` |
| Acquisisci | Blocco sede solo in 0.16.52; assente in 0.16.37 | Unificare contratto UI sede (stessi stati) | Distinguere sede vs fonti | Portare 0.16.52 in base **oppure** reimplementare minimo; `CensusViewModel` |
| Aggiorna | Solo conteggi | Riga sede compatta | Coerenza Organizza | `Aggiorna` screen / VM |
| Configura → Setup | Ingresso valido ma non unico | Punta allo **stesso** ingresso unico | Evitare procedure parallele | Route già `CatalogSeatWizard` |
| S0 Inizio | Crea/Modifica + posizione | Invariato + stato raggiungibilità | Fase 1 prompt | `CatalogSeatWizard` |
| Passo 0 W1–W4 | Presente | Conservare tutte e 4 | Nota §7.1octies | Nessuna riduzione |
| W1 | Conferma + pick | Percorso diretto (come oggi, testi snelli) | Fase 3 | Minimo |
| W3 / W4 | Guida + pick | Percorso diretto + stato verifica onesto | Fase 3 | Nessuna falsa completezza |
| W2 Passi 1–4 | Sempre obbligatori | Condizionati alla domanda «share già pronta?» | Ridurre ritorni inutili | Solo Wizard PC; stessi export/CIFS |
| W2 Passo 5 CIFS | IP auto parcheggiato; form Copia | Vedi §D | Affidabilità Host | `SetupLanAnnounce`, script, VM |
| Importa / Pulisci / Ricerca | Funzionanti | **Nessuna riprogettazione** | Vincolo prompt | Regressione da evitare |
| Scansione Wi‑Fi dispositivi | Non prodotto | **Non introdurre** | Nota + prompt | — |

---

## D. Analisi tecnica recupero IP (CIFS)

### D.1 Cosa fa oggi il codice (branch 0.16.52)

- **Componente PC:** script `CONFIGURA` / routine Windows, dopo «Tutto pronto», espone payload (`PVMSETUP1`) in **HTTP :38456** e **UDP :38455** (IP, user, share, nome PC, token).  
- **Componente app:** `SetupLanAnnounce.discover` — ascolto UDP + **scansione HTTP degli host della subnet Wi‑Fi** cercando il token; merge finché `IPv4 + User`.  
- **Momento:** da Passo 4 (attesa) e Passo 5 (form CIFS); flag `recoveringPcData`.  
- **Host CIFS:** deve essere IPv4 (`hostForCifs`); il solo nome PC **non** è accettato come Host.  
- **Non** è l’IP del tablet: è l’IP annunciato dal PC (o scoperto come peer HTTP).

### D.2 Affidabilità (esito prove)

| Aspetto | Esito |
|---|---|
| Univocità | Non garantita (PC multi‑NIC; più IPv4 in `ips`) |
| Attendibilità in campo | **Insufficiente** (KO 0.16.46–0.49: UDP parziale, User senza IP, Riprova nascosto) |
| Isolamento AP / Wi‑Fi guest | Può impedire UDP/HTTP tra PC e tablet |
| Provider CIFS | Resta app esterna; l’utente copia Host/User/Password |

**Conclusione fattibilità:** il recupero automatico è **tecnicamente possibile** come *assistenza*, non come garanzia. Oggi **non** è una base UX consolidata.

### D.3 Soluzione raccomandata (unica)

**Modello: «Assistenza IP» + Host sempre manuale editabile.**

1. **Precompilazione automatica solo se payload completo** (`isComplete`: IPv4+User) e token atteso corretto.  
2. **Host e User sempre visibili e modificabili**; Copia resta.  
3. **Riprova sempre disponibile** (anche durante attesa) — corregge KO 0.16.49.  
4. **Timeout esplicito** → messaggio: «IP non ricevuto. Digita l’IPv4 del PC (impostazioni rete Windows) oppure Riprova tenendo aperta «Tutto pronto».»  
5. Se più IPv4 nel payload: preferire quello della stessa subnet del tablet; se ambigui, **non scegliere in silenzio** — mostrare elenco breve o lasciare vuoto con suggerimento.  
6. **Non** presentare l’auto‑IP come “configurazione già riuscita”.  
7. **Non** espandere la scansione HTTP a prodotto “trova PC in LAN” / porta 445 generica (contrasta Nota: niente scan LAN). La scansione attuale resta **solo** peer‑discovery del token di setup, con timeout corto e messaggio onesto.

**Fallback primario (sempre):** digitazione / copia manuale dell’IPv4 del PC.  
**Fallback secondario (opzionale, stessa filiera):** sul PC, in «Tutto pronto», mostrare IP in grande (e/o QR testo) da ricopiare — riduce dipendenza dalla rete senza nuovo client Windows.

### D.4 Componenti coinvolti

- `WindowsCatalogSetupScript.kt` / script esportato  
- `SetupLanAnnounce.kt`  
- `CatalogSeatWizardViewModel` / `CatalogSeatWizardScreen` (Passo 5)  
- Test unitari payload/merge; prova telefono dedicata **solo dopo** sblocco approvato

### D.5 Test necessari (IP)

1. Stessa Wi‑Fi, script a «Tutto pronto»: Host precompilato correttamente.  
2. Wi‑Fi che isola client: fallimento esplicito + inserimento manuale + CIFS ok.  
3. PC con due IPv4: nessun Host sbagliato silenzioso.  
4. Durante `recoveringPcData`: **Riprova** e modifica Host usabili.  
5. Regressione: Password / Copia / pick cartella share invariati.

---

## E. Piano di intervento (post-approvazione)

### E.0 Prerequisito di processo (da decidere in §F)

Allineare il lavoro su **un solo branch di riferimento** (consigliato: integrare 0.16.52 nella base oppure basare la tranche sul tip 0.16.52). Sviluppare sulla sola 0.16.37 **senza** il blocco Acquisisci sede duplica lavoro già fatto.

### E.1 Ordine attività (dopo OK Renato)

| # | Attività | File / componenti tipici |
|---|---|---|
| 1 | Contratto UI comune `CatalogSeatStatus` (label, kind, stato enum) | domain/hub + Facade |
| 2 | Ingresso unico = S0 arricchito (stato + stesse azioni) | `CatalogSeatWizard*` |
| 3 | Riga sede su Dashboard e Organizza (tap → ingresso) | `HomeScreen`, `MacroHubScreen` |
| 4 | Allineare Acquisisci 0.16.52 al contratto comune; riga su Aggiorna | `StaticPageMocks` / Aggiorna |
| 5 | W2: domanda «share già pronta?» → skip preparatori **con stesse verifiche finali** | Wizard VM/Screen |
| 6 | IP: assistenza secondo §D (Riprova, no falsa completezza) | Announce + Wizard CIFS |
| 7 | Test automatici + **una** guida prova telefono (REGOLE §9) | test + docs guida |

### E.2 Dipendenze dirette

- Persistenza già in `CatalogSeatStore` / `CatalogHubService.seatReachable`.  
- Provider CIFS esterno invariato in v1.  
- Non toccare Importa/Pulisci/Ricerca salvo wiring sede.

### E.3 Criteri di accettazione verificabili (dopo sviluppo)

1. Da Dashboard, Organizza e Acquisisci si legge la **stessa** sede e uno stato tra: non configurata / raggiungibile / non raggiungibile / non verificabile.  
2. W1–W4 restano tutte scelte di primo livello.  
3. Nessuna scansione Wi‑Fi per elenco dispositivi.  
4. Nessun menu rapida/guidata sui rami W1/W3/W4; su W2 solo la domanda contestuale §B.3.  
5. Sede ≠ elenco fonti in Acquisisci.  
6. Host CIFS: auto solo se completo; sempre correggibile; Riprova sempre.  
7. Acquisisci→Importa→Aggiorna→Pulisci→Ricerca senza regressioni funzionali.  
8. Probe sede non dichiara “ok” senza verifica effettiva.

### E.4 Regressione

- Smoke: Conferma Acquisisci → Importa; Aggiorna 3 stati; Pulisci selezionati; Ricerca.  
- Wizard W1 pick locale; W2 pick share (IP manuale); W3/W4 pick se disponibili.  
- «Aggiorna Catalogo» con sede offline → stato non raggiungibile, Catalogo locale intatto.

---

## F. Criticità residue (solo decisioni Renato)

| # | Decisione | Perché serve Renato | Alternative tecnicamente sostenibili |
|---|---|---|---|
| **F1** | Branch di partenza: consolidare **0.16.52** nella base prima della tranche, oppure rifare il minimo sede su 0.16.37? | Evita doppio lavoro e discordanze tablet/CI | (a) merge/rebase 0.16.52 → base; (b) cherry-pick solo UI sede Acquisisci |
| **F2** | IP CIFS: adottare §D «assistenza + manuale sempre», oppure **lasciare parcheggiato** l’auto‑IP e solo ripristinare Riprova/UX Host? | Auto‑IP ha già consumato molti cicli; il prompt lo richiede di nuovo in analisi | (a) §D completo; (b) parcheggio resta, solo Host editabile chiaro + guida «trova IP su Windows» |
| **F3** | W2: introdurre la domanda «share già pronta?» (scorciatoia alle **stesse** verifiche)? | Accorcia chi ha già CIFS; rischia di saltare checklist se usata male | (a) sì come in §B.3; (b) no, sempre Passi 1–5 |
| **F4** | Estendere la riga sede anche ad **Aggiorna** oltre Dashboard/Organizza/Acquisisci? | Prompt cita le tre; Aggiorna aiuta ma aggiunge densità | (a) sì compatta; (b) solo le tre sedi del prompt |

Nessun’altra fork di prodotto in questa proposta.

---

## Checklist completezza proposta (criteri §10 prompt)

1. Coerente con Nota v5.3 H1/W1–W4 e con codice 0.16.37 + 0.16.52 esaminato — **sì** (discordanze dichiarate).  
2. Conserva W1–W4 — **sì**.  
3. Niente scan Wi‑Fi dispositivi — **sì**.  
4. Niente rapida/guidata inutile — **sì** (solo domanda contestuale W2).  
5. Sede in Dashboard, Organizza, Acquisisci — **sì**.  
6. Distingue sede e fonti — **sì**.  
7. Affronta IP CIFS concretamente — **sì** (§D).  
8. Niente sync auto / client Windows / cancellazione originali — **sì**.  
9. Non riscrive Importa/Aggiorna/Pulisci/Ricerca — **sì**.  
10. Piano test/regressione — **sì** (§E).

---

## Autorizzazione

**STOP.** Attendere revisione e **OK esplicito** di Renato (o OK con elenco puntuale di modifiche a questa proposta).  
Fino ad allora: nessun commit di codice applicativo, nessun branch di implementazione, nessun ciclo sperimentale.
