# Backlog UX — Wizard Setup Catalogo (PC)

**Stato:** annotato, **non implementare ora** (richiesta Renato 17/10/2026).  
**Branch di riferimento:** `cursor/wizard-setup-pc-c-5645`  
**Nota 17/10 sera:** T1–T6 del percorso PC (C:/USB) sono in codice (0.16.33); serve ancora la prova end-to-end sul telefono con l’ultima build.

---

## IN EVIDENZA — Guida wizard (fino a revisione backlog)

**Documento di riferimento (allegato Renato 18/10/2026):**  
`docs/reference/Guida_wizard_cartella_condivisa.pdf`

- **Titolo interno:** «Cosa fare se il PC non riconosce il tuo dispositivo mobile» + sezione **«Punti di attenzione per Cursor»**.
- **Ambito dichiarato:** solo ramo PC/notebook, disco interno **C:**.
- **Stato in questa chat:** **prima volta** con questo file (`Guida_wizard_cartella_condivisa…`).  
  In precedenza c’era solo la bozza più corta `Cosa_fare_se_il_PC_non_riconosce_il_tuo_dispositivo_mobile_…pdf` (USB), non questo documento completo.
- **Obbligo agente:** tenerlo in evidenza e usarlo come input primario quando si riprende il backlog UX / guida. **Non implementare** le correzioni della sezione «Punti di attenzione» finché Renato non chiede di riprendere il backlog (salvo bug bloccanti già trattati altrove).

Copia anche in artifact di sessione: `/opt/cursor/artifacts/guida-wizard/Guida_wizard_cartella_condivisa.pdf`.

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
- Allineare eventuali testi della guida in `docs/reference/Guida_wizard_cartella_condivisa.pdf`.

---

## BL-UX-3 — Ristrutturare la pagina della guida

**Sintomo (test Renato):** la pagina della guida va strutturata diversamente (oggi troppo densa / poco scansionabile).

**Da fare in seguito (non ora):**
- Riprogettare layout e gerarchia della guida USB / fuori-app partendo da **`docs/reference/Guida_wizard_cartella_condivisa.pdf`** (documento in evidenza).
- Un’azione primaria per schermata; non moltiplicare micro-passi inutili.
- Validare con Renato prima di congelare i testi.

---

## Ordine suggerito quando si riprende

0. **Rileggere** `docs/reference/Guida_wizard_cartella_condivisa.pdf` (sezione «Punti di attenzione per Cursor» per prima).  
1. BL-UX-3 (struttura guida) — riduce confusione generale.  
2. BL-UX-2 (messaggio attesa script) — basso costo, alto sollievo in prova.  
3. BL-UX-1 (async / transizioni) — richiede prova su telefono + eventuali fix ViewModel/Compose.
