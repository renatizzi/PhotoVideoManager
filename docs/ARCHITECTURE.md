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

## Checkpoint CP1 (in corso / completato in codice)

- Schema Catalogo v2: `displayName`/`adapterKind` su StorageLocation + `scan_sessions`
- `SourceRegistry`: bootstrap personale, registrazione cartelle SAF, refresh disponibilità
- Adapter `SafTreeStorageAdapter` + factory
- UI CONFIGURA → Archivio: elenco sorgenti, Aggiungi cartella, Rimuovi
