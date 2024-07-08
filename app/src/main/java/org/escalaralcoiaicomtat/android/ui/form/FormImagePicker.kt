package org.escalaralcoiaicomtat.android.ui.form

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ImageSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import org.escalaralcoiaicomtat.android.R

@Composable
fun FormImagePicker(
    image: Bitmap?,
    contentDescription: String?,
    isLoadingImage: Flow<Boolean>,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .width(400.dp)
            .height(200.dp)
            .padding(vertical = 8.dp)
    ) {
        image?.let { img ->
            Image(
                bitmap = img.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
            )
        } ?: run {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val loading by isLoadingImage.collectAsState(initial = false)

                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Icon(
                        imageVector = Icons.Rounded.ImageSearch,
                        contentDescription = stringResource(R.string.form_image_pick)
                    )
                }
            }
        }
    }
}
