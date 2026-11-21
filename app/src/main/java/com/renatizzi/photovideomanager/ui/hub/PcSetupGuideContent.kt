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
    val body: String,
    /** Etichetta CTA: Pronto / Fatto / Importa… */
    val actionLabel: String,
)

object PcSetupGuideContent {
    /** Fasi 1–5 della procedura USB (la Fase 6 è W2_CIFS_INSTALL + W2_CIFS). */
    const val PHASE_COUNT = 5
    const val GUIDE_PDF_ASSET = "setup_guide/Guida_Setup_Catalogo_USB_ONLY.pdf"

    /** @deprecated Usato solo da test legacy; UI USB-only non mostra autodichiarazioni. */
    @Deprecated("USB-only: niente autodichiarazioni")
    val CONFIRM_SEND_TO_PC = ""
    @Deprecated("USB-only: niente autodichiarazioni")
    val CONFIRM_SCRIPT = ""
    @Deprecated("USB-only: niente autodichiarazioni")
    val CONFIRM_SEND_TO_PHONE = ""
    @Deprecated("USB-only: niente autodichiarazioni")
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

    fun guideSubtitle(mode: PcLinkMode): String =
        "Guida — Fase corrente (USB)"

    fun guideDescription(): String =
        "Segui la fase corrente. L’intera guida può essere stampata qui"

    fun associationSubtitle(): String = "Collegamento fisico USB"

    fun associationDescription(): String =
        "Predisponi il collegamento con un cavo USB tra il Telefono e il PC. " +
            "Usa Trasferimento file / MTP (non Solo ricarica)."

    fun usbConnectBullets(): List<String> = listOf(
        "Collega il Telefono al PC con un cavo USB che supporti il trasferimento dati " +
            "e mantieni il telefono sbloccato.",
        "Apri sulla Home del Telefono la videata del tipo di collegamento USB, " +
            "passa da «Solo ricarica» a «Trasferimento file» e autorizza l’accesso ai dati.",
    )

    fun phases(@Suppress("UNUSED_PARAMETER") mode: PcLinkMode = PcLinkMode.USB): List<PcGuidePhase> =
        usbPhases()

    fun usbPhases(): List<PcGuidePhase> = listOf(
        PcGuidePhase(
            title = "Fase 1 – Creazione script sul Telefono",
            body =
                "Fase gestita dall’app dopo la tua conferma.\n" +
                    "Vengono creati i file necessari per configurare la cartella del Catalogo personale: " +
                    "CONFIGURA.CMD, QRCoder.dll e sup.ps1.\n" +
                    "I file restano in Download del Telefono per la copia USB nella fase successiva.\n\n" +
                    "Quando pronto clicca sul relativo bottone.",
            actionLabel = "Pronto",
        ),
        PcGuidePhase(
            title = "Fase 2 – Invio script al PC",
            body =
                "Fase gestita manualmente mediante copia con cavo USB.\n" +
                    "I file da inviare sono tre: CONFIGURA.CMD, QRCoder.dll e sup.ps1.\n" +
                    "PC: Esplora file → Questo PC → nome del Telefono → Memoria interna → Download → " +
                    "seleziona e copia i tre file → Incolla sul Desktop del PC " +
                    "(sostituisci versioni precedenti; evita nomi con (1)/(2)).",
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 3 – Esecuzione script sul PC",
            body =
                "Fase gestita manualmente.\n" +
                    "PC: Desktop → doppio clic su CONFIGURA.CMD → avvio esecuzione.\n" +
                    "Attendi il messaggio «Tutto pronto».\n" +
                    "Lo script crea no_pw.json e DATI_CIFS.json " +
                    "(solo DATI_CIFS.json va inviato al Telefono).",
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 4 – Invio dati CIFS al TELEFONO",
            body =
                "Fase gestita manualmente mediante cavo USB.\n" +
                    "Dal Desktop del PC: seleziona e copia DATI_CIFS.json → " +
                    "Telefono → Memoria interna → Download → Incolla il file.\n" +
                    "Non usare no_pw.json né file con nome DATI_CIFS (1).json.",
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 5 – Import dati CIFS",
            body =
                "Fase gestita dall’app dopo la tua conferma.\n" +
                    "Il file in Download del Telefono — se trovato — viene letto e importato in MediaManager.\n" +
                    "I dati (Host, User, Password, Folder, …) non sono modificabili a mano: " +
                    "se l’import fallisce, riprova (niente digitazione).",
            actionLabel = "Fatto",
        ),
    )

    fun phaseCompletedLabel(index: Int): String =
        "✓ FASE ${index + 1} COMPLETATA"

    fun lampLabel(index: Int): String = when (index) {
        0 -> "1 Creazione script"
        1 -> "2 Invio script"
        2 -> "3 Esecuzione"
        3 -> "4 Invio DATI_CIFS"
        4 -> "5 Import"
        else -> "${index + 1}"
    }

    /** Indici mostrati insieme sulla stessa pagina (template USB2.3). */
    fun pageGroupFor(index: Int): IntRange = when (index) {
        0 -> 0..0
        1, 2 -> 1..2
        else -> 3..4
    }

    fun cifs61Title(): String = "Fase 6/1 – Configurazione CIFS"
    fun cifs61Body(): String =
        "Fase semiautomatica: CIFS Documents Provider è necessaria per vedere dal Telefono " +
            "la cartella condivisa del Catalogo.\n" +
            "Installa o apri l’app, poi tocca Fatto. Il copia dei campi è nello step successivo."

    fun cifs62Title(): String = "Fase 6/2 – Copia dati CIFS"
    fun cifs62Body(): String =
        "Apri CIFS → [+] → Input. Torna qui: per ogni campo tocca Copia e incolla in CIFS.\n" +
            "Alla fine salva in CIFS, verifica che la cartella sia in elenco, " +
            "conferma e tocca Attiva Catalogo."
}
