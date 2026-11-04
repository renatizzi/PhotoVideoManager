# Tabella — 5 dispositivi di riferimento (Setup vs Acquisisci)

**Data:** 03/11/2026  
**Scopo:** capire dove collocare i passi nell’albero (Setup Catalogo · Acquisisci · altro).  
**Riferimenti:** `PROPOSTA_INTERSCAMBIO_WIFI_SEMPLICE.md`, `LOCK_INTERSCAMBIO_WIFI_SEMPLICE.md`, Nota W1–W4.

## Distinzione obbligatoria

| Job | Schermata | Quando |
|---|---|---|
| **Sede Catalogo** — dove vive il Catalogo | **Setup Catalogo** (Configura / riga Catalogo / primo avvio) | Una tantum (o cambio sede) |
| **Fonte foto** — da dove acquisisco i file | **Organizza → Acquisisci → +** | Ripetibile |
| **Non fare** | Terzo ramo «Setup Sorgenti» / scan Wi‑Fi | — |

---

## Tabella operativa

| # | Dispositivo | Passi — sede (Setup Catalogo) | Passi — fonte foto (Acquisisci → +) | Dove nell’albero | Nota |
|---|-------------|-------------------------------|--------------------------------------|------------------|------|
| 1 | **Stesso telefono / tablet** | 1. Setup → questo dispositivo · 2. Conferma / cartella locale se chiesta | 1. + → «Questo telefono» · 2. Scegli cartella · 3. Conferma → Importa | Setup **e** Acquisisci + | Nessun CIFS |
| 2 | **Altro telefono / tablet** | — (non è sede peer in v1) | 1. + → «Un altro telefono» (solo testo) · 2. Copia foto in cartella condivisa PC/NAS o Drive · 3. Aggiungi quella cartella (voci 3/4/5) · 4. Conferma | **Solo** Acquisisci + (aiuto) | Niente Wi‑Fi diretto |
| 3 | **PC / notebook** | 1. Setup → PC · 2. Se serve: crea share · 3. CIFS / Disco di rete · 4. Scegli cartella sede | 1. + → PC · 2a. Share già pronta? **Sì** → Apri CIFS → Scegli cartella · 2b. **No** → guida breve crea-share → CIFS → Scegli cartella · 3. Conferma | Setup = sede · Acquisisci + = fonti | CIFS resta; non rifare Setup per ogni fonte |
| 4 | **SMB / NAS / disco di rete** | 1. Setup → Disco di rete · 2. CIFS (share già sul NAS) · 3. Scegli cartella sede | 1. + → NAS · 2. **Niente** domanda «già pronta?» · 3. Apri CIFS se serve → Scegli cartella · 4. Conferma | Setup = sede · Acquisisci + = fonti | PC ≠ NAS (niente crea-share Windows) |
| 5 | **Cloud (Drive o simile)** | 1. Setup → Drive · 2. Scegli cartella Drive | 1. + → Drive · 2. Tip → Scegli cartella · 3. Conferma | Setup = sede · Acquisisci + = fonti | Opt-in; nessun CIFS |

---

## Raccomandazione collocazione (sintesi)

1. **Setup Catalogo:** voci 1, 3, 4, 5 (sede).  
2. **Acquisisci → +:** tutte e 5 (fonti; la 2 solo come spiegazione).  
3. **Altro:** non aggiungere un setup parallelo; help CIFS/cavo dentro i passi PC/NAS.
