# Proposta navigazione — Setup Catalogo personale (revisione UIX)

**Data:** 18/10/2026  
**Build di riferimento (congelata lato PC):** 0.16.33 — test Renato OK, nessun KO funzionale.  
**Documenti di input:**
1. `docs/reference/Nota_operativa_Wizard_Catalogo_Cursor_e1d1.pdf` (merge Nota + guida PC) — **riferimento primario**
2. `docs/reference/Workflow_Catalogo_Cartella_Condivisa_d6dc.pdf` (artefatto Claude) — principi UX e struttura S0→A–D→F  
3. `docs/reference/Guida_wizard_cartella_condivisa.pdf` — già in evidenza  
4. Stato codice attuale Wizard (`CatalogSeatWizard*` su branch `cursor/wizard-setup-pc-c-5645`)

**Questa fase:** solo proposta. **Nessuna variazione di codice.**

---

## 1. La documentazione è sufficiente?

**Sì, è sufficiente per tarare la navigazione UIX del ramo Setup** (soprattutto PC) senza riaprire architettura, script Windows o SMB nativo.

| Area | Sufficienza | Nota |
|---|---|---|
| Principi UX (una decisione/schermata, no falsa automazione, prova reale, ripresa) | Adeguata | Allineati a ciò che già funziona in 0.16.33 |
| Struttura S0 → A–D → F | Adeguata | Da usare come scheletro di navigazione |
| Ramo B PC (USB → script → CIFS → SAF → verifica) | Adeguata | Congruente col percorso già collaudato |
| Fuori perimetro esplicito | Adeguata | Blocca bene SMB nativo, deep-link, QR, ritorno automatico |
| Warning / messaggi | Buona base | Catalogo Nota §5.6 + Claude §7: da mappare sui testi attuali, non da inventare |
| Rami C/D (Drive/NAS) | Sufficiente per *presentazione* | Dichiarare non completi; non fingere end-to-end |
| Ramo A locale | Parziale | Claude prevede rilevamento volumi; codice attuale è più semplice (conferma → SAF). Proposta: non allargare A in questa revisione |

### Lacune da chiudere in interlocuzione (non bloccano la proposta)

1. **Nomi file setup:** la Nota §5.3 cita ancora `MediaManager_Setup_Catalogo.*`; in app sono `AVVIA_Configurazione_Catalogo` / `_non_aprire_supporto_setup`. La proposta usa i nomi attuali.
2. **Quick Share:** Nota lo cita come alternativa USB; in fase congelata PC era **solo USB**. Proposta: USB obbligatorio nel percorso principale; Quick Share solo come link secondario “Non riesco” (o fuori scope fino a OK Renato).
3. **Client SMB in-app (Claude M3):** fuori perimetro Nota §11 — **non** rientra nella revisione UIX.
4. **Password più semplice:** emersa in feedback storici, non trattata nella Nota — resta nel backlog UX, non nella navigazione.
5. **BL-UX già annotati** (async, attesa script, struttura guida): la proposta li assorbe nella nuova mappa schermate.

**Verdetto:** si può procedere a una proposta di navigazione concreta; eventuali punti 1–2 vanno solo confermati da Renato prima dell’implementazione.

---

## 2. Navigazione attuale (ramo Setup) — fotografia

### Ingresso
`Configura` → Wizard · oppure `Acquisisci` senza sede → Wizard (ritorno alla origine dopo OK).

### Sequenza odierna (PC, percorso completo tipico)

| # | Step codice | Cosa vede l’utente | Problema UIX |
|---|---|---|---|
| 1 | `S0_START` | Crea / Modifica / Riprendi | Utile, ma non è la “S0 destinazione” della Nota |
| 2 | `INTRO` | Scelta dispositivo + **Avanti** | Un tocco in più; sottotitoli requisiti deboli |
| 3 | `W2_ROUTINE_PARAMS` | Path C: + nome share + Prepara | Chiede valori già defaultabili (Nota 5.4) |
| 4 | `W2_ROUTINE_READY` | File pronti **+** guida USB lunga **+** riepilogo | Troppo densa (BL-UX-3); async percepito (BL-UX-1) |
| 5 | `W2_ROUTINE_CONFIRM` | Avvia .cmd / Tutto pronto | Manca avviso pausa lunga (BL-UX-2) |
| 6 | `W2_SUMMARY` | Riepilogo senza password | Ok principio; forse fondibile con chiusura F |
| 7 | `W2_CIFS` | Password + Installa/Apri CIFS | Ok; Copia campi assente sul percorso script (Nota valuta Copia per campo) |
| 8 | `W2_PICK` | Scegli cartella + stati verifica | Ok prova reale |
| 9 | `DONE` | Conferma senza password | Ok; allineare copy a “Configurazione completata” |

Laterali: `W2_HELP`, `W2_PASSWORD_HELP`. Morti/deboli: `W2_CHOICE` (saltato), `W2_PREPARE` (manuale).  
Altri rami: `W1_CONFIRM` (locale), `W3_GUIDE`, `W4_GUIDE` — stub; da non presentare come completi.

**Conteggio tipico PC oggi:** ~9 schermate + testi densi.  
**Target Nota/Claude per PC:** 5–8 schermate, una decisione per schermata, avanzamento visibile.

---

## 3. Navigazione proposta (intero Setup)

### 3.1 Scheletro unico (come Nota)

```
[Ingresso Configura|Acquisisci]
        ↓
   E0  Modalità (Crea / Modifica / Riprendi)     ← attuale S0_START, rinominato
        ↓
   S0  Dove tenere il Catalogo?                  ← attuale INTRO, ridisegnato
        ├── A  Questo telefono/tablet
        ├── B  PC o notebook          ← priorità, percorso completo
        ├── C  Drive/cloud            ← se non completo: non avviare percorso finto
        └── D  NAS                    ← idem
        ↓ (ogni ramo)
   F   Chiusura comune: riepilogo → verifica scrittura → esito
        ↓
   Ritorno a Configura o Acquisisci
```

### 3.2 E0 — Modalità (invariata nel ruolo)

- **Crea** / **Modifica** / **Riprendi bozza** come oggi.
- Sede precedente resta attiva fino a verifica OK della nuova (Nota §7) — già coerente; va solo reso esplicito in UI Modifica.
- Origine (Configura vs Acquisisci) salvata per il ritorno.

### 3.3 S0 — Destinazione (una decisione)

Sostituisce l’attuale combo + Avanti:

| Opzione | Sottotitolo (Nota §3.1) | Comportamento |
|---|---|---|
| Questo telefono o tablet | Nessuna rete necessaria | → ramo A |
| PC o notebook | Deve essere acceso e raggiungibile; altrimenti trasferimento manuale | → ramo B |
| Google Drive / cloud | … | Solo se verificato; altrimenti “Non ancora disponibile” / Altre opzioni |
| Disco di rete / NAS | … | Idem |

- **Un tocco = avanza** (niente Avanti).
- Indicatore “Passo x di y” quando il totale del ramo è determinabile.
- Indietro sempre a E0.

### 3.4 Ramo A — Locale (impatto minimo)

Non allargare ora al rilevamento multi-volume Claude.

Proposta corta:
1. **A1** — Breve spiegazione + **Scegli cartella** (SAF), suggerimento Catalogo.
2. **F** — Riepilogo senza segreti → prova scrittura → esito.

(Se in seguito si aggiunge rilevamento volumi, A1 resta una sola schermata grazie al selettore Android.)

### 3.5 Ramo B — PC (cuore della revisione)

Obiettivo: **stessa filiera funzionale congelata**, meno attrito percettivo.

| Schermata proposta | Codice odierno da fondere/sostituire | Una decisione / un compito | Note |
|---|---|---|---|
| **B0 — Preparazione** | (nuovo, leggero) | “PC acceso, non in sospensione?” Continua / Non è raggiungibile | Se non raggiungibile → B1 USB senza scansioni infinite. Niente SMB scan. |
| **B1 — Prepara file** | `W2_ROUTINE_PARAMS` ridotto + export | “Prepara i file di setup” (default C:\MediaManager\Catalogo + share/utente **non chiesti** se fissi) | Path/share solo in “Modifica dettagli” collassabile. Allinea Nota 5.4. |
| **B2 — Copia sul PC (USB)** | parte di `W2_ROUTINE_READY` | Solo istruzioni USB corte (Solo ricarica → Trasferimento file → Download → Desktop) + “Ho copiato i due file” | Nomi: `AVVIA_…` / `_non_aprire_…`. Quick Share solo in “Non riesco”. **BL-UX-3** |
| **B3 — Esegui sul PC** | `W2_ROUTINE_CONFIRM` | Doppio clic su AVVIA_…, UAC, **avviso pausa lunga**, attendi «Tutto pronto» | **BL-UX-2**. Dichiarazione utente, non automazione. |
| **B4 — Dati per CIFS** | `W2_SUMMARY` + inizio credenziali | Mostra host/share/utente **senza password**; “Continua a CIFS” | Password solo in B5. |
| **B5 — CIFS (fuori app)** | `W2_CIFS` (+ help) | Installa/Apri CIFS; **qui** password + campi; Copia **per campo** solo se utile in prova; “Ho configurato CIFS” | Nessun ritorno automatico. Help laterale invariato nel ruolo. |
| **B6 — Scegli cartella** | `W2_PICK` | SAF via CIFS + stati verifica | Come oggi (prova reale). |
| **F — Esito** | `DONE` (+ pezzo summary) | “Configurazione completata” / errore con Riprova·Modifica·Guida | Password assente. Ritorno origine. |

**Schermate tipiche PC proposte:** B0→B1→B2→B3→B4→B5→B6→F = **8** (o 7 se B0 si fonde in B1).  
Rispetto a oggi: meno densità per schermata, stessi gate manuali, avanzamento leggibile.

#### Cosa si elimina o si nasconde nel ramo PC
- Schermata `W2_CHOICE` (script vs già condiviso) dal percorso principale; “già condiviso” → voce secondaria in B1.
- Blocco unico READY con tre contenuti sovrapposti.
- Richiesta obbligatoria di path/share se restano i default collaudati.
- Qualsiasi testo che suggerisca ritorno automatico, QR, import CIFS, scansione 445.

#### Transizioni (assorbe BL-UX-1)
- Un passo = uno stato stabile; niente aggiornare status/error sulla stessa pagina in modo che “salti” il contenuto principale.
- Dopo Prepara file: passare a B2 solo a export riuscito (oggi a volte status + corpo lunghi competono).
- Indicatori di fase espliciti: es. “PC · passo 3 di 7 — Copia file”.

### 3.6 Rami C e D

- Restano nella struttura S0.
- Se non end-to-end verificati: **non selezionabili come percorso completo** (o messaggio “Non ancora disponibile”).
- Non riusare il flusso PC come se fosse NAS.

### 3.7 Chiusura F (tutti i rami)

Unificare copy e regole:
1. Riepilogo (no password)  
2. Verifica scrittura reale (già in `configureCatalogSeat` / fascicolo)  
3. Esito + ritorno origine  

Oggi F è spezzata (SUMMARY prima di CIFS sul PC, DONE dopo). Proposta: sul PC il riepilogo “senza password” resta **prima di CIFS (B4)**; il riepilogo finale F ripete solo destinazione/cartella/esito verifica.

### 3.8 Ripresa e errori (senza cambiare archivio bozza)

- Persistenza non sensibile come oggi; dopo CIFS/SAF ripresa sulla fase corretta (Nota T13).
- Se password persa da memoria: ripetere solo B5, non tutto B.
- Warning: mappare Nota §5.6 sui casi già gestiti (CIFS assente, non raggiungibile, scrittura fallita, script non completato).

---

## 4. Mappa “attuale → proposto” (PC)

```
E0  S0_START                    →  E0  (stesso ruolo)
S1  INTRO                       →  S0  (tocco unico + sottotitoli)
—   (assente)                   →  B0  Preparazione PC
S2  W2_ROUTINE_PARAMS           →  B1  Prepara file (default, dettagli opzionali)
S3a+S3b W2_ROUTINE_READY        →  B2  Solo USB (testi corti)
S3c W2_ROUTINE_CONFIRM          →  B3  Esegui script + avviso attesa
S4  W2_SUMMARY                  →  B4  Dati senza password
S5  W2_CIFS (+ HELP)            →  B5  CIFS + password (+ help)
S6  W2_PICK                     →  B6  SAF + verifica
S7  DONE                        →  F   Esito
```

---

## 5. Impatto UIX vs efficacia/efficienza

| Levante | Effetto atteso |
|---|---|
| Una decisione / schermata | Meno confusione; meno “pagine asincrone” percepite |
| Default path/share nascosti | Meno digitazione; meno errori C: |
| USB e script separati + avviso attesa | Guida seguibile; meno abbandono nella pausa lunga |
| Password solo in B5; assente in B4/F | Coerente con test OK e Nota |
| Nessuna falsa automazione | Interlocuzione onesta; meno false aspettative |
| C/D non finti | Evita percorsi che non possono chiudere |
| Stessa filiera PC congelata | Impatto funzionale basso; rischio regressione contenuto |

**Non si tocca:** script Windows, modello sede/fascicolo, CIFS come ponte, gate Acquisisci→Setup.

---

## 6. Decisioni Renato (18/10) — congelate

1. **Quick Share:** solo in «Non riesco» (percorso principale solo USB).  
2. **Path/share:** default attuali; «Modifica dettagli» opzionale.  
3. **B0:** indifferente → **fuso in B1** (checklist «PC acceso»).  
4. **Drive/NAS in S0:** come oggi (stub).

**Template videate PC:** `docs/reference/template-videate-wizard-pc.md` + Canvas «Template videate Wizard PC».  
Attesa OK sui template prima di riprendere lo sviluppo.

---

## 7. Ordine di implementazione suggerito (dopo OK)

1. S0 destinazione + indicatore avanzamento + policy C/D  
2. Spezzare READY → B2/B3 + avviso attesa (chiude BL-UX-2/3)  
3. B1 default senza form obbligatorio  
4. Allineo copy B4/B5/F e warning Nota §5.6  
5. Passata anti-async (BL-UX-1)  
6. Prova telefono sui criteri Nota §10 (T07–T18) e accettazione bozza già nota

---

## 8. Conclusione

La **azione allegata **è sufficiente** per riprogettare la navigazione del Setup Catalogo con impatto UIX contenuto e maggiore efficacia dell’interlocuzione, **riusando la filiera PC già collaudata**.  
La proposta sopra è il piano di navigazione che intenderei seguire in implementazione, senza modifiche di codice in questa fase.
