package org.escalaralcoiaicomtat.android.storage.type

interface GradeValue {
    companion object {
        fun fromString(value: String): GradeValue? {
            val name = value.replace("+", "_PLUS")
            return SportsGrade.entries.find { it.name.endsWith(name) }
                ?: ArtificialGrade.entries.find { it.name == name }
        }
    }

    val name: String
}

enum class SportsGrade : GradeValue {
    G1,
    G2, G2_PLUS,
    G3A, G3B, G3C,
    G4A, G4B, G4C,
    G5A, G5B, G5C,
    G6A, G6A_PLUS, G6B, G6B_PLUS, G6C, G6C_PLUS,
    G7A, G7A_PLUS, G7B, G7B_PLUS, G7C, G7C_PLUS,
    G8A, G8A_PLUS, G8B, G8B_PLUS, G8C, G8C_PLUS,
    G9A, G9A_PLUS, G9B, G9B_PLUS, G9C, G9C_PLUS
}

enum class ArtificialGrade : GradeValue {
    A1, A2, A3
}
