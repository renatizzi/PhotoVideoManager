package com.renatizzi.photovideomanager.ui.hub

/**
 * Contenuti guida Setup PC — USB-only (specifica Cursor REV4).
 * Sette fasi, una pagina ciascuna; Pronto (app) vs Fatto (manuale).
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
    /** Fasi 1–5 in PC_GUIDE (Fase 6 = W2_CIFS_INSTALL, Fase 7 = W2_CIFS). */
    const val PHASE_COUNT = 5
    /** Totale fasi operative della procedura (1–7). */
    const val TOTAL_PHASES = 7
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

    /** @deprecated REV4: il titolo fase sostituisce il sottotitolo generico. */
    @Deprecated("REV4: usare titolo fase")
    fun guideSubtitle(@Suppress("UNUSED_PARAMETER") mode: PcLinkMode = PcLinkMode.USB): String =
        "Guida collegamento Telefono e PC (USB)"

    fun guideDescription(): String =
        "L’intera guida può essere stampata qui"

    fun cosaFareLabel(): String = "Cosa fare:"

    fun associationSubtitle(): String = "Collegamento fisico USB"

    fun associationDescription(): String =
        "Predisponi il collegamento con un cavo USB tra il Telefono e il PC."

    fun usbConnectBullets(): List<String> = listOf(
        "Collega il Telefono al PC con un cavo USB che supporti il trasferimento dati. Mantieni entrambi i dispositivi accesi e sbloccati.",
        "Sul Telefono, scorri dall’alto verso il basso per aprire il pannello delle notifiche. Apri la notifica relativa alle impostazioni USB.",
        "Seleziona «Trasferimento di file» (o «Trasferimento file/MTP») al posto di «Solo ricarica» e autorizza l’accesso ai dati, se richiesto.",
    )

    /** Overview 7 fasi (specifica REV4). */
    fun sevenPhaseOverview(): List<String> = listOf(
        "1. Creazione file di configurazione",
        "2. Invio script al PC",
        "3. Esecuzione script sul PC",
        "4. Invio dati CIFS al Telefono",
        "5. Import dati CIFS",
        "6. Installa / Apri CIFS",
        "7. Configura CIFS e attiva Catalogo",
    )

    /** @deprecated Alias verso [sevenPhaseOverview]. */
    @Deprecated("REV4: sette fasi", ReplaceWith("sevenPhaseOverview()"))
    fun sixPhaseOverview(): List<String> = sevenPhaseOverview()

    fun phases(@Suppress("UNUSED_PARAMETER") mode: PcLinkMode = PcLinkMode.USB): List<PcGuidePhase> =
        usbPhases()

    fun usbPhases(): List<PcGuidePhase> = listOf(
        PcGuidePhase(
            title = "Fase 1 – Creazione file di configurazione",
            bullets = listOf(
                "Fase gestita dall’app dopo la tua conferma.",
                "Saranno creati nella cartella Download del Telefono i file (script) necessari per configurare la cartella del Catalogo personale.",
            ),
            actionLabel = "Pronto",
        ),
        PcGuidePhase(
            title = "Fase 2 – Invio script al PC",
            bullets = listOf(
                "Fase gestita manualmente dall’utente.",
                "I file creati da inviare al PC sono tre: CONFIGURA.CMD, QRCoder.dll e sup.ps1.",
                "Sul Telefono apri File/Archivio → Memoria interna → Download. Seleziona e copia i tre file.",
                "Sul PC apri Esplora file → Questo PC → Nome del Telefono → Memoria interna → Download. Seleziona i tre file e copiali sul Desktop del PC.",
            ),
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 3 – Esecuzione script sul PC",
            bullets = listOf(
                "Fase gestita manualmente dall’utente.",
                "Sul PC apri Esplora file → Questo PC → Desktop. Seleziona CONFIGURA.CMD e avvialo con doppio clic.",
                "Segui le istruzioni visualizzate e attendi il messaggio «Tutto pronto», che indica la conclusione dello script.",
                "Lo script crea automaticamente i file no_pw.json e DATI_CIFS.json. Solo DATI_CIFS.json sarà trasferito al Telefono per la configurazione CIFS.",
            ),
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 4 – Invio dati CIFS al Telefono",
            bullets = listOf(
                "Fase gestita manualmente dall’utente.",
                "Lo script ha creato automaticamente il file DATI_CIFS.json.",
                "Sul PC apri Esplora file → Desktop e individua DATI_CIFS.json.",
                "Collega il Telefono al PC con il cavo USB. Apri la memoria interna del Telefono → Download e copia qui DATI_CIFS.json.",
            ),
            actionLabel = "Fatto",
        ),
        PcGuidePhase(
            title = "Fase 5 – Import dati CIFS",
            bullets = listOf(
                "Fase gestita dall’app dopo la conferma dell’utente.",
                "Il file DATI_CIFS.json si trova nella cartella Download del Telefono.",
                "Premi «Importa dati CIFS» e seleziona DATI_CIFS.json tramite il selettore file.",
                "MediaManager legge e interpreta il file e prepara i dati necessari per la configurazione CIFS.",
            ),
            actionLabel = "Fatto",
        ),
    )

    fun phaseCompletedLabel(index: Int): String =
        "✓ FASE ${index + 1} COMPLETATA"

    /** Indice fase corrente 0-based (0..4) da [guidePhaseDone]. */
    fun currentGuidePhaseIndex(guidePhaseDone: List<Boolean>): Int =
        (0 until PHASE_COUNT)
            .firstOrNull { !guidePhaseDone.getOrElse(it) { false } }
            ?: (PHASE_COUNT - 1)

    /** Una sola fase per pagina (REV4). */
    fun pageGroupFor(index: Int): IntRange = index.coerceIn(0, PHASE_COUNT - 1).let { it..it }

    fun cifs61Title(): String = "Fase 6 – Installa / Apri CIFS"
    fun cifs61Bullets(): List<String> = listOf(
        "Fase gestita dall’utente.",
        "Installa o apri CIFS Documents Provider, necessario per accedere dal Telefono alla cartella condivisa del Catalogo creata sul PC.",
        "Al termine torna a MediaManager e conferma il completamento.",
    )
    fun cifs61Body(): String = cifs61Bullets().joinToString("\n")

    fun cifs62Title(): String = "Fase 7 – Configura CIFS e attiva Catalogo"
    fun cifs62Bullets(): List<String> = listOf(
        "Apri CIFS → premi [+] → seleziona «Input».",
        "Torna a questa schermata di MediaManager.",
        "Per ogni campo: premi il relativo pulsante «Copia» in MediaManager, passa a CIFS e incolla il valore nel campo corrispondente.",
        "Ripeti l’operazione fino a completare tutti i campi richiesti.",
        "Salva la configurazione in CIFS e verifica che la cartella condivisa sia presente nell’elenco di CIFS.",
        "Torna a MediaManager e conferma di aver salvato i dati.",
        "Premi «Attiva Catalogo».",
    )
    fun cifs62Body(): String = cifs62Bullets().joinToString("\n")
}
