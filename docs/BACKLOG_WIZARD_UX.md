# Backlog UX — Wizard Setup Catalogo (PC)

**Aggiornato:** 19/10/2026 — feedback Renato su discrasie A–E recepito e chiuso.  
**Valutazione:** `docs/reference/valutazione-revisione-guida-pptx.md`  
**PPT:** `docs/reference/revisione_guida.pptx`

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

---

## Aperto — da fare prima/durante lo sviluppo UIX

### P0 — Bloccanti di metodo
1. **Help contestuale (nuovo)** — repository what/if (da catalogo Claude W-xxx + Nota §5.6), ingresso per passo, niente gestione errori lunga sulle pagine operative.  
2. **Contratto CIFS** — scheda + **Copia** (host/porta/folder/user/**password**) + Apri app; **niente auto-fill form**. Spike auto-fill solo se richiesto esplicitamente.

### P1 — Da implementare (decisioni già prese)
3. **Slide 0 condizionale** — etichette/campi adattati al dispositivo (PC → lettera+path; mobile → Memoria principale/Secondaria; Cloud/NAS → stub come oggi).  
4. **Password usabilità** — allo step CIFS: mostra PW già nota all’app + **Copia** (niente trascrizione da PC, niente tocco script).  
5. **Prepara file sull’OK** — export `CONFIGURA`/`sup` al conferma del passo checklist pre-copia (gate: tutte le spunte).  
6. **Rinomina file** `CONFIGURA` / `sup` — aggiornare exporter + testi.  
7. **Messaggio attesa** — obbligatorio in checklist app; in-script = opzionale/backlog script.  
8. **Istruzione avvio** — doppio clic + UAC (auto-elevate).

### P2 — Fuori Wizard PC immediato
9. Pagina **Configura** «stile Utility» (slide 2 PPT).  
10. Completare rami Cloud/NAS (oggi stub).  
11. Funzione stampa/PDF scheda dati (nice-to-have; printscreen utente resta fallback).

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

## Prossimo passo consigliato

1. Congelare contratto CIFS (manuale + Copia, inclusa password).  
2. Implementazione UI a tranche: chrome → slide 0 condizionale → checklist + OK/export → scheda CIFS con Copia → help contestuale.
