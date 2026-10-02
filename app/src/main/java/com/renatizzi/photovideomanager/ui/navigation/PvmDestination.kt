package com.renatizzi.photovideomanager.ui.navigation

sealed class PvmDestination(val route: String) {
    data object Home : PvmDestination("home")
    data object Organizza : PvmDestination("organizza")
    data object Componi : PvmDestination("componi")
    data object Pubblica : PvmDestination("pubblica")
    data object Gestisci : PvmDestination("gestisci")
    data object Config : PvmDestination("config")
    data object ArchiveSources : PvmDestination("config/archive")
    data object Acquire : PvmDestination("organizza/acquire")
    data object FeatureStub : PvmDestination("feature/{featureId}") {
        fun create(featureId: String) = "feature/$featureId"
    }
}
