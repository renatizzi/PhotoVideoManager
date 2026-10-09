package com.renatizzi.photovideomanager.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
 * Home / Dashboard — struttura statica allineata al template Navigazione11 (slide 4).
 * KPI + Ricerca nel Catalogo + Accesso rapido (funzioni, non macro Bottom Bar).
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.dashboard_inline_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        // KPI 2x2 — template
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KpiCard(
                title = stringResource(R.string.kpi_foto_originali),
                value = photos,
                detail = stringResource(R.string.kpi_di_cui_acquisite, "xxx"),
                modifier = Modifier.weight(1f),
                onClick = { onOpenFeature("ricerca") },
            )
            KpiCard(
                title = stringResource(R.string.kpi_video_originali),
                value = videos,
                detail = stringResource(R.string.kpi_di_cui_acquisiti, "xxx"),
                modifier = Modifier.weight(1f),
                onClick = { onOpenFeature("ricerca") },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KpiCard(
                title = stringResource(R.string.kpi_spazio_foto),
                value = "xxx MB",
                detail = stringResource(R.string.kpi_di_cui_duplicati, "xxx"),
                modifier = Modifier.weight(1f),
                onClick = { onOpenFeature("spazio") },
            )
            KpiCard(
                title = stringResource(R.string.kpi_spazio_video),
                value = "xxx MB",
                detail = stringResource(R.string.kpi_di_cui_duplicati, "xxx"),
                modifier = Modifier.weight(1f),
                onClick = { onOpenFeature("spazio") },
            )
        }

        // Ricerca nel Catalogo — motore unico, un riquadro
        Text(
            text = stringResource(R.string.feature_ricerca),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenFeature("ricerca") },
            placeholder = { Text(stringResource(R.string.search_query_hint)) },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            trailingIcon = {
                Icon(Icons.Outlined.Mic, contentDescription = null)
            },
            singleLine = true,
        )

        // Accesso rapido — funzioni (NON le macro già in Bottom Bar)
        Text(
            text = stringResource(R.string.quick_access),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        val quick = listOf(
            "acquisisci" to R.string.feature_acquisisci,
            "crea" to R.string.feature_crea,
            "condividi" to R.string.feature_condividi,
            "raggruppa" to R.string.feature_raggruppa,
            "edita" to R.string.feature_edita,
            "pulisci" to R.string.feature_pulisci,
            "salva" to R.string.feature_salva,
            "ripristina" to R.string.feature_ripristina,
        )
        quick.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                pair.forEach { (id, titleRes) ->
                    QuickAccessButton(
                        title = stringResource(titleRes),
                        color = MacroNavigation.organizaAccent,
                        onClick = { onOpenFeature(id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    detail: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Card(
        modifier = if (onClick != null) modifier.clickable(onClick = onClick) else modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
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
            .height(56.dp)
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
