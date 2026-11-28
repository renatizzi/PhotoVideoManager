# Assessment codice — Photo&VideoManager 0.16.101

**Data:** 28/11/2026 (Europe/Rome)  
**Build di riferimento:** `0.16.101-preview` (versionCode 142)  
**Autore assessment:** agente Cursor (analisi codice + documentazione; **non** inventa nuovi requisiti)  
**Fonti:** `docs/PVM_Nota_Integrata.md` v5.3→v5.4 · `docs/BACKLOG_WIZARD_UX.md` · LOCK Setup USB/CIFS · package `app/src/main/java/...`

---

## 1. Sintesi in una frase

È in piedi un **vertical slice locale** (UI congelata + Catalogo + Acquisisci/Pulisci/Cestino + sede H1/W2 USB→CIFS) usabile in preview; **non** è ancora il prodotto completo della Nota (Componi, famiglia, backup Archivio Condiviso, editing, ricerca avanzata, Cloud/NAS completi).

---

## 2. Cosa è effettivamente sviluppato (evidenza in codice)

| Area Nota | Evidenza codice | Maturità |
|---|---|---|
| **M02 Architettura** | Layer `domain` / `application` / `data` / `ui`; Port `CatalogStore`; adapter SAF/MediaStore; hub separato | **Alta** (struttura stabile) |
| **M03 Catalogo** | `MediaItem` / `MediaCopy` / Room `CatalogDatabase`; dominio PERSONAL; fascicolo `catalog_hub.json` | **Media–alta** (v1 locale; FAMILY incompleto) |
| **M04 Storage** | SAF, locale, CIFS via Documents Provider esterno; capability non uniformi rispettate in pratica | **Media** (CIFS dipende da app terze; NAS/Cloud stub) |
| **M05 Censimento** | `CensusService`, UI Acquisisci, ricorsione cartelle | **Media–alta** |
| **M06 Acquisisci** | `AcquisitionService`, Importa, selezione fonti, anti re-import (BL-15) | **Media–alta** |
| **M07 Dedup** | `DedupService`, duplicati esatti; scarti BL-01 | **Media** (simili = BL-11 non ora) |
| **M09 Ricerca** | `SearchService` + UI search | **Bassa–media** (base; non M10) |
| **M16 Cestino** | `TrashService` + UI; focus Catalogo/app (non solo Archivio Condiviso come M16 pieno) | **Media** (v1 adattata al Catalogo) |
| **M18 UI** | Shell Home/Organizza/Componi/Utility; layout congelato v5.3 | **Alta** struttura; **media** completezza schermate |
| **BL-17 / H1** | Wizard W1–W4; W2 USB-only + script Windows + DATI_CIFS + CIFS pin; `CatalogHubService` + Aggiorna Catalogo con conflitti espliciti | **W2 in verifica multi-dispositivo**; W3/W4 guida breve; H1 sync schede presente ma da stress-test |
| **Setup PC (tranche recente)** | Script CONFIGURA/sup/QRCoder; Maggiordomo dual-pattern `(n)`; ACL profili senza hang; scrub post-import; alias locali Immagini→Pictures (0.16.101) | **In prova Renato** (LOCK CIFS locale aperto) |

### 2.1 Setup Catalogo PC — filo storico recente (codice)

| Versione | Contenuto principale |
|---|---|
| 0.16.96–99 | UX Fase3/Domain/Attiva; ACL; purge set; ordine CIFS; Salva/ANR |
| 0.16.100 | Scrub post-import + DATI_CIFS più recente + Domain/DFS allineati |
| 0.16.101 | Alias cartelle localizzate CIFS + scrub DATI_CIFS* aggressivo a fine sessione |

Percorso ufficiale corrente: **USB-only** (BT deprecato come primario). Bundle: `DATI_CIFS.json`. Share disco = radice volume + pin Folder.

---

## 3. Gap rispetto alla Nota Integrata (senza nuovi requisiti)

| Tema consolidato in Nota | Gap codice |
|---|---|
| W3 Cloud / W4 NAS obbligatori in v1 wizard | Rami presenti; **implementazione incompleta** vs W2 (guide/stub) |
| H1 Aggiorna Catalogo multi-dispositivo | Motore diff + UI esistono; **prova stress** tra dispositivi ancora da chiudere |
| M08 Organizzazione (persone/luoghi/tag) | Assente / minimale |
| M10 Riconoscimento | Assente (corretto: non v1 H1) |
| M11 Album/Componi | Landing/switch; **non** modello album completo |
| M12 Editing | Assente |
| M13 Condivisione familiare | Assente |
| M14 Pubblicazione/PDF/stampe | Assente / stub |
| M15 Backup Archivio Condiviso | Non allineato a M15 pieno |
| Help contestuale wizard (BACKLOG) | Non in app |
| Scan LAN Host (BL-UI-01) | Non fatto (Host manuale) |

---

## 4. Piano di sviluppo proposto (dopo prova 0.16.101)

Ordine pensato per **non spezzare** il test multi-dispositivo in corso e per restare dentro requisiti già in Nota/backlog.

### Fase A — Chiudere il filo sede PC (ora)

1. Esito Renato su **0.16.101** (LOCK `LOCK_CIFS_LOCALE_DATI_SCRUB`) su tutti i dispositivi.  
2. Eventuali fix LOCK-only; **chiudere** LOCK USB REV4 / MTP / CIFS locale.  
3. Checklist H1 §7.24 su **Aggiorna Catalogo** (pubblica / recepisci / conflitti) con sede su PC già setup.

**Esito atteso:** W2 dichiarato «verificato in campo» nella Nota.

### Fase B — Completare BL-17 v1 (W3/W4 + honesty)

4. W3 Drive e W4 NAS: o **completare** il percorso minimo accettabile, o rendere **esplicito** in UI «non ancora completo» (niente falsa completezza — già regola backlog).  
5. Help contestuale wizard (P0 BACKLOG).  
6. Residui BL-UI utili al setup (Esc conferma, pulizia dati app BL-UI-12) se richiesti da Renato.

### Fase C — Rafforzare Organizza (vertical slice già aperto)

7. Stabilizzare Acquisisci multi-fonte (già 0.16.51+) su tutti i dispositivi della tabella 5.  
8. Pulisci/Cestino: non regressione BL-01/12/15; nessun «simili» (BL-11).  
9. KPI Home: solo se emerge peso reale del gap (BL-13).

### Fase D — Macro successive (solo con specifica ChatGPT/Renato)

10. **M11 Componi** — riprendere solo dopo chiusura specifica vs landing.  
11. **M08/M09** organizzazione e ricerca strutturata.  
12. **M15/M16** Backup/Cestino allineati ad Archivio Condiviso.  
13. **M13/M14** famiglia e pubblicazione.  
14. **M12** editing.  
15. **M19/M20** integrazione e release.

---

## 5. Rischi tecnici da tenere in vista

- CIFS dipende da **CX File Explorer** (o equivalente): fuori controllo MediaManager.  
- MTP/MediaStore Samsung: overwrite file in Download resta fragile → scrub aggressivo necessario.  
- Script Windows ASCII-only: commenti/stringhe non ASCII rompono PowerShell 5.1.  
- Share = radice disco: ACL e AccessBased devono restare mirate (niente `/T` su profilo intero).

---

## 6. Documenti aggiornati insieme a questo assessment

- `docs/PVM_Nota_Integrata.md` → **v5.4** (§7.1 stato + §7.1novies + BL-17)  
- `docs/BACKLOG_WIZARD_UX.md` → stato USB/CIFS 0.16.96–101  
- `docs/PROMPT_CONTINUITA_AGENTE.md` → puntatore alla fase corrente  

La **fonte di stato** resta la Nota (§7); questo file è l’analisi Cursor a supporto.
