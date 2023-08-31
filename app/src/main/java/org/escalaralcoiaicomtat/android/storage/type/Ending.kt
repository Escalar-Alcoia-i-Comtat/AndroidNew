package org.escalaralcoiaicomtat.android.storage.type

import androidx.annotation.StringRes
import org.escalaralcoiaicomtat.android.R

enum class Ending(@StringRes val displayName: Int) {
    NONE(R.string.ending_none),
    PLATE(R.string.ending_plate),
    PLATE_RING(R.string.ending_plate_ring),
    PLATE_LANYARD(R.string.ending_plate_lanyard),
    PLATE_CARABINER(R.string.ending_plate_carabiner),
    CHAIN_RING(R.string.ending_chain_ring),
    CHAIN_CARABINER(R.string.ending_chain_carabiner),
    PITON(R.string.ending_piton),
    LANYARD(R.string.ending_lanyard),
    WALKING(R.string.ending_walking),
    RAPPEL(R.string.ending_rappel)
}
