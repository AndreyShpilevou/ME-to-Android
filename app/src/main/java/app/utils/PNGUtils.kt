package app.utils

import app.utils.IOUtils.toByteArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import ar.com.hjg.pngj.PngReaderInt
import ar.com.hjg.pngj.chunks.PngChunkTRNS
import ar.com.hjg.pngj.chunks.PngChunkPLTE
import ar.com.hjg.pngj.ImageLineSetDefault
import ar.com.hjg.pngj.ImageLineInt
import ar.com.hjg.pngj.ImageLineHelper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.util.*

object PNGUtils {

    private val PNG_SIGNATURE = byteArrayOf(-119, 80, 78, 71, 13, 10, 26, 10)

    @JvmStatic
    fun getFixedBitmap(stream: InputStream?): Bitmap? {
        try {
            val data = toByteArray(stream!!)
            return getFixedBitmap(data, 0, data.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    @JvmStatic
    fun getFixedBitmap(imageData: ByteArray?, imageOffset: Int, imageLength: Int): Bitmap? {
        var b: Bitmap? = null
        val signature = Arrays.copyOfRange(imageData, imageOffset, imageOffset + PNG_SIGNATURE.size)
        if (Arrays.equals(signature, PNG_SIGNATURE)) {
            try {
                ByteArrayInputStream(imageData, imageOffset, imageLength).use { stream ->
                    b = fixPNG(stream)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                b = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength)
            }
        } else {
            b = BitmapFactory.decodeByteArray(imageData, imageOffset, imageLength)
        }
        return b
    }

    @Throws(IOException::class)
    private fun fixPNG(stream: InputStream): Bitmap {
        val reader = PngReaderInt(stream)
        reader.setCrcCheckDisabled()
        val imageInfo = reader.imgInfo
        val width = imageInfo.cols
        val height = imageInfo.rows
        val trns = reader.metadata.trns
        val plte = reader.metadata.plte
        val lineSet: ImageLineSetDefault<ImageLineInt> = reader.readRows() as ImageLineSetDefault<ImageLineInt>
        val pix = IntArray(width * height)
        val buf = IntArray(width)
        for (i in 0 until height) {
            val lineInt = lineSet.getImageLine(i)
            ImageLineHelper.scaleUp(lineInt)
            val r = lineToARGB32(lineInt, plte, trns, buf)
            for (j in 0 until width) {
                pix[i * width + j] = r[j]
            }
        }
        reader.end()
        return Bitmap.createBitmap(pix, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun lineToARGB32(
        line: ImageLineInt,
        pal: PngChunkPLTE,
        trns: PngChunkTRNS?,
        buf: IntArray
    ): IntArray {
        var bufTmp: IntArray = buf
        val alphachannel = line.imgInfo.alpha
        val scanline = line.scanline
        val cols = line.imgInfo.cols
        if (bufTmp == null || buf.size < cols) bufTmp = IntArray(cols)
        var index: Int
        var rgb: Int
        var alpha: Int
        val ga: Int
        var g: Int
        if (line.imgInfo.indexed) { // palette
            val nindexesWithAlpha = trns?.palletteAlpha?.size ?: 0
            for (c in 0 until cols) {
                index = scanline[c]
                rgb = pal.getEntry(index)
                alpha = if (index < nindexesWithAlpha) trns!!.palletteAlpha[index] else 255
                bufTmp[c] = alpha shl 24 or rgb
            }
        } else if (line.imgInfo.greyscale) { // gray
            if (trns != null) {
                ga = ImageLineHelper.scaleUp(line.imgInfo.bitDepth, trns.gray.toByte()).toInt() and 0xFF
            } else {
                ga = -1
            }
            var c = 0
            var c2 = 0
            while (c < cols) {
                g = scanline[c2++]
                alpha = if (alphachannel) scanline[c2++] else if (g != ga) 255 else 0
                bufTmp[c] = alpha shl 24 or g or (g shl 8) or (g shl 16)
                c++
            }
        } else if (line.imgInfo.bitDepth == 16) {
            ga = trns?.rgB888 ?: -1
            var c = 0
            var c2 = 0
            while (c < cols) {
                rgb = (scanline[c2++] and 0xFF00 shl 8 or (scanline[c2++] and 0xFF00)
                        or (scanline[c2++] and 0xFF00 shr 8))
                alpha =
                    if (alphachannel) scanline[c2++] and 0xFF00 shr 8 else if (rgb != ga) 255 else 0
                bufTmp[c] = alpha shl 24 or rgb
                c++
            }
        } else {
            ga = trns?.rgB888 ?: -1
            var c = 0
            var c2 = 0
            while (c < cols) {
                rgb = (scanline[c2++] shl 16 or (scanline[c2++] shl 8)
                        or scanline[c2++])
                alpha = if (alphachannel) scanline[c2++] else if (rgb != ga) 255 else 0
                bufTmp[c] = alpha shl 24 or rgb
                c++
            }
        }
        return bufTmp
    }
}