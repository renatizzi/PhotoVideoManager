# LOCK — Test 0.16.101 Setup su sei percorsi

**Stato:** in attesa OK Renato  
**Data:** 2026-11-28  
**Richiesta / test di riferimento:** esito Renato 28/11/2026 — app `0.16.101-preview`; Catalogo sempre **MyMedia**; dispositivo prova **S25 Ultra di RENATO**; percorsi: Questo dispositivo, Altro dispositivo, PC C:, PC E:, Disco di rete, Cloud (Google)  
**Approvazione Renato:** in attesa

**Blocco piano:** finché questo LOCK non è **approvato** e poi **chiuso in prova**, non si apre la Fase B del piano assessment (§7.1novies). Solo fix sulle righe `#N` sotto.

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | **Questo dispositivo — KO.** La cartella Catalogo non viene creata (= nome Catalogo). La scelta cartella sul dispositivo non funziona (appare «fase terminata correttamente» ma non è vero). Dopo OK, Attiva ripropone «Seleziona cartella» e riapre i contenuti del dispositivo. Da rivedere completamente: la cartella va creata in automatico dal nome della prima pagina; si può saltare la selezione cartella e lasciare nella stessa videata la conferma di attivazione; se si modifica il nome si usa Setup in Configura (come già deciso). | (a) Dopo conferma, esiste sul dispositivo una cartella con il **nome Catalogo** della prima pagina (es. MyMedia). (b) **Niente** passo di selezione cartella in questo ramo. (c) Stessa videata: conferma di attivazione. (d) Cambio nome solo da Configura / Setup. (e) Nessun messaggio «fase terminata correttamente» se l’attivazione non è riuscita. | Rami PC/NAS/Cloud; redesign generale wizard oltre questo ramo |
| 2 | **Altro dispositivo (tablet) — KO.** Messaggio non chiaro. | Testo rivisto: l’utente capisce subito cosa può/non può fare (nessuna sede peer in v1; dove mettere le foto / come acquisirle). | Setup sede su tablet come peer; Wi‑Fi diretto tra telefoni |
| 3 | **PC disco C: / E: / Disco di rete — OK funzionali**, ma Domain e l’altra opzione (DFS) hanno formato diverso dalle altre voci CIFS (ci sono ancora quadrati di selezione e istruzioni). | Nella scheda CIFS, Domain e Enable DFS: **solo titolo** (e riquadro valore allineato se serve), **senza** quadrati/checkbox di selezione e **senza** istruzioni extra — stesso stile delle altre etichette CIFS. Vale per PC C:, PC E: e Disco di rete. | Altri campi CIFS già OK; app CX File Explorer |
| 4 | **PC C: / E: / Disco di rete:** le cartelle viste sul telefono restano quasi tutte in inglese (es. Pictures). Uniformarle **tutte** come in Windows. | Via CIFS, le cartelle note del profilo mostrano i **nomi come in Esplora risorse Windows** (es. Immagini, Documenti, …) — non solo alcune. Vale per share su C:, E: e disco di rete già setup con nuovo script se necessario. | Rinominare/cancellare cartelle di sistema inglesi; UI di app CIFS di terzi |
| 5 | **Cloud (Google) — OK.** Servevano diversi tentativi prima di vedere Drive in elenco (tempi di refresh lunghi). | Dopo «OK»/selezione Drive, l’elenco fonti mostra Drive **senza** dover ripetere a caso; se il refresh è lento, c’è un’indicazione chiara di attesa (non silenzio). | Account Google / provider Drive fuori app |

## Note operative (agente)

- Nessun codice finché Stato = `approvato`.
- `#3` e `#4` valgono per i tre percorsi PC C:, PC E:, Disco di rete (Renato: «come punto 3»).
- `#1` è priorità: redesign W1 locale, non patch cosmetica.
- `#4` estende/sostituisce il residuo di `docs/LOCK_CIFS_LOCALE_DATI_SCRUB.md` (#1 cartelle): qui si chiede **tutte** le cartelle note, non solo alcune.
- PROVA: guida test con gli stessi `#1`–`#5` su S25 Ultra; Catalogo MyMedia.
- Commit preferiti: `fix(LOCK-N): …` per riga.

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
