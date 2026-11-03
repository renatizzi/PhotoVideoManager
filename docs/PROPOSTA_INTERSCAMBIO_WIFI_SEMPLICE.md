# Proposta — Interscambio file tra dispositivi (semplice, basso profilo)

**Stato:** in attesa OK Renato  
**Data:** 03/11/2026  
**Vincolo esplicito:** niente routine che complicano; linguaggio da utente non tecnico.  
**LOCK operativo:** `docs/LOCK_INTERSCAMBIO_WIFI_SEMPLICE.md`

Riferimenti: Nota §7.1quinquies (cartella condivisa), §7.1sexies (target basso profilo), W1–W4; `PROPOSTA_VISIBILITA_FONTI_ACQUISISCI.md` (modello elenco); **non** riaprire setup sede Catalogo come percorso obbligatorio per ogni fonte.

---

## 1. Idea in una frase

> **Una cartella in rete = il posto dove i dispositivi si incontrano.**  
> Il telefono non “cerca” gli altri dispositivi: si collega a quella cartella e da lì acquisisce i file.

Niente magia Wi‑Fi. Niente app peer tra telefoni. Niente seconda procedura parallela al Catalogo.

---

## 2. Perché non inventiamo altro

| Alternativa | Perché no (v1) |
|---|---|
| Scansione Wi‑Fi / discovery | Instabile, gergo, fuori Nota |
| Telefono ↔ telefono diretto | Android non offre una share semplice come il PC |
| Cloud obbligatorio | Contraddice target basso profilo |
| Rifare tutto il wizard sede Catalogo per ogni fonte | È la farragine da evitare |

**Conclusione:** riprendiamo il meccanismo già noto (cartella condivisa + scelta cartella), ma **solo il pezzo corto** e **solo per le fonti foto**, non per rimettere in piedi la sede del Catalogo.

---

## 3. Cosa vede l’utente (percorso corto)

### Uso quotidiano (dopo il primo collegamento)

Apre **Acquisisci** → vede i dispositivi già collegati → seleziona → **Conferma**. Fine.

### Prima volta — aggiungere un posto da cui prendere le foto

In Acquisisci → **Aggiungi** → una sola domanda:

**«Da dove vuoi prendere le foto?»**

1. **Questo telefono** → scegli cartella (come oggi)  
2. **PC o notebook** → vedi §4  
3. **Disco di rete / NAS** → vedi §4 (ancora più corto)  
4. **Google Drive** → scegli cartella Drive  
5. **Un altro telefono** → **non** una procedura tecnica: tre frasi (§5)

Dopo la scelta cartella: la fonte compare sotto il dispositivo. Stesso Acquisisci di sempre.

---

## 4. PC e disco di rete — una sola biforcazione (anti-farragine)

**Domanda unica** (chiusa, sì/no):

> «La cartella condivisa è già pronta e la vedi da questo telefono?»

| Risposta | Cosa succede | Cosa **non** succede |
|---|---|---|
| **Sì** | Apri collegamento rete (CIFS / Disco di rete) → **Scegli cartella** → fine | Niente USB, niente script, niente checklist lunga |
| **No** (solo PC) | Guida **breve** già esistente per creare la share sul PC (stessi strumenti del setup Catalogo, non un nuovo motore) → poi stesso «Scegli cartella» | Non inventare un secondo wizard; non obbligare a rifare la sede Catalogo |

**Disco di rete / NAS:** di solito la share c’è già → percorso = solo **Sì** (scegli cartella). Niente ramo “crea share”.

**Regola d’oro:** la procedura lunga esiste **una volta** per creare la cartella sul PC; ogni fonte successiva sulla stessa share = solo «Scegli cartella».

---

## 5. Altro telefono (onestà semplice)

Testo utente (bozza):

> Su un altro telefono non si apre una cartella in rete come sul PC.  
> Metti le foto che ti servono nella **cartella condivisa** (sul PC o sul disco di rete), oppure su Drive.  
> Poi da questo telefono aggiungi quella cartella e acquisisci.

Opzionale secondario (help): cavo / Quick Share — **non** percorso principale.

---

## 6. Distinzione mobile / PC / NAS — sì o no?

| Fase | Distinzione? |
|---|---|
| Collegamento (prima volta) | **Sì** — solo la scelta del tipo (5 voci) e, per PC, la domanda Sì/No |
| Uso quotidiano | **No** — stesso Acquisisci / Importa |

Non tre prodotti diversi. Un prodotto, attacchi diversi.

---

## 7. Cosa riusiamo vs cosa non tocchiamo

**Riuso:**
- `+` / OpenDocumentTree  
- CIFS / «Disco di rete» (Host anche a mano)  
- Elenco dispositivi/fonti in Acquisisci (modello già convalidato 23/10)  
- Testi/strumenti della share PC **solo** se l’utente dice «No, non è pronta»

**Non tocchiamo in questa tranche:**
- Ridisegno completo wizard sede Catalogo  
- Recupero automatico IP (resta parcheggiato)  
- Peer MediaManager, scan LAN, smart TV  
- Cambi a Importa / Pulisci / KPI  

---

## 8. Criterio di accettazione “basso profilo”

Una persona che non sa cos’è SMB deve poter dire:

1. «Aggiungo il PC» → se la cartella c’è già → **pochi tap** e vede le foto.  
2. Se la cartella non c’è → **una** guida chiara, non un labirinto.  
3. Non gli chiediamo mai di “scoprire la rete” né di capire catalogo vs fonte nello stesso flusso farraginoso.

Se un passo non serve al risultato «vedo le foto da acquisire», **non esiste**.
