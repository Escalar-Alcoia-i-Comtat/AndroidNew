package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Filled.Falcon: ImageVector
    get() {
        if (_falcon != null) {
            return _falcon!!
        }
        _falcon = ImageVector.Builder(
            name = "Falcon",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24F,
            viewportHeight = 24F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(21.921875F, 8.953125F)
            curveTo(21.683594F, 7.949219F, 20.027344F, 6.894531F, 19.0F, 7.0F)
            curveTo(19.078125F, 6.625F, 17.394531F, 5.339844F, 17.066406F, 5.140625F)
            curveTo(15.320313F, 4.074219F, 13.144531F, 3.019531F, 11.0F, 3.0F)
            curveTo(6.75F, 2.960938F, 4.890625F, 6.171875F, 4.0F, 7.035156F)
            curveTo(3.457031F, 7.5625F, 2.628906F, 8.335938F, 2.0F, 9.0F)
            curveTo(2.625F, 9.0F, 3.542969F, 9.0F, 4.0F, 9.0F)
            curveTo(3.46875F, 9.894531F, 1.796875F, 11.855469F, 2.0F, 14.0F)
            curveTo(2.855469F, 12.855469F, 3.4375F, 12.625F, 4.140625F, 12.3125F)
            curveTo(2.84375F, 16.207031F, 4.625F, 20.0F, 7.0F, 21.0F)
            curveTo(6.71875F, 19.75F, 6.6875F, 17.625F, 7.0F, 17.0F)
            curveTo(8.203125F, 19.035156F, 10.855469F, 20.480469F, 12.0F, 21.0F)
            curveTo(12.0F, 18.875F, 11.78125F, 16.238281F, 13.0F, 14.0F)
            curveTo(13.789063F, 12.550781F, 15.40625F, 11.0F, 17.0F, 11.0F)
            curveTo(18.359375F, 11.0F, 19.140625F, 11.0F, 21.0F, 11.0F)
            curveTo(21.21875F, 11.296875F, 21.5F, 11.5F, 22.0F, 12.0F)
            curveTo(22.0F, 11.28125F, 22.066406F, 10.636719F, 22.046875F, 10.0F)
            curveTo(22.035156F, 9.652344F, 22.003906F, 9.308594F, 21.921875F, 8.953125F)

            moveTo(14.625F, 8.101563F)
            curveTo(13.769531F, 8.101563F, 13.15625F, 7.5625F, 13.0F, 7.0F)
            curveTo(12.785156F, 6.761719F, 12.167969F, 6.335938F, 12.0F, 6.0F)
            curveTo(12.429688F, 6.40625F, 15.625F, 7.0F, 16.0F, 7.0F)
            curveTo(16.0F, 7.53125F, 15.78125F, 8.101563F, 14.625F, 8.101563F)

            close()
        }.build()
        return _falcon!!
    }
private var _falcon: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconFalconPreview() {
    Image(imageVector = Icons.Filled.Falcon, contentDescription = null)
}
