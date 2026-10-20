package com.renatizzi.photovideomanager.ui.hub

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.data.hub.WindowsCatalogSetupScript
import com.renatizzi.photovideomanager.data.hub.WindowsSetupExportResult
import com.renatizzi.photovideomanager.data.hub.WindowsSetupScriptExporter
import com.renatizzi.photovideomanager.data.hub.WizardDraftStore
import com.renatizzi.photovideomanager.data.storage.NetworkDriveAssistant
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class SetupMode {
    CREATE,
    MODIFY,
}

/** Campi scheda CIFS — ordine form Connection (screenshot PPT V2). */
enum class CifsCopyField {
    NAME,
    STORAGE,
    HOST,
    PORT,
    USERNAME,
    PASSWORD,
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
    val selectedKind: CatalogSeatKind = CatalogSeatKind.PC_NOTEBOOK,
    val setupMode: SetupMode = SetupMode.CREATE,
    val seatConfigured: Boolean = false,
    val seatLabel: String = "",
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
    /** Passo 1 — Verifica disponibilità. */
    PC_AVAILABILITY,
    /** Passo 2 — USB / collegamento; OK esporta CONFIGURA + sup. */
    PC_USB_PREPARE,
    /** Passo 3 — Copia manuale sul Desktop. */
    PC_COPY_FILES,
    /** Passo 4 — Avvio script (doppio clic + UAC + Tutto pronto). */
    PC_RUN_SCRIPT,
    /** Passo 5 — Scheda CIFS: Copia/Conferma in riga + checkbox. */
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

    init {
        refreshCifsInstalled()
        loadEntryState()
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
                canResumeDraft = canResume && resumeStep != null,
                resumeStepLabel = resumeStep?.let { s -> humanStepLabel(s) }.orEmpty(),
                selectedKind = if (seat.isConfigured) seat.kind else CatalogSeatKind.LOCAL_DEVICE,
                folderPath = draft?.folderPath ?: WindowsCatalogSetupScript.DEFAULT_FOLDER,
                shareName = draft?.shareName ?: WindowsCatalogSetupScript.DEFAULT_SHARE,
                userName = draft?.userName ?: WindowsCatalogSetupScript.DEFAULT_USER,
                scriptPrepared = draft?.scriptPrepared == true,
                usbCopiedDeclared = draft?.usbCopiedDeclared == true,
                scriptRanDeclared = draft?.scriptRanDeclared == true,
            )
        }
    }

    fun startCreate() {
        draftStore.clear()
        _state.update {
            it.copy(
                setupMode = SetupMode.CREATE,
                step = WizardStep.INTRO,
                selectedKind = CatalogSeatKind.LOCAL_DEVICE,
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
        persistDraft()
    }

    fun startModify() {
        val seat = catalogFacade.catalogSeat()
        _state.update {
            it.copy(
                setupMode = SetupMode.MODIFY,
                step = WizardStep.INTRO,
                selectedKind = if (seat.kind != CatalogSeatKind.NONE) seat.kind else CatalogSeatKind.PC_NOTEBOOK,
                completedSeat = null,
                errorMessage = null,
                statusMessage = null,
                canResumeDraft = false,
            )
        }
        persistDraft()
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
                canResumeDraft = false,
                errorMessage = null,
                statusMessage = null,
                completedSeat = null,
            )
        }
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

    fun avantiFromIntro() {
        val current = _state.value
        val kind = current.selectedKind
        if (kind == CatalogSeatKind.PC_NOTEBOOK) {
            val pathErr = WindowsCatalogSetupScript.validateFolderPath(
                current.folderPath,
                requireDriveC = true,
            )
            if (pathErr != null) {
                _state.update { it.copy(errorMessage = pathErr) }
                return
            }
        }
        val step = when (kind) {
            CatalogSeatKind.LOCAL_DEVICE -> WizardStep.W1_CONFIRM
            CatalogSeatKind.PC_NOTEBOOK -> WizardStep.PC_AVAILABILITY
            CatalogSeatKind.CLOUD_DRIVE -> WizardStep.W3_GUIDE
            CatalogSeatKind.NETWORK_NAS -> WizardStep.W4_GUIDE
            CatalogSeatKind.NONE -> WizardStep.INTRO
        }
        _state.update { it.copy(step = step, errorMessage = null, statusMessage = null) }
        persistDraft()
    }

    /** Passo 1 → 2. */
    fun confirmAvailabilityChecked() {
        _state.update {
            it.copy(step = WizardStep.PC_USB_PREPARE, errorMessage = null, statusMessage = null)
        }
        persistDraft()
    }

    /** Passo 2 OK → esporta CONFIGURA/sup in Download, poi passo 3. */
    fun confirmUsbAndPrepareFiles() {
        prepareWindowsScript()
    }

    /** Passo 3 → 4. */
    fun confirmCopyChecked() {
        _state.update {
            it.copy(
                usbCopiedDeclared = true,
                step = WizardStep.PC_RUN_SCRIPT,
                errorMessage = null,
                statusMessage = null,
            )
        }
        persistDraft()
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
        -> WizardStep.W2_CIFS
        else -> step
    }

    private fun persistDraft() {
        val s = _state.value
        if (s.step == WizardStep.S0_START || s.step == WizardStep.DONE) {
            if (s.step == WizardStep.DONE) draftStore.clear()
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
            ),
        )
    }

    private fun humanStepLabel(step: WizardStep): String =
        WizardStepChrome.subtitle(normalizeLegacyStep(step), CatalogSeatKind.PC_NOTEBOOK).text

    /** Solo password, per copia rapida allo step CIFS (usabilità senza tocco script). */
    fun passwordClipboardText(): String = _state.value.temporaryPassword

    fun fieldClipboardText(field: CifsCopyField): String {
        val s = _state.value
        return when (field) {
            CifsCopyField.NAME -> s.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
            CifsCopyField.STORAGE -> "SMB2,3 (jCIFS NG)"
            CifsCopyField.HOST -> "" // IP solo dal PC; non inventare
            CifsCopyField.PORT -> "445"
            CifsCopyField.USERNAME -> s.userName.ifBlank { WindowsCatalogSetupScript.DEFAULT_USER }
            CifsCopyField.PASSWORD -> s.temporaryPassword
        }
    }

    fun refreshCifsInstalled() {
        val installed = NetworkDriveAssistant.isHelperInstalled(appContext)
        _state.update { it.copy(cifsInstalled = installed) }
    }

    /** Passo 4 OK: Tutto pronto → scrub Download → passo 5 CIFS. */
    fun confirmScriptRanOnPc() {
        viewModelScope.launch(Dispatchers.IO) {
            val n = runCatching { exporter.scrubDownloadsScripts() }.getOrDefault(0)
            refreshCifsInstalled()
            _state.update {
                it.copy(
                    step = WizardStep.W2_CIFS,
                    scriptRanDeclared = true,
                    statusMessage = if (n > 0) {
                        "File di supporto rimosso da Download sul telefono (conteneva la password)."
                    } else {
                        null
                    },
                    errorMessage = null,
                )
            }
            persistDraft()
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

    fun prepareWindowsScript() {
        viewModelScope.launch {
            val current = _state.value
            val pathErr = WindowsCatalogSetupScript.validateFolderPath(
                current.folderPath,
                requireDriveC = true,
            )
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
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val pkg = WindowsCatalogSetupScript.build(
                        WindowsCatalogSetupScript.Params(
                            folderPath = current.folderPath.trim(),
                            shareName = current.shareName.trim(),
                            userName = current.userName.trim(),
                            temporaryPassword = password,
                        ),
                    )
                    exporter.export(pkg)
                }
            }
            result.fold(
                onSuccess = { export ->
                    lastExport = export
                    val status = buildString {
                        if (export.downloadsSaved) {
                            append("File CONFIGURA e sup salvati in Download. ")
                        }
                        if (export.usbCopiedTo != null) {
                            append("Copiati anche su USB («${export.usbCopiedTo}»). ")
                        } else {
                            append("Ora copiali sul Desktop del PC come da istruzioni. ")
                        }
                    }.trim()
                    _state.update {
                        it.copy(
                            busy = false,
                            temporaryPassword = password,
                            scriptPrepared = true,
                            downloadsSaved = export.downloadsSaved,
                            usbCopiedTo = export.usbCopiedTo,
                            statusMessage = status,
                            step = WizardStep.PC_COPY_FILES,
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
        val user = s.userName.ifBlank { WindowsCatalogSetupScript.DEFAULT_USER }
        return buildString {
            append("Name: $share\n")
            append("Storage: SMB2,3 (jCIFS NG)\n")
            append("Domain: (lascia vuoto)\n")
            append("Host: (IP del PC dalla finestra Tutto pronto)\n")
            append("Port: 445\n")
            append("Enable DFS: (lascia deselezionato)\n")
            append("User: $user\n")
            append("Password: ${s.temporaryPassword}")
        }
    }

    fun onFolderPicked(uri: String, label: String) {
        _state.update {
            it.copy(
                pendingTreeUri = uri,
                pendingLabel = label,
                errorMessage = null,
                verification = SeatVerificationUi.NONE,
            )
        }
        confirmSeat(
            kind = _state.value.selectedKind,
            treeUri = uri,
            label = label,
        )
    }

    fun confirmLocalSeat() {
        confirmSeat(
            kind = CatalogSeatKind.LOCAL_DEVICE,
            treeUri = null,
            label = "Questo dispositivo",
        )
    }

    /** Riprova S6 con l'ultima cartella scelta, senza azzerare S1–S2. */
    fun retryPendingFolderVerification() {
        val s = _state.value
        val uri = s.pendingTreeUri ?: return
        confirmSeat(
            kind = s.selectedKind,
            treeUri = uri,
            label = s.pendingLabel.ifBlank { "Cartella Catalogo" },
        )
    }

    private fun confirmSeat(kind: CatalogSeatKind, treeUri: String?, label: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    busy = true,
                    errorMessage = null,
                    statusMessage = null,
                    verification = SeatVerificationUi.IN_PROGRESS,
                    step = if (kind == CatalogSeatKind.LOCAL_DEVICE) it.step else WizardStep.W2_PICK,
                )
            }
            val seat = CatalogSeat(
                kind = kind,
                treeUri = treeUri,
                displayLabel = label,
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
                        errorMessage = friendlySeatError(raw, unreachable),
                        // Resta su S6 (o W1); non azzera path/script/dichiarazioni.
                        step = if (kind == CatalogSeatKind.LOCAL_DEVICE) {
                            WizardStep.W1_CONFIRM
                        } else {
                            WizardStep.W2_PICK
                        },
                    )
                }
                persistDraft()
            }
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
            m.contains("not found")
    }

    private fun friendlySeatError(raw: String, unreachable: Boolean): String {
        return if (unreachable) {
            "Posizione del Catalogo non raggiungibile o non scrivibile. Controlla CIFS, Wi‑Fi e che il PC non sia in sospensione, poi riprova."
        } else {
            raw.ifBlank { "Verifica della Posizione del Catalogo non riuscita. Riprova o torna a CIFS." }
        }
    }

    companion object {
        fun factory(catalogFacade: CatalogFacade, appContext: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatalogSeatWizardViewModel(catalogFacade, appContext.applicationContext) as T
            }
    }
}
