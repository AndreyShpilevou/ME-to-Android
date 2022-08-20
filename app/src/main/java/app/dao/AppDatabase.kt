package app.dao

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room

@Database(entities = [AppItem::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appItemDao(): AppItemDao?

    companion object {
        @JvmStatic
		@Synchronized
        fun open(context: Context, dir: String): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "$dir/J2ME-apps.db"
            ).build()
        }
    }
}