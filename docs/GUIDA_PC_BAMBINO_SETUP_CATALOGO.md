# Guida superdettagliata — Collegare un PC / notebook a MediaManager

**Versione guida:** 04/11/2026  
**Ambito:** solo **PC o notebook Windows** (non telefono, non NAS, non Drive).  
**Livello:** zero competenze informatiche richieste.  
**App di riferimento:** MediaManager (setup Catalogo sul PC).  
**File Word scaricabile:** vedi artifact `Guida_PC_MediaManager_bambino.docx`

---

## 0. Cosa stai per fare (in due frasi)

Vuoi che il **telefono** veda una **cartella sul PC**, così MediaManager può tenere lì il Catalogo (l’elenco delle tue foto).

Il telefono **non** apre da solo il PC. Devi: (1) preparare una cartella sul PC, (2) farla “vedere in rete”, (3) insegnare al telefono come arrivarci con un’app gratis chiamata **CIFS**.

---

## 1. Cosa ti serve prima di iniziare

Metti sulla scrivania:

1. Il **PC o notebook** con **Windows 10 o Windows 11** acceso.  
2. Il **telefono o tablet Android** con **MediaManager** installata.  
3. Il **cavo USB** del telefono (quello che serve anche a trasferire file, non solo a caricare).  
4. La stessa **rete Wi‑Fi** per telefono e PC (stesso modem/router di casa).  
5. Circa **20–40 minuti** senza fretta.

Non ti serve: cloud, NAS, programmi a pagamento.

---

## 2. Parole difficili (spiegate come a scuola)

| Parola | Significa |
|--------|-----------|
| **Cartella** | Un “scatolone” nel computer dove metti i file. |
| **Desktop** | Lo schermo principale del PC, dove di solito ci sono le icone. |
| **Wi‑Fi** | La rete senza filo di casa. Telefono e PC devono essere sulla **stessa**. |
| **IP** | Il “numero civico” del PC sulla Wi‑Fi. Esempio: `192.168.1.20`. Serve al telefono per trovare il PC. |
| **Cartella condivisa (share)** | Una cartella del PC che altri dispositivi in casa possono aprire in rete. Nome tipico: `MediaManagerCatalog`. |
| **Utente MediaManager** | Un “badge” speciale creato sul PC solo per questa app (non è la tua mail Google). |
| **Password** | La chiave di quel badge. La crea MediaManager; **non** inventarla a mano. |
| **CIFS Documents Provider** | App gratis su Google Play. È il **ponte** tra telefono e cartella del PC. MediaManager da sola non basta. |
| **CONFIGURA** e **sup** | Due file che MediaManager crea nel telefono. Li copi sul Desktop del PC. Apri **solo CONFIGURA** (doppio clic). |

---

## 3. Dove si ricavano credenziali e dati di rete

### 3.1 Tabella “chi dà cosa”

| Dato | Dove lo trovi | Cosa fai |
|------|---------------|----------|
| **Name** (nome connessione) | Scheda in MediaManager (passo CIFS) → bottone **Copia** | Incolli in CIFS nel campo **Name**. Di solito: `MediaManagerCatalog` |
| **Host** (IP del PC) | 1) Se compare in MediaManager → **Copia**. 2) Se manca → leggilo dalla finestra **«Tutto pronto»** sul PC. 3) Se ancora manca → §6 (IP a mano) | Incolli in CIFS nel campo **Host** |
| **Port** | Sempre **445** (MediaManager può offrire **Copia**) | In CIFS metti `445` |
| **User** | MediaManager → **Copia** (spesso `NOMEPC\MediaManager`). Se manca → finestra «Tutto pronto» sul PC | Incolli in CIFS in **User** |
| **Password** | Solo in **MediaManager** (Mostra + **Copia**). Non è la password di Windows né Gmail | Incolli in CIFS in **Password** |
| **Domain** | — | Lascia **vuoto** |
| **Folder** in CIFS | — | Lascia **vuoto** (poi apri la riga `MediaManagerCatalog`) |
| **Storage** | — | Scegli **SMB2,3 (jCIFS NG)** |

### 3.2 Ordine mentale (importante)

1. Prima fai girare **CONFIGURA** sul PC fino a **«Tutto pronto»** (così nascono cartella, utente, share).  
2. **Lascia aperta** quella finestra nera/blu sul PC.  
3. Poi sul telefono, in MediaManager, usi i bottoni **Copia** verso CIFS.  
4. La password **non** si copia dalla finestra del PC: si copia dall’**app**.

### 3.3 Se Host o User in app sono vuoti

- Telefono e PC sulla **stessa Wi‑Fi**.  
- Finestra **«Tutto pronto»** ancora aperta.  
- In MediaManager usa **Riprova recupero dati** (se c’è).  
- Altrimenti: Host = IP a mano (§6); User = `MediaManager` oppure `NOMEDELPC\MediaManager` (il nome PC è in Impostazioni Windows → Sistema → Informazioni).

---

## 4. Tool di terze parti: sì / no

| Tool | Serve? | Perché |
|------|--------|--------|
| **CIFS Documents Provider** (Google Play, gratis) | **SÌ, obbligatorio** sul telefono | È il ponte ufficiale usato da MediaManager (come in BoxManager). Senza CIFS il telefono non “monta” la cartella del PC. |
| **Esplora file di Windows** (già sul PC) | **SÌ** | Solo per copiare CONFIGURA e sup dal telefono al Desktop. Non è un’app da installare. |
| **File Explorer+** / altre app file manager Android | **NO nel percorso ufficiale** | Non fanno parte della procedura MediaManager per il PC. Non usarle al posto di CIFS. |
| **Gestione file / Archivio Samsung** (SMB nativo) | **Non è il percorso principale** di questa guida | In passato era un’alternativa generica; oggi il binario previsto dall’app è **CIFS Documents Provider**. |
| Programmi SMB a pagamento sul PC | **NO** | Lo script CONFIGURA prepara utente e condivisione su Windows. |

**Regola:** sul telefono, per il PC, usi **CIFS**. Sul PC usi solo Windows + il file **CONFIGURA**.

---

## 5. Procedura completa (binario GUIDATO) — passo dopo passo

> Questa è la procedura **lunga / guidata** (crea la cartella sul PC da zero).  
> Il binario **rapido** (share già pronta) lo adatterai dopo il test: per ora concentrati su questa.

### Parte A — Sul telefono, in MediaManager

**A1.** Apri **MediaManager**.  
**A2.** Vai in **Setup Catalogo personale** (da Configura, oppure dalla riga del Catalogo, oppure al primo avvio).  
**A3.** Alla domanda **dove tenere il Catalogo**, scegli: **Su un PC / notebook Windows**.  
**A4.** Lascia (o controlla) il percorso tipo: `C:\MediaManager\Catalogo`.  
**A5.** Tocca **OK** e segui i passi 1 → 5 dell’app.

### Parte B — Passo 1 app: “Ho tutto?”

Spunta solo se è vero:

- [ ] Ho il telefono con MediaManager  
- [ ] Ho un PC Windows 10/11  
- [ ] Ho un cavo USB adatto  

Poi **OK**.

### Parte C — Passo 2 app: collega USB

**C1.** Infila il cavo: telefono ↔ PC.  
**C2.** Sblocca il telefono.  
**C3.** Sul telefono: scorri le **notifiche** dall’alto.  
**C4.** Tocca la voce tipo **«Ricarica questo dispositivo via USB»** / **USB**.  
**C5.** Scegli **Trasferimento file** (o **MTP** / **File transfer**). Non lasciare “Solo ricarica”.  
**C6.** Se chiede permesso di accesso ai dati → **Consenti**.  
**C7.** In MediaManager spunta le voci e tocca **OK**.  
→ L’app crea in **Download** del telefono i file **CONFIGURA** e **sup**.

### Parte D — Passo 3: copia i file sul Desktop del PC

**D1.** Sul PC apri **Esplora file** (icona cartella gialla nella barra).  
**D2.** A sinistra (o in “Questo PC”) cerca il **nome del telefono**.  
**D3.** Apri: **Memoria interna** (o **Telefono** / **Internal storage**).  
**D4.** Apri la cartella **Download**.  
**D5.** Trova **CONFIGURA** e **sup**.  
**D6.** Seleziona **entrambi** (tieni premuto Ctrl e clicca i due file).  
**D7.** Copia: **Ctrl+C**.  
**D8.** Vai sul **Desktop** del PC.  
**D9.** Incolla: **Ctrl+V**.  
**D10.** Controlla: sul Desktop vedi **due** file.  
**D11.** In MediaManager spunta le voci e **OK**.

Se non vedi il telefono in Esplora file: ripeti C3–C6; prova un altro cavo/porta USB; sblocca di nuovo il telefono.

### Parte E — Passo 4: fai partire CONFIGURA sul PC

**E1.** Sul Desktop, **doppio clic** su **CONFIGURA**.  
**E2.** **Non** aprire **sup**.  
**E3.** Se compare **Controllo account utente** (finestra grigia “Vuoi consentire…?”) → **Sì**.  
**E4.** Non usare “Esegui come amministratore” dal tasto destro: non serve.  
**E5.** Aspetta. Può volerci **qualche minuto**. Non chiudere la finestra.  
**E6.** Quando leggi **«Tutto pronto»** → **lascia la finestra aperta**.  
**E7.** In MediaManager spunta le voci e **OK**.

Cosa ha fatto CONFIGURA (in parole semplici): ha creato la cartella, un utente `MediaManager`, la condivisione `MediaManagerCatalog`, e aiuta l’app a sapere IP e utente.

### Parte F — Passo 5: CIFS sul telefono

**F1.** Su Google Play installa **CIFS Documents Provider** (se non ce l’hai).  
**F2.** Aprilo da MediaManager (**Apri CIFS** / Installa) oppure dall’elenco app.  
**F3.** Crea una **nuova Connection**.  
**F4.** Campo per campo (usa **Copia** in MediaManager, poi tieni premuto nel campo CIFS → Incolla):

1. **ID** → non toccare  
2. **Name** → incolla (es. `MediaManagerCatalog`)  
3. **Storage** → `SMB2,3 (jCIFS NG)`  
4. **Domain** → vuoto  
5. **Host** → IP del PC  
6. **Port** → `445`  
7. **Enable DFS** → spento  
8. **User** → es. `NOMEPC\MediaManager`  
9. **Password** → quella di MediaManager (Mostra + Copia)  
10. **Folder** → **MyMedia** (nome share; non vuoto e non path Windows)  
11. Opzioni varie → lascia i default  

**F5.** In CIFS tocca **Save**.  
**F6.** Torna a MediaManager col tasto **Indietro** del telefono (il ritorno non è automatico).  
**F7.** In MediaManager: **Attiva collegamento**.  
**F8.** Nell’elenco, apri la riga **MediaManagerCatalog** (non restare sulla radice / elenco generico).  
**F9.** Tocca **Usa questa cartella**.  
**F10.** Se l’app chiede se l’accesso è andato bene → **Sì** solo se hai davvero aperto quella cartella.

---

## 6. Come ricavare l’IP del PC a mano (se manca)

### Metodo semplice (Windows 10/11)

1. Sul PC clicca il pulsante **Start**.  
2. Digita: `cmd` e apri **Prompt dei comandi**.  
3. Scrivi: `ipconfig` e premi **Invio**.  
4. Cerca la sezione della **Wi‑Fi** (o “Scheda LAN wireless”).  
5. Leggi **Indirizzo IPv4**. Esempio: `192.168.1.20`.  
6. Quello è l’**Host** da mettere in CIFS.

Attenzione: non usare indirizzi che iniziano con `169.254.` (rete non a posto). Non usare `127.0.0.1`.

Telefono e PC devono essere sulla **stessa** Wi‑Fi (non dati mobili sul telefono).

---

## 7. Doppio binario (rapida / guidata) — bozza per il tuo test

| Binario | Quando | Cosa fa (idea) |
|---------|--------|----------------|
| **Guidata** (questa guida) | La cartella condivisa **non** esiste ancora | USB → CONFIGURA → CIFS → scegli cartella |
| **Rapida** (da adattare dopo il tuo test) | Share **già** pronta e raggiungibile | Salta USB/CONFIGURA → CIFS (o CIFS già ok) → scegli cartella |

Non mescolare i due binari a metà. Se parti “rapida” e CIFS non vede il PC, torna alla guidata o verifica Wi‑Fi / Host / password.

---

## 8. Checklist finale “ha funzionato?”

- [ ] Sul PC esiste la cartella (di solito `C:\MediaManager\Catalogo`)  
- [ ] CONFIGURA è arrivato a **Tutto pronto**  
- [ ] In CIFS la Connection è **salvata**  
- [ ] Da MediaManager hai scelto **MediaManagerCatalog** con **Usa questa cartella**  
- [ ] MediaManager dice che la posizione Catalogo è ok / raggiungibile  

Se un punto è NO: non passare ad altri dispositivi; ripeti solo il pezzo fallito (USB, CONFIGURA, oppure CIFS).

---

## 9. Errori tipici (e cosa fare)

| Cosa vedi | Cosa fare |
|-----------|-----------|
| Telefono non compare sul PC | Modalità USB = Trasferimento file; altro cavo; telefono sbloccato |
| CONFIGURA si chiude subito | Conferma UAC; Windows 10/11; non aprire `sup` |
| CIFS non connette / Catalogo non raggiungibile | Stessa Wi‑Fi; Host = IPv4; Port 445; User/Password da Copia; Folder=MyMedia; Read only OFF |
| Vedi elenco share ma non Catalogo | Apri la riga **MediaManagerCatalog**, non la radice |
| Password sparita in app | Rifai il passo USB/OK che rigenera i file CONFIGURA |

---

*Fine guida PC. Prossimi dispositivi (altro telefono, NAS, cloud) solo dopo che questa procedura è stata provata e adattata.*
