package app.profile

import app.utils.FileUtils.deleteDirectory
import ru.playsoftware.j2meloader.config.Config
import java.io.File
import java.util.*

class Profile(var name: String) : Comparable<Profile> {
    fun create() {
        dir.mkdirs()
    }

    fun renameTo(newName: String): Boolean {
        val oldDir = dir
        val newDir = File(Config.getProfilesDir(), newName)
        name = newName
        return oldDir.renameTo(newDir)
    }

    fun delete() {
        deleteDirectory(dir)
    }

    val dir: File
        get() = File(Config.getProfilesDir(), name)
    val config: File
        get() = File(Config.getProfilesDir(), name + Config.MIDLET_CONFIG_FILE)
    val keyLayout: File
        get() = File(Config.getProfilesDir(), name + Config.MIDLET_KEY_LAYOUT_FILE)

    override fun toString(): String {
        return name
    }

    override fun compareTo(o: Profile): Int {
        return name.lowercase(Locale.getDefault()).compareTo(o.name.lowercase(Locale.getDefault()))
    }

    fun hasConfig(): Boolean {
        return config.exists()
    }

    fun hasKeyLayout(): Boolean {
        return keyLayout.exists()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        return if (obj !is Profile) {
            false
        } else name == obj.name
    }

    fun hasOldConfig(): Boolean {
        return File(Config.getProfilesDir(), "$name/config.xml").exists()
    }
}