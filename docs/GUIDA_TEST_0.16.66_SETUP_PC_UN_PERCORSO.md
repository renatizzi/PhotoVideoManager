# Guida test 0.16.66 — Setup PC un percorso (MyMedia)

**Versione:** `0.16.66-preview` · **versionCode:** `107`  
**LOCK:** `docs/LOCK_SETUP_PC_UN_PERCORSO.md`  
**Workflow:** `docs/WORKFLOW_SETUP_PC_UN_PERCORSO.md`

## Cosa cambia rispetto a prima

- Niente scelta RAPIDA/GUIDATA: da Avvio con **PC** si entra subito nei macro-passi.
- Cartella/share/utente fissi: `C:\MyMedia` / `MyMedia` / `MyMedia`.
- User CIFS = `COMPUTERNAME\MyMedia` (Copia dopo «Tutto pronto»), non User nudo.
- Bottone **Scarica/Apri CIFS** in cima al passo CIFS (pattern BoxManager → Play Store).

## Checklist LOCK

| # | Prova | Atteso |
|---|-------|--------|
| 1 | Setup Catalogo → PC → Continua | Nessun menu rapida/guidata; passo disponibilità |
| 2 | Passi USB → file in Download → copia → CONFIGURA | OK gated; «Tutto pronto» |
| 3 | Dopo Tutto pronto → CIFS | User tipo `NOMEPC\MyMedia` (Copia); Host = IP; Folder vuoto |
| 4 | Path | Nessun campo path editabile; share `MyMedia` |
| 5 | Solo sede | Nessun setup sorgenti in questo filo |
| 6 | Bottone CIFS | Se non installato apre Play Store; se sì apre CIFS; poi Attiva collegamento → riga **MyMedia** |

## Fuori prova

Help contestuale completo per ogni passo; fonti Acquisisci +.
