package com.renatizzi.photovideomanager.ui.clean

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.DuplicateGroup
import com.renatizzi.photovideomanager.domain.model.DuplicateMember
import com.renatizzi.photovideomanager.domain.model.MediaKind
import com.renatizzi.photovideomanager.ui.common.MediaThumbnail

@Composable
fun CleanScreen(
    state: CleanUiState,
    onAnalyze: () -> Unit,
    onRequestTrash: (DuplicateGroup) -> Unit,
    onConfirmTrash: () -> Unit,
    onDismissTrash: () -> Unit,
    onOpenTrash: () -> Unit,
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
            text = stringResource(R.string.clean_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.clean_intro_v2),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.clean_inline_help_v2),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
            )
        }

        Button(
            onClick = onAnalyze,
            enabled = !state.analyzing && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (state.analyzing) {
                    stringResource(R.string.clean_analyzing)
                } else {
                    stringResource(R.string.clean_analyze)
                },
            )
        }

        OutlinedButton(
            onClick = onOpenTrash,
            enabled = !state.analyzing && !state.busy,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.clean_open_trash))
        }

        if (state.analyzing || state.busy) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        if (state.hasAnalyzed && !state.analyzing) {
            Text(
                text = stringResource(
                    R.string.clean_stats,
                    state.groups.size,
                    state.copiesScanned,
                    state.hashesComputed,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        HorizontalDivider()

        Text(
            text = stringResource(R.string.clean_groups_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )

        if (state.hasAnalyzed && state.groups.isEmpty() && !state.analyzing) {
            Text(
                text = stringResource(R.string.clean_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            )
        }

        state.groups.forEachIndexed { index, group ->
            DuplicateGroupBlock(
                index = index + 1,
                group = group,
                enabled = !state.busy && !state.analyzing,
                onTrashExtras = { onRequestTrash(group) },
            )
            HorizontalDivider()
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    val pending = state.pendingTrashGroup
    if (pending != null) {
        AlertDialog(
            onDismissRequest = onDismissTrash,
            title = { Text(stringResource(R.string.clean_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.clean_confirm_body,
                        pending.extraItemCount,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmTrash) {
                    Text(stringResource(R.string.clean_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissTrash) {
                    Text(stringResource(R.string.clean_confirm_no))
                }
            },
        )
    }
}

@Composable
private fun DuplicateGroupBlock(
    index: Int,
    group: DuplicateGroup,
    enabled: Boolean,
    onTrashExtras: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(
                R.string.clean_group_header,
                index,
                group.distinctItemCount,
                group.extraItemCount,
            ),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(
                R.string.clean_group_hash,
                group.fingerprintValue.take(12),
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        )
        group.members.forEach { member ->
            MemberRow(member)
        }
        OutlinedButton(
            onClick = onTrashExtras,
            enabled = enabled && group.extraItemCount > 0,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.clean_trash_extras))
        }
    }
}

@Composable
private fun MemberRow(member: DuplicateMember) {
    val title = member.mediaItem.displayTitle?.ifBlank { null }
        ?: member.mediaItem.id
    Row(
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MediaThumbnail(
            copy = member.mediaCopy,
            kind = member.mediaItem.kind,
            size = 48.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = kindLabel(member.mediaItem.kind),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.clean_member_location, member.locationName),
                style = MaterialTheme.typography.bodySmall,
            )
            if (member.isSuggestedKeep) {
                Text(
                    text = stringResource(R.string.clean_suggested_keep),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun kindLabel(kind: MediaKind): String = when (kind) {
    MediaKind.PHOTO -> stringResource(R.string.media_kind_photo)
    MediaKind.VIDEO -> stringResource(R.string.media_kind_video)
}
