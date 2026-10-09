package com.renatizzi.photovideomanager.ui.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R

/**
 * Anteprima statica allineata ai template Navigazione11.
 * Nessuna logica di dominio: solo struttura UI da convalidare.
 */

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
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.acquisisci_static_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        Text(
            text = stringResource(R.string.acquisisci_elenco_fonti),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.acquisisci_riepilogo_static))

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
        Button(
            onClick = onConferma,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.acquisisci_conferma))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ImportaStaticScreen(
    onImporta: () -> Unit,
    onBackToAcquisisci: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.importa_static_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        Text(
            text = stringResource(R.string.importa_elenco),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.importa_riepilogo_static))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                R.string.search_filter_all,
                R.string.search_filter_photo,
                R.string.search_filter_video,
            ).forEach { res ->
                OutlinedButton(onClick = {}) { Text(stringResource(res)) }
            }
        }

        listOf(
            "IMG-20180509-WA0000.jpg" to "Pictures",
            "VID_sample.mp4" to "Movies",
            "DSC_0001.jpg" to "DCIM",
        ).forEach { (name, src) ->
            HorizontalDivider()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = true, onCheckedChange = null)
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(name, fontWeight = FontWeight.Medium)
                    Text(
                        stringResource(R.string.importa_riga_meta, src),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onImporta, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.importa_action))
        }
        OutlinedButton(onClick = onBackToAcquisisci, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.importa_back_acquisisci))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AggiornaStaticScreen(
    onPulisci: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.aggiorna_static_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        Text(
            text = stringResource(R.string.aggiorna_catalogo_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.aggiorna_riepilogo_static))

        listOf("foto_vacanze.jpg", "video_compleanno.mp4", "scan_documento.jpg").forEach { name ->
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = false, onCheckedChange = null)
                Text(name, modifier = Modifier.weight(1f).padding(start = 8.dp))
                Text("⋮", style = MaterialTheme.typography.titleLarge)
            }
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onPulisci, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.feature_pulisci))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ComponiLandingStaticScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.componi),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.hub_componi_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.componi_static_help),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }
        Text(
            text = stringResource(R.string.componi_elenco_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(stringResource(R.string.componi_empty_static))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {}) { Text(stringResource(R.string.feature_crea)) }
            OutlinedButton(onClick = {}) { Text(stringResource(R.string.feature_edita)) }
            OutlinedButton(onClick = {}) { Text(stringResource(R.string.feature_pubblica)) }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
