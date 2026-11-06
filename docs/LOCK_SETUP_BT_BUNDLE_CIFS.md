# LOCK — Setup Catalogo PC via Bluetooth + bundle CIFS completo

**Stato:** in implementazione su tip 0.16.69 → app 0.16.71-preview (base corretta)
**Ambito:** ramo wizard «Su un PC/Notebook Windows»  
**Sostituisce come trasporto file:** percorso USB/MTP (passi 1–3 attuali)  
**Sostituisce come recupero dati post-«Tutto pronto»:** LAN fragile, QR/URI, digitazione Host, integrazione pezzi tra sessione app e finestra PC  
**Fondamento tecnico:** `docs/STUDIO_FATTIBILITA_BLUETOOTH_GOVERNO_PC_CIFS.md`  
**Depreca come primario:** hotspot-only / USB-roundtrip delle proposte bundle precedenti *se* in conflitto con questo flusso; USB resta solo piano B di trasporto file.

---

## Decisione in una riga

Bluetooth = **corriere** andata (`CONFIGURA`/`sup`) e ritorno (bundle CIFS).  
Lo script gira **a mano** sul PC (UAC).  
Il telefono **scarica** il bundle (nessun automatismo di push nello script).  
La scheda speculare a CIFS è **intera e solo-copia**: zero integrazioni o modifiche utente sui dati raccolti.

---

## Flusso utente (passi)

| Passo | Titolo | Cosa fa l’utente | Cosa fa l’app / lo script |
|------:|--------|------------------|---------------------------|
| 0 | Dove tenere il Catalogo | Sceglie PC + percorso cartella | Invariato |
| 1 | Prerequisiti | Spunta: telefono, PC Win10+, **Bluetooth** su entrambi, CIFS installabile | Niente cavo USB obbligatorio |
| 2 | Collegamento BT + preparazione file | Pairing BT; su PC apre «Ricevi file» (`fsquirt`); sull’app conferma e invia | OK → genera `CONFIGURA` + `sup` e avvia invio BT (OPP/Share) |
| 3 | File sul Desktop | Accetta i due file e li tiene sul Desktop (o li sposta lì) | Checklist corta; **niente** Esplora→telefono→Download |
| 4 | Esecuzione script | Doppio clic `CONFIGURA`, UAC, attende «Tutto pronto» | Script crea share/utente/ACL/firewall; scrive sul Desktop `MediaManager_cifs_bundle.json` (**non** lo invia) |
| 4b | Scarica dati CIFS | In app: «Scarica dati CIFS»; sul PC: invio OS una volta del file bundle già sul Desktop (gesto umano, non logica script) | App importa bundle → popola sessione Copia; **rifiuta** bundle incompleto |
| 5 | Scheda CIFS (form speculare) | Solo **Copia** → Incolla in CIFS; Save; Indietro; poi verifica sede | Mostra form **solo se** bundle completo; ogni campo dati ha Copia con valore definitivo |

**USB:** fuori dal percorso felice. Piano B solo se BT/OPP fallisce sull’OEM (stessi file, stesso bundle).

**Wi‑Fi / IP:** dopo il passo 5, CIFS Documents Provider parla SMB `:445` sulla rete IP (LAN o hotspot). BT non sostituisce L3.

---

## Contratto bundle (unica fonte di verità post-«Tutto pronto»)

File: `MediaManager_cifs_bundle.json` (Desktop PC).  
ASCII. Password **inclusa**.

| Chiave JSON | Valore | Uso form CIFS |
|-------------|--------|----------------|
| `hostIp` | IPv4 dell’interfaccia usabile al «Tutto pronto» | **Host** |
| `userQualified` | `COMPUTERNAME\MediaManager` (o utente scelto al passo 0) | **User** — già qualificato, non da ricomporre |
| `password` | Password effettiva usata dallo script per l’account SMB | **Password** |
| `port` | `445` | **Port** |
| `connectionName` | Nome connection (= share di default prodotto) | **Name** |
| `folder` | Pin share (stesso nome della share SMB) | **Folder** — obbligatorio; mai vuoto |
| `shareName` | Nome share SMB | Allineato a `folder` / Name |
| `computerName` | Hostname Windows | Solo diagnostica / ricostruzione; User è già in `userQualified` |
| `storage` | `SMB2,3 (jCIFS NG)` | **Storage** (costante prodotto) |
| `token` | Token sessione wizard (anti-bundle-stale) | Gate import |

### Completezza (`isComplete`) — gate duro

Il bundle è accettato **solo se** tutti veri:

1. `hostIp` è IPv4 valido (non hostname, non vuoto, non placeholder)  
2. `userQualified` non vuoto e contiene `\` (`PC\user`)  
3. `password` non vuota  
4. `port` = `445` (o normalizzabile a 445)  
5. `folder` non vuoto (= nome share)  
6. `connectionName` non vuoto  
7. `storage` = `SMB2,3 (jCIFS NG)` (o costante prodotto equivalente)  
8. `token` combacia con la sessione wizard in corso (se presente)

Se manca un campo → **nessuna** scheda Copia parziale; messaggio: riprovare lo scarico / rieseguire lo script.  
Vietato riempire i buchi con valori «di cortesia» inventati dall’app.

---

## Form speculare a CIFS — regola della raccomandazione Renato

> Il form speculare a quello di CIFS deve essere **interamente compilato** e **tutti i dati** devono poter essere **copiati** senza chiedere all’utente **integrazioni o modifiche** dei dati raccolti.

### Campi dati (valore definitivo + Copia obbligatorio)

| Etichetta CIFS (come in provider) | Origine | Copia | Vietato |
|-----------------------------------|---------|-------|---------|
| **Name** | `connectionName` | Sì | Placeholder / default diverso dal bundle |
| **Storage** | costante `SMB2,3 (jCIFS NG)` | Sì | Chiedere scelta manuale |
| **Host** | `hostIp` | Sì | Hint «leggi dalla finestra Tutto pronto»; Host vuoto; digitazione |
| **Port** | `445` | Sì | — |
| **Folder** | `folder` (pin share) | Sì | Campo assente o vuoto (rompe sede scrivibile) |
| **User** | `userQualified` | Sì | Mostrare solo `MediaManager` senza `PC\`; far concatenare all’utente |
| **Password** | `password` | Sì (+ Mostra/Nascondi) | Password solo «in sessione app» se bundle non la riporta |

### Campi fissi di procedura (non sono «dati raccolti» da integrare)

| Etichetta | Comportamento UI | Azione utente in CIFS |
|-----------|------------------|------------------------|
| **ID** | Testo «(generato da CIFS)» — niente Copia | Non modificare |
| **Domain** | Valore mostrato vuoto / «(lascia vuoto)» — Copia del vuoto opzionale | Lascia vuoto |
| **Enable DFS** | «Non attivo» | Lascia deselezionato |
| **Read only** *(se presente nel form CIFS installato)* | Istruzione fissa: disattivato | Deve restare OFF |
| **Safe data transfer** *(se presente)* | Istruzione fissa consigliata: ON | Come da checklist |

Queste istruzioni **non** contano come modifica dei dati del bundle: sono impostazioni fisse del provider.

### Vietato esplicitamente

- Host / User / Password / Folder / Name parziali o da completare a mano  
- «Copia tutto» che omette Host o Password  
- Far usare la lente di scansione CIFS come percorso felice  
- Far trascrivere dalla console «Tutto pronto»  
- QR/URI senza password  
- Avanzare al passo 5 (o abilitare Continua verso verifica sede) senza `isComplete`

### Gate UI passo 5

1. Passo 5 **non si apre** (o resta in stato «in attesa bundle») finché l’import 4b non è `isComplete`.  
2. Ogni bottone **Copia** dei campi dati è abilitato solo con valore non vuoto dal bundle.  
3. Checklist operativa: un riquadro per ogni Copia dati + riquadri fissi (ID/Domain/DFS/Read only) + Save + Indietro + esito.  
4. OK del passo 5: autodichiarazione come oggi, **dopo** che i Copia erano tutti disponibili.

---

## Mapping rispetto all’implementazione attuale (USB)

| Oggi | Con questo LOCK |
|------|-----------------|
| `PC_AVAILABILITY` + cavo USB | Stesso passo; prerequisito **Bluetooth** |
| `PC_USB_PREPARE` (MTP) | `PC_BT_PREPARE` (pairing + ricevi file + export/invio) |
| `PC_COPY_FILES` (Download telefono → Desktop) | Assorbito / checklist «file sul Desktop» |
| `PC_RUN_SCRIPT` | Invariato + scrittura bundle su Desktop |
| *(gap)* Host vuoto / LAN | `PC_BT_DOWNLOAD_BUNDLE` (4b) |
| `W2_CIFS` con Host placeholder e senza Folder | Form completo da bundle; **Folder** obbligatorio in scheda |

---

## Criterio GO (prima di APK / nuova versione)

1. Unit: parse bundle → `isComplete`; reject senza Host/Password/User qualificati/Folder.  
2. Unit/UI-model: tutti i `CifsCopyField` (inclusi Host e Folder) espongono testo non vuoto solo da bundle.  
3. Nessun string/hint che chieda di leggere l’IP dalla finestra PC come happy path.  
4. Dry-run script (ambiente agent): scrive JSON bundle con gli stessi campi.  
5. Documentazione utente (videate) allineata a questo LOCK; USB non è più passo 1–3 obbligatorio.

## Fuori scope (questa LOCK)

- Agente Windows residente / governo remoto continuo via BT  
- Automatismo di push bundle dallo script senza gesto OS  
- Rendere SMB raggiungibile senza rete IP comune  
- Cambiare il form interno di CIFS Documents Provider (terza parte)

---

## Approvazioni

| Voce | Esito |
|------|--------|
| Flusso BT al posto di USB | OK Renato (conversazione 06/11) |
| Punto 4 = download richiesto dal telefono, no push automatico PC | OK Renato |
| Form CIFS interamente compilato, solo Copia, zero integrazioni/modifiche sui dati | Raccomandazione obbligatoria recepita in questa LOCK |
