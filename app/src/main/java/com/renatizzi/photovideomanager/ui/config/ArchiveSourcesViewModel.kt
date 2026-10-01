package com.renatizzi.photovideomanager.ui.config

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.SourceSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArchiveSourcesUiState(
    val sources: List<SourceSummary> = emptyList(),
    val loading: Boolean = true,
    val message: String? = null,
)

class ArchiveSourcesViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(ArchiveSourcesUiState())
    val state: StateFlow<ArchiveSourcesUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching { catalogFacade.listSources(refresh = true) }
                .onSuccess { sources ->
                    _state.update { it.copy(sources = sources, loading = false) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            message = error.message ?: "Errore nel caricamento delle sorgenti",
                        )
                    }
                }
        }
    }

    fun addSafFolder(uri: Uri, displayName: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null) }
            runCatching { catalogFacade.registerSafFolder(uri, displayName) }
                .onSuccess { added ->
                    val sources = catalogFacade.listSources(refresh = true)
                    _state.update {
                        it.copy(
                            sources = sources,
                            loading = false,
                            message = "Cartella aggiunta: ${added.displayName}",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            message = error.message ?: "Impossibile aggiungere la cartella",
                        )
                    }
                }
        }
    }

    fun removeSource(locationId: String) {
        viewModelScope.launch {
            runCatching { catalogFacade.removeSource(locationId) }
                .onSuccess {
                    val sources = catalogFacade.listSources(refresh = true)
                    _state.update {
                        it.copy(sources = sources, message = "Sorgente rimossa")
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(message = error.message ?: "Impossibile rimuovere la sorgente")
                    }
                }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(catalogFacade: CatalogFacade): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ArchiveSourcesViewModel(catalogFacade) as T
                }
            }
    }
}
