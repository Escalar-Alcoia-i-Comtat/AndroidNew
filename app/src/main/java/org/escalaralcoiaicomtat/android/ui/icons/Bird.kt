package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val Icons.Filled.Bird: ImageVector
    get() {
        if (_bird != null) {
            return _bird!!
        }
        _bird = ImageVector.Builder(
            name = "Bird",
            defaultWidth = 200.0.dp,
            defaultHeight = 200.0.dp,
            viewportWidth = 200.0F,
            viewportHeight = 200.0F,
        ).materialPath {
            verticalLineToRelative(0.0F)
            moveTo(3.0625F, 0.05078125F)
            lineTo(3.0625F, 2.359375F)
            curveTo(3.0625F, 5.986375F, 3.9305313F, 8.883281F, 5.6445312F, 10.988281F)
            curveTo(2.0005312F, 15.161281F, 2.0F, 20.216F, 2.0F, 23.0F)
            lineTo(4.0F, 23.0F)
            lineTo(16.0F, 23.0F)
            lineTo(18.0F, 23.0F)
            lineTo(18.0F, 19.957031F)
            curveTo(18.331194F, 19.92118F, 18.691435F, 19.860222F, 19.082031F, 19.720703F)
            curveTo(20.03981F, 19.378592F, 20.970127F, 18.377789F, 21.50586F, 16.921875F)
            lineTo(23.0F, 17.220703F)
            lineTo(23.0F, 16.0F)
            curveTo(23.0F, 12.722222F, 21.529705F, 10.754888F, 20.042969F, 9.792969F)
            curveTo(19.195278F, 9.2445135F, 18.37531F, 9.002427F, 17.796875F, 8.8828125F)
            curveTo(17.334396F, 7.4438534F, 16.049782F, 6.0F, 14.0F, 6.0F)
            curveTo(9.464F, 6.0F, 4.7930937F, 1.6729063F, 4.7460938F, 1.6289062F)
            lineTo(3.0625F, 0.05078125F)

            moveTo(13.0F, 11.0F)
            curveTo(13.552F, 11.0F, 14.0F, 11.448F, 14.0F, 12.0F)
            curveTo(14.0F, 12.552F, 13.552F, 13.0F, 13.0F, 13.0F)
            curveTo(12.448F, 13.0F, 12.0F, 12.552F, 12.0F, 12.0F)
            curveTo(12.0F, 11.448F, 12.448F, 11.0F, 13.0F, 11.0F)

            moveTo(18.0F, 11.134766F)
            curveTo(18.368076F, 11.232766F, 18.473227F, 11.159636F, 18.957031F, 11.472656F)
            curveTo(19.757996F, 11.99088F, 20.425623F, 13.058064F, 20.730469F, 14.726562F)
            lineTo(18.0F, 14.179688F)
            lineTo(18.0F, 11.134766F)

            moveTo(18.0F, 16.220703F)
            lineTo(19.601562F, 16.541016F)
            curveTo(19.29097F, 17.253527F, 18.879171F, 17.669664F, 18.408203F, 17.83789F)
            curveTo(18.26771F, 17.888075F, 18.131254F, 17.918734F, 18.0F, 17.94336F)
            lineTo(18.0F, 16.220703F)

            close()
        }.build()
        return _bird!!
    }
private var _bird: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconBirdPreview() {
    Image(imageVector = Icons.Filled.Bird, contentDescription = null)
}
