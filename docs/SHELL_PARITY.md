# Shell parity checklist — Photo&VideoManager vs BoxManager

Baseline: `docs/BOXMANAGER_REUSE_ASSESSMENT.md`, Nota Integrata v5.1 §5 / M18, proposta navigazione dashboard (2026-10).

## Slot Top Bar

| Slot | BoxManager | PVM Compose | Stato |
|---|---|---|---|
| Titolo prodotto | sì | sì (`app_name`) | OK |
| Versione | sì | sì (`VERSION_NAME`) | OK |
| Utente | sì (prefs) | sì (placeholder locale) | OK (username reale in M13/CONFIGURA) |
| Data/ora | sì `dd/MM/yyyy HH:mm` | sì stesso formato | OK |
| Configura | via tab BM | icona ingranaggio Top Bar | OK |
| Guida | sì | sì (dialog minimo) | OK (contenuti guida = backlog) |
| Dark mode | Switch | Switch + persistenza | OK |
| Card elevata colorata | MaterialCardView | Card 12dp / elevation 3 | OK |
| Sotto status bar | edge-to-edge + padding | `statusBarsPadding` | OK |

## Bottom Bar

| Voce | BoxManager | PVM | Stato |
|---|---|---|---|
| Globale su tutte le schermate shell | sì | sì (`PvmScaffold`) | OK |
| Destinazioni | 5 tab dominio BM | **Home + Organizza + Componi + Pubblica + Gestisci** | OK (scelta UX 2026-10; Configura fuori) |
| Configura / Impostazioni | tab BM | Top Bar (ingranaggio) | OK |
| Colori active/inactive da token | sì | sì | OK |

## Tema

| Aspetto | Stato |
|---|---|
| Token strutturati (top/bottom/accent/page) | OK |
| Persistenza night mode | OK (`ThemePreferences`) |
| Palette prodotto PVM (blue) | OK (struttura BM, branding PVM) |
| Accenti macro-aree (blu/verde/arancio/viola) | OK (hub + accesso rapido) |

## Stati UI (contratto nomi)

`INITIAL`, `LOADING`, `EMPTY`, `IN_PROGRESS`, `COMPLETED`, `ERROR`, `SOURCE_UNAVAILABLE`, `NEEDS_AUTHORIZATION`, `CONFIRM_DESTRUCTIVE`  
→ definiti in `UiScreenState`; adozione progressiva nelle schermate feature.

## Non in parity (volontariamente)

- Destinazioni esatte dei 5 tab BoxManager (dominio diverso)
- Domain/backend/family sync BoxManager
- Layout XML Views
