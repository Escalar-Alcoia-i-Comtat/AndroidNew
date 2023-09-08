package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Filled.ClimbingAnchor: ImageVector
    get() {
        if (_climbingAnchor != null) {
            return _climbingAnchor!!
        }
        _climbingAnchor = ImageVector.Builder(
            name = "ClimbingAnchor",
            defaultWidth = 200.0.dp,
            defaultHeight = 200.0.dp,
            viewportWidth = 200.0F,
            viewportHeight = 200.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(7.40625F, 2.0F)
            curveTo(4.207031F, 2.0F, 3.0F, 5.5F, 3.0F, 8.0F)
            curveTo(3.0F, 17.199219F, 4.511719F, 22.0F, 7.3125F, 22.0F)
            lineTo(7.90625F, 21.90625F)
            curveTo(7.207031F, 21.605469F, 5.0F, 19.699219F, 5.0F, 8.0F)
            curveTo(5.0F, 5.5F, 5.695313F, 2.199219F, 8.59375F, 2.0F)

            moveTo(10.3125F, 2.09375F)
            curveTo(7.113281F, 1.992188F, 6.199219F, 5.5F, 6.0F, 8.0F)
            curveTo(5.898438F, 10.199219F, 6.292969F, 14.007813F, 7.09375F, 17.90625F)
            curveTo(8.09375F, 22.40625F, 9.789063F, 22.105469F, 10.1875F, 21.90625F)
            curveTo(12.085938F, 21.207031F, 13.613281F, 19.210938F, 14.3125F, 18.3125F)
            curveTo(15.011719F, 17.511719F, 17.988281F, 14.0F, 19.1875F, 12.5F)
            curveTo(21.085938F, 10.300781F, 21.207031F, 9.007813F, 20.90625F, 7.40625F)
            curveTo(20.605469F, 5.707031F, 19.414062F, 4.40625F, 16.8125F, 3.40625F)
            curveTo(14.210938F, 2.40625F, 12.210938F, 2.09375F, 10.3125F, 2.09375F)

            moveTo(16.0F, 6.0F)
            curveTo(17.101562F, 6.0F, 18.0F, 6.898438F, 18.0F, 8.0F)
            curveTo(18.0F, 9.101563F, 17.101562F, 10.0F, 16.0F, 10.0F)
            curveTo(14.898438F, 10.0F, 14.0F, 9.101563F, 14.0F, 8.0F)
            curveTo(14.0F, 6.898438F, 14.898438F, 6.0F, 16.0F, 6.0F)

            moveTo(9.03125F, 6.1875F)
            curveTo(9.257813F, 6.132813F, 9.460938F, 6.460938F, 9.6875F, 6.6875F)
            curveTo(9.886719F, 6.988281F, 10.386719F, 7.886719F, 10.6875F, 9.1875F)
            curveTo(10.988281F, 10.488281F, 11.207031F, 11.894531F, 11.40625F, 12.59375F)
            curveTo(11.507813F, 13.492188F, 11.300781F, 14.59375F, 11.0F, 15.09375F)
            curveTo(10.699219F, 15.59375F, 10.386719F, 15.914063F, 10.1875F, 16.3125F)
            curveTo(9.988281F, 16.613281F, 9.511719F, 17.011719F, 9.3125F, 16.8125F)
            curveTo(9.113281F, 16.710938F, 8.605469F, 14.894531F, 8.40625F, 13.59375F)
            curveTo(8.105469F, 12.394531F, 7.992188F, 11.800781F, 8.09375F, 9.5F)
            curveTo(8.195313F, 7.300781F, 8.8125F, 6.40625F, 8.8125F, 6.40625F)
            curveTo(8.886719F, 6.28125F, 8.957031F, 6.207031F, 9.03125F, 6.1875F)

            close()
        }.build()
        return _climbingAnchor!!
    }
private var _climbingAnchor: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconClimbingAnchorPreview() {
    Image(imageVector = Icons.Filled.ClimbingAnchor, contentDescription = null)
}
