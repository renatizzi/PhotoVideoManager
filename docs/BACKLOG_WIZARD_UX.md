# Backlog UX — Wizard Setup Catalogo (PC)

**Aggiornato:** 19/10/2026 dopo `revisione_guida.pptx` + valutazione critica.  
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

---

## Aperto — da fare prima/durante lo sviluppo UIX

### P0 — Bloccanti di metodo
1. **Help contestuale (nuovo)** — repository what/if (da catalogo Claude W-xxx + Nota §5.6), ingresso per passo, niente gestione errori lunga sulle pagine operative.  
2. **Contratto CIFS** — livello attuale: scheda + Copia + Apri app; **niente auto-fill form**. Spike auto-fill solo se richiesto esplicitamente.  
3. **Correzione slide 0** — non unire path `C:\` alla scelta dispositivo per tutti i rami; path solo per PC.

### P1 — Decisioni prodotto (richiedono OK esplicito)
4. **Password fissa `mediamanager`** — richiede modifica script + app (fuori «solo UI»).  
5. **Rinomina file** `CONFIGURA` / `sup` — OK di principio; aggiornare exporter + testi.  
6. **Messaggio attesa dentro lo script** — tranche script, non solo stringhe Wizard.  
7. **Istruzione avvio:** doppio clic (auto-elevate) vs «Esegui come Amministratore» da tasto destro — allineare al `.cmd` attuale.

### P2 — Fuori Wizard PC immediato
8. Pagina **Configura** «stile Utility» (slide 2 PPT).  
9. Completare rami Cloud/NAS (oggi stub).  
10. Funzione stampa/PDF scheda dati (nice-to-have; printscreen utente resta fallback).

---

## Decisioni già congelate (invariate)

- Quick Share solo in help/Non riesco (non percorso principale)  
- Default path/share PC attuali  
- Drive/NAS: come oggi (stub) finché non completi  
- Termine UI: **Posizione del Catalogo** (non «Sede»)  
- Filiera funzionale 0.16.33: non riaprire architettura sede / SMB nativo  

---

## Prossimo passo consigliato

1. Tuo OK (o correzioni) sulla valutazione critica §2–4.  
2. Congelare contratto CIFS (manuale + Copia).  
3. Solo dopo: implementazione UI a tranche (chrome → checklist passi → help contestuale).
