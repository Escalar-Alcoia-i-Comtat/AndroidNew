package org.escalaralcoiaicomtat.android.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import org.escalaralcoiaicomtat.android.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val rubik = FontFamily(
    Font(googleFont = GoogleFont("Rubik"), fontProvider = provider)
)

val jost = FontFamily(
    Font(googleFont = GoogleFont("Jost"), fontProvider = provider)
)

val roboto = FontFamily(
    Font(googleFont = GoogleFont("Roboto"), fontProvider = provider)
)
