package com.renatizzi.photovideomanager.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R

/** Anteprima statica — template Navigazione11 (senza logica dominio). */

@Composable
fun AcquisisciStaticScreen(
    onConferma: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var allChecked by remember { mutableStateOf(true) }
    val rows = listOf(
        "Questo dispositivo" to "primary/Pictures/MediaManager_test",
        "Questo dispositivo" to "2ADB-FB1A/Movies",
        "Questo dispositivo" to "2ADB-FB1A/Pictures",
    )
    var checked by remember { mutableStateOf(rows.map { true }) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_acquisisci),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.acquisisci_static_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.acquisisci_elenco_fonti),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            IconButton(onClick = {}) {
                Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.refresh_sources))
            }
        }
        Text(stringResource(R.string.acquisisci_riepilogo_static), style = MaterialTheme.typography.bodySmall)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = allChecked,
                onCheckedChange = {
                    allChecked = it
                    checked = rows.map { allChecked }
                },
            )
            Text(stringResource(R.string.acquisisci_tutti))
        }

        rows.forEachIndexed { index, (device, path) ->
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = checked[index],
                    onCheckedChange = { value ->
                        checked = checked.toMutableList().also { it[index] = value }
                        allChecked = checked.all { it }
                    },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(device, fontWeight = FontWeight.SemiBold)
                    Text(path, style = MaterialTheme.typography.bodySmall)
                    Text(
                        stringResource(R.string.status_available),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Text(">", style = MaterialTheme.typography.titleLarge)
            }
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onConferma, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.acquisisci_conferma))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ImportaStaticScreen(
    onImporta: () -> Unit,
    onBackToAcquisisci: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val items = listOf(
        "IMG-20180509-WA0000.jpg" to "Pictures",
        "VID_sample.mp4" to "Movies",
        "DSC_0001.jpg" to "DCIM",
        "FB_IMG_1785082611772.jpg" to "Pictures",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.importa_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.importa_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        CatalogSearchField(query = query, onQueryChange = { query = it })

        Text(
            text = stringResource(R.string.importa_elenco),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.importa_riepilogo_static), style = MaterialTheme.typography.bodySmall)

        KindFilters()

        items.forEach { (name, src) ->
            HorizontalDivider()
            MediaRow(
                title = name,
                subtitle = stringResource(R.string.importa_riga_meta, src),
                checked = true,
            )
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onImporta, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.importa_action))
        }
        OutlinedButton(onClick = onBackToAcquisisci, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.importa_back_acquisisci))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AggiornaStaticScreen(
    onPulisci: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    val items = listOf(
        "foto_vacanze.jpg" to "Pictures",
        "video_compleanno.mp4" to "Movies",
        "scan_documento.jpg" to "DCIM",
        "IMG_2024_001.jpg" to "Camera",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_aggiorna),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.aggiorna_static_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        CatalogSearchField(query = query, onQueryChange = { query = it })

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.aggiorna_catalogo_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            // Bottone aggiornamento / allinea (come template): refresh catalogo-sorgenti
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Refresh,
                    contentDescription = stringResource(R.string.aggiorna_refresh_cd),
                )
            }
        }
        Text(stringResource(R.string.aggiorna_riepilogo_static), style = MaterialTheme.typography.bodySmall)

        KindFilters()

        items.forEach { (name, src) ->
            HorizontalDivider()
            MediaRow(
                title = name,
                subtitle = stringResource(R.string.importa_riga_meta, src),
                checked = false,
                trailing = "⋮",
            )
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onPulisci, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.feature_pulisci))
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Landing Componi — template image16: ricerca + elenco album/raccolte.
 */
@Composable
fun ComponiLandingStaticScreen(modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val albums = listOf(
        "Vacanze 2024" to "Album fotografico · 12/08/2026",
        "Compleanno Marco" to "Raccolta · 03/09/2026",
        "Ricordi famiglia" to "Album fotografico · 01/10/2026",
        "Evento scuola" to "Raccolta · 20/09/2026",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.componi),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.componi_landing_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        CatalogSearchField(query = query, onQueryChange = { query = it })

        Text(
            text = stringResource(R.string.componi_elenco_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.componi_riepilogo_static), style = MaterialTheme.typography.bodySmall)

        KindFilters()

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}) { Text(stringResource(R.string.feature_crea)) }
            OutlinedButton(onClick = {}) { Text(stringResource(R.string.feature_edita)) }
            OutlinedButton(onClick = {}) { Text(stringResource(R.string.feature_pubblica)) }
        }

        albums.forEach { (name, meta) ->
            HorizontalDivider()
            MediaRow(
                title = name,
                subtitle = meta,
                checked = false,
                trailing = "⋮",
            )
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CatalogSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_query_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        singleLine = true,
        label = { Text(stringResource(R.string.feature_ricerca)) },
    )
}

@Composable
private fun KindFilters() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = true, onClick = {}, label = { Text(stringResource(R.string.search_filter_all)) })
        FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.search_filter_photo)) })
        FilterChip(selected = false, onClick = {}, label = { Text(stringResource(R.string.search_filter_video)) })
    }
}

@Composable
private fun MediaRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    trailing: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        // Mini foto / cover accanto alla selezione (tutti gli elenchi media)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.titleLarge)
        }
    }
}
