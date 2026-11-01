# Guida prova telefono — 0.16.55 fix F1–F4 + chrome TO-BE

**Versione:** `0.16.55-preview` · **versionCode:** `96`  
**Spec pin:** `PVM_Proposta_Editing_UI_Template_TOBE.html` @ `c1ef993`

## Obiettivo
Verificare che il Catalogo creato risulti subito in Acquisisci; Salva esplicito; niente SI/NO accesso; Setup Sorgenti; testi chrome allineati.

## Preparazione
1. Installare APK 0.16.55 (versionCode 96) con `consegna-telefono.bat`.
2. Se serve: disinstalla prima l’app (prefs sede non devono ripristinarsi da backup).

## Procedura

### A — F1 Catalogo → Acquisisci
1. Apri **Acquisisci** senza Catalogo → vedi invito «Catalogo Personale non presente…».
2. Tocca l’invito → Setup → Avvio procedura → CONTINUA (locale) → **Scegli cartella** → **Salva**.
3. Tocca **Torna all’accesso**.
4. **Atteso:** in Acquisisci la riga mostra `Catalogo: …` (non più l’invito) e compaiono + / CONFERMA.

### B — F2/F3 Salva
1. Setup locale: dopo pick, **Salva** obbligatorio (non salva da solo al pick).
2. Setup PC RAPIDA → CIFS → pick → **Salva** (niente domanda Sì/No accesso) → esito → Torna all’accesso.

### C — F4 Setup Sorgenti
1. In Acquisisci con Catalogo: tocco **+** → dialog **Setup Sorgenti / Avvio procedura**.
2. Scegli dispositivo → Scegli cartella → dopo pick: «Configurazione completata… sorgente disponibile…».
3. **Atteso:** sede Catalogo invariata; nuova fonte in elenco.

### D — Chrome (smoke)
1. Dashboard: sottotitolo con «kpi e scorciatoie» + description homepage.
2. Organizza: sottotitolo con «Catalogo Personale» + description.
3. Wizard CIFS: sottotitolo «Collegamento CIFS».

## Segnali di errore
- Dopo Torna all’accesso, Acquisisci ancora «non presente» → F1 non risolto.
- Pick senza Salva che completa comunque → F2 non risolto.
- Domanda SI/NO accesso dopo pick PC → F3 non risolto.
- + apre ancora solo elenco W1–W4 senza «Setup Sorgenti» → F4 non risolto.

## Riscontro da inviare
Per ogni sezione A–D: OK / KO + screenshot se KO (titolo + riga Catalogo).
