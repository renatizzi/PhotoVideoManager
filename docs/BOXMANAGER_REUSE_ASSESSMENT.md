# Assessment riuso BoxManager → Photo&VideoManager

Data: 2026-10-01  
Fonti: `renatizzi/BoxManagerNew` (clone lettura), Nota Integrata v5.1 (E8 / M18), Interlocuzione (shared shell).

## Verdetto

**Sì: conviene fissare ora il perimetro condiviso**, prima di costruire altre macrofunzioni UI.  
Altrimenti Top/Bottom Bar, tema e stati schermata andrebbero rifatti più volte.

**Vincolo tecnico importante:** BoxManager è **Views/XML + Activity**; PVM è **Jetpack Compose**.  
Non si possono “incollare” i layout XML così come sono. Il riuso utile è:

1. **contratti / slot / comportamento** (cosa mostra la shell, in che ordine, quali regole),
2. **token di tema** (colori top/bottom, night mode, palette),
3. **pattern applicativi** (backup ZIP/manifest, cestino, i18n, dialog/conferme),
4. **parity checklist** (stessa esperienza percettiva, implementazione Compose dedicata).

Non riusare: domain BoxManager, backend/obblighi rete, bottom nav a 5 tab, codice Activity-specifico.

## Matrice riuso

| Area BoxManager | Riuso proposto | Modalità | Priorità | Note |
|---|---|---|---|---|
| Top Bar slots (titolo, versione, utente, data/ora, guida, dark) | Sì | Contratto + Compose | **P0** | Layout XML `layout_global_topbar` = specifica slot; PVM già parziale |
| Bottom Bar globale | Sì (solo Home + Impostazioni) | Contratto + Compose | **P0** | BM ha 5 tab; Nota PVM vieta scorciatoie macrofunzioni in bottom |
| `ThemeManager` (night + palette + colori shell) | Sì | Porting logico → token Compose | **P0** | Evita drift tema tra app |
| `LocaleManager` / i18n | Sì | Porting + strings | **P1** | Dopo shell stabile |
| `BaseActivity` app-shell / edge-to-edge | Parziale | Pattern → `PvmScaffold` | **P0** | Non la classe Activity |
| Dialog / feedback / conferma distruttive | Sì | Pattern Compose | **P1** | Allineare stati UI Nota §5 |
| Backup ZIP/manifest/coordinator | Sì selettivo | Libreria/pattern in M15 | **P2** | Domain backup PVM distinto (solo Archivio Condiviso) |
| Trash retention/store | Sì selettivo | Pattern in M16 | **P2** | Solo contenuti consolidati FAMILY |
| SAF helpers (`SafFolderLabel`) | Sì | Utilità | **P1** | Affina CP1/CP2 |
| Family catalog / QR / premium / box domain | No | — | — | Dominio diverso; rischio contaminazione |
| BottomNav 5 destinazioni BM | No | — | — | Confligge con Nota PVM §5.2 |

## Strategia consigliata (anti-riciclo)

### Fase S0 — Shared Shell Contract (subito, prima di M06+)
Definire in PVM (documento + codice minimo):

- `ShellSlots` Top Bar (titolo prodotto, versione, utente, clock, help, theme toggle)
- `ShellTabs` Bottom (HOME, SETTINGS only)
- `ShellThemeTokens` (bg/title/subtitle/accent/bottom active-inactive, light/dark)
- `UiStateKit` nomi stati: loading / empty / error / unavailable / confirm-destructive
- Checklist parity vs BoxManager (cosa deve “sembrare uguale”)

Output: modulo interno `ui/shell` + `docs/SHELL_PARITY.md`.  
Niente multi-repo obbligatorio nella v1.

### Fase S1 — Allineamento visivo PVM alla shell
Portare Top/Bottom/tema PVM a parity (Compose), senza cambiare requisiti funzionali.

### Fase S2 — Utility condivise non-UI
i18n, dialog pattern, etichette SAF.

### Fase S3 — Riuso selettivo Backup/Cestino
Solo quando si aprono M15/M16; estrarre idee/algoritmi, non dipendere dal DB BoxManager.

## Impatto sul macropiano

Ordine aggiornato:

1. CP0/CP1/CP2 già fatti (scaffold, sorgenti, censimento) — restano validi  
2. **CP-S — Shared shell assessment + contratto + parity Top/Bottom/tema** ← inserito ora  
3. Poi riprendere funzioni: Acquisisci (M06) → Pulisci exact (M07) → …

Così le schermate successive nascono già sulla cornice giusta.

## Cosa non fare

- Non creare un monorepo forzato BoxManager+PVM in questa fase  
- Non copiare `BottomNavManager` a 5 tab  
- Non importare backend/family sync di BoxManager nel local-first PVM  
- Non dichiarare “shared library” pubblicata finché i contratti non sono stabili
