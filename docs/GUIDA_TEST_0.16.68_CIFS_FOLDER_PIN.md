# Guida test 0.16.68 — CIFS Folder pin (MyMedia)

**Problema 0.16.67:** Attiva collegamento ok, poi Stato «non raggiungibile» e etichetta tipo `DELL_SHARED` (Name CIFS) invece di `DELLRS`/`MyMedia` dello script.

**Causa tecnica:** Folder CIFS vuoto → radice SAF = elenco share. Scegliendo la connessione (Name) non si scrive `MediaManager/catalog_hub.json`.

**Fix:** Folder = `MyMedia` (Copia); Read only OFF; Safe data transfer ON; Attiva → connessione → Usa questa cartella.

| # | Passo | Esito atteso |
|---|--------|--------------|
| 1 | CIFS Name = MyMedia (Copia) | Solo etichetta; non è il PC `DELLRS` |
| 2 | Host = IP; User = `DELLRS\MyMedia`; Folder = **MyMedia** (non vuoto) | Connessione punta a `\\PC\MyMedia` |
| 3 | Option: Safe data transfer ON; Read only OFF | Obbligatorio per scrittura sede |
| 4 | Salva CIFS → Attiva collegamento → Usa questa cartella sulla connessione | Nessun elenco share da aprire |
| 5 | Verifica sede | Stato raggiungibile / Catalogo OK (non «non raggiungibile») |
