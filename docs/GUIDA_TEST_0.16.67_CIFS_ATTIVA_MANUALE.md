# Guida test 0.16.67 — Attiva collegamento senza recupero IP automatico

**Versione:** `0.16.67-preview` · **versionCode:** `108`  
**Problema:** 0.16.66 bloccava «Attiva collegamento» se LAN non consegnava Host/User, anche con CIFS già ok.

## Cosa fare

1. Installa 0.16.67.
2. Ripeti Setup PC fino al passo CIFS (o riprendi dove eri).
3. Se vedi «Dati del PC non ricevuti»:
   - Digita l’**IP nel campo Host in MediaManager** (stesso IP usato in CIFS).
   - Se User non è `NOMEPC\MyMedia`, incollalo dal testo «Tutto pronto» sul PC nel campo **User CIFS (modificabile)**.
4. Spunta «Confermo di aver configurato CIFS…».
5. **Attiva collegamento** deve abilitarsi → apri share **MyMedia** → Usa questa cartella.

## Atteso

- Attiva collegamento **non** resta grigio se Host è compilato in MediaManager + password + conferma CIFS.
- Messaggio rosso sotto Attiva elenca cosa manca, se ancora bloccato.
