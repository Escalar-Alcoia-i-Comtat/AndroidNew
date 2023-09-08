package org.escalaralcoiaicomtat.android

import android.net.Uri
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.type.SunTime
import java.time.Instant
import java.util.UUID

object SampleDataProvider {
    val SampleArea = Area(
        0L,
        Instant.ofEpochMilli(123),
        "Testing Area",
        Uri.EMPTY,
        UUID.randomUUID().toString(),
        false
    )
    val SampleZone = Zone(
        0L,
        Instant.ofEpochMilli(123),
        "Testing Zone",
        Uri.EMPTY,
        UUID.randomUUID().toString(),
        "",
        null,
        emptyList(),
        0L,
        false
    )
    val SampleSector = Sector(
        0L,
        Instant.ofEpochMilli(123),
        "Testing Zone",
        false,
        SunTime.Day,
        0L,
        UUID.randomUUID().toString(),
        null,
        "aaa",
        0,
        false
    )
    val SamplePath = Path(
        id = 0L,
        timestamp = Instant.ofEpochMilli(123),
        displayName = "Testing Path",
        sketchId = 1,
        height = null,
        grade = null,
        ending = null,
        pitches = null,
        stringCount = null,
        paraboltCount = null,
        burilCount = null,
        pitonCount = null,
        spitCount = null,
        tensorCount = null,
        crackerRequired = false,
        friendRequired = false,
        lanyardRequired = false,
        nailRequired = false,
        pitonRequired = false,
        stapesRequired = false,
        showDescription = false,
        description = null,
        builder = null,
        reBuilder = null,
        parentId = 0L,
        isFavorite = false
    )
}
