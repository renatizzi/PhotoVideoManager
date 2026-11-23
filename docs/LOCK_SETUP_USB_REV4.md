# LOCK — Setup Catalogo USB-only (specifica Cursor REV4)

**Stato:** approvato (sviluppo)  
**Data:** 2026-11-23  
**Richiesta / test di riferimento:** `Template_Setup_Catalogo_ONLY_USB_specifica_Cursor_REV4.pdf` + chiarimenti Renato (7 fasi, no strip, pagina unica, titolo fase → sottotitolo, testi nuovi)  
**Approvazione Renato:** 23/11/2026 («Se è tutto chiaro puoi procedere»)  
**Versione target:** `0.16.78-preview` / `versionCode` 119  
**Baseline precedente:** LOCK USB2.3 / USB75 (6 fasi + strip) — **superata** da questa decisione sui punti in conflitto.

| # | Decisione | Risultato atteso (misurabile) | Fuori scope |
|---|-----------|-------------------------------|-------------|
| 1 | 7 fasi per linearità | Fasi 1–7 separate; TOTAL_PHASES=7; 6=Installa/Apri; 7=Copia+Attiva | Accorpamento 2+3 / 4+5 |
| 2 | Eliminare strip numerica | Nessuna riga 1…N; avanzamento = titolo fase | Nuova flowchart |
| 3 | Una fase per pagina | `pageGroupFor` = indice..indice; bottoni Annulla/CTA sempre visibili (CTA disabilitata se checkpoint OK) | Nascondere bottoni dopo Fatto sulla stessa card |
| 4 | Titolo fase = sottotitolo | Bold/corsivo/blu; niente duplicazione titolo in body | Nuovo chrome TopBar |
| 5 | Testi REV4 | Contenuti da specifica (USB fisico notifiche; titoli Fase 1/6/7; sequenza Fase 7) | Riscrittura guida PDF esterna in questo lock |

## Note operative

- Specifica: upload `Template_Setup_Catalogo_ONLY_USB_specifica_Cursor_REV4.pdf`
- Branch: `cursor/setup-usb-rev4-7fasi-b602`
- Una unità di lavoro per commit; build + unit test prima di prova telefono.
