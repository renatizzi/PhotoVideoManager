package com.renatizzi.photovideomanager.ui.home

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.ui.shell.ShellTab

data class MacroFeature(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val available: Boolean = false,
)

data class MacroAreaDefinition(
    val tab: ShellTab,
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int,
    val accent: Color,
    val features: List<MacroFeature>,
)

object MacroNavigation {
    val organizaAccent = Color(0xFF1565C0)
    val componiAccent = Color(0xFF2E7D32)
    val utilityAccent = Color(0xFF6A1B9A)

    val areas: List<MacroAreaDefinition> = listOf(
        MacroAreaDefinition(
            tab = ShellTab.ORGANIZZA,
            titleRes = R.string.organizza,
            subtitleRes = R.string.hub_organizza_subtitle,
            accent = organizaAccent,
            features = listOf(
                MacroFeature(
                    id = "acquisisci",
                    titleRes = R.string.feature_acquisisci,
                    subtitleRes = R.string.feature_acquisisci_desc,
                    available = true,
                ),
                MacroFeature(
                    id = "aggiorna",
                    titleRes = R.string.feature_aggiorna,
                    subtitleRes = R.string.feature_aggiorna_desc,
                    available = true,
                ),
            ),
        ),
        MacroAreaDefinition(
            tab = ShellTab.COMPONI,
            titleRes = R.string.componi,
            subtitleRes = R.string.hub_componi_subtitle,
            accent = componiAccent,
            features = listOf(
                MacroFeature(
                    id = "crea",
                    titleRes = R.string.feature_crea,
                    subtitleRes = R.string.feature_crea_desc,
                ),
                MacroFeature(
                    id = "edita",
                    titleRes = R.string.feature_edita,
                    subtitleRes = R.string.feature_edita_desc,
                ),
                MacroFeature(
                    id = "pubblica",
                    titleRes = R.string.feature_pubblica,
                    subtitleRes = R.string.feature_pubblica_desc,
                ),
            ),
        ),
        MacroAreaDefinition(
            tab = ShellTab.UTILITY,
            titleRes = R.string.utility,
            subtitleRes = R.string.hub_utility_subtitle,
            accent = utilityAccent,
            features = listOf(
                MacroFeature(
                    id = "backup",
                    titleRes = R.string.feature_backup,
                    subtitleRes = R.string.feature_backup_desc,
                ),
                MacroFeature(
                    id = "ripristina",
                    titleRes = R.string.feature_ripristina,
                    subtitleRes = R.string.feature_ripristina_desc,
                    available = true,
                ),
                MacroFeature(
                    id = "revisione",
                    titleRes = R.string.feature_revisione,
                    subtitleRes = R.string.feature_revisione_desc,
                ),
                MacroFeature(
                    id = "spazio",
                    titleRes = R.string.feature_spazio,
                    subtitleRes = R.string.feature_spazio_desc,
                ),
            ),
        ),
    )

    fun areaFor(tab: ShellTab): MacroAreaDefinition? = areas.firstOrNull { it.tab == tab }
}
