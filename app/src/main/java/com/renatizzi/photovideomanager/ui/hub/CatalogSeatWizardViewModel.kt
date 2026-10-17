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
)

/**
 * Passi Wizard. S0 = avvio; INTRO = S1 scelta dispositivo; poi rami W1–W4 fino a DONE (S7).
 * T1: scheletro S0 + ripresa; percorso PC completo nelle tranche successive.
 */
enum class WizardStep {
    S0_START,
    INTRO,
    W1_CONFIRM,
    W2_CHOICE,
    W2_ROUTINE_PARAMS,
    W2_ROUTINE_READY,
    W2_ROUTINE_CONFIRM,
    W2_PREPARE,
    W2_CIFS,
    W2_HELP,
    W2_PICK,
    W2_PASSWORD_HELP,
    W3_GUIDE,
    W4_GUIDE,
    DONE,
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
                selectedKind = if (seat.isConfigured) seat.kind else CatalogSeatKind.PC_NOTEBOOK,
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
                selectedKind = CatalogSeatKind.PC_NOTEBOOK,
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
        val step = runCatching { WizardStep.valueOf(draft.stepName) }.getOrNull() ?: return
        val kind = runCatching { CatalogSeatKind.valueOf(draft.selectedKindName) }
            .getOrDefault(CatalogSeatKind.PC_NOTEBOOK)
        val mode = if (draft.setupMode == WizardDraftStore.MODE_MODIFY) {
            SetupMode.MODIFY
        } else {
            SetupMode.CREATE
        }
        if (step == WizardStep.W2_CIFS || step == WizardStep.W2_PICK || step == WizardStep.W4_GUIDE) {
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
        val kind = _state.value.selectedKind
        // Fase corrente: ramo PC va diretto a S2 (path su C:) + preparazione script.
        val step = when (kind) {
            CatalogSeatKind.LOCAL_DEVICE -> WizardStep.W1_CONFIRM
            CatalogSeatKind.PC_NOTEBOOK -> WizardStep.W2_ROUTINE_PARAMS
            CatalogSeatKind.CLOUD_DRIVE -> WizardStep.W3_GUIDE
            CatalogSeatKind.NETWORK_NAS -> WizardStep.W4_GUIDE
            CatalogSeatKind.NONE -> WizardStep.INTRO
        }
        _state.update { it.copy(step = step, errorMessage = null) }
        persistDraft()
    }

    fun goStep(step: WizardStep) {
        if (step == WizardStep.W2_CIFS || step == WizardStep.W2_PICK || step == WizardStep.W4_GUIDE) {
            refreshCifsInstalled()
        }
        _state.update { it.copy(step = step, errorMessage = null, statusMessage = null) }
        persistDraft()
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

    private fun humanStepLabel(step: WizardStep): String = when (step) {
        WizardStep.S0_START -> "Avvio"
        WizardStep.INTRO -> "Scelta del dispositivo"
        WizardStep.W1_CONFIRM -> "Sede su questo apparecchio"
        WizardStep.W2_CHOICE,
        WizardStep.W2_ROUTINE_PARAMS,
        WizardStep.W2_ROUTINE_READY,
        WizardStep.W2_ROUTINE_CONFIRM,
        WizardStep.W2_PREPARE,
        -> "Preparazione sul PC"
        WizardStep.W2_CIFS,
        WizardStep.W2_HELP,
        WizardStep.W2_PASSWORD_HELP,
        -> "Collegamento CIFS"
        WizardStep.W2_PICK -> "Scelta cartella"
        WizardStep.W3_GUIDE -> "Google Drive"
        WizardStep.W4_GUIDE -> "NAS / rete"
        WizardStep.DONE -> "Conferma"
    }

    fun refreshCifsInstalled() {
        val installed = NetworkDriveAssistant.isHelperInstalled(appContext)
        _state.update { it.copy(cifsInstalled = installed) }
    }

    /** After PC script success: scrub password file and open CIFS step. */
    fun confirmScriptRanOnPc() {
        viewModelScope.launch(Dispatchers.IO) {
            val n = runCatching { exporter.scrubDownloadsScripts() }.getOrDefault(0)
            val installed = NetworkDriveAssistant.isHelperInstalled(appContext)
            _state.update {
                it.copy(
                    step = WizardStep.W2_CIFS,
                    cifsInstalled = installed,
                    scriptRanDeclared = true,
                    statusMessage = if (n > 0) {
                        "File di supporto rimosso da Download sul telefono (conteneva la password)."
                    } else {
                        "Se in Download resta ancora _non_aprire_supporto_setup, eliminalo: contiene la password."
                    },
                    errorMessage = null,
                )
            }
            persistDraft()
        }
    }

    fun markUsbCopied() {
        _state.update { it.copy(usbCopiedDeclared = true, errorMessage = null) }
        persistDraft()
    }

    /** Manual share path (no script): go to CIFS with whatever credentials known. */
    fun continueToCifsFromManualPrepare() {
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
                        if (export.downloadsSaved) append("Salvato in Download. ")
                        if (export.usbCopiedTo != null) {
                            append("Copiato anche su USB («${export.usbCopiedTo}»). ")
                        } else {
                            append(
                                "Collega il cavo USB e sul telefono passa da Solo ricarica " +
                                    "a Trasferimento file, poi copia i due file sul Desktop del PC. ",
                            )
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
                            step = WizardStep.W2_ROUTINE_READY,
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
        return buildString {
            append("Host: IP-del-PC (o nome PC)\n")
            append("Porta: 445\n")
            append("Utente: ${s.userName}\n")
            append("Password: ${s.temporaryPassword}\n")
            append("Cartella/condivisione: ${s.shareName}\n")
            append("Storage in CIFS: SMB2,3 (jCIFS NG)")
        }
    }

    fun onFolderPicked(uri: String, label: String) {
        _state.update { it.copy(pendingTreeUri = uri, pendingLabel = label, errorMessage = null) }
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

    private fun confirmSeat(kind: CatalogSeatKind, treeUri: String?, label: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, errorMessage = null) }
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
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Configurazione sede non riuscita",
                    )
                }
            }
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
