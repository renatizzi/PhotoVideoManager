# Guida test 0.16.96 — esito UX + ACL profili CIFS

Build: `0.16.96-preview` (versionCode 137).

## Cosa cambia

1. **Fase 3**: niente messaggio residuo della Fase 2 (copia USB / file pronti).
2. **CIFS (Fase 7)**: campo **Domain** vuoto, senza bottone Copia.
3. **Attiva Catalogo**: dopo la scelta cartella il bottone si disabilita; scroll automatico su **Salva**.
4. **Acquisisci**: messaggio sotto non ripete più l’hint del [+].
5. **Acquisisci → PC**: un solo percorso chiaro (apri CIFS / scegli cartella); niente «No, va creata».
6. **Script CONFIGURA**: ACL RX ricorsiva (`/T`) **solo** sui profili in `Users`/`Utenti` (es. `Dell XPS 15 7590`), così AccessBased li elenca in CIFS. Mai `/T` su tutta `C:\`.

## Cosa rifare sul PC

1. Sul telefono: Setup Catalogo → ripeti almeno Fase 1 (nuovi script in Download) e Fase 2–3 (copia + esegui `CONFIGURA.CMD`).
2. Attendi «Tutto pronto» (log: `ACL profilo utente: …`).
3. In CIFS: apri la share del Catalogo → `Users` (o `Utenti`) → deve comparire `Dell XPS 15 7590`.
4. Acquisisci → [+] → Su un PC → Apri CIFS → Scegli cartella foto.

## Note

- Se la share era già attiva, **bisogna rieseguire** lo script: i permessi NTFS non si aggiornano da soli.
- Cartelle di sistema restano escluse; top-level dati (es. `Dell XPS 15 7590` anche in `C:\`) ricevono RX senza `/T` sulla radice.
