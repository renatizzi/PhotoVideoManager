package com.renatizzi.photovideomanager.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrokenImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Size
import com.renatizzi.photovideomanager.domain.model.MediaCopy
import com.renatizzi.photovideomanager.domain.model.MediaKind

val LocalThumbnailResolver = staticCompositionLocalOf<ThumbnailResolver> {
    error("ThumbnailResolver non fornito")
}

@Composable
fun MediaThumbnail(
    copy: MediaCopy?,
    kind: MediaKind,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val resolver = LocalThumbnailResolver.current
    var model by remember(copy?.id) { mutableStateOf<Any?>(null) }
    LaunchedEffect(copy?.id) {
        model = copy?.let { resolver.modelFor(it) }
    }
    MediaThumbnailModel(
        model = model,
        kind = kind,
        modifier = modifier,
        size = size,
    )
}

@Composable
fun MediaThumbnailModel(
    model: Any?,
    kind: MediaKind,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (model == null) {
            PlaceholderIcon(kind = kind)
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .size(Size(256, 256))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            ) {
                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    is AsyncImagePainter.State.Error -> {
                        PlaceholderIcon(kind = kind, broken = true)
                    }
                    else -> {
                        SubcomposeAsyncImageContent()
                        if (kind == MediaKind.VIDEO) {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(22.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderIcon(kind: MediaKind, broken: Boolean = false) {
    Icon(
        imageVector = when {
            broken -> Icons.Outlined.BrokenImage
            kind == MediaKind.VIDEO -> Icons.Outlined.PlayCircle
            else -> Icons.Outlined.Image
        },
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        modifier = Modifier.size(24.dp),
    )
}
