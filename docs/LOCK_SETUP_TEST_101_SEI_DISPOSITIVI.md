# LOCK — Test 0.16.101 Setup su sei percorsi

**Stato:** in prova (`0.16.102-preview`)  
**Data:** 2026-11-28  
**Richiesta / test di riferimento:** esito Renato 28/11/2026 — app `0.16.101-preview`; Catalogo **MyMedia**; **S25 Ultra di RENATO**  
**Approvazione Renato:** 28/11/2026 — OK; `#2` = eliminare voce «Altro telefono» (caso in guida)  
**Build FIX:** `0.16.102-preview` — guida `docs/GUIDA_TEST_0.16.102_LOCK_101.md`

**Blocco piano:** niente Fase B finché LOCK **approvato** e poi **chiuso in prova**. Solo fix `#N`.

---

## Verifica preventiva (richiesta Renato prima del FIX)

### A) Sostenibilità proposta W1 (#1)

| Domanda | Esito verifica codice/Nota |
|---|---|
| La proposta (cartella = nome, niente pick, conferma stessa videata, rename da Configura) è sostenibile? | **Sì.** Allinea la Nota consolidata W1 («nessun OpenDocumentTree obbligatorio») da cui il codice attuale è **deviato** (oggi forza SAF + fasi checklist con falso «fase terminata»). |
| Dove vive la cartella? | Creazione automatica sotto storage accessibile all’app (es. Documents/`NomeCatalogo` o area dedicata) + sede `LOCAL_DEVICE` senza URI obbligatorio; hub locale già previsto in `HubPackageIo`. |
| Rischio Android | Su Android 10+ non si crea una cartella arbitraria «ovunque» senza permessi; il percorso standard Documents/app è sostenibile e visibile all’utente. |
| Rename | Solo Configura/Setup — già deciso; non tocca il ramo PC. |

### B) Serve un redesign generale wizard per PC / NAS / Cloud?

| Ramo | Esito prova 101 | Redesign generale? |
|---|---|---|
| PC (C: / E:) | OK funzionale | **No** — solo residuali LOCK `#3` (UI Domain/DFS) e `#4` (nomi cartelle) |
| Disco di rete | OK funzionale | **No** — stessi residuali `#3`/`#4` |
| Cloud | OK | **No** — solo `#5` refresh/attesa |
| Questo dispositivo | KO | **Sì, ma solo ramo W1** (non copiare il modello auto-cartella su PC/NAS/Cloud: lì la cartella remota **deve** restare scelta esplicita) |

**Conclusione B:** nessun redesign generale del wizard. Redesign **solo** del ramo «Questo dispositivo».

### C) «Altro dispositivo» — messaggio dopo reachability vs eliminare

| Fatto | Evidenza |
|---|---|
| MediaManager vede un altro telefono/tablet come sede? | **No.** Nessun `CatalogSeatKind` peer; nessun discovery LAN/Wi‑Fi/BT peer. |
| Setup Catalogo menu oggi | Solo 4 voci: Questo dispositivo · PC Windows · Cloud · Disco di Rete. **Nessuna** voce «Altro dispositivo» in Setup. |
| Dove compare oggi | **Acquisisci → + → «Su un altro telefono»** (solo dialog di aiuto, niente collegamento). |
| «Verificare se raggiungibile e poi mostrare messaggio» | **Non sostenibile in v1:** richiederebbe discovery peer (fuori scope Nota/LOCK Wi‑Fi semplice). |

**Raccomandazione agente (da confermare Renato):**  
1. **Non** introdurre reachability peer.  
2. In **Setup Catalogo:** nessuna opzione «Altro dispositivo» (già assente — non aggiungerla).  
3. In **Acquisisci → +:** rinominare l’ultima voce in **«Altro dispositivo»** e tenere **solo** testo guida chiaro (o spostare tutto in guida e togliere la voce — preferenza Renato).  
4. LOCK `#2` sotto recepisce questa linea.

---

## Tabella LOCK

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | **Questo dispositivo — KO.** Cartella non creata; scelta cartella finta OK; Attiva ripropone Seleziona cartella. Proposta: cartella automatica = nome prima pagina; saltare selezione; stessa videata conferma attivazione; modifica nome da Configura. | (a) Dopo conferma, cartella con **nome Catalogo** creata e sede attiva. (b) **Niente** selezione cartella in W1. (c) Stessa videata: conferma attivazione. (d) Cambio nome solo Configura. (e) Nessun falso «fase terminata». | Redesign PC/NAS/Cloud; pick SAF obbligatorio su W1 |
| 2 | **Altro dispositivo — KO** (messaggio non chiaro). Renato: eliminare la voce se mai visibile. | **Decisione OK Renato:** eliminare «Su un altro telefono» da Acquisisci → +. Il caso resta documentato in guida (foto da altro mobile → cartella condivisa PC/NAS/Drive, poi + su questo telefono). | Discovery peer; sede su secondo telefono |
| 3 | PC C:/E:/Disco rete OK, ma Domain/DFS formato diverso (quadrati + istruzioni) | Domain e DFS: **solo titolo** (+ riquadro valore se serve), senza checkbox/quadrati e senza istruzioni extra | Altri campi CIFS; CX File Explorer |
| 4 | Cartelle CIFS quasi tutte in inglese | Tutte le cartelle note del profilo con nomi come in Windows | Cancellare nomi FS inglesi; UI app CIFS |
| 5 | Cloud OK ma Drive in elenco dopo più tentativi | Drive compare senza tentativi a caso; se lento, indicazione di attesa | Account Google |

## Note operative (agente)

- Nessun codice finché Stato = `approvato` (con conferma su `#2`: tenere voce Acquisisci o solo guida).
- Commit: `fix(LOCK-N): …`

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
