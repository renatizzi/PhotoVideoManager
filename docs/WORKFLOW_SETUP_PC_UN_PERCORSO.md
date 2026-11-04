# Workflow Setup Catalogo su PC — un percorso (bozza)

**Data:** 04/11/2026  
**Stato:** bozza per validazione Renato (visibilità workflow — **niente codice** finché non OK)  
**Decisioni di chat:** niente doppio binario come scelta utente; help contestuale per passo; checklist solo su macro con checkpoint veri; Catalogo prima, sorgenti dopo.

## Decisioni di prodotto

| Tema | Scelta |
|------|--------|
| RAPIDA / GUIDATA | **Tolte** come voci di prodotto. Un solo percorso. Eventuale salto tecnico interno solo se i dati PC sono già noti (non scelta utente). |
| Help | Contestuale, **solo sul passo corrente** |
| Checklist | Solo **macro-passi**, abilitate da **checkpoint** verificabili dove possibile |
| Catalogo vs sorgenti | **Progressivo:** prima sede Catalogo; poi Acquisisci → + quando serve |

## Ruoli

| Chi | Cosa |
|-----|------|
| Utente | USB / Trasferimento file · copia file · UAC · CIFS · scegli cartella |
| Script | Rete privata, firewall/share, utente, cartella, share, IP, annuncio dati |
| App | Genera file, ascolta/mostra dati, Copia, apre CIFS, pick SAF, verifica sede |

## Macro-passi

| ID | Macro | Checkpoint |
|----|-------|------------|
| S0 | Avvio — nome + PC (+ percorso opz.) | Dispositivo PC + nome ok |
| M1 | Prepara file sul telefono | CONFIGURA + sup presenti in Download |
| M2 | Porta file sul Desktop PC | Dichiarazione + ascolto rete (PC non verificabile dal telefono) |
| M3 | Script Windows (UAC → Tutto pronto) | Host+User ricevuti (o Host manuale + esito) |
| M4 | CIFS — Incolla e Salva | Torna in app per Attiva collegamento |
| M5 | Scegli MediaManagerCatalog | Verifica read/write sede OK |
| F | Fine | Sede persistita |

## Fuori da questo filo

- Setup di tutti i sorgenti all’inizio  
- Peer telefono↔telefono / scan Wi‑Fi  
- File Explorer+ al posto di CIFS  

## Sequenza di lavoro proposta

1. Validazione Renato di questo workflow  
2. LOCK tabella solo **sede PC**  
3. Implementazione UI + ritocchi script se servono  
4. Tranche successiva: fonti in Acquisisci +  
