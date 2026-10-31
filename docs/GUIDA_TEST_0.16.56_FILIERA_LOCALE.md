# Filiera 1 — Catalogo su questo dispositivo (0.16.56)

**Versione:** `0.16.56-preview` · **versionCode:** `97`  
**Scope:** solo dispositivo locale. Cloud / PC / NAS in tranche successive.

## Cosa è stato verificato/sistemato in codice
- Setup locale: Avvio → Scelta cartella → **Salva** obbligatorio (serve URI) → Torna all’accesso → riga Catalogo in Acquisisci (refresh sede).
- Acquisizione: censimento **ricorsivo** delle sottocartelle (contratto BFS come Pictures → familiari/vacanze); test automatico `CensusRecursionContractTest`.
- Testi UI: esplicitato che la cartella sorgente include le sottocartelle.

## Prova utente (completa a tuo carico)
1. Crea Catalogo su questo dispositivo e controlla Acquisisci/Dashboard/Componi.
2. Aggiungi sorgente (es. Pictures) → CONFERMA: devono entrare anche i media nelle sottocartelle.
3. Importa / Aggiorna come da uso normale.
