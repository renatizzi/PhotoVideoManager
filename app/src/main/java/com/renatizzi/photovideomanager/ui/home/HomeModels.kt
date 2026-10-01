package com.renatizzi.photovideomanager.ui.home

data class MacroFeature(
    val id: String,
    val title: String,
)

data class MacroArea(
    val titleRes: Int,
    val features: List<MacroFeature>,
)
