package org.escalaralcoiaicomtat.android

import org.escalaralcoiaicomtat.android.SampleDataProvider.SampleArea
import org.escalaralcoiaicomtat.android.SampleDataProvider.SampleSector
import org.escalaralcoiaicomtat.android.SampleDataProvider.SampleZone
import org.escalaralcoiaicomtat.android.storage.data.properties
import org.junit.Assert.assertNotNull
import org.junit.Test

class DataEntityPropertiesTest {
    @Test
    fun test_gettingPropertiesOfArea() {
        val properties = SampleArea.properties()

        assertNotNull(
            properties.find { it.name == "displayName" }
        )
        assertNotNull(
            properties.find { it.name == "webUrl" }
        )
    }

    @Test
    fun test_gettingPropertiesOfZone() {
        val properties = SampleZone.properties()

        assertNotNull(
            properties.find { it.name == "displayName" }
        )
        assertNotNull(
            properties.find { it.name == "webUrl" }
        )
        assertNotNull(
            properties.find { it.name == "points" }
        )
        assertNotNull(
            properties.find { it.name == "area" }
        )
        assertNotNull(
            properties.find { it.name == "point" }
        )
    }

    @Test
    fun test_gettingPropertiesOfSector() {
        val properties = SampleSector.properties()

        assertNotNull(
            properties.find { it.name == "displayName" }
        )
        assertNotNull(
            properties.find { it.name == "kidsApt" }
        )
        assertNotNull(
            properties.find { it.name == "sunTime" }
        )
        assertNotNull(
            properties.find { it.name == "weight" }
        )
        assertNotNull(
            properties.find { it.name == "zone" }
        )
        assertNotNull(
            properties.find { it.name == "point" }
        )
        assertNotNull(
            properties.find { it.name == "walkingTime" }
        )
    }
}
