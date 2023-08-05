package org.escalaralcoiaicomtat.android.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.escalaralcoiaicomtat.android.storage.converters.AndroidConverters
import org.escalaralcoiaicomtat.android.storage.converters.Converters
import org.escalaralcoiaicomtat.android.storage.converters.JavaConverters
import org.escalaralcoiaicomtat.android.storage.converters.ListConverters
import org.escalaralcoiaicomtat.android.storage.dao.DataDao
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone

@Database(
    entities = [Area::class, Zone::class, Sector::class, Path::class, Blocking::class],
    version = 1
)
@TypeConverters(ListConverters::class, Converters::class, JavaConverters::class, AndroidConverters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(applicationContext: Context) = instance ?: synchronized(this) {
            instance ?: Room
                .databaseBuilder(
                    applicationContext,
                    AppDatabase::class.java,
                    "database"
                )
                .build()
                .also { instance = it }
        }
    }

    abstract fun dataDao(): DataDao
}
