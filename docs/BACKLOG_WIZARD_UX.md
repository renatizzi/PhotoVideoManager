# Backlog UX — Wizard Setup Catalogo (PC)

**Aggiornato:** 19/10/2026 — UIX 0.16.36: filiera Setup Catalogo allineata al PPT (passi 0–5), menu a tendina, checklist gated, file CONFIGURA/sup.  
**Valutazione:** `docs/reference/valutazione-revisione-guida-pptx.md`  
**PPT (fonte testi):** `docs/reference/revisione_guida.pptx`  
**Non usare come specifica:** `docs/reference/template-videate-wizard-pc.md`

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

### P0 — Bloccanti di metodo
1. **Help contestuale (nuovo)** — repository what/if (da catalogo Claude W-xxx + Nota §5.6), ingresso per passo, niente gestione errori lunga sulle pagine operative. *Non in 0.16.36.*

### P1 — Da implementare (decisioni già prese)
3. **Slide 0 condizionale** — ✅ 0.16.36: menu a tendina + campi PC/locale/stub Cloud-NAS.  
4. **Password usabilità** — ✅ CIFS con **Copia password** + Mostra/Nascondi.  
5. **Prepara file sull’OK** — ✅ export sull’OK del passo 2 (checklist USB).  
6. **Rinomina file** `CONFIGURA` / `sup` — ✅ 0.16.36.  
7. **Messaggio attesa** — ✅ in checklist app (voce «Tutto pronto»); in-script = opzionale.  
8. **Istruzione avvio** — ✅ doppio clic + UAC.  
9. **Chrome titolo/sottotitolo** — ✅ passi PPT 0–5 (non «Passo N di 10»).

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

## Verificato in 0.16.36 (testo/struttura vs PPT)

- Passo 0: tendina «Seleziona il dispositivo» + campi condizionati + OK  
- Passi 1–4: checklist + OK gated  
- Passo 5: scheda CIFS con Copia per campo (password inclusa)  
- File: CONFIGURA / sup  

Non verificato in questa tranche: help contestuale, Configura stile Utility, Cloud/NAS completi.
