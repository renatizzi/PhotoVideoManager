# Guida prova telefono — Acquisisci dispositivi / fonti (0.16.51 → 0.16.52)

**Versione APK:** `0.16.52-preview` · **versionCode:** `93` (sostituisce 0.16.51)  
**Obiettivo:** una sola prova (REGOLE §9). Criteri da `docs/PROPOSTA_VISIBILITA_FONTI_ACQUISISCI.md` §5.  
**Fuori prova:** recupero automatico IP Wizard CIFS (parcheggio; IP manuale ok).

**0.16.52 (chiarimento UI):** in alto **Catalogo di riferimento** (Posizione + raggiungibile sì/no); sotto i gruppi fonti. Il gruppo locali è **Questo tablet** (non il nome G:/GeeKom).

---

## Prima di installare

1. Sul PC: `consegna-telefono.bat` (o Installazione APK da Studio).  
2. Controllare che l’APK installata sia **0.16.51-preview** (Impostazioni app / About).  
3. Tablet e (se usi PC/NAS) dispositivi sulla stessa Wi‑Fi.

---

## Percorso obbligatorio (ordine)

### 1) Catalogo di riferimento + elenco per dispositivo

1. Apri **Acquisisci**.  
2. In alto: blocco **Catalogo di riferimento** con nome Posizione (es. ssd extreme / GeeKom) e **Raggiungibile** / **Non raggiungibile**.  
3. Sotto: «Dispositivi e cartelle (fonti)» + Refresh / **+** / **CONFERMA**.  
4. Gruppo locali = nome **tablet** (o «Questo tablet: …»), **non** lo stesso testo del Catalogo di riferimento.  
5. Sotto quell’intestazione: cartelle locali con riquadro ✓/vuoto/X e **Disponibile** / **Non disponibile**.

**OK se:** Catalogo e fonti sono due zone distinte; il gruppo tablet non si chiama come la Posizione Catalogo.

### 2) Aggiungi W1–W4 (menu +)

1. Tocca **+** (cerchio).  
2. Dialog «Aggiungi dispositivo o cartella» con quattro voci:  
   - Su questo telefono o tablet  
   - Su un PC o notebook  
   - Su Google Drive (o cloud)  
   - Su un disco di rete o NAS  
   e opzionale «Prepara Posizione del Catalogo (PC)».  
3. **Questo dispositivo** → si apre il selettore cartelle (come il vecchio +). Aggiungi una cartella locale se vuoi.  
4. **PC** → guida «Da PC» già nota → «Scegli cartella» (Disco di rete / share).  
5. **Drive** / **NAS** → tip breve → «Scegli cartella» (stesso SAF).

**OK se:** le quattro voci ci sono e riaprono i flussi già usati (nessun percorso nuovo inventato).

### 3) PC già in CIFS (se lo hai)

1. Con share già collegata (Host anche digitato a mano), torna in Acquisisci e Refresh.  
2. Deve comparire un gruppo **PC / notebook** (o etichetta chiara) con la/e cartella/e share.  
3. Non deve sparire il gruppo del telefono.

**OK se:** PC e telefono sono due intestazioni distinte.

### 4) Drive / NAS (se collegati)

1. Cartella Drive o NAS già autorizzata → gruppo **Google Drive** o **Disco di rete / NAS**.  
2. Se non collegati: basta che **Aggiungi** proponga il tipo giusto (punto 2).

### 5) Riquadro 3 stati + Conferma

1. Tocca il riquadro di una fonte: cicla ✓ → vuoto → X (come prima; niente cestino).  
2. Con almeno una fonte in ✓ e **Disponibile**, tocca **CONFERMA**.  
3. Deve aprirsi / proseguire verso **Importa** come nelle build precedenti.

**OK se:** selezione e Conferma→Importa invariati nel comportamento.

### 6) Fonte non in rete (opzionale ma utile)

1. Spegnere PC/NAS o togliere Wi‑Fi della share; Refresh.  
2. La fonte resta in elenco con **Non in rete** (o equivalente); l’app non si chiude e il Catalogo non si cancella.

---

## Esito da riportare

Rispondi con **una** riga per criterio:

| # | Criterio | OK / KO | Nota breve |
|---|----------|---------|------------|
| 1 | Gruppo telefono + fonti | | |
| 2 | Menu Aggiungi W1–W4 | | |
| 3 | Gruppo PC se CIFS | | |
| 4 | Drive/NAS o voce Aggiungi | | |
| 5 | Riquadro + Conferma→Importa | | |
| 6 | Non in rete (se provato) | | |

Se KO: solo il punto fallito + cosa vedi a schermo (niente ridisegno generale).
