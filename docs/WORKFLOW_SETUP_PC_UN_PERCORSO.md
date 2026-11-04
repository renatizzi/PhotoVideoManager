# Workflow Setup Catalogo su PC — un percorso

**Data:** 04/11/2026  
**Stato:** **CONVALIDATO** Renato 04/11 (workflow) + raccomandazioni account/`MyMedia`  
**Decisioni:** niente doppio binario; help per passo; checklist+checkpoint; Catalogo prima / sorgenti dopo; **account locale dedicato fisso**; **cartella Catalogo non editabile (`MyMedia`)**.

## Decisioni di prodotto

| Tema | Scelta |
|------|--------|
| RAPIDA / GUIDATA | **Tolte** come voci di prodotto. Un solo percorso. |
| Help | Contestuale, **solo sul passo corrente** |
| Checklist | Solo **macro-passi** + **checkpoint** verificabili |
| Catalogo vs sorgenti | **Progressivo:** prima sede; poi Acquisisci → + |
| Account Windows per CIFS | **Account locale dedicato fisso** creato dallo script (non account Microsoft, non PIN/impronta). Username fisso noto a priori; password generata dall’app → solo **Copia**. |
| Cartella / share Catalogo | **Fisse, non editabili** dall’utente. Proposta: cartella `C:\MyMedia`, share `MyMedia` (stesso nome corto ovunque). |

## Pacchetto credenziali CIFS (fisso)

Obiettivo: l’utente non deve più chiedersi «quale account?».

| Campo CIFS | Valore |
|------------|--------|
| Name | `MyMedia` |
| Host | IP (automatico o Copia / a mano) |
| Port | `445` |
| User | **`COMPUTERNAME\MyMedia`** (prova OK: `MINI_IT13\MediaManager`; il bare user era KO) → solo Copia. Non è il campo Host. Fallback help: Domain=COMPUTERNAME + User=MyMedia |
| Password | Solo **Copia** dall’app (provvisoria) |
| Domain / Folder | Vuoti; poi aprire la riga share `MyMedia` |

*Nota implementativa:* oggi lo script usa già un utente dedicato `MediaManager` e cartella `C:\MediaManager\Catalogo` / share `MediaManagerCatalog`. In implementazione si allineano i default a **`MyMedia`** (utente + cartella + share) per un solo vocabolo.

## Ruoli

| Chi | Cosa |
|-----|------|
| Utente | USB · copia file · UAC · CIFS (Copia/Incolla) · scegli riga `MyMedia` |
| Script | Rete privata, firewall/share, **crea account locale fisso**, cartella `C:\MyMedia`, share, IP, annuncio |
| App | Genera file/password, mostra Copia, apre CIFS, pick, verifica sede |

## Macro-passi

| ID | Macro | Checkpoint |
|----|-------|------------|
| S0 | Avvio — nome Catalogo logico + PC (niente path editabile) | Dispositivo PC + nome ok |
| M1 | Prepara file sul telefono | CONFIGURA + sup in Download |
| M2 | Porta file sul Desktop PC | Dichiarazione + ascolto rete |
| M3 | Script Windows (UAC → Tutto pronto) | Host+User ricevuti (o Host manuale + esito) |
| M4 | CIFS — Incolla e Salva | Torna in app |
| M5 | Scegli share **MyMedia** | Verifica read/write sede OK |
| F | Fine | Sede persistita |

## Fuori da questo filo

- Setup di tutti i sorgenti all’inizio  
- Peer telefono↔telefono / scan Wi‑Fi  
- File Explorer+ al posto di CIFS  
- Scelta lettera disco / path libero in v1 (solo `C:\MyMedia`) — dischi esterni PC: eventuale evoluzione dopo  

## Sequenza di lavoro

1. ~~Validazione workflow~~ **OK Renato 04/11**  
2. LOCK tabella sede PC (prossimo)  
3. Implementazione UI + allineamento script a `MyMedia`  
4. Tranche successiva: fonti in Acquisisci +  
