# Guida prova — LOCK test 0.16.101 (sei percorsi)

**LOCK:** `docs/LOCK_SETUP_TEST_101_SEI_DISPOSITIVI.md`  
**Dispositivo prova:** S25 Ultra · Catalogo: **MyMedia**  
**APK:** da assegnare dopo FIX (non 0.16.101 se non ricompilata).

Checkbox 1:1 sulle righe LOCK.

| # | Preparazione | Passi | Atteso | Riscontro |
|---|--------------|-------|--------|-----------|
| 1 | Reset/nuova installazione o Catalogo non configurato; nome MyMedia in prima pagina | Setup → Questo dispositivo → conferma attivazione **senza** scegliere cartella | Cartella MyMedia creata; attivazione OK; niente «Seleziona cartella» dopo OK; niente falso «fase terminata» | ☐ |
| 2 | — | Setup → Altro dispositivo | Messaggio chiaro (cosa fare / limiti v1) | ☐ |
| 3 | PC già con share (C: o E: o disco rete) | Apri scheda CIFS in guida | Domain e DFS: solo titolo, niente quadrati selezione né istruzioni extra | ☐ |
| 4 | Stesso PC, nuovo CONFIGURA se serve alias | CIFS → Users → profilo | Nomi cartelle come in Windows (Immagini, Documenti, …) | ☐ |
| 5 | Account Google | Setup → Cloud → Drive | Drive compare in elenco senza tentativi a caso; se lento, indicazione di attesa | ☐ |
