# Backlog UX — Wizard Setup Catalogo (PC)

**Aggiornato:** 23/10/2026 — proposta unica visibilità fonti; IP recovery parcheggiato.  
**Specifica UI:** `docs/reference/videate-utente-setup-catalogo-pc.md`  
**PPT V2:** `docs/reference/revisione_guida_V2.pptx`  
**Non usare come specifica:** `docs/reference/template-videate-wizard-pc.md`  
**Proposta blocco P0 Acquisisci (convalidata → 0.16.51):** `docs/PROPOSTA_VISIBILITA_FONTI_ACQUISISCI.md` · prova: `docs/GUIDA_TEST_0.16.51_ACQUISISCI.md`

---

## P0 — Bloccante prodotto (Acquisisci / fonti) — Renato 22/10

**Sintomo (S25):** in **Acquisisci** si vedono solo le cartelle del telefono, quelle di Drive e quelle di CIFS. **Nessuna visibilità** sugli altri dispositivi dove ci sono foto/video da importare nel Catalogo.

**Impatto:** senza questa funzione l’app **non regge più**.

**Obiettivo:** prevedere un **censimento dei dispositivi** con cui si può interagire **prima** di parlare di Catalogo (oppure un meccanismo equivalente con lo stesso risultato). Quando si apre Acquisisci devono essere visibili **tutti i dispositivi** (ambito Nota W1–W4) e quindi le **cartelle disponibili** utilizzabili come fonti.

**Stato:** proposta convalidata 23/10 → implementata in **0.16.51** (raggruppamento dispositivi, Aggiungi W1–W4, stati disponibilità). In verifica telefono.  
**Fuori blocco:** recupero automatico IP CIFS (parcheggio; IP manuale ok).

---

## Chiusi / assorbiti da questa proposta (rimossi come voci separate)

| Ex voce | Dove è andata |
|---|---|
| BL-UX-1 Pagine asincrone / taglio schermate | Chrome PPT (titolo/sottotitolo/intro) + sequenza a blocchi |
| BL-UX-2 Messaggio attesa script | PPT template 4 (nota attesa) — *implementazione in-script = tranche script* |
| BL-UX-3 Ristrutturare guida USB | PPT template 2–3 (checklist USB/copia) |
| Domande chiuse vs «Ho fatto X» | Checklist a riquadri + OK gated (metodo PPT) |
| Errori in-page / checkpoint in-page | Sostituiti da **Help contestuale** (da sviluppare) |
| Guida_wizard in evidenza come sola fonte | Superata da Nota + PPT; resta in `docs/reference/` come archivio |
| Slide 0 path unificato «sempre C:\» | **Chiuso:** campi volume/path **condizionati** al tipo dispositivo |
| «Esegui come Amministratore» da tasto destro | **Chiuso:** doppio clic + UAC |
| Step UI separato «Prepara file» | **Chiuso:** export sull’OK del passo checklist pre-copia |
| Password fissa `mediamanager` | **Non adottata** → Copia dall’app allo step CIFS |
| Pulsanti Sì/No / «Passo N di 10» | **Chiuso in 0.16.36:** combo + checklist PPT 0–5 |
| Nomi file lunghi | **Chiuso in 0.16.36:** `CONFIGURA` / `sup` |

---

## Aperto — da fare prima/durante lo sviluppo UIX

### P0 — Bloccanti di metodo (Wizard)
1. **Help contestuale (nuovo)** — repository what/if (da catalogo Claude W-xxx + Nota §5.6), ingresso per passo, niente gestione errori lunga sulle pagine operative. *Non in 0.16.36.*

### P1 — Tranche attiva (22–23/10): visibilità share + routine→app
2. **Visibilità cartella condivisa (Passo 5)** — ✅ Folder vuoto, share `MediaManagerCatalog`, testi Attiva collegamento.  
3. **User + Host senza impatto cliente** — ⏸ **PARCHEGGIATO (Renato 23/10):** KO fino a 0.16.49 (IP assente / Riprova sparito con `recoveringPcData`). Non riprendere finché non richiesto.  
4. **Script:** ✅ attesa; finestra resta con pause; annuncio rete; niente cleanup CONFIGURA a metà esecuzione.

### P1 — Già implementato
5. **Slide 0 condizionale** — ✅ 0.16.36: menu a tendina + campi PC/locale/stub Cloud-NAS.  
6. **Password usabilità** — ✅ CIFS con **Copia password** + Mostra/Nascondi.  
7. **Prepara file sull’OK** — ✅ export sull’OK del passo 2 (checklist USB).  
8. **Rinomina file** `CONFIGURA` / `sup` — ✅ 0.16.36.  
9. **Messaggio attesa** — ✅ in checklist app (voce «Tutto pronto»); in-script = voce 4 sopra.  
10. **Istruzione avvio** — ✅ doppio clic + UAC.  
11. **Chrome titolo/sottotitolo** — ✅ passi PPT 0–5 (non «Passo N di 10»).

### P2 — Fuori Wizard PC immediato
12. Pagina **Configura** «stile Utility» (slide 2 PPT).  
13. Completare rami Cloud/NAS (oggi stub).  
14. Funzione stampa/PDF scheda dati (nice-to-have; printscreen utente resta fallback).  
15. **Disco esterno su secondo PC** — verificare cosa succede se lo stesso volume (stessa lettera + percorso) configurato su un PC viene usato su un altro PC (share SMB / CIFS / MediaManager). Annotato Renato 22/10.

---

## Decisioni già congelate (invariate)

- Quick Share solo in help/Non riesco (non percorso principale)  
- Default path/share PC attuali  
- Drive/NAS: come oggi (stub) finché non completi — senza falsa completezza  
- Termine UI: **Posizione del Catalogo** (non «Sede»)  
- Filiera funzionale 0.16.33: non riaprire architettura sede / SMB nativo  
- Checklist = autodichiarazione granulare; prova reale = SAF  
- Non modificare lo script Windows «per opportunità» UI (PW via Copia in-app)

---

## Verificato in 0.16.36 (testo/struttura vs PPT)

- Passo 0: tendina «Seleziona il dispositivo» + campi condizionati + OK  
- Passi 1–4: checklist + OK gated  
- Passo 5: scheda CIFS con Copia per campo (password inclusa)  
- File: CONFIGURA / sup  

Non verificato in questa tranche: help contestuale, Configura stile Utility, Cloud/NAS completi.

---

## BL-UI — residuale dopo 0.16.54 (TO-BE integrazione 30/10)

Tranche **0.16.54** ha portato Avvio procedura, RAPIDA/GUIDATA PC/NAS, riga Catalogo Online/Offline/NV, Acquisisci senza Tutti, Componi switch ALBUM/RACCOLTA, `allowBackup=false`.

Aperti:
- **BL-UI-01** Host: scansione LAN (oggi solo Host manuale)
- **BL-UI-02** PC non-Windows (macOS/Linux)
- **BL-UI-03** Configura reorg stile Utility
- **BL-UI-06** Dettaglio RAPIDA (Host→CIFS→folder) — base in 0.16.54, raffinare testi
- **BL-UI-07** GUIDATA su Setup Sorgenti
- **BL-UI-08** Esc con dialog di conferma abbandono
- **BL-UI-09** Cloud oltre SAF (account)
- **BL-UI-10** Setup Sorgenti «cartella da censire» (landing dedicata)
- Font/stile carattere — correzione UI differita (accordo 30/10)
