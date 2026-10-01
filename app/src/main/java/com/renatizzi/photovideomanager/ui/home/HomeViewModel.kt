package com.renatizzi.photovideomanager.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.renatizzi.photovideomanager.application.CatalogFacade
import com.renatizzi.photovideomanager.domain.model.Availability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val catalogCount: Long = 0,
    val localAvailability: Availability = Availability.UNKNOWN,
    val ready: Boolean = false,
    val errorMessage: String? = null,
)

class HomeViewModel(
    private val catalogFacade: CatalogFacade,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                catalogFacade.bootstrapPersonalArchiveIfNeeded()
                val count = catalogFacade.mediaItemCount()
                val availability = catalogFacade.localStorageAvailability()
                _state.update {
                    HomeUiState(
                        catalogCount = count,
                        localAvailability = availability,
                        ready = true,
                    )
                }
            }.onFailure { error ->
                _state.update {
                    it.copy(ready = true, errorMessage = error.message ?: "Errore sconosciuto")
                }
            }
        }
    }

    companion object {
        fun factory(catalogFacade: CatalogFacade): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(catalogFacade) as T
                }
            }
    }
}
