# Proposta unica — Visibilità dispositivi/fonti in Acquisisci (P0)

**Stato:** **CONVALIDATA** Renato 23/10 (OK + integrazione con Acquisisci esistente) — implementata in **0.16.51**  
**Data:** 23/10/2026  
**Ambito Nota:** W1–W4 (§7.1octies) — telefono/tablet, PC/notebook (disco interno/esterno sul PC), Google Drive/cloud, disco di rete/NAS. **Esclusi:** smart TV e altri IoT Wi‑Fi.  
**Congelato fuori ambito:** recupero automatico IP Host in Wizard CIFS (parcheggio 23/10; IP manuale in CIFS accettato).

Riferimenti: `docs/PVM_Nota_Integrata.md` §3.4 / §5.4.1 / §7.1octies · `docs/BACKLOG_WIZARD_UX.md` P0 Acquisisci · `docs/TERMINOLOGIA_UI.md` · BL-03 (riquadro 3 stati).

---

## 1. Problema da chiudere

Su Acquisisci oggi compaiono soprattutto cartelle già aggiunte con **+** (telefono, Drive, CIFS), **senza** un elenco chiaro dei **dispositivi** da cui acquisire.  
Obiettivo prodotto: aprendo Acquisisci si vedono i **dispositivi previsti dalla Nota** e, sotto ciascuno, le **cartelle/fonti** usabili — con stato raggiungibile o no.

---

## 2. Modello (Gradi 1–3) — contratto di prodotto

### Grado 1 — Elenco stabile «i miei dispositivi / fonti» (sempre in Acquisisci)

Struttura a due livelli:

1. **Dispositivo** (etichetta utente): es. «Questo telefono», «Tablet …», «PC …», «Google Drive», «NAS …».  
2. **Fonti** sotto il dispositivo: percorsi/cartelle già autorizzate (SAF, CIFS, volume, ecc.).

Tipi ammessi (solo questi):

| Tipo | Come entra in elenco |
|------|----------------------|
| Questo telefono/tablet | Automatico (volumi/cartelle già noti all’app + quelle aggiunte con +) |
| PC / notebook | Dopo collegamento CIFS/SAF alla share (IP anche manuale) — anche path su disco esterno **del PC** |
| Google Drive (cloud) | Dopo collegamento/cartella scelta (W3; oggi stub → da rendere fonte reale nell’ambito del blocco se già parzialmente presente, altrimenti voce + guida senza falsa completezza) |
| Disco di rete / NAS | Come PC via CIFS/«Disco di rete» (W4) |

Niente scansione “tutta la Wi‑Fi”. Solo ciò che l’utente ha (o sta) collegando.

### Grado 2 — Aiuto in «Aggiungi dispositivo / fonte» (solo in setup)

Pulsante **Aggiungi** (o equivalente) → scelta tipo W1–W4:

- **Questo dispositivo:** selettore cartella (SAF / volumi) come oggi.  
- **PC:** flusso già noto (CIFS / Archivio); Host compilabile a mano; opzionale in v1 del blocco: suggerimento host in rete (scan leggero porta 445) — **non bloccante** se assente.  
- **Drive / NAS:** guida + collegamento; niente falsa completezza se il ramo è ancora limitato — ma la **voce dispositivo** compare e spiega lo stato.

### Grado 3 — Raggiungibilità in sessione

Per ogni fonte: **Disponibile** / **Non in rete** / **Sconosciuto** (allineato Nota: niente retry automatici).  
Si possono selezionare per Conferma solo le fonti selezionate (✓) e, in pratica, acquisire da quelle Disponibili; le non raggiungibili restano visibili con stato chiaro.

---

## 3. UI Acquisisci (invarianti)

- Un solo processo Acquisisci → Importa (layout congelato).  
- **Riquadro a 3 stati** per fonte (BL-03): ✓ / vuoto / X — **niente** icona cestino separata.  
- Ordinamento: per **Dispositivo**, poi fonti.  
- Riepilogo → Conferma usa solo fonti in ✓.  
- Terminologia: **Catalogo**, **File del dispositivo**, **Dispositivo**.

---

## 4. Perimetro di implementazione del blocco (una sola tranche)

Incluso (da chiudere insieme):

1. Modello persistente Dispositivo ↔ Fonti (o equivalente con etichette stabili).  
2. Acquisisci ridisegnato a due livelli + stati Grado 3.  
3. Seed automatico «Questo telefono/tablet» + fonti già registrate (SAF/CIFS/Drive se presenti).  
4. Flusso **Aggiungi dispositivo/fonte** per i quattro tipi W1–W4 (riusando i collegamenti esistenti dove ci sono).  
5. PC/NAS già in CIFS visibili come dispositivo dedicato (non solo path anonimo).  
6. Test automatici sul raggruppamento / selezione; **una** prova telefono finale con istruzioni complete (REGOLE §6).  
7. Aggiornamento backlog/annotazioni: P0 Acquisisci → chiuso o «in verifica»; IP recovery resta parcheggiato.

Escluso (non toccare in questo blocco):

- Recupero automatico IP / annuncio LAN Wizard (congelato).  
- Riscrittura script CONFIGURA/sup.  
- Accesso peer file tra due telefoni MediaManager senza share.  
- Help contestuale Wizard (P0 metodo, voce separata).  
- Smart TV / IoT.

---

## 5. Criteri di accettazione (prova telefono unica)

1. Su Acquisisci compare almeno **Questo telefono** con le sue cartelle/fonti.  
2. Con CIFS già configurato (Host anche manuale), compare un **dispositivo PC** (o etichetta chiara) e le cartelle share usabili.  
3. Drive e/o NAS, se collegati, compaiono come dispositivo; se non collegati, **Aggiungi** propone il tipo giusto.  
4. Riquadro 3 stati funziona; X toglie dalla lista senza cestino separato.  
5. Fonte non in rete: visibile, stato «Non in rete», non blocca l’app né cancella il Catalogo.  
6. Conferma → Importa continua a funzionare sulle fonti ✓ disponibili.

---

## 6. Sequenza di lavoro dopo convalida (senza ricicli intermedi)

1. Implementazione completa punti §4.  
2. Test interni (unit + assemble + BAT/versionCode).  
3. **Una** richiesta di prova telefono con istruzioni §5.  
4. Solo se KO con riscontro puntuale: una correzione mirata, non un nuovo redesign.

---

## 7. Convalida

**Chiusa 23/10:** OK Renato (tablet, integrazione con Acquisisci esistente).  
Implementazione: **0.16.51** · prova unica: `docs/GUIDA_TEST_0.16.51_ACQUISISCI.md`.
