package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Filled.Quickdraw: ImageVector
    get() {
        if (_quickdraw != null) {
            return _quickdraw!!
        }
        _quickdraw = ImageVector.Builder(
            name = "Quickdraw",
            defaultWidth = 200.0.dp,
            defaultHeight = 200.0.dp,
            viewportWidth = 200.0F,
            viewportHeight = 200.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(5.09375F, 2.0F)
            curveTo(3.394531F, 2.0F, 2.0F, 3.394531F, 2.0F, 5.09375F)
            curveTo(2.0F, 6.792969F, 3.3125F, 9.1875F, 4.8125F, 10.1875F)
            curveTo(5.914063F, 10.886719F, 6.710938F, 10.894531F, 7.3125F, 10.59375F)
            curveTo(8.011719F, 10.292969F, 8.105469F, 10.085938F, 7.90625F, 9.6875F)
            curveTo(7.804688F, 9.585938F, 7.695313F, 9.289063F, 7.59375F, 9.1875F)
            curveTo(7.492188F, 8.988281F, 7.292969F, 8.699219F, 6.59375F, 9.0F)
            lineTo(6.5F, 9.0F)
            curveTo(6.5F, 9.199219F, 6.707031F, 9.699219F, 6.90625F, 10.0F)
            curveTo(6.507813F, 9.699219F, 6.195313F, 9.207031F, 6.09375F, 8.90625F)
            curveTo(5.992188F, 8.90625F, 5.914063F, 8.8125F, 5.8125F, 8.8125F)
            curveTo(4.8125F, 8.113281F, 3.6875F, 6.289063F, 3.6875F, 5.1875F)
            curveTo(3.6875F, 4.488281F, 4.386719F, 3.6875F, 5.1875F, 3.6875F)
            lineTo(10.59375F, 3.6875F)
            curveTo(11.292969F, 3.6875F, 12.0F, 4.488281F, 12.0F, 5.1875F)
            curveTo(12.0F, 5.585938F, 11.789063F, 6.011719F, 11.6875F, 6.3125F)
            curveTo(11.585938F, 6.3125F, 11.886719F, 6.394531F, 12.1875F, 6.59375F)
            curveTo(12.488281F, 6.792969F, 12.804688F, 7.488281F, 12.40625F, 8.1875F)
            curveTo(13.304688F, 7.386719F, 13.6875F, 6.394531F, 13.6875F, 5.09375F)
            curveTo(13.6875F, 3.394531F, 12.292969F, 2.0F, 10.59375F, 2.0F)

            moveTo(15.0F, 3.0F)
            lineTo(15.0F, 5.0F)
            lineTo(16.0F, 5.0F)
            curveTo(17.65625F, 5.0F, 19.0F, 6.34375F, 19.0F, 8.0F)
            lineTo(19.0F, 9.0F)
            lineTo(21.0F, 9.0F)
            lineTo(21.0F, 8.0F)
            curveTo(21.0F, 5.253906F, 18.746094F, 3.0F, 16.0F, 3.0F)

            moveTo(8.4375F, 6.0625F)
            curveTo(7.4375F, 6.085938F, 6.738281F, 6.3125F, 6.6875F, 6.3125F)
            curveTo(5.886719F, 6.613281F, 5.894531F, 6.699219F, 6.09375F, 7.5F)
            curveTo(6.292969F, 7.898438F, 6.3125F, 8.300781F, 7.3125F, 8.0F)
            curveTo(7.3125F, 8.0F, 9.304688F, 7.398438F, 10.90625F, 8.5F)
            curveTo(11.304688F, 8.800781F, 11.886719F, 8.710938F, 12.1875F, 8.3125F)
            curveTo(12.289063F, 8.210938F, 12.304688F, 8.007813F, 12.40625F, 7.90625F)
            curveTo(12.40625F, 7.605469F, 12.300781F, 7.199219F, 12.0F, 7.0F)
            curveTo(10.75F, 6.199219F, 9.4375F, 6.039063F, 8.4375F, 6.0625F)

            moveTo(11.34375F, 7.40625F)
            curveTo(11.398438F, 7.390625F, 11.449219F, 7.40625F, 11.5F, 7.40625F)
            curveTo(11.800781F, 7.40625F, 11.90625F, 7.605469F, 11.90625F, 7.90625F)
            curveTo(11.90625F, 8.207031F, 11.605469F, 8.3125F, 11.40625F, 8.3125F)
            curveTo(11.105469F, 8.3125F, 11.0F, 8.011719F, 11.0F, 7.8125F)
            curveTo(11.0F, 7.585938F, 11.175781F, 7.449219F, 11.34375F, 7.40625F)

            moveTo(18.90625F, 10.3125F)
            curveTo(17.605469F, 10.3125F, 16.613281F, 10.695313F, 15.8125F, 11.59375F)
            curveTo(16.511719F, 11.195313F, 17.207031F, 11.511719F, 17.40625F, 11.8125F)
            curveTo(17.605469F, 12.113281F, 17.710938F, 12.414063F, 17.8125F, 12.3125F)
            curveTo(18.113281F, 12.113281F, 18.507812F, 12.0F, 18.90625F, 12.0F)
            curveTo(19.605469F, 12.0F, 20.40625F, 12.707031F, 20.40625F, 13.40625F)
            lineTo(20.40625F, 18.8125F)
            curveTo(20.40625F, 19.511719F, 19.605469F, 20.3125F, 18.90625F, 20.3125F)
            curveTo(17.804688F, 20.3125F, 16.011719F, 19.1875F, 15.3125F, 18.1875F)
            curveTo(15.210938F, 18.085938F, 15.09375F, 18.007812F, 15.09375F, 17.90625F)
            curveTo(14.792969F, 17.707031F, 14.398438F, 17.398438F, 14.0F, 17.0F)
            curveTo(14.300781F, 17.199219F, 14.800781F, 17.40625F, 15.0F, 17.40625F)
            lineTo(15.0F, 17.3125F)
            curveTo(15.300781F, 16.613281F, 15.105469F, 16.414062F, 14.90625F, 16.3125F)
            lineTo(14.3125F, 16.0F)
            curveTo(13.914063F, 15.800781F, 13.707031F, 15.894531F, 13.40625F, 16.59375F)
            curveTo(13.105469F, 17.195312F, 13.113281F, 17.992188F, 13.8125F, 19.09375F)
            curveTo(14.8125F, 20.59375F, 17.207031F, 21.90625F, 18.90625F, 21.90625F)
            curveTo(20.605469F, 21.90625F, 22.0F, 20.511719F, 22.0F, 18.8125F)
            lineTo(22.0F, 13.40625F)
            curveTo(22.0F, 11.707031F, 20.605469F, 10.3125F, 18.90625F, 10.3125F)

            moveTo(16.0F, 11.6875F)
            curveTo(15.898438F, 11.6875F, 15.695313F, 11.804688F, 15.59375F, 11.90625F)
            curveTo(15.195313F, 12.207031F, 15.105469F, 12.6875F, 15.40625F, 13.1875F)
            curveTo(16.605469F, 14.886719F, 16.007812F, 16.8125F, 15.90625F, 16.8125F)
            curveTo(15.605469F, 17.710938F, 16.0F, 17.804688F, 16.5F, 17.90625F)
            curveTo(17.199219F, 18.105469F, 17.394531F, 18.210938F, 17.59375F, 17.3125F)
            curveTo(17.695312F, 17.210938F, 18.605469F, 14.492188F, 16.90625F, 12.09375F)
            curveTo(16.707031F, 11.792969F, 16.398438F, 11.6875F, 16.0F, 11.6875F)

            moveTo(16.09375F, 12.09375F)
            curveTo(16.394531F, 12.09375F, 16.59375F, 12.199219F, 16.59375F, 12.5F)
            curveTo(16.59375F, 12.800781F, 16.488281F, 13.0F, 16.1875F, 13.0F)
            curveTo(15.886719F, 13.0F, 15.6875F, 12.894531F, 15.6875F, 12.59375F)
            curveTo(15.6875F, 12.292969F, 15.792969F, 12.09375F, 16.09375F, 12.09375F)

            close()
        }.build()
        return _quickdraw!!
    }
private var _quickdraw: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconQuickdrawPreview() {
    Image(imageVector = Icons.Filled.Quickdraw, contentDescription = null)
}
