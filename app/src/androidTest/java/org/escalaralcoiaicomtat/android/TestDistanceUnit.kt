package org.escalaralcoiaicomtat.android

import org.escalaralcoiaicomtat.android.unit.DistanceUnit
import org.junit.Test

class TestDistanceUnit {
    @Test
    fun testFeetConversion() {
        val oneMeter = DistanceUnit(1.0)
        val feet = oneMeter.toFeet()
        assert(feet.value == 3.28084) { "Expected ${feet.value} to be 3.28084" }
    }
}
