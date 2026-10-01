package com.renatizzi.photovideomanager.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    catalogCount: Long,
    storageStatus: String,
    onFeatureClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val areas = listOf(
        MacroArea(
            titleRes = R.string.organizza,
            features = listOf(
                MacroFeature("acquisisci", "Acquisisci"),
                MacroFeature("archivia", "Archivia"),
                MacroFeature("ricerca", "Ricerca"),
                MacroFeature("raggruppa", "Raggruppa"),
            ),
        ),
        MacroArea(
            titleRes = R.string.componi,
            features = listOf(
                MacroFeature("edita", "Edita"),
                MacroFeature("crea", "Crea"),
            ),
        ),
        MacroArea(
            titleRes = R.string.pubblica,
            features = listOf(
                MacroFeature("condividi", "Condividi"),
                MacroFeature("social", "Invia ai social"),
                MacroFeature("produci", "Produci"),
            ),
        ),
        MacroArea(
            titleRes = R.string.gestisci,
            features = listOf(
                MacroFeature("salva", "Salva"),
                MacroFeature("pulisci", "Pulisci"),
            ),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.catalog_status, catalogCount),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = storageStatus,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        areas.forEach { area ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(area.titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    area.features.forEach { feature ->
                        AssistChip(
                            onClick = { onFeatureClick(feature.id) },
                            label = { Text(feature.title) },
                        )
                    }
                }
            }
        }
    }
}
