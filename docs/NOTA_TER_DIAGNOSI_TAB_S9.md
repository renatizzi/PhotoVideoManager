# Diagnosi 1:1 — Tab S9 (osservazioni utente 3 nov)

**Approccio:** solo cause evidenziate dal test; nessuna modifica fuori da questa tabella.

| # | Osservazione | Causa in codice | Fix previsto |
|---|---|---|---|
| 1 | Prima Conferma: 16 invece di 11 | `resolveRegisterTargets` registra root+figli; `CensusService` sul root è ricorsivo e re-censisce i figli già registrati come sorgenti separate → MediaItem duplicati | Durante census, **non scendere** in sottocartelle già registrate come altre `StorageLocation` nello stesso albero |
| 2 | Dopo Importa: 16 | Conseguenza di #1 | Stesso fix |
| 3 | Dopo Pulisci: 6 ok, Indietro ancora 16 | `Indietro` da Pulisci non refresh Aggiorna | `onBack` → refresh SearchViewModel / re-carica elenco |
| 4 | Acquisisci: 6 file ok, cartelle ancora errate | Righe cartella duplicate già in DB da #1; listSources non deduplica | Dedup per `canonicalKey` in elenco + evitare nuove duplicazioni (#1) |
| 5 | Dettaglio foto/video fuori standard | Anteprima live senza miniatura / formato Catalogo | Allineare riga Dettaglio al formato Catalogo (thumb + meta) |
| 6 | `pietro.mov` senza miniatura | Thumb video non gestisce bene `.mov` / MIME | Estendere ThumbnailResolver per `.mov` / `video/quicktime` |
| 7 | Dettaglio Ripristina fuori standard | UI Cestino reinventata (bottoni multipli, no riquadro Catalogo) | Riga standard Catalogo: riquadro [ ]/[✓] + thumb + nome + meta; un [Ripristina] |

**Matrice attesa (invariante test):** 4 cartelle · 11 file (5 foto+6 video) · 5 duplicati da cestino · 6 in Catalogo dopo pulizia.

**Stato fix (branch `cursor/fix-tab-s9-16dup-b602`):**
1–2. `CensusService` non scende in sottocartelle già `StorageLocation`; purge copie residue sul genitore; `listLiveMediaPreview` stesso skip; `listSources` dedup `canonicalKey`.
3. Aggiorna: refresh su `RESUMED` (Indietro da Pulisci).
4. Dedup elenco fonti (vedi #1).
5. Dettaglio live: riga con `MediaThumbnailModel`.
6. `MediaThumbnail`: `VideoFrameDecoder` forzato su `VIDEO` (.mov).
7. Esclusi Importa + Cestino: riquadro ✓ + thumb + meta; un [Ripristina] (niente Elimina per riga).

**Fuori scope di questo passo:** filiera dispositivi; nuove regole UX non presenti nella tabella.
