package org.escalaralcoiaicomtat.android.ui.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Downloading
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.network.NetworkObserver.Companion.rememberNetworkObserver
import org.escalaralcoiaicomtat.android.storage.AppDatabase
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.ImageEntity
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.files.FilesCrate
import org.escalaralcoiaicomtat.android.ui.reusable.CircularProgressIndicator
import timber.log.Timber

@Composable
fun <T: ImageEntity> DataCard(
    item: T,
    imageHeight: Dp,
    childCount: UInt,
    modifier: Modifier = Modifier,
    onFavoriteToggle: () -> Job?,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?
) {
    val context = LocalContext.current
    val localDensity = LocalDensity.current

    val networkObserver = rememberNetworkObserver()
    val isNetworkAvailable by networkObserver.collectIsNetworkAvailable()

    val filesCrate = FilesCrate.rememberInstance()
    val appDatabase = AppDatabase.rememberInstance()
    val userDao = appDatabase.userDao()

    val imageFile by item.rememberImageFile().collectAsState(null)
    var progress by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var imageFileNotAvailable by remember { mutableStateOf(false) }

    var isDownloading by remember { mutableStateOf(false) }
    var isTogglingFavorite by remember { mutableStateOf(false) }

    val isDownloaded by filesCrate.rememberExistsPermanent(item.imageUUID)
    val isFavoriteLive = when (item) {
        is Area -> userDao.getAreaFlow(item.id)
        is Zone -> userDao.getSectorFlow(item.id)
        is Sector -> userDao.getSectorFlow(item.id)
        else -> throw IllegalArgumentException("Item type is not supported: ${item::class.simpleName}")
    }
    val isFavorite by isFavoriteLive.collectAsState(null)

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

    LaunchedEffect(item, isNetworkAvailable) {
        withContext(Dispatchers.IO) {
            if (imageFile != null) return@withContext

            // Reset state
            imageFileNotAvailable = false

            // Get the display's width
            val height = with(localDensity) { imageHeight.roundToPx() }

            val error = item.updateImageIfNeeded(context, height = height) { current, max ->
                progress = current.toInt() to max.toInt()
            }

            // TODO - if bitmap is null, show error
            if (error != null) {
                // Probably no Internet Connection, so the image of the card cannot be loaded.
                // in any case, show the placeholder
                imageFileNotAvailable = true
            }
        }
    }

    Column(
        modifier = modifier.clickable(
            enabled = !imageFileNotAvailable && (imageFile != null || isNetworkAvailable),
            onClick = onClick
        )
    ) {
        Text(
            text = item.displayName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontStyle = if (imageFileNotAvailable) FontStyle.Italic else FontStyle.Normal,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            fontSize = 20.sp
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight),
            contentAlignment = Alignment.Center
        ) {
            if (imageFileNotAvailable) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_launcher_monochrome),
                            contentDescription = null,
                            modifier = Modifier.size(.4 * imageHeight),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = stringResource(R.string.list_image_not_available),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            } else imageFile?.let { file ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(file)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.displayName,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            } ?: CircularProgressIndicator(progress)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (childCount > 0U) {
                AssistChip(
                    onClick = { /*TODO*/ },
                    label = {
                        Text(
                            text = item.labelWithCount(childCount.toInt()),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = {
                    isTogglingFavorite = true
                    onFavoriteToggle()
                        ?.invokeOnCompletion { isTogglingFavorite = false }
                        ?: run { isTogglingFavorite = false }
                },
                enabled = !isTogglingFavorite
            ) {
                AnimatedContent(
                    targetState = isFavorite,
                    label = "animate-favorite"
                ) { favorite ->
                    Icon(
                        if (favorite != null)
                            Icons.Rounded.Bookmark
                        else
                            Icons.Rounded.BookmarkBorder,
                        stringResource(R.string.list_item_favorite)
                    )
                }
            }

            IconButton(
                enabled = !isDownloading && imageFile != null,
                onClick = {
                    // If clicked, isDownloaded is not null
                    // TODO: Move logic to ViewModel
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

            if (onEdit != null) {
                var expanded by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Rounded.MoreVert, null)
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.list_item_edit)) },
                            onClick = onEdit
                        )
                    }
                }
            }
        }
    }
}
