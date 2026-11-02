# Nota bis — 2 novembre 2026 (modifichebis_2nov26)

Fonte: `docs/reference/modifichebis_2nov26.pdf`

## Modifica Setup Catalogo — Prima pagina

- **Modello dispositivo:** se è possibile leggere il nome dispositivo già impostato dall’utente, usare quel nome come default al posto di `MyDevice` (es. «Tab S9 di RENATO»).

## Acquisisci

- Eliminare da Elenco il **vecchio nome del dispositivo** (riga/alias obsoleto).
- Sotto il titolo del **dettaglio**, eliminare «SD» per dispositivi diversi da telefoni e tablet (regola globale: SD solo su mobili con memoria Secondaria; se Principale → vuoto).
- **Elenco foto/video (Catalogo)** — ordine: tipo file, dispositivo, nome file. Composizione riga:

  1. `[Miniatura]` (preceduta da eventuale riquadro selezione)
  2. `<Nome File>` (blu bold corsivo)
  3. `<Nome Dispositivo>`, (eventuale `<SD>` solo mobili + Secondaria), `<percorso cartella>` (sempre con `\` iniziale) - icona stato, `<Stato>` (corsivo blu)

- **Elenco cartelle** — ordine: tipo file, dispositivo. Composizione:

  1. `<Nome Dispositivo>` (blu bold corsivo)
  2. `[SD se Secondaria]`, `<percorso cartella>` (`\` iniziale) - icona stato, `<Stato>` (corsivo blu)

- Dettaglio cartella su **Cloud** molto lento: c’è un modo per velocizzarlo? (punto aperto)
- Evitare di riproporre i file **scartati in Importa** ogni volta che si clicca **[Conferma]** (se si vogliono mantenere gli originali). (punto aperto / requisito)

## Aggiorna

- I filtri **Tutti / Foto / Video** devono agire sulle righe con lo **stesso criterio ciclico** del riquadro di selezione (vuoto → ✓ → X → vuoto).

## Pagina Pulisci

- Lancio con il bottone omologo: analisi scarti + duplicati.
- Output: **due elenchi distinti**, aggregati secondo i criteri sotto.
- Selezione: **manuale** per scarti; **filtri + manuale** per duplicati.
- **Niente** bottone «Analizza duplicati».
- Manca bottone **[Indietro]** → torna ad Aggiorna.

### Layout TO-BE

| Elemento | Contenuto |
|---|---|
| Titolo | invariato |
| Sottotitolo | Sposta nel cestino i file indesiderati |
| Descrizione | Verifica quali sono i file anomali da tenere in Catalogo e quelli invece da eliminare. Questi possono essere eventualmente ripristinati usando l’omologa funzione presente in Ripristina |
| Possibili scarti | N. \<Contatore\> · selezione solo manuale · elenco formato Catalogo |
| Duplicati | N. \<Contatore\> · filtro più vecchi / più recenti · elenco formato Catalogo |

## BUG rilevati

1. Approfondire perché alcuni file importati non vengono riconosciuti (es. estensione `.mp4`).
