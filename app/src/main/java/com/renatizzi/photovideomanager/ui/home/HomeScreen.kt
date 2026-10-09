package com.renatizzi.photovideomanager.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renatizzi.photovideomanager.R
import com.renatizzi.photovideomanager.domain.model.DashboardSnapshot
import com.renatizzi.photovideomanager.ui.common.formatBytes

/**
 * Dashboard densità stile BoxManager: titolo grande, KPI a due colonne senza card,
 * ricerca in riquadro, accesso rapido a tessere che riempiono lo spazio restante.
 * Nessuno scrolling.
 */
@Composable
fun HomeScreen(
    snapshot: DashboardSnapshot?,
    onOpenTab: (com.renatizzi.photovideomanager.ui.shell.ShellTab) -> Unit,
    onOpenFeature: (String) -> Unit,
    onSearchCatalog: (String) -> Unit = { onOpenFeature("ricerca") },
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val photos = snapshot?.photoCount?.toString() ?: "0"
    val videos = snapshot?.videoCount?.toString() ?: "0"
    val acquiredPhotos = (snapshot?.acquiredPhotoCount ?: 0L).toString()
    val acquiredVideos = (snapshot?.acquiredVideoCount ?: 0L).toString()
    val photoSpace = formatBytes(snapshot?.photoUsedBytes ?: 0L)
    val videoSpace = formatBytes(snapshot?.videoUsedBytes ?: 0L)
    val dupPhotos = (snapshot?.duplicatePhotoCount ?: 0L).toString()
    val dupVideos = (snapshot?.duplicateVideoCount ?: 0L).toString()
    val accent = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 34.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.dashboard_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = labelColor,
        )

        Spacer(modifier = Modifier.height(10.dp))

        // KPI stile BoxManager: due colonne con intestazione, etichetta› + numero grande
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KpiColumnHeader(
                    icon = Icons.Outlined.Image,
                    title = stringResource(R.string.kpi_col_foto),
                    tint = accent,
                )
                KpiMetric(
                    label = stringResource(R.string.kpi_foto_originali),
                    value = photos,
                    detail = stringResource(R.string.kpi_di_cui_acquisite, acquiredPhotos),
                    labelColor = labelColor,
                    onClick = { onOpenFeature("ricerca") },
                )
                KpiMetric(
                    label = stringResource(R.string.kpi_spazio_foto),
                    value = photoSpace,
                    detail = stringResource(R.string.kpi_di_cui_duplicati, dupPhotos),
                    labelColor = labelColor,
                    onClick = { onOpenFeature("pulisci") },
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KpiColumnHeader(
                    icon = Icons.Outlined.Videocam,
                    title = stringResource(R.string.kpi_col_video),
                    tint = accent,
                )
                KpiMetric(
                    label = stringResource(R.string.kpi_video_originali),
                    value = videos,
                    detail = stringResource(R.string.kpi_di_cui_acquisiti, acquiredVideos),
                    labelColor = labelColor,
                    onClick = { onOpenFeature("ricerca") },
                )
                KpiMetric(
                    label = stringResource(R.string.kpi_spazio_video),
                    value = videoSpace,
                    detail = stringResource(R.string.kpi_di_cui_duplicati, dupVideos),
                    labelColor = labelColor,
                    onClick = { onOpenFeature("pulisci") },
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            icon = Icons.Outlined.Search,
            title = stringResource(R.string.feature_ricerca),
            tint = accent,
        )
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .height(52.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.dashboard_search_hint),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Outlined.Search,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                focusManager.clearFocus()
                                onSearchCatalog(searchQuery)
                            },
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Outlined.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        focusManager.clearFocus()
                        onSearchCatalog(searchQuery)
                    },
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f))
        Spacer(modifier = Modifier.height(8.dp))

        SectionHeader(
            icon = Icons.Outlined.Bolt,
            title = stringResource(R.string.quick_access),
            tint = accent,
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Tessere grandi che riempiono tutto lo spazio restante (come BoxManager)
        val quick = listOf(
            Triple("acquisisci", R.string.feature_acquisisci, Icons.Outlined.AddAPhoto),
            Triple("aggiorna", R.string.feature_aggiorna, Icons.Outlined.Sync),
            Triple("crea", R.string.feature_crea, Icons.Outlined.CreateNewFolder),
            Triple("edita", R.string.feature_edita, Icons.Outlined.Edit),
            Triple("backup", R.string.feature_backup, Icons.Outlined.CloudUpload),
            Triple("ripristina", R.string.feature_ripristina, Icons.Outlined.SettingsBackupRestore),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            quick.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { (id, titleRes, icon) ->
                        QuickAccessTile(
                            title = stringResource(titleRes),
                            icon = icon,
                            onClick = { onOpenFeature(id) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    tint: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun KpiColumnHeader(
    icon: ImageVector,
    title: String,
    tint: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun KpiMetric(
    label: String,
    value: String,
    detail: String,
    labelColor: Color,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 36.sp,
            ),
            maxLines = 1,
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QuickAccessTile(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(26.dp),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
