package com.renatizzi.photovideomanager.ui.navigation

sealed class PvmDestination(val route: String) {
    data object Home : PvmDestination("home")
    data object Organizza : PvmDestination("organizza")
    data object Componi : PvmDestination("componi")
    data object Utility : PvmDestination("utility")
    data object Config : PvmDestination("config")
    data object Acquire : PvmDestination("organizza/acquire")
    data object SourceBrowse : PvmDestination("organizza/acquire/browse/{locationId}") {
        fun create(locationId: String) = "organizza/acquire/browse/$locationId"
    }
    data object Aggiorna : PvmDestination("organizza/aggiorna")
    data object Search : PvmDestination("organizza/search")
    data object Clean : PvmDestination("organizza/clean")
    data object Ripristina : PvmDestination("utility/ripristina")
    data object Trash : PvmDestination("utility/trash")
    data object FeatureStub : PvmDestination("feature/{featureId}") {
        fun create(featureId: String) = "feature/$featureId"
    }
}
