# LOCK — Interscambio Wi‑Fi semplice (fonti dispositivi)

**Stato:** in attesa OK Renato  
**Data:** 2026-11-03  
**Richiesta / test di riferimento:** chat 03/11 — preoccupazione routine farraginose; OK di direzione sulla cartella condivisa se non c’è alternativa  
**Approvazione Renato:** in attesa  
**Proposta di prodotto:** `docs/PROPOSTA_INTERSCAMBIO_WIFI_SEMPLICE.md`

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Le routine finiscono per complicare invece di semplificare | Percorso **corto di default**: se la cartella in rete è già pronta → solo collega + **Scegli cartella**. Nessuna checklist USB/script obbligatoria in quel caso | Nuovo wizard inventato; scan Wi‑Fi; peer telefono↔telefono |
| 2 | Deve essere comprensibile a basso profilo informatico | Una sola idea utente: **«cartella in rete = posto di incontro»**. In Acquisisci → Aggiungi: scelta tipo in linguaggio semplice (Questo telefono / PC / Disco di rete / Drive / Altro telefono). Max **una** schermata di spiegazione prima di Scegli cartella | Gergo SMB/SAF in etichette principali; cloud obbligatorio |
| 3 | Distinzione mobile / PC / disco di rete | Distinzione **solo** al collegamento (scelta tipo + per PC domanda Sì/No «cartella già pronta?»). Uso quotidiano = stesso Acquisisci→Importa | Tre filiere d’uso diverse dopo il collegamento |
| 4 | Non ripetere il setup Catalogo/sorgenti in modo pesante | Si **riusa** il meccanismo già noto (CIFS / Disco di rete / +). Non si obbliga a rifare la **sede Catalogo** per aggiungere una fonte foto. Procedura lunga PC solo se utente dice che la share **non** è pronta | Riscrittura completa wizard sede; recupero auto IP |
| 5 | Altro telefono come sorgente | Nessuna falsa procedura “collega telefono in Wi‑Fi”. Testo onesto: metti i file nella cartella condivisa (o Drive), poi acquisisci da lì. Help cavo/Quick Share solo secondario | Discovery peer MediaManager; Quick Share come percorso principale |
| 6 | Visibilità in Acquisisci | Dopo il collegamento, il dispositivo/fonte compare in Acquisisci con stato Disponibile / Non in rete; si acquisisce come le altre fonti | Smart TV / IoT |

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
