package org.escalaralcoiaicomtat.android.ui.intro

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.escalaralcoiaicomtat.android.ui.icons.Quickdraw
import org.escalaralcoiaicomtat.android.ui.reusable.Icon
import org.escalaralcoiaicomtat.android.ui.theme.AppTheme

object IntroPage {
    abstract class Action {
        abstract val text: @Composable () -> String
        open val icon: (@Composable () -> Icon)? = null

        abstract fun onClick()
    }
}

@Composable
fun IntroPage(
    @DrawableRes icon: Int,
    title: String,
    message: String,
    iconColor: Color = LocalContentColor.current,
    action: IntroPage.Action? = null
) = IntroPage(Icon(icon), title, message, iconColor, action)

@Composable
fun IntroPage(
    icon: ImageVector,
    title: String,
    message: String,
    iconColor: Color = LocalContentColor.current,
    action: IntroPage.Action? = null
) = IntroPage(Icon(icon), title, message, iconColor, action)

@Composable
fun IntroPage(
    icon: Icon,
    title: String,
    message: String,
    iconColor: Color = LocalContentColor.current,
    action: IntroPage.Action? = null
) {
    // Icon size: 96x96

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                // Restrict width so that big displays do not use whole span
                .widthIn(max = 600.dp)
                .fillMaxSize()
        ) {
            Icon(
                icon = icon,
                contentDescription = title,
                modifier = Modifier
                    .padding(top = 156.dp, bottom = 36.dp)
                    .size(96.dp),
                tint = iconColor
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontSize = 26.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )

            action?.let {
                OutlinedButton(onClick = action::onClick) {
                    action.icon?.let {
                        val actionIcon = it()
                        actionIcon.Content(contentDescription = action.text())
                    }
                    Text(action.text())
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun IntroPage_Preview() {
    AppTheme {
        IntroPage(
            icon = Icons.Filled.Quickdraw,
            title = "Example Page Title",
            message = "This is the message to be shown in the intro page. It shouldn't be too long."
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun IntroPage_PreviewAction() {
    AppTheme {
        IntroPage(
            icon = Icons.Filled.Quickdraw,
            title = "Example Page Title",
            message = "This is the message to be shown in the intro page. It shouldn't be too long.",
            action = object : IntroPage.Action() {
                override val text: @Composable () -> String = { "Action button" }

                override fun onClick() { }
            }
        )
    }
}
