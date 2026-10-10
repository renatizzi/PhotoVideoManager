package com.renatizzi.photovideomanager.ui.restore

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R

/**
 * Atterraggio Ripristina (Nota §5.4.3): Cestino + ripristino da Backup.
 * Il Backup resta in preparazione fino a specifica M15.
 */
@Composable
fun RipristinaLandingScreen(
    onOpenTrash: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_ripristina),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.ripristina_landing_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
        )
        Button(
            onClick = onOpenTrash,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ripristina_open_trash))
        }
        OutlinedButton(
            onClick = onOpenBackupRestore,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.ripristina_open_backup))
        }
        Text(
            text = stringResource(R.string.ripristina_backup_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        )
    }
}
