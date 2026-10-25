package com.renatizzi.photovideomanager.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatKindLabels
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatReachability
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatStatus

/**
 * Riga compatta «Posizione del Catalogo» — stesso contratto su Dashboard, Organizza,
 * Acquisisci e Aggiorna. Tap apre l’ingresso unico (Wizard / Setup).
 */
@Composable
fun CatalogSeatStatusRow(
    status: CatalogSeatStatus?,
    onOpenSeat: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val reach = status?.reachability ?: CatalogSeatReachability.UNKNOWN
    val label = status?.label?.takeIf { it.isNotBlank() }
    val kind = status?.let { CatalogSeatKindLabels.label(it.kind) }.orEmpty()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSeat)
            .padding(vertical = if (compact) 2.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        if (!compact) {
            Text(
                text = stringResource(R.string.catalog_seat_row_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
        }
        when (reach) {
            CatalogSeatReachability.NOT_CONFIGURED -> {
                Text(
                    text = stringResource(R.string.catalog_seat_row_missing),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
            }
            else -> {
                val line = buildString {
                    append(label ?: "—")
                    if (kind.isNotBlank()) {
                        append(" · ")
                        append(kind)
                    }
                }
                Text(
                    text = if (compact) {
                        stringResource(R.string.catalog_seat_row_compact, line)
                    } else {
                        line
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                val (reachText, reachColor) = when (reach) {
                    CatalogSeatReachability.REACHABLE ->
                        stringResource(R.string.acquisisci_catalog_ref_reachable) to
                            MaterialTheme.colorScheme.secondary
                    CatalogSeatReachability.UNREACHABLE ->
                        stringResource(R.string.acquisisci_catalog_ref_unreachable) to
                            MaterialTheme.colorScheme.error
                    CatalogSeatReachability.UNKNOWN,
                    CatalogSeatReachability.NOT_CONFIGURED,
                    ->
                        stringResource(R.string.acquisisci_catalog_ref_unknown) to
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                }
                Text(
                    text = reachText,
                    style = MaterialTheme.typography.labelMedium,
                    color = reachColor,
                )
            }
        }
    }
}
