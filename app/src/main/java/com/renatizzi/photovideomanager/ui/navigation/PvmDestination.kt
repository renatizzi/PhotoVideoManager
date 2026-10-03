package com.renatizzi.photovideomanager.ui.navigation

sealed class PvmDestination(val route: String) {
    data object Home : PvmDestination("home")
    data object Organizza : PvmDestination("organizza")
    data object Componi : PvmDestination("componi")
    data object Pubblica : PvmDestination("pubblica")
    data object Gestisci : PvmDestination("gestisci")
    data object Config : PvmDestination("config")
    data object Acquire : PvmDestination("organizza/acquire")
    data object SourceBrowse : PvmDestination("organizza/acquire/source/{locationId}") {
        fun create(locationId: String) = "organizza/acquire/source/$locationId"
    }
    data object ArchiveBrowse : PvmDestination("organizza/archive")
    data object Search : PvmDestination("organizza/search")
    data object Clean : PvmDestination("gestisci/clean")
    data object Trash : PvmDestination("gestisci/trash")
    data object FeatureStub : PvmDestination("feature/{featureId}") {
        fun create(featureId: String) = "feature/$featureId"
    }
}
