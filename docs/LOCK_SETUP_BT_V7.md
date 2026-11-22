# LOCK — Setup Catalogo BT v7 (REV9 + porte)

**Stato:** in test  
**Data:** 2026-11-19  
**Approvazione sviluppo Renato:** 19/11/2026 (autorizza sviluppo; test telefono solo dopo verifica ambiente agente)  
**Versione:** `0.16.74-preview` / `versionCode` 115

| # | Osservazione (Renato) | Risultato atteso (misurabile) | Fuori scope |
|---|----------------------|-------------------------------|-------------|
| 1 | Parte 1 REV9 = PDF stampa/visualizza | Tap «qui 🖨» apre PDF REV9 (`ACTION_VIEW`), non share testo | Editor PDF / PrintManager custom |
| 2 | Parte 2 online una fase / ramo | Solo fase corrente; Fase 1 solo ramo scelto; Fase 3 stesso metodo | Accordion multi-fase |
| 3 | Niente Continua / Puoi passare | CTA = Invia set / Apri istruzioni script / Usa questi dati / Attiva Catalogo | Autodichiarazioni «Confermo di aver…» |
| 4 | Indicatori onesti | Lampade ambra/verde su evidenza (invio, attesa, file ricevuto, import) | Fingere OK Desktop dal telefono |
| 5 | Ridurre caccia cartelle | Script maggiordomo + ascolto MediaStore/LAN; picker solo recupero | Agente PC preinstallato |
| 6 | File `(1)/(2)` bloccanti | Happy path rifiuta nomi con `(n)`; script rimuove duplicati | Rinominare file di terze parti già aperti |

## Note operative
- Guida test: `docs/GUIDA_TEST_0.16.74_BT_V7.md`
- Template: `docs/reference/PVM_Proposta_Setup_BT_REV9_v7.pdf`

## Esito test (dopo prova)

| # | OK / KO | Nota breve di Renato |
|---|---------|----------------------|
| 1 | | |
| 2 | | |
| 3 | | |
| 4 | | |
| 5 | | |
| 6 | | |
