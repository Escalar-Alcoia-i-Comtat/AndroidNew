package org.escalaralcoiaicomtat.android.storage.converters

import android.net.Uri
import androidx.room.TypeConverter

class AndroidConverters {

    @TypeConverter
    fun toURL(value: String?): Uri? = value?.let(Uri::parse)

    @TypeConverter
    fun fromURL(value: Uri?): String? = value?.toString()
}
