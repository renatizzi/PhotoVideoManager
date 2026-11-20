package com.renatizzi.photovideomanager.ui.hub

/**
 * Guide a fasi per target non-PC — solo contenuti già documentati (no protocolli nuovi).
 * Scheletro analogo al PC: titoli autoconsistenti + conferma lineare.
 */
object AnalogSetupGuideContent {
    data class Phase(val title: String, val body: String, val confirmMessage: String)

    /** Locale / altro dispositivo mobile raggiungibile via SAF su questo Telefono. */
    fun localPhases(deviceLabel: String): List<Phase> = listOf(
        Phase(
            title = "Scelta della cartella sul dispositivo",
            body = "Seleziona la cartella che ospiterà il Catalogo su $deviceLabel. " +
                "Con «Telefono» si intende anche un Tablet Android. " +
                "Usa Seleziona Cartella e conferma l’accesso.",
            confirmMessage =
                "Confermo di aver scelto la cartella del Catalogo secondo le modalità descritte nella guida",
        ),
        Phase(
            title = "Attivazione del Catalogo",
            body = "Dopo la scelta della cartella, salva. Il Catalogo resta su questo dispositivo " +
                "(nessuna rete obbligatoria).",
            confirmMessage =
                "Confermo di aver attivato il Catalogo sul dispositivo secondo le modalità descritte nella guida",
        ),
    )

    fun cloudPhases(): List<Phase> = listOf(
        Phase(
            title = "Account e cartella Cloud",
            body = "Serve un account Google sul dispositivo. Tocca Seleziona Cartella, apri Google Drive, " +
                "crea o seleziona una cartella (es. Foto Catalogo) e conferma. " +
                "Non è obbligatorio usare il cloud.",
            confirmMessage =
                "Confermo di aver scelto la cartella Cloud del Catalogo secondo le modalità descritte nella guida",
        ),
        Phase(
            title = "Attivazione del Catalogo",
            body = "Dopo la conferma SAF, salva. MediaManager userà quella cartella Drive come Posizione del Catalogo.",
            confirmMessage =
                "Confermo di aver attivato il Catalogo su Cloud secondo le modalità descritte nella guida",
        ),
    )

    fun nasPhases(): List<Phase> = listOf(
        Phase(
            title = "Collegamento CIFS al disco di rete / NAS",
            body = "Collega il NAS/disco di rete con CIFS Documents Provider (Host, porta 445, utente, cartella). " +
                "Le credenziali restano in CIFS; MediaManager sceglie solo la cartella. " +
                "Apri CIFS Documents Provider se non è ancora installato.",
            confirmMessage =
                "Confermo di aver configurato CIFS e di aver attivato il collegamento secondo le modalità descritte nella guida",
        ),
        Phase(
            title = "Scelta della cartella Catalogo",
            body = "Quando CIFS mostra la cartella del NAS, tocca Seleziona Cartella qui in MediaManager " +
                "e conferma l’accesso.",
            confirmMessage =
                "Confermo di aver scelto la cartella del Catalogo sul disco di rete secondo le modalità descritte nella guida",
        ),
    )
}
