# Analisi rigorosa — `revisione_guida_V2.pptx`

**Aggiornato:** 20/10/2026 — risposte Renato a Q1–Q7 recepite.  
**Documento videate utente consolidato:** `docs/reference/videate-utente-setup-catalogo-pc.md`  
**Fonte:** `docs/reference/revisione_guida_V2.pptx`  
**Escluso:** gestione errori / help what-if (slide 7, 10–12, 14–16)

---

## Decisioni chiuse (20/10)

| Q | Risposta Renato | Impatto sul documento UI |
|---|---|---|
| 1 Percorso | Oggi unico `C:\MediaManager\Catalogo`; a regime controllo che disegna riquadro/valori per tipo dispositivo | Passo 0: un riquadro; nota «a regime» |
| 2 Elenco 1–5 | Solo sul passo 0 dopo scelta PC; altri dispositivi → loro passi (non ancora definiti) | Blocco variabile sotto la combo |
| 3 Sottotitolo 0 | Senza numero; eccezione solo per lo 0 (passo comune) | Chrome: eccezione documentata |
| 4 Avvio CONFIGURA | Adeguare a ciò che l’utente deve fare (autoelevazione) | **Doppio clic + UAC**; no «Esegui come amministratore» |
| 5 Prendi nota | Omettere se scheda CIFS è completa | Omesso al passo 4 |
| 6 Campi CIFS | Screenshot già nel PPT | Form Connection: ID, Name, Storage, Domain, Host, Port, DFS, User, Password |
| 7 Dopo CIFS | Tutto nel passo 5, nessuna coda; checklist per ogni copia + esito | Passo 5 = scheda + checklist copia/esito |

---

## Slide PPT → documento UI

| Slide PPT | Nel documento videate? | Note |
|---|---|---|
| 1 Copertina | No | — |
| 2 Configura Utility | No | Separato |
| 3 Chrome | Sì (senza Help) | Help escluso in questa fase |
| 4 Passo 0 | Sì | Tendina + percorso + elenco 1–5 solo se PC |
| 5 Passi 1–2 | Sì | Checklist; niente pagina intro duplicata (elenco solo su 0) |
| 6 Passo 3 | Sì | CONFIGURA / sup |
| 7 Help | No | Dopo UI |
| 8 Passo 4 | Sì | Doppio clic + UAC; no prendi nota |
| 9 Riepilogo PC | No come pagina app | Dati utili → scheda passo 5 |
| 10–12 Help | No | — |
| 13 Passo 5 | Sì | Screenshot form Connection |
| 14–16 Help | No | — |
| 17 Da valutare | No coda | Assorbito nel passo 5 |

---

## Prossimo passo implementativo (non in questo documento)

Allineare il codice UI (oggi 0.16.36) a `videate-utente-setup-catalogo-pc.md`: sottotitolo 0 senza numero, elenco 1–5 sul passo 0 solo per PC, scheda CIFS nell’ordine dello screenshot, checklist passo 5 per campo + esito.
