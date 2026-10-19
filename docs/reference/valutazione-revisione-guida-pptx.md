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
| Sequenza PC 1→5 | Media (manca esplicitamente «Prepara file» app) | Media |
| Path + dispositivo sulla stessa slide 0 | **Bassa** vs «una decisione» e vs rami non-PC | Bassa se applicata a tutti i dispositivi |
| «Esegui come Amministratore» da tasto destro | **Bassa** vs script attuale (auto-elevate del `.cmd`) | Media se si cambia istruzione; alta se si resta sul doppio clic |
| Password fissa `mediamanager` | Fuori Nota UI-only | Richiede **cambio script + app** esplicito |
| Integrazione CIFS «tutto tranne password» | **Non sostenibile oggi** (vedi §4) | Solo dopo spike tecnico dedicato |
| Rinomina `CONFIGURA` / `sup` | Coerente con problema estensioni nascoste | OK se aggiorniamo exporter + testi |

**Conclusione:** la proposta è **utilizzabile come direzione UIX** (checklist + help contestuale), **non** come specifica chiuda end-to-end senza le correzioni alle discrasie sotto. Non adotto acriticamente path unificato, auto-fill CIFS e password fissa.

---

## 3. Discrasie e rischi (senso critico)

### A — Slide 0: dispositivo + path (chiarimento Renato 19/10)
- **Chiarimento:** i campi sotto (lettera/percorso) si **adattano** al dispositivo scelto (es. mobile → Memoria principale / Secondaria microSD; PC → `C:\…`).  
- **Revisione posizione:** non è più una discrasia di principio se l’UI è davvero condizionale.  
- **Rischio residuo:** restano due decisioni sulla stessa pagina (dove + quale volume/path). Accettabile se il secondo blocco compare solo dopo la scelta dispositivo e con etichette adattate.  
- **In implementazione:** validazione diversa per ramo (PC = solo C: in questa fase; mobile = volumi disponibili; stub per Cloud/NAS).

### B — Cloud/NAS e «stub» (chiarimento linguaggio)
- **«Stub»** = voce visibile ma percorso **non completo/verificato**: o disabilitata, o con etichetta tipo «Non ancora disponibile», o apre un messaggio breve invece del Wizard end-to-end.  
- **Nota:** non far credere che Cloud/NAS funzionino come il PC.  
- **Chiedere a Renato:** in slide 0 le voci Cloud/NAS devono (1) apparire selezionabili come oggi, (2) mostrare «in arrivo», o (3) essere nascoste?

### C — Checklist = dichiarazione, non prova
- Spuntare «ho copiato i file» **non** prova che i file siano sul Desktop (stesso limite epistemico delle Note).  
- È comunque **migliore** di un solo bottone «Ho fatto tutto» se le voci sono osservabili («vedo entrambi i file»).  
- La **prova reale** resta solo a fine percorso (SAF + scrittura fascicolo), come Nota F3.  
- Non vendere la checklist come «controllo automatico».

### D — Script: istruzioni e password
- **PPT:** tasto destro → Esegui come Amministratore; password fissa `mediamanager`; messaggio di attesa *dentro* lo script.  
- **Codice/Nota:** `.cmd` già auto-eleva; password generata; «non modificare lo script per opportunità UI».  
- **Discrasia:** tre richieste toccano lo script. Sostenibili solo con **tranche esplicita script** (fuori dal solo riordino UI), dopo OK tuo.  
- Per la UI: preferisco istruzione **doppio clic su CONFIGURA** + UAC (coerente con l’auto-elevate già collaudato).

### E — Sequenza: quando l’app prepara i file?
- PPT: passo 3 = copia da Download, ma non c’è uno step chiaro «Prepara i file» che chiama l’exporter.  
- **Serve** uno step (o sotto-step di 2) che genera `CONFIGURA`/`sup` *prima* della copia. Altrimenti Download è vuoto.

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

0. Scelta dispositivo (solo)  
1. Checklist disponibilità (telefono, PC Win10+, cavo)  
2. Checklist collegamento USB + Trasferimento file **+ Prepara file (app)**  
3. Checklist copia Desktop (`CONFIGURA` + `sup`)  
4. Checklist avvio script (doppio clic / UAC / attesa / Tutto pronto) + scheda dati stampabile  
5. CIFS: scheda formato CIFS + Copia campi + Apri CIFS + ritorno manuale  
6. SAF + prova scrittura → esito  

Errori → Help contestuale (non in-page).  
Rami non-PC: invariati/stub finché non specificati.

---

## 7. Backlog — punti da chiudere / tenere

Vedi aggiornamento in `docs/BACKLOG_WIZARD_UX.md`.
