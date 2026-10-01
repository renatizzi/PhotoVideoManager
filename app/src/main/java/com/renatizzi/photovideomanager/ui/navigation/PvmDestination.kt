package com.renatizzi.photovideomanager.ui.navigation

sealed class PvmDestination(val route: String) {
    data object Home : PvmDestination("home")
    data object Config : PvmDestination("config")
    data object ArchiveSources : PvmDestination("config/archive")
    data object FeatureStub : PvmDestination("feature/{featureId}") {
        fun create(featureId: String) = "feature/$featureId"
    }
}
