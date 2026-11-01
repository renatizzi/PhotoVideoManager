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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatReachability
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatRowFormatter
import com.renatizzi.photovideomanager.domain.hub.CatalogSeatStatus

/**
 * Riga compatta Catalogo — Dashboard, Acquisisci, Aggiorna, Componi.
 * Formato TO-BE 2 linee (C1). Tap apre Setup Catalogo.
 *
 * @param showWhenMissing se false (Dashboard/Componi) non mostra testo assenza.
 */
@Composable
fun CatalogSeatStatusRow(
    status: CatalogSeatStatus?,
    onOpenSeat: () -> Unit,
    compact: Boolean = false,
    showWhenMissing: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val reach = status?.reachability ?: CatalogSeatReachability.UNKNOWN
    val missing = status == null ||
        reach == CatalogSeatReachability.NOT_CONFIGURED ||
        status.configured.not()
    if (missing && !showWhenMissing) {
        return
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSeat)
            .padding(vertical = if (compact) 2.dp else 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when {
            missing -> {
                Text(
                    text = stringResource(R.string.catalog_seat_row_missing),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                )
            }
            else -> {
                val nameColor = MaterialTheme.colorScheme.primary
                val labelColor = MaterialTheme.colorScheme.onSurface
                Text(
                    text = buildAnnotatedString {
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = labelColor,
                            ),
                        ) {
                            append(stringResource(R.string.catalog_seat_row_title))
                            append(": ")
                        }
                        withStyle(
                            SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                                color = nameColor,
                            ),
                        ) {
                            append(status!!.catalogName)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                val detail = CatalogSeatRowFormatter.detailLine(status)
                val detailColor = when (reach) {
                    CatalogSeatReachability.REACHABLE -> MaterialTheme.colorScheme.primary
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
