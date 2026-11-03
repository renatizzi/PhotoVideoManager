# LOCK — Tab S9 conteggi e Ripristina (3 nov)

**Stato:** approvato  
**Data:** 2026-11-03  
**Richiesta / test di riferimento:** Modifiche del 3 novembre 2026 — Tab S9 (dopo prove su build recente)  
**Approvazione Renato:** 2026-11-03 OK (2 righe Acquisisci = Test1+Test2; file Duplicati nei 11 senza righe separate)

## Contesto test (invariante — non è elenco bug)

- Cartelle sul dispositivo: Main `Test1` (3 video) + `Duplicati` (1 foto + 2 video); SD `Test2` (2 foto + 1 video) + `Duplicati` (2 foto)
- TOT file nell’albero: **11** (5 foto + 6 video)
- TOT duplicati da eliminare: **5** (3 foto + 2 video)
- In Catalogo dopo pulizia: **6** (2 foto + 4 video)

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | In Acquisisci le cartelle selezionate vengono ancora duplicate (4 invece di 2) | In Acquisisci compaiono **2** cartelle (non 4) | Filiera dispositivi; altre schermate |
| 2 | Il totale degli elementi prima della conferma è di 8 elementi invece di 11 | Prima di Conferma: **11** elementi (5 foto + 6 video) | — |
| 3 | Il totale degli elementi dopo la conferma e importazione è di 8 elementi invece di 11 | Dopo Conferma/Importa: **11** elementi (5 foto + 6 video) | — |
| 4 | Il totale degli elementi eliminati dopo pulisci è di 3 elementi invece di 5 | Dopo Pulisci: **5** elementi spostati nel Cestino (3 foto + 2 video) | — |
| 5 | Il totale degli elementi in Catalogo dopo pulisci è di 5 invece dei 6 attesi | Dopo Pulisci: **6** in Catalogo (2 foto + 4 video) | — |
| 6 | Il dettaglio delle foto e video riportati è fuori standard: in alcuni casi riporta correttamente il percorso della cartella, in altri no | In Dettaglio, **ogni** riga mostra il percorso cartella in modo uniforme (stesso formato standard) | Cambi layout non legati al path |
| 7 | Nell’elenco dei file da ripristinare: messaggio richiesto | Testo esatto: «Sono stati trovati file in precedenza esclusi dal Catalogo e spostati nel Cestino. Selezionali e clicca su [Ripristina] per recuperarli» | Altri testi UI |
| 8 | Accanto al messaggio: bottone [Ripristina] e NON [Ripristina in Importa] | Etichetta bottone: **Ripristina** (mai «Ripristina in Importa») | — |
| 9 | Nel dettaglio invece di Escluso metti l’icona del Cestino | Al posto della dicitura «Escluso»: **icona Cestino** | Altri stati meta |

## Note operative (agente)

- Prima del codice: se Stato ≠ `approvato`, chiedere OK su questa tabella.
- FIX: solo righe `#N`; commit preferiti `fix(LOCK-N): …`.
- PROVA: guida test con gli stessi `#N`; nuova APK solo a checklist LOCK completa (REGOLE §11).
- Chiusura: Renato conferma OK per riga o elenco residuale.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
| 7 | | |
| 8 | | |
| 9 | | |
