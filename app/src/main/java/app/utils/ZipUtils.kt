package app.utils

import net.lingala.zip4j.ZipFile
import java.io.*

object ZipUtils {
    private const val BUFFER_SIZE = 8096
    @JvmStatic
    @Throws(IOException::class)
    fun unzipEntry(srcZip: File, name: String, dst: File?) {
        val zip = ZipFile(srcZip)
        val entry = zip.getFileHeader(name)
            ?: throw IOException("Entry '$name' not found in zip: $srcZip")
        BufferedInputStream(zip.getInputStream(entry)).use { bis ->
            BufferedOutputStream(
                FileOutputStream(dst), BUFFER_SIZE
            ).use { bos ->
                val data = ByteArray(BUFFER_SIZE)
                var read: Int
                while (bis.read(data).also { read = it } != -1) {
                    bos.write(data, 0, read)
                }
            }
        }
    }
}