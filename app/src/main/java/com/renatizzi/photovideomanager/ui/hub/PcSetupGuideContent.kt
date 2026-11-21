package com.renatizzi.photovideomanager.ui.hub

/**
 * Contenuti guida Setup PC — USB-only (ripristino USB2.3).
 * Sei fasi autoconsistenti; Pronto (app) vs Fatto (manuale).
 */
enum class PcLinkMode {
    /** @deprecated Percorso BT abbandonato; resta per bozze legacy. */
    @Deprecated("USB-only: non esporre in UI")
    ALREADY_PAIRED,
    /** @deprecated Percorso BT abbandonato; resta per bozze legacy. */
    @Deprecated("USB-only: non esporre in UI")
    NEED_PAIRING,
    USB,
}

data class PcGuidePhase(
    val title: String,
    val bullets: List<String>,
    /** Etichetta CTA: Pronto / Fatto. */
    val actionLabel: String,
) {
    /** Testo unico per test / compat. */
    val body: String get() = bullets.joinToString("\n")
}

object PcSetupGuideContent {
    /** Fasi 1–5 della procedura USB (la Fase 6 è W2_CIFS_INSTALL + W2_CIFS). */
    const val PHASE_COUNT = 5
    const val TOTAL_PHASES = 6
    const val GUIDE_PDF_ASSET = "setup_guide/Guida_Setup_Catalogo_USB_ONLY.pdf"

    /** @deprecated Compat test legacy. */
    @Deprecated("USB-only")
    val CONFIRM_SEND_TO_PC = ""
    @Deprecated("USB-only")
    val CONFIRM_SCRIPT = ""
    @Deprecated("USB-only")
    val CONFIRM_SEND_TO_PHONE = ""
    @Deprecated("USB-only")
    val CONFIRM_CIFS = ""

    @Suppress("DEPRECATION")
    fun modeLabel(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED ->
            "Telefono e PC sono già associati (lo vedo in Bluetooth)"
        PcLinkMode.NEED_PAIRING ->
            "Telefono e PC NON sono ancora associati (devo fare il pairing)"
        PcLinkMode.USB ->
            "Collegamento tramite cavo USB (percorso unico)"
    }

    fun guideSubtitle(@Suppress("UNUSED_PARAMETER") mode: PcLinkMode = PcLinkMode.USB): String =
        "Guida collegamento Telefono e PC (USB)"

    fun guideDescription(): String =
        "Segui la fase corrente. L’intera guida può essere stampata qui"

    fun associationSubtitle(): String = "Collegamento fisico USB"

    fun associationDescription(): String =
        "Predisponi il collegamento con un cavo USB tra il Telefono e il PC."

    fun usbConnectBullets(): List<String> = listOf(
        "Collega il Telefono al PC con un cavo USB che supporti il trasferimento dati e mantieni il telefono sbloccato.",
        "Apri sulla Home del Telefono la videata del tipo di collegamento USB, passa da «Solo ricarica» a «Trasferimento file» e autorizza l’accesso ai dati.",
    )

    /** Elenco/overview 6 fasi (pag. 5 e 11 template). */
    fun sixPhaseOverview(): List<String> = listOf(
        "1. Creazione script sul TEL.",
        "2. Invio script al PC",
        "3. Esecuzione script sul PC",
        "4. Invio dati CIFS al TEL.",
        "5. Import dati CIFS sul TEL.",
        "6. Configurazione CIFS sul TEL.",
    )

    fun phases(@Suppress("UNUSED_PARAMETER") mode: PcLinkMode = PcLinkMode.USB): List<PcGuidePhase> =
        usbPhases()

    fun usbPhases(): List<PcGuidePhase> = listOf(
        PcGuidePhase(
            title = "Fase 1 – Creazione script sul Telefono",
            bullets = listOf(
                "Fase gestita dall’app dopo la tua conferma.",
                "Vengono creati alcuni file (script) necessari per configurare la cartella del Catalogo personale.",
                "I file creati dovranno essere inviati al PC tramite cavo USB nella fase successiva.",
            ),
            actionLabel = "Pronto",
        ),
        PcGuidePhase(
            title = "Fase 2 – Invio script al PC",
            bullets = listOf(
                "Fase gestita manualmente dall’utente mediante copia ed incolla con cavo USB.",
                "I file creati (script) e da inviare sul PC sono tre: CONFIGURA.CMD, QRCoder.dll e sup.ps1.",
                "Apri sul PC Esplora file → Questo PC → nome del Telefono → Memoria interna → Download → seleziona e copia i tre file → Incolla sul Desktop del PC.",
            ),
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 3 – Esecuzione script sul PC",
            bullets = listOf(
                "Fase gestita manualmente dall’utente.",
                "Apri sul PC Esplora file → Questo PC → Desktop → seleziona ed esegui con doppio clic CONFIGURA.CMD.",
                "Attendi il messaggio «Tutto pronto» che indica la conclusione dello script.",
                "Lo script crea in automatico no_pw.json e DATI_CIFS.json (solo quest’ultimo sarà inviato al Telefono).",
            ),
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 4 – Invio dati CIFS al TELEFONO",
            bullets = listOf(
                "Fase gestita manualmente dall’utente mediante cavo USB.",
                "Lo script eseguito crea in automatico no_pw.json e DATI_CIFS.json.",
                "Solo DATI_CIFS.json: Desktop PC → copia → Telefono → Memoria interna → Download → Incolla.",
            ),
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 5 – Import dati CIFS",
            bullets = listOf(
                "Fase gestita dall’app dopo la tua conferma.",
                "Il file in Download del Telefono — se trovato — viene letto e importato in MediaManager.",
                "I dati non sono modificabili a mano: se l’import fallisce, messaggio con istruzioni e riprova.",
            ),
            actionLabel = "Fatto",
        ),
    )

    fun phaseCompletedLabel(index: Int): String =
        "✓ FASE ${index + 1} COMPLETATA"

    fun lampLabel(index: Int): String = when (index) {
        0 -> "1 Creazione"
        1 -> "2 Invio"
        2 -> "3 Script"
        3 -> "4 DATI_CIFS"
        4 -> "5 Import"
        5 -> "6 CIFS"
        else -> "${index + 1}"
    }

    /** Indici mostrati insieme sulla stessa pagina (template USB2.3). */
    fun pageGroupFor(index: Int): IntRange = when (index) {
        0 -> 0..0
        1, 2 -> 1..2
        else -> 3..4
    }

    fun cifs61Title(): String = "Fase 6/1 – Configurazione CIFS"
    fun cifs61Bullets(): List<String> = listOf(
        "CIFS Documents Provider è necessaria per visualizzare dal Telefono la cartella condivisa del Catalogo.",
        "Installa o apri l’app da questa pagina (Play Store / MediaManager).",
        "Il copia dei campi avviene nello step successivo.",
    )
    fun cifs61Body(): String = cifs61Bullets().joinToString("\n")

    fun cifs62Title(): String = "Fase 6/2 – Copia dati CIFS"
    fun cifs62Bullets(): List<String> = listOf(
        "Apri CIFS → [+] → Input. Torna qui: per ogni campo tocca Copia e incolla in CIFS.",
        "Alla fine Salva in CIFS e verifica che la cartella sia presente nell’elenco.",
        "Torna qui, conferma di aver salvato i dati e tocca Attiva Catalogo.",
    )
    fun cifs62Body(): String = cifs62Bullets().joinToString("\n")
}
