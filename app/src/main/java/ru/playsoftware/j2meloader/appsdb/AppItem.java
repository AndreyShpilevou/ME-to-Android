package ru.playsoftware.j2meloader.appsdb;

import android.content.Context;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import ru.playsoftware.j2meloader.R;
import ru.playsoftware.j2meloader.config.Config;

@Entity(tableName = "apps", indices = {@Index(value = {"path"}, unique = true)})
public class AppItem {
	@PrimaryKey(autoGenerate = true)
	private int id;
	private String imagePath;
	private String title;
	private final String author;
	private final String version;
	private final String path;

	public AppItem(String path, String title, String author, String version) {
		this.path = path;
		this.title = title;
		this.author = author;
		this.version = version;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getAuthor() {
		return author;
	}

	public String getVersion() {
		return version;
	}

	public String getPathExt() {
		return Config.getAppDir() + path;
	}

	public void setImagePathExt(String imagePath) {
		if (imagePath.length() > 0 && imagePath.charAt(0) != '/') {
			imagePath = "/" + imagePath;
		}
		this.imagePath = path + imagePath;
	}

	public String getImagePathExt() {
		if (imagePath == null) {
			return null;
		}
		return Config.getAppDir() + imagePath;
	}

	public String getAuthorExt(Context context) {
		return context.getString(R.string.author) + author;
	}

	public String getVersionExt(Context context) {
		return context.getString(R.string.version) + version;
	}

}
