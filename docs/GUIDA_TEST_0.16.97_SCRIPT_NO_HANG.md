# Guida test 0.16.97 — script senza hang + set overwrite

Build: `0.16.97-preview` (versionCode 138).

## Cause trovate (simulazione agente)

1. **Hang Passo 5**: `icacls … /T` sull’intero profilo `Users\Dell XPS 15 7590` (AppData, `.android`, …) non termina. Log fermo su `ACL profilo utente: Dell XPS 15 7590`.
2. **Set `(1)/(2)`**: Maggiordomo cercava `CONFIGURA.cmd (*)`, ma Windows/MTP crea `CONFIGURA (1).cmd` (numero **prima** dell’estensione). Il CMD cercava solo `sup.ps1` e falliva se c’era solo `sup (1).ps1`.

## Fix

- ACL: RX sulla **radice** profilo (no `/T`); `/T` solo su cartelle media (Immagini, Documenti, …) con **timeout** (continua se lento).
- Maggiordomo: riconosce `stem (n).ext`; consolida sui nomi canonici; non tocca `DATI_CIFS`/`no_pw`.
- `CONFIGURA.cmd`: se manca `sup.ps1`, usa `sup (n).ps1` più recente.

## Simulazioni eseguite in agente (prima del test telefono)

- `sim_maggiordomo_set_overwrite.py` — PASS (overwrite + rimozione `(1)`, bundle intatto)
- `sim_acl_profile_timeout.py` — PASS (hang riprodotto; media-only avanza)
- Unit test `WindowsCatalogSetupScript*` — BUILD SUCCESSFUL
- Artifact: `generated-sup.ps1`, `generated-CONFIGURA.cmd`

## Cosa fare sul PC (solo dopo APK)

1. Installa 0.16.97; Fase 1 → controlla Download TEL: solo `CONFIGURA.cmd`, `sup.ps1`, `QRCoder.dll` (senza `(1)` se possibile).
2. Sul Desktop: elimina eventuali `CONFIGURA (1)` / `sup (1)` residui, copia i 3 file (sovrascrivi).
3. Avvia `CONFIGURA.cmd` — deve arrivare a **Tutto pronto** senza restare fermo su ACL profilo.
4. In CIFS: `Users` → profilo deve essere elencato; apri Immagini/Documenti.
