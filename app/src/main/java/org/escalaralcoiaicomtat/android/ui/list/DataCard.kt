package org.escalaralcoiaicomtat.android.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.network.NetworkObserver.Companion.rememberNetworkObserver
import org.escalaralcoiaicomtat.android.storage.data.DataEntity
import org.escalaralcoiaicomtat.android.storage.files.FilesCrate
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import timber.log.Timber

@Composable
fun <T: DataEntity> DataCard(
    item: T,
    imageHeight: Dp,
    modifier: Modifier = Modifier,
    onFavoriteToggle: () -> Job?,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?
) {
    val context = LocalContext.current

    val networkObserver = rememberNetworkObserver()
    val isNetworkAvailable by networkObserver.isNetworkAvailable.observeAsState()

    val filesCrate = FilesCrate.rememberInstance()

    val imageFile by item.rememberImageFile()
    var progress by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    var imageSize by remember { mutableStateOf<IntSize?>(null) }

    var isDownloading by remember { mutableStateOf(false) }
    var isTogglingFavorite by remember { mutableStateOf(false) }

    val isDownloaded by FilesCrate.getInstance(context)
        .existsLive(item.imageUUID)

    var isShowingDeleteDialog by remember { mutableStateOf(false) }
    if (isShowingDeleteDialog)
        AlertDialog(
            onDismissRequest = { isShowingDeleteDialog = false },
            title = { Text(stringResource(R.string.dialog_delete_title)) },
            text = { Text(stringResource(R.string.dialog_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        filesCrate.permanent(item.imageUUID).delete()

                        isShowingDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { isShowingDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )

    OutlinedCard(
        modifier = modifier.clickable(
            enabled = imageFile != null || isNetworkAvailable == true,
            onClick = onClick
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(ListItemDefaults.HeaderHeight)
        ) {
            Text(
                text = item.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 20.sp
            )

            onEdit?.let {
                IconButton(onClick = it) {
                    Icon(
                        Icons.Rounded.Edit,
                        stringResource(R.string.list_item_edit)
                    )
                }
            }

            IconButton(
                enabled = !isDownloading && imageFile != null,
                onClick = {
                    // If clicked, isDownloaded is not null
                    if (isDownloaded) {
                        isShowingDeleteDialog = true
                    } else {
                        isDownloading = true

                        CoroutineScope(Dispatchers.IO).launch {
                            val cache = filesCrate.cache(item.imageUUID)
                            val permanent = filesCrate.permanent(item.imageUUID)

                            Timber.d("Copying area's (${item.id}) data cache to permanent storage ($permanent)...")

                            if (!cache.exists()) {
                                Timber.w("Cache ($cache) doesn't exist.")
                                return@launch
                            }

                            cache.copyTo(permanent)
                        }.invokeOnCompletion {
                            Timber.d("Copied area (${item.id}) successfully.")
                            isDownloading = false
                        }
                    }
                }
            ) {
                Icon(
                    if (isDownloaded)
                        Icons.Rounded.DownloadDone
                    else if (isDownloading)
                        Icons.Rounded.Downloading
                    else
                        Icons.Rounded.Download,
                    stringResource(R.string.list_item_download)
                )
            }

            IconButton(
                onClick = {
                    isTogglingFavorite = true
                    onFavoriteToggle()
                        ?.invokeOnCompletion { isTogglingFavorite = false }
                        ?: run { isTogglingFavorite = false }
                },
                enabled = !isTogglingFavorite,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (item.isFavorite)
                        MaterialTheme.colorScheme.primary
                    else
                        Color.Unspecified,
                    contentColor = if (item.isFavorite)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onBackground
                )
            ) {
                AnimatedContent(
                    targetState = item,
                    label = "animate-favorite"
                ) {
                    Icon(
                        if (it.isFavorite)
                            Icons.Rounded.Star
                        else
                            Icons.Rounded.StarBorder,
                        stringResource(R.string.list_item_favorite)
                    )
                }
            }
        }

        LaunchedEffect(imageSize, item) {
            withContext(Dispatchers.IO) {
                if (imageFile != null) return@withContext
                if (imageSize == null) return@withContext

                // Get the display's width
                val width = imageSize?.width

                item.updateImageIfNeeded(context, width) { current, max ->
                    progress = current.toInt() to max.toInt()
                }

                // TODO - if bitmap is null, show error
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .onGloballyPositioned { imageSize = it.size },
            contentAlignment = Alignment.Center
        ) {
            imageFile?.let { file ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(file)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: CircularProgressIndicator(progress)
        }
    }
}
