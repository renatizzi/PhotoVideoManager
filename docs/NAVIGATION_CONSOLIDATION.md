# Convalida navigazione MediaManager ↔ Nota Integrata v5.1

Stato: **convalidato in interlocuzione** (2026-10-08).  
Fonti: Nota Integrata v5.1; proposta `Navigazione11.pptx`; chiarimenti Renato; decisioni residue chiuse.

Scopo: riorganizzare l’albero UI senza inventare requisiti funzionali. I requisiti restano sostanzialmente inalterati.

## Principi

- Una funzione ha un solo punto di accesso principale.
- Sotto i riquadri hub compaiono **funzionalità da gestire**, non necessariamente voci di menu.
- Passi di workflow (Seleziona, Importa, Modifica, …) non diventano livelli di navigazione.
- Home / Top Bar / Bottom Bar restano componenti globali.
- Configura resta distinta dalle macro operative.
- Accesso rapido Home: stesso modello già definito; varia solo il numero di link (non è un secondo albero).

## Shell

| Componente | Decisione |
|---|---|
| Bottom Bar | `Home` · `Organizza` · `Componi` · `Utility` · `Impostazioni` (solo icona) |
| Top Bar | invariata nella sostanza (identità, utente, data/ora, Guida, switch tema) |
| Pubblica | **non** è macro né voce Bottom Bar; è funzione dentro `Componi` |
| Quarta macro | nome ufficiale **`Utility`** (sostituisce «Gestisci» in navigazione) |

Nota storica: la Nota v5.1 prevedeva Bottom Bar solo Home+CONFIGURA e macro Pubblica/Gestisci. Questa revisione, convalidata, aggiorna la navigazione UI; i requisiti di dominio restano.

## Home

- KPI (come da template)
- **Ricerca nel Catalogo** — motore unico, **un solo riquadro** (foto/video e album/raccolte)
- Accesso rapido — link alle destinazioni già previste (numero aumentato rispetto alla versione precedente)

## Organizza

Ambito: solo Catalogo logico.

| Funzione hub | Contenuto |
|---|---|
| **Acquisisci** | Unico processo a due fasi: (1) fonti/dispositivi → Conferma; (2) schermata **Importa** → commit nel Catalogo personale → messaggio → Dashboard. Concettualmente due fasi, una sola voce hub. |
| **Aggiorna** | Gestione elementi Catalogo (modifica, spostamento, eliminazione singola via menu contestuale). Include **Pulisci** (solo duplicati) e **Allinea** (explore/controllo sorgenti da app). |

Selezione elenchi (sorgenti e file): solo **on / selezionato** e **off / non selezionato**. Nessuno stato «rimosso» / ❌.

### Mappatura ex-voci Nota sotto Organizza

| Voce Nota | Destino convalidato |
|---|---|
| Archivia (come macro hub) | Non hub. Archivio Condiviso = disco/sorgente individuabile in scansione; setup in Configura; operazioni in Utility |
| Raggruppa (come macro hub) | Non hub. Selezione/raggruppo elementi nel flusso `Componi` |
| Ricerca | Motore unico riusabile ovunque sia prevista ricerca (vedi Home) |

## Componi

- **Crea** / **Edita** composizioni (solo: album fotografico; raccolta foto/video)
- **Pubblica** (funzione): stampa; condivisione interna (Archivio Condiviso); esterna (social / terze parti)
- Editing foto/video di Catalogo raggiungibile anche da qui (oltre che da Organizza → Aggiorna)
- Cover, nome, descrizione, data modifica; blocco privacy; spostamento elementi tra composizioni

## Utility

| Area | Contenuto |
|---|---|
| Backup | Backup manuale Archivio Condiviso; salvataggi contestuali; non è sincronizzazione |
| Ripristina | Da backup **e** da Cestino |
| Revisione / Elimina | Svecchiamento sorgenti (non dedup) |
| Verifica spazio | Controllo spazio nel workflow |

**Pulisci** ≠ Revisione/Elimina ≠ Cestino:

- Pulisci → solo eliminazione duplicati (sotto Organizza → Aggiorna)
- Revisione/Elimina → svecchiamento sorgenti (Utility)
- Cestino → solo recupero elementi eliminati involontariamente (Catalogo e Archivio Condiviso); ingresso via Utility → Ripristina (niente link rapido obbligatorio in Home)

«Salva» della Nota = Backup / Ripristina / spazio in Utility (non quarta voce hub «Salva»).

## Configura

| Ramo | Contenuto |
|---|---|
| Impostazioni | Preferenze generali app |
| Sicurezza e Privacy | come da proposta |
| Setup archivio condiviso | configurazione tecnica dell’Archivio Condiviso |
| **Accessi all'Archivio** | chi è autorizzato ad accedere (membri + amministratore). Sostituisce l’etichetta «Famiglia» (evitare fraintendimenti BoxManager) |
| Parametri Play Store | in scope (riuso approccio BoxManager: bonus / distribuzione); app gratuita |

## Editing

- Catalogo (singole foto/video): da Organizza → Aggiorna (menu contestuale) e da Componi
- Composizioni (album/raccolte): da Componi
- Strumenti di base condivisibili; due ingressi UI, non due prodotti diversi

## Regole per lo sviluppo

1. Usare i nomi dell’albero convalidato (`Utility`, `Accessi all'Archivio`, `Ricerca nel Catalogo`, …).
2. Non creare voci menu per passi di workflow.
3. Non duplicare la stessa funzione in più macro come hub distinti.
4. Non trasformare Spazio / Verifica / Cestino in macro.
5. Non usare Dashboard/Accessi rapidi come secondo sistema di navigazione.
6. In caso di contraddizione residuale con testo Nota non aggiornato: fermarsi e chiedere; questo documento è il riferimento UI post-convalida.

## Impatto immediato sull’app attuale (0.13.x)

- Bottom Bar: rimuovere tab `Pubblica` e `Gestisci`; introdurre `Utility`; mantenere Impostazioni icon-only.
- Hub Organizza: Acquisisci + Aggiorna (non più griglia Nota a quattro macro-voci).
- Acquisisci: unificare UX fasi fonti → Importa; selezione binaria.
- Spostare Pulisci sotto Aggiorna; Cestino sotto Utility → Ripristina.
- Configura: rami come tabella sopra (placeholder ammessi finché il dominio non è pronto).
