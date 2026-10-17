package com.renatizzi.photovideomanager.ui.hub

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.data.hub.WindowsCatalogSetupScript
import com.renatizzi.photovideomanager.data.storage.NetworkDriveAssistant
import com.renatizzi.photovideomanager.data.storage.OpenDocumentTreeAdvanced
import com.renatizzi.photovideomanager.data.storage.SafPathLabels
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKind

@Composable
fun CatalogSeatWizardScreen(
    state: CatalogSeatWizardUiState,
    onStartCreate: () -> Unit,
    onStartModify: () -> Unit,
    onResumeDraft: () -> Unit,
    onDiscardDraft: () -> Unit,
    onSelectKind: (CatalogSeatKind) -> Unit,
    onAvanti: () -> Unit,
    onBackIntro: () -> Unit,
    onBackS0: () -> Unit,
    onGoStep: (WizardStep) -> Unit,
    onConfirmLocal: () -> Unit,
    onFolderPathChange: (String) -> Unit,
    onShareNameChange: (String) -> Unit,
    onPrepareScript: () -> Unit,
    onCredentialsText: () -> String,
    onConfirmScriptRan: () -> Unit,
    onUsbCopiedContinue: () -> Unit,
    onContinueFromSummary: () -> Unit,
    onContinueToCifs: () -> Unit,
    onRefreshCifs: () -> Unit,
    onRetryVerify: () -> Unit,
    onFolderPicked: (uri: String, label: String) -> Unit,
    onDone: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val openTree = rememberLauncherForActivityResult(
        contract = OpenDocumentTreeAdvanced(),
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
            WizardStep.S0_START -> S0StartStep(
                seatConfigured = state.seatConfigured,
                seatLabel = state.seatLabel,
                canResumeDraft = state.canResumeDraft,
                resumeStepLabel = state.resumeStepLabel,
                onStartCreate = onStartCreate,
                onStartModify = onStartModify,
                onResumeDraft = onResumeDraft,
                onDiscardDraft = onDiscardDraft,
                onCancel = onCancel,
            )
            WizardStep.INTRO -> IntroStep(
                selected = state.selectedKind,
                onSelectKind = onSelectKind,
                onAvanti = onAvanti,
                onCancel = onBackS0,
            )
            WizardStep.W1_CONFIRM -> SimpleGuide(
                body = stringResource(R.string.hub_w1_body),
                primary = stringResource(R.string.hub_w1_confirm),
                onPrimary = onConfirmLocal,
                onBack = onBackIntro,
                busy = state.busy,
            )
            WizardStep.W2_CHOICE -> W2ChoiceStep(
                onRoutine = { onGoStep(WizardStep.W2_ROUTINE_PARAMS) },
                onAlready = { onGoStep(WizardStep.W2_PREPARE) },
                onBack = onBackIntro,
            )
            WizardStep.W2_ROUTINE_PARAMS -> W2RoutineParamsStep(
                folderPath = state.folderPath,
                shareName = state.shareName,
                onFolderPathChange = onFolderPathChange,
                onShareNameChange = onShareNameChange,
                onPrepare = onPrepareScript,
                onBack = onBackIntro,
                busy = state.busy,
            )
            WizardStep.W2_ROUTINE_READY -> W2RoutineReadyStep(
                state = state,
                onContinue = onUsbCopiedContinue,
                onBack = { onGoStep(WizardStep.W2_ROUTINE_PARAMS) },
                onReprepare = onPrepareScript,
                busy = state.busy,
            )
            WizardStep.W2_ROUTINE_CONFIRM -> SimpleGuide(
                body = stringResource(R.string.hub_w2_routine_confirm_body),
                primary = stringResource(R.string.hub_w2_routine_confirm_primary),
                onPrimary = onConfirmScriptRan,
                onBack = { onGoStep(WizardStep.W2_ROUTINE_READY) },
            )
            WizardStep.W2_SUMMARY -> W2SummaryNoPasswordStep(
                state = state,
                onContinue = onContinueFromSummary,
                onBack = { onGoStep(WizardStep.W2_ROUTINE_CONFIRM) },
            )
            WizardStep.W2_PREPARE -> SimpleGuide(
                body = stringResource(R.string.hub_w2_prepare),
                primary = stringResource(R.string.hub_w2_prepared),
                onPrimary = onContinueToCifs,
                onBack = onBackIntro,
            )
            WizardStep.W2_CIFS -> W2CifsStep(
                state = state,
                showCopyCredentials = !state.scriptPrepared,
                onInstallOrOpen = {
                    onRefreshCifs()
                    if (NetworkDriveAssistant.isHelperInstalled(context)) {
                        if (!NetworkDriveAssistant.openHelperApp(context)) {
                            NetworkDriveAssistant.openHelperStore(context)
                        }
                    } else {
                        NetworkDriveAssistant.openHelperStore(context)
                    }
                },
                onCopyCredentials = {
                    copyToClipboard(context, onCredentialsText())
                },
                onRefresh = onRefreshCifs,
                onContinue = { onGoStep(WizardStep.W2_PICK) },
                onBack = {
                    when {
                        state.scriptPrepared -> onGoStep(WizardStep.W2_SUMMARY)
                        else -> onGoStep(WizardStep.W2_PREPARE)
                    }
                },
                onHelp = { onGoStep(WizardStep.W2_HELP) },
            )
            WizardStep.W2_HELP -> SimpleGuide(
                body = stringResource(R.string.hub_w2_help_body),
                primary = stringResource(R.string.hub_w2_help_back),
                onPrimary = { onGoStep(WizardStep.W2_CIFS) },
                onBack = onBackIntro,
                secondary = stringResource(R.string.hub_w2_password_help_link),
                onSecondary = { onGoStep(WizardStep.W2_PASSWORD_HELP) },
            )
            WizardStep.W2_PASSWORD_HELP -> SimpleGuide(
                body = stringResource(R.string.hub_w2_password_help_body),
                primary = stringResource(R.string.hub_w2_help_back),
                onPrimary = { onGoStep(WizardStep.W2_CIFS) },
                onBack = { onGoStep(WizardStep.W2_HELP) },
            )
            WizardStep.W2_PICK -> W2VerifyPickStep(
                state = state,
                onChooseFolder = { if (!state.busy) openTree.launch(null) },
                onRetry = onRetryVerify,
                onBackCifs = { onGoStep(WizardStep.W2_CIFS) },
                onBack = { onGoStep(WizardStep.W2_CIFS) },
            )
            WizardStep.W3_GUIDE -> SimpleGuide(
                body = stringResource(R.string.hub_w3_body),
                primary = stringResource(R.string.hub_choose_folder),
                onPrimary = { if (!state.busy) openTree.launch(null) },
                onBack = onBackIntro,
                busy = state.busy,
            )
            WizardStep.W4_GUIDE -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.hub_w4_body))
                Text(stringResource(R.string.hub_w4_cifs_note))
                if (state.cifsInstalled) {
                    Button(
                        onClick = {
                            onRefreshCifs()
                            NetworkDriveAssistant.openHelperApp(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.hub_cifs_open))
                    }
                } else {
                    Button(
                        onClick = {
                            NetworkDriveAssistant.openHelperStore(context)
                            onRefreshCifs()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.hub_cifs_install))
                    }
                }
                SimpleGuide(
                    body = stringResource(R.string.hub_w4_pick_hint),
                    primary = stringResource(R.string.hub_choose_folder),
                    onPrimary = { if (!state.busy) openTree.launch(null) },
                    onBack = onBackIntro,
                    busy = state.busy,
                )
            }
            WizardStep.DONE -> W2DoneStep(
                state = state,
                onDone = onDone,
            )
        }

        state.statusMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.primary)
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
private fun S0StartStep(
    seatConfigured: Boolean,
    seatLabel: String,
    canResumeDraft: Boolean,
    resumeStepLabel: String,
    onStartCreate: () -> Unit,
    onStartModify: () -> Unit,
    onResumeDraft: () -> Unit,
    onDiscardDraft: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(stringResource(R.string.hub_s0_body))
    if (seatConfigured) {
        Text(
            text = stringResource(R.string.hub_s0_current_seat, seatLabel.ifBlank { "—" }),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    } else {
        Text(
            text = stringResource(R.string.hub_s0_no_seat),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
    }
    if (canResumeDraft) {
        Text(
            text = stringResource(R.string.hub_s0_resume_hint, resumeStepLabel),
            style = MaterialTheme.typography.bodyMedium,
        )
        Button(onClick = onResumeDraft, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.hub_s0_resume))
        }
        TextButton(onClick = onDiscardDraft) {
            Text(stringResource(R.string.hub_s0_discard_draft))
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
    Button(onClick = onStartCreate, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.hub_s0_create))
    }
    OutlinedButton(
        onClick = onStartModify,
        modifier = Modifier.fillMaxWidth(),
        enabled = seatConfigured,
    ) {
        Text(stringResource(R.string.hub_s0_modify))
    }
    TextButton(onClick = onCancel) {
        Text(stringResource(R.string.importa_annulla))
    }
}

@Composable
private fun IntroStep(
    selected: CatalogSeatKind,
    onSelectKind: (CatalogSeatKind) -> Unit,
    onAvanti: () -> Unit,
    onCancel: () -> Unit,
) {
    Text(stringResource(R.string.hub_s1_body))
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
private fun W2ChoiceStep(
    onRoutine: () -> Unit,
    onAlready: () -> Unit,
    onBack: () -> Unit,
) {
    Text(stringResource(R.string.hub_w2_choice_body))
    Button(onClick = onRoutine, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.hub_w2_choice_routine))
    }
    OutlinedButton(onClick = onAlready, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.hub_w2_choice_already))
    }
    TextButton(onClick = onBack) {
        Text(stringResource(R.string.hub_indietro))
    }
}

@Composable
private fun W2RoutineParamsStep(
    folderPath: String,
    shareName: String,
    onFolderPathChange: (String) -> Unit,
    onShareNameChange: (String) -> Unit,
    onPrepare: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
) {
    Text(stringResource(R.string.hub_s2_pc_body))
    OutlinedTextField(
        value = folderPath,
        onValueChange = onFolderPathChange,
        label = { Text(stringResource(R.string.hub_w2_folder_path_label)) },
        supportingText = { Text(stringResource(R.string.hub_s2_pc_path_hint)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !busy,
    )
    OutlinedTextField(
        value = shareName,
        onValueChange = onShareNameChange,
        label = { Text(stringResource(R.string.hub_w2_share_name_label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !busy,
    )
    Text(
        stringResource(R.string.hub_s2_pc_note),
        style = MaterialTheme.typography.bodySmall,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(onClick = onPrepare, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_w2_prepare_script))
        }
    }
}

@Composable
private fun W2RoutineReadyStep(
    state: CatalogSeatWizardUiState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onReprepare: () -> Unit,
    busy: Boolean,
) {
    Text(stringResource(R.string.hub_s3a_ready_body))
    Text(stringResource(R.string.hub_w2_routine_ready_body))
    Text(stringResource(R.string.hub_s3a_summary_title), fontWeight = FontWeight.Medium)
    Text(
        text = stringResource(
            R.string.hub_s3a_summary_lines,
            state.folderPath,
            state.shareName,
            state.userName,
        ),
        fontFamily = FontFamily.Monospace,
    )
    Text(
        stringResource(R.string.hub_s3a_password_later),
        style = MaterialTheme.typography.bodySmall,
    )
    TextButton(onClick = onReprepare, enabled = !busy) {
        Text(stringResource(R.string.hub_w2_reprepare_script))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(onClick = onContinue, enabled = !busy && state.scriptPrepared, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_s3b_copied_primary))
        }
    }
}

@Composable
private fun W2VerifyPickStep(
    state: CatalogSeatWizardUiState,
    onChooseFolder: () -> Unit,
    onRetry: () -> Unit,
    onBackCifs: () -> Unit,
    onBack: () -> Unit,
) {
    Text(
        if (state.scriptPrepared) {
            stringResource(R.string.hub_s6_pick_body)
        } else {
            stringResource(R.string.hub_w2_pick)
        },
    )
    val statusText = when (state.verification) {
        SeatVerificationUi.NONE -> stringResource(R.string.hub_s6_status_idle)
        SeatVerificationUi.IN_PROGRESS -> stringResource(R.string.hub_s6_status_progress)
        SeatVerificationUi.VERIFIED -> stringResource(R.string.hub_s6_status_ok)
        SeatVerificationUi.UNREACHABLE -> stringResource(R.string.hub_s6_status_unreachable)
        SeatVerificationUi.FAILED -> stringResource(R.string.hub_s6_status_failed)
    }
    val statusColor = when (state.verification) {
        SeatVerificationUi.VERIFIED -> MaterialTheme.colorScheme.primary
        SeatVerificationUi.UNREACHABLE,
        SeatVerificationUi.FAILED,
        -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }
    Text(statusText, color = statusColor, fontWeight = FontWeight.Medium)
    if (state.pendingLabel.isNotBlank() && state.verification != SeatVerificationUi.NONE) {
        Text(
            text = state.pendingLabel,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
    Button(
        onClick = onChooseFolder,
        enabled = !state.busy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_choose_folder))
    }
    if (state.pendingTreeUri != null &&
        (state.verification == SeatVerificationUi.FAILED ||
            state.verification == SeatVerificationUi.UNREACHABLE)
    ) {
        OutlinedButton(
            onClick = onRetry,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.hub_s6_retry))
        }
    }
    if (state.scriptPrepared) {
        TextButton(onClick = onBackCifs, enabled = !state.busy) {
            Text(stringResource(R.string.hub_s6_back_cifs))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, enabled = !state.busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun W2DoneStep(
    state: CatalogSeatWizardUiState,
    onDone: () -> Unit,
) {
    Text(
        if (state.scriptPrepared) {
            stringResource(R.string.hub_s7_title)
        } else {
            stringResource(R.string.hub_done_title)
        },
        fontWeight = FontWeight.Medium,
    )
    if (state.scriptPrepared && state.verification == SeatVerificationUi.VERIFIED) {
        Text(
            stringResource(R.string.hub_s7_verified_line),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(
                R.string.hub_s7_summary_lines,
                state.folderPath.ifBlank { WindowsCatalogSetupScript.DEFAULT_FOLDER },
                state.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
                state.userName.ifBlank { WindowsCatalogSetupScript.DEFAULT_USER },
                state.completedSeat?.displayLabel ?: state.seatLabel.ifBlank { "—" },
            ),
            fontFamily = FontFamily.Monospace,
        )
        Text(
            stringResource(R.string.hub_s7_no_password),
            style = MaterialTheme.typography.bodySmall,
        )
        Text(stringResource(R.string.hub_w2_done_password_reminder))
    } else {
        Text(
            stringResource(
                R.string.hub_done_body,
                state.completedSeat?.displayLabel ?: "—",
            ),
        )
    }
    Text(stringResource(R.string.hub_done_resume_catalog))
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.ok))
    }
}

@Composable
private fun W2SummaryNoPasswordStep(
    state: CatalogSeatWizardUiState,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Text(stringResource(R.string.hub_s4_summary_body))
    Text(
        text = stringResource(
            R.string.hub_s4_summary_lines,
            state.folderPath.ifBlank { WindowsCatalogSetupScript.DEFAULT_FOLDER },
            state.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE },
            state.userName.ifBlank { WindowsCatalogSetupScript.DEFAULT_USER },
        ),
        fontFamily = FontFamily.Monospace,
    )
    Text(
        stringResource(R.string.hub_s3a_password_later),
        style = MaterialTheme.typography.bodySmall,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(onClick = onContinue, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_s4_continue_cifs))
        }
    }
}

@Composable
private fun W2CifsStep(
    state: CatalogSeatWizardUiState,
    showCopyCredentials: Boolean,
    onInstallOrOpen: () -> Unit,
    onCopyCredentials: () -> Unit,
    onRefresh: () -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
) {
    Text(
        if (state.scriptPrepared) {
            stringResource(R.string.hub_s5_cifs_body)
        } else {
            stringResource(R.string.hub_w2_cifs_body)
        },
    )
    Text(stringResource(R.string.hub_w2_credentials_title), fontWeight = FontWeight.Medium)
    Text(
        text = stringResource(
            if (state.scriptPrepared) R.string.hub_s5_cifs_fields else R.string.hub_w2_cifs_fields,
            state.userName.ifBlank { "MediaManager" },
            state.temporaryPassword.ifBlank { "(quella dello script o dell'account Windows)" },
            state.shareName.ifBlank { "MediaManagerCatalog" },
        ),
        fontFamily = FontFamily.Monospace,
    )
    if (showCopyCredentials) {
        OutlinedButton(onClick = onCopyCredentials, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.hub_w2_copy_credentials))
        }
    }
    Button(onClick = onInstallOrOpen, modifier = Modifier.fillMaxWidth()) {
        Text(
            if (state.cifsInstalled) {
                stringResource(R.string.hub_cifs_open)
            } else {
                stringResource(R.string.hub_cifs_install)
            },
        )
    }
    TextButton(onClick = onRefresh) {
        Text(stringResource(R.string.hub_cifs_refresh))
    }
    Text(
        if (state.cifsInstalled) {
            stringResource(R.string.hub_cifs_status_ready)
        } else {
            stringResource(R.string.hub_cifs_status_missing)
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (state.cifsInstalled) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        },
    )
    TextButton(onClick = onHelp) {
        Text(stringResource(R.string.hub_w2_help_link))
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(
            onClick = onContinue,
            enabled = state.cifsInstalled,
            modifier = Modifier.weight(1f),
        ) {
            Text(stringResource(R.string.hub_w2_cifs_done))
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

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("MediaManager SMB", text))
}
