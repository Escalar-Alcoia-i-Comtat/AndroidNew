package org.escalaralcoiaicomtat.android.storage.type

import androidx.core.text.isDigitsOnly
import org.escalaralcoiaicomtat.android.ui.theme.ColorGrade1
import org.escalaralcoiaicomtat.android.ui.theme.ColorGrade2
import org.escalaralcoiaicomtat.android.ui.theme.ColorGrade3
import org.escalaralcoiaicomtat.android.ui.theme.ColorGrade4
import org.escalaralcoiaicomtat.android.ui.theme.ColorGradeA
import org.escalaralcoiaicomtat.android.ui.theme.ColorGradeP
import org.escalaralcoiaicomtat.android.ui.utils.ColorGroup

interface GradeValue {
    companion object {
        fun fromString(value: String): GradeValue? {
            val name = value.replace("+", "_PLUS")
            return SportsGrade.entries.find { it.name.endsWith(name) }
                ?: ArtificialGrade.entries.find { it.name == name }
        }
    }

    val name: String

    val displayName: String
}

val GradeValue?.color: ColorGroup
    get() =
        if (this is SportsGrade) {
            val number = name[1].digitToInt()
            if (number <= 5)
                ColorGrade1
            else if (number <= 6)
                ColorGrade2
            else if (number <= 7)
                ColorGrade3
            else
                ColorGrade4
        } else if (this is ArtificialGrade) {
            ColorGradeA
        } else {
            ColorGradeP
        }

@Suppress("unused")
enum class SportsGrade : GradeValue {
    G1,
    G2, G2_PLUS,
    G3A, G3B, G3C, G3_PLUS, G3,
    G4A, G4B, G4C, G4_PLUS, G4,
    G5A, G5B, G5C, G5_PLUS, G5,
    G6A, G6A_PLUS, G6B, G6B_PLUS, G6C, G6C_PLUS,
    G7A, G7A_PLUS, G7B, G7B_PLUS, G7C, G7C_PLUS,
    G8A, G8A_PLUS, G8B, G8B_PLUS, G8C, G8C_PLUS,
    G9A, G9A_PLUS, G9B, G9B_PLUS, G9C, G9C_PLUS;

    override val displayName: String = name
        .substring(1)
        .replace("_PLUS", "+")
        .lowercase()
        .let {
            if (it.isDigitsOnly()) {
                "${it}º"
            } else {
                it
            }
        }
}

@Suppress("unused")
enum class ArtificialGrade : GradeValue {
    A0,
    A1, A1_PLUS,
    A2, A2_PLUS,
    A3, A3_PLUS,
    A4, A4_PLUS,
    A5, A5_PLUS;

    override val displayName: String = name
}
