package com.renatizzi.photovideomanager.ui.navigation

sealed class PvmDestination(val route: String) {
    data object Home : PvmDestination("home")
    data object Organizza : PvmDestination("organizza")
    data object Componi : PvmDestination("componi")
    data object Utility : PvmDestination("utility")
    data object Config : PvmDestination("config")
    data object Acquire : PvmDestination("organizza/acquire")
    data object CatalogSeatWizard : PvmDestination("organizza/acquire/catalog-seat")
    data object CatalogUpdate : PvmDestination("organizza/acquire/catalog-update")
    data object SourceBrowse : PvmDestination("organizza/acquire/browse/{locationId}") {
        fun create(locationId: String) = "organizza/acquire/browse/$locationId"
    }
    data object Aggiorna : PvmDestination("organizza/aggiorna")
    data object Search : PvmDestination("organizza/search")
    /**
     * Pulisci. [sel] = id selezionati in Aggiorna separati da virgola (BL-12).
     * Passarli nella route evita di perdere i ✓ all’apertura della pagina.
     */
    data object Clean : PvmDestination("organizza/clean?sel={sel}") {
        fun create(selectedIds: Set<String> = emptySet()): String {
            val sel = selectedIds.joinToString(",")
            return "organizza/clean?sel=$sel"
        }
    }
    data object Ripristina : PvmDestination("utility/ripristina")
    data object Trash : PvmDestination("utility/trash")
    data object FeatureStub : PvmDestination("feature/{featureId}") {
        fun create(featureId: String) = "feature/$featureId"
    }
}
