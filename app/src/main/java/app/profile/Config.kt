package app.profile

import android.content.Context
import app.utils.FileUtils.isExternalStorageLegacy
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.SharedPreferences
import android.content.Intent
import android.net.Uri
import javax.microedition.shell.MicroActivity
import javax.microedition.util.ContextHolder
import ru.playsoftware.j2meloader.R
import android.os.Environment
import androidx.preference.PreferenceManager
import app.utils.Constants.ACTION_EDIT
import app.utils.Constants.KEY_MIDLET_NAME
import app.utils.Constants.KEY_START_ARGUMENTS
import app.utils.Constants.PREF_EMULATOR_DIR
import ru.playsoftware.j2meloader.BuildConfig
import java.io.File

object Config {
    const val DEX_OPT_CACHE_DIR = "dex_opt"
    const val MIDLET_CONFIG_FILE = "/config.json"
    const val MIDLET_CONFIGS_DIR = "/configs/"
    const val MIDLET_DATA_DIR = "/data/"
    const val MIDLET_DEX_FILE = "/converted.dex"
    const val MIDLET_ICON_FILE = "/icon.png"
    const val MIDLET_KEY_LAYOUT_FILE = "/VirtualKeyboardLayout"
    const val MIDLET_MANIFEST_FILE = "$MIDLET_DEX_FILE.conf"
    const val MIDLET_RES_DIR = "/res"
    const val MIDLET_RES_FILE = "/res.jar"

    @JvmField
	var SCREENSHOTS_DIR: String? = null

    const val SHADERS_DIR = "/shaders/"

    @JvmStatic
	var emulatorDir: String? = null
        private set

    @JvmStatic
	var dataDir: String? = null
        private set

    @JvmStatic
	var configsDir: String? = null
        private set

    @JvmStatic
	var profilesDir: String? = null
        private set

    @JvmStatic
	var appDir: String? = null
        private set

    private val sPrefListener =
        OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, key: String ->
            if (key == PREF_EMULATOR_DIR) {
                initDirs(sharedPreferences.getString(key, emulatorDir))
            }
        }
    val shadersDir: String
        get() = emulatorDir + SHADERS_DIR

    @JvmStatic
	@JvmOverloads
    fun startApp(
        context: Context,
        name: String,
        path: String,
        showSettings: Boolean,
        arguments: String? = null
    ) {
        val appDir = File(path)
        val workDir = appDir.parentFile?.parent
        val file = File(workDir + MIDLET_CONFIGS_DIR + appDir.name)
        if (showSettings || !file.exists()) {
            val intent = Intent(
                ACTION_EDIT, Uri.parse(path),
                context, ConfigActivity::class.java
            )
            intent.putExtra(KEY_MIDLET_NAME, name)
            intent.putExtra(KEY_START_ARGUMENTS, arguments)
            context.startActivity(intent)
        } else {
            val intent = Intent(
                Intent.ACTION_DEFAULT, Uri.parse(path),
                context, MicroActivity::class.java
            )
            intent.putExtra(KEY_MIDLET_NAME, name)
            intent.putExtra(KEY_START_ARGUMENTS, arguments)
            context.startActivity(intent)
        }
    }

    private fun initDirs(path: String?) {
        emulatorDir = path
        dataDir = emulatorDir + MIDLET_DATA_DIR
        configsDir = emulatorDir + MIDLET_CONFIGS_DIR
        profilesDir = "$emulatorDir/templates/"
        appDir = "$emulatorDir/converted/"
    }

    init {
        val context = ContextHolder.getAppContext()
        var appName = "J2ME-Loader"
        if (!BuildConfig.FULL_EMULATOR) {
            appName = context.getString(R.string.app_name)
        }
        SCREENSHOTS_DIR =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/" + appName
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        var path: String? = if (isExternalStorageLegacy()) preferences.getString(
            PREF_EMULATOR_DIR,
            null
        ) else context.getExternalFilesDir(null)?.path

        if (path == null) {
            path = Environment.getExternalStorageDirectory().toString() + "/" + appName
        }
        initDirs(path)
        preferences.registerOnSharedPreferenceChangeListener(sPrefListener)
    }
}