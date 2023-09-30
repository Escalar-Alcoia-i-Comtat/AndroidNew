package org.escalaralcoiaicomtat.android.storage

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.escalaralcoiaicomtat.android.storage.converters.AndroidConverters
import org.escalaralcoiaicomtat.android.storage.converters.Converters
import org.escalaralcoiaicomtat.android.storage.converters.JavaConverters
import org.escalaralcoiaicomtat.android.storage.converters.ListConverters
import org.escalaralcoiaicomtat.android.storage.dao.DataDao
import org.escalaralcoiaicomtat.android.storage.dao.UserDao
import org.escalaralcoiaicomtat.android.storage.data.Area
import org.escalaralcoiaicomtat.android.storage.data.Blocking
import org.escalaralcoiaicomtat.android.storage.data.LocalDeletion
import org.escalaralcoiaicomtat.android.storage.data.Path
import org.escalaralcoiaicomtat.android.storage.data.Sector
import org.escalaralcoiaicomtat.android.storage.data.Zone
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteArea
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteSector
import org.escalaralcoiaicomtat.android.storage.data.favorites.FavoriteZone

@Database(
    entities = [
        Area::class, Zone::class, Sector::class, Path::class, Blocking::class, LocalDeletion::class,
        FavoriteArea::class, FavoriteZone::class, FavoriteSector::class
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    version = 2
)
@TypeConverters(
    ListConverters::class,
    Converters::class,
    JavaConverters::class,
    AndroidConverters::class
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        @Volatile
        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        var instance: AppDatabase? = null

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

        @Composable
        fun rememberInstance(): AppDatabase {
            val context = LocalContext.current
            return remember { getInstance(context) }
        }
    }

    abstract fun dataDao(): DataDao

    abstract fun userDao(): UserDao
}
