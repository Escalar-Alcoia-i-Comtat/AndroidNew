package org.escalaralcoiaicomtat.android.ui.reusable

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource

class Icon(
    val imageVector: ImageVector?,
    @DrawableRes val drawableRes: Int?
) {
    constructor(imageVector: ImageVector) : this(imageVector, null)
    constructor(@DrawableRes drawableRes: Int) : this(null, drawableRes)

    @Composable
    fun Content(
        contentDescription: String?,
        modifier: Modifier = Modifier,
        tint: Color = LocalContentColor.current
    ) {
        if (imageVector != null) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = modifier,
                tint = tint
            )
        } else if (drawableRes != null) {
            Image(
                painter = painterResource(drawableRes),
                contentDescription = contentDescription,
                modifier = modifier
            )
        }
    }
}

@Composable
fun Icon(
    icon: Icon,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    icon.Content(contentDescription, modifier, tint)
}
