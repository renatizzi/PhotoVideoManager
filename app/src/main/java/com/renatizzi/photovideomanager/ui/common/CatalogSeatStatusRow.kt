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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatReachability
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatRowFormatter
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatStatus

/**
 * Riga compatta Catalogo — Dashboard, Acquisisci, Aggiorna, Componi.
 * Formato TO-BE: «Catalogo: nome» + dettaglio dispositivo/path/stato.
 * Tap apre Setup Catalogo (Avvio procedura).
 */
@Composable
fun CatalogSeatStatusRow(
    status: CatalogSeatStatus?,
    onOpenSeat: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val reach = status?.reachability ?: CatalogSeatReachability.UNKNOWN
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSeat)
            .padding(vertical = if (compact) 2.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when {
            status == null || reach == CatalogSeatReachability.NOT_CONFIGURED ||
                status.configured.not() -> {
                Text(
                    text = stringResource(R.string.catalog_seat_row_missing),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                )
            }
            else -> {
                Text(
                    text = stringResource(
                        R.string.catalog_seat_row_name,
                        status.catalogName,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                )
                val detail = CatalogSeatRowFormatter.detailLine(status)
                val detailColor = when (reach) {
                    CatalogSeatReachability.REACHABLE -> MaterialTheme.colorScheme.secondary
                    CatalogSeatReachability.UNREACHABLE -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                }
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = detailColor,
                )
            }
        }
    }
}
