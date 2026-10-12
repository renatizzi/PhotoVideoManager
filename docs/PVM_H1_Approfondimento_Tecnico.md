# H1 — Approfondimento tecnico implementativo (v1)

12/10/2026 — Cursor. Delega tecnica Renato. Allineato a Nota §7.1octies / interlocuzione §7.24.

## Decisione tecnica

| Elemento | Scelta v1 |
|---|---|
| Sede | `CatalogSeatStore` (SharedPreferences): kind + tree URI opzionale + etichetta |
| Fascicolo | File JSON `MediaManager/catalog_hub.json` nella cartella sede (SAF) oppure `filesDir/catalog_hub/` per W1 |
| Copia di lavoro | Room esistente (`pvm_catalog.db`) |
| Scheda nel fascicolo | id, kind, title, updatedAt, updatedByDeviceId, trashed, sha256 opzionale |
| Diff | Confronto per id: solo locale → pubblica; solo sede/più recente sede → recepisci; entrambi cambiati vs ultimo sync → conflitto |
| Conflitto | Nessuna scrittura finché l’utente non sceglie versione locale o sede |
| Aggiorna | Anteprima obbligatoria; Annulla = no-op |
| Device id | UUID installazione in prefs (`hub_device_id`) |

## Fuori v1 implementativa

Client Windows; sync background; merge campo-per-campo; cifratura fascicolo.
