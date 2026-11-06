# LOCK — Recupero bundle CIFS completo dopo «Tutto pronto»

**Stato:** in implementazione (QR handoff **bocciato** Renato 06/11)  
**Sostituisce:** `LOCK_RECUPERO_PC_HANDOFF_QR.md` come primario  
**Riferimento:** `PROPOSTA_RECUPERO_BUNDLE_CIFS.md`

| # | Osservazione | Risultato atteso | Fuori scope |
|---|--------------|------------------|-------------|
| 1 | Dopo Tutto pronto servono **tutti** i dati CIFS, non solo IP | Bundle: Host IPv4, User `PC\MyMedia`, **Password**, Port 445, Name/Folder `MyMedia` → tutti i Copia abilitati | Host digitato in app come happy path |
| 2 | LAN domestica fragile (isolation) | Primario: **hotspot telefono** → PC join → announce bundle pieno | Harden AP casa senza cambio topologia |
| 3 | Password non può mancare nel recupero | Password **nel** bundle (announce + file USB) | QR/URI senza password |
| 4 | Piano B senza rete | Script scrive bundle sullo **stesso volume** di CONFIGURA; app importa al reinserimento | Chiedere copia JSON a mano in Download |
| 5 | QR | Deprecato come recupero | Nuove proposte QR |

## Criterio GO APK
Unit bundle + dry-run announce/USB con password; hotspot istruito in UI; QR non è il percorso felice.
