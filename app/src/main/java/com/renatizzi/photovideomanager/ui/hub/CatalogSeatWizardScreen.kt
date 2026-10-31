package com.renatizzi.photovideomanager.ui.hub

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.style.TextOverflow
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
    onCatalogNameChange: (String) -> Unit = {},
    onDeviceModelChange: (String) -> Unit = {},
    onProcedureMode: (ProcedureMode) -> Unit = {},
    onAvanti: () -> Unit,
    onContinuaAvvio: () -> Unit = onAvanti,
    onBackIntro: () -> Unit,
    onBackS0: () -> Unit,
    onGoStep: (WizardStep) -> Unit,
    onConfirmLocal: () -> Unit,
    onFolderPathChange: (String) -> Unit,
    onConfirmAvailability: () -> Unit,
    onPcShareAlreadyReady: () -> Unit,
    onPcShareNeedsPrepare: () -> Unit,
    onConfirmUsbAndPrepare: () -> Unit,
    onConfirmCopy: () -> Unit,
    onConfirmScriptRan: () -> Unit,
    onRetryRecoverPcData: () -> Unit,
    onPcHostIpChange: (String) -> Unit,
    onCredentialsText: () -> String,
    onPasswordText: () -> String,
    onFieldText: (CifsCopyField) -> String,
    onRefreshCifs: () -> Unit,
    onRetryVerify: () -> Unit,
    onFolderPicked: (uri: String, label: String) -> Unit,
    onAccessYes: () -> Unit,
    onAccessNo: () -> Unit,
    onDone: () -> Unit,
    onGoToAcquire: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    check(WizardUiContract.DEVICE_SELECTION_USES_DROPDOWN) {
        "Passo 0 deve usare un menu a tendina (WizardUiContract)"
    }
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
        Text(
            text = WizardStepChrome.subtitle(state.step, state.selectedKind).text,
            style = MaterialTheme.typography.titleMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
        )

        when (state.step) {
            WizardStep.S0_START -> AvvioProceduraStep(
                state = state,
                onCatalogNameChange = onCatalogNameChange,
                onSelectKind = onSelectKind,
                onDeviceModelChange = onDeviceModelChange,
                onContinua = onContinuaAvvio,
                onResumeDraft = onResumeDraft,
                onDiscardDraft = onDiscardDraft,
                onCancel = onCancel,
                // compat unused
                onStartCreate = onStartCreate,
                onStartModify = onStartModify,
            )
            WizardStep.INTRO -> IntroStep(
                selected = state.selectedKind,
                folderPath = state.folderPath,
                onSelectKind = onSelectKind,
                onFolderPathChange = onFolderPathChange,
                onOk = onAvanti,
                onCancel = onBackS0,
                busy = state.busy,
            )
            WizardStep.W1_CONFIRM -> FolderChoiceStep(
                body = stringResource(R.string.hub_w1_body),
                pendingLabel = state.pendingLabel,
                catalogName = state.catalogName,
                deviceModel = state.deviceModel,
                memoryLabel = state.memoryLabel,
                busy = state.busy,
                onChooseFolder = { if (!state.busy) openTree.launch(null) },
                onSalva = onConfirmLocal,
                onBack = onBackS0,
                onEsc = onCancel,
            )
            WizardStep.PC_SHARE_READY -> PcShareReadyStep(
                kind = state.selectedKind,
                procedureMode = state.procedureMode,
                onProcedureMode = onProcedureMode,
                onAvvia = {
                    when (state.procedureMode) {
                        ProcedureMode.RAPIDA -> onPcShareAlreadyReady()
                        ProcedureMode.GUIDATA -> onPcShareNeedsPrepare()
                    }
                },
                onBack = onBackS0,
                busy = state.busy,
            )
            WizardStep.PC_AVAILABILITY,
            WizardStep.W2_CHOICE,
            WizardStep.W2_ROUTINE_PARAMS,
            -> AvailabilityChecklistStep(
                onOk = onConfirmAvailability,
                onBack = { onGoStep(WizardStep.PC_SHARE_READY) },
                busy = state.busy,
            )
            WizardStep.PC_USB_PREPARE -> UsbPrepareChecklistStep(
                onOk = onConfirmUsbAndPrepare,
                onBack = { onGoStep(WizardStep.PC_AVAILABILITY) },
                busy = state.busy,
            )
            WizardStep.PC_COPY_FILES,
            WizardStep.W2_ROUTINE_READY,
            -> CopyFilesChecklistStep(
                onOk = onConfirmCopy,
                onBack = { onGoStep(WizardStep.PC_USB_PREPARE) },
                busy = state.busy,
                scriptPrepared = state.scriptPrepared,
            )
            WizardStep.PC_RUN_SCRIPT,
            WizardStep.W2_SCRIPT_FIND,
            WizardStep.W2_SCRIPT_LAUNCH,
            WizardStep.W2_SCRIPT_RESULT,
            WizardStep.W2_ROUTINE_CONFIRM,
            -> ScriptChecklistStep(
                onOk = onConfirmScriptRan,
                onBack = { onGoStep(WizardStep.PC_COPY_FILES) },
                busy = state.busy,
            )
            WizardStep.W2_CIFS,
            WizardStep.W2_PICK,
            WizardStep.W2_CIFS_BRIDGE,
            WizardStep.W2_SUMMARY,
            WizardStep.W2_CIFS_METHOD,
            WizardStep.W2_PREPARE,
            WizardStep.W2_HELP,
            WizardStep.W2_PASSWORD_HELP,
            -> CifsCardStep(
                state = state,
                onCopyField = { field ->
                    val value = onFieldText(field)
                    if (value.isNotBlank()) copyToClipboard(context, value)
                },
                onCopyPassword = {
                    val pwd = onPasswordText()
                    if (pwd.isNotBlank()) copyToClipboard(context, pwd)
                },
                onRetryRecoverPcData = onRetryRecoverPcData,
                onPcHostIpChange = onPcHostIpChange,
                onOpenCifs = {
                    onRefreshCifs()
                    if (NetworkDriveAssistant.isHelperInstalled(context)) {
                        if (!NetworkDriveAssistant.openHelperApp(context)) {
                            NetworkDriveAssistant.openHelperStore(context)
                        }
                    } else {
                        NetworkDriveAssistant.openHelperStore(context)
                    }
                },
                onChooseFolder = { if (!state.busy) openTree.launch(null) },
                onRetry = onRetryVerify,
                onAccessYes = {
                    onAccessYes()
                    onGoToAcquire()
                },
                onAccessNo = onAccessNo,
                onSalva = onConfirmLocal,
                onBack = {
                    onGoStep(
                        if (state.procedureMode == ProcedureMode.RAPIDA) {
                            WizardStep.PC_SHARE_READY
                        } else {
                            WizardStep.PC_RUN_SCRIPT
                        },
                    )
                },
            )
            WizardStep.W3_GUIDE -> FolderChoiceStep(
                body = stringResource(R.string.hub_folder_choice_desc_cloud),
                pendingLabel = state.pendingLabel,
                catalogName = state.catalogName,
                deviceModel = state.deviceModel.ifBlank { "Google Drive" },
                memoryLabel = "",
                busy = state.busy,
                onChooseFolder = { if (!state.busy) openTree.launch(null) },
                onSalva = onConfirmLocal,
                onBack = onBackS0,
                onEsc = onCancel,
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
                    onBack = { onGoStep(WizardStep.PC_SHARE_READY) },
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
private fun QuestionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun AvvioProceduraStep(
    state: CatalogSeatWizardUiState,
    onCatalogNameChange: (String) -> Unit,
    onSelectKind: (CatalogSeatKind) -> Unit,
    onDeviceModelChange: (String) -> Unit,
    onContinua: () -> Unit,
    onResumeDraft: () -> Unit,
    onDiscardDraft: () -> Unit,
    onCancel: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onStartCreate: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onStartModify: () -> Unit,
) {
    Text(
        text = stringResource(R.string.hub_avvio_desc),
        style = MaterialTheme.typography.bodyMedium,
    )
    if (state.seatConfigured) {
        Text(
            text = stringResource(
                R.string.hub_s0_current_seat,
                state.catalogName.ifBlank { state.seatLabel }.ifBlank { "—" },
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
    }
    if (state.canResumeDraft) {
        Text(stringResource(R.string.hub_s0_resume_hint, state.resumeStepLabel))
        OutlinedButton(onClick = onResumeDraft, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.hub_s0_resume, state.resumeStepLabel))
        }
        TextButton(onClick = onDiscardDraft) {
            Text(stringResource(R.string.hub_s0_discard_draft))
        }
        Spacer(modifier = Modifier.height(4.dp))
    }

    QuestionText(stringResource(R.string.hub_avvio_name_q))
    OutlinedTextField(
        value = state.catalogName,
        onValueChange = onCatalogNameChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.hub_avvio_name_default)) },
    )

    QuestionText(stringResource(R.string.hub_avvio_device_q))
    DeviceKindDropdown(
        selected = state.selectedKind,
        onSelectKind = onSelectKind,
        enabled = !state.busy,
    )

    QuestionText(stringResource(R.string.hub_avvio_model_q))
    OutlinedTextField(
        value = state.deviceModel,
        onValueChange = onDeviceModelChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.hub_avvio_model_hint)) },
    )

    Button(
        onClick = onContinua,
        enabled = !state.busy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_avvio_continua))
    }
    TextButton(onClick = onCancel, enabled = !state.busy) {
        Text(stringResource(R.string.hub_avvio_esc))
    }
}

/**
 * Passo 0: menu a tendina chiuso + blocco variabile per dispositivo + OK.
 * Le voci NON sono pulsanti uno sotto l'altro.
 */
@Composable
private fun IntroStep(
    selected: CatalogSeatKind,
    folderPath: String,
    onSelectKind: (CatalogSeatKind) -> Unit,
    onFolderPathChange: (String) -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit,
    busy: Boolean,
) {
    var localVolume by remember { mutableStateOf(LocalVolume.PRIMARY) }
    Text(stringResource(R.string.hub_s1_body))
    DeviceKindDropdown(
        selected = selected,
        onSelectKind = onSelectKind,
        enabled = !busy,
    )
    when (selected) {
        CatalogSeatKind.PC_NOTEBOOK -> {
            Text(
                stringResource(R.string.hub_s0_pc_guided_intro),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(R.string.hub_s0_pc_procedure_label),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.hub_s0_pc_procedure_steps),
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedTextField(
                value = folderPath,
                onValueChange = onFolderPathChange,
                label = { Text(stringResource(R.string.hub_w2_folder_path_label)) },
                supportingText = {
                    Text(
                        "${stringResource(R.string.hub_s0_valori_modificabili)}. " +
                            stringResource(R.string.hub_s2_pc_path_hint),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !busy,
            )
        }
        CatalogSeatKind.LOCAL_DEVICE -> {
            Text(
                stringResource(R.string.hub_s0_local_volume_intro),
                style = MaterialTheme.typography.bodyMedium,
            )
            LocalVolumeDropdown(
                selected = localVolume,
                onSelect = { localVolume = it },
                enabled = !busy,
            )
            Text(
                stringResource(R.string.hub_s0_stub_procedure_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }
        CatalogSeatKind.CLOUD_DRIVE,
        CatalogSeatKind.NETWORK_NAS,
        -> {
            Text(
                stringResource(R.string.hub_s0_stub_procedure_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }
        CatalogSeatKind.NONE -> Unit
    }
    Text(
        stringResource(R.string.hub_s0_when_ready),
        style = MaterialTheme.typography.bodyMedium,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onCancel, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(onClick = onOk, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_checklist_ok))
        }
    }
}

private enum class LocalVolume { PRIMARY, SECONDARY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceKindDropdown(
    selected: CatalogSeatKind,
    onSelectKind: (CatalogSeatKind) -> Unit,
    enabled: Boolean,
) {
    check(WizardUiContract.DEVICE_SELECTION_USES_DROPDOWN)
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        CatalogSeatKind.LOCAL_DEVICE to R.string.hub_kind_local,
        CatalogSeatKind.PC_NOTEBOOK to R.string.hub_kind_pc,
        CatalogSeatKind.CLOUD_DRIVE to R.string.hub_kind_drive,
        CatalogSeatKind.NETWORK_NAS to R.string.hub_kind_nas,
    )
    val selectedLabel = stringResource(
        options.firstOrNull { it.first == selected }?.second ?: R.string.hub_kind_pc,
    )
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.hub_combo_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (kind, labelRes) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    onClick = {
                        onSelectKind(kind)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalVolumeDropdown(
    selected: LocalVolume,
    onSelect: (LocalVolume) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        LocalVolume.PRIMARY -> stringResource(R.string.hub_s0_volume_primary)
        LocalVolume.SECONDARY -> stringResource(R.string.hub_s0_volume_secondary)
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(stringResource(R.string.hub_s0_volume_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = enabled)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.hub_s0_volume_primary)) },
                onClick = {
                    onSelect(LocalVolume.PRIMARY)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.hub_s0_volume_secondary)) },
                onClick = {
                    onSelect(LocalVolume.SECONDARY)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun AvailabilityChecklistStep(
    onOk: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
) {
    Text(stringResource(R.string.hub_pc1_intro))
    val items = listOf(
        WizardChecklistItem(stringResource(R.string.hub_pc1_item_phone)),
        WizardChecklistItem(stringResource(R.string.hub_pc1_item_pc)),
        WizardChecklistItem(stringResource(R.string.hub_pc1_item_cable)),
    )
    ChecklistOkBlock(items = items, onOk = onOk, onBack = onBack, busy = busy)
}

@Composable
private fun UsbPrepareChecklistStep(
    onOk: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
) {
    Text(stringResource(R.string.hub_pc2_intro))
    val items = listOf(
        WizardChecklistItem(stringResource(R.string.hub_pc2_item_unlocked)),
        WizardChecklistItem(stringResource(R.string.hub_pc2_item_connect)),
        WizardChecklistItem(
            label = stringResource(R.string.hub_pc2_item_transfer),
            nestedBullets = listOf(
                WizardNestedBullet(stringResource(R.string.hub_pc2_bullet_notif)),
                WizardNestedBullet(stringResource(R.string.hub_pc2_bullet_tap)),
                WizardNestedBullet(stringResource(R.string.hub_pc2_bullet_choose)),
                WizardNestedBullet(stringResource(R.string.hub_pc2_bullet_allow)),
            ),
        ),
    )
    ChecklistOkBlock(items = items, onOk = onOk, onBack = onBack, busy = busy)
}

@Composable
private fun CopyFilesChecklistStep(
    onOk: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
    scriptPrepared: Boolean,
) {
    Text(stringResource(R.string.hub_pc3_intro))
    val items = listOf(
        WizardChecklistItem(
            label = stringResource(R.string.hub_pc3_item_download),
            nestedBullets = listOf(
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_explorer)),
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_thispc)),
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_phone)),
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_dbl)),
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_mem)),
            ),
        ),
        WizardChecklistItem(
            label = stringResource(R.string.hub_pc3_item_copy),
            nestedBullets = listOf(
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_search)),
                WizardNestedBullet(
                    text = stringResource(R.string.hub_pc3_b_file1),
                    marker = "✓",
                    extraStart = 16.dp,
                ),
                WizardNestedBullet(
                    text = stringResource(R.string.hub_pc3_b_file2),
                    marker = "✓",
                    extraStart = 16.dp,
                ),
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_ctrlc)),
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_ctrlv)),
            ),
        ),
        WizardChecklistItem(
            label = stringResource(R.string.hub_pc3_item_verify),
            nestedBullets = listOf(
                WizardNestedBullet(stringResource(R.string.hub_pc3_b_verify)),
            ),
        ),
    )
    ChecklistOkBlock(
        items = items,
        onOk = onOk,
        onBack = onBack,
        busy = busy,
        okEnabledExtra = scriptPrepared,
        showWhenReadyHint = true,
    )
}

@Composable
private fun ScriptChecklistStep(
    onOk: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
) {
    Text(stringResource(R.string.hub_pc4_intro))
    val items = listOf(
        WizardChecklistItem(stringResource(R.string.hub_pc4_item_1)),
        WizardChecklistItem(stringResource(R.string.hub_pc4_item_2)),
        WizardChecklistItem(stringResource(R.string.hub_pc4_item_3)),
    )
    ChecklistOkBlock(items = items, onOk = onOk, onBack = onBack, busy = busy)
}

@Composable
private fun ChecklistOkBlock(
    items: List<WizardChecklistItem>,
    onOk: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
    okEnabledExtra: Boolean = true,
    showWhenReadyHint: Boolean = true,
    showOk: Boolean = true,
) {
    var flags by remember(items.size) { mutableStateOf(BooleanArray(items.size) { false }) }
    WizardChecklistBlock(
        items = items,
        checked = flags,
        onToggle = { index ->
            flags = flags.copyOf().also { it[index] = !it[index] }
        },
        enabled = !busy,
    )
    if (showWhenReadyHint) {
        Text(
            stringResource(R.string.hub_s0_when_ready),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        if (showOk) {
            Button(
                onClick = onOk,
                enabled = !busy && okEnabledExtra && allChecklistChecked(flags),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.hub_checklist_ok))
            }
        }
    }
}

@Composable
private fun PcShareReadyStep(
    kind: CatalogSeatKind,
    procedureMode: ProcedureMode,
    onProcedureMode: (ProcedureMode) -> Unit,
    onAvvia: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean,
) {
    val intro = if (kind == CatalogSeatKind.NETWORK_NAS) {
        stringResource(R.string.hub_pc_share_ready_intro_nas)
    } else {
        stringResource(R.string.hub_pc_share_ready_intro)
    }
    Text(intro)
    Text(
        stringResource(R.string.hub_pc_share_ready_question),
        fontWeight = FontWeight.SemiBold,
    )
    ProcedureModeDropdown(
        selected = procedureMode,
        onSelect = onProcedureMode,
        enabled = !busy,
    )
    Button(
        onClick = onAvvia,
        enabled = !busy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_procedure_avvia))
    }
    TextButton(onClick = onBack, enabled = !busy) {
        Text(stringResource(R.string.hub_indietro))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProcedureModeDropdown(
    selected: ProcedureMode,
    onSelect: (ProcedureMode) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (selected) {
        ProcedureMode.RAPIDA -> stringResource(R.string.hub_procedure_rapida)
        ProcedureMode.GUIDATA -> stringResource(R.string.hub_procedure_guidata)
    }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            label = { Text(stringResource(R.string.hub_pc_share_ready_question)) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.hub_procedure_rapida)) },
                onClick = {
                    onSelect(ProcedureMode.RAPIDA)
                    expanded = false
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.hub_procedure_guidata)) },
                onClick = {
                    onSelect(ProcedureMode.GUIDATA)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun FolderChoiceStep(
    body: String,
    pendingLabel: String,
    catalogName: String,
    deviceModel: String,
    memoryLabel: String,
    busy: Boolean,
    onChooseFolder: () -> Unit,
    onSalva: () -> Unit,
    onBack: () -> Unit,
    onEsc: () -> Unit,
) {
    Text(body)
    if (pendingLabel.isNotBlank()) {
        val detail = buildString {
            append(catalogName.ifBlank { "MediaManagerCatalog" })
            append(" · ")
            if (deviceModel.isNotBlank()) {
                append(deviceModel)
                append(", ")
            }
            if (memoryLabel.isNotBlank()) {
                append(memoryLabel)
                append(", ")
            }
            append(pendingLabel)
        }
        Text(
            text = stringResource(R.string.catalog_seat_row_name, detail),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
    }
    Button(
        onClick = onChooseFolder,
        enabled = !busy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_choose_folder))
    }
    Button(
        onClick = onSalva,
        enabled = !busy && pendingLabel.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_salva))
    }
    OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.hub_indietro))
    }
    TextButton(onClick = onEsc, enabled = !busy) {
        Text(stringResource(R.string.hub_avvio_esc))
    }
}

@Composable
private fun CifsCardStep(
    state: CatalogSeatWizardUiState,
    onCopyField: (CifsCopyField) -> Unit,
    onCopyPassword: () -> Unit,
    onRetryRecoverPcData: () -> Unit,
    onPcHostIpChange: (String) -> Unit = {},
    onOpenCifs: () -> Unit,
    onChooseFolder: () -> Unit,
    onRetry: () -> Unit,
    onAccessYes: () -> Unit,
    onAccessNo: () -> Unit,
    onSalva: () -> Unit,
    onBack: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    // Name, Storage, Host, Port, User, Password, Anonymous, Folder
    var rowFlags by remember { mutableStateOf(BooleanArray(8) { false }) }
    var cifsSavedConfirmed by remember { mutableStateOf(false) }
    @Suppress("UNUSED_VARIABLE", "UNUSED_PARAMETER")
    val unusedAccess = onAccessYes to onAccessNo
    val share = state.shareName.ifBlank { WindowsCatalogSetupScript.DEFAULT_SHARE }
    val user = state.cifsUserQualified.trim().ifBlank {
        WindowsCatalogSetupScript.cifsUserCopyText(state.userName)
    }
    val hostValue = state.pcHostIp.trim().ifBlank {
        stringResource(R.string.hub_b5b_host_placeholder)
    }
    val password = state.temporaryPassword
    val hasPassword = password.isNotBlank()
    val hostCopyable = state.pcHostIp.isNotBlank()
    val readyForPick = state.cifsInstalled && hasPassword && cifsSavedConfirmed

    Text(stringResource(R.string.hub_cifs_tobe_desc))
    Text(
        stringResource(R.string.hub_pc5_share_visibility),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
    )
    // Stato recupero IP: Riprova sempre accessibile (§D / F2), anche durante recoveringPcData.
    Text(
        text = when {
            state.recoveringPcData -> stringResource(R.string.hub_pc5_recovering)
            state.setupResultImported -> stringResource(R.string.hub_pc5_result_imported)
            else -> stringResource(R.string.hub_pc5_result_missing)
        },
        style = MaterialTheme.typography.bodySmall,
        color = when {
            state.recoveringPcData || state.setupResultImported ->
                MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
        },
    )
    OutlinedButton(
        onClick = onRetryRecoverPcData,
        enabled = !state.busy,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_pc5_retry_recover))
    }
    OutlinedTextField(
        value = state.pcHostIp,
        onValueChange = onPcHostIpChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !state.busy,
        label = { Text(stringResource(R.string.hub_b5b_host_edit_label)) },
        supportingText = { Text(stringResource(R.string.hub_b5b_host_edit_hint)) },
    )
    Text(
        stringResource(R.string.hub_b5b_card_title),
        fontWeight = FontWeight.Medium,
    )
    // Ordine = form CIFS «Connection» (screenshot completo)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_id),
            value = stringResource(R.string.hub_b5b_id_value),
            actionLabel = null,
            onAction = null,
            checked = null,
            onCheckedChange = null,
            hint = stringResource(R.string.hub_b5b_id_hint),
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_name),
            value = share,
            actionLabel = stringResource(R.string.hub_copy),
            onAction = { onCopyField(CifsCopyField.NAME) },
            checked = rowFlags[0],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[0] = !it[0] } },
            hint = stringResource(R.string.hub_b5b_name_hint),
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_storage),
            value = stringResource(R.string.hub_b5b_storage_value),
            actionLabel = stringResource(R.string.hub_pc5_confirm),
            onAction = { rowFlags = rowFlags.copyOf().also { it[1] = true } },
            checked = rowFlags[1],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[1] = !it[1] } },
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_domain),
            value = stringResource(R.string.hub_b5b_domain_value),
            actionLabel = null,
            onAction = null,
            checked = null,
            onCheckedChange = null,
            hint = stringResource(R.string.hub_b5b_domain_hint),
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_host),
            value = hostValue,
            actionLabel = stringResource(R.string.hub_copy),
            onAction = if (hostCopyable) {
                { onCopyField(CifsCopyField.HOST) }
            } else {
                null
            },
            checked = rowFlags[2],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[2] = !it[2] } },
            hint = stringResource(
                if (hostCopyable) R.string.hub_b5b_host_hint_ready else R.string.hub_b5b_host_hint,
            ),
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_port),
            value = "445",
            actionLabel = stringResource(R.string.hub_copy),
            onAction = { onCopyField(CifsCopyField.PORT) },
            checked = rowFlags[3],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[3] = !it[3] } },
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_dfs),
            value = stringResource(R.string.hub_b5b_dfs_value),
            actionLabel = null,
            onAction = null,
            checked = null,
            onCheckedChange = null,
            hint = stringResource(R.string.hub_b5b_dfs_hint),
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_user),
            value = user,
            actionLabel = stringResource(R.string.hub_copy),
            onAction = { onCopyField(CifsCopyField.USERNAME) },
            checked = rowFlags[4],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[4] = !it[4] } },
            hint = stringResource(R.string.hub_b5b_user_hint),
            enabled = !state.busy,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Checkbox(
                checked = rowFlags[5],
                onCheckedChange = {
                    if (!state.busy) {
                        rowFlags = rowFlags.copyOf().also { it[5] = !it[5] }
                    }
                },
                enabled = !state.busy,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.hub_b5b_label_password),
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = when {
                        !hasPassword -> "—"
                        passwordVisible -> password
                        else -> "•".repeat(password.length.coerceAtMost(16))
                    },
                    fontFamily = FontFamily.Monospace,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (hasPassword) {
                TextButton(
                    onClick = { passwordVisible = !passwordVisible },
                    enabled = !state.busy,
                ) {
                    Text(
                        stringResource(
                            if (passwordVisible) R.string.hub_hide else R.string.hub_show,
                        ),
                    )
                }
                TextButton(onClick = onCopyPassword, enabled = !state.busy) {
                    Text(stringResource(R.string.hub_copy))
                }
            }
        }
        if (!hasPassword) {
            Text(
                stringResource(R.string.hub_password_missing_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_anonymous),
            value = stringResource(R.string.hub_b5b_anonymous_value),
            actionLabel = stringResource(R.string.hub_pc5_confirm),
            onAction = { rowFlags = rowFlags.copyOf().also { it[6] = true } },
            checked = rowFlags[6],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[6] = !it[6] } },
            hint = stringResource(R.string.hub_b5b_anonymous_hint),
            enabled = !state.busy,
        )
        CifsFieldRow(
            label = stringResource(R.string.hub_b5b_label_folder),
            value = stringResource(R.string.hub_b5b_folder_value),
            actionLabel = stringResource(R.string.hub_pc5_confirm),
            onAction = { rowFlags = rowFlags.copyOf().also { it[7] = true } },
            checked = rowFlags[7],
            onCheckedChange = { rowFlags = rowFlags.copyOf().also { it[7] = !it[7] } },
            hint = stringResource(R.string.hub_b5b_folder_hint),
            enabled = !state.busy,
        )
        Text(
            stringResource(R.string.hub_pc5_options_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
    }

    Button(onClick = onOpenCifs, modifier = Modifier.fillMaxWidth(), enabled = !state.busy) {
        Text(
            if (state.cifsInstalled) {
                stringResource(R.string.hub_cifs_open)
            } else {
                stringResource(R.string.hub_cifs_install)
            },
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !state.busy) { cifsSavedConfirmed = !cifsSavedConfirmed },
        verticalAlignment = Alignment.Top,
    ) {
        Checkbox(
            checked = cifsSavedConfirmed,
            onCheckedChange = { if (!state.busy) cifsSavedConfirmed = it },
            enabled = !state.busy,
        )
        Text(
            text = stringResource(R.string.hub_pc5_item_saved),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp, top = 12.dp),
        )
    }

    Text(
        stringResource(R.string.hub_pc5_ultima_azione),
        style = MaterialTheme.typography.bodyMedium,
    )
    Button(
        onClick = onChooseFolder,
        enabled = !state.busy && readyForPick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.hub_pc5_activate_link))
    }

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
    if (state.verification != SeatVerificationUi.NONE) {
        Text(statusText, color = statusColor, fontWeight = FontWeight.Medium)
    }
    if (state.pendingLabel.isNotBlank() && state.verification != SeatVerificationUi.NONE) {
        Text(
            text = state.pendingLabel,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
    if (state.pendingTreeUri != null && state.pendingLabel.isNotBlank()) {
        Text(
            text = stringResource(R.string.hub_folder_summary_title),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = state.pendingLabel,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
        Button(
            onClick = onSalva,
            enabled = !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.hub_salva))
        }
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

    OutlinedButton(onClick = onBack, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.hub_indietro))
    }
}

@Composable
private fun CifsFieldRow(
    label: String,
    value: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    checked: Boolean?,
    onCheckedChange: (() -> Unit)?,
    hint: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (checked != null && onCheckedChange != null) {
            Checkbox(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange() },
                enabled = enabled,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, fontFamily = FontFamily.Monospace)
            if (hint != null) {
                Text(
                    hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
        }
        if (actionLabel != null) {
            TextButton(
                onClick = { onAction?.invoke() },
                enabled = enabled && onAction != null,
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun W2DoneStep(
    state: CatalogSeatWizardUiState,
    onDone: () -> Unit,
) {
    Text(
        stringResource(R.string.hub_done_title),
        fontWeight = FontWeight.Medium,
    )
    val name = state.completedSeat?.effectiveCatalogName
        ?: state.catalogName.ifBlank { "MediaManagerCatalog" }
    Text(
        stringResource(R.string.hub_done_body, name),
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
    )
    if (state.completedSeat?.displayLabel?.isNotBlank() == true) {
        Text(
            text = state.completedSeat.displayLabel,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
    Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.hub_done_torna_accesso))
    }
}

@Composable
private fun QuestionGuide(
    intro: String,
    question: String?,
    primary: String,
    onPrimary: () -> Unit,
    onBack: () -> Unit,
    busy: Boolean = false,
) {
    Text(intro)
    if (question != null) QuestionText(question)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onBack, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.hub_indietro))
        }
        Button(onClick = onPrimary, enabled = !busy, modifier = Modifier.weight(1f)) {
            Text(primary)
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
) {
    Text(body)
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
    cm.setPrimaryClip(ClipData.newPlainText("MediaManager CIFS", text))
}
