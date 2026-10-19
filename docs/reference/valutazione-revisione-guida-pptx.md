# Valutazione critica — `revisione_guida.pptx` (19/10/2026)

**Input:** proposta Renato (`revisione_guida_92a0.pptx`)  
**Confronto con:** Nota operativa Cursor (18/10), Workflow Claude (18/10), template videate PC v2, codice 0.16.33 (`NetworkDriveAssistant`).  
**Metodo:** coerenza, sostenibilità, niente compiacimento. Nessuna modifica codice in questa fase.

Copia PPT in repo: `docs/reference/revisione_guida.pptx`.

---

## 1. Cosa ho recepito (criteri e metodo delle Note)

Dalle **ultime due note** (Nota operativa + Workflow Claude) i criteri che considero vincolanti sono:

| Criterio | Fonte | Stato di recezione |
|---|---|---|
| Titolo fisso + sottotitolo passo | Nota / tua raccomandazione | **Allineato** al PPT (slide 3) |
| Una decisione (o un blocco operativo) per schermata | Claude / Nota §2 | **Parzialmente** (vedi discrasia A) |
| Niente falsa automazione | Nota §2, §5.2, §11 | **Vincolo** su CIFS auto-fill e su checklist |
| Password solo dove serve; mai in riepilogo finale | Nota §5.5 | **Allineato** (PPT: printscreen a fine script + scheda CIFS) |
| Quattro passi manuali chiari (USB, script, CIFS, SAF) | Nota §5.2 | **Allineato** nella sostanza (PPT li riduce a 5 blocchi) |
| Warning risolvibili, non solo codice | Nota §8 / Claude W-xxx | **Allineato** all’idea di help contestuale / repository |
| Non modificare lo script «per opportunità» UI | Nota §11 | **Tensione** con password fissa e messaggio in-script (vedi D) |
| C/D non come percorsi completi se non verificati | Nota §3.1, §6 | **Discrasia** slide 4 (vedi B) |
| Ritorno da CIFS manuale; no deep-link/import CIFS | Nota §5.2, §11 | **Vincolo** → risposta CIFS sotto |

Il PPT introduce due novità di metodo che **non contraddicono** le Note, ma le **concretizzano**:

1. **Checklist a blocchi** (spuntare → poi OK) al posto di alcune domande chiuse.  
2. **Help contestuale** (what/if + repository) al posto dei checkpoint in-page.

---

## 2. Verdetto di coerenza (sintesi)

| Area | Coerenza con Note | Sostenibilità |
|---|---|---|
| Chrome pagina (titolo/sottotitolo/intro/OK) | Alta | Alta |
| Checklist operativa + OK gated | Media–alta (è ancora autodichiarazione, ma granulare) | Alta in UI; non sostituisce prova SAF finale |
| Errori fuori dalle pagine operative → help | Alta (catalogo W-xxx Claude = contenuto del repository) | Media: è un **nuovo sottosistema** da progettare |
| Sequenza PC 1→5 | Alta (export sull’OK del passo pre-copia — chiarito 19/10) | Alta |
| Path + dispositivo sulla stessa slide 0 | **Ok se condizionale** (campi adattati al dispositivo) | Alta |
| «Esegui come Amministratore» da tasto destro | **Corretto in impl** → doppio clic + UAC | Alta |
| Password usabilità | **Copia dall’app** allo step CIFS (niente tocco script) | Alta |
| Integrazione CIFS «tutto tranne password» | **Non sostenibile oggi** (vedi §4) | Solo dopo spike tecnico dedicato |
| Rinomina `CONFIGURA` / `sup` | Coerente con problema estensioni nascoste | OK se aggiorniamo exporter + testi |

**Conclusione:** la proposta è **utilizzabile come direzione UIX** (checklist + help contestuale), **non** come specifica chiuda end-to-end senza le correzioni alle discrasie sotto. Adotto checklist + help; slide 0 condizionale; password via Copia in-app; niente auto-fill CIFS né password fissa in script.

---

## 3. Discrasie e rischi (senso critico)

### A — Slide 0: dispositivo + path (chiarimento Renato 19/10)
- **Chiarimento:** i campi sotto (lettera/percorso) si **adattano** al dispositivo scelto (es. mobile → Memoria principale / Secondaria microSD; PC → `C:\…`).  
- **Revisione posizione:** non è più una discrasia di principio se l’UI è davvero condizionale.  
- **Rischio residuo:** restano due decisioni sulla stessa pagina (dove + quale volume/path). Accettabile se il secondo blocco compare solo dopo la scelta dispositivo e con etichette adattate.  
- **In implementazione:** validazione diversa per ramo (PC = solo C: in questa fase; mobile = volumi disponibili; stub per Cloud/NAS).

### B — Cloud/NAS e «stub» (chiarimento linguaggio) — chiuso
- **«Stub»** = voce/ramo presente in UI ma **senza percorso Wizard completo e verificato** come il PC: non è un bug, è un placeholder funzionale. In pratica: si può selezionare/aprire, ma non promette end-to-end (es. messaggio breve, campi non operativi, o flusso ridotto).  
- **Decisione adottata (già in Nota):** Cloud/NAS restano **come oggi** (stub) finché non completiamo i rami. In UIX: etichette/campi adattati al tipo, senza far credere che funzionino come il PC.  
- Non è richiesta marcatura «in arrivo» separata salvo preferenza futura.

### C — Checklist = dichiarazione, non prova — accettato
- Spuntare «ho copiato i file» **non** prova che i file siano sul Desktop (stesso limite epistemico delle Note).  
- Accettato come metodo: checklist **granulare** + OK gated; meglio del solo «Ho fatto tutto».  
- La **prova reale** resta solo a fine percorso (SAF + scrittura fascicolo), come Nota F3.  
- Non vendere la checklist come «controllo automatico».

### D — Script: istruzioni e password — chiuso (19/10)
- **Avvio script:** in implementazione istruzione = **doppio clic** su `CONFIGURA` + conferma UAC (auto-elevate già nel `.cmd`). Niente «Esegui come Amministratore» da tasto destro.  
- **Password usabilità (senza toccare lo script):** non chiedere di trascrivere dalla finestra PC. Allo step CIFS l’app mostra la password **già nota all’app** (quella generata/esportata) con **Copia** + Mostra/Nascondi — un tap, niente digitazione da monitor.  
- Messaggio di attesa: obbligatorio in checklist app; in-script resta opzionale (tranche script separata).  
- Password fissa `mediamanager` **non** adottata (richiederebbe cambio script).

### E — Sequenza: quando l’app prepara i file? — chiuso (19/10)
- **Chiarimento Renato:** non serve uno step UI separato «Prepara file». La generazione (`CONFIGURA`/`sup`) avviene sull’**OK** del passo checklist che conferma le attività spuntate (senza OK non si avanza).  
- **In implementazione:** allineare numerazione PPT: l’OK del blocco pre-copia (USB/collegamento) **esporta** i file in Download, poi si passa alla checklist copia Desktop.

### F — Help contestuale
- Coerente con Note (warning catalog, what-if).  
- **Non esiste** nel codice: va definito come modulo (contesto passo + sintomo → scheda aiuto).  
- I placeholder PPT (slide 7, 10–12, 14–16) non bastano: serve mappare il repository Claude W-xxx → contesti (USB, script, CIFS, SAF).

### G — Print/PDF dei dati
- Utile; in-app «condividi/salva scheda» è sostenibile.  
- «Stampante» nativa è secondaria; screenshot utente sul PC resta valido.

### H — Configura stile Utility (slide 2)
- Fuori dal ramo Wizard PC; annotare come lavoro separato, non mescolare con Setup Catalogo.

---

## 4. Integrazione CIFS ↔ MediaManager — livello definitivo atteso

### Cosa fa oggi MediaManager (0.16.33) — verificato nel codice
`NetworkDriveAssistant` può solo:
1. rilevare se è installato `com.wa2c.android.cifsdocumentsprovider`;  
2. aprire il Play Store;  
3. **lanciare** l’app CIFS (intent di launch generico).

Poi MediaManager usa solo **SAF** (`OPEN_DOCUMENT_TREE`) sulla cartella già esposta da CIFS.

### Cosa **non** possiamo promettere (allineato a Nota §11 e analisi BoxManager)
- Compilazione automatica del form CIFS (Host, Porta, Folder, User, …).  
- Import profilo / deep-link alla connessione.  
- Scansione LAN:445 o preselezione della share dalla lista.  
- Salvataggio password SMB dentro MediaManager.

**Non risulta** (né in BoxManager né in PVM) un’API pubblica documentata e collaudata di CIFS Documents Provider per precompilare i campi. Finché non esiste uno spike dedicato con prova su dispositivo, l’auspicio PPT «integrazione completa esclusa password» resta **non affidabile**.

### Livello che puoi attenderti in questa fase (contratto chiaro)

| Dato | In MediaManager | Nel form CIFS |
|---|---|---|
| Host / IP | Mostrato + **Copia** | Inserimento **manuale** (incolla) |
| Porta 445 | Mostrato + Copia | Manuale |
| Folder / share | Mostrato + Copia | Manuale |
| Username | Mostrato + Copia | Manuale |
| Password | Mostrata **solo** nello step CIFS + Copia/Mostra | Manuale (unica eccezione che resta comunque manuale) |
| Storage SMB2,3 | Istruzione testuale | Scelta manuale in CIFS |
| Apertura app | Pulsante Apri CIFS | — |
| Scelta cartella | Dopo: SAF in MediaManager | — |

**Raccomandazione operativa:** percorso **inserimento manuale** (come da tua osservazione: la scan spesso non elenca la share). Opzione scan solo con avviso.  
Se in futuro vorrai l’auto-fill: aprire uno **spike tecnico separato** (reverse/API CIFS, prova su 2–3 device); solo dopo OK esito si cambia il contratto UI.

---

## 5. Help contestuale — inquadramento

**Sì, lo recepisco come novità strutturale** della proposta:

- Pagine operative = intro + (domanda) + **checklist** + OK.  
- Problemi = **Help** contestuale (what/if), non riquadri errore lunghi sulla stessa pagina.  
- Contenuto = repository derivato dal catalogo warning Claude + messaggi Nota §5.6, filtrato per contesto (USB / script / CIFS / SAF).

**Dipendenze prima dello sviluppo UI dei passi:**
1. Schema minimo repository (id, contesto, sintomi, azioni).  
2. Punti di ingresso Help per ogni passo PPT 1–5.  
3. Decisione: help in-app (schermata) vs bottom-sheet — da definire in implementazione.

Senza (1)–(2), le pagine operative restano «cieche» quando qualcosa va male.

---

## 6. Metodo proposto (dopo le correzioni)

Sequenza PC sostenibile, fondendo PPT + Note + limiti tecnici:

0. Scelta dispositivo + campi volume/path **condizionati** al tipo  
1. Checklist disponibilità (telefono, PC Win10+, cavo)  
2. Checklist collegamento USB + Trasferimento file → **OK = export** `CONFIGURA`/`sup` in Download  
3. Checklist copia Desktop (`CONFIGURA` + `sup`)  
4. Checklist avvio script (doppio clic / UAC / attesa / Tutto pronto) + scheda dati stampabile  
5. CIFS: scheda + Copia campi **inclusa password** + Apri CIFS + ritorno manuale  
6. SAF + prova scrittura → esito  

Errori → Help contestuale (non in-page).  
Rami non-PC: stub come oggi finché non specificati.

---

## 7. Backlog — punti da chiudere / tenere

Vedi aggiornamento in `docs/BACKLOG_WIZARD_UX.md`.
