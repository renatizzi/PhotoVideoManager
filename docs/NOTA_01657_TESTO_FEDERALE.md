# Nota v0.16.57 — pagina PDF 1
## Generale

- Uniformare lo stile dei bottoni (contorni, dimensioni, icone): BACKLOG
- Non tutti i motori di ricerca hanno il microfono: BACKLOG
- la riga fissa del Catalogo deve essere formattata secondo le specifiche concordate (applicare la stessa formattazione a tutte le le pagine dove compare Catalogo):
● sempre su due righe: prima riga: " Catalogo:" (bold corsivo nero)", < Nome Catalogo > (bold corsivo blu); seconda riga: (tutto corsivo blu): < Nome Dispositivo >, < SD > (SD solo se il tipo memoria è Secondaria, se Principale vuoto), < percorso cartella > (il percorso inizia sempre con '\') icona stato, <Stato> (corsivo blu). Di seguito un esempio:
○ Catalogo: MediaManagerCatalog
○ Tablet S9 SD \pictures\vacanze
- 🛜 Online
- Verificare cosa accade se il nome della cartella selezionata per il Catalogo viene modificato.
- Dopo la disinstallazione è corretto che la cartella resti nel dispositivo? 

## Dashboard

- catalogo non presente: vedi nota a parte
- una volta creato il catalogo usare la formattazione della riga fissa. Avvio setup Catalogo da 

## Dashboard

- il messaggio di "assenza catalogo" non deve essere mostrato su 

## Dashboard

- verificare se sia opportuno introdurre una guida implicita alla creazione del Catalogo (ad esempio in assenza del Catalogo si potrebbero inibire le scorciatoie e lasciare solo il bottone 

## Acquisisci
 che in questo caso porta direttamente al setup del Catalogo) 

## Modifica Setup Catalogo
 Se Catalogo assente, modificare come segue" 

## Prima pagina

* Titolo: invariato
* Sottotitolo: invariato
* Descrizione: invariata
* Nome del Catalogo: modificare il valore di default del Catalogo in ' MyMedia ' (il nome attuale è troppo lungo)
* Selezione dispositivo: modificare i nomi così: 'Questo dispositivo, PC Windows, Cloud, Disco di Rete'
* Modello dispositivo: modificare il valore di default del modello in 'MyDevice ' ('dispositivo selezionato' è troppo lungo)
* In fondo alla videata mettere due bottoni: [Annulla] e [Continua] in modo da evitare di usare Esc (con Annulla si ritorna al punto di accesso) 

## Pagina di 'scelta della cartella'

* Titolo: invariato
* Sottotitolo: invariato
* Descrizione: modificare il messaggio così: "Seleziona e poi salva la cartella del Catalogo su < modello dispositivo >"
* Modificare il bottone di selezione in "Seleziona Cartella"
* In fondo alla videata prevedere due bottoni: [Annulla] e [Salva] (in questa fase non ha senso il bottone 'indietro' e quindi non occorre memorizzare il "percorso pending") 

## Pagina di riepilogo e conferma

* Titolo: invariato
* Sottotitolo: Riepilogo e conferma
* Descrizione "Riepilogo dati" (Normale nero)
* Riga fissa Catalogo
* Messaggio: "Catalogo censito correttamente" (se ko, messaggio esplicativo del ko con istruzioni su cosa deve fare)
* Modificare il bottone in "Continua" e restituire il controllo al punto di accesso 

## Acquisisci

- Rivedere l'intero processo di acquisizione in modo da assicurare coerenza con i requisiti ufficiali
- Bilanciamento della pagina (poco spazio per lo scrolling dell'elenco). La nuova Pagina deve essere così organizzata:
* Titolo: invariato
* Sottotitolo: invariato
* Descrizione: Modificare così:
● Con Catalogo assente: visualizzare il seguente messaggio: "Prima di utilizzare questa funzione devi creare il Catalogo personale" segue bottone [Crea] => setup catalogo
● Con Catalogo presente: visualizzare il seguente messaggio: "Seleziona i dispositivi con le cartelle condivise ed importa i media (foto e video) che intendi gestire nel tuo Catalogo"

# Nota v0.16.57 — pagina PDF 2
• Riga fissa Catalogo (come sopra)
• Contatori (solo per N. Elementi > 0): "N. Cartelle collegate:" <N. Cartelle e sottocartelle acquisite in Catalogo>, "N. Foto:" <N. di foto acquisite in Catalogo>, "N. Video:" <N. di Video acquisiti in Catalogo>
• Messaggio: "Clicca sul bottone [+] per aggiungere una alla volta le cartelle del dispositivo dove conservi i tuoi file. Quando hai terminato clicca sul bottone [Conferma]"
• Bottoni: invariati
• Titolo: Elenco delle cartelle collegate al Catalogo (solo se è stato inserita almeno una cartella)
• Struttura elenco delle cartelle/dispositivi: invariato
• Per selezione cartella elenco vedi BUG sotto
• Il messaggio di conferma eliminazione cartella deve essere così modificato: "Eliminare la cartella <nome cartella> dal catalogo? Riga sotto: (L'eliminazione non comporta alcuna perdita di dati originali ma solo l'esclusione dal Catalogo dei relativi media)"
• I bottoni di ordinamento devono essere visualizzati solo con N. elementi > 0. Il messaggio con N. Elementi = 0 deve essere modificato così: "La cartella selezionata non contiene foto o video: torna su 

## Acquisisci
 e seleziona un'altra cartella"
• La pagina del dettaglio deve essere modificata come segue (invariata la miniatura e il link al dettaglio):
● Titolo: "Dettaglio della cartella" + <percorso completo della cartella che contiene media>
● Riga successiva: "su" <nome dispositivo>, SD (solo se su memoria secondaria).
● Testo:"N. Elementi:" <contatore> (solo se il numero degli elementi è maggiore di zero) 

## NOVITA'
 Attenzione, se in ACQUISISCI la cartella selezionata rappresenta la root di sottocartelle che contengono media allora ogni sottocartella deve essere indicata nell'elenco (corresponsione biunivoca cartella/media). Ciò evita disallineamenti tra la cartella visualizzata nell'elenco e il suo contenuto. Il dettaglio delle sottocartelle della root non risolve lo specifico problema e complica la lettura con elementi inutili 

## BUG RILEVATI

1. Verificare se selezionando la root vengono sempre visualizzati i media delle sottocartelle (in alcuni casi non mi è sembrato di no)
2. Dopo la selezione della cartella, l'elenco non viene aggiornato automaticamente (occorre riprovare ed aggiungere un'altra cartella oppure andare se un'altra pagina e ritornare)
3. la selezione ciclica delle cartelle di 

## Acquisisci
 non funziona bene (dopo la X e il popup non è più possibile flaggare la cartella
4. Nella pagina del dettaglio di 

## Acquisisci
 manca il bottone di ritorno al punto di accesso
5. In fase di inserimento la tastiera va a coprire l'area di input e quindi non si legge quello che di scrive
6. Il setup delle sorgenti, così come è stato implementato, non funziona (è inutile implementare una procedura guidata se tutte le attività più complicate richiedono comunque un intervento manuale. Quindi le procedure di setup sia del Catalogo che delle Sorgenti devono essere estrapolare e riprogettate.
