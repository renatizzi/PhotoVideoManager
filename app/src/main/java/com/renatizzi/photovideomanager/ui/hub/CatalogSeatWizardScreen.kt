package com.renatizzi.photovideomanager.ui.hub

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind

@Composable
fun CatalogSeatWizardScreen(
    state: CatalogSeatWizardUiState,
    onSelectKind: (CatalogSeatKind) -> Unit,
    onAvanti: () -> Unit,
    onBackIntro: () -> Unit,
    onGoStep: (WizardStep) -> Unit,
    onConfirmLocal: () -> Unit,
    onFolderPicked: (uri: String, label: String) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val openTree = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri: Uri? ->
        if (uri != null) {
            val label = SafPathLabels.folderTitle(
                context,
                uri,
                context.getString(R.string.hub_seat_folder_fallback),
            )
            onFolderPicked(uri.toString(), label)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.hub_wizard_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        when (state.step) {
            WizardStep.INTRO -> IntroStep(
                selected = state.selectedKind,
                onSelectKind = onSelectKind,
                onAvanti = onAvanti,
                onCancel = onCancel,
            )
            WizardStep.W1_CONFIRM -> SimpleGuide(
                body = stringResource(R.string.hub_w1_body),
                primary = stringResource(R.string.hub_w1_confirm),
                onPrimary = onConfirmLocal,
                onBack = onBackIntro,
                busy = state.busy,
            )
            WizardStep.W2_PREPARE -> SimpleGuide(
                body = stringResource(R.string.hub_w2_prepare),
                primary = stringResource(R.string.hub_w2_prepared),
                onPrimary = { onGoStep(WizardStep.W2_CONNECT) },
                onBack = onBackIntro,
            )
            WizardStep.W2_CONNECT -> SimpleGuide(
                body = stringResource(R.string.hub_w2_connect),
                primary = stringResource(R.string.hub_w2_connected),
                onPrimary = { onGoStep(WizardStep.W2_PICK) },
                onBack = { onGoStep(WizardStep.W2_PREPARE) },
                secondary = stringResource(R.string.hub_w2_help_link),
                onSecondary = { onGoStep(WizardStep.W2_HELP) },
            )
            WizardStep.W2_HELP -> SimpleGuide(
                body = stringResource(R.string.hub_w2_help_body),
                primary = stringResource(R.string.hub_w2_help_back),
                onPrimary = { onGoStep(WizardStep.W2_CONNECT) },
                onBack = onBackIntro,
            )
            WizardStep.W2_PICK -> SimpleGuide(
                body = stringResource(R.string.hub_w2_pick),
                primary = stringResource(R.string.hub_choose_folder),
                onPrimary = { if (!state.busy) openTree.launch(null) },
                onBack = { onGoStep(WizardStep.W2_CONNECT) },
                busy = state.busy,
            )
            WizardStep.W3_GUIDE -> SimpleGuide(
                body = stringResource(R.string.hub_w3_body),
                primary = stringResource(R.string.hub_choose_folder),
                onPrimary = { if (!state.busy) openTree.launch(null) },
                onBack = onBackIntro,
                busy = state.busy,
            )
            WizardStep.W4_GUIDE -> SimpleGuide(
                body = stringResource(R.string.hub_w4_body),
                primary = stringResource(R.string.hub_choose_folder),
                onPrimary = { if (!state.busy) openTree.launch(null) },
                onBack = onBackIntro,
                busy = state.busy,
            )
            WizardStep.DONE -> {
                Text(stringResource(R.string.hub_done_title), fontWeight = FontWeight.Medium)
                Text(
                    stringResource(
                        R.string.hub_done_body,
                        state.completedSeat?.displayLabel ?: "—",
                    ),
                )
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.ok))
                }
            }
        }

        state.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        if (state.busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun IntroStep(
    selected: CatalogSeatKind,
    onSelectKind: (CatalogSeatKind) -> Unit,
    onAvanti: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(stringResource(R.string.hub_intro_body))
    Text(stringResource(R.string.hub_combo_label), fontWeight = FontWeight.Medium)
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        CatalogSeatKind.LOCAL_DEVICE to stringResource(R.string.hub_kind_local),
        CatalogSeatKind.PC_NOTEBOOK to stringResource(R.string.hub_kind_pc),
        CatalogSeatKind.CLOUD_DRIVE to stringResource(R.string.hub_kind_drive),
        CatalogSeatKind.NETWORK_NAS to stringResource(R.string.hub_kind_nas),
    )
    val selectedLabel = options.first { it.first == selected }.second
    OutlinedButton(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(selectedLabel)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { (kind, label) ->
            DropdownMenuItem(
                text = { Text(label) },
                onClick = {
                    onSelectKind(kind)
                    expanded = false
                },
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.importa_annulla))
        }
        Button(onClick = onAvanti, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_avanti))
        }
    }
}

@Composable
private fun SimpleGuide(
    body: String,
    primary: String,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean = false,
    secondary: String? = null,
    onSecondary: (() -> Unit)? = null,
) {
    Text(body)
    if (secondary != null && onSecondary != null) {
        TextButton(onClick = onSecondary, enabled = !busy) { Text(secondary) }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(onClick = onPrimary, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(primary)
        }
    }
}
