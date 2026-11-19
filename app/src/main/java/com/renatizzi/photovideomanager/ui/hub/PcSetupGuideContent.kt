package com.renatizzi.photovideomanager.ui.hub

/**
 * Contenuti guida Setup PC — REV9 Parte 2 (online) + integrazioni v7 (porte / ascolto).
 * Una fase alla volta; nessun messaggio «Confermo di aver…».
 */
enum class PcLinkMode {
    ALREADY_PAIRED,
    NEED_PAIRING,
    USB,
}

data class PcGuidePhase(
    val title: String,
    val body: String,
    /** Etichetta CTA d’azione (non un assenso vuoto). */
    val actionLabel: String,
)

object PcSetupGuideContent {
    const val PHASE_COUNT = 4
    const val GUIDE_PDF_ASSET = "setup_guide/Guida_Setup_Catalogo_MediaManager_REV9.pdf"

    /** @deprecated Usato solo da test legacy; UI v7 non mostra autodichiarazioni. */
    @Deprecated("v7: niente autodichiarazioni")
    val CONFIRM_SEND_TO_PC = ""
    @Deprecated("v7: niente autodichiarazioni")
    val CONFIRM_SCRIPT = ""
    @Deprecated("v7: niente autodichiarazioni")
    val CONFIRM_SEND_TO_PHONE = ""
    @Deprecated("v7: niente autodichiarazioni")
    val CONFIRM_CIFS = ""

    fun modeLabel(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED ->
            "Telefono e PC sono già associati (lo vedo in Bluetooth)"
        PcLinkMode.NEED_PAIRING ->
            "Telefono e PC NON sono ancora associati (devo fare il pairing)"
        PcLinkMode.USB ->
            "Voglio usare il collegamento tramite cavo USB (non posso usare il BT)"
    }

    fun guideSubtitle(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED -> "Guida — Fase corrente (Bluetooth già associati)"
        PcLinkMode.NEED_PAIRING -> "Guida — Fase corrente (Bluetooth non associati)"
        PcLinkMode.USB -> "Guida — Fase corrente (USB)"
    }

    fun guideDescription(): String =
        "Segui la fase corrente. L’intera guida può essere stampata qui"

    fun associationSubtitle(): String = "Modalità di associazione tra Telefono e PC"

    fun associationDescription(): String =
        "Attiva il collegamento tra il tuo telefono e il PC dove hai deciso di tenere il Catalogo delle foto e video. " +
            "Mantieni lo stesso metodo anche per riportare DATI_CIFS.json."

    fun phases(mode: PcLinkMode): List<PcGuidePhase> = listOf(
        PcGuidePhase(
            title = "Fase 1 — Trasferisci i tre file dal Telefono al PC",
            body = sendBody(mode),
            actionLabel = "Invia set di configurazione",
        ),
        PcGuidePhase(
            title = "Fase 2 — Esegui lo script sul PC",
            body = SCRIPT_BODY,
            actionLabel = "Apri istruzioni script",
        ),
        PcGuidePhase(
            title = "Fase 3 — Trasferisci DATI_CIFS.json al Telefono",
            body = returnBody(mode),
            actionLabel = "Usa questi dati",
        ),
        PcGuidePhase(
            title = "Fase 4 — Importa i dati e configura CIFS",
            body = CIFS_BODY,
            actionLabel = "Vai a configurazione CIFS",
        ),
    )

    fun lampLabel(index: Int): String = when (index) {
        0 -> "1 Invio set"
        1 -> "2 Script PC"
        2 -> "3 Dati CIFS"
        else -> "4 Attivazione"
    }

    private fun sendBody(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED ->
            "Se il nome del PC compare già nell’elenco Bluetooth del Telefono, non ripetere il pairing. " +
                "Attiva Bluetooth su entrambi.\n\n" +
                "PC: Trasferimento file Bluetooth → Ricevi file.\n" +
                "Telefono: tap su «Invia set di configurazione» → seleziona il PC.\n\n" +
                "I file non arrivano sul Desktop: cerca la cartella di ricezione (es. File ricevuti), " +
                "copia sul Desktop CONFIGURA.CMD, QRCoder.dll e sup.ps1 " +
                "(sostituisci le versioni precedenti; evita nomi con (1)/(2)).\n" +
                "In alternativa puoi avviare CONFIGURA.CMD direttamente dalla cartella di ricezione."
        PcLinkMode.NEED_PAIRING ->
            "PC: Impostazioni → Bluetooth (attiva e lascia aperto).\n" +
                "Telefono: Impostazioni → Bluetooth → cerca il PC → seleziona → " +
                "verifica il codice e conferma su entrambi.\n\n" +
                "Poi PC: Trasferimento file Bluetooth → Ricevi file.\n" +
                "Telefono: tap su «Invia set di configurazione» → seleziona il PC.\n\n" +
                "I file non arrivano sul Desktop: cercali nella cartella di ricezione e copiali sul Desktop " +
                "(overwrite; niente (1)/(2)), oppure avvia CONFIGURA dalla cartella di ricezione."
        PcLinkMode.USB ->
            "Collega il cavo dati; sul Telefono seleziona Trasferimento file / MTP.\n" +
                "PC: Questo PC → Telefono → Memoria interna → Download → " +
                "copia sul Desktop CONFIGURA.CMD, QRCoder.dll e sup.ps1 (overwrite, niente (1)/(2))."
    }

    private const val SCRIPT_BODY =
        "Sul PC: doppio clic su CONFIGURA.CMD (Desktop oppure cartella di ricezione).\n" +
            "Segui le istruzioni; non chiudere la finestra.\n" +
            "Al termine deve comparire DATI_CIFS.json. Lo script ripulisce i duplicati (1)/(2).\n" +
            "Se lo script segnala un errore o il JSON non viene creato, fermati.\n\n" +
            "Resta su questa schermata: l’app ascolta l’arrivo di DATI_CIFS.json."

    private fun returnBody(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.USB ->
            "Se i dati non sono già stati rilevati: copia DATI_CIFS.json dal Desktop del PC " +
                "alla cartella Download del Telefono (solo il file senza (1)/(2)).\n" +
                "L’app lo intercetta automaticamente; il selettore file resta come recupero."
        else ->
            "Se i dati non sono già stati rilevati: PC → Desktop → tasto destro su DATI_CIFS.json → " +
                "Invia a dispositivo Bluetooth → Telefono → accetta.\n" +
                "L’app lo intercetta automaticamente; il selettore file resta come recupero."
    }

    private const val CIFS_BODY =
        "Usa i dati ricevuti: Apri CIFS Documents Provider, Copia Host/User/Password/Folder un campo alla volta, " +
            "salva. Verifica che la cartella/dispositivo compaia nella lista CIFS. " +
            "Solo dopo: Attiva Catalogo."
}
