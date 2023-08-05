package org.escalaralcoiaicomtat.android.storage.relations

import androidx.room.Embedded
import androidx.room.Relation
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.Path

data class PathWithBlocks(
    @Embedded val path: Path,
    @Relation(parentColumn = "id", entityColumn = "pathId")
    val blocks: List<Blocking>
)
