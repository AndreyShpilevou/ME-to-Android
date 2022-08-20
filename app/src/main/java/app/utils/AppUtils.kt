package app.utils

import app.dao.AppItem
import app.dao.AppRepository
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import ru.playsoftware.j2meloader.config.Config
import ru.woesss.j2me.jar.Descriptor
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

object AppUtils {
    private val TAG = AppUtils::class.java.simpleName

    private fun getAppsList(appFolders: List<String>): ArrayList<AppItem> {
        val apps = ArrayList<AppItem>()
        val appsDir = File(Config.getAppDir())
        for (appFolderName in appFolders) {
            val appFolder = File(appsDir, appFolderName)
            if (!appFolder.isDirectory) {
                if (!appFolder.delete()) {
                    Log.e(TAG, "getAppsList() failed delete file: $appFolder")
                }
                continue
            }
            val dex = File(appFolder, Config.MIDLET_DEX_FILE)
            if (!dex.isFile) {
                FileUtils.deleteDirectory(appFolder)
                continue
            }
            try {
                val item = getApp(appFolder)
                apps.add(item)
            } catch (e: Exception) {
                Log.w(TAG, "getAppsList: ", e)
                FileUtils.deleteDirectory(appFolder)
            }
        }
        return apps
    }

    @Throws(IOException::class)
    private fun getApp(appDir: File): AppItem {
        val mf = File(appDir, Config.MIDLET_MANIFEST_FILE)
        val params = Descriptor(mf, false)
        val item = AppItem(
            appDir.name, params.name,
            params.vendor,
            params.version
        )
        var icon = File(appDir, Config.MIDLET_ICON_FILE)
        if (icon.exists()) {
            item.setImagePathExt(Config.MIDLET_ICON_FILE)
        } else {
            val iconPath = Config.MIDLET_RES_DIR + '/' + params.icon
            icon = File(appDir, iconPath)
            if (icon.exists()) {
                item.setImagePathExt(iconPath)
            }
        }
        return item
    }

    @JvmStatic
    @Throws(IOException::class)
    fun findApp(name: String?, vendor: String?, uid: String?): AppItem? {
        val appsDir = File(Config.getAppDir())
        for (appFolderName in appsDir.list()!!) {
            val appDir = File(appsDir, appFolderName)
            if (!appDir.isDirectory) {
                continue
            }
            val dex = File(appDir, Config.MIDLET_DEX_FILE)
            if (!dex.isFile) {
                FileUtils.deleteDirectory(appDir)
                continue
            }
            try {
                val mf = File(appDir, Config.MIDLET_MANIFEST_FILE)
                val params = Descriptor(mf, false)
                if (uid != null && params.nokiaUID != null && params.nokiaUID.equals(
                        uid,
                        ignoreCase = true
                    ) ||
                    name != null && params.name.equals(name, ignoreCase = true) &&
                    (vendor == null || params.vendor.equals(vendor, ignoreCase = true))
                ) {
                    return AppItem(
                        appDir.name, params.name,
                        params.vendor,
                        params.version
                    )
                }
            } catch (e: Exception) {
            }
        }
        return null
    }

    fun deleteApp(item: AppItem) {
        val appDir = File(item.pathExt)
        FileUtils.deleteDirectory(appDir)
        val appSaveDir = File(Config.getDataDir(), item.path)
        FileUtils.deleteDirectory(appSaveDir)
        val appConfigsDir = File(Config.getConfigsDir(), item.path)
        FileUtils.deleteDirectory(appConfigsDir)
    }

    fun updateDb(appRepository: AppRepository, items: MutableList<AppItem>) {
        val tmp = File(Config.getAppDir(), ".tmp")
        if (tmp.exists()) {
            FileUtils.deleteDirectory(tmp)
        }
        val appFolders = File(Config.getAppDir()).list()
        if (appFolders == null || appFolders.isEmpty()) {
            if (items.size != 0) {
                appRepository.deleteAll()
            }
            return
        }
        val appFoldersList: MutableList<String> = ArrayList(listOf(*appFolders))
        val iterator = items.listIterator(items.size)
        while (iterator.hasPrevious()) {
            val item = iterator.previous()
            if (appFoldersList.remove(item.path)) {
                iterator.remove()
            }
        }
        if (items.size > 0) {
            appRepository.delete(items)
        }
        if (appFoldersList.size > 0) {
            appRepository.insert(getAppsList(appFoldersList))
        }
    }

    fun getIconBitmap(appItem: AppItem): Bitmap? {
        val file = appItem.imagePathExt ?: return null
        return BitmapFactory.decodeFile(file)
    }
}