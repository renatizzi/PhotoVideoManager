# Guida test 0.16.62 — LOCK Tab S9 conteggi / Ripristina

Versione: `0.16.62-preview`  
LOCK: `docs/LOCK_TAB_S9_3NOV_CONTEGGI.md` (approvato 3/11/2026)

## Obiettivo

Chiudere i checkbox `#1`–`#9` del LOCK (nessun punto extra).

## Preparazione

1. Installare `0.16.62-preview` su Tab S9.
2. Struttura file come nel LOCK (Test1 + Duplicati su Main; Test2 + Duplicati su SD).
3. Preferibile ripartire da Catalogo/fonti puliti, oppure aprire Acquisisci (collassa da solo i figli duplicati).

## Procedura (= LOCK)

1. **#1** Collegare solo `Test1` e `Test2` (le due root). In Acquisisci: **2** cartelle (non 4).
2. **#2** Prima di Conferma: totale elementi **11** (5 foto + 6 video). Controllare anche Dettaglio path uniforme (**#6**).
3. Conferma → Importa: **#3** totale **11**.
4. Pulisci i duplicati: **#4** Cestino **5**; **#5** Catalogo **6**.
5. Elenco da ripristinare (Importa esclusi e/o Cestino):
   - **#7** messaggio esatto con «in precedenza» e «clicca su [Ripristina]»
   - **#8** bottone **[Ripristina]** (mai «Ripristina in Importa»)
   - **#9** icona Cestino al posto di «Escluso»

## Risultato atteso

Tutte le righe `#1`–`#9` OK sulla matrice 2 cartelle · 11 · 5 · 6.

## Segnali di errore

4 cartelle in Acquisisci; totali 8/3/5; path assente su alcune righe; testo/bottone Ripristina vecchi; dicitura «Escluso» senza icona.

## Riscontro

Screenshot Acquisisci (2 cartelle), Importa (11), dopo Pulisci (6 + Cestino 5), riga Ripristina con messaggio/bottone/icona.
