# Proposta — Recupero bundle CIFS completo dopo «Tutto pronto»

**Stato:** in attesa OK Renato (QR handoff **respinto**)  
**Data:** 2026-11-06  
**Sostituisce come primario:** `docs/LOCK_RECUPERO_PC_HANDOFF_QR.md` (QR-first senza password)

## Verdetto

| Ruolo | Scelta | Motivo in una riga |
|-------|--------|-------------------|
| **Primario** | **A — Hotspot telefono obbligatorio** | Stessa rete controllata → announce affidabile **e** IP coerente con CIFS; payload = bundle completo **con password** |
| **Backup** | **B — Round-trip USB automatico** | Zero digitazione Host; bundle completo su supporto già in workflow; nessun annuncio di rete |

---

## Perché il QR era sbagliato

1. **Bundle incompleto di proposito** (password esclusa) → Copia verso CIFS resta a metà; Renato la vive come soluzione non chiusa, anche se la password è ancora in sessione app.  
2. **Soglia prodotto:** se resta anche un solo campo da recuperare a mano (Host in app ≈ Host in CIFS), il canale extra non vale.  
3. **Non risolve la rete:** dopo lo scan l’app ha un IP, ma su AP con client isolation CIFS può fallire comunque; il QR non crea un percorso L3 utilizzabile.  
4. **Fallback «Incolla URI»** = ancora copia manuale (stesso odore del JSON su Download, già KO storico).

---

## Ranking (migliore → peggiore)

| Rank | Opzione | Giudizio |
|------|---------|----------|
| **1** | **A Hotspot** | Primario. Topologia fissa (telefono = AP); announce esistente diventa affidabile; estendere payload a **tutti** i campi Copia (Host IPv4, User `COMPUTERNAME\MyMedia`, Password, Port 445, Folder `MyMedia`, Name). Stesso Wi‑Fi serve poi a CIFS → un solo vincolo di rete. |
| **2** | **B USB auto-write** | Backup. A «Tutto pronto» lo script scrive il **bundle pieno** sul volume da cui è partito CONFIGURA; l’app importa al reinserimento. Vietato chiedere al cliente di copiare JSON a mano (già KO 0.16.45); qui scrive lo **script**, importa l’**app**. |
| **3** | **C HTTP phone + IP telefono a console PC** | Un solo campo digitato, ma **è ancora un IP a mano** → rischio stesso rifiuto della soglia Host. Dipende comunque da rete raggiungibile. Solo terziario se A/B impossibili. |
| **4** | **E altre** | QR cifrato con password: resta QR (già respinto). BLE/Wi‑Fi Direct: costo/testabilità alti. Write-back MTP sul telefono ancora collegato: variante di B, fragile su Windows. Nessuna batte A+B. |
| **5** | **D Harden LAN home AP** | **Escluso.** Parcheggiato 23/10; isolation/FW/subnet strutturali; vietato come primario. |

---

## Come torna la password in Copia

**Contratto bundle (A e B, stessi campi):**

| Campo CIFS | Origine nel bundle |
|------------|--------------------|
| Host | IPv4 dell’interfaccia attiva al momento di «Tutto pronto» |
| User | `COMPUTERNAME\MyMedia` |
| Password | Quella generata all’export script (ora **inclusa** nel canale di recupero) |
| Port | `445` |
| Folder | `MyMedia` |
| Name | `MyMedia` |

- **A:** announce UDP/HTTP (già in codice) esteso → app riempie sessione → **tutti** i Copia abilitati, zero Host digitato.  
- **B:** stesso JSON/bundle su USB → import → stessi Copia.  
- La password **non** resta solo «in sessione se va bene»: arriva di nuovo dal PC insieme a Host/User, così Copia è un pacchetto unico.

---

## Flusso primario A (azione)

1. Prima di CONFIGURA (o prima del bind di rete): app istruisce **hotspot telefono ON**; PC si collega a quell’SSID.  
2. CONFIGURA → «Tutto pronto» stampa IP/User/Password in console (come oggi) **e** emette announce con **bundle completo**.  
3. App riceve → Host/User/Password/… pronti → Copia → CIFS sulla **stessa** rete hotspot.  
4. Fast-path LAN su AP di casa: resta opzionale silenzioso; **mai** gate di OK; **mai** messaggio «stessa Wi‑Fi» come happy path senza hotspot.

## Flusso backup B (quando A non si può)

PC senza Wi‑Fi, hotspot vietato, join fallito:

1. CONFIGURA gira da supporto rimovibile (o path noto all’export).  
2. A «Tutto pronto» scrive `MediaManager_setup_bundle` (nome prodotto da fissare in LOCK) **sullo stesso volume** — automatico.  
3. Utente reinserisce nel telefono → app importa → Copia completo.  
4. CIFS richiede comunque L3 verso quell’Host: se isolation blocca, tornare ad A (e rilevare IP sulla rete hotspot).

---

## Testabilità in agent Linux (senza telefono)

| Cosa | Come (senza phone/APK) |
|------|-------------------------|
| Codec/schema bundle + password obbligatoria | Unit test parse/reject (campi, IPv4, User con `\`, token sessione) |
| A — announce | UDP/HTTP loopback o rete Docker; assert payload = bundle pieno |
| B — USB | Mount/temp dir come “stick”; script dry-run scrive file; importer legge e popola modello Copia |
| CIFS UI Copia | Test su modello sessione: tutti i bottoni Copia abilitati solo se bundle completo |
| Gap accettato | Hotspot reale e fotocamera: solo sessione telefono finale **una volta** (REGOLE §5), dopo prove agent verdi |

---

## Criterio GO (prima di APK)

1. Unit bundle (con password) verdi in CI.  
2. Dry-run script: emit announce **o** file USB con gli stessi campi.  
3. Documentazione utente: hotspot come passo obbligatorio del percorso felice; USB reimport solo come piano B.  
4. Deprecare QR / `pvmsetup` senza password come recupero primario.

## Fuori scope

- Chiedere al cliente di copiare JSON/URI a mano sul telefono.  
- Digitazione Host in app come happy path.  
- Nuovi tentativi di “indovinare” la LAN domestica senza cambio topologia.
