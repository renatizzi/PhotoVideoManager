package com.renatizzi.photovideomanager.ui.hub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Bullet sotto una voce checklist (• oppure ✓ con indentazione extra). */
data class WizardNestedBullet(
    val text: String,
    val marker: String = "•",
    val extraStart: Dp = 0.dp,
)

/**
 * Voce checklist: etichetta + eventuali bullet indentati (come nei template PPT).
 * La spunta riguarda la voce principale; i bullet sono solo istruzioni.
 */
data class WizardChecklistItem(
    val label: String,
    val nestedBullets: List<WizardNestedBullet> = emptyList(),
)

/**
 * Checklist a dichiarazione (UIX): spunte osservabili → OK gated.
 * Non è prova automatica; la prova reale resta SAF.
 */
@Composable
fun WizardChecklistBlock(
    items: List<WizardChecklistItem>,
    checked: BooleanArray,
    onToggle: (index: Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items.forEachIndexed { index, item ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = enabled) { onToggle(index) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Checkbox(
                        checked = checked.getOrElse(index) { false },
                        onCheckedChange = { if (enabled) onToggle(index) },
                        enabled = enabled,
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp),
                    )
                }
                if (item.nestedBullets.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(start = 48.dp, end = 8.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        item.nestedBullets.forEach { bullet ->
                            Text(
                                text = "${bullet.marker} ${bullet.text}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier.padding(start = bullet.extraStart),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Compatibilità: lista semplice senza bullet annidati. */
@Composable
fun WizardChecklistBlock(
    items: List<Pair<String, Boolean>>,
    onToggle: (index: Int) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    WizardChecklistBlock(
        items = items.map { WizardChecklistItem(it.first) },
        checked = BooleanArray(items.size) { i -> items[i].second },
        onToggle = onToggle,
        enabled = enabled,
        modifier = modifier,
    )
}

fun allChecklistChecked(flags: BooleanArray): Boolean =
    flags.isNotEmpty() && flags.all { it }
