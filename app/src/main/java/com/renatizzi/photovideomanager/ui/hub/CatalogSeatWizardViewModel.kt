package com.renatizzi.photovideomanager.ui.hub

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.data.hub.PcCatalogSeatGate
import com.renatizzi.photovideomanager.data.hub.SetupCifsBundle
import com.renatizzi.photovideomanager.data.hub.SetupLanAnnounce
import com.renatizzi.photovideomanager.data.hub.WindowsCatalogSetupScript
import com.renatizzi.photovideomanager.data.hub.WindowsSetupExportResult
import com.renatizzi.photovideomanager.data.hub.WindowsSetupResult
import com.renatizzi.photovideomanager.data.hub.WindowsSetupResultParser
import com.renatizzi.photovideomanager.data.hub.WindowsSetupScriptExporter
import com.renatizzi.photovideomanager.data.hub.WizardDraftStore
import com.renatizzi.photovideomanager.data.storage.NetworkDriveAssistant
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKindLabels
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SetupMode {
    CREATE,
    MODIFY,
}

/** PC/NAS: RAPIDA (default) o GUIDATA. */
enum class ProcedureMode {
    RAPIDA,
    GUIDATA,
}

/** Campi scheda CIFS — ordine form Connection (screenshot PPT V2). */
enum class CifsCopyField {
    NAME,
    STORAGE,
    HOST,
    PORT,
    USERNAME,
    PASSWORD,
    FOLDER,
}

/** Esito prova lettura/scrittura + fascicolo (S6). */
enum class SeatVerificationUi {
    NONE,
    IN_PROGRESS,
    VERIFIED,
    UNREACHABLE,
    FAILED,
}

data class CatalogSeatWizardUiState(
    val step: WizardStep = WizardStep.S0_START,
    val selectedKind: CatalogSeatKind = CatalogSeatKind.LOCAL_DEVICE,
    val setupMode: SetupMode = SetupMode.CREATE,
    val seatConfigured: Boolean = false,
    val seatLabel: String = "",
    /** Nome Catalogo (Avvio procedura). */
    val catalogName: String = CatalogSeat.DEFAULT_CATALOG_NAME,
    /** Modello dispositivo (Avvio procedura). */
    val deviceModel: String = "",
    /** Memoria locale Principale/Secondaria. */
    val memoryLabel: String = CatalogSeat.MEMORY_PRIMARY,
    /** PC/NAS: RAPIDA (default) o GUIDATA. */
    val procedureMode: ProcedureMode = ProcedureMode.RAPIDA,
    val canResumeDraft: Boolean = false,
    val resumeStepLabel: String = "",
    val pendingTreeUri: String? = null,
    val pendingLabel: String = "",
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val completedSeat: CatalogSeat? = null,
    val folderPath: String = WindowsCatalogSetupScript.DEFAULT_FOLDER,
    val shareName: String = WindowsCatalogSetupScript.DEFAULT_SHARE,
    val userName: String = WindowsCatalogSetupScript.DEFAULT_USER,
    val temporaryPassword: String = "",
    val scriptPrepared: Boolean = false,
    val usbCopiedDeclared: Boolean = false,
    val scriptRanDeclared: Boolean = false,
    val downloadsSaved: Boolean = false,
    val usbCopiedTo: String? = null,
    val statusMessage: String? = null,
    val cifsInstalled: Boolean = false,
    val verification: SeatVerificationUi = SeatVerificationUi.NONE,
    /** Dopo Scegli cartella: mostra domanda SI/NO (revisione4). */
    val awaitAccessOutcome: Boolean = false,
    /** Host/User recuperati a «Tutto pronto» (bundle BT / rete). */
    val pcHostIp: String = "",
    val cifsUserQualified: String = "",
    /** Nome PC dal bundle (es. DELLRS), per riepilogo import. */
    val pcComputerName: String = "",
    val setupResultImported: Boolean = false,
    val setupToken: String = "",
    /** Nome file bundle atteso (univoco per sessione). */
    val expectedBundleFileName: String = "",
    /** Ascolto rete in corso (fallback hotspot, non percorso felice). */
    val recoveringPcData: Boolean = false,
    /** Dopo export: UI deve aprire share Bluetooth prima di passare al passo 3. */
    val pendingShareScripts: Boolean = false,
)

/**
 * Passi Wizard — filiera PC allineata al PPT (passi 0–5).
 * Enum legacy restano per bozze vecchie e si rimappano in [normalizeLegacyStep].
 */
enum class WizardStep {
    S0_START,
    /** Passo 0 — Dove tenere il Catalogo (menu a tendina). */
    INTRO,
    W1_CONFIRM,
    /** Domanda contestuale: share già pronta? (F3) — prima dei passi 1–4. */
    PC_SHARE_READY,
    /** Passo 1 — Verifica disponibilità. */
    PC_AVAILABILITY,
    /** Passo 2 — Bluetooth + preparazione/invio CONFIGURA + sup. */
    PC_USB_PREPARE,
    /** Passo 3 — File sul Desktop del PC (dopo ricezione BT). */
    PC_COPY_FILES,
    /** Passo 4 — Avvio script (doppio clic + UAC + Tutto pronto). */
    PC_RUN_SCRIPT,
    /** Passo 4b — Scarica bundle CIFS via Bluetooth (OpenDocument). */
    PC_BT_DOWNLOAD_BUNDLE,
    /** Passo 5 — Scheda CIFS + collegamento Catalogo (revisione4). */
    W2_CIFS,
    W2_PICK,
    W3_GUIDE,
    W4_GUIDE,
    DONE,
    // --- legacy (bozze pre-0.16.36) ---
    @Deprecated("legacy") W2_CHOICE,
    @Deprecated("legacy") W2_ROUTINE_PARAMS,
    @Deprecated("legacy") W2_ROUTINE_READY,
    @Deprecated("legacy") W2_SCRIPT_FIND,
    @Deprecated("legacy") W2_SCRIPT_LAUNCH,
    @Deprecated("legacy") W2_SCRIPT_RESULT,
    @Deprecated("legacy") W2_ROUTINE_CONFIRM,
    @Deprecated("legacy") W2_CIFS_BRIDGE,
    @Deprecated("legacy") W2_SUMMARY,
    @Deprecated("legacy") W2_PREPARE,
    @Deprecated("legacy") W2_CIFS_METHOD,
    @Deprecated("legacy") W2_HELP,
    @Deprecated("legacy") W2_PASSWORD_HELP,
}

class CatalogSeatWizardViewModel(
    private val catalogFacade: CatalogFacade,
    private val appContext: Context,
    private val exporter: WindowsSetupScriptExporter = WindowsSetupScriptExporter(appContext),
    private val draftStore: WizardDraftStore = WizardDraftStore(appContext),
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogSeatWizardUiState())
    val state: StateFlow<CatalogSeatWizardUiState> = _state.asStateFlow()

    private var lastExport: WindowsSetupExportResult? = null
    /** Ascolto LAN già dal Passo 4 (mentre CONFIGURA gira sul PC). */
    private var lanCancel: AtomicBoolean? = null
    private val cachedLan = AtomicReference<SetupLanAnnounce.Payload?>(null)
    private var lanJob: Job? = null

    init {
        refreshCifsInstalled()
        loadEntryState()
    }

    override fun onCleared() {
        stopBackgroundLanListen()
        super.onCleared()
    }

    private fun loadEntryState() {
        val seat = catalogFacade.catalogSeat()
        val draft = draftStore.load()
        val canResume = draft != null &&
            draft.stepName != WizardStep.S0_START.name &&
            draft.stepName != WizardStep.DONE.name
        val resumeStep = if (canResume) {
            runCatching { WizardStep.valueOf(draft!!.stepName) }.getOrNull()
        } else {
            null
        }
        _state.update {
            it.copy(
                step = WizardStep.S0_START,
                seatConfigured = seat.isConfigured,
                seatLabel = seat.displayLabel,
                catalogName = if (seat.isConfigured) {
                    seat.effectiveCatalogName
                } else {
                    CatalogSeat.DEFAULT_CATALOG_NAME
                },
                deviceModel = seat.deviceModel.ifBlank {
                    if (seat.isConfigured) {
                        CatalogSeatKindLabels.shortDevice(seat.kind)
                    } else {
                        suggestedDeviceModel()
                    }
                },
                memoryLabel = seat.memoryLabel.ifBlank { CatalogSeat.MEMORY_PRIMARY },
                procedureMode = ProcedureMode.RAPIDA,
                canResumeDraft = canResume && resumeStep != null,
                resumeStepLabel = resumeStep?.let { s -> humanStepLabel(s) }.orEmpty(),
                selectedKind = if (seat.isConfigured) seat.kind else CatalogSeatKind.LOCAL_DEVICE,
                setupMode = if (seat.isConfigured) SetupMode.MODIFY else SetupMode.CREATE,
                folderPath = draft?.folderPath ?: WindowsCatalogSetupScript.DEFAULT_FOLDER,
                shareName = draft?.shareName ?: WindowsCatalogSetupScript.DEFAULT_SHARE,
                userName = draft?.userName ?: WindowsCatalogSetupScript.DEFAULT_USER,
                scriptPrepared = draft?.scriptPrepared == true,
                usbCopiedDeclared = draft?.usbCopiedDeclared == true,
                scriptRanDeclared = draft?.scriptRanDeclared == true,
            )
        }
    }

    /** Compat: avvia da landing già su S0 (Avvio procedura). */
    fun startCreate() {
        draftStore.clear()
        _state.update {
            it.copy(
                setupMode = SetupMode.CREATE,
                step = WizardStep.S0_START,
                selectedKind = CatalogSeatKind.LOCAL_DEVICE,
                catalogName = CatalogSeat.DEFAULT_CATALOG_NAME,
                deviceModel = suggestedDeviceModel(),
                memoryLabel = CatalogSeat.MEMORY_PRIMARY,
                procedureMode = ProcedureMode.RAPIDA,
                completedSeat = null,
                errorMessage = null,
                statusMessage = null,
                canResumeDraft = false,
                scriptPrepared = false,
                usbCopiedDeclared = false,
                scriptRanDeclared = false,
                temporaryPassword = "",
                folderPath = WindowsCatalogSetupScript.DEFAULT_FOLDER,
                shareName = WindowsCatalogSetupScript.DEFAULT_SHARE,
            )
        }
    }

    fun startModify() {
        val seat = catalogFacade.catalogSeat()
        _state.update {
            it.copy(
                setupMode = SetupMode.MODIFY,
                step = WizardStep.S0_START,
                selectedKind = if (seat.kind != CatalogSeatKind.NONE) seat.kind else CatalogSeatKind.LOCAL_DEVICE,
                catalogName = seat.effectiveCatalogName,
                deviceModel = seat.deviceModel,
                memoryLabel = seat.memoryLabel.ifBlank { CatalogSeat.MEMORY_PRIMARY },
                procedureMode = ProcedureMode.RAPIDA,
                completedSeat = null,
                errorMessage = null,
                statusMessage = null,
                canResumeDraft = false,
            )
        }
    }

    fun updateCatalogName(name: String) {
        _state.update { it.copy(catalogName = name, errorMessage = null) }
    }

    fun updateDeviceModel(model: String) {
        _state.update { it.copy(deviceModel = model, errorMessage = null) }
    }

    fun updateMemoryLabel(label: String) {
        _state.update { it.copy(memoryLabel = label, errorMessage = null) }
    }

    fun selectProcedureMode(mode: ProcedureMode) {
        _state.update { it.copy(procedureMode = mode, errorMessage = null) }
    }

    fun resumeDraft() {
        val draft = draftStore.load() ?: return
        val rawStep = runCatching { WizardStep.valueOf(draft.stepName) }.getOrNull() ?: return
        val step = normalizeLegacyStep(rawStep)
        val kind = runCatching { CatalogSeatKind.valueOf(draft.selectedKindName) }
            .getOrDefault(CatalogSeatKind.PC_NOTEBOOK)
        val mode = if (draft.setupMode == WizardDraftStore.MODE_MODIFY) {
            SetupMode.MODIFY
        } else {
            SetupMode.CREATE
        }
        if (step == WizardStep.W2_CIFS ||
            step == WizardStep.W2_PICK ||
            step == WizardStep.W4_GUIDE
        ) {
            refreshCifsInstalled()
        }
        _state.update {
            it.copy(
                step = step,
                setupMode = mode,
                selectedKind = kind,
                folderPath = draft.folderPath,
                shareName = draft.shareName,
                userName = draft.userName,
                scriptPrepared = draft.scriptPrepared,
                usbCopiedDeclared = draft.usbCopiedDeclared,
                scriptRanDeclared = draft.scriptRanDeclared,
                setupToken = draft.setupToken,
                pcHostIp = draft.pcHostIp,
                cifsUserQualified = draft.cifsUserQualified,
                setupResultImported = draft.pcHostIp.isNotBlank() || draft.cifsUserQualified.isNotBlank(),
                canResumeDraft = false,
                errorMessage = null,
                statusMessage = null,
                completedSeat = null,
            )
        }
        val needLan = draft.setupToken.isNotBlank() &&
            draft.pcHostIp.isBlank() &&
            (
                step == WizardStep.PC_RUN_SCRIPT ||
                    step == WizardStep.W2_CIFS ||
                    step == WizardStep.W2_PICK
                )
        if (needLan) startBackgroundLanListen()
    }

    fun discardDraftAndStayOnS0() {
        draftStore.clear()
        loadEntryState()
        _state.update { it.copy(canResumeDraft = false, errorMessage = null) }
    }

    fun selectKind(kind: CatalogSeatKind) {
        _state.update { it.copy(selectedKind = kind, errorMessage = null) }
        persistDraft()
    }

    fun goIntro() {
        _state.update {
            it.copy(
                step = WizardStep.INTRO,
                errorMessage = null,
                completedSeat = null,
                statusMessage = null,
            )
        }
        persistDraft()
    }

    fun goS0() {
        loadEntryState()
    }

    /**
     * CONTINUA da Avvio procedura: nome + dispositivo + modello → ramo locale/cloud/PC/NAS.
     * Salta INTRO (scelta dispositivo già fatta in landing).
     */
    fun continuaFromAvvio() {
        val current = _state.value
        val name = current.catalogName.trim().ifBlank { CatalogSeat.DEFAULT_CATALOG_NAME }
        val kind = current.selectedKind
        if (kind == CatalogSeatKind.NONE) {
            _state.update { it.copy(errorMessage = "Seleziona un dispositivo") }
            return
        }
        val model = current.deviceModel.trim().ifBlank {
            suggestedDeviceModel()
        }
        // LOCK Setup PC: un percorso; path/share/user fissi MyMedia (non editabili).
        val folderPath = if (kind == CatalogSeatKind.PC_NOTEBOOK) {
            WindowsCatalogSetupScript.DEFAULT_FOLDER
        } else {
            current.folderPath
        }
        val shareName = if (kind == CatalogSeatKind.PC_NOTEBOOK) {
            WindowsCatalogSetupScript.DEFAULT_SHARE
        } else {
            current.shareName
        }
        val userName = if (kind == CatalogSeatKind.PC_NOTEBOOK) {
            WindowsCatalogSetupScript.DEFAULT_USER
        } else {
            current.userName
        }
        if (kind == CatalogSeatKind.PC_NOTEBOOK) {
            val pathErr = WindowsCatalogSetupScript.validateFolderPath(
                folderPath,
                requireDriveC = true,
            )
            if (pathErr != null) {
                _state.update { it.copy(errorMessage = pathErr) }
                return
            }
        }
        val step = when (kind) {
            CatalogSeatKind.LOCAL_DEVICE -> WizardStep.W1_CONFIRM
            // Un percorso: niente RAPIDA/GUIDATA. NAS resta guida breve.
            CatalogSeatKind.PC_NOTEBOOK -> WizardStep.PC_AVAILABILITY
            CatalogSeatKind.NETWORK_NAS -> WizardStep.W4_GUIDE
            CatalogSeatKind.CLOUD_DRIVE -> WizardStep.W3_GUIDE
            CatalogSeatKind.NONE -> WizardStep.S0_START
        }
        draftStore.clear()
        _state.update {
            it.copy(
                catalogName = name,
                deviceModel = model,
                folderPath = folderPath,
                shareName = shareName,
                userName = userName,
                step = step,
                errorMessage = null,
                statusMessage = null,
                completedSeat = null,
                canResumeDraft = false,
                procedureMode = ProcedureMode.GUIDATA,
            )
        }
        persistDraft()
    }

    /** Compat: INTRO legacy (bozze) → stesso routing di Avvio. */
    fun avantiFromIntro() {
        continuaFromAvvio()
    }

    /** RAPIDA: subito a CIFS + pick (PC e NAS). */
    fun onPcShareAlreadyReady() {
        refreshCifsInstalled()
        _state.update {
            it.copy(
                procedureMode = ProcedureMode.RAPIDA,
                step = WizardStep.W2_CIFS,
                recoveringPcData = false,
                errorMessage = null,
                statusMessage = "Inserisci Host (IP) a mano e copia i dati in CIFS, poi scegli la cartella.",
            )
        }
        persistDraft()
    }

    /** GUIDATA: PC → passi 1–4; NAS → guida NAS poi CIFS. */
    fun onPcShareNeedsPrepare() {
        val kind = _state.value.selectedKind
        val step = when (kind) {
            CatalogSeatKind.NETWORK_NAS -> WizardStep.W4_GUIDE
            else -> WizardStep.PC_AVAILABILITY
        }
        _state.update {
            it.copy(
                procedureMode = ProcedureMode.GUIDATA,
                step = step,
                errorMessage = null,
                statusMessage = null,
            )
        }
        persistDraft()
    }

    /** Avvia dalla combo RAPIDA/GUIDATA su PC_SHARE_READY. */
    fun avviaProcedure() {
        when (_state.value.procedureMode) {
            ProcedureMode.RAPIDA -> onPcShareAlreadyReady()
            ProcedureMode.GUIDATA -> onPcShareNeedsPrepare()
        }
    }

    /** Passo 1 → 2. */
    fun confirmAvailabilityChecked() {
        _state.update {
            it.copy(step = WizardStep.PC_USB_PREPARE, errorMessage = null, statusMessage = null)
        }
        persistDraft()
    }

    /** Passo 2 OK → esporta CONFIGURA/sup e apre share Bluetooth; solo dopo si va al passo 3. */
    fun confirmUsbAndPrepareFiles() {
        prepareWindowsScript(shareAfterExport = true)
    }

    fun confirmBtAndPrepareFiles() = confirmUsbAndPrepareFiles()

    /** Chiamato dalla UI dopo aver lanciato (o scartato) il chooser Bluetooth. */
    fun onShareScriptsHandled() {
        _state.update {
            if (!it.scriptPrepared) {
                it.copy(pendingShareScripts = false)
            } else {
                it.copy(
                    pendingShareScripts = false,
                    step = WizardStep.PC_COPY_FILES,
                    statusMessage =
                        "Sei di nuovo in MediaManager. Sul PC i file devono essere sul Desktop: " +
                            "poi spunta e OK.",
                    errorMessage = null,
                )
            }
        }
        persistDraft()
    }

    fun consumePendingShareScripts() {
        _state.update { it.copy(pendingShareScripts = false) }
    }

    /** Passo 3 → 4. Checkpoint: script inviato e sul Desktop. */
    fun confirmCopyChecked() {
        if (!_state.value.scriptPrepared) {
            _state.update {
                it.copy(
                    errorMessage =
                        "Prima invia i file via Bluetooth (passo precedente): CONFIGURA e sup devono essere sul Desktop del PC.",
                )
            }
            return
        }
        _state.update {
            it.copy(
                usbCopiedDeclared = true,
                step = WizardStep.PC_RUN_SCRIPT,
                errorMessage = null,
                statusMessage = null,
            )
        }
        persistDraft()
        // Nessun ascolto LAN qui: il bundle torna al passo 4b via Bluetooth (sequenza sincrona).
    }

    private fun startBackgroundLanListen() {
        val token = _state.value.setupToken.trim()
        if (token.isBlank()) return
        stopBackgroundLanListen()
        val cancel = AtomicBoolean(false)
        lanCancel = cancel
        lanJob = viewModelScope.launch(Dispatchers.IO) {
            // Continua finché non abbiamo IPv4 + utente (non fermarsi al solo User).
            while (isActive && !cancel.get() && cachedLan.get()?.isComplete != true) {
                val payload = runCatching {
                    SetupLanAnnounce.discover(
                        context = appContext,
                        expectedToken = token,
                        cancelled = cancel,
                        discoverMs = 45_000L,
                    )
                }.getOrNull()
                if (payload != null) {
                    val merged = SetupLanAnnounce.mergePayloads(cachedLan.get(), payload)
                    cachedLan.set(merged)
                    withContext(Dispatchers.Main.immediate) {
                        applyLanPayload(merged)
                    }
                    if (merged.isComplete) return@launch
                }
                if (cancel.get()) break
                delay(500)
            }
        }
    }

    private fun stopBackgroundLanListen() {
        lanCancel?.set(true)
        lanCancel = null
        lanJob?.cancel()
        lanJob = null
    }

    fun goStep(step: WizardStep) {
        val target = normalizeLegacyStep(step)
        if (target == WizardStep.W2_CIFS ||
            target == WizardStep.W2_PICK ||
            target == WizardStep.W4_GUIDE
        ) {
            refreshCifsInstalled()
        }
        _state.update { it.copy(step = target, errorMessage = null, statusMessage = null) }
        persistDraft()
    }

    @Suppress("DEPRECATION")
    fun normalizeLegacyStep(step: WizardStep): WizardStep = when (step) {
        WizardStep.W2_CHOICE,
        WizardStep.W2_ROUTINE_PARAMS,
        -> WizardStep.PC_AVAILABILITY
        WizardStep.W2_ROUTINE_READY -> WizardStep.PC_COPY_FILES
        WizardStep.W2_SCRIPT_FIND,
        WizardStep.W2_SCRIPT_LAUNCH,
        WizardStep.W2_SCRIPT_RESULT,
        WizardStep.W2_ROUTINE_CONFIRM,
        -> WizardStep.PC_RUN_SCRIPT
        WizardStep.W2_CIFS_BRIDGE,
        WizardStep.W2_SUMMARY,
        WizardStep.W2_CIFS_METHOD,
        WizardStep.W2_PREPARE,
        WizardStep.W2_HELP,
        WizardStep.W2_PASSWORD_HELP,
        WizardStep.W2_PICK,
        -> WizardStep.W2_CIFS
        else -> step
    }

    private fun persistDraft() {
        val s = _state.value
        // D5: flusso corto locale/cloud (Avvio → cartella → Riepilogo) = pending solo in sessione.
        if (s.step == WizardStep.S0_START ||
            s.step == WizardStep.DONE ||
            s.step == WizardStep.W1_CONFIRM ||
            s.step == WizardStep.W3_GUIDE
        ) {
            if (s.step == WizardStep.DONE ||
                s.step == WizardStep.W1_CONFIRM ||
                s.step == WizardStep.W3_GUIDE
            ) {
                draftStore.clear()
            }
            return
        }
        draftStore.save(
            WizardDraftStore.Draft(
                stepName = s.step.name,
                selectedKindName = s.selectedKind.name,
                folderPath = s.folderPath,
                shareName = s.shareName,
                userName = s.userName,
                scriptPrepared = s.scriptPrepared,
                usbCopiedDeclared = s.usbCopiedDeclared,
                scriptRanDeclared = s.scriptRanDeclared,
                setupMode = when (s.setupMode) {
                    SetupMode.MODIFY -> WizardDraftStore.MODE_MODIFY
                    SetupMode.CREATE -> WizardDraftStore.MODE_CREATE
                },
                setupToken = s.setupToken,
                pcHostIp = s.pcHostIp,
                cifsUserQualified = s.cifsUserQualified,
            ),
        )
    }

    private fun humanStepLabel(step: WizardStep): String =
        WizardStepChrome.subtitle(normalizeLegacyStep(step), CatalogSeatKind.PC_NOTEBOOK).text

    /** Solo password, per copia rapida allo step CIFS (usabilità senza tocco script). */
    fun passwordClipboardText(): String = _state.value.temporaryPassword

    fun fieldClipboardText(field: CifsCopyField): String {
        val s = _state.value
        // Solo valori definitivi: niente placeholder Host se bundle assente.
        return when (field) {
            CifsCopyField.NAME -> s.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
            CifsCopyField.STORAGE -> SetupCifsBundle.STORAGE_VALUE
            CifsCopyField.HOST -> s.pcHostIp.trim()
            CifsCopyField.PORT -> "445"
            CifsCopyField.USERNAME -> s.cifsUserQualified.trim()
            CifsCopyField.PASSWORD -> s.temporaryPassword
            // Folder CIFS: pin sulla share (MyMedia), non path Windows e non vuoto.
            CifsCopyField.FOLDER -> WindowsCatalogSetupScript.cifsFolderPin(
                s.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
            )
        }
    }

    val cifsBundleComplete: Boolean
        get() {
            val s = _state.value
            return SetupLanAnnounce.isIpv4(s.pcHostIp) &&
                s.cifsUserQualified.contains('\\') &&
                s.temporaryPassword.isNotBlank() &&
                s.setupResultImported
        }

    /** Riprova ascolto Wi‑Fi (stesso token della sessione). Sempre invocabile (§D / F2). */
    fun retryRecoverPcData() {
        val token = _state.value.setupToken
        if (token.isBlank()) {
            _state.update {
                it.copy(
                    recoveringPcData = false,
                    errorMessage = "Nessun annuncio PC in corso. Digita l'IP del PC nel campo Host " +
                        "(modificabile), oppure torna indietro e completa «Tutto pronto» sul PC.",
                )
            }
            return
        }
        viewModelScope.launch { recoverPcDataAndOpenCifs(token, alreadyOnCifs = true) }
    }

    /** Host CIFS sempre modificabile a mano (§D) — sblocca Attiva anche senza LAN. */
    fun setPcHostIp(value: String) {
        val trimmed = value.trim()
        _state.update {
            it.copy(
                pcHostIp = trimmed,
                setupResultImported = SetupLanAnnounce.isIpv4(trimmed) &&
                    (it.cifsUserQualified.contains('\\') || it.cifsUserQualified.isNotBlank()),
                errorMessage = null,
            )
        }
        persistDraft()
    }

    /**
     * User CIFS editabile se il recupero automatico non arriva:
     * incolla da «Tutto pronto» il valore `COMPUTERNAME\MyMedia`.
     */
    fun setCifsUserQualified(value: String) {
        val trimmed = value.trim()
        _state.update {
            it.copy(
                cifsUserQualified = trimmed,
                setupResultImported = SetupLanAnnounce.isIpv4(it.pcHostIp.trim()) &&
                    trimmed.isNotBlank(),
                errorMessage = null,
            )
        }
        persistDraft()
    }

    private fun applyLanPayload(payload: SetupLanAnnounce.Payload) {
        val ipv4 = payload.hostForCifs // solo IPv4, mai il solo nome PC
        val user = payload.userQualified.ifBlank {
            if (payload.computerName.isNotBlank()) {
                "${payload.computerName}\\${WindowsCatalogSetupScript.DEFAULT_USER}"
            } else {
                ""
            }
        }
        _state.update {
            val host = when {
                ipv4.isNotBlank() -> ipv4
                SetupLanAnnounce.isIpv4(it.pcHostIp) -> it.pcHostIp
                else -> "" // non usare DESKTOP-xxx come Host CIFS
            }
            val userOut = user.ifBlank { it.cifsUserQualified }
            val passwordOut = payload.password.ifBlank { it.temporaryPassword }
            val complete = SetupLanAnnounce.isIpv4(host) &&
                userOut.isNotBlank() &&
                passwordOut.isNotBlank()
            it.copy(
                cifsUserQualified = userOut,
                pcHostIp = host,
                pcComputerName = payload.computerName.ifBlank { it.pcComputerName },
                temporaryPassword = passwordOut,
                shareName = payload.shareName.ifBlank { it.shareName },
                setupResultImported = complete,
                recoveringPcData = !complete,
                errorMessage = null,
                statusMessage = when {
                    complete ->
                        "Dati CIFS ricevuti (IP, User, Password): usa Copia su tutti i campi."
                    userOut.isNotBlank() && host.isBlank() ->
                        "Utente ricevuto; manca l'IP — inseriscilo a mano nel campo Host oppure reinvia il bundle."
                    host.isNotBlank() && passwordOut.isBlank() ->
                        "Host ricevuto; manca ancora la password nel bundle dal PC…"
                    else -> null
                },
            )
        }
        persistDraft()
    }

    private fun applySetupResult(result: WindowsSetupResult) {
        val user = WindowsSetupResultParser.resolvedUser(result)
        val ip = result.primaryIpv4
        _state.update {
            it.copy(
                cifsUserQualified = user,
                pcHostIp = ip,
                pcComputerName = result.computerName.ifBlank { it.pcComputerName },
                shareName = result.shareName.ifBlank { it.shareName },
                setupResultImported = SetupLanAnnounce.isIpv4(ip) && user.isNotBlank(),
                recoveringPcData = false,
                errorMessage = if (ip.isBlank()) {
                    "IP non presente nel result: inseriscilo a mano nel campo Host."
                } else {
                    null
                },
                statusMessage = null,
            )
        }
        persistDraft()
    }

    fun refreshCifsInstalled() {
        val installed = NetworkDriveAssistant.isHelperInstalled(appContext)
        _state.update { it.copy(cifsInstalled = installed) }
    }

    /**
     * Passo 4 OK → passo 4b (scarica bundle via BT).
     * Non parte il recupero hotspot come percorso felice (asincrono rispetto al PC).
     */
    fun confirmScriptRanOnPc() {
        viewModelScope.launch {
            runCatching { withContext(Dispatchers.IO) { exporter.scrubDownloadsScripts() } }
            refreshCifsInstalled()
            _state.update {
                it.copy(
                    busy = false,
                    recoveringPcData = false,
                    scriptRanDeclared = true,
                    step = WizardStep.PC_BT_DOWNLOAD_BUNDLE,
                    statusMessage = null,
                    errorMessage = null,
                )
            }
            persistDraft()
        }
    }

    /** Import bundle JSON ricevuto via Bluetooth (picker file sul telefono). */
    fun importCifsBundleFromUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, errorMessage = null, statusMessage = null) }
            val token = _state.value.setupToken
            val expectedName = _state.value.expectedBundleFileName
                .ifBlank { SetupCifsBundle.bundleFileNameForToken(token) }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val raw = appContext.contentResolver.openInputStream(uri)?.use { input ->
                        input.bufferedReader(Charsets.UTF_8).readText()
                    } ?: error("Impossibile leggere il file")
                    if (SetupCifsBundle.looksLikeSetupResultNotBundle(raw)) {
                        error(
                            "Hai selezionato MediaManager_setup_result.json (senza password). " +
                                "Dal Desktop del PC invia $expectedName via Bluetooth e seleziona quello.",
                        )
                    }
                    val bundle = SetupCifsBundle.parse(raw, expectedToken = token.ifBlank { null })
                        ?: error(
                            "File non riconosciuto come bundle MediaManager. " +
                                "Serve $expectedName (non setup_result).",
                        )
                    if (!bundle.isComplete) {
                        val missing = buildList {
                            if (!SetupLanAnnounce.isIpv4(bundle.hostIp)) add("Host IPv4")
                            if (bundle.userQualified.isBlank()) add("User")
                            if (bundle.password.isBlank()) add("Password")
                        }.joinToString(", ")
                        error(
                            "Bundle incompleto (manca: $missing). " +
                                "Dal Desktop del PC invia di nuovo $expectedName via Bluetooth.",
                        )
                    }
                    bundle
                }
            }
            result.fold(
                onSuccess = { bundle ->
                    cachedLan.set(
                        SetupLanAnnounce.mergePayloads(cachedLan.get(), bundle.toLanPayload()),
                    )
                    applyLanPayload(bundle.toLanPayload())
                    stopBackgroundLanListen()
                    _state.update {
                        it.copy(
                            busy = false,
                            recoveringPcData = false,
                            setupResultImported = true,
                            pcComputerName = bundle.computerName.ifBlank { it.pcComputerName },
                            // Resta su 4b per mostrare l'esito; OK porta a CIFS.
                            step = WizardStep.PC_BT_DOWNLOAD_BUNDLE,
                            statusMessage =
                                "Import riuscito: Host, User e Password pronti. Tocca OK per CIFS.",
                            errorMessage = null,
                        )
                    }
                    persistDraft()
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            busy = false,
                            setupResultImported = false,
                            errorMessage = e.message ?: "Import bundle non riuscito",
                        )
                    }
                },
            )
        }
    }

    private suspend fun recoverPcDataAndOpenCifs(token: String, alreadyOnCifs: Boolean) {
        refreshCifsInstalled()
        _state.update {
            it.copy(
                busy = true,
                recoveringPcData = true,
                scriptRanDeclared = true,
                step = if (alreadyOnCifs) it.step else WizardStep.W2_CIFS,
                statusMessage = null,
                errorMessage = null,
            )
        }
        persistDraft()

        // Gia completo (QR/handoff o fast-path LAN) mentre eri al Passo 4?
        val early = cachedLan.get()
        if (early?.isComplete == true) {
            applyLanPayload(early)
            stopBackgroundLanListen()
            _state.update { it.copy(busy = false, recoveringPcData = false) }
            persistDraft()
            return
        }

        // Primario: announce bundle completo su hotspot telefono (topologia controllata).
        val cancelled = AtomicBoolean(false)
        val lan = if (token.isNotBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    SetupLanAnnounce.discover(
                        appContext,
                        token,
                        cancelled,
                        discoverMs = SetupLanAnnounce.DISCOVER_MS,
                    )
                }.getOrNull()
            }
        } else {
            null
        }

        if (lan != null) {
            val merged = SetupLanAnnounce.mergePayloads(cachedLan.get(), lan)
            cachedLan.set(merged)
            applyLanPayload(merged)
            if (merged.isComplete) {
                stopBackgroundLanListen()
                _state.update { it.copy(busy = false, recoveringPcData = false) }
                persistDraft()
                return
            }
        }

        // Riserva silenziosa: JSON in Download (senza chiedere copia al cliente).
        val fileResult = withContext(Dispatchers.IO) {
            runCatching { exporter.findSetupResultInDownloads() }.getOrNull()
        }
        if (fileResult != null && fileResult.isCompleted) {
            applySetupResult(fileResult)
            val ip = fileResult.primaryIpv4
            if (SetupLanAnnounce.isIpv4(ip)) {
                stopBackgroundLanListen()
                _state.update { it.copy(busy = false, recoveringPcData = false) }
                persistDraft()
                return
            }
        }

        // Piano B: bundle USB scritto dallo script sullo stesso volume di CONFIGURA.
        val usbBundle = withContext(Dispatchers.IO) {
            runCatching { exporter.findCifsBundle(token) }.getOrNull()
        }
        if (usbBundle != null && usbBundle.isComplete) {
            cachedLan.set(SetupLanAnnounce.mergePayloads(cachedLan.get(), usbBundle.toLanPayload()))
            applyLanPayload(usbBundle.toLanPayload())
            stopBackgroundLanListen()
            _state.update { it.copy(busy = false, recoveringPcData = false) }
            persistDraft()
            return
        }

        if (token.isNotBlank() && lanJob?.isActive != true) {
            startBackgroundLanListen()
        }
        _state.update {
            it.copy(
                busy = false,
                recoveringPcData = true,
                setupResultImported = false,
                errorMessage =
                    "Bundle CIFS non ricevuto. Verifica: 1) hotspot telefono ON e PC collegato a quell'SSID; " +
                        "2) finestra «Tutto pronto» ancora aperta. Oppure reinserisci la chiavetta USB " +
                        "da cui hai lanciato CONFIGURA e tocca «Leggi bundle dalla chiavetta».",
            )
        }
        persistDraft()
    }

    /** Piano B: rilegge [SetupCifsBundle.BUNDLE_FILE_NAME] da USB/Download. */
    fun importCifsBundleFromRemovable() {
        viewModelScope.launch {
            val token = _state.value.setupToken
            _state.update { it.copy(busy = true, errorMessage = null) }
            val bundle = withContext(Dispatchers.IO) {
                runCatching { exporter.findCifsBundle(token) }.getOrNull()
            }
            if (bundle == null || !bundle.isComplete) {
                _state.update {
                    it.copy(
                        busy = false,
                        errorMessage =
                            "Bundle non trovato. Reinserisci la chiavetta usata con CONFIGURA " +
                                "(file ${SetupCifsBundle.BUNDLE_FILE_NAME}) oppure ripeti con hotspot.",
                    )
                }
                return@launch
            }
            cachedLan.set(SetupLanAnnounce.mergePayloads(cachedLan.get(), bundle.toLanPayload()))
            applyLanPayload(bundle.toLanPayload())
            stopBackgroundLanListen()
            _state.update { it.copy(busy = false, recoveringPcData = false) }
            persistDraft()
        }
    }

    /** @deprecated QR bocciato; tenuto solo per bozze vecchie. Preferire bundle hotspot/USB. */
    fun applySetupHandoffRaw(raw: String) {
        val token = _state.value.setupToken
        val fromBundle = SetupCifsBundle.parse(raw, expectedToken = token)
        if (fromBundle != null && fromBundle.isComplete) {
            cachedLan.set(SetupLanAnnounce.mergePayloads(cachedLan.get(), fromBundle.toLanPayload()))
            applyLanPayload(fromBundle.toLanPayload())
            stopBackgroundLanListen()
            _state.update {
                it.copy(
                    recoveringPcData = false,
                    setupResultImported = true,
                    errorMessage = null,
                    statusMessage = "Bundle CIFS applicato (IP, User, Password).",
                )
            }
            persistDraft()
            return
        }
        _state.update {
            it.copy(
                errorMessage =
                    "Serve il bundle completo con password (hotspot o file USB), " +
                        "non il vecchio codice QR senza password.",
            )
        }
    }

    /** @deprecated bozze: usa [confirmCopyChecked]. */
    fun markUsbCopiedAndContinue() = confirmCopyChecked()

    fun continueToCifsForm() {
        refreshCifsInstalled()
        _state.update {
            it.copy(step = WizardStep.W2_CIFS, errorMessage = null, statusMessage = null)
        }
        persistDraft()
    }

    fun updateFolderPath(value: String) {
        _state.update { it.copy(folderPath = value, errorMessage = null, scriptPrepared = false) }
        persistDraft()
    }

    fun updateShareName(value: String) {
        _state.update { it.copy(shareName = value, errorMessage = null, scriptPrepared = false) }
        persistDraft()
    }

    fun prepareWindowsScript(shareAfterExport: Boolean = false) {
        viewModelScope.launch {
            val current = _state.value
            val pathErr = WindowsCatalogSetupScript.validateFolderPath(current.folderPath)
            val shareErr = WindowsCatalogSetupScript.validateShareName(current.shareName)
            val userErr = WindowsCatalogSetupScript.validateUserName(current.userName)
            if (pathErr != null || shareErr != null || userErr != null) {
                _state.update {
                    it.copy(errorMessage = pathErr ?: shareErr ?: userErr)
                }
                return@launch
            }

            _state.update { it.copy(busy = true, errorMessage = null, statusMessage = null) }
            val password = current.temporaryPassword.ifBlank {
                WindowsCatalogSetupScript.generatePassword()
            }
            val setupToken = current.setupToken.ifBlank {
                UUID.randomUUID().toString().replace("-", "")
            }
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val pkg = WindowsCatalogSetupScript.build(
                        WindowsCatalogSetupScript.Params(
                            folderPath = current.folderPath.trim(),
                            shareName = current.shareName.trim(),
                            userName = current.userName.trim(),
                            temporaryPassword = password,
                            setupToken = setupToken,
                        ),
                    )
                    exporter.export(pkg)
                }
            }
            result.fold(
                onSuccess = { export ->
                    lastExport = export
                    val bundleName = SetupCifsBundle.bundleFileNameForToken(setupToken)
                    // Sequenza sincrona: prima share BT (resta al passo 2), poi passo 2b.
                    _state.update {
                        it.copy(
                            busy = false,
                            temporaryPassword = password,
                            setupToken = setupToken,
                            expectedBundleFileName = bundleName,
                            scriptPrepared = true,
                            downloadsSaved = export.downloadsSaved,
                            usbCopiedTo = export.usbCopiedTo,
                            setupResultImported = false,
                            pendingShareScripts = shareAfterExport && export.shareUris.isNotEmpty(),
                            statusMessage = if (shareAfterExport) {
                                "TELEFONO: nel menu Condividi scegli Bluetooth e il PC. " +
                                    "Sul PC deve essere già aperto «Ricevi file»."
                            } else {
                                null
                            },
                            step = if (shareAfterExport) {
                                WizardStep.PC_USB_PREPARE
                            } else {
                                WizardStep.PC_COPY_FILES
                            },
                        )
                    }
                    persistDraft()
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            busy = false,
                            errorMessage = e.message ?: "Preparazione script non riuscita",
                        )
                    }
                },
            )
        }
    }

    fun shareScriptIntent(): Intent? {
        val export = lastExport ?: return null
        return exporter.shareIntent(export)
    }

    fun credentialsClipboardText(): String {
        val s = _state.value
        val share = s.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
        val user = s.cifsUserQualified.trim().ifBlank {
            WindowsCatalogSetupScript.cifsUserCopyText(s.userName)
        }
        if (!cifsBundleComplete) return ""
        val host = s.pcHostIp.trim()
        return buildString {
            append("Name: $share\n")
            append("Storage: ${SetupCifsBundle.STORAGE_VALUE}\n")
            append("Domain: (lascia vuoto)\n")
            append("Host: $host\n")
            append("Port: 445\n")
            append("Enable DFS: (lascia deselezionato)\n")
            append("User: $user\n")
            append("Password: ${s.temporaryPassword}\n")
            append("Anonymous: (lascia deselezionato)\n")
            append("Folder: ${WindowsCatalogSetupScript.cifsFolderPin(share)}")
        }
    }

    /** Solo memorizza la cartella scelta; il salvataggio avviene con [confirmPendingSeat] (Salva). */
    fun onFolderPicked(uri: String, label: String) {
        _state.update {
            it.copy(
                pendingTreeUri = uri,
                pendingLabel = label,
                errorMessage = null,
                statusMessage = null,
                verification = SeatVerificationUi.NONE,
                awaitAccessOutcome = false,
            )
        }
    }

    /**
     * Locale senza URI: non più usato come conferma diretta.
     * La UI apre il selettore cartelle; resta come fallback etichetta.
     */
    fun confirmLocalSeat() {
        confirmPendingSeat()
    }

    /** TO-BE: Salva — conferma la cartella in pending (locale / cloud / CIFS). */
    fun confirmPendingSeat() {
        val s = _state.value
        val uri = s.pendingTreeUri
        // Filiera locale inclusa: serve sempre una cartella scelta (es. Documenti/Catalogo).
        if (uri.isNullOrBlank()) {
            _state.update { it.copy(errorMessage = "Scegli prima la cartella del Catalogo") }
            return
        }
        confirmSeat(
            kind = s.selectedKind,
            treeUri = uri,
            label = s.pendingLabel.ifBlank {
                s.catalogName.ifBlank { CatalogSeat.DEFAULT_CATALOG_NAME }
            },
        )
    }

    /** Compat: stesso di [confirmPendingSeat]. */
    fun retryPendingFolderVerification() = confirmPendingSeat()

    private fun confirmSeat(kind: CatalogSeatKind, treeUri: String?, label: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    busy = true,
                    errorMessage = null,
                    statusMessage = null,
                    verification = SeatVerificationUi.IN_PROGRESS,
                    awaitAccessOutcome = false,
                )
            }
            val ui = _state.value
            val seat = CatalogSeat(
                kind = kind,
                treeUri = treeUri,
                displayLabel = label,
                catalogName = ui.catalogName.ifBlank { CatalogSeat.DEFAULT_CATALOG_NAME },
                deviceModel = ui.deviceModel.ifBlank { CatalogSeat.DEFAULT_DEVICE_MODEL },
                memoryLabel = if (kind == CatalogSeatKind.LOCAL_DEVICE) {
                    ui.memoryLabel.ifBlank { CatalogSeat.MEMORY_PRIMARY }
                } else {
                    ""
                },
            )
            val result = catalogFacade.configureCatalogSeat(seat)
            if (result.isSuccess) {
                draftStore.clear()
                _state.update {
                    it.copy(
                        busy = false,
                        completedSeat = result.getOrNull(),
                        step = WizardStep.DONE,
                        seatConfigured = true,
                        seatLabel = label,
                        verification = SeatVerificationUi.VERIFIED,
                        awaitAccessOutcome = false,
                        errorMessage = null,
                        statusMessage = null,
                    )
                }
            } else {
                val raw = result.exceptionOrNull()?.message.orEmpty()
                val unreachable = isUnreachableSeatError(raw)
                _state.update {
                    it.copy(
                        busy = false,
                        verification = if (unreachable) {
                            SeatVerificationUi.UNREACHABLE
                        } else {
                            SeatVerificationUi.FAILED
                        },
                        awaitAccessOutcome = false,
                        errorMessage = friendlySeatError(raw, unreachable),
                        step = when (kind) {
                            CatalogSeatKind.LOCAL_DEVICE -> WizardStep.W1_CONFIRM
                            CatalogSeatKind.CLOUD_DRIVE -> WizardStep.W3_GUIDE
                            else -> WizardStep.W2_CIFS
                        },
                    )
                }
                persistDraft()
            }
        }
    }

    /** Compat legacy: porta all’esito DONE. */
    fun acceptAccessYes() {
        _state.update {
            it.copy(
                step = WizardStep.DONE,
                awaitAccessOutcome = false,
                errorMessage = null,
                statusMessage = null,
            )
        }
    }

    fun acceptAccessNo() {
        _state.update {
            it.copy(
                awaitAccessOutcome = false,
                statusMessage = null,
                errorMessage = null,
            )
        }
    }

    private fun isUnreachableSeatError(message: String): Boolean {
        val m = message.lowercase()
        return m.contains("fascicolo") ||
            m.contains("scrittura") ||
            m.contains("non disponibile") ||
            m.contains("permission") ||
            m.contains("eacces") ||
            m.contains("enoent") ||
            m.contains("not found") ||
            m.contains("elenco delle share") ||
            m.contains("folder=") ||
            m.contains("read only") ||
            m.contains("non scrivibile")
    }

    private fun friendlySeatError(raw: String, unreachable: Boolean): String {
        val share = _state.value.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
        return when {
            raw.contains("elenco delle share", ignoreCase = true) -> raw
            unreachable -> PcCatalogSeatGate.writableProbeFailureHint(share)
            else -> raw.ifBlank {
                "Verifica della Posizione del Catalogo non riuscita. Riprova o torna a CIFS."
            }
        }
    }

    /** Nota bis: default modello = nome dispositivo di sistema se leggibile. */
    private fun suggestedDeviceModel(): String =
        SafPathLabels.deviceLabel(appContext).trim()
            .ifBlank { CatalogSeat.DEFAULT_DEVICE_MODEL }

    companion object {
        fun factory(catalogFacade: CatalogFacade, appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatalogSeatWizardViewModel(catalogFacade, appContext.applicationContext) as T
            }
    }
}
