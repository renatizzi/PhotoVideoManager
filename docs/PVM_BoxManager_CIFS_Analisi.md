# Analisi BoxManager → MediaManager: disco di rete / CIFS

**15/10/2026 — Cursor.** Fonte: `renatizzi/BoxManagerNew` (clone lettura) + docs famiglia GUIDA_DISCO_RETE / Allegato 4.22.

## Verdetto

BoxManager **non parla SMB**. Usa **CIFS Documents Provider** (`com.wa2c.android.cifsdocumentsprovider`) e poi **SAF** (`ACTION_OPEN_DOCUMENT_TREE` + URI persistente). Stesso modello già scelto per MediaManager S6-A.

## Flusso BoxManager (archivio / backup su rete)

1. Impostazioni → **Disco di rete** (premium Archivio completo).
2. `NetworkDriveAssistant`: se CIFS manca → Play Store; se c’è → **Apri l’app**.
3. L’utente configura Host / porta 445 / utente / password / cartella **dentro CIFS** (credenziali solo lì).
4. In BoxManager → Sfoglia → sceglie la cartella esposta da CIFS (a volte **Tutte le cartelle…** → ☰ → CIFS).
5. `takePersistableUriPermission` + salvataggio tree URI in preferenze. I/O solo via `DocumentFile`.

Documentazione utente: `docs/famiglia/GUIDA_DISCO_RETE.md`.  
Codice chiave: `storage/NetworkDriveAssistant.kt`, `StorageFolderPicker.kt`, `StorageFolderRoots.kt`, `<queries>` in Manifest.

## Cosa non fa BoxManager

- Client SMB nativo (esplicitamente fuori V1).
- Export/import profili connessione, QR credenziali, scan LAN:445.
- Memorizzazione password SMB in-app.

## Riuso consigliato in MediaManager (W2 / sede Catalogo)

| Da portare | Perché |
|---|---|
| Stesso package CIFS + `<queries>` + Installa/Apri | Stessa esperienza di BoxManager |
| Dialogo setup minimalista (campi a–f come guida) | Coerenza famiglia prodotti |
| `SHOW_ADVANCED` + eventuale elenco root DocumentsProvider | Trovare CIFS senza caccia nel ☰ |
| Solo URI sede; password mai in MediaManager | Allineato a S6-A e a BM |
| Script PC W2 (già in PVM) | BoxManager non ha l’equivalente; resta vantaggio PVM |

## Implementato in MediaManager 0.16.26

- `NetworkDriveAssistant` + `<queries>` (stesso package BoxManager)
- Wizard W2: script PC → passo CIFS (Installa/Apri) → `OpenDocumentTreeAdvanced` → sede
- W4 NAS riusa Installa/Apri CIFS
- Fine wizard: ripresa Catalogo (Acquisisci / Aggiorna Catalogo)

Verifica telefono: dopo «Tutto pronto» sul PC, in CIFS Host/445/MediaManager/password/share, poi Scegli cartella in MediaManager.
