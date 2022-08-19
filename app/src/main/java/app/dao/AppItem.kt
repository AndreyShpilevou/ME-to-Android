package app.dao

import android.content.Context
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.playsoftware.j2meloader.R
import ru.playsoftware.j2meloader.config.Config

@Entity(tableName = "apps", indices = [Index(value = ["path"], unique = true)])
class AppItem(val path: String, var title: String, val author: String, val version: String) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var imagePath: String? = null
    val pathExt: String
        get() = Config.getAppDir() + path

    fun setImagePathExt(imagePath: String) {
        var imagePath = imagePath
        if (imagePath.isNotEmpty() && imagePath[0] != '/') {
            imagePath = "/$imagePath"
        }
        this.imagePath = path + imagePath
    }

    val imagePathExt: String?
        get() = if (imagePath == null) {
            null
        } else Config.getAppDir() + imagePath

    fun getAuthorExt(context: Context): String {
        return context.getString(R.string.author) + author
    }

    fun getVersionExt(context: Context): String {
        return context.getString(R.string.version) + version
    }
}