package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Filled.Carabiner: ImageVector
    get() {
        if (_carabiner != null) {
            return _carabiner!!
        }
        _carabiner = ImageVector.Builder(
            name = "Carabiner",
            defaultWidth = 200.0.dp,
            defaultHeight = 200.0.dp,
            viewportWidth = 200.0F,
            viewportHeight = 200.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(13.8125F, 2.0F)
            curveTo(8.414063F, 2.0F, 4.0F, 6.414063F, 4.0F, 11.8125F)
            lineTo(4.0F, 12.3125F)
            curveTo(4.0F, 13.414063F, 6.0F, 13.007813F, 7.0F, 12.40625F)
            curveTo(7.601563F, 12.007813F, 7.992188F, 11.386719F, 8.09375F, 10.6875F)
            curveTo(8.09375F, 10.488281F, 6.0F, 11.0F, 6.0F, 11.0F)
            curveTo(6.0F, 10.898438F, 8.40625F, 9.699219F, 8.40625F, 9.5F)
            curveTo(9.304688F, 7.5F, 11.414063F, 6.0F, 13.8125F, 6.0F)
            lineTo(14.5F, 6.0F)
            curveTo(15.300781F, 6.0F, 16.0F, 6.699219F, 16.0F, 7.5F)
            lineTo(16.0F, 15.6875F)
            curveTo(16.0F, 16.988281F, 14.988281F, 18.0F, 13.6875F, 18.0F)
            lineTo(13.1875F, 18.0F)
            curveTo(12.886719F, 18.898438F, 12.414063F, 19.90625F, 11.8125F, 20.90625F)
            curveTo(11.613281F, 21.207031F, 11.300781F, 21.492188F, 11.0F, 21.59375F)
            curveTo(12.0F, 21.894531F, 13.0F, 22.0F, 14.0F, 22.0F)
            curveTo(17.398438F, 21.898438F, 20.0F, 19.085938F, 20.0F, 15.6875F)
            lineTo(20.0F, 7.5F)
            curveTo(20.0F, 4.5F, 17.5F, 2.0F, 14.5F, 2.0F)

            moveTo(11.40625F, 11.0F)
            curveTo(11.136719F, 10.996094F, 10.800781F, 11.074219F, 10.3125F, 11.1875F)
            curveTo(9.414063F, 11.488281F, 8.8125F, 11.613281F, 9.3125F, 13.3125F)
            curveTo(9.3125F, 13.3125F, 10.011719F, 16.011719F, 8.3125F, 18.3125F)
            curveTo(7.8125F, 19.011719F, 7.988281F, 20.09375F, 8.6875F, 20.59375F)
            curveTo(8.886719F, 20.894531F, 9.207031F, 21.0F, 9.40625F, 21.0F)
            curveTo(10.007813F, 21.101562F, 10.605469F, 20.8125F, 10.90625F, 20.3125F)
            curveTo(13.707031F, 16.3125F, 12.507813F, 12.511719F, 12.40625F, 12.3125F)
            curveTo(12.09375F, 11.3125F, 11.855469F, 11.007813F, 11.40625F, 11.0F)

            moveTo(9.8125F, 18.40625F)
            curveTo(10.3125F, 18.507812F, 10.601563F, 18.90625F, 10.5F, 19.40625F)
            curveTo(10.398438F, 19.90625F, 10.0F, 20.289062F, 9.5F, 20.1875F)
            curveTo(9.0F, 20.085938F, 8.710938F, 19.6875F, 8.8125F, 19.1875F)
            curveTo(8.914063F, 18.6875F, 9.3125F, 18.40625F, 9.8125F, 18.40625F)

            close()
        }.build()
        return _carabiner!!
    }
private var _carabiner: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconCarabinerPreview() {
    Image(imageVector = Icons.Filled.Carabiner, contentDescription = null)
}
