package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Rounded.Sunrise: ImageVector
    get() {
        if (_sunrise != null) {
            return _sunrise!!
        }
        _sunrise = ImageVector.Builder(
            name = "Sunrise",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0F,
            viewportHeight = 24.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(12.0F, 3.0F)
            curveTo(11.77975F, 3.0F, 11.560578F, 3.0839531F, 11.392578F, 3.2519531F)
            lineTo(8.732422F, 5.9101562F)
            curveTo(8.330421F, 6.313156F, 8.615594F, 7.0F, 9.183594F, 7.0F)
            lineTo(14.814453F, 7.0F)
            curveTo(15.383453F, 7.0F, 15.669578F, 6.313156F, 15.267578F, 5.9101562F)
            lineTo(12.609375F, 3.2519531F)
            curveTo(12.441375F, 3.0839531F, 12.22025F, 3.0F, 12.0F, 3.0F)

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
        return _sunrise!!
    }
private var _sunrise: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconSunrisePreview() {
    Image(imageVector = Icons.Rounded.Sunrise, contentDescription = null)
}
