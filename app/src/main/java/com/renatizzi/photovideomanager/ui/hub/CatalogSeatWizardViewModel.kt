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
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CatalogSeatWizardUiState(
    val step: WizardStep = WizardStep.INTRO,
    val selectedKind: CatalogSeatKind = CatalogSeatKind.LOCAL_DEVICE,
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
    val downloadsSaved: Boolean = false,
    val usbCopiedTo: String? = null,
    val statusMessage: String? = null,
)

enum class WizardStep {
    INTRO,
    W1_CONFIRM,
    W2_CHOICE,
    W2_ROUTINE_PARAMS,
    W2_ROUTINE_READY,
    W2_ROUTINE_CONFIRM,
    W2_PREPARE,
    W2_CONNECT,
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
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogSeatWizardUiState())
    val state: StateFlow<CatalogSeatWizardUiState> = _state.asStateFlow()

    private var lastExport: WindowsSetupExportResult? = null

    fun selectKind(kind: CatalogSeatKind) {
        _state.update { it.copy(selectedKind = kind, errorMessage = null) }
    }

    fun goIntro() {
        _state.update {
            it.copy(step = WizardStep.INTRO, errorMessage = null, completedSeat = null, statusMessage = null)
        }
    }

    fun avantiFromIntro() {
        val kind = _state.value.selectedKind
        val step = when (kind) {
            CatalogSeatKind.LOCAL_DEVICE -> WizardStep.W1_CONFIRM
            CatalogSeatKind.PC_NOTEBOOK -> WizardStep.W2_CHOICE
            CatalogSeatKind.CLOUD_DRIVE -> WizardStep.W3_GUIDE
            CatalogSeatKind.NETWORK_NAS -> WizardStep.W4_GUIDE
            CatalogSeatKind.NONE -> WizardStep.INTRO
        }
        _state.update { it.copy(step = step, errorMessage = null) }
    }

    fun goStep(step: WizardStep) {
        _state.update { it.copy(step = step, errorMessage = null, statusMessage = null) }
    }

    /** Call when the user confirms the PC script finished successfully. */
    fun confirmScriptRanOnPc() {
        viewModelScope.launch(Dispatchers.IO) {
            val n = runCatching { exporter.scrubDownloadsScripts() }.getOrDefault(0)
            _state.update {
                it.copy(
                    step = WizardStep.W2_CONNECT,
                    statusMessage = if (n > 0) {
                        "Script rimosso da Download sul telefono (conteneva la password)."
                    } else {
                        "Se in Download resta ancora .ps1, eliminalo: contiene la password."
                    },
                    errorMessage = null,
                )
            }
        }
    }

    fun updateFolderPath(value: String) {
        _state.update { it.copy(folderPath = value, errorMessage = null, scriptPrepared = false) }
    }

    fun updateShareName(value: String) {
        _state.update { it.copy(shareName = value, errorMessage = null, scriptPrepared = false) }
    }

    fun prepareWindowsScript() {
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
                            append("Nessuna USB scrivibile rilevata (puoi usare Condividi). ")
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
        return "Utente: ${s.userName}\nPassword: ${s.temporaryPassword}\nCondivisione: \\\\PC\\${s.shareName}"
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
            _state.update {
                if (result.isSuccess) {
                    it.copy(
                        busy = false,
                        completedSeat = result.getOrNull(),
                        step = WizardStep.DONE,
                    )
                } else {
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
