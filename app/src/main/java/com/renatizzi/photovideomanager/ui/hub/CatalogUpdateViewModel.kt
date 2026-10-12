package com.renatizzi.photovideomanager.ui.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.ConflictResolution
import com.renatizzi.photovideomanager.domain.hub.HubDiffPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUpdateUiState(
    val seat: CatalogSeat = CatalogSeat(com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind.NONE),
    val reachable: Boolean? = null,
    val busy: Boolean = false,
    val preview: HubDiffPreview? = null,
    val resolutions: Map<String, ConflictResolution> = emptyMap(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val applied: Boolean = false,
)

class CatalogUpdateViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(CatalogUpdateUiState())
    val state: StateFlow<CatalogUpdateUiState> = _state.asStateFlow()

    init {
        refreshSeat()
    }

    fun refreshSeat() {
        viewModelScope.launch {
            val seat = catalogFacade.catalogSeat()
            _state.update { it.copy(seat = seat, busy = true, errorMessage = null) }
            if (!seat.isConfigured) {
                _state.update { it.copy(busy = false, reachable = false) }
                return@launch
            }
            val ok = catalogFacade.catalogSeatReachable()
            _state.update { it.copy(busy = false, reachable = ok) }
        }
    }

    fun loadPreview() {
        viewModelScope.launch {
            _state.update {
                it.copy(busy = true, errorMessage = null, successMessage = null, applied = false)
            }
            val result = catalogFacade.catalogHubPreview()
            _state.update {
                if (result.isSuccess) {
                    it.copy(busy = false, preview = result.getOrNull(), resolutions = emptyMap())
                } else {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Impossibile confrontare il Catalogo",
                        preview = null,
                    )
                }
            }
        }
    }

    fun setResolution(cardId: String, resolution: ConflictResolution) {
        _state.update {
            it.copy(resolutions = it.resolutions + (cardId to resolution))
        }
    }

    fun cancelPreview() {
        _state.update {
            it.copy(preview = null, resolutions = emptyMap(), successMessage = null)
        }
    }

    fun apply() {
        val preview = _state.value.preview ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, errorMessage = null) }
            val result = catalogFacade.applyCatalogHubUpdate(preview, _state.value.resolutions)
            _state.update {
                if (result.isFailure) {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Aggiornamento non riuscito",
                    )
                } else {
                    val left = result.getOrNull()
                    if (left != null && left.conflicts.isNotEmpty()) {
                        it.copy(
                            busy = false,
                            preview = left,
                            errorMessage = "Risolvi i conflitti prima di continuare",
                        )
                    } else {
                        it.copy(
                            busy = false,
                            preview = null,
                            resolutions = emptyMap(),
                            applied = true,
                            successMessage = "Catalogo aggiornato",
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun factory(catalogFacade: CatalogFacade): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CatalogUpdateViewModel(catalogFacade) as T
            }
    }
}
