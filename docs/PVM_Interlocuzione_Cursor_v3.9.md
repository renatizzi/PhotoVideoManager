
# 7.26 Feedback Cursor — Proposta routine PC Windows (account dedicato SMB)

**14 ottobre 2026 — Feedback tecnico. Nessuno sviluppo. Nessuna modifica autonoma alla Nota.**  
Documento utente: proposta integrata configurazione automatica Catalogo su PC Windows (PDF). Allineato a H1 / Wizard BL-17 W2 già in 0.16.20.

## 7.26.1 Giudizio complessivo

**Direzione funzionale: condivisa e corretta.** L’account Windows dedicato + cartella/condivisione ad hoc è la risposta giusta al fallimento ripetuto dell’autenticazione con account Microsoft/Gmail personale. Va integrata nel ramo **W2** del Wizard, non come percorso parallelo.

I **7 principi** (responsabilità utente, trasparenza, account dedicato, password provvisoria, privilegi minimi, verifica effettiva, compatibilità) sono **compatibili** con Nota §3.1 / H1 e con i vincoli «niente operazioni nascoste / niente configurazione valida se incompleta».

## 7.26.2 Cosa c’è già nel codice (riuso)

| Esigenza proposta | Stato attuale |
|---|---|
| Wizard sede W1–W4 | `CatalogSeatWizard*` — W2 = guida manuale SMB + `OpenDocumentTree` |
| Sede + fascicolo schede H1 | `CatalogHubService` / `catalog_hub.json` |
| Verifica lettura/scrittura sede | `HubPackageIo.probeWritable` dopo tree URI |
| Registrazione fonti media | `SourceRegistry.registerSafFolder` (dopo SAF) |
| Auth SMB in-app | **Assente**: `AuthPort` = `LocalTrustAuthPort` (no-op); accesso rete solo via **provider SAF** (es. Archivio Samsung) |
| Client / routine Windows | **Assente** nel repository |

## 7.26.3 Feedback per fase

### Fase 1 — Censimento dispositivi
- **Utile** come elenco «dove ho i media» + scelta Master.
- **Non** confondere con il censimento file di Acquisisci (`CensusService`).
- **Riuso:** alias dispositivo (`SourceLabelStore`) e tipi sede già in combo W1–W4; eventuale lista dispositivi Master = **nuovo** piccolo modello UI/prefs, non duplicare lo scan media.

### Fase 2 — Scelta Master e percorso
- Allineata a W2 + precisazione già consolidata (C: / USB / microSD sul PC).
- Il percorso scelto sul tablet **non** può essere applicato direttamente sul PC: va **passato alla routine Windows** (parametro nello script) o riscelto sul PC con default proposto.

### Fase 3 — Autorizzazione e trasferimento routine
- Android **non può** creare utenti/share sul PC da remoto.
- **USB come unico canale: sconsigliato** (MTP vs mass storage, OTG, permessi). Il collegamento fisico **non** prova la configurazione (d’accordo con la proposta).
- **Alternativa raccomandata (T1):** il Wizard genera/scarica uno **script PowerShell** (o piccolo `.exe` firmato in seguito) con parametri (percorso, nome share, utente `MediaManager`, password provvisoria generata dall’app). L’utente lo copia sul PC (USB **oppure** download/email) ed esegue **Esegui come amministratore** una volta, dopo conferma esplicita.
- **Alternativa T2 (più tardi):** app companion Windows — fuori perimetro v1 della routine.

### Fase 4 — Routine Windows (fattibile)
Script elevato, **idempotente**:
1. Crea cartella se assente (non sovrascrive contenuti senza chiedere).
2. Crea utente locale `MediaManager` se assente; se esiste, riusa (opzione «reset password provvisoria» solo su conferma).
3. Share SMB + ACL NTFS **solo** su quell’utente; no guest; no «Everyone».
4. Abilita solo regole firewall strettamente necessarie (File and Printer Sharing profilo Privato), se mancanti.
5. Log a schermo attività OK/KO; file esito non sensibile (JSON) sul Desktop o nella cartella Catalogo.
6. Password **non** nei log; mostrata una volta a fine setup + istruzione a cambiarla.

**Windows target v1 proposto:** Windows 10/11 Home e Pro (PowerShell 5.1+).

### Fase 5 — Ritorno al Wizard Android
- Non esiste un «callback» automatico affidabile PC→app.
- **Meccanismo raccomandato:** (a) l’app genera password/token prima dello script; (b) utente conferma «Ho eseguito la routine sul PC»; (c) Wizard passa a Fase 6 con credenziali già note all’app per la guida; (d) esito script = file `MediaManager_setup_result.json` (senza password) che l’utente può anche ignorare se conferma manuale.

### Fase 6 — Connessione SMB su Android — punto critico
Oggi MediaManager **non parla SMB**: apre il selettore cartelle e usa il **provider di sistema** (Archivio). Le credenziali restano in quell’app, non in MediaManager.

| Opzione | Significato | Raccomandazione |
|---|---|---|
| **S6-A** | Wizard guida Archivio/SMB con utente `MediaManager` + password provvisoria (nota a schermo); poi `OpenDocumentTree` sulla share; collaudo con `probeWritable` | **v1 consigliata** — riusa architettura; risolve il problema credenziali personali |
| **S6-B** | Client SMB nativo in MediaManager + `AuthPort` + Keystore | Più «tutto in app», ma nuova dipendenza, permessi, manutenzione; **dopo** se S6-A non basta |

La richiesta «soluzione utilizzabile dal codice, non solo istruzione» in v1 si soddisfa con: parametri noti + avvio intent dove possibile + **verifica obbligata** via SAF/hub dopo la scelta cartella (già nel codice). Non implica necessariamente uno stack SMB proprietario.

### Fase 7 — Collaudo e registrazione sede
- Allineato a H1: sede valida solo dopo probe lettura/scrittura fascicolo.
- Distinguere: raggiungibilità / auth (lato provider) / accesso cartella / R/W hub.
- Fallimenti → ripresa dal passo giusto, non «tutto da capo» se account/share già ok.

### Password successive (§4 proposta)
- Con **S6-A**: percorso guidato «Cambia password sul PC → aggiorna credenziali in Archivio → Riprova in MediaManager»; la sede (tree URI) resta.
- Con **S6-B**: aggiornamento in Keystore via AuthPort.
- Obiettivo: non perdere il riferimento sede (già vero se non si cancella la registrazione seat).

## 7.26.4 Piano tecnico di implementazione (proposto, non avviato)

| Step | Contenuto |
|---|---|
| 1 | Estendere solo il ramo **W2** del Wizard: sotto-flusso «Configura PC con routine» vs «Ho già la share» |
| 2 | Generatore script PowerShell + schermata parametri (percorso, share name, user, password temp) |
| 3 | Trasferimento: **Scarica script** (+ opzione «copia su USB» se volume montato); no dipendenza obbligatoria da USB |
| 4 | Dopo conferma utente: guida S6-A → Scegli cartella → `configureCatalogSeat` |
| 5 | Schermata «Cambia password MediaManager» (guida) |
| 6 | Test accettazione allineati ai 12 criteri della proposta (reinterpretati con S6-A) |

## 7.26.5 Decisioni ancora necessarie (Renato / ChatGPT)

1. **Trasferimento routine:** accetti **T1 (script scaricabile / copiabile)** come canale v1, con USB opzionale?
2. **SMB su Android:** accetti **S6-A** (provider sistema + account dedicato + verifica in-app) per v1, lasciando S6-B come evoluzione?
3. **Fase 1 censimento dispositivi:** la vuoi **obbligatoria prima** della scelta sede, o la lista Master resta la combo W1–W4 attuale con dettaglio percorso solo se W2?

## 7.26.6 Stato

**FEEDBACK_TECNICO — in valutazione.** Nessuno sviluppo fino a conferma di T1/S6-A (o alternative scelte).

Fine feedback Cursor — §7.26 — PVM_Interlocuzione_Cursor_v3.9
