package com.renatizzi.photovideomanager.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogSeatWizardUiState(
    val step: WizardStep = WizardStep.INTRO,
    val selectedKind: CatalogSeatKind = CatalogSeatKind.LOCAL_DEVICE,
    val pendingTreeUri: String? = null,
    val pendingLabel: String = "",
    val busy: Boolean = false,
    val errorMessage: String? = null,
    val completedSeat: CatalogSeat? = null,
)

enum class WizardStep {
    INTRO,
    W1_CONFIRM,
    W2_PREPARE,
    W2_CONNECT,
    W2_HELP,
    W2_PICK,
    W3_GUIDE,
    W4_GUIDE,
    DONE,
}

class CatalogSeatWizardViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogSeatWizardUiState())
    val state: StateFlow<CatalogSeatWizardUiState> = _state.asStateFlow()

    fun selectKind(kind: CatalogSeatKind) {
        _state.update { it.copy(selectedKind = kind, errorMessage = null) }
    }

    fun goIntro() {
        _state.update {
            it.copy(step = WizardStep.INTRO, errorMessage = null, completedSeat = null)
        }
    }

    fun avantiFromIntro() {
        val kind = _state.value.selectedKind
        val step = when (kind) {
            CatalogSeatKind.LOCAL_DEVICE -> WizardStep.W1_CONFIRM
            CatalogSeatKind.PC_NOTEBOOK -> WizardStep.W2_PREPARE
            CatalogSeatKind.CLOUD_DRIVE -> WizardStep.W3_GUIDE
            CatalogSeatKind.NETWORK_NAS -> WizardStep.W4_GUIDE
            CatalogSeatKind.NONE -> WizardStep.INTRO
        }
        _state.update { it.copy(step = step, errorMessage = null) }
    }

    fun goStep(step: WizardStep) {
        _state.update { it.copy(step = step, errorMessage = null) }
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
        fun factory(catalogFacade: CatalogFacade): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatalogSeatWizardViewModel(catalogFacade) as T
            }
    }
}
