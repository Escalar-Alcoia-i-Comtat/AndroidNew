package org.escalaralcoiaicomtat.android.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Rounded.SlingHere: ImageVector
    get() {
        if (_slingHere != null) {
            return _slingHere!!
        }
        _slingHere = Builder(
            name = "Icons8-sling-here", defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight = 24.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = null, strokeLineWidth = 0.0f,
                strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(12.0605f, 0.0f)
                curveTo(11.8784f, -0.0022f, 11.6942f, 0.0056f, 11.5078f, 0.0234f)
                curveTo(8.9088f, 0.2714f, 7.0f, 2.6048f, 7.0f, 5.2168f)
                lineTo(7.0f, 6.0f)
                curveTo(7.0f, 6.552f, 7.448f, 7.0f, 8.0f, 7.0f)
                curveTo(8.552f, 7.0f, 9.0f, 6.552f, 9.0f, 6.0f)
                lineTo(9.0f, 5.1699f)
                curveTo(9.0f, 3.5449f, 10.2178f, 2.0909f, 11.8398f, 2.0059f)
                curveTo(13.5698f, 1.9149f, 15.0f, 3.29f, 15.0f, 5.0f)
                lineTo(15.0f, 6.0f)
                curveTo(15.0f, 6.552f, 15.448f, 7.0f, 16.0f, 7.0f)
                curveTo(16.552f, 7.0f, 17.0f, 6.552f, 17.0f, 6.0f)
                lineTo(17.0f, 5.0f)
                curveTo(17.0f, 2.2597f, 14.7934f, 0.0327f, 12.0605f, 0.0f)
                close()
                moveTo(12.0f, 5.0f)
                curveTo(11.448f, 5.0f, 11.0f, 5.448f, 11.0f, 6.0f)
                lineTo(11.0f, 10.0f)
                curveTo(11.0f, 10.552f, 11.448f, 11.0f, 12.0f, 11.0f)
                curveTo(12.552f, 11.0f, 13.0f, 10.552f, 13.0f, 10.0f)
                lineTo(13.0f, 6.0f)
                curveTo(13.0f, 5.448f, 12.552f, 5.0f, 12.0f, 5.0f)
                close()
                moveTo(8.0f, 9.0f)
                curveTo(7.448f, 9.0f, 7.0f, 9.448f, 7.0f, 10.0f)
                lineTo(7.0f, 11.0f)
                curveTo(7.0f, 13.4176f, 8.7195f, 15.4283f, 11.0f, 15.8926f)
                lineTo(11.0f, 20.5859f)
                lineTo(9.707f, 19.293f)
                arcTo(1.0001f, 1.0001f, 0.0f, false, false, 8.9902f, 18.9902f)
                arcTo(1.0001f, 1.0001f, 0.0f, false, false, 8.293f, 20.707f)
                lineTo(11.293f, 23.707f)
                arcTo(1.0001f, 1.0001f, 0.0f, false, false, 12.707f, 23.707f)
                lineTo(15.707f, 20.707f)
                arcTo(1.0001f, 1.0001f, 0.0f, true, false, 14.293f, 19.293f)
                lineTo(13.0f, 20.5859f)
                lineTo(13.0f, 15.9023f)
                curveTo(13.0033f, 15.9017f, 13.0065f, 15.901f, 13.0098f, 15.9004f)
                curveTo(15.3838f, 15.4364f, 17.0f, 13.2022f, 17.0f, 10.7832f)
                lineTo(17.0f, 10.0f)
                curveTo(17.0f, 9.448f, 16.552f, 9.0f, 16.0f, 9.0f)
                curveTo(15.448f, 9.0f, 15.0f, 9.448f, 15.0f, 10.0f)
                lineTo(15.0f, 10.8301f)
                curveTo(15.0f, 12.4551f, 13.7822f, 13.9091f, 12.1602f, 13.9941f)
                curveTo(10.4302f, 14.0851f, 9.0f, 12.71f, 9.0f, 11.0f)
                lineTo(9.0f, 10.0f)
                curveTo(9.0f, 9.448f, 8.552f, 9.0f, 8.0f, 9.0f)
                close()
            }
        }
            .build()
        return _slingHere!!
    }

private var _slingHere: ImageVector? = null
