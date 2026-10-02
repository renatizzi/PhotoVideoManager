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
    val pubblicaAccent = Color(0xFFEF6C00)
    val gestisciAccent = Color(0xFF6A1B9A)

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
                    id = "archivia",
                    titleRes = R.string.feature_archivia,
                    subtitleRes = R.string.feature_archivia_desc,
                ),
                MacroFeature(
                    id = "ricerca",
                    titleRes = R.string.feature_ricerca,
                    subtitleRes = R.string.feature_ricerca_desc,
                ),
                MacroFeature(
                    id = "raggruppa",
                    titleRes = R.string.feature_raggruppa,
                    subtitleRes = R.string.feature_raggruppa_desc,
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
                    id = "edita",
                    titleRes = R.string.feature_edita,
                    subtitleRes = R.string.feature_edita_desc,
                ),
                MacroFeature(
                    id = "crea",
                    titleRes = R.string.feature_crea,
                    subtitleRes = R.string.feature_crea_desc,
                ),
            ),
        ),
        MacroAreaDefinition(
            tab = ShellTab.PUBBLICA,
            titleRes = R.string.pubblica,
            subtitleRes = R.string.hub_pubblica_subtitle,
            accent = pubblicaAccent,
            features = listOf(
                MacroFeature(
                    id = "condividi",
                    titleRes = R.string.feature_condividi,
                    subtitleRes = R.string.feature_condividi_desc,
                ),
                MacroFeature(
                    id = "social",
                    titleRes = R.string.feature_social,
                    subtitleRes = R.string.feature_social_desc,
                ),
                MacroFeature(
                    id = "produci",
                    titleRes = R.string.feature_produci,
                    subtitleRes = R.string.feature_produci_desc,
                ),
            ),
        ),
        MacroAreaDefinition(
            tab = ShellTab.GESTISCI,
            titleRes = R.string.gestisci,
            subtitleRes = R.string.hub_gestisci_subtitle,
            accent = gestisciAccent,
            features = listOf(
                MacroFeature(
                    id = "salva",
                    titleRes = R.string.feature_salva,
                    subtitleRes = R.string.feature_salva_desc,
                ),
                MacroFeature(
                    id = "pulisci",
                    titleRes = R.string.feature_pulisci,
                    subtitleRes = R.string.feature_pulisci_desc,
                ),
            ),
        ),
    )

    fun areaFor(tab: ShellTab): MacroAreaDefinition? = areas.firstOrNull { it.tab == tab }
}
