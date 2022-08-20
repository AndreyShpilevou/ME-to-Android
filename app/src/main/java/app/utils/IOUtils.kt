package app.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object IOUtils {

    private const val BUFFER_SIZE = 16384

    @JvmStatic
    @Throws(IOException::class)
    fun toByteArray(stream: InputStream): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(BUFFER_SIZE)
        var len: Int
        while (stream.read(buf).also { len = it } != -1) {
            outputStream.write(buf, 0, len)
        }
        return outputStream.toByteArray()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream) {
        val buf = ByteArray(BUFFER_SIZE)
        var len: Int
        while (input.read(buf).also { len = it } != -1) {
            output.write(buf, 0, len)
        }
    }
}