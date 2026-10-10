# Prompt di continuità — un solo agente (Photo&VideoManager)

**Data:** 10/10/2026 (aggiornato: build correttiva 0.16.3-preview in verifica telefono)  
**Autore del handoff:** sessione Cursor + interlocuzione Renato Stefanizzi  
**Uso:** incolla questo intero documento come primo messaggio a un **unico** agente Cursor Cloud / Composer. Non spezzare in più agenti.  
**Nota avanzamento:** BL-03 portato in codice su branch `cursor/bl03-acquisisci-tre-stati-c499` (da `m02-…-e02a`); **non chiudere** senza OK Renato sul telefono.

---

## Ruolo e obiettivo

Sei l’agente unico di sviluppo su **Photo&VideoManager** (app Android, nome prodotto in UI: **MediaManager**).  
Riprendi dal branch corrente, **non inventare requisiti**, allinea Codice + Nota Integrata + prove sul telefono.  
La verifica di **0.16.2 è KO**: priorità assoluta correggere i criteri non recepiti (in particolare **BL-03**) e aggiornare roadmap/piano prima di nuove funzioni di dominio.

---

## 1. Riferimenti al progetto e regole vincolanti condivise

### 1.1 Repository e branch
- Repo: `renatizzi/PhotoVideoManager` (anche URL legacy `photovideomanager`)
- Branch di lavoro tipico: `cursor/m02-architettura-applicativa-e02a` (base PR verso `main`)
- Ultima versione codice nota al handoff: **`0.16.2-preview`** (`versionCode` 43), commit tipico `b1ffec4` e successivi
- PR associata (se presente): branch `cursor/m02-architettura-applicativa-e02a`

### 1.2 Documenti vincolanti (leggere per intero prima di modificare codice)
| Documento | Percorso | Perché |
|---|---|---|
| **Nota Integrata (documento unico)** | `docs/PVM_Nota_Integrata.md` (v5.3) | Requisiti, UI congelata §5.6, roadmap §7, backlog §7.3 |
| **Terminologia UI** | `docs/TERMINOLOGIA_UI.md` | Catalogo / File del dispositivo; conferme SÌ/NO stile BoxManager |
| **Shell parity** | `docs/SHELL_PARITY.md` | Coerenza shell con BoxManager |
| **Assessment BoxManager** | `docs/BOXMANAGER_REUSE_ASSESSMENT.md` | Pattern dialog/conferme, shell |
| **Questo prompt** | `docs/PROMPT_CONTINUITA_AGENTE.md` | Handoff e piano rinnovato |

### 1.3 Regole operative (non negoziabili)
1. **Cursor non inventa requisiti** né cambia decisioni vincolanti senza decisione esplicita di Renato / ChatGPT (Nota §1.2).
2. **Layout UI congelato** (Nota v5.3 §5.6): non riaprire struttura Home / Bottom Bar / Acquisisci→Importa / Aggiorna / Componi landing. Dietro il layout si collegano dati e si correggono comportamenti.
3. **Comunicazione con Renato (vincolante, 10/10/2026):**
   - linguaggio comprensibile a un **non addetto ai lavori**; niente gergo tecnico;
   - ogni prova sul telefono = istruzioni **passo-passo** (cosa toccare, cosa deve vedere, cosa segnalare se non torna);
   - dopo build/run: elenco concreto delle prove, non “verifica tu”.
4. **Terminologia UI (vincolante):** usare **Catalogo** (non «spazio app» / «spazio interno»); **File del dispositivo** (non «file interno»); conferme tipo «Conferma eliminazione?» + **SÌ** / **NO**.
5. **Componi / Backup / CONFIGURA** restano stub fino a specifica M11 / M14 / M15.
6. **Elimina dal Catalogo** non cancella il file del dispositivo (solo voce Catalogo → Cestino).
7. Branch naming Cloud Agent: `cursor/<nome>-e02a`; commit + push; PR via strumento dedicato ManagePullRequest (non `gh` per creare/aggiornare PR).
8. Git: non amend/force senza richiesta; un commit logico per cambiamento.

### 1.4 Stack (solo per orientamento tecnico interno)
Kotlin, Jetpack Compose, Room, navigazione a shell Home / Organizza / Componi / Utility / Impostazioni.  
Facade: `CatalogFacade`. Flusso Acquisisci → Importa → Aggiorna.  
**Nelle risposte a Renato non usare questi termini** se evitabili; spiega in italiano semplice.

---

## 2. Esame attento della Nota e della documentazione di questa sessione

### 2.1 Checklist obbligatoria all’avvio (prima di scrivere codice)
- [ ] Leggere `docs/PVM_Nota_Integrata.md` §1.2, §5 (soprattutto §5.4.1 Acquisisci/Aggiorna, §5.4.3 Ripristina/Cestino, §5.5, §5.6), §7.1, §7.3 backlog
- [ ] Leggere `docs/TERMINOLOGIA_UI.md` per intero
- [ ] Leggere BL-03 in §7.3 **alla lettera** (3 stati; **niente icona cestino separata**)
- [ ] Confrontare l’UI attuale di Acquisisci (`StaticPageMocks.kt` / `AcquisisciStaticScreen`) con BL-03: se c’è icona cestino separata, è **errore di recepimento**
- [ ] Verificare messaggi Elimina / Cestino / Ripristina rispetto a TERMINOLOGIA_UI e stile BoxManager
- [ ] Aggiornare §7.1 e backlog con lo stato reale dopo ogni correzione accettata

### 2.2 Documentazione prodotta / aggiornata in sessione (10/10/2026)
- `docs/TERMINOLOGIA_UI.md` — **nuova**, legenda termini e conferme
- `docs/PVM_Nota_Integrata.md` — regole comunicazione/terminologia; §7.1 aggiornato; BL-05/06/08 chiusi; BL-09/10 aggiunti; stato verifica 0.16.2
- Codice 0.16.0–0.16.2: menu Aggiorna, export, Cestino raggruppato, Ripristina landing, filtri «Spazio interno app», ecc.
- **Attenzione:** parte di 0.16.2 (icona cestino in Acquisisci) **contraddice** BL-03 già scritto in Nota: non trattarla come requisito consolidato.

### 2.3 Cosa è consolidato vs backlog
- **Consolidato / da rispettare:** UI congelata v5.3; flusso Acquisisci→Importa unico; KPI Catalogo; Elimina non tocca file dispositivo; Ripristina = Cestino + Backup; terminologia; comunicazione semplice.
- **Backlog (non inventare chiusura):** BL-01, BL-02, BL-03 (**urgente post-KO**), BL-04, BL-07, BL-09, BL-10.
- **Chiusi in backlog (con riserva di non regressione):** BL-05, BL-06, BL-08.

---

## 3. Stato di avanzamento del progetto (verifica 0.16.2 = KO)

### 3.1 Cosa funzionava già (HOLD chiuso, convalida Renato su 0.15.x)
- KPI Dashboard / «di cui in Catalogo» allineati ad Aggiorna
- Acquisisci → Importa → navigazione ad Aggiorna; CONTINUA se già in Catalogo
- Nascondere «Spazio interno app» dall’elenco fonti (BL-05)
- Pulisci duplicati esatti + Cestino di base

### 3.2 Cosa è stato tentato in 0.16.0–0.16.2
| Area | Intent | Esito verifica Renato |
|---|---|---|
| Menu Aggiorna: Rinomina / Elimina / Copia su dispositivo / Modifica (stub) | OK di direzione | Da ri-verificare dopo fix P0 |
| Conferma Elimina stile BoxManager + precisazione file dispositivo / Cestino | OK di direzione | Da ri-verificare |
| Cestino: una riga per elemento; Ripristina completa | Correzione bug doppia riga | Da ri-verificare |
| Ripristina landing: Cestino + Backup (stub) | Conforme Nota §5.4.3 | Da ri-verificare |
| Terminologia Catalogo / File del dispositivo | Conforme legenda | Da ri-verificare in tutte le stringhe residue |
| **Acquisisci: rimozione fonti** | In 0.16.2 aggiunta **icona cestino** | **KO** — viola BL-03 |

### 3.3 Motivo ufficiale del KO (vincolante)
Renato: il test dell’ultima implementazione è **KO** perché **non sono stati recepiti**:
1. i **criteri di selezione fonti a 3 stati** (BL-03):  
   - **selezionato** = ✓ nel riquadro  
   - **non selezionato** = riquadro vuoto  
   - **escluso / rimosso** = **X nel riquadro stesso**  
   - **vietato** usare un’icona cestino separata per togliere la fonte  
2. **altri requisiti già convalidati** (allineamento messaggi/terminologia/comportamenti attesi in verifica) non pienamente rispettati o regressivi rispetto all’accordo.

Quindi: **0.16.2 non è accettata** come chiusura della tranche. La prossima release deve prima correggere il P0.

### 3.4 Chiarimenti di dominio già dati a Renato (non riaprire senza motivo)
- **Elimina dal Catalogo** → non cancella il file del dispositivo; va nel Cestino; ripristinabile.
- **Scansione dispositivi:** oggi solo cartelle aggiunte con **+** (es. telefono, Google Drive). Tablet / PC Wi‑Fi / SSD sul router **non** si scoprono da soli → BL-09.

---

## 4. Rivisitazione della Roadmap alla luce delle urgenze e del backlog

### 4.1 Macro-attività (Tabella 1 Nota) — lettura operativa
Le macro M01–M20 restano in **SPECIFICA CONSOLIDATA** a livello di Nota; lo sviluppo “dietro UI congelata” continua sulla fetta M02–M07 / M09 / M16 / M18.  
**Non** aprire M11/M14/M15 (Componi/Backup/CONFIGURA reali) senza nuova specifica ChatGPT.

### 4.2 Urgenze (superano l’ordine “vertical slice completo”)
| Priorità | Voce | Motivo |
|---|---|---|
| **P0** | **BL-03** selezione 3 stati in Acquisisci | KO esplicito; requisito già in Nota; implementazione 0.16.2 sbagliata |
| **P0** | Ripristino conformità ai requisiti convalidati in verifica (senza cestino separato; messaggi/terminologia; Cestino/Ripristina/menu) | Blocca accettazione 0.16.x |
| **P1** | **BL-10** fonti non scelte (Eraser/Facebook) | Fiducia sull’Acquisisci |
| **P1** | **BL-04** multi-selezione / esclusione in Importa | Evita import indesiderati |
| **P2** | **BL-07** Sposta (serve decisione ChatGPT/Renato) | Non implementare a indovinare |
| **P2** | **BL-09** scansione rete/tablet/SSD | Serve specifica; oggi solo + |
| **P2** | **BL-01** Pulisci anche file spurî | Serve criteri |
| **P3** | **BL-02** ombre Home | Cosmetico, layout congelato |

### 4.3 Cosa declassare / non fare ora
- Non proseguire con nuove feature Componi/Backup reali.
- Non aggiungere altre icone/azioni in Acquisisci che aggirano il riquadro a 3 stati.
- Non dichiarare “vertical slice completo” finché P0 non è OK in prova telefono.

---

## 5. Piano rinnovato (da eseguire in questa continuità)

### Fase A — Allineamento documentale (subito, prima o insieme al fix)
1. Confermare in Nota §7.1 lo stato **VERIFICA KO 0.16.2** (già impostato al handoff; aggiornare dopo il fix).
2. Portare BL-03 da “backlog passivo” a **lavoro corrente P0** (criteri di accettazione sotto).
3. Elencare eventuali stringhe UI ancora non conformi a `TERMINOLOGIA_UI.md` e correggerle nella stessa tranche.

### Fase B — Fix P0 Acquisisci (BL-03) — criteri di accettazione
**Obiettivo UI (parole semplici per Renato):**  
In Acquisisci, ogni riga fonte ha **un solo riquadro** che può essere:
- con la spunta (**selezionata** → entra in Conferma/Importa);
- vuoto (**non selezionata** → resta in elenco ma non si usa);
- con la **X** (**tolta dall’elenco** / esclusa — non icona cestino a parte).

**Criteri tecnici di completamento (agente):**
1. Rimuovere l’icona cestino separata introdotta in 0.16.2 su `AcquisisciStaticScreen` / `SourceRow`.
2. Introdurre stato a tre valori (es. SELECTED / NOT_SELECTED / EXCLUDED) coerente con BL-03; ciclare nel riquadro (o interazione chiara documentata).
3. Definire e implementare effetto di **X / escluso** su:
   - presenza in elenco Acquisisci;
   - cosa succede al Catalogo già censito da quella fonte (proposta allineata a Nota: niente cancellazioni distruttive automatiche dei file dispositivo; chiarire se si nasconde solo la fonte o si ripulisce il Catalogo — **se ambiguo, chiedere a Renato in italiano semplice con 2 opzioni**, non inventare).
4. Conferma / Importa usano solo le fonti in stato selezionato.
5. Nessuna regressione su CONTINUA / Importa / KPI.
6. Version bump (es. **0.16.3-preview**), test automatici + istruzioni prova telefono passo-passo.
7. Aggiornare Nota §7.1 e BL-03 (stato: in verifica / fatto solo dopo OK Renato).

### Fase C — P0 collaterale (stessa o immediata tranche successiva)
- Verificare che Cestino mostri **una riga** per foto/video e che Ripristina non lasci residui.
- Verificare menu Aggiorna: Modifica (messaggio in preparazione) / Rinomina / Copia su dispositivo / Elimina (Conferma eliminazione? SÌ/NO + testo legenda).
- Verificare pagina Ripristina: due porte (Cestino e Backup in preparazione).
- Passare le stringhe residue ancora “spazio app/interno”.

### Fase D — P1 dopo OK telefono su P0
1. **BL-10:** capire perché Eraser/Facebook sono apparsi; prevenire; istruzioni a Renato per ripulire elenco con il nuovo 3° stato.
2. **BL-04:** multi-selezione / esclusione in Importa (senza rompere layout congelato).

### Fase E — P2 solo con decisione/specifica
- BL-07 Sposta (serve scelta: togliere vs spostare copia fisica).
- BL-09 rete/tablet/SSD (specifica M04/SMB).
- BL-01 Pulisci spurî; BL-02 ombre.

### Fase F — Comunicazione e chiusura tranche
Dopo ogni build consegnabile:
1. Elenco prove telefono numerato (A/B/C…) in italiano semplice.
2. Cosa deve vedere; cosa segnalare se non torna.
3. Non dichiarare OK finché Renato non convalida esplicitamente il P0.

---

## Istruzioni operative immediate per l’agente

1. `git status` / branch; lavorare su `cursor/m02-architettura-applicativa-e02a` (o nuovo `cursor/...-e02a` se richiesto dalle regole Cloud).
2. Eseguire la checklist §2.1.
3. Implementare **Fase B (BL-03)** senza scorciatoie (niente cestino separato).
4. Compilare e testare (`./gradlew :app:testDebugUnitTest :app:assembleDebug`).
5. Commit + push; aggiornare PR.
6. Aggiornare Nota §7.1 / BL-03.
7. Rispondere a Renato **solo** in linguaggio semplice, con prove telefono dettagliate.

### Prove telefono minime (da adattare dopo il fix — esempio)
**A — Tre stati in Acquisisci**  
1. Organizza → Acquisisci  
2. Su una cartella: tocca il riquadro fino a vedere ✓, poi vuoto, poi X (o il ciclo documentato)  
3. **Non** deve esserci icona cestino separata  
4. Con la X la cartella non deve più essere usata come selezionata; comportamento elenco come da specifica  

**B — Conferma / Importa**  
1. Lascia selezionate solo le cartelle volute (✓)  
2. CONFERMA → Importa deve mostrare solo quelle  

**C — Elimina / Cestino**  
1. Aggiorna → ⋮ → Elimina → «Conferma eliminazione?» SÌ/NO  
2. Cestino: una sola riga; Ripristina la fa sparire dal Cestino  

---

## Cosa non fare
- Non riaprire il layout congelato.
- Non implementare Sposta / rete SMB / Backup reale / editing Modifica completo senza specifica.
- Non sostituire il riquadro a 3 stati con icone cestino o menu nascosti.
- Non chiudere BL-03 come “fatto” senza convalida Renato sul telefono.

---

*Fine prompt di continuità. Un solo agente; priorità P0 = BL-03 + ripristino conformità post-KO 0.16.2.*
