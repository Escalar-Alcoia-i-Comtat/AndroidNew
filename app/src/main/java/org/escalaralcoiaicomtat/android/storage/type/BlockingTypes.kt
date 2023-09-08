package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Grass
import androidx.compose.ui.graphics.vector.ImageVector
import org.escalaralcoiaicomtat.android.R
import org.escalaralcoiaicomtat.android.ui.icons.Falcon
import org.escalaralcoiaicomtat.android.ui.icons.Rock
import org.escalaralcoiaicomtat.android.ui.icons.Rope

enum class BlockingTypes(
    val icon: ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val messageRes: Int,
    val level: Level
) {
    DRY(Icons.Filled.Construction, R.string.block_dry_title, R.string.block_dry_message, Level.CRITICAL),
    BUILD(Icons.Filled.Construction, R.string.block_build_title, R.string.block_build_message, Level.CRITICAL),
    BIRD(Icons.Filled.Falcon, R.string.block_bird_title, R.string.block_bird_message, Level.CRITICAL),
    OLD(Icons.Filled.Rock, R.string.block_old_title, R.string.block_old_message, Level.WARNING),
    PLANTS(Icons.Filled.Grass, R.string.block_plants_title, R.string.block_plants_message, Level.WARNING),
    ROPE_LENGTH(Icons.Filled.Rope, R.string.block_rope_length_title, R.string.block_rope_length_message, Level.WARNING);

    enum class Level { WARNING, CRITICAL }
}
