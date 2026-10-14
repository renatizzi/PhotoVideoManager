# Legenda termini UI — MediaManager / Photo&VideoManager

**Vincolante per messaggi, avvisi e etichette** (decisione Renato 10/10/2026).  
Stile comunicativo: semplice e chiaro, come in BoxManager.

## Termini da usare

| Usa sempre | Non usare (evitare) | Significato |
|---|---|---|
| **Catalogo** | Spazio app, spazio interno, archivio personale app, file interno | L’elenco gestito dall’app: foto/video conosciuti e organizzabili. Non è una cartella del telefono. |
| **File del dispositivo** | File interno, originale sul telefono (ok in spiegazioni lunghe), copia SAF | Il file fisico nelle cartelle del telefono, Drive, disco, ecc. |
| **Cestino** | Soft-delete, trash | Elementi tolti dal Catalogo; si possono ripristinare. |
| **Dispositivo** | Device, sorgente tecnica | Telefono, tablet, PC, Drive, disco di rete… come li vede l’utente. |
| **Cartella / Fonte** | StorageLocation, albero SAF | Una cartella o archivio aggiunto in Acquisisci. |
| **Importa** | Acquisisci copia, sync | Copia nel Catalogo (gli originali restano dove sono). |
| **Elimina** (dal Catalogo) | Cancella file | Toglie dal Catalogo → Cestino; **non** cancella il file del dispositivo. |
| **Modifica** | Edit, editing | Modifica di una singola foto/video (funzione in preparazione). |
| **Ripristina** | Restore | Tornare indietro: dal Cestino e (in seguito) da Backup. |
| **Backup** | Sync, cloud obbligatorio | Copia di protezione dell’Archivio Condiviso (funzione in preparazione). |

## Messaggi di conferma (stile BoxManager)

- Titolo breve tipo: **«Conferma eliminazione?»**
- Pulsanti: **SÌ** / **NO**
- Se serve una precisazione (solo quando evita un dubbio reale), una o due frasi sotto il titolo, non un paragrafo lungo.

### Elimina dal Catalogo (testo di riferimento)

- Titolo: `Conferma eliminazione?`
- Testo: `Il file del dispositivo non viene cancellato. Potrai ripristinarlo dal Cestino.`
- Pulsanti: `SÌ` · `NO`

## Note

- «Spazio interno app» non si mostra all’utente in Acquisisci (BL-05).
- In testi tecnici interni (Nota, codice) si può ancora parlare di copie locali; in UI no.
