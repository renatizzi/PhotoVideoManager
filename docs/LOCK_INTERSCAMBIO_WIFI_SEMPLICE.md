# LOCK — Interscambio Wi‑Fi semplice (fonti dispositivi)

**Stato:** approvato (procedi)  
**Data:** 2026-11-03  
**Richiesta / test di riferimento:** chat 03/11 — routine farraginose; chiarimento PC≠NAS/CIFS; «procedi pure… provo a seguirti»  
**Approvazione Renato:** 2026-11-03 OK di direzione (procedi)  
**Proposta di prodotto:** `docs/PROPOSTA_INTERSCAMBIO_WIFI_SEMPLICE.md`

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Le routine finiscono per complicare invece di semplificare | Percorso **corto di default** quando la share esiste già: CIFS/Disco di rete + **Scegli cartella**. USB/script solo se PC e utente dice che la share non è pronta | Nuovo wizard inventato; scan Wi‑Fi; peer telefono↔telefono |
| 2 | Deve essere comprensibile a basso profilo informatico | Idea utente: **«cartella in rete = posto di incontro»**. Aggiungi: Questo telefono / PC / Disco di rete / Drive / Altro telefono. Max **una** schermata di spiegazione prima di Scegli cartella | Gergo SMB/SAF in etichette principali; cloud obbligatorio |
| 3 | Distinzione mobile / PC / disco di rete; non equiparare PC e NAS | **PC:** domanda Sì/No «cartella già pronta?» (+ eventuale crea-share). **NAS:** niente quella domanda; share già sul NAS → CIFS + Scegli cartella. **CIFS resta** per entrambi sul telefono. Uso quotidiano = stesso Acquisisci→Importa | Tre filiere d’uso diverse dopo il collegamento; eliminare CIFS per NAS |
| 4 | Non ripetere il setup Catalogo/sorgenti in modo pesante | Si **riusa** CIFS / Disco di rete / +. Non si obbliga a rifare la **sede Catalogo** per una fonte foto. Procedura lunga solo PC se share assente | Riscrittura completa wizard sede; recupero auto IP |
| 5 | Altro telefono come sorgente | Nessuna falsa procedura “collega telefono in Wi‑Fi”. Testo onesto: file nella cartella condivisa (o Drive), poi acquisisci. Help cavo/Quick Share solo secondario | Discovery peer MediaManager; Quick Share come percorso principale |
| 6 | Visibilità in Acquisisci | Dopo il collegamento, dispositivo/fonte in Acquisisci con Disponibile / Non in rete; acquisizione come le altre fonti | Smart TV / IoT |

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
