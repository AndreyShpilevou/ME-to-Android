package app.profile

import app.utils.FileUtils.copyFileUsingChannel
import app.utils.XmlUtils.readMapXml
import com.google.gson.GsonBuilder
import javax.microedition.lcdui.keyboard.VirtualKeyboard
import android.os.Build
import android.util.Log
import ru.playsoftware.j2meloader.config.Config
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import javax.microedition.util.ContextHolder

object ProfilesManager {
    private val TAG = ProfilesManager::class.java.name
    private val gson = GsonBuilder().setPrettyPrinting().create()
    @JvmStatic
	val profiles: ArrayList<Profile>
        get() {
            val root = File(Config.getProfilesDir())
            return getList(root)
        }

    private fun getList(root: File): ArrayList<Profile> {
        val dirs = root.listFiles() ?: return ArrayList()
        val size = dirs.size
        val profiles = arrayOfNulls<Profile>(size)
        for (i in 0 until size) {
            profiles[i] = Profile(dirs[i].name)
        }
        return ArrayList(Arrays.asList(*profiles))
    }

    @JvmStatic
	@Throws(IOException::class)
    fun load(from: Profile, toPath: String?, config: Boolean, keyboard: Boolean) {
        if (!config && !keyboard) {
            return
        }
        val dstConfig = File(toPath, Config.MIDLET_CONFIG_FILE)
        val dstKeyLayout = File(toPath, Config.MIDLET_KEY_LAYOUT_FILE)
        try {
            if (config) {
                val source = from.config
                if (source.exists()) copyFileUsingChannel(source, dstConfig) else {
                    val params = loadConfig(from.dir)
                    if (params != null) {
                        params.dir = dstConfig.parentFile
                        saveConfig(params)
                    }
                }
            }
            if (keyboard) copyFileUsingChannel(from.keyLayout, dstKeyLayout)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
	@Throws(IOException::class)
    fun save(profile: Profile, fromPath: String?, config: Boolean, keyboard: Boolean) {
        if (!config && !keyboard) {
            return
        }
        profile.create()
        val srcConfig = File(fromPath, Config.MIDLET_CONFIG_FILE)
        val srcKeyLayout = File(fromPath, Config.MIDLET_KEY_LAYOUT_FILE)
        try {
            if (config) copyFileUsingChannel(srcConfig, profile.config)
            if (keyboard) copyFileUsingChannel(srcKeyLayout, profile.keyLayout)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
	fun loadConfig(dir: File?): ProfileModel? {
        val file = File(dir, Config.MIDLET_CONFIG_FILE)
        var params: ProfileModel? = null
        if (file.exists()) {
            try {
                FileReader(file).use { reader ->
                    params = gson.fromJson(reader, ProfileModel::class.java)
                    params!!.dir = dir
                }
            } catch (e: Exception) {
                Log.e(TAG, "loadConfig: ", e)
            }
        }
        if (params == null) {
            val oldFile = File(dir, "config.xml")
            if (oldFile.exists()) {
                try {
                    FileInputStream(oldFile).use { `in` ->
                        val map = readMapXml(`in`)
                        val json = gson.toJsonTree(map)
                        params = gson.fromJson(json, ProfileModel::class.java)
                        params!!.dir = dir
                        // Fix keyboard shape for old configs
                        if (params!!.vkType == 1 || params!!.vkType == 2) {
                            params!!.vkButtonShape = VirtualKeyboard.ROUND_RECT_SHAPE
                        }
                        if (saveConfig(params) && oldFile.delete()) {
                            Log.d(TAG, "loadConfig: old config file deleted")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "loadConfig: ", e)
                }
            }
        }
        if (params == null) {
            return null
        }
        when (params!!.version) {
            0 -> {
                if (params!!.hwAcceleration) {
                    params!!.graphicsMode =
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) 2 else 3
                }
                updateSystemProperties(params!!)
                params!!.fontAA = true
                if (params!!.screenScaleToFit) {
                    if (params!!.screenKeepAspectRatio) {
                        params!!.screenScaleType = 1
                    } else {
                        params!!.screenScaleType = 2
                    }
                } else {
                    params!!.screenScaleType = 0
                }
                params!!.version = ProfileModel.VERSION
                saveConfig(params)
            }
            1 -> {
                params!!.fontAA = true
                if (params!!.screenScaleToFit) {
                    if (params!!.screenKeepAspectRatio) {
                        params!!.screenScaleType = 1
                    } else {
                        params!!.screenScaleType = 2
                    }
                } else {
                    params!!.screenScaleType = 0
                }
                params!!.version = ProfileModel.VERSION
                saveConfig(params)
            }
            2 -> {
                if (params!!.screenScaleToFit) {
                    if (params!!.screenKeepAspectRatio) {
                        params!!.screenScaleType = 1
                    } else {
                        params!!.screenScaleType = 2
                    }
                } else {
                    params!!.screenScaleType = 0
                }
                params!!.version = ProfileModel.VERSION
                saveConfig(params)
            }
        }
        return params
    }

    @JvmStatic
	fun saveConfig(p: ProfileModel?): Boolean {
        try {
            FileWriter(File(p!!.dir, Config.MIDLET_CONFIG_FILE)).use { writer ->
                gson.toJson(p, writer)
                writer.close()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveConfig: ", e)
        }
        return false
    }

    fun updateSystemProperties(params: ProfileModel) {
        val defaultProperties = ContextHolder.getAssetAsString("defaults/system.props")
        val properties = params.systemProperties
        val sb = StringBuilder()
        if (properties == null) {
            params.systemProperties = defaultProperties
            return
        }
        sb.append(properties)
        val defaults = defaultProperties.split("[\\n\\r]+".toRegex()).toTypedArray()
        for (line in defaults) {
            if (properties.contains(line.substring(0, line.indexOf(':')))) continue
            sb.append(line).append('\n')
        }
        params.systemProperties = sb.toString()
    }
}