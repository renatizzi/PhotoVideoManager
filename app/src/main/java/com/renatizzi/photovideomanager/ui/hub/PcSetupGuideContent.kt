package com.renatizzi.photovideomanager.ui.hub

/**
 * Contenuti guida Setup PC — template v3 + Ottimizzazione + Difficoltà_BT (6/11).
 * Messaggi di conferma letterali (senza citare BT/USB).
 */
enum class PcLinkMode {
    ALREADY_PAIRED,
    NEED_PAIRING,
    USB,
}

data class PcGuidePhase(
    val title: String,
    val body: String,
    val confirmMessage: String,
)

object PcSetupGuideContent {
    const val PHASE_COUNT = 4

    val CONFIRM_SEND_TO_PC =
        "Confermo di aver inviato il set di file dal Telefono al PC secondo le modalità descritte nella guida"
    val CONFIRM_SCRIPT =
        "Confermo di aver lanciato lo script ricevuto sul PC e di aver atteso il messaggio «Tutto pronto», secondo le modalità descritte nella guida"
    val CONFIRM_SEND_TO_PHONE =
        "Confermo di aver inviato il set di file dal PC al Telefono secondo le modalità descritte nella guida"
    val CONFIRM_CIFS =
        "Confermo di aver configurato CIFS e di aver attivato il collegamento secondo le modalità descritte nella guida"

    fun modeLabel(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED ->
            "Telefono e PC sono già associati (lo vedo in ‘Il mio Computer’)"
        PcLinkMode.NEED_PAIRING ->
            "Telefono e PC NON sono ancora associati (devo fare il pairing)"
        PcLinkMode.USB ->
            "Voglio usare il collegamento tramite cavo USB (non posso usare il BT)"
    }

    fun guideSubtitle(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED -> "Collegamento BT tra Telefono e PC già associati"
        PcLinkMode.NEED_PAIRING -> "Collegamento BT tra Telefono e PC non ancora associati"
        PcLinkMode.USB -> "Collegamento USB tra Telefono e PC"
    }

    fun guideDescription(): String =
        "Attiva la procedura semiautomatica per l’interscambio dei file di configurazione tra il tuo telefono e il PC. " +
            "Segui la guida cliccando sulla freccia accanto al titolo della fase per espandere il contenuto. " +
            "L’intera guida può essere stampata qui"

    fun associationSubtitle(): String = "Modalità di associazione tra Telefono e PC"

    fun associationDescription(): String =
        "Attiva il collegamento tra il tuo telefono e il PC dove hai deciso di tenere il Catalogo delle foto e video."

    fun phases(mode: PcLinkMode): List<PcGuidePhase> {
        val sendTitle = when (mode) {
            PcLinkMode.USB -> "Invio USB (dal Telefono al PC) dei file di configurazione"
            else -> "Invio BT (dal Telefono al PC) dei file di configurazione"
        }
        val returnTitle = when (mode) {
            PcLinkMode.USB -> "Invio USB (dal PC al Telefono) dei file generati dallo script"
            else -> "Invio BT (dal PC al Telefono) dei file generati dallo script"
        }
        return listOf(
            PcGuidePhase(sendTitle, sendBody(mode), CONFIRM_SEND_TO_PC),
            PcGuidePhase(
                "Lancio (dal desktop del PC) dello script ricevuto",
                SCRIPT_BODY,
                CONFIRM_SCRIPT,
            ),
            PcGuidePhase(returnTitle, returnBody(mode), CONFIRM_SEND_TO_PHONE),
            PcGuidePhase(
                "Configurazione CIFS e attivazione del Catalogo",
                CIFS_BODY,
                CONFIRM_CIFS,
            ),
        )
    }

    private fun sendBody(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.ALREADY_PAIRED ->
            "Se Telefono e PC sono già associati, non ripetere il pairing.\n\n" +
                "1) Nell’app, all’OK di questa fase, nel popup Condividi scegli l’opzione che invia i 3 file al PC " +
                "(es. Bluetooth / Collegamento… Invia al PC).\n" +
                "2) Conferma sul nome del PC proposto.\n" +
                "3) Sul PC trova i file (di solito Download; altrimenti Impostazioni → Bluetooth e dispositivi → " +
                "Dispositivi mobili → cartella trasferimenti).\n" +
                "4) Copia i 3 file sul Desktop (sostituisci le versioni precedenti: deve restare solo l’ultima).\n" +
                "5) Non restare sulla pagina Invio/Ricevi di sistema: torna a MediaManager."
        PcLinkMode.NEED_PAIRING ->
            "Prima: pairing dal Telefono (non dal PC).\n" +
                "Impostazioni → Connessioni → Bluetooth → Associa/Accoppia nuovo dispositivo → scegli il PC → " +
                "conferma il codice su entrambi.\n\n" +
                "Importante: il Bluetooth sul PC e sul Telefono non deve essere già impegnato da un altro " +
                "collegamento esclusivo, altrimenti i due dispositivi non si vedono.\n\n" +
                "Poi: come nel ramo «già associati» — popup Condividi → invia i 3 file → conferma nome PC → " +
                "trova i file sul PC → copia sul Desktop (overwrite) → torna a MediaManager."
        PcLinkMode.USB ->
            "Collega il Telefono al PC con cavo USB e sblocca il Telefono.\n" +
                "Sul PC: Esplora file → Telefono → Memoria interna → individua i 3 file generati dall’app " +
                "(o dalla cartella Download del Telefono).\n" +
                "Copiali sul Desktop del PC (overwrite versioni precedenti). Non aprire i file di supporto."
    }

    private const val SCRIPT_BODY =
        "Sul Desktop: doppio clic su CONFIGURA. Se compare UAC, conferma.\n" +
            "Non aprire i file di supporto. Attendi il messaggio verde «Tutto pronto».\n" +
            "Sul Desktop compare DATI_CIFS.json (e no_pw.json da non usare). " +
            "Ogni nuovo tentativo deve sostituire i file precedenti."

    private fun returnBody(mode: PcLinkMode): String = when (mode) {
        PcLinkMode.USB ->
            "Copia solo DATI_CIFS.json dal Desktop alla cartella Download del Telefono (via USB). " +
                "Ignora no_pw.json e file omonimi vecchi.\n" +
                "Poi torna all’app e importa DATI_CIFS.json."
        else ->
            "Dal Desktop seleziona solo DATI_CIFS.json. Invia via Bluetooth (Invia file). " +
                "Phone Link / «Collegamento al telefono» solo se già attivo (non obbligatorio).\n" +
                "Ignora no_pw.json. Torna a MediaManager: non restare sulla pagina Invio/Ricevi di sistema, " +
                "poi importa DATI_CIFS.json."
    }

    private const val CIFS_BODY =
        "Nell’app: Importa DATI_CIFS.json → OK. Poi Apri CIFS Documents Provider, " +
            "Copia Host/User/Password/Folder (MyMedia), salva, Attiva collegamento → Usa questa cartella."
}
