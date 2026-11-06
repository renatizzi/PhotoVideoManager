# LOCK — Recupero dati PC → CIFS (QR-first)

**Stato:** in implementazione (prova protocollo in ambiente; **nessuna APK** finché prove verdi e filiera M3 stabile)  
**Data:** 2026-11-05  
**Contesto:** il recupero LAN (UDP 38455 + scan HTTP /24) è strutturalmente fragile (AP isolation, subnet, FW, token, TTL 5 min). Parcheggiato il 23/10; ripreso per tentativi → stesso KO. Renato chiede riprogettazione sostenibile, non altre versioni deboli.

| # | Osservazione | Risultato atteso (misurabile) | Fuori scope |
|---|--------------|-------------------------------|-------------|
| 1 | LAN non può essere il canale primario | Primario = **QR a «Tutto pronto»** (out-of-band). Zero dipendenza da broadcast/scan | Nuovo prodotto «scanner Wi‑Fi» |
| 2 | App deve ottenere Host IPv4 + User `COMPUTERNAME\MyMedia` senza digitare NOMEPC | Scan QR (o Incolla URI `pvmsetup:2;…`) con `setupToken` della sessione → campi pronti per **Copia** verso CIFS | Password nel QR/LAN |
| 3 | Copia IP importante | Dopo handoff, Host in app è pieno → **Copia Host** abilitata | Chiedere copia di `MediaManager_setup_result.json` sul telefono |
| 4 | LAN resta utile solo se «gratis» | Fast-path **silenzioso ≤15 s**; mai gate di OK; fallimento invisibile | Messaggi «stessa Wi‑Fi per ricevere IP» nel happy path |
| 5 | Replicabile / testabile senza telefono | Codec URI + reject regole in unit test; PNG QR decodificabile; script emette URI senza password | Solo prove telefono iterative |

## Protocollo

- URI: `pvmsetup:2;<base64url(JSON)>`
- JSON: `v,m=PVMSETUP1,t,ip,user,share,pc,exp` — **mai** password
- Accettazione: token sessione, `exp` non scaduto, IPv4, user con `\`
- Artefatti PC: QR PNG su Desktop + URI in `MediaManager_setup_handoff.txt` + testo sotto QR
- Encoder QR Windows: `QRCoder.dll` (MIT) esportato con CONFIGURA/sup

## Deprecato come primario

UDP blast + HTTP /24 scan come recupero utente; UX «l’app recupera da sola via Wi‑Fi».

## Criterio GO APK

1. Unit codec + QR roundtrip verdi in CI  
2. Script dry-run: URI senza password; istruzioni Scansiona  
3. Solo dopo: build APK per prova telefono (scan reale)
