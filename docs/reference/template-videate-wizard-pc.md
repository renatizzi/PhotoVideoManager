# Template videate — Setup Catalogo · ramo PC

**Stato:** revisione UIX pre-sviluppo (nessun codice).  
**Decisioni Renato 18/10:** Quick Share solo in «Non riesco» · default path/share attuali · B0 fuso in B1 · Drive/NAS come oggi.  
**Canvas interattivo:** vedi chat / Cursor Canvas «Template videate Wizard PC».

Filiera funzionale invariata rispetto a 0.16.33. Cambia taglio schermate e densità testi.

---

## Sequenza

`E0 → S0 → B1 → B2 (↔ B2·aiuto) → B3 → B4 → B5 (↔ B5·aiuto) → B6 → F`

---

## E0 — Setup Catalogo personale
**Indicatore:** Ingresso · Configura  
**Testo:** Scegli se creare una nuova sede, modificare quella attuale o riprendere un percorso interrotto.  
**Eventuale:** Sede attuale · Percorso interrotto  
**Azioni:** Riprendi · Crea · Modifica · Annulla  

---

## S0 — Dove tenere il Catalogo?
**Indicatore:** Scelta destinazione  
**Opzioni (un tocco = avanza):**
- Su questo telefono o tablet — Nessuna rete necessaria  
- Su un PC o notebook — Deve essere acceso; altrimenti userai il cavo USB  
- Su Google Drive (o cloud) — come oggi (stub)  
- Su un disco di rete o NAS — come oggi (stub)  
**Azioni:** Indietro (E0)

---

## B1 — Prepara i file di setup
**Indicatore:** PC · passo 1 di 7  
**Testo:** L’app prepara due file in Download. Poi li copierai sul Desktop del PC con il cavo USB.  
**Avviso:** PC acceso e non in sospensione; telefono e PC sbloccati.  
**Riepilogo default:**
```
Cartella:       C:\MediaManager\Catalogo
Condivisione:   MediaManagerCatalog
Utente:         MediaManager
```
**Link:** ▸ Modifica dettagli (opzionale)  
**Azioni:** Prepara i file · Ho già la cartella condivisa sul PC · Indietro  

---

## B2 — Copia i file sul Desktop
**Indicatore:** PC · passo 2 di 7  
**File:**
```
AVVIA_Configurazione_Catalogo  ← questo
_non_aprire_supporto_setup      ← non aprirlo
```
**Istruzioni:**
1. Cavo USB che trasferisce file (non solo ricarica).  
2. Notifica USB → Trasferimento file (non Solo ricarica).  
3. PC: Questo PC → telefono → Download → copia entrambi sul Desktop.  
**Azioni:** Ho copiato i file sul Desktop · Non riesco — mostra aiuto · Indietro  

---

## B2·aiuto — Il telefono non compare sul PC
**Controlli USB:** Trasferimento file · sblocca · ricollega · altra porta/cavo · driver  
**Alternativa (solo qui):** Quick Share — invia i due file e spostali insieme sul Desktop.  
**Azioni:** Ho risolto — torna alla copia · Indietro  

---

## B3 — Esegui lo script sul PC
**Indicatore:** PC · passo 3 di 7  
**Istruzioni:** doppio clic su AVVIA_… · non aprire l’altro · SmartScreen · UAC  
**Avviso:** Questa fase dura qualche minuto. Aspetta «Tutto pronto», poi torna qui.  
**Azioni:** Ho visto Tutto pronto sul PC · Indietro  

---

## B4 — Controlla i dati (senza password)
**Indicatore:** PC · passo 4 di 7  
**Testo:** Controlla che sia corretto. La password la vedi al passo successivo, solo per CIFS.  
**Riepilogo:** PC Windows · path · share · utente (no password)  
**Azioni:** Continua: configura CIFS · Indietro  

---

## B5 — Collega il PC con CIFS
**Indicatore:** PC · passo 5 di 7  
**Testo:** Apri CIFS, crea connessione, salva, torna con Indietro (non automatico).  
**Campi (password solo qui):** Host · Utente · Password · Folder · Porta 445 · Storage SMB2,3  
**Azioni:** Ho configurato CIFS · Non riesco a collegarmi · Apri/Installa CIFS · Indietro  

---

## B5·aiuto — Non riesco a collegarmi
MediaManager locale · SMB2,3 · porta 445 · stessa Wi‑Fi · PC non in sospensione · Ho cambiato la password  
**Azioni:** Torna al collegamento · Indietro  

---

## B6 — Scegli la cartella del Catalogo
**Indicatore:** PC · passo 6 di 7  
**Testo:** Scegli cartella via CIFS Documents Provider · Usa questa cartella.  
**Stato:** non verificato / in corso / verificato / non raggiungibile  
**Azioni:** Scegli cartella · Riprova verifica · Torna a CIFS · Indietro  

---

## F — Catalogo / configurazione impostata
**Indicatore:** PC · passo 7 di 7 · Esito  
**Esito:** Sede valida: verificata  
**Riepilogo senza password** + reminder password provvisoria  
**Azioni:** OK → ritorno a Configura o Acquisisci  

---

## Criterio di accettazione template

Prima dello sviluppo: Renato conferma che titoli, azioni primarie e densità di ogni schermata vanno bene così (eventuali ritocchi di copy OK).
