package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Filled.ClimbingShoes: ImageVector
    get() {
        if (_climbingShoes != null) {
            return _climbingShoes!!
        }
        _climbingShoes = ImageVector.Builder(
            name = "ClimbingShoes",
            defaultWidth = 200.0.dp,
            defaultHeight = 200.0.dp,
            viewportWidth = 200.0F,
            viewportHeight = 200.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(19.5F, 2.0F)
            curveTo(18.800781F, 2.0F, 17.8125F, 2.3125F, 17.8125F, 2.3125F)
            curveTo(20.414062F, 4.8125F, 11.90625F, 7.601563F, 12.90625F, 11.5F)
            curveTo(8.804688F, 7.800781F, 20.710938F, 3.007813F, 15.8125F, 2.90625F)
            curveTo(10.210938F, 4.605469F, 6.0F, 9.0F, 6.0F, 9.0F)
            curveTo(6.0F, 9.0F, 6.300781F, 10.3125F, 8.5F, 10.3125F)
            curveTo(10.898438F, 10.3125F, 9.90625F, 12.207031F, 8.90625F, 14.40625F)
            curveTo(8.179688F, 15.863281F, 7.238281F, 16.785156F, 5.90625F, 16.71875F)
            curveTo(5.671875F, 16.597656F, 5.40625F, 16.425781F, 5.21875F, 16.3125F)
            curveTo(4.382813F, 15.796875F, 4.0F, 15.53125F, 4.0F, 14.0F)
            lineTo(2.0F, 14.0F)
            curveTo(2.0F, 16.070312F, 3.074219F, 17.347656F, 4.1875F, 18.03125F)
            curveTo(5.300781F, 18.714844F, 6.34375F, 19.074219F, 7.15625F, 19.96875F)
            lineTo(7.1875F, 19.9375F)
            curveTo(8.15625F, 21.042969F, 9.402344F, 22.0F, 10.90625F, 22.0F)
            curveTo(14.105469F, 22.0F, 13.3125F, 15.789063F, 14.3125F, 13.6875F)
            curveTo(16.210938F, 9.585938F, 22.0F, 8.3125F, 22.0F, 3.8125F)
            curveTo(22.0F, 2.8125F, 21.199219F, 2.0F, 19.5F, 2.0F)

            close()
        }.build()
        return _climbingShoes!!
    }
private var _climbingShoes: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconClimbingShoesPreview() {
    Image(imageVector = Icons.Filled.ClimbingShoes, contentDescription = null)
}
