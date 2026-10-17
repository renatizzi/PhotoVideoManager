# Backlog UX — Wizard Setup Catalogo (PC)

**Stato:** annotato, **non implementare ora** (richiesta Renato 17/10/2026).  
**Branch di riferimento:** `cursor/wizard-setup-pc-c-5645`  
Proseguire prima le tranche T3–T6 del piano; riprendere questi punti in una tranche UX dedicata.

---

## BL-UX-1 — Pagine mostrate in modo asincrono

**Sintomo (test Renato):** le pagine del Wizard compaiono / si aggiornano in modo asincrono (transizioni poco chiare, contenuto che “salta” o si aggiorna fuori sequenza).

**Da fare in seguito (non ora):**
- Rendere le transizioni di passo sincrone e leggibili (un passo = una schermata stabile).
- Evitare aggiornamenti di stato che ridisegnano la pagina corrente in modo confuso (status/error/busy).
- Verificare su dispositivo reale dopo ogni tocco Avanti / Indietro.

---

## BL-UX-2 — Pausa lunga nello script PC: messaggio di attesa

**Sintomo (test Renato):** dopo i primi ~6 passi dello script Windows c’è una pausa molto lunga senza indicazioni.

**Da fare in seguito (non ora):**
- Nelle istruzioni in-app (S3c / «esegui script») e/o nello script stesso, avvisare prima della fase lenta con un testo del tipo:

  > Questa fase dura qualche minuto… aspetta di vedere il messaggio «Tutto pronto», dopo il quale potrai tornare all’app sul tuo dispositivo.

- Non simulare progresso falso; solo aspettativa chiara.

---

## BL-UX-3 — Ristrutturare la pagina della guida

**Sintomo (test Renato):** la pagina della guida va strutturata diversamente (oggi troppo densa / poco scansionabile).

**Da fare in seguito (non ora):**
- Riprogettare layout e gerarchia della guida USB / fuori-app (titoli, elenco numerato corto, un’azione primaria per schermata).
- Allineare a bozza interlocuzione S3b/S3c senza moltiplicare micro-passi inutili.
- Validare con Renato prima di congelare i testi.

---

## Ordine suggerito quando si riprende

1. BL-UX-3 (struttura guida) — riduce confusione generale.  
2. BL-UX-2 (messaggio attesa script) — basso costo, alto sollievo in prova.  
3. BL-UX-1 (async / transizioni) — richiede prova su telefono + eventuali fix ViewModel/Compose.
