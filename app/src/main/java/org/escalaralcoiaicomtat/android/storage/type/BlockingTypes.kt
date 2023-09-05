package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

enum class BlockingTypes(
    @DrawableRes val iconRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val level: Level
) {
    DRY(R.drawable.construction, R.string.block_dry_title, R.string.block_dry_message, Level.CRITICAL),
    BUILD(R.drawable.construction, R.string.block_build_title, R.string.block_build_message, Level.CRITICAL),
    BIRD(R.drawable.bird, R.string.block_bird_title, R.string.block_bird_message, Level.CRITICAL),
    OLD(R.drawable.rock, R.string.block_old_title, R.string.block_old_message, Level.WARNING),
    PLANTS(R.drawable.grass, R.string.block_plants_title, R.string.block_plants_message, Level.WARNING),
    ROPE_LENGTH(R.drawable.rope, R.string.block_rope_length_title, R.string.block_rope_length_message, Level.WARNING);

    enum class Level { WARNING, CRITICAL }
}
