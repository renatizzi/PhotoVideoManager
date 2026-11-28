# LOCK — Test 0.16.102 W1 messaggio/percorso + Acquisisci C:/Cloud

**Stato:** in attesa OK Renato  
**Data:** 2026-11-28  
**Richiesta / test di riferimento:** prova Renato 28/11/2026 ~15:38 Europe/Rome — app `0.16.102-preview`; Catalogo **MyMedia**; **S25 Ultra di RENATO**; test fermato dopo punti 1–3 (non proseguito su Domain/DFS/alias)  
**Approvazione Renato:** in attesa  
**Screenshot:** `/opt/cursor/artifacts/test-102-acquisisci-offline-cloud.png` (Acquisisci: sede `\Documenti\MyMedia`; CIFS Pictures **Offline** senza `C:`; Drive assente dall’elenco)

**Blocco:** niente codice finché Stato = `approvato`. Solo fix `#N`.

LOCK precedente `docs/LOCK_SETUP_TEST_101_SEI_DISPOSITIVI.md` (0.16.102 in prova): residuale / riaperto qui.

---

## Tabella LOCK

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Messaggio W1 non convincente: sembra che il Catalogo sia sempre MyMedia. Ha senso il nome nel riquadro ma **editabile**. Stessi criteri caratteri del nome CIFS (no speciali). Testo obbligatorio: *Il Catalogo verrà creato automaticamente su questo dispositivo in una cartella con lo stesso nome: per cambiarlo usa "Setup Catalogo Personale" in Configura.* | (a) Testo W1 = frase sopra (letterale). (b) Riquadro nome **editabile** prima di Attiva. (c) Validazione = stessi criteri `validateCatalogNameForPc` (3–20, inizia con lettera, solo lettere/numeri/_/- , niente spazi/speciali). (d) Attiva usa il nome digitato (non un MyMedia fisso in UI). | Redesign Avvio/Configura oltre rename; altri rami wizard |
| 2 | Cartella Documenti per il Catalogo locale è inventata. Deve usare gli stessi criteri già usati per PC e altri dispositivi: **`<nome catalogo>/Mediamanager` sulla root principale**. | Dopo Attiva: cartella hub su storage primario = `<NomeCatalogo>/MediaManager` (stesso criterio PC `X:\Nome\MediaManager`, costante hub `MediaManager`); sede attiva; in Acquisisci/status **niente** path `Documenti\…` come sede. | Spostare Cataloghi già creati sotto Documenti; redesign PC/NAS/Cloud |
| 3 | In Acquisisci: disco PC (CIFS già da test precedenti) **non** riconosciuto come `C:`; dopo tentativo + Cloud la riga CIFS è **Offline** e Cloud non compare; nuovo tentativo Cloud → «sorgente già presente» ma **non** in elenco (v. screenshot). | (a) Cartella del disco PC via CIFS: etichetta **PC Windows** e path con lettera disco (es. `C:\Users\…\Pictures`), non `Disco di rete / NAS \Users\…` senza lettera. (b) Dopo + Drive: la sorgente compare in elenco **oppure**, se messaggio «già presente», la riga è già visibile. (c) + Cloud non lascia Offline senza motivo le altre sorgenti raggiungibili. | Account Google; CX File Explorer; Domain/DFS (#3 LOCK 101); alias nomi cartelle (#4 LOCK 101) |

## Note operative (agente)

- Nessun codice finché OK esplicito su questa tabella.
- Commit: `fix(LOCK-N): …`
- Per `#2`: allineare a `HubPackage.RELATIVE_DIR` = `MediaManager` (Renato: «Mediamanager»).
- Per `#3`: con sede passata a «Questo dispositivo», oggi `matchesCatalogSeatTree` / path `C:` dipendono da sede PC — il FIX deve rispettare l’atteso misurabile sopra (etichetta/path e Drive in elenco), senza scope creep.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
