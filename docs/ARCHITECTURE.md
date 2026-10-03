# Photo&VideoManager — Architettura applicativa (M02)

Baseline: Nota Integrata v5.1. Decisioni tecniche di implementazione (non consolidano requisiti).

## Layer

| Layer | Package | Responsabilità |
|---|---|---|
| UI | `ui.*` | Compose, navigazione, Top/Bottom Bar, stati presentazione |
| Application | `application.*` | Orchestrazione casi d’uso (es. `CatalogFacade`) |
| Domain | `domain.model`, `domain.port`, `domain.policy` | Entità logiche, porte, policy; **nessun** path/FS/SMB |
| Adapters / Data | `data.catalog`, `data.storage` | Room CatalogStore; StorageAdapter concreti |
| Composition root | `di.AppContainer` | Wiring manuale |

## Porte predisposte

- `CatalogStore` — persistenza Catalogo
- `StorageAdapter` + `StorageCapability` — accesso fisico capability-based
- `PermissionGate` — autorizzazione logica app
- `AuthPort` — autenticazione risorsa (v1: `LocalTrustAuthPort`)
- `SyncPort` — estensione futura disabilitata (`DisabledSyncPort`)

## Sequenza implementativa ottimizzata (risorse)

1. **M02** (questa tranche): struttura progetto + porte + shell UI
2. **Fondamenta M03/M04** incluse nello scaffold (schema Room minimo + adapter FS locale) per evitare rebuild a vuoto
3. Poi vertical slice operativo: **M05 → M06 → M07** (censimento / acquisizione / dedup)
4. Shell UI completa (M18) e feature successive senza invertire dipendenze

## Checkpoint CP-S (shared shell)

- Contratti `ShellTab` / `ShellTopBarModel` / `UiScreenState`
- Token tema parity BoxManager + branding PVM blue
- `PvmScaffold` + Top Bar card + Bottom Bar 2 tab + tema persistito
- Checklist: `docs/SHELL_PARITY.md`

## Checkpoint CP3 (M06 acquisizione — fetta v1)

- `AcquisitionService`: staging → verify SHA-256 → commit copia nello spazio app personale
- Catalogo: `ImportSession` + fingerprint L2; sorgente non modificata
- UI Home → Acquisisci: elenco candidati da Catalogo, selezione, avvio acquisizione
- Destinazione Archivio Condiviso: tranche successiva

## Navigazione dashboard (2026-10)

- Bottom Bar a **5 tab**: Home · Organizza · Componi · Pubblica · Gestisci (scelta UX, parity struttura BoxManager)
- **Configura** via icona Top Bar (non in bottom)
- Home = dashboard KPI + accesso rapido; ogni macro-area = hub con lista spiegata
- Checklist aggiornata: `docs/SHELL_PARITY.md`

## Tema / palette

- **Ora:** switch chiaro/scuro in Top Bar (parity minima BoxManager) + branding PVM blue
- **Non ora:** selettore palette multiple (orange/green/…) di BoxManager ThemeManager — utile ma non bloccante; da valutare in Preferenze dopo le macrofunzioni prioritarie
- Help inline: già avviato (testi hub/dashboard); Guida contestuale più ricca = backlog

## Checkpoint CP4 (M07 Pulisci — fetta v1)

- `DedupService` + `ExactDedupGrouping`: duplicati esatti SHA-256 su MediaItem distinti
- Calcolo hash mancanti in lettura (senza modificare sorgenti)
- UI Gestisci → Pulisci: Analizza + revisione gruppi
- KPI Home: conteggio “duplicati” (elementi in più per gruppo)

## Checkpoint CP5 (tranche accorpata)

- **Pulisci**: “Metti nel Cestino i duplicati” con conferma (tiene il consigliato)
- **Cestino**: elenco, ripristina, elimina uno, svuota (file fisici solo nello spazio app)
- **Archivia v1**: elenco copie ACTIVE nello spazio personale
- Policy: cartelle SAF/telefono non cancellate in soft-delete; solo Catalogo

## Checkpoint CP6 (tranche accorpata)

- **Ricerca v1**: elenco Catalogo ACTIVE con filtro testo + tipo (foto/video/tutti)
- KPI Home: conteggio elementi nel Cestino

## Checkpoint CP7 (icona + miniature)

- Icona launcher con grafica + wordmark **Media**/**Manager** (come bozza inviata)
- Safe-zone adaptive per non tagliare titolo e elementi
- Miniature nelle liste: Acquisisci, Archivia, Ricerca, Pulisci, Cestino (Coil)

## Checkpoint CP8

- Acquisisci: filtro Tutti/Foto/Video (selezione allineata al filtro)
- Home: scorciatoie funzioni pronte + KPI cliccabili (Ricerca/Archivia/Cestino)
