# MediaManager — istruzioni permanenti per agenti

All’avvio di ogni sessione, **leggere e applicare** le regole vincolanti:

**→ [`docs/REGOLE_COLLABORAZIONE_MEDIAMANAGER.md`](docs/REGOLE_COLLABORAZIONE_MEDIAMANAGER.md)**

Adottate il 23/10/2026. Valgono per tutte le sessioni. Non basta dichiararle comprese: vanno applicate.

## Sintesi operativa (non sostituisce il documento completo)

1. **Comunicazione:** italiano semplice verso Renato; niente gergo inutile; istruzioni con cosa / dove / risultato.
2. **Prima del codice:** coerenza con requisiti e piano; in caso di dubbio che cambia il risultato → chiarire; niente ricicli di analisi, niente scope creep.
3. **Protocollo LOCK (3/11/2026, REGOLE §11):** se Renato manda un elenco di bug/osservazioni → `docs/LOCK_<argomento>.md` da `docs/LOCK_TEMPLATE.md`; **fuori tabella = blocco totale**; nuova APK solo a LOCK chiuso in test. Senza LOCK approvato su elenco punti: bozza + attendere OK, non patchare.
4. **Test telefono:** non frammentare; una sessione finale per gruppo di lavoro; ogni test con obiettivo, preparazione, passi numerati, atteso, errori, riscontro (checkbox 1:1 sul LOCK se presente).
5. **Gate consegna (anti-spreco):** niente richiesta prova telefono senza `versionCode` massimo univoco, unit test pertinenti OK, `assembleDebug` OK, APK in artifact, e controlli/simulazioni possibili in ambiente agente. Percorso PC: `consegna-telefono.bat` → in caso di KO solo `build-consegna.log`.
6. **Piano:** non alterare ciò che non è richiesto; riprendere dallo stato già raggiunto.
7. **Documentazione vincolante di prodotto:** `docs/PVM_Nota_Integrata.md`, `docs/TERMINOLOGIA_UI.md`, backlog pertinenti (es. `docs/BACKLOG_WIZARD_UX.md`).

## Cursor Cloud specific instructions

**Niente prova telefono a vuoto (Renato 19/11/2026):** non chiedere a Renato di testare una nuova versione finché in questo ambiente non risultano verdi, nell’ordine:

1. unit test pertinenti (`:app:testDebugUnitTest` o subset mirato);
2. `:app:assembleDebug`;
3. controlli/simulazioni possibili qui (es. asset in APK, script generato, import/parse, overwrite/`(1)/(2)` dove simulabile);
4. APK in `/opt/cursor/artifacts/` con `versionName`/`versionCode` dichiarati + guida test aggiornata.

Solo dopo questi gate si può proporre la prova sul telefono (una sessione, allineata al LOCK se presente). Dettaglio vincolante: `docs/REGOLE_COLLABORAZIONE_MEDIAMANAGER.md` §9.

## Handoff storico

Per contesto di continuità (può essere datato): `docs/PROMPT_CONTINUITA_AGENTE.md` — le regole in `REGOLE_COLLABORAZIONE_MEDIAMANAGER.md` prevalgono in caso di contrasto su metodo di lavoro.
