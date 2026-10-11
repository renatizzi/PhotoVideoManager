# Photo&VideoManager — NOTA INTEGRATA

Documento tecnico, architetturale e funzionale di riferimento (**documento unico**).

- **File:** PVM_Nota_Integrata_v5.3 (fonte operativa: `docs/PVM_Nota_Integrata.md`)
- **Versione:** 5.3
- **Data ultima modifica:** 10/10/2026
- **Responsabile:** Renato Stefanizzi
- **Delta v5.1 → v5.2:** recepita la revisione di navigazione (proposta Navigazione11 + interlocuzione 08/10/2026). Aggiornati §5 e chiarimenti di collocazione in §6. Nessun nuovo requisito di dominio inventato.
- **Delta v5.2 → v5.3:** **congelamento layout UI** anteprima `0.14.6-preview` (Dashboard densità BoxManager; Accesso rapido Home fissato; albero §5 confermato). Nessun nuovo requisito di dominio. Affinamenti estetici minori (es. spessore ombre) ammessi in seguito senza riaprire la struttura.
- **HOLD 0.15.4–0.15.12 chiuso (09–10/10/2026):** Renato ha convalidato KPI/Importa/navigazione; ripresa sviluppo dietro UI congelata da **0.16.0**.

# 1. Scopo del progetto e regole operative
## 1.1 Scopo
REQUISITI: Photo&VideoManager è un'applicazione Android per la gestione dell'archivio personale e familiare di fotografie e video. E’ concepita per:
Censire fotografie e video distribuiti tra più sorgenti.
Raccogliere e consolidare i contenuti in un Archivio Condiviso configurabile.
Individuare e gestire duplicati e contenuti simili.
Ottimizzare con semplici funzioni di editing foto e video
Costruire un Catalogo logico indipendente dalla collocazione fisica dei file.
Organizzare, cercare, raggruppare e comporre contenuti.
Consentire la condivisione all'interno della famiglia.
Produrre album, PDF, stampe ed esportazioni.
Proteggere i contenuti attraverso backup e meccanismi di recupero.
## 1.2 Regole per la implementazione con Cursor
- La progettazione concettuale viene completata prima della roadmap tecnica definitiva e dell'implementazione.
- ChatGPT definisce analisi, requisiti, decisioni architetturali, criteri di accettazione e piano di lavoro.
- Cursor viene coinvolto sulla base di una specifica consolidata e, quando richiesto, per approfondimenti tecnici separati dalla Nota Integrata.
- Cursor non deve inventare requisiti, modificare decisioni vincolanti, introdurre backend/cloud obbligatori o modificare l'architettura senza una decisione esplicita.
- **Comunicazione con Renato (vincolante, 10/10/2026):** linguaggio sempre comprensibile a un non addetto ai lavori; niente gergo tecnico. Per ogni prova dell’app sul telefono: istruzioni passo-passo dettagliate (cosa toccare, cosa deve apparire, cosa segnalare se non torna).
- **Terminologia UI (vincolante, 10/10/2026):** legenda in `docs/TERMINOLOGIA_UI.md` — usare **Catalogo** (non «spazio app»), **File del dispositivo** (non «file interno»); conferme stile BoxManager (`Conferma …?` + **SÌ** / **NO**).
- **Metodo agenti (vincolante, 11/10/2026):** di norma **un solo agente operativo** per tranche di codice. Se durante lo sviluppo l’agente ritiene utile attivare **più agenti** (es. analisi / revisione / implementazione separati), deve **dirlo esplicitamente a Renato e attendere conferma** — non avviare multi-agente di propria iniziativa.
- Il lavoro procede per macro-attività: obiettivi → input → decisioni → output → criteri di completamento.
- Le idee che emergono fuori dal percorso corrente vengono registrate nel Backlog e non interrompono la macro-attività in corso.
- Evitare divagazioni, fughe in avanti, assunzioni non autorizzate, duplicazioni e regressioni concettuali.
- ChatGPT fornisce a Cursor una specifica consolidata e vincolante e fa confluire nella Nota Integrata esclusivamente le decisioni consolidate.
- Cursor viene coinvolto quando la specifica della macro-attività è sufficientemente definita e quando è necessario un approfondimento tecnico.
- Per ogni macro-attività sono definiti obiettivo, input necessari, decisioni, output e criteri di completamento.
# 2. Modello concettuale di riferimento
## 2.1 Modello
REQUISITI: Il modello concettuale di Photo&VideoManager si basa sul seguente processo logico:
ARCHIVI E SORGENTI DISPONIBILI → ACQUISIZIONE/CENSIMENTO E CONFRONTO → AGGIORNAMENTO DEL CATALOGO LOGICO → ORGANIZZAZIONE / RICERCA / COMPOSIZIONE / PUBBLICAZIONE / GESTIONE → EVENTUALE CONSOLIDAMENTO NELL’ARCHIVIO CONDIVISO.
VINCOLI: Nessuna funzione deve introdurre operazioni distruttive automatiche sui contenuti originali.
## 2.2 Legenda — terminologia convenzionale
File fisico
Singolo file effettivamente presente su una specifica sorgente o unità di storage, identificabile attraverso la propria collocazione fisica e le proprie caratteristiche tecniche. Più file fisici possono rappresentare copie dello stesso contenuto logico.
Archivio personale
Insieme logicamente definito di contenuti personali e delle relative copie fisiche che appartengono alla sfera personale di un utente e che sono gestiti autonomamente dall'utente stesso. Può risiedere su uno o più dispositivi, dischi, cartelle o altri storage disponibili.
Archivio Condiviso
Archivio esplicitamente destinato alla gestione e alla condivisione di contenuti appartenenti al dominio familiare dell'applicazione. Il passaggio di un contenuto dal dominio personale al dominio familiare avviene a seguito di una decisione esplicita dell'utente di condividere/consolidare il contenuto nell'Archivio Condiviso. Il salvataggio verificato della copia nell'Archivio Condiviso costituisce la ratifica operativa del passaggio al dominio familiare.
Catalogo logico
Rappresentazione strutturata e persistente delle informazioni relative ai contenuti e alle loro copie, indipendente dalla collocazione fisica dei file. Il Catalogo può raccogliere informazioni provenienti da più archivi e sorgenti, anche appartenenti a dispositivi diversi, mantenendo distinta l'identità logica del contenuto dalle sue copie fisiche. Il Catalogo è persistito localmente sul dispositivo che lo gestisce; può tuttavia contenere informazioni relative a contenuti e copie presenti su più archivi e sorgenti, anche appartenenti a dispositivi diversi, quando tali informazioni sono state acquisite o rese disponibili nell'ambito delle operazioni effettuate dall'utente.
Relazioni organizzative
Informazioni che collegano i contenuti del Catalogo a persone, luoghi, eventi, album, tag, categorie e ad altre strutture utilizzate per organizzarli, ricercarli, raggrupparli, comporli o pubblicarli. Le relazioni organizzative non implicano necessariamente modifiche o duplicazioni dei file fisici.
# 3. Architettura logica e principi funzionali
REQUISITI: I file fisici, gli archivi/sorgenti, il Catalogo logico e le relazioni organizzative devono essere concettualmente separati. L’Archivio Condiviso è uno degli archivi gestiti e non deve essere assunto come passaggio obbligatorio del modello generale.
L'accesso ai diversi tipi di archivio/storage deve essere mediato da uno StorageAdapter che espone capacità esplicite e non necessariamente uniformi tra i diversi tipi di storage. La progettazione tecnica deve definire la matrice di capacità e vincoli almeno per:
storage locale/filesystem;
MediaStore/SAF Android;
CIFS/SMB/NAS;
storage/cloud accessibili tramite provider;
eventuali ulteriori adapter futuri.
Le principali capability comprendono almeno LIST, READ, WRITE, CREATE_DIRECTORY, RENAME, MOVE, DELETE, RANDOM_READ, SEQUENTIAL_READ, METADATA, FINGERPRINT_STREAM e AVAILABILITY. Per ogni adapter devono inoltre essere valutati disponibilità offline, gestione degli errori, performance, integrità e recuperabilità. Non devono essere assunte capacità uniformi né atomicità di move/write su SAF o SMB/CIFS. Quando una capability manca, l'operazione deve degradare in modo esplicito e sicuro.
UI: Home, navigazione, schermate, componenti e dialoghi.
Presentation: ViewModel e stato della UI.
Domain: media, Catalogo, acquisizione, deduplicazione, concetti logici di archivio e copia, ricerca, condivisione, album, backup e gestione. Il termine “storage” nel Domain indica esclusivamente il concetto logico di archivi, sorgenti e copie; l’accesso fisico ai file e ai dispositivi/storage è esclusivamente responsabilità degli appositi Port/StorageAdapter.
L'accesso fisico agli archivi e alle sorgenti è mediato da uno StorageAdapter che espone capacità esplicite e non necessariamente uniformi tra i diversi tipi di storage. Le capability devono essere verificate prima dell'esecuzione delle operazioni che le richiedono.
Le principali capability dello StorageAdapter comprendono almeno LIST, READ, WRITE, CREATE_DIRECTORY, RENAME, MOVE, DELETE, RANDOM_READ, SEQUENTIAL_READ, METADATA, FINGERPRINT_STREAM e AVAILABILITY. L'assenza di una capability deve essere gestita esplicitamente e in modo sicuro, senza assumere capacità uniformi tra filesystem, SAF/MediaStore, SMB/CIFS e altri adapter.
Data: database locale, filesystem locale, storage Archivio Condiviso, SMB/CIFS e sorgenti/provider supportati.
VINCOLI: La progettazione concettuale precede la scelta definitiva di classi, package, librerie e implementazioni.
CONTRATTI ARCHITETTURALI CONSOLIDATI:
- Il Domain non accede direttamente a path, filesystem, SMB/NAS o altre tecnologie fisiche; l'accesso allo storage avviene esclusivamente attraverso Port/StorageAdapter.
- I permessi logici dell'app sono separati dall'accesso fisico allo storage.
- Il PermissionGate gestisce l'autorizzazione dell'utente rispetto alle operazioni applicative; lo StorageAdapter gestisce l'accesso fisico e le capability della sorgente/storage; l'AuthPort costituisce la porta astratta per l'eventuale autenticazione richiesta dalla risorsa, senza introdurre autenticazione obbligatoria nella versione local-first.
- Le permission del sistema operativo restano distinte dai tre livelli precedenti.
- È predisposto un AuthPort sostituibile, senza autenticazione obbligatoria nella versione local-first.
- I confini Domain/Application/Adapter/Port devono preservare l'evoluzione futura verso client desktop, backend e storage remoti senza dipendenze obbligatorie.
- È possibile predisporre una SyncPort come estensione architetturale futura. La presenza della porta non costituisce una funzionalità di sincronizzazione dell'attuale versione di Photo&VideoManager e non autorizza code, retry o sincronizzazioni automatiche. Il protocollo e la relativa implementazione sono fuori scope v1 e potranno essere definiti solo a seguito di una futura decisione progettuale.
## 3.1 Principi architetturali vincolanti
Local-first: l'app deve poter funzionare senza backend, Internet, cloud o NAS.
L'architettura deve consentire la gestione di più archivi e sorgenti indipendenti. L’Archivio Condiviso è configurabile e opzionale e non costituisce una dipendenza obbligatoria né un archivio privilegiato rispetto agli altri archivi gestiti dall'applicazione. NAS/CIFS/SMB è supportato come possibilità di condivisione dell'archivio, non come dipendenza obbligatoria.
Il cloud è opzionale e deve essere trattato come categoria generica di sorgenti/storage, non come sinonimo di Google Drive.
Non è previsto un backend proprietario obbligatorio.
I dati personali, le fotografie e i video non devono essere inviati a terze parti per riconoscimento o classificazione; privilegiare elaborazioni locali/on-device.
Archivio Condiviso e Backup sono concetti distinti.
Nessuna sincronizzazione proprietaria obbligatoria tra dispositivi: ogni dispositivo mantiene autonomamente il proprio archivio personale/Catalogo locale. Photo&VideoManager facilita il confronto, la pulizia e l’acquisizione/consolidamento di contenuti scelti dall’utente. La condivisione nell'Archivio Condiviso avviene solo per scelta esplicita dell’utente; eventuali sincronizzazioni native del sistema operativo o di altre app possono produrre copie che Photo&VideoManager dovrà saper riconoscere e gestire.
L'indisponibilità temporanea di un archivio o di una sorgente non costituisce di per sé un errore funzionale dell'applicazione. Le funzioni che non dipendono dalla risorsa indisponibile devono rimanere utilizzabili; le operazioni che la richiedono devono essere bloccate o non avviate con un'indicazione esplicita dello stato della risorsa.
Non introdurre code automatiche di operazioni differite.
Prima di ogni funzione complessa devono essere valutati privacy, sicurezza, integrità, offline, conflitti, performance, storage, compatibilità, manutenzione, recuperabilità e scalabilità.
## 3.2. Separazione file fisici, Catalogo e relazioni
REQUISITI: Il Catalogo deve descrivere i contenuti senza confondere il file fisico con l'identità logica del contenuto.
Un MediaItem rappresenta il contenuto logico. Il dominio di appartenenza del contenuto è una proprietà del MediaItem e non della singola MediaCopy. Un contenuto può quindi essere rappresentato da più MediaCopy appartenenti a sorgenti o archivi differenti, mantenendo un'unica identità logica e un unico dominio di appartenenza.
Il passaggio del MediaItem dal dominio PERSONAL al dominio FAMILY deve essere modellato come effetto della condivisione/consolidamento esplicitamente deciso dall'utente. La presenza della copia fisica nell'Archivio Condiviso, una volta completato e verificato il consolidamento, ratifica tale passaggio, ma non costituisce di per sé una nuova identità del contenuto né una nuova proprietà riferita alla sola MediaCopy.
Le copie fisiche devono poter essere rappresentate separatamente.
Devono essere tracciabili origine, collocazione, fingerprint/hash e metadati.
Le relazioni con persone, luoghi, eventi, album, tag e membri familiari sono dati del Catalogo.
## 3.3 Modello concettuale dei dati
REQUISITI: Le entità concettuali, le relazioni e le invarianti definite nella presente Nota costituiscono il riferimento vincolante per l’implementazione del modello dati. Lo schema tecnico, la cardinalità, la persistenza, gli indici, le migrazioni e la tecnologia del database sono definiti nell’implementazione di M03, nel rispetto del modello concettuale e delle relative invarianti.
MediaItem, Photo, Video
MediaCopy / StorageLocation
MediaFingerprint
Person, Place, Event
Album, Tag, Category
FamilyMember, Share
Backup
ScanSession, ImportSession, OperationLog
Il modello deve distinguere tra identità logica del contenuto, copie fisiche e dominio di appartenenza. Il dominio (PERSONAL / FAMILY) è riferito al MediaItem. La MediaCopy descrive invece una specifica copia fisica e la relativa collocazione, senza costituire autonomamente un'entità di proprietà familiare.
MediaItem.domainScope rappresenta il dominio logico di appartenenza del contenuto:
- PERSONAL: contenuto appartenente alla sfera personale dell'utente;
- FAMILY: contenuto esplicitamente condiviso/consolidato nel dominio familiare.
La transizione PERSONAL → FAMILY è un effetto dell'operazione di condivisione/consolidamento e non una conseguenza automatica della semplice esistenza di una MediaCopy nell'Archivio Condiviso.
StorageLocation rappresenta nel Catalogo la collocazione concreta di una MediaCopy mediante un riferimento logico/opaco al livello Domain; non rappresenta direttamente il filesystem, il path o la tecnologia fisica sottostante.
ArchiveRef rappresenta il riferimento logico all'archivio cui appartiene una StorageLocation. La sorgente rappresenta invece una risorsa effettivamente disponibile e selezionabile nell'ambito di una specifica operazione. ArchiveRef, sorgente e StorageLocation devono rimanere concettualmente distinti anche nella progettazione tecnica.
La disponibilità di un archivio o di una sorgente non coincide con la disponibilità delle singole MediaCopy in esso collocate. Il Catalogo può conservare informazioni relative a copie precedentemente censite anche quando la relativa sorgente non è attualmente raggiungibile.
VINCOLI: Lo schema tecnico definitivo, la cardinalità, la persistenza, gli indici, le migrazioni e la tecnologia del database sono definiti nell’ambito di M03, nel rispetto del modello concettuale e delle relative invarianti.
# 4. Dispositivi fruibili
REQUISITI: L'app deve poter censire sorgenti eterogenee.
Smartphone/tablet.
PC e dischi locali.
Dischi esterni.
NAS/storage di rete tramite SMB/CIFS.
Cloud/provider supportati, senza rendere obbligatorio uno specifico provider.
Le operazioni che richiedono il confronto o l'acquisizione di contenuti devono operare sulle sorgenti effettivamente disponibili nella sessione corrente.
Flusso concettuale:
l'utente attiva esplicitamente l'operazione;
l'applicazione individua e analizza le sorgenti attualmente disponibili;
l'applicazione presenta all'utente le sorgenti individuate;
l'utente seleziona dispositivi, cartelle o archivi da coinvolgere nell'operazione;
l'utente avvia esplicitamente l'operazione.
La disponibilità delle sorgenti deve essere verificata nell'ambito della sessione corrente. Una sorgente può risultare AVAILABLE, UNAVAILABLE o UNKNOWN. L'indisponibilità della sorgente non comporta la perdita delle informazioni già presenti nel Catalogo e non costituisce un errore funzionale. Non devono essere generati automaticamente retry, code di operazioni o sincronizzazioni differite.

# 5. Navigazione e struttura UI

**STATO:** albero navigazione consolidato il 08/10/2026 (v5.2); **layout UI congelato** il 09/10/2026 (v5.3, build `0.14.6-preview`). I requisiti di dominio restano sostanzialmente inalterati rispetto a v5.1; cambia solo collocazione UI (v5.2) e densità/struttura visuale Home (v5.3).

## 5.0 Principi

- Una funzione ha un solo punto di accesso principale.
- Le voci sotto i riquadri hub indicano **funzionalità da gestire**, non necessariamente voci di menu.
- I passi di workflow (Seleziona, Importa, Modifica, Allinea, …) non costituiscono livelli di navigazione.
- Home, Top Bar e Bottom Bar restano componenti globali.
- CONFIGURA resta distinta dalle macro operative.
- Accesso rapido in Home: stesso modello già definito; può variare solo il numero di link. Non è un secondo sistema di navigazione.
- Selezione elenchi: per **Acquisisci/Aggiorna** vale il riquadro a **3 stati** (BL-03 / decisione 10–11/10/2026); altri elenchi restano on/off salvo diversa specifica.

## 5.1 Home Page

La Home Page è la pagina di atterraggio e il principale hub operativo.

Contiene:
- KPI di sintesi (due colonne Foto / Video, densità tipografica allineata a BoxManager; senza card separate per ogni metrica);
- **Ricerca nel Catalogo** — motore unico, **un solo riquadro**, valido per foto/video e per album/raccolte; lo stesso motore è riusabile in tutte le pagine dove è prevista la ricerca;
- **Accesso rapido** (congelato v5.3): **Acquisisci**, **Aggiorna**, **Crea**, **Edita**, **Backup**, **Ripristina**. Griglia 2×3 a tessere che riempiono lo spazio restante del viewport. Non duplica le macro già presenti in Bottom Bar. Non include Archivio Condiviso né scorciatoie a macro Bottom Bar.

**Semantica KPI Home (requisito condiviso — modello A; BL-14 in 0.16.8, ritocco Renato 0.16.9):**
- **Foto/Video originali** = file conosciuti sulle **fonti dispositivo** (copie non-personali **ACTIVE**), e comunque **≥ Catalogo** (se un elemento è solo nello spazio app resta contato). **Il Cestino app non conta** (non è la cartella Cestino del telefono).
- **di cui in Catalogo** = elementi con almeno una copia **ACTIVE** (= elenco **Aggiorna**);
- **Momento di ingresso in Catalogo:** **CONFERMA**, non Importa.
- Con ingresso alla CONFERMA e Cestino escluso, dopo un censimento completo i due numeri tendono a coincidere; un gap utile richiederebbe censimento senza catalogazione immediata (valutare in **BL-13**).

La navigazione mantiene la distinzione tra Home, macrofunzioni e CONFIGURA. Home **senza scrolling** sul viewport tipico.

Le schermate devono mantenere coerenza con struttura e principi UI già consolidati in BoxManager (shared shell), adattando branding e contenuti PVM.

La UI deve gestire esplicitamente almeno: stato iniziale; caricamento; contenuto vuoto; operazione in corso; completamento; errore; indisponibilità della sorgente/storage; richiesta di autorizzazione; conferma delle operazioni distruttive.

**VINCOLI:**
- La UI non deve introdurre comportamenti funzionali non definiti nelle rispettive macro-attività.
- Top Bar e Bottom Bar devono rimanere componenti globali.
- Operazioni su sorgenti/storage indisponibili: mostrare lo stato senza retry automatici.
- Funzioni sensibili: privacy e sicurezza come definiti nella Nota.
- Accessibilità, leggibilità, navigazione coerente e gestione dello stato UI.

## 5.2 Top Bar

**REQUISITI:** struttura e comportamento come shared shell BoxManager:
- Identità dell’app e versione;
- Indicazione dell’utente;
- Data e ora;
- Accesso alla Guida;
- Controllo Light/Dark Mode.

**VINCOLI:** componente globale, non duplicata nelle singole schermate.

**STATO FUNZIONALE:** FUNZIONE CONGELATA. Adattamenti PVM limitati a branding, risorse grafiche, dimensionamenti e contenuti coerenti con gli slot definiti (dettaglio grafico in M18).

## 5.3 Bottom Bar

**REQUISITI (v5.2):** Bottom Bar globale con destinazioni:

- **Home** — ritorno alla Home Page;
- **Organizza** — hub Catalogo;
- **Componi** — hub composizioni (include Pubblica come funzione);
- **Utility** — backup, ripristino, spazio, revisione sorgenti;
- **Impostazioni** — accesso a CONFIGURA (solo icona, senza etichetta testuale).

**VINCOLI:**
- Non inserire scorciatoie aggiuntive oltre a queste destinazioni.
- Non riportare in Bottom Bar la macro «Pubblica» (è funzione di Componi).
- Il nome ufficiale della quarta macro operativa è **Utility** (non «Gestisci»).

> Nota storica v5.1: Bottom Bar solo Home + Impostazioni. La presente revisione, convalidata in interlocuzione, aggiorna la shell UI per fluidità operativa; i requisiti di dominio non cambiano nel merito.

## 5.4 Albero delle macroaree (v5.2)

### 5.4.1 ORGANIZZA

Ambito: Catalogo logico.

**Acquisisci** — alimentazione del Catalogo da fonti esterne. **Unico processo** (censimento e acquisizione non sono separati in menu):
1. pagina **Acquisisci**: elenco fonti (dispositivo + percorso), **riquadro selezione a 3 stati** (BL-03: ✓ selezionata · vuoto non usata · **X** tolta dall’elenco — niente icona cestino separata), riepilogo → **Conferma**;
2. pagina **Importa** (schermata successiva del medesimo processo, non voce hub): selezione file → Importa → esito → **Aggiorna** (Catalogo).

Niente chip/passi di navigazione «1. Fonti / 2. Importa» come livelli di menu.

**Aggiorna** — gestione del Catalogo. Elenco sempre visibile; **riquadro a 3 stati** (stesso criterio di Acquisisci, default diverso):
- default **deselezionato** (riquadro vuoto) per consentire l’uso dei filtri Tutti/Foto/Video sull’elenco completo;
- ciclo: vuoto → ✓ selezionata → **X** = Elimina dal Catalogo → Cestino (conferma SÌ/NO; file del dispositivo intatti);
- menu contestuale ⋮: Modifica, Rinomina, Copia su dispositivo (**senza** voce Elimina — ridondante rispetto alla X);
- **regola multiselezione + menu (convalidata 11/10/2026, opzione A):** il menu ⋮ agisce **solo sulla riga** aperta; la selezione ✓ multipla serve a **Pulisci**, non al menu.
Include:
- **Pulisci** — doppia funzione (convalidata 11/10/2026; implementazione a tranche): (1) eliminazione degli elementi **selezionati** (✓) in Aggiorna; (2) analisi/eliminazione **duplicati** secondo criteri (v1: esatti; estensioni in BL-11). UI duplicati: un solo sottotitolo, bottone **Analizza duplicati**, niente box di aiuto sparsi (*fatto testi 0.16.5*);
- **Allinea** — controllo/explore da app delle sorgenti.

**Mappatura ex-voci v5.1 sotto Organizza:**
- **Archivia** (come hub): non presente. L’Archivio Condiviso è un disco/sorgente individuabile in scansione; setup in CONFIGURA; operazioni di protezione in Utility.
- **Raggruppa** (come hub): non presente. La selezione/raggruppamento degli elementi avviene nel flusso di Componi.
- **Ricerca**: motore unico (vedi §5.1), non voce hub obbligatoria.

### 5.4.2 COMPONI

Creazione, gestione e produzione di album e raccolte (uniche due tipologie: album fotografico; raccolta di foto e video).

Funzioni hub: **Crea**, **Edita**.

**Pubblica** è funzione interna a Componi (non macro né voce Bottom Bar) e consente: stampa; condivisione interna (Archivio Condiviso); condivisione esterna (social / terze parti).

Editing delle singole foto/video di Catalogo: raggiungibile dal menu contestuale di Organizza → Aggiorna e da Componi. Editing delle composizioni: in Componi.

### 5.4.3 UTILITY

Sostituisce in navigazione l’etichetta «Gestisci».

- **Backup** — backup manuale dell’Archivio Condiviso; salvataggi contestuali; non è sincronizzazione.
- **Ripristina** — ripristino da backup e recupero dal **Cestino**.
- **Revisione / Elimina** — svecchiamento delle sorgenti (non deduplica).
- **Verifica spazio** — controllo spazio nel workflow.

**Distinzione obbligatoria:**
- **Pulisci** (Organizza → Aggiorna) = pulizia Catalogo: elementi selezionati (✓) **e** duplicati (criteri); non è svecchiamento sorgenti;
- **Revisione/Elimina** = svecchiamento sorgenti (Utility);
- **Cestino** = solo recupero di elementi eliminati involontariamente dal Catalogo e dall’Archivio Condiviso (**unico ingresso** via Utility → Ripristina; non da Pulisci né da scorciatoie in Aggiorna).

«Salva» della v5.1 corrisponde a Backup / Ripristina / spazio in Utility (non è una quarta voce hub).

### 5.4.4 CONFIGURA

Rami:
- **Impostazioni** — preferenze generali;
- **Sicurezza e Privacy**;
- **Setup archivio condiviso** — configurazione tecnica dell’Archivio Condiviso;
- **Accessi all’Archivio** — individuazione di chi è autorizzato ad accedere all’Archivio Condiviso (membri e amministratore). Sostituisce l’etichetta «Famiglia» per evitare fraintendimenti;
- **Parametri Play Store** — in scope (app gratuita; bonus/distribuzione in continuità con BoxManager).

## 5.5 Stati UI

Come in v5.1: INITIAL, LOADING, EMPTY, IN_PROGRESS, COMPLETED, ERROR, SOURCE_UNAVAILABLE, NEEDS_AUTHORIZATION, CONFIRM_DESTRUCTIVE.

## 5.6 Congelamento layout UI (v5.3)

**STATO FUNZIONALE:** layout delle schermate di anteprima **congelato** a partire da MediaManager `0.14.6-preview` (commit di riferimento densità Dashboard).

**Cosa è congelato (non modificare senza decisione esplicita di Renato):**
- Bottom Bar: Home · Organizza · Componi · Utility · Impostazioni (solo icona);
- Home: struttura sezioni (titolo, KPI a colonne, ricerca, Accesso rapido 2×3) e densità complessiva;
- Acquisisci → Importa: unico processo, azioni in alto, riquadro selezione fonti (3 stati BL-03 nello stesso slot), niente chip di passo;
- Componi landing: elenco Album/Raccolte + azioni +Album/+Raccolta/⋮;
- Aggiorna (layout): ricerca + elenco + Pulisci in alto (Allinea = refresh).

**Cosa resta liberamente evolutivo dietro il layout:**
- collegamento a ViewModel/servizi di dominio (dati reali al posto dei mock);
- stati LOADING/EMPTY/ERROR e messaggi;
- affinamenti estetici minori (ombre, radius, padding ± pochi dp) senza cambiare gerarchia o contenuti delle sezioni;
- CONFIGURA e feature ancora stub (Crea/Edita/Pubblica/Backup/…) quando M11/M14/M15 saranno in sviluppo.

**VINCOLO per Cursor:** dopo il congelamento, lo sviluppo procede **senza riaprire la struttura UI**; eventuale feedback estetico si raccoglie in backlog e si applica a tranche dedicate.

# 6. Macrofunzioni principali

Le macrofunzioni di dominio (Censimento, Acquisizione, Archivio Condiviso, Deduplicazione, Organizzazione, Ricerca, Editing/Produzione, Backup, …) restano definite nel merito come in v5.1.

**Recepimento navigazione v5.2 (chiarimenti di collocazione, non nuovi requisiti di dominio):**
- Censimento + acquisizione/importazione: percorso UI unico Organizza → Acquisisci (due fasi).
- Pulisci (selezionati ✓ + duplicati): UI sotto Organizza → Aggiorna; non sotto Utility.
- Cestino: recupero involontario; UI sotto Utility → Ripristina.
- Condivisione familiare / accessi: CONFIGURA → Accessi all’Archivio (membri + amministratore); setup tecnico in Setup archivio condiviso.
- Pubblicazione/social/produzione output: funzione Pubblica in Componi.
- Backup/spazio: Utility.

## 6.1 Analisi preventiva dei rischi
REQUISITI: Prima di introdurre una funzione significativa deve essere analizzato il suo impatto almeno sui seguenti aspetti:
Privacy.
Sicurezza.
Integrità e rischio di perdita dati.
Funzionamento offline.
Conflitti e copie.
Performance.
Spazio di archiviazione.
Compatibilità Android/filesystem/SMB.
Manutenibilità e dipendenze.
Recuperabilità.
Scalabilità.
VINCOLI: La complessità tecnica non deve essere eliminata automaticamente se una funzione offre valore significativo; deve essere valutata rispetto a valore, rischio, privacy e costo di manutenzione.
## 6.2 Classificazione delle funzionalità
Ogni funzionalità dovrà essere classificata, quando il progetto entrerà nella fase di pianificazione, come:
CORE
SUPPORTO
AVANZATA
OPZIONALE
BACKLOG
VINCOLI: Le funzionalità non necessarie alla macro-attività corrente non devono interrompere il percorso di progettazione; vengono registrate nel Backlog.
Dalla mappa dell’albero di navigazione si evincono le principali macro funzioni dell’app che costituiscono comunque una prima bozza progettuale dell’app:
## 6.3 Censimento (SUPPORTO)

> Il dettaglio esteso di §6.3 e seguenti della v5.1 resta in vigore nel merito funzionale; in caso di conflitto sulla sola collocazione UI prevale il §5 v5.2.

# Roadmap delle macro-attività

I nomi di navigazione UI seguono il §5 v5.2 (Utility, Accessi all’Archivio, Pubblica in Componi). La sequenza delle macro-attività di dominio resta quella della roadmap.
- La presente sezione costituisce lo strumento unico di monitoraggio dello stato del progetto. La roadmap integra analisi, approfondimenti tecnici e sviluppo, mantenendo separati i rispettivi ruoli di ChatGPT e Cursor.
- Per ogni macro-attività ChatGPT definisce requisiti, vincoli, decisioni progettuali, criteri di accettazione e punti da approfondire. Cursor, prima dello sviluppo, analizza le criticità tecniche indicate, evidenzia eventuali ulteriori criticità rilevanti e propone soluzioni tecniche compatibili con la specifica consolidata. Lo sviluppo inizia solo dopo la chiusura della fase di approfondimento della relativa macro-attività.
- Come precisato, Cursor non può trasformare autonomamente una proposta tecnica in requisito consolidato.
- Stati: DA AVVIARE; ANALISI; SPECIFICA CONSOLIDATA; SVILUPPO; VERIFICA; CHIUSA.
## 7.1 FASE CORRENTE: verifica BL-14 KPI Home + backlog
MACRO-ATTIVITÀ DI RIFERIMENTO: M02 + vertical slice M03–M07 / M09 / M16 (v1) + **M18 layout congelato (v5.3)**.
STATO ATTUALE (12/10/2026): **0.16.19-preview** (versionCode 60). **BL-17:** Catalogo condiviso scelto a monte tramite **wizard** (non presupporre NAS/cloud; target = foto su telefono/tablet/notebook). Se sede = PC, stesso wizard guida la condivisione. Vedi §7.1sexies. In attesa convalida struttura wizard.
Ordine operativo:
1. **P1** — consolidare wizard BL-17 (§7.1sexies) con Renato/ChatGPT; poi specifica + sviluppo.
2. **P2** — BL-09 resta ramo del wizard «PC», non percorso unico; BL-02/BL-11 non ora.
3. **Componi** — non riprendere finché non chiusa specifica M11 vs landing congelata.

### 7.1quater Decisioni Renato 11/10/2026 (questionario)

| # | Tema | Decisione | Note |
|---|---|---|---|
| 1 | BL-16 Sync | **A** — solo schede Catalogo (metadati), non i file | Provider e conflitti ancora da chiudere |
| 2 | BL-07 Sposta | **A** — non aggiungere | Chiuso |
| 3 | BL-01 Scarti | **A** — sezione «Possibili scarti» in Pulisci | **SVILUPPO 0.16.17** |
| 4 | BL-11 Simili | **A** — restano solo duplicati esatti | Non ora |
| 5 | BL-02 Ombre | **A** — lasciare ora; poi stile BoxManager | Non ora |
| 6 | BL-09 Rete | **A** — restare sul «+»; approfondimento subito | Vedi sotto |
| 7 | PC in Wi‑Fi | **A** — cartella condivisa sul PC… | **Riaperta 12/10:** troppo difficile in prova reale → §7.1sexies |

### 7.1sexies Pivot — Catalogo condiviso a monte + wizard (target basso profilo) — 12/10/2026

**Correzione Renato (sera 12/10):** non tarare la soluzione sul contesto personale (NAS / cloud). Il **target di basso profilo** conserva le foto su **dispositivi mobili** (telefono, tablet, notebook che funge da desktop). Non presupporre NAS né cloud.

**Obiettivo:** aiutare l’utente a **decidere dove tenere il Catalogo condiviso** (sede unica a monte). I dispositivi si collegano a quella sede; non si “espongono” uno per uno senza guida.

**Mezzo:** un **wizard** in app che:
1. spiega in parole semplici le sedi possibili;
2. fa scegliere **dove** vive il Catalogo condiviso;
3. se la scelta è **PC/notebook**, **continua nello stesso wizard** e guida passo-passo la configurazione della condivisione (non lasciare l’utente solo in Archivio Samsung / impostazioni Windows).

| Ramo wizard (proposta) | Per chi | Cosa fa il wizard |
|---|---|---|
| **W1 — Questo telefono/tablet** | Foto già qui; un solo dispositivo per ora | Imposta Catalogo su questo dispositivo; spiega come aggiungere altri dispositivi dopo |
| **W2 — PC / notebook** | Notebook = archivio principale | Guida: crea cartella Foto → condividi su Windows → stessa Wi‑Fi → collega da Archivio/SMB → scegli cartella in MediaManager |
| **W3 — Google Drive (o simile)** | Chi già usa il cloud foto | Guida a scegliere la cartella Drive con **+** (opt-in, non obbligatorio) |
| **W4 — Disco di rete / NAS** | Chi ce l’ha già | Guida breve al selettore «Disco di rete» (ramo avanzato, non default) |

**Default consigliato in UI (da convalidare):** evidenziare **W1** o **W2** come tipici del target; W3/W4 secondari («Altre opzioni»).

**Rapporto con C1/C2:** la sede scelta dal wizard = Archivio/Catalogo a monte (C1). Sync schede tra dispositivi (C2 / BL-16) resta evoluzione successiva, non bloccante il wizard v1.

**Fuori percorso:** assumere che tutti abbiano NAS; obbligare il cloud; lasciare SMB senza guida wizard.

**Prossimo passo:** Renato convalida i rami W1–W4 (o li riduce); poi specifica testi + schermate → sviluppo.

### 7.1quinquies Approfondimento BL-09 — PC / NAS in rete (senza cloud obbligatorio)

**Decisione Renato (11/10/2026):** acquisire dal **PC in Wi‑Fi** tramite **cartella condivisa** (soluzione A), **senza vincolarsi al cloud**. Requisito: percorso operativo **comprensibile anche a utenti di basso profilo**.

**Perché non “magia Wi‑Fi”:** il tablet non apre da solo il disco del PC. Il PC deve **mostrare una cartella** in rete; il tablet la sceglie una volta con **+** (come per il NAS).

**Modello operativo da progettare (facile):**
1. **Una tantum sul PC** — condividere *una* cartella foto (es. «Foto famiglia»), con passi minimi e linguaggio semplice (guida in Guida / dialog in Acquisisci).
2. **Una tantum sul tablet** — stessa Wi‑Fi → in Acquisisci un ingresso chiaro tipo «Da PC o rete» che spiega in 3 righe e apre il selettore cartelle (stesso meccanismo del **+**).
3. **Uso quotidiano** — CONFERMA / Importa come oggi; PC acceso e in rete solo quando acquisisci da lì.

| Opzione UX in app | Cosa fa | Pro | Contro |
|---|---|---|---|
| **U1 — Decisione Renato + SVILUPPO 0.16.18** | Pulsante «Da PC o rete» + dialog 3 passi, poi «Scegli cartella» = stesso OpenDocumentTree del **+**. | Adatto a utenti poco esperti; niente cloud; riusa il **+** | Serve ancora condivisione Windows/NAS una tantum |
| **U2** | Solo testo in Guida | Zero UI nuova | Non scelta |

**Quando si attiverà l’aiuto in rete (R1 vs R2):** preferire **R1** (aiuto al selettore / provider già presenti), non scanner LAN (**R2**).

**Cosa non fare:** obbligo cloud; sync automatica dal PC; salvataggio password di rete senza specifica AuthPort; scansione aggressiva della Wi‑Fi.

### 7.1bis Checkpoint urgente — Catalogo unico utente vs dispositivi (11/10/2026)

**Domanda Renato:** il Catalogo è unico per utente (non per dispositivo); la struttura regge un unico Catalogo alimentato dai dispositivi/fonti selezionati?

| Aspetto | Stato |
|---|---|
| **Modello di dominio** | **Sì.** `MediaItem` = identità logica unica; `MediaCopy` + `StorageLocation`/`ArchiveRef` = copie su fonti/dispositivi diversi. Un solo Catalogo raccoglie più sorgenti scelte dall’utente (§2 / §3.2). |
| **Implementazione attuale (v1 local-first)** | **Sì, sul dispositivo che esegue l’app.** Room persiste un Catalogo locale; Acquisisci aggiunge cartelle SAF da telefoni/Drive/disco; Aggiorna elenca l’universo unico. Alias dispositivo e path distinguono le fonti in UI. |
| **Catalogo “roaming” tra più telefoni dello stesso account** | **Non ancora.** Nota §3.1: nessuna sync proprietaria obbligatoria; ogni installazione ha il proprio Catalogo locale. Porte `AuthPort` / `SyncPort` predisposte ma non operative. «Utente locale» in Top Bar ≠ account cloud. |
| **Vincolo distruttività** | Eliminazione automatica degli originali **vietata** (§1 VINCOLI). Oggi Elimina/Cestino/Pulisci toccano solo il Catalogo (e copie nello spazio app). Cancellazione file in sorgente = solo con scelta esplicita → **BL-15**. |

**Assicurazione onesta:** la struttura **è adatta** a un Catalogo unico per l’utente *di questa installazione*, alimentato da tutti i dispositivi/fonti che seleziona. Non posso assicurare oggi un Catalogo cloud unico che segua l’utente su più installazioni senza una futura tranche sync/account.

### 7.1ter Analisi tecnica BL-16 — Sync Catalogo (evoluzione Archivio Condiviso) — 11/10/2026

**Stato:** ANALISI (Cursor). **Non è specifica consolidata.** Nessuna implementazione sync finché ChatGPT/Renato non chiudono i punti aperti.

| Tema | Proposta tecnica (da consolidare) |
|---|---|
| Inquadramento | Evoluzione dell’**Archivio Condiviso** (M04/M15), non un silo parallelo: riuso `ArchiveRef` / `StorageLocation` / capability; dominio **PERSONAL** (Catalogo utente) distinto da **FAMILY** finché M13/M15 non unificano. |
| Cosa sincronizzare (ipotesi) | **Metadati Catalogo** (MediaItem, relazioni, fingerprint, esclusioni) su area personale cloud opt-in. **File fisici** restano sulle fonti; copie Importa/Backup restano locali salvo scelta esplicita. |
| Porte già predisposte | `SyncPort` (oggi disabilitata), `AuthPort` (fiducia locale). Estendere solo dopo specifica; niente code/retry automatici (§3.1). |
| Local-first | L’app resta usabile offline; sync opt-in e non blocca Acquisisci/Aggiorna/Pulisci. |
| Conflitti | Da decidere: last-write-wins vs merge per campo vs «dispositivo di verità». |
| Provider | Da decidere: Drive personale / altro; cifratura a riposo sì/no in v1. |
| Perimetro v1 sync (proposta) | Solo metadati PERSONAL; niente FAMIGLIA; niente cancellazione remota automatica degli originali. |
| Dipendenze macro | M03 (modello), M04 (storage), M13 (identità se FAMILY), M15 (backup), M17 (privacy). |

**Decisione Renato 11/10/2026:** sincronizzare **solo le schede** (opzione A), non i file delle copie Importa.

**Ancora da chiudere prima dello sviluppo:** provider (es. Drive personale), cifratura sì/no, modello conflitti, rapporto con Backup.

Documenti di sessione: `docs/TERMINOLOGIA_UI.md`; prompt di continuità `docs/PROMPT_CONTINUITA_AGENTE.md`.
Nota: Le dipendenze riportate per ciascuna macro-attività della Roadmap devono indicare esclusivamente le dipendenze dirette da macro-attività precedenti, ossia quelle il cui output costituisce un input necessario alla macro-attività corrente. Le dipendenze transitive non devono essere ripetute.
Regola di aggiornamento della roadmap
La tabella costituisce l'unico riferimento operativo per lo stato di avanzamento. Non viene creato un file Excel parallelo come fonte di riferimento. Ogni decisione consolidata, criticità emersa, modifica dello stato o chiusura di una macro-attività deve essere recepita nella Nota Integrata.
Una macro-attività può passare a SVILUPPO solo dopo che ChatGPT ha consolidato requisiti e criteri di accettazione e che Cursor ha completato l'approfondimento tecnico preliminare. Le criticità che richiedono una decisione progettuale vengono riportate nella Nota e risolte prima dell'avvio dello sviluppo interessato
## 7.2 Roadmap aggiornata al 01/10/2026

## 7.3 Backlog — future implementazioni (non consolidato)

Elenco di idee/esigenze emerse in interlocuzione, **non ancora requisiti consolidati**. Non interrompono lo sviluppo corrente; da recepire in specifica quando ChatGPT le valorizza.

| ID | Area | Nota | Origine |
|---|---|---|---|
| BL-01 | **Pulisci — possibili scarti** | **Decisione A + SVILUPPO 0.16.17:** sezione in Pulisci; criteri v1 = nome tipico di scarto (thumbs/albumart/folder…) o file &lt; 2 KB; conferma → Cestino; default non tocca il file del dispositivo (BL-15). | Renato 09–11/10/2026 |
| BL-02 | Estetica Home | **Decisione A:** lasciare ora; in seguito ombre stile BoxManager (senza riaprire layout). | Renato 09–11/10/2026 |
| BL-03 | **Acquisisci — selezione fonti a 3 stati** | **OK** prova Renato 0.16.15. Acquisisci: riquadro **vuoto ↔ X** (✓ solo con Tutti); Aggiorna resta vuoto→✓→X; X = Elimina→Cestino; niente cestino separato. | Renato 09–11/10/2026 |
| BL-04 | **Eliminazione multi-selezione** | **OK** 0.16.15: deselezione multi in Importa; deselezionati scartati dal Catalogo; conteggio «nuovi selezionati». | Renato 09–11/10/2026 |
| BL-05 | **Acquisisci — nascondere «Spazio interno app»** | ~~Pleonastica in elenco fonti~~ — *fatto 0.16.0* (già filtrata in Acquisisci; nascosta anche in CONFIGURA fonti). | Renato 09/10/2026 |
| BL-06 | **Aggiorna — menu contestuale «Rinomina»** | ~~Aggiungere Rinomina~~ — *fatto 0.16.0* (insieme a Elimina→Cestino e Copia su dispositivo). | Renato 09/10/2026 |
| BL-07 | **Aggiorna — «Sposta» nel menu contestuale** | **CHIUSA (decisione A 11/10/2026):** non aggiungere Sposta. | Renato 09–11/10/2026 |
| BL-08 | **Copia da Catalogo → dispositivo** | ~~Export esplicito SAF CreateDocument~~ — *fatto 0.16.0* (menu Aggiorna «Copia su dispositivo»; preferisce copia spazio app). Sposta resta BL-07. | Renato 09/10/2026 |
| BL-09 | **PC / NAS in rete** | U1 in app 0.16.18–19. **12/10:** SMB PC→tablet troppo difficile → **deprioritata**; pivot Archivio unico §7.1sexies. | Renato 10–12/10/2026 |
| BL-17 | **Wizard sede Catalogo condiviso** | Target basso profilo (foto su telefono/tablet/notebook). Wizard decide dove vive il Catalogo; se PC → guida condivisione nello stesso flusso. §7.1sexies (W1–W4). Non presupporre NAS/cloud. | Renato 12/10/2026 |
| BL-10 | **Fonti apparire senza scelta utente** | **OK** (diagnosi + dedupe SAF 0.16.10; prove 0.16.15). Fonti solo da «+»; residui togliibili con X. | Renato 10–11/10/2026 |
| BL-11 | **Pulisci — criteri duplicati avanzati (tipo Krokiet)** | **Decisione A 11/10/2026:** restano solo duplicati esatti. Simili = versione successiva. | Renato 11/10/2026 |
| BL-12 | **Pulisci — doppia funzione (selezionati + duplicati)** | Convalidato 11/10/2026. **0.16.6–0.16.7**: (1) ✓ da Aggiorna → Pulisci; (2) Analizza duplicati. Fix navigazione ✓ in 0.16.7. Prova Renato OK su selezione. | Renato 11/10/2026 |
| BL-13 | **Home — revisione KPI** | Tenere per ora il modello A (originali=dispositivi · di cui in Catalogo=Catalogo). **In seguito**, con feedback concreto sul *peso del gap* in uso reale, valutare revisione/sostituzione dei KPI Home (senza obbligo di mantenere il gap). Non fare ora. | Renato 11/10/2026 |
| BL-14 | **Home — allineare KPI al modello A** | **0.16.8–0.16.9**: di cui in Catalogo = Aggiorna; originali = fonti ACTIVE (no Cestino app) e ≥ Catalogo; ingresso = **CONFERMA**. **OK** prova Renato 11/10/2026. | Renato 11/10/2026 |
| BL-15 | **Pulisci — opzione elimina originali in sorgente + anti re-import** | **OK** prove 0.16.11–0.16.15 (punti 5–9). Checkbox sorgente; esclusioni anti re-import; default solo Catalogo. | Renato 11/10/2026 |
| BL-16 | **Sync Catalogo personale — evoluzione Archivio Condiviso** | **Decisione A:** solo schede/metadati. Analisi §7.1ter. **Manca ancora:** provider, cifratura, conflitti, Backup. No codice sync. | Renato 11/10/2026 |

Fine documento

=== TABLE 1 ===
ID | Macro-attività | Obiettivo / perimetro | ChatGPT — analisi e requisiti | Cursor — approfondimenti prima dello sviluppo | Cursor — sviluppo | Stato
M01 | Roadmap e pianificazione | Definire e consolidare il percorso progettuale, le dipendenze, il metodo di lavoro e i criteri di completamento delle macro-attività. | Roadmap, metodo, dipendenze e criteri di avanzamento. La fase concettuale è conclusa; la Nota Integrata corrente è consolidata. | Verifica preliminare di fattibilità generale e dipendenze tecniche; la verifica tecnica preliminare di Cursor ha escluso blocchi all'avvio di M02. | Nessuno. | SPECIFICA CONSOLIDATA
M02 | Architettura applicativa | Tradurre i vincoli della Nota in architettura implementabile. | Criticità e dipendenze; Domain/Application/Adapter/Port, PermissionGate e AuthPort predisposto. | Criticità, dipendenze, performance, manutenzione e compatibilità. | Struttura tecnica del progetto. | SPECIFICA CONSOLIDATA
M03 | Modello dati e Catalogo | Definire il modello dati e il Catalogo logico: identità dei contenuti, dominio PERSONAL/FAMILY, copie fisiche, sorgenti e archivi logici, StorageLocation, metadati, fingerprint, relazioni strutturali, persistenza, consistenza, migrazioni e scalabilità. / Dipendenze: M03 -> M04, M05, M06, M07, M08, M09, M10, M11, M12, M15, M16. | Entità, relazioni, invarianti, significato e requisiti del Catalogo logico. / Modello MediaItem / MediaCopy / StorageLocation / MediaFingerprint / ArchiveRef; dominio PERSONAL / FAMILY e transizione PERSONAL → FAMILY; cardinalità, invarianti, persistenza, consistenza, migrazioni, indici e criteri di scalabilità. | Persistenza, cardinalità, indici, scalabilità, consistenza, migrazioni e criticità tecniche. / Verifica tecnica della separazione tra identità logica, copie fisiche, collocazioni concrete e archivi logici. | Modello dati, persistenza e infrastruttura del Catalogo. | SPECIFICA CONSOLIDATA
M04 | Storage e Archivio Condiviso | Definire Archivio Condiviso, archivi e sorgenti, StorageLocation, capability degli adapter, disponibilità e comportamento offline. / Dipendenze: M04 -> M13, M15, M16, M17; / M03 -> M04 utilizza il modello di ArchiveRef/StorageLocation e gli stati di disponibilità. | Modello Archivio/Sorgente/StorageLocation; contratto StorageAdapter; capability matrix; stati AVAILABLE/UNAVAILABLE/UNKNOWN; distinzione tra disponibilità dello storage e disponibilità delle MediaCopy; comportamento offline; assenza di retry, queue e sincronizzazione automatica. | Eventuali criticità tecniche residue relative a filesystem, SAF/MediaStore, SMB/CIFS, capability, disponibilità, errori di accesso e recuperabilità. | Accesso e gestione storage. | SPECIFICA CONSOLIDATA
M05 | Censimento sorgenti | Individuare e analizzare i media senza modificare le sorgenti. / Dipendenze: M03 -> M05 aggiorna il Catalogo sulla base del censimento. | Workflow di censimento; disponibilità delle sorgenti; individuazione media; metadati; origine; fingerprint; identità logica e copie fisiche; idempotenza; gestione dei censimenti completi, parziali e interrotti; aggiornamento del Catalogo senza cancellazioni automatiche; separazione tra evidenze tecniche e decisioni di deduplicazione, acquisizione e pulizia. | Performance, permessi, provider e grandi archivi. | Scanner e censimento. | SPECIFICA CONSOLIDATA
M06 | Acquisisci e consolidamento | Scansione, selezione, acquisizione, duplicati e consolidamento nell'Archivio Condiviso. / Dipendenze: M06 -> M13; / M03 -> M06 utilizza le regole di consistenza e commit delle MediaCopy | Workflow a stati; preflight; disponibilità e capability; spazio; staging; verifica integrità; commit; gestione errori e interruzioni; recovery esplicito; assenza di retry/queue automatici; conservazione delle copie sorgenti; parallelismo, margine di spazio e fail-fast/continue. | Errori/interruzioni, spazio, conflitti e atomicità. | Workflow di acquisizione e consolidamento. | SPECIFICA CONSOLIDATA
M07 | Deduplicazione | Gestire duplicati esatti e relazioni tra copie/versioni secondo la scelta dell’utente. / Dipendenze: M07 -> M12 / M03 -> M07 utilizza MediaFingerprint e relativi indici. | Categorie, confronto, decisioni e sicurezza. | Fingerprint/hash, costi computazionali, accuratezza e grandi volumi. | Rilevazione e gestione interattiva. | SPECIFICA CONSOLIDATA
M08 | Organizzazione dei contenuti | Definire e implementare le funzioni di organizzazione dei contenuti mediante le informazioni e le relazioni organizzative presenti nel Catalogo, mantenendo separati e autonomi gli elementi oggetto di archiviazione. / Dipendenze: M08 -> M09, M10, M12. / M03 -> M08 utilizza le relazioni organizzative. | Modello di organizzazione logica; distinzione tra organizzazione del Catalogo e gestione fisica delle copie; requisiti per Archivia e Raggruppa; relazioni con persone, luoghi, eventi, tag, categorie e altri criteri; autonomia e indipendenza dei MediaItem; coerenza e persistenza delle relazioni; esclusione di modifiche fisiche o cambi di domainScope non esplicitamente richiesti. | Impatto sulle query, persistenza e indicizzazione delle relazioni, prestazioni, consistenza e gestione delle associazioni multiple. | Funzioni di organizzazione e gestione delle relazioni organizzative. | SPECIFICA CONSOLIDATA
M09 | Ricerca | Recuperare contenuti dal Catalogo secondo i criteri definiti. Dipendenze: M09 -> M10; / M03 -> M09 utilizza il Catalogo logico; / M08 -> M09 utilizza le relazioni organizzative. | Criteri strutturati e combinati; ricerca per data, persona, animale, oggetto, luogo, evento, contesto, tipo, testo/semantica e relazioni organizzative; risultati basati su MediaItem; indipendenza dalla disponibilità delle copie fisiche; ricerca non modificativa; local-first; separazione tra ricerca e riconoscimento M10; criteri di completamento. / Indicizzazione, performance, offline, full-text, semantica locale, grandi volumi e tecnologia di | Indicizzazione, performance, offline, full-text, semantica locale, grandi volumi e tecnologia di ricerca. | Motori di ricerca e UI. | SPECIFICA CONSOLIDATA
M10 | Persone, luoghi ed eventi | Associare e raggruppare contenuti mediante persone, luoghi, eventi e altre entità riconoscibili, mantenendo separata l'identità del MediaItem dalle relazioni organizzative. / Dipendenze: M10 → M17; / M03 -> M10 utilizza il Catalogo per la persistenza delle entità e delle associazioni;  / M08 -> M10 integra le relazioni organizzative;  / M09 -> M10 integra le funzioni di ricerca. | Modello delle entità e delle associazioni; associazioni manuali e automatiche; distinzione tra informazione, proposta e associazione confermata; conferma/correzione/rifiuto; provenienza e stato delle associazioni; persistenza nel Catalogo; indipendenza dalle copie fisiche; integrazione con M08 e M09; privacy e local-first. | Tecnologie locali, accuratezza, soglie, costi computazionali, modelli on-device, compatibilità Android, scalabilità e gestione delle associazioni | Funzioni e strutture dati. | SPECIFICA CONSOLIDATA
M11 | Album e Componi | Creare e gestire strutture logiche per album, ricordi, eventi e composizioni, mantenendo separati e autonomi i MediaItem dalle strutture e dalle relazioni di composizione. / Dipendenze: M11 -> M14 / M03 -> M11 utilizza il Catalogo logico e le identità dei MediaItem. | Modello funzionale di album, ricordi, eventi e composizioni; relazioni con il Catalogo; autonomia e indipendenza dei MediaItem; appartenenza multipla; creazione, modifica, aggiunta, rimozione e riordinamento degli elementi; distinzione tra evento e album; persistenza delle relazioni anche in caso di indisponibilità delle MediaCopy; assenza di duplicazione fisica; nessuna modifica automatica del domainScope; separazione da editing M12 e pubblicazione/produzione M14; criteri di accettazione. | Modello dati, cardinalità, ordinamento, versioni, prestazioni, gestione anteprime/cache e struttura delle composizioni | Implementazione funzioni. | SPECIFICA CONSOLIDATA
M12 | Editing e derivati | Gestire editing fotografico e video secondo un modello non distruttivo, preservando l'originale e generando versioni derivate collegate logicamente al contenuto originale. / Dipendenze: M12 -> M14; / M03 -> M12 utilizza il modello MediaItem / MediaCopy; / M07 -> M12 utilizza le regole di distinzione tra duplicati e versioni; / M08 -> M12 utilizza i principi di gestione fisica delle MediaCopy. | Perimetro dell'editing fotografico e video; modello originale/derivato; identità e persistenza dei derivati nel Catalogo; rapporto tra MediaItem, derivati e MediaCopy; comportamento in caso di indisponibilità delle copie fisiche; criteri di accettazione e vincoli di non distruttività; integrazione con il modello MediaItem/MediaCopy di M03; distinzione dei derivati dai duplicati e dalle versioni secondo le regole di M07; separazione tra generazione logica del derivato e gestione fisica delle MediaCopy secondo i principi di M08. | approfondimenti prima dello sviluppo: Librerie e tecnologie di editing; formati; risoluzione; qualità; compressione; gestione delle versioni derivate; performance; compatibilità Android; persistenza e pipeline tecnica. | Pipeline di editing e generazione/gestione dei derivati. | SPECIFICA CONSOLIDATA
M13 | Condivisione familiare | Gestire identità dei membri familiari, ruoli, autorizzazioni, dispositivi autorizzati e accesso ai contenuti dell'Archivio Condiviso, mantenendo separati identità, diritti logici e accesso fisico allo storage. / Dipendenze: M13 -> M14, M15, M17; / M04 -> M13 utilizza l'Archivio Condiviso e le relative modalità di accesso; / M06 -> M13 integra le regole e il risultato del consolidamento nell'Archivio C | Modello del membro familiare e dei dispositivi associati; distinzione autenticazione/autorizzazione; ruoli e granularità dei permessi; ciclo di vita di membri, dispositivi e autorizzazioni; comportamento offline e gestione di revoche/modifiche; distinzione tra accesso ai contenuti condivisi e sincronizzazione dei Cataloghi locali; integrazione con M04/M06 e separazione da M17; criteri di accettazione. | Meccanismi di autenticazione e autorizzazione; gestione delle identità; persistenza dei ruoli e dei permessi; gestione dei dispositivi autorizzati; sessioni/accesso; revoca e aggiornamento delle autorizzazioni; integrazione con storage condiviso; sicurezza tecnica e compatibilità Android. | Meccanismi di accesso, gestione membri, autorizzazioni e dispositivi autorizzati. | SPECIFICA CONSOLIDATA
M14 | Pubblicazione e produzione | Condividere, inviare e produrre album/PDF/stampe/esportazioni. / Dipendenze: M11 -> M14 utilizza album, ricordi, eventi e composizioni come strutture logiche di input; / M12 -> M14 utilizza il modello di originali e derivati per la produzione degli output;  / M13 -> M14 utilizza il modello di membri, ruoli e autorizzazioni per le funzioni di condivisione. | Perimetro della condivisione, dell'invio e della produzione; album, eventi e composizioni come input; distinzione tra produzione e editing; gestione di originali, derivati e output; formati, risoluzione, qualità, compressione e layout; esportazione; stampa; invio a social e servizi esterni; gestione delle MediaCopy non disponibili; errori e interruzioni; privacy e sicurezza; criteri di accettazione. | API Android, compatibilità, privacy e gestione file. | Implementazione funzioni. | SPECIFICA CONSOLIDATA
M15 | Backup e recupero | Proteggere esclusivamente l'Archivio Condiviso mediante backup manuali e salvataggi contestuali di protezione, con possibilità di recupero controllato dello stato fisico e logico dell'archivio. / M03 → M15 per il Catalogo e lo stato logico da proteggere e recuperare;  / M04 → M15 per l'Archivio Condiviso, lo storage e le modalità di accesso fisico ai contenuti;  / M13 → M15 per l'eventuale stato logico di membri, ruoli e autorizzazioni dell'Archivio Condiviso che debba essere preservato dal backup/ripristino. | Perimetro del backup; distinzione tra Archivio Condiviso e contenuti personali; protezione del contenuto fisico e dello stato logico; gestione dello spazio; versionamento e conservazione; verifica di integrità; gestione di errori e interruzioni; ripristino e reversibilità; rapporto con il Cestino; criteri di accettazione. Valutazione del possibile riuso/adattamento delle funzioni di backup e ripristino già sviluppate in BoxManager. / Implementazione M15: riuso selettivo di componenti/pattern BoxManager; formato, manifest, consistenza, gestione file, compatibilità e ripristino. Backup v1 esclusivamente completo; incrementale/differenziale fuori scope v1. | Formato, incrementale/differenziale, consistenza e ripristino. | Backup e restore dell’Archivio Condiviso. | SPECIFICA CONSOLIDATA
M16 | Cestino e cancellazioni | Gestire il Cestino e le cancellazioni dei contenuti consolidati nell'Archivio Condiviso. /  / M03 → M16 per il modello di MediaItem/MediaCopy e le informazioni necessarie a distinguere le copie;  / M04 → M16 per Archivio Condiviso, storage e capability necessarie alle operazioni sul Cestino;  / M15 → M16 per le regole consolidate relative al rapporto tra Cestino e Backup. | Cestino esclusivamente per contenuti consolidati nell'Archivio Condiviso; distinzione DeletePersonalCopy / SoftDeleteShared / PurgeShared; area dedicata; metadati per il ripristino; integrità; protezione dell'unica copia; recupero controllato; gestione di errori, interruzioni e collisioni; separazione dal Backup; retention di 30 giorni; svuotamento/cancellazione permanente sempre espliciti; Cestino escluso dal Backup per impostazione predefinita; criteri di accettazione. | Retention, spazio, lifecycle, atomicità delle operazioni, gestione filesystem/storage, collisioni di ripristino e rapporto tecnico Cestino/Backup. | Cestino e cancellazioni. | SPECIFICA CONSOLIDATA
M17 | Sicurezza e Privacy | Applicare privacy-by-design e protezione delle operazioni sensibili. /  / M02 → M17 per i confini architetturali e i Port/Adapter da proteggere;  / M04 → M17 per accesso, capability e disponibilità degli storage; / M10 → M17 per le funzioni di riconoscimento e trattamento locale dei dati;  / M13 → M17 per identità, ruoli, autorizzazioni e accesso all'Archivio Condiviso. | Gate privacy/security anticipato per M02/M04/M10/M13; requisiti trasversali di privacy e sicurezza; threat model; protezione dei dati, delle credenziali e delle operazioni sensibili; criteri di accettazione. | Threat model/STRIDE, least privilege, protezione credenziali, logging limitato, conferme delle operazioni distruttive, separazione autorizzazione/capability, protezione backup/restore/purge/export; DB locale non obbligatoriamente cifrato in v1, predisposto a evoluzione futura. | Misure tecniche di sicurezza. | SPECIFICA CONSOLIDATA
M18 | UI e navigazione | Implementare Home, macroaree, Top Bar, Bottom Bar e schermate. | Struttura funzionale e UX; Home e macroaree; shared shell BoxManager; Top Bar; Bottom Bar; CONFIGURA; stati UI; errori; indisponibilità delle sorgenti; autorizzazioni; conferme; accessibilità; criteri di accettazione. | Compatibilità, stato UI, navigazione, accessibilità, riuso dei componenti e convenzioni BoxManager, gestione degli stati e comportamento in caso di risorse indisponibili. | Layout, componenti e navigazione. | SPECIFICA CONSOLIDATA
M19 | Integrazione e qualità | Verificare coerenza complessiva, assenza di regressioni e rispetto dei requisiti consolidati. / Dipendenze: M19 → M20; / M16 → M19 per verificare l'integrazione di Cestino e cancellazioni; / M17 → M19 per verificare l'integrazione dei requisiti di sicurezza e privacy; / M18 → M19 per verificare l'integrazione della UI e della navigazione. | Criteri di verifica; casi limite; matrice dei test funzionali e di integrazione; verifica dei flussi tra moduli; gestione delle sorgenti/storage disponibili e indisponibili; verifica di errori, interruzioni, recuperabilità, privacy, sicurezza, performance, compatibilità e assenza di regressioni. | Integrazione, performance, compatibilità, disponibilità storage × sorgenti, errori/interruzioni, gestione degli stati e verifica dei contratti architetturali. | Test, debugging e correzioni. | SPECIFICA CONSOLIDATA
M20 | Rilascio | Preparare il prodotto alla distribuzione. / Dipendenze: M19 → M20 per utilizzare l'esito della verifica di integrazione e qualità come prerequisito per il rilascio. | Checklist e criteri di release. | Build, configurazione, sicurezza e packaging. | Release build e distribuzione. | SPECIFICA CONSOLIDATA
