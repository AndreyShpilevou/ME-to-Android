package app.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import app.profile.Config
import java.io.*

object FileUtils {
    private val TAG = FileUtils::class.java.name
    private const val TEMP_JAR_NAME = "tmp.jar"
    private const val TEMP_JAD_NAME = "tmp.jad"
    private const val TEMP_KJX_NAME = "tmp.kjx"
    private const val BUFFER_SIZE = 1024
    const val ILLEGAL_FILENAME_CHARS = "[/\\\\:*?\"<>|]"
    @JvmStatic
    fun copyFiles(src: File, dst: File, filter: FilenameFilter?) {
        if (!dst.exists() && !dst.mkdirs()) {
            Log.e(TAG, "copyFiles() failed create dir: $dst")
            return
        }
        val list = src.listFiles(filter) ?: return
        for (file in list) {
            val to = File(dst, file.name)
            if (file.isDirectory) {
                copyFiles(src, to, filter)
            } else {
                try {
                    copyFileUsingChannel(file, to)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copyFileUsingChannel(source: File?, dest: File?) {
        FileInputStream(source).channel.use { sourceChannel ->
            FileOutputStream(dest).channel.use { destChannel ->
                destChannel.transferFrom(
                    sourceChannel,
                    0,
                    sourceChannel.size()
                )
            }
        }
    }

    @JvmStatic
    fun deleteDirectory(dir: File) {
        if (dir.isDirectory) {
            val listFiles = dir.listFiles()
            if (listFiles != null && listFiles.isNotEmpty()) {
                for (file in listFiles) {
                    deleteDirectory(file)
                }
            }
        }
        if (!dir.delete() && dir.exists()) {
            Log.w(TAG, "Can't delete file: $dir")
        }
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getFileForUri(context: Context, uri: Uri): File {
        if ("file" == uri.scheme) {
            val path = uri.path
            if (path != null) {
                val file = File(path)
                if (file.exists()) {
                    return file
                }
            }
        }
        if (context.packageName + ".provider" == uri.authority) {
            try {
                val file = getFileForUri(uri)
                if (file.isFile) {
                    return file
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val tmpDir = File(context.cacheDir, "installer")
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw IOException("Can't create directory: $tmpDir")
        }
        var file: File
        context.contentResolver.openInputStream(uri).use { stream ->
            val buf = ByteArray(BUFFER_SIZE)
            var len: Int = 0
            if (stream == null || stream.read(buf).also { len = it } == -1)
                throw IOException("Can't read data from uri: $uri")

            file = if (buf[0].toInt() == 0x50 && buf[1].toInt() == 0x4B) {
                File(tmpDir, TEMP_JAR_NAME)
            } else if (buf[0] == 'K'.code.toByte() && buf[1] == 'J'.code.toByte() && buf[2] == 'X'.code.toByte()) {
                File(tmpDir, TEMP_KJX_NAME)
            } else {
                File(tmpDir, TEMP_JAD_NAME)
            }
            FileOutputStream(file).use { out ->
                out.write(buf, 0, len)
                while (stream.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
            }
        }
        return file
    }

    @JvmStatic
    @Throws(IOException::class)
    fun getBytes(file: File): ByteArray {
        DataInputStream(FileInputStream(file)).use { dis ->
            val b = ByteArray(file.length().toInt())
            dis.readFully(b)
            return b
        }
    }

    @JvmStatic
    fun clearDirectory(dir: File) {
        if (!dir.isDirectory) return
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                deleteDirectory(dir)
            } else {
                file.delete()
            }
        }
    }

    fun moveFiles(src: File, dst: File) {
        if (src.renameTo(dst)) return
        val files = src.listFiles() ?: return
        if (!dst.mkdirs()) {
            Log.e(TAG, "moveFiles() can't create directory: $dst")
        }
        for (file in files) {
            val to = File(dst, file.name)
            if (file.isDirectory) {
                moveFiles(file, to)
            } else if (!file.renameTo(to)) {
                try {
                    copyFileUsingChannel(file, to)
                    if (!file.delete()) {
                        Log.e(TAG, "moveFiles() can't delete: $file")
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "moveFiles() can't move [$file] to [$to]", e)
                }
            }
        }
        if (!src.delete()) {
            Log.e(TAG, "moveFiles() can't delete: $src")
        }
    }

    @JvmStatic
    fun getText(path: String): String {
        try {
            DataInputStream(FileInputStream(path)).use { dis ->
                val buf = ByteArray(dis.available())
                dis.readFully(buf)
                return String(buf)
            }
        } catch (e: IOException) {
            Log.e(TAG, "getText: $path", e)
        }
        return ""
    }

    @JvmStatic
    fun initWorkDir(dir: File): Boolean {
        if ((dir.isDirectory || dir.mkdirs()) && dir.canWrite()) {
            File(dir, Config.SHADERS_DIR).mkdir()
            try {
                val nomedia = File(dir, ".nomedia")
                nomedia.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return true
        }
        return false
    }

    @JvmStatic
    fun isExternalStorageLegacy() : Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || Environment.isExternalStorageLegacy()
    }

    @JvmStatic
    fun getFilePicker() = SAFFileResultContract()
    //if (isExternalStorageLegacy()) {
//            SAFFileResultContract()
//            //PickFileResultContract()
//        } else {
//            SAFFileResultContract()
//        }


    fun getFileForUri(uri: Uri): File {
        var path = uri.encodedPath
        val splitIndex = path!!.indexOf('/', 1)
        val tag = Uri.decode(path.substring(1, splitIndex))
        path = Uri.decode(path.substring(splitIndex + 1))
        require("root".equals(tag, ignoreCase = true)) {
            String.format("Can't decode paths to '%s', only for 'root' paths.", tag)
        }
        val root = File("/")
        var file = File(root, path)
        file = try {
            file.canonicalFile
        } catch (e: IOException) {
            throw IllegalArgumentException("Failed to resolve canonical path for $file")
        }
        if (!file.path.startsWith(root.path)) {
            throw SecurityException("Resolved path jumped beyond configured root")
        }
        return file
    }
}