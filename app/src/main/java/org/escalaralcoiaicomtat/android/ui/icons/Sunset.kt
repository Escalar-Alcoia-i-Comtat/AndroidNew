package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Rounded.Sunset: ImageVector
    get() {
        if (_sunset != null) {
            return _sunset!!
        }
        _sunset = ImageVector.Builder(
            name = "Sunset",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0F,
            viewportHeight = 24.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(9.183594F, 3.0F)
            curveTo(8.615594F, 3.0F, 8.330421F, 3.6868436F, 8.732422F, 4.0898438F)
            lineTo(11.392578F, 6.748047F)
            curveTo(11.728578F, 7.084047F, 12.273375F, 7.084047F, 12.609375F, 6.748047F)
            lineTo(15.267578F, 4.0898438F)
            curveTo(15.669578F, 3.6868436F, 15.383453F, 3.0F, 14.814453F, 3.0F)
            lineTo(9.183594F, 3.0F)

            moveTo(11.984375F, 8.986328F)
            arcTo(1.0001F, 1.0001F, 0.0F, false, false, 11.0F, 10.0F)
            lineTo(11.0F, 12.0F)
            arcTo(1.0001F, 1.0001F, 0.0F, true, false, 13.0F, 12.0F)
            lineTo(13.0F, 10.0F)
            arcTo(1.0001F, 1.0001F, 0.0F, false, false, 11.984375F, 8.986328F)

            moveTo(4.9179688F, 11.917969F)
            arcTo(1.0001F, 1.0001F, 0.0F, false, false, 4.2226562F, 13.636719F)
            lineTo(5.6367188F, 15.050781F)
            arcTo(1.0001F, 1.0001F, 0.0F, true, false, 7.0507812F, 13.636719F)
            lineTo(5.6367188F, 12.222656F)
            arcTo(1.0001F, 1.0001F, 0.0F, false, false, 4.9179688F, 11.917969F)

            moveTo(19.050781F, 11.919922F)
            arcTo(1.0001F, 1.0001F, 0.0F, false, false, 18.363281F, 12.222656F)
            lineTo(16.949219F, 13.636719F)
            arcTo(1.0001F, 1.0001F, 0.0F, true, false, 18.363281F, 15.050781F)
            lineTo(19.777344F, 13.636719F)
            arcTo(1.0001F, 1.0001F, 0.0F, false, false, 19.050781F, 11.919922F)

            moveTo(12.0F, 15.0F)
            curveTo(9.592847F, 15.0F, 7.5684514F, 16.725424F, 7.1015625F, 19.0F)
            lineTo(3.0F, 19.0F)
            arcTo(1.0001F, 1.0001F, 0.0F, true, false, 3.0F, 21.0F)
            lineTo(21.0F, 21.0F)
            arcTo(1.0001F, 1.0001F, 0.0F, true, false, 21.0F, 19.0F)
            lineTo(16.898438F, 19.0F)
            curveTo(16.43155F, 16.725424F, 14.407153F, 15.0F, 12.0F, 15.0F)

            close()
        }.build()
        return _sunset!!
    }
private var _sunset: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconSunsetPreview() {
    Image(imageVector = Icons.Rounded.Sunset, contentDescription = null)
}
