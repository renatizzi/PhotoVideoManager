package com.renatizzi.photovideomanager.ui.hub

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.hub.CatalogSeat
import com.renatizzi.photovideomanager.domain.hub.ConflictResolution
import com.renatizzi.photovideomanager.domain.hub.HubDiffPreview
import com.renatizzi.photovideomanager.domain.hub.HubSourceDescriptor
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
    val unverified: Boolean = false,
    val sources: List<HubSourceDescriptor> = emptyList(),
    val linkedKeys: Set<String> = emptySet(),
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
                _state.update {
                    it.copy(busy = false, reachable = false, unverified = false, sources = emptyList())
                }
                return@launch
            }
            val ok = catalogFacade.catalogSeatReachable()
            val sources = catalogFacade.portableHubSources()
            _state.update {
                it.copy(
                    busy = false,
                    reachable = ok,
                    unverified = catalogFacade.hubUnverified(),
                    sources = sources,
                    linkedKeys = catalogFacade.linkedHubSourceKeys(),
                )
            }
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
                    it.copy(
                        busy = false,
                        preview = result.getOrNull(),
                        resolutions = emptyMap(),
                        unverified = catalogFacade.hubUnverified(),
                    )
                } else {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Impossibile confrontare il Catalogo",
                        preview = null,
                        unverified = catalogFacade.hubUnverified(),
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

    fun apply() = applyUpdate(receiveOnly = false)

    fun applyReceiveOnly() = applyUpdate(receiveOnly = true)

    fun restoreBackup() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, errorMessage = null, successMessage = null) }
            val result = catalogFacade.restoreHubBackup()
            if (result.isFailure) {
                _state.update {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Ripristino non riuscito",
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        preview = null,
                        resolutions = emptyMap(),
                        successMessage = "Copia verificata ripristinata",
                    )
                }
                refreshSeat()
            }
        }
    }

    fun linkSource(sourceKey: String, treeUri: String, displayName: String) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, errorMessage = null, successMessage = null) }
            val result = catalogFacade.linkHubSource(sourceKey, Uri.parse(treeUri), displayName)
            _state.update {
                if (result.isSuccess) {
                    it.copy(
                        busy = false,
                        linkedKeys = catalogFacade.linkedHubSourceKeys(),
                        successMessage = "Cartella collegata su questo apparecchio. I file non sono stati copiati.",
                    )
                } else {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Collegamento non riuscito",
                    )
                }
            }
        }
    }

    private fun applyUpdate(receiveOnly: Boolean) {
        val preview = _state.value.preview ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true, errorMessage = null, successMessage = null) }
            val result = catalogFacade.applyCatalogHubUpdate(
                preview,
                _state.value.resolutions,
                receiveOnly,
            )
            _state.update {
                if (result.isFailure) {
                    it.copy(
                        busy = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Aggiornamento non riuscito",
                        unverified = catalogFacade.hubUnverified(),
                    )
                } else {
                    val remaining = result.getOrNull()?.conflicts.orEmpty()
                    val message = when {
                        receiveOnly && remaining.isNotEmpty() ->
                            "Schede del fascicolo recepite, senza pubblicare quelle di questo apparecchio. I conflitti sono invariati."
                        receiveOnly ->
                            "Schede del fascicolo recepite. Nessuna scheda solo di questo apparecchio è stata pubblicata."
                        remaining.isNotEmpty() ->
                            "Aggiornate le schede senza conflitto. Quelle in conflitto sono rimaste invariate."
                        else -> "Catalogo aggiornato"
                    }
                    it.copy(
                        busy = false,
                        preview = if (remaining.isEmpty()) {
                            null
                        } else {
                            HubDiffPreview(emptyList(), emptyList(), remaining)
                        },
                        resolutions = emptyMap(),
                        applied = true,
                        successMessage = message,
                        unverified = catalogFacade.hubUnverified(),
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
                    CatalogUpdateViewModel(catalogFacade) as T
            }
    }
}
