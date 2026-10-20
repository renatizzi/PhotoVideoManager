# Analisi rigorosa — `revisione_guida_V2.pptx`

**Data:** 20/10/2026  
**Fonte:** `docs/reference/revisione_guida_V2.pptx` (correzioni in rosso rispetto ai template)  
**Confronto:** decisioni 19/10 (`valutazione-revisione-guida-pptx.md`), prompt continuità, UI **0.16.36**  
**Escluso da questo lavoro:** gestione errori / help what-if (slide 7, 10–12, 14–16)

Canvas: analisi strutturata nella sessione agente (stesso contenuto).

---

## Verdetto

Il documento **videate utente** non è ancora consolidato: ci sono conflitti materiali tra il rosso della V2 e decisioni già chiuse (soprattutto avvio script) e ambiguità di layout sul passo 0 / pagina introduttiva.  
**Non si procede d’iniziativa** sulle parti dubbie finché Renato non risponde alle domande Q1–Q7.

---

## Slide per slide

| Slide | Ruolo | Stato | Nota |
|---|---|---|---|
| 1 | Copertina | Fuori scope UI | Nessun testo operativo |
| 2 | Configura stile Utility | Fuori scope | DA FARE; non mescolare col Wizard |
| 3 | Chrome comune | Gap | Help non in documento UI di questa fase |
| 4 | Passo 0 | **Conflitto** | Percorso unico; sottotitolo senza n.; lista 1–5; testi da ripulire |
| 5 | Passi 1–2 + intro procedura | **Conflitto** | Dove vive il testo «Da questa pagina si attiva…»? |
| 6 | Passo 3 copia | Gap chiaro | Template 3 + CONFIGURA/sup; togliere testi extra |
| 7 | Help errori | Escluso | DA SVILUPPARE dopo UI |
| 8 | Passo 4 script | **Conflitto** | V2: tasto destro→Apri vs decisione 19/10: doppio clic+UAC |
| 9 | Riepilogo PC | Congelato | Non come videata app; password fissa non adottata |
| 10–12 | Help | Escluso | — |
| 13 | Passo 5 CIFS | Gap | Serve elenco campi form CIFS reali |
| 14–16 | Help | Escluso | — |
| 17 | Da valutare | **Conflitto** | SAF «Scegli cartella» dopo CIFS? |

---

## Domande bloccanti

1. **Percorso unico (passo 0):** un solo campo `C:\…` validato, o controllo composto che appare come un riquadro?
2. **Elenco passi 1–5:** solo sul passo 0, pagina intro separata tra 0 e 1, o entrambi?
3. **Sottotitolo passo 0 senza numero:** conferma eccezione solo per lo 0?
4. **Avvio CONFIGURA:** tasto destro→Apri (V2) oppure doppio clic+UAC (decisione 19/10)?
5. **«Prendi nota»:** lasciare / togliere / riformulare (password solo in app)?
6. **Campi CIFS:** elenco o screenshot del form «Nuova connessione»?
7. **Scegli cartella dopo CIFS:** passo 6, coda del 5, o fuori documento?

---

## Perimetro del futuro documento «slide che l’utente vede»

**Incluso (dopo risposte):** chrome senza Help; passo 0; eventuale intro; passi 1–5 PC; eventuale SAF se Q7 lo richiede.  
**Escluso:** errori/help; Configura Utility; slide 9 come pagina app.
