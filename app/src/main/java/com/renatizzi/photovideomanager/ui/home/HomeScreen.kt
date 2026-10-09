package com.renatizzi.photovideomanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot

/**
 * Home senza scrolling: KPI + ricerca + 6 accessi rapidi funzioni.
 * Compattato per restare in un viewport tipico.
 */
@Composable
fun HomeScreen(
    snapshot: DashboardSnapshot?,
    onOpenTab: (com.renatizzi.photovideomanager.ui.shell.ShellTab) -> Unit,
    onOpenFeature: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val photos = snapshot?.photoCount?.toString() ?: "xxx"
    val videos = snapshot?.videoCount?.toString() ?: "xxx"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            KpiCard(
                title = stringResource(R.string.kpi_foto_originali),
                value = photos,
                detail = stringResource(R.string.kpi_di_cui_acquisite, "xxx"),
                modifier = Modifier.weight(1f),
            )
            KpiCard(
                title = stringResource(R.string.kpi_video_originali),
                value = videos,
                detail = stringResource(R.string.kpi_di_cui_acquisiti, "xxx"),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            KpiCard(
                title = stringResource(R.string.kpi_spazio_foto),
                value = "xxx MB",
                detail = stringResource(R.string.kpi_di_cui_duplicati, "xxx"),
                modifier = Modifier.weight(1f),
            )
            KpiCard(
                title = stringResource(R.string.kpi_spazio_video),
                value = "xxx MB",
                detail = stringResource(R.string.kpi_di_cui_duplicati, "xxx"),
                modifier = Modifier.weight(1f),
            )
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable { onOpenFeature("ricerca") },
            placeholder = { Text(stringResource(R.string.feature_ricerca)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = { Icon(Icons.Outlined.Mic, contentDescription = null) },
            singleLine = true,
        )

        Text(
            text = stringResource(R.string.quick_access),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        val quick = listOf(
            "acquisisci" to R.string.feature_acquisisci,
            "aggiorna" to R.string.feature_aggiorna,
            "crea" to R.string.feature_crea,
            "edita" to R.string.feature_edita,
            "backup" to R.string.feature_backup,
            "ripristina" to R.string.feature_ripristina,
        )
        quick.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                pair.forEach { (id, titleRes) ->
                    QuickAccessButton(
                        title = stringResource(titleRes),
                        color = MacroNavigation.organizaAccent,
                        onClick = { onOpenFeature(id) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun QuickAccessButton(
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
