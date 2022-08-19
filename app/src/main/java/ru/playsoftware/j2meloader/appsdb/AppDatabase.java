package ru.playsoftware.j2meloader.appsdb;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {AppItem.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

	public abstract AppItemDao appItemDao();

	public static synchronized AppDatabase open(Context context, String dir) {
		return Room.databaseBuilder(
				context.getApplicationContext(),
				AppDatabase.class,
				dir + "/J2ME-apps.db")
				.allowMainThreadQueries()
				.build();
	}
}
