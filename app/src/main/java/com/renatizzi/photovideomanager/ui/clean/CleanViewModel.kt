package com.renatizzi.photovideomanager.ui.clean

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CleanUiState(
    val groups: List<DuplicateGroup> = emptyList(),
    val analyzing: Boolean = false,
    val copiesScanned: Int = 0,
    val hashesComputed: Int = 0,
    val hashesFailed: Int = 0,
    val hasAnalyzed: Boolean = false,
    val message: String? = null,
)

class CleanViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(CleanUiState())
    val state: StateFlow<CleanUiState> = _state.asStateFlow()

    fun analyze() {
        viewModelScope.launch {
            _state.update { it.copy(analyzing = true, message = null) }
            runCatching { catalogFacade.analyzeExactDuplicates() }
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            analyzing = false,
                            hasAnalyzed = true,
                            groups = result.groups,
                            copiesScanned = result.copiesScanned,
                            hashesComputed = result.hashesComputed,
                            hashesFailed = result.hashesFailed,
                            message = result.message ?: buildString {
                                append("Analisi terminata: ${result.groups.size} gruppi")
                                if (result.hashesComputed > 0) {
                                    append(", ${result.hashesComputed} hash calcolati")
                                }
                                if (result.hashesFailed > 0) {
                                    append(", ${result.hashesFailed} non leggibili")
                                }
                            },
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            analyzing = false,
                            message = error.message ?: "Analisi non riuscita",
                        )
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
                    return CleanViewModel(catalogFacade) as T
                }
            }
    }
}
