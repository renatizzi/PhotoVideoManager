# Studio di fattibilità — Bluetooth come autorizzazione e governo PC → bundle CIFS

**Tipo:** analisi obiettiva (nessun codice, nessuna assunzione su script SETUP / LAN / QR esistenti)  
**Ambito OS:** Android 12+ · Windows 10/11  
**Obiettivi obbligatori del processo:**

1. Autorizzare lo scambio dati telefono Android ↔ PC Windows  
2. Configurare cartelle condivise (share SMB/CIFS sul PC)  
3. Produrre un **bundle credenziali completo** per CIFS Documents Provider su Android: Host IPv4, User, Password, nome share, cartella  

**Tesi utente (valutata):** *senza autorizzazione il PC non è governabile dal telefono; se si autorizza via Bluetooth si possono scambiare file e interagire col PC.*

**Seguito prodotto (06/11):** percorso approvato come **BT corriere + script manuale + download bundle dal telefono** — vedi `docs/LOCK_SETUP_BT_BUNDLE_CIFS.md` (form CIFS intero, solo Copia).

---

## Verdetto in una frase

La tesi è **mezza vera sul piano intenzionale** (serve un consenso utente esplicito) e **falsa sul piano tecnico**: Bluetooth autorizza un **tubo di byte** tra due stack radio; **non** concede al telefono poteri amministrativi sul PC. Gobernare share SMB, utenti locali e firewall richiede un **agente Windows privilegiato** e un canale IP (tipicamente Wi‑Fi) per l’uso quotidiano di CIFS. Bluetooth da solo **non** chiude i tre obiettivi.

| Obiettivo | Verdetto |
|---|---|
| 1 — Autorizzare scambio dati | **Possible** con BT (pairing/bond + sessione RFCOMM/GATT) |
| 2 — Configurare share SMB/CIFS | **Not with BT alone** — Partial solo se BT trasporta comandi a un agente Windows elevato |
| 3 — Bundle CIFS completo (Host IPv4 + User + Password + share + folder) | **Not with BT alone** — Partial: BT può consegnare i campi, ma Host IPv4 e percorso L3 verso :445 restano fuori dal radio BT |

---

## A) Cosa fornisce davvero Bluetooth (Android 12+ / Windows 10–11)

### Canali disponibili

| Canale | Natura | Throughput pratico | Uso realistico |
|---|---|---|---|
| **Classic RFCOMM / SPP** | Stream bidirezionale (socket seriale) | Ordine 100–500+ kb/s tipici; non è Wi‑Fi | Scambio comandi, piccoli payload, file piccoli |
| **BLE GATT** | Attributi (characteristic read/write/notify), MTU tipica 23–512 B | Basso; adatto a segnalazione / bootstrap | Pairing, handshake, consegna token/credenziali corte |
| **Advertising BLE** | Beacon unilaterale | Nessun pipe dati applicativo | Discovery / presence |

Bluetooth **non** implementa SMB, non apre la porta TCP 445, non monta share, non crea utenti Windows. È solo trasporto L2 radio + (dopo pairing) un canale applicativo.

### Pairing vs bonded

- **Pairing:** scambio chiavi di sessione (Just Works / Numeric Comparison / Passkey). Just Works è debole contro MITM attivo.
- **Bonded (paired device):** le chiavi restano in store di sistema; le app possono riconnettersi senza ripetere il rituale UX ogni volta.
- Su Windows 10 1607+: RFCOMM può connettersi anche senza pairing preventivo via Settings; BLE GATT client moderno può usare API async senza pairing obbligatorio. Su Android, RFCOMM verso un peer Classic tipicamente passa da `BluetoothSocket` su device già noto/paired; BLE usa `connectGatt`.
- **Pairing ≠ autorizzazione applicativa.** Il sistema OS conferma «questi due radio possono parlare»; non autorizza «questa app telefono può creare share sul PC».

### Permessi Android 12+

Runtime obbligatori (gruppo «Dispositivi nelle vicinanze»):

- `BLUETOOTH_SCAN` — discovery  
- `BLUETOOTH_CONNECT` — comunicazione con device già noti / connessione  
- `BLUETOOTH_ADVERTISE` — solo se il telefono annuncia  

Legacy `BLUETOOTH` / `BLUETOOTH_ADMIN` con `maxSdkVersion=30`. Location non è più obbligatoria per scan se si dichiara `neverForLocation`, ma molti OEM restano fragili.

### Limiti background (Android)

- GATT/RFCOMM restano vivi **solo finché il processo app è vivo**.
- Da Android 12: avvio FGS da background ristretto; per sessioni lunghe serve FGS tipo `connectedDevice` o Companion Device Manager.
- Non esiste un «tunnel permanente PC↔telefono» gestito dal sistema per app di terze parti senza companion service / FGS e UX di notifica.

### Stack Windows

- Classic: `Windows.Devices.Bluetooth.Rfcomm` (UWP/WinRT) — socket bidirezionale; capability `bluetooth` nel manifest.  
- BLE: GATT client/server (`GenericAttributeProfile`); Windows può fare da GATT Server ma le sessioni in background/sospensione sono limitate (advertising foreground tipicamente).  
- Non c’è API pubblica «Bluetooth → crea SMB share».  
- Desktop Win32 può aprire socket RFCOMM via stack Microsoft, ma il prodotto resta: **pipe + codice locale**.

### Cosa BT *non* dà

- Indirizzo IPv4 del PC sulla LAN/hotspot  
- Apertura automatica firewall profilo privato/pubblico per 445  
- Elevazione UAC / creazione utente locale / `New-SmbShare`  
- Accesso allo storage Windows oltre a ciò che un’app companion espone volontariamente  
- Un’API Phone Link/Nearby Share riusabile come bus di controllo per share SMB

---

## B) Cosa significa davvero «governare il PC» oltre al tubo di byte

Un byte pipe (RFCOMM o GATT) consente solo: *inviare messaggi a chi ascolta sull’altro capo*. Per gli obiettivi 2–3 serve un **agente companion su Windows** che esegua azioni locali. Quell’agente, non Bluetooth, è il punto di governo.

### Azioni necessarie lato Windows (realtà amministrativa)

| Azione | Privilegio | Blocco tipico |
|---|---|---|
| `New-SmbShare` / modifica ACL share | **Amministratore** (elevazione) | UAC prompt; senza admin fallisce |
| Creare utente locale dedicato alla share | **Amministratore** | Stesso |
| Impostare password utente | **Amministratore** | Stesso |
| Regola firewall SMB (445) inbound | **Amministratore** | Profilo rete Pubblico spesso blocca comunque |
| Avviare servizio Server (LanmanServer) se disabilitato | **Amministratore** | SKU Home ok di default; policy aziendali no |
| Leggere IPv4 «usabile» dal telefono | Utente standard (query NIC) | Scelta NIC corretta (Wi‑Fi vs Ethernet vs vEthernet) è logicamente difficile |
| Esporre path cartella e nome share | Utente / admin | Path fuori profilo utente può richiedere ACL |

### UAC e sessione

- Un processo utente standard **non** può elevare in silenzio. Serve consenso UAC nella sessione desktop interattiva (Secure Desktop).  
- Un servizio SYSTEM può creare share senza UAC interattivo, ma l’installazione del servizio richiede già admin una volta, e Microsoft/antivirus trattano «agente remoto che eleva» come superficie ad alto rischio.  
- Sessione locked / Fast User Switching / nessun utente loggato: un listener RFCOMM in servizio può restare su, ma qualsiasi UI di consenso scompare.  
- «Autorizzare via Bluetooth» sul telefono **non sostituisce** l’UAC sul PC. Sono due consentimenti ortogonali.

### Separazione dei piani

```
[Telefono] --BT RFCOMM/GATT--> [Agente Windows] --API locali elevate--> SMB/utenti/firewall
                                      |
                                      +--TCP :445 (Wi‑Fi/LAN)--> [CIFS Documents Provider]
```

Bluetooth può autenticare/bootstrapare la relazione col companion. Il traffico file reale di CIFS Documents Provider è **SMB su IP**, non BT.

---

## C) Architetture end-to-end (ordinate)

Criteri di ranking: chiusura dei 3 obiettivi, onestà UX (pochi passi non ambigui), sicurezza, dipendenze OS non controllabili, testabilità.

### 1) BT solo come pairing + data pipe + companion Windows — **Rank #2 (bootstrap ok, uso CIFS no)**

- Telefono e PC si bondano; RFCOMM/GATT scambia comandi «crea share / genera password / leggi IP».  
- Companion elevato esegue `New-SmbShare` + utente + firewall.  
- Bundle torna al telefono via BT.  
- **Pro:** consenso radio chiaro; non dipende da discovery LAN.  
- **Contro:** throughput BT inutile per foto/video; Host IPv4 restituito può essere irraggiungibile se telefono e PC non condividono L3; installazione companion + UAC restano.  
- **Chiude:** (1) sì; (2) sì *solo con agente+admin*; (3) bundle sì come stringhe, ma CIFS operativo richiede ancora rete IP.

### 2) BT bootstrap → poi Wi‑Fi/TCP — **Rank #1 (unica architettura coerente se si insiste su BT)**

- BT: pairing, scambio di un secret di sessione / endpoint (SSID hotspot, IP, token).  
- Wi‑Fi (hotspot telefono o stessa LAN): TCP per comandi lunghi e soprattutto per SMB :445.  
- Companion Windows resta obbligatorio per creare la share.  
- **Pro:** BT risolve «chi è chi» a corto raggio; Wi‑Fi porta banda e CIFS.  
- **Contro:** due radio, due fail-mode; hotspot obbligatorio è vincolo UX forte ma topologicamente onesto.  
- **Chiude:** (1)(2)(3) se L3 è coerente (stesso AP / no client isolation).

### 3) Funzioni OS: Phone Link / Nearby Share (Quick Share) — **Rank #4 (fuori obiettivo governo)**

- Phone Link + Link to Windows: sync file/notifiche/call consumer; task continuity è LAF Microsoft, non un bus di controllo SMB.  
- Nearby Share / Quick Share: trasferimento file peer-to-peer con formato proprietario; **nessuna API pubblica** per far creare share Windows o restituire un bundle CIFS strutturato a un’app terza.  
- **Pro:** zero companion custom per «mandare un file».  
- **Contro:** non configura SMB; non produce Host/User/Password/share/folder; account Microsoft / cloud path spesso in mezzo; non governabile programmaticamente in modo affidabile.  
- **Chiude:** (1) solo file ad-hoc; (2)(3) **no**.

### 4) USB (MTP / AOA / accessory) — **Rank #3 (pipe forte, stesso problema di privilegio)**

- USB dà banda e UX «cavo = autorizzazione fisica» chiara.  
- Su Windows, MTP espone lo storage del telefono al PC, **non** il contrario come share SMB. Per governare il PC serve comunque un driver/agente lato host (ADB non è consumer; WinUSB/custom).  
- **Pro:** consenso fisico; no RF MITM a distanza.  
- **Contro:** cavo; stesso requisito companion+admin per SMB; su alcuni OEM USB file transfer è solo MTP.  
- **Chiude:** (1) sì come scambio file/credenziali; (2)(3) solo con agente Windows.

### 5) Solo Wi‑Fi (LAN o hotspot) — **Rank #1-bis se si abbandona il feticcio BT**

- Stesso piano L3 del CIFS: un solo vincolo di rete.  
- Autorizzazione = pairing applicativo (PIN/QR/codice monouso) sopra TCP, non bonding radio.  
- **Pro:** meno stack; CIFS e controllo sullo stesso path.  
- **Contro:** discovery e «quale IP» restano difficili su reti ostili (AP isolation); senza BT manca il segnale di prossimità RF.  
- **Chiude:** (1)(2)(3) con companion; prossimità fisica va simulata con codice corto, non con radio.

**Sintesi ranking per il processo richiesto:**  
**(2) BT bootstrap + Wi‑Fi** ≈ **(5) pure Wi‑Fi** > **(1) BT-only pipe** > **(4) USB** ≫ **(3) Phone Link/Nearby Share**.  
**(1) da solo non basta** all’obiettivo 3 operativo. **(3) non è un’architettura di governo SMB.**

---

## D) Fattibilità per i tre esiti richiesti

### 1) Autorizzare lo scambio dati Android ↔ Windows

| Modalità | Esito |
|---|---|
| Solo bonding BT di sistema | **Possible** — autorizza il radio, non l’app di controllo |
| Bonding BT + handshake applicativo (token monouso) | **Possible** — livello corretto di «autorizzazione prodotto» |
| Phone Link | **Partial** — autorizza l’ecosistema Microsoft, non il tuo processo CIFS |
| BT come prova di «posso governare il PC» | **Not with BT alone** — confonde pairing radio con privilegio OS |

### 2) Configurare cartelle condivise (SMB/CIFS su Windows)

| Modalità | Esito |
|---|---|
| Solo API Bluetooth su telefono/PC | **Not with BT alone** |
| BT che comanda un companion **già elevato** / servizio SYSTEM | **Partial → Possible** (il merito è del companion + admin, non del BT) |
| Phone Link / Nearby Share | **Not** |
| Utente crea share a mano, BT solo consegna metadati | **Partial** (configurazione non automatizzata)

Realtà dura: senza un binario Windows con diritti admin (almeno una volta), **nessun** canale dal telefono configura `New-SmbShare`.

### 3) Bundle completo CIFS Documents Provider

Campi: **Host IPv4, User, Password, share name, folder**.

| Campo | Ottenibile via solo BT? | Note |
|---|---|---|
| User | Sì (come stringa) | Deve esistere sul PC; creazione → admin |
| Password | Sì (come stringa) | Trasporto BT di secret in chiaro sul pipe = rischio; meglio derivare da ECDH post-pairing |
| Share name / folder | Sì (come stringhe) | Devono corrispondere a share reale |
| Host IPv4 | **No come garanzia** | BT non conosce l’IP «raggiungibile dal telefono»; l’agente può *indovinare* un IP locale che poi non routea |
| Raggiungibilità :445 | **No** | Fuori banda BT |

**Verdetto bundle:**  
- **Produrre il documento credenziali** via BT+agente: **Partial / Possible**.  
- **Far funzionare CIFS Documents Provider** con quel bundle: **Not with BT alone** — serve L3 IP comune. Senza di esso il bundle è teatro.

---

## E) Rischi sicurezza / UX

| Rischio | Dettaglio concreto |
|---|---|
| **MITM su pairing** | Just Works / legacy PIN 0000: attaccante a corto raggio può intercettare. Numeric Comparison obbligatoria se si trasportano password. |
| **Password temporanea su BT** | Anche su link bonded, malware sul telefono o log di debug espongono il secret. Preferire: generazione sul PC, visualizzazione UI PC, o ECDH + AEAD sul pipe. |
| **UX pairing** | Discoverable mode Windows breve; OEM Android agresivi sui permessi «Nearby devices»; fallimenti silenziosi se BT off / LE-only vs Classic mismatch (RFCOMM richiede Classic). |
| **Admin elevation** | Ogni automazione onesta mostra UAC. Nasconderlo = malware pattern. L’utente dovrà comunque toccare il PC. |
| **Agente persistente** | Servizio che ascolta RFCOMM/TCP ed esegue PowerShell elevato è superficie di RCE locale: auth mutua, allowlist comandi, timeout, revoca bond. |
| **IP sbagliato nel bundle** | UX «tutto ok» sul telefono + CIFS che non connette: peggio di una procedura manuale chiara. |
| **Firewall / profilo Pubblico** | Share creata ma :445 filtrato; BT non aiuta. |
| **Phone Link come scorciatoia** | Account Microsoft, sync cloud, policy aziendale: falso amico per un prodotto che vuole solo una cartella SMB locale. |

---

## F) Testabilità senza due device fisici (agente cloud Linux)

Un runner Linux cloud **non** sostituisce un telefono Android + un PC Windows con radio BT reale. Cosa si può e non si può validare:

| Layer | In cloud Linux | Come |
|---|---|---|
| Schema messaggi bootstrap (JSON/CBOR, ECDH, campi bundle) | **Sì** | Unit/integration test puro |
| Validazione bundle (5 campi obbligatori, formato `HOST\User`) | **Sì** | Fixture |
| Logica scelta NIC / IP «candidati» | **Parziale** | Mock interfacce; non prova raggiungibilità telefono |
| Esecuzione `New-SmbShare` / UAC | **No** su Linux | Serve VM/host Windows dedicato o runner CI Windows self-hosted |
| RFCOMM/GATT end-to-end | **No** affidabile | Niente controller BT consumer tipico; HCI bluez mock ≠ stack Microsoft ↔ Android |
| CIFS Documents Provider | **No** | Solo su device Android reale / emulatore con SMB server fittizio |
| SMB server fittizio → client Linux `smbclient` | **Sì** | Prova il *contratto* share+cred, non BT né CIFS app |
| Phone Link / Nearby Share | **No** | Servizi proprietari closed |

**Conclusione testabilità:** si può falsificare/confermare la **logica di protocollo e il contratto del bundle** in CI; **non** si può dichiarare «BT governa il PC» senza banco fisico Android+Windows. Chi vende la tesi BT-only senza quel banco sta testando speranze, non sistemi.

---

## Decisione finale (senza ambiguità)

1. **Sì:** senza un atto di autorizzazione il PC non deve essere governabile dal telefono.  
2. **No:** l’autorizzazione Bluetooth **non** equivale a governo del PC. Equivale a un canale cifrato a corto raggio verso un processo Windows che *eventualmente* ha privilegi.  
3. **BT alone** non configura share SMB e non garantisce un Host IPv4 usabile da CIFS Documents Provider.  
4. Se si vuole BT nel prodotto, l’unico disegno onesto è **(2) BT = pairing/bootstrap + Wi‑Fi/TCP = piano dati e SMB**, con **companion Windows e almeno un consenso admin sul PC**.  
5. Phone Link / Nearby Share **non** sono un sostituto di quel disegno per gli obiettivi 2–3.  
6. Pure Wi‑Fi (hotspot telefono o LAN senza isolation) resta architetturalmente più semplice dello stesso risultato: un solo piano di rete per controllo e CIFS; BT aggiunge prossimità, non potere.

**Raccomandazione di processo:** trattare Bluetooth come *opzionale di prossimità/bootstrap*, non come piano di governo. Il percorso critico resta: **agente Windows elevato → share+utente+firewall → bundle → stesso L3 del telefono → CIFS Documents Provider**. Qualunque narrazione che salti l’agente o l’L3 è tecnicamente falsa.
