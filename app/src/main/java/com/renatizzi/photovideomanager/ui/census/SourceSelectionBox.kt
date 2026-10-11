package com.renatizzi.photovideomanager.ui.census

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.SourceCensusSelection

/**
 * Riquadro selezione fonti a 3 stati (BL-03): ✓ / vuoto / X.
 * Un solo controllo — niente icona cestino separata.
 */
@Composable
fun SourceSelectionBox(
    selection: SourceCensusSelection,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (selection) {
        SourceCensusSelection.SELECTED -> stringResource(R.string.acquisisci_state_selected_cd)
        SourceCensusSelection.NOT_SELECTED -> stringResource(R.string.acquisisci_state_not_selected_cd)
        SourceCensusSelection.EXCLUDED -> stringResource(R.string.acquisisci_state_excluded_cd)
    }
    val mark = when (selection) {
        SourceCensusSelection.SELECTED -> "✓"
        SourceCensusSelection.NOT_SELECTED -> ""
        SourceCensusSelection.EXCLUDED -> "X"
    }
    val borderColor = when (selection) {
        SourceCensusSelection.SELECTED -> MaterialTheme.colorScheme.primary
        SourceCensusSelection.NOT_SELECTED ->
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        SourceCensusSelection.EXCLUDED -> MaterialTheme.colorScheme.error
    }
    val markColor = when (selection) {
        SourceCensusSelection.SELECTED -> MaterialTheme.colorScheme.primary
        SourceCensusSelection.NOT_SELECTED -> MaterialTheme.colorScheme.onSurface
        SourceCensusSelection.EXCLUDED -> MaterialTheme.colorScheme.error
    }
    Box(
        modifier = modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(2.dp, borderColor, RoundedCornerShape(4.dp))
            .semantics {
                contentDescription = label
                role = Role.Checkbox
            }
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mark,
            color = markColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
