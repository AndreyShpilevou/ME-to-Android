package app.utils

import org.xmlpull.v1.XmlSerializer
import java.io.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.UnsupportedOperationException
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.*

class FastXmlSerializer private constructor(bufferSize: Int) : XmlSerializer {
    private val mBufferLen: Int
    private val mText: CharArray
    private var mPos = 0
    private var mWriter: Writer? = null
    private var mOutputStream: OutputStream? = null
    private var mCharset: CharsetEncoder? = null
    private val mBytes: ByteBuffer
    private var mIndent = false
    private var mInTag = false
    private var mNesting = 0
    private var mLineStart = true

    constructor() : this(DEFAULT_BUFFER_LEN) {}

    @Throws(IOException::class)
    private fun append(c: Char) {
        var pos = mPos
        if (pos >= mBufferLen - 1) {
            flush()
            pos = mPos
        }
        mText[pos] = c
        mPos = pos + 1
    }

    @Throws(IOException::class)
    private fun append(str: String, i: Int = 0, length: Int = str.length) {
        var i = i
        if (length > mBufferLen) {
            val end = i + length
            while (i < end) {
                val next = i + mBufferLen
                append(str, i, if (next < end) mBufferLen else end - i)
                i = next
            }
            return
        }
        var pos = mPos
        if (pos + length > mBufferLen) {
            flush()
            pos = mPos
        }
        str.toCharArray(mText, pos, i, i + length)
        mPos = pos + length
    }

    @Throws(IOException::class)
    private fun append(buf: CharArray, i: Int, length: Int) {
        var i = i
        if (length > mBufferLen) {
            val end = i + length
            while (i < end) {
                val next = i + mBufferLen
                append(buf, i, if (next < end) mBufferLen else end - i)
                i = next
            }
            return
        }
        var pos = mPos
        if (pos + length > mBufferLen) {
            flush()
            pos = mPos
        }
        System.arraycopy(buf, i, mText, pos, length)
        mPos = pos + length
    }

    @Throws(IOException::class)
    private fun appendIndent(indent: Int) {
        var indent = indent
        indent *= 4
        if (indent > sSpace.length) {
            indent = sSpace.length
        }
        append(sSpace, 0, indent)
    }

    @Throws(IOException::class)
    private fun escapeAndAppendString(string: String) {
        val N = string.length
        val NE = ESCAPE_TABLE.size.toChar()
        val escapes = ESCAPE_TABLE
        var lastPos = 0
        var pos: Int
        pos = 0
        while (pos < N) {
            val c = string[pos]
            if (c >= NE) {
                pos++
                continue
            }
            val escape = escapes[c.code]
            if (escape == null) {
                pos++
                continue
            }
            if (lastPos < pos) append(string, lastPos, pos - lastPos)
            lastPos = pos + 1
            append(escape)
            pos++
        }
        if (lastPos < pos) append(string, lastPos, pos - lastPos)
    }

    @Throws(IOException::class)
    private fun escapeAndAppendString(buf: CharArray, start: Int, len: Int) {
        val NE = ESCAPE_TABLE.size.toChar()
        val escapes = ESCAPE_TABLE
        val end = start + len
        var lastPos = start
        var pos: Int
        pos = start
        while (pos < end) {
            val c = buf[pos]
            if (c >= NE) {
                pos++
                continue
            }
            val escape = escapes[c.code]
            if (escape == null) {
                pos++
                continue
            }
            if (lastPos < pos) append(buf, lastPos, pos - lastPos)
            lastPos = pos + 1
            append(escape)
            pos++
        }
        if (lastPos < pos) append(buf, lastPos, pos - lastPos)
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun attribute(namespace: String, name: String, value: String): XmlSerializer {
        append(' ')
        if (namespace != null) {
            append(namespace)
            append(':')
        }
        append(name)
        append("=\"")
        escapeAndAppendString(value)
        append('"')
        mLineStart = false
        return this
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun cdsect(text: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun comment(text: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun docdecl(text: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun endDocument() {
        flush()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun endTag(namespace: String, name: String): XmlSerializer {
        mNesting--
        if (mInTag) {
            append(" />\n")
        } else {
            if (mIndent && mLineStart) {
                appendIndent(mNesting)
            }
            append("</")
            if (namespace != null) {
                append(namespace)
                append(':')
            }
            append(name)
            append(">\n")
        }
        mLineStart = true
        mInTag = false
        return this
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun entityRef(text: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class)
    private fun flushBytes() {
        var position: Int
        if (mBytes.position().also { position = it } > 0) {
            mBytes.flip()
            mOutputStream!!.write(mBytes.array(), 0, position)
            mBytes.clear()
        }
    }

    @Throws(IOException::class)
    override fun flush() {
        if (mPos > 0) {
            if (mOutputStream != null) {
                val charBuffer = CharBuffer.wrap(mText, 0, mPos)
                var result = mCharset!!.encode(charBuffer, mBytes, true)
                while (true) {
                    if (result.isError) {
                        throw IOException(result.toString())
                    } else if (result.isOverflow) {
                        flushBytes()
                        result = mCharset!!.encode(charBuffer, mBytes, true)
                        continue
                    }
                    break
                }
                flushBytes()
                mOutputStream!!.flush()
            } else {
                mWriter!!.write(mText, 0, mPos)
                mWriter!!.flush()
            }
            mPos = 0
        }
    }

    override fun getDepth(): Int {
        throw UnsupportedOperationException()
    }

    override fun getFeature(name: String): Boolean {
        throw UnsupportedOperationException()
    }

    override fun getName(): String {
        throw UnsupportedOperationException()
    }

    override fun getNamespace(): String {
        throw UnsupportedOperationException()
    }

    @Throws(IllegalArgumentException::class)
    override fun getPrefix(namespace: String, generatePrefix: Boolean): String {
        throw UnsupportedOperationException()
    }

    override fun getProperty(name: String): Any {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun ignorableWhitespace(text: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun processingInstruction(text: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun setFeature(name: String, state: Boolean) {
        if (name == "http://xmlpull.org/v1/doc/features.html#indent-output") {
            mIndent = true
            return
        }
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun setOutput(os: OutputStream, encoding: String) {
        requireNotNull(os)
        if (true) {
            mCharset = try {
                Charset.forName(encoding).newEncoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE)
            } catch (e: IllegalCharsetNameException) {
                throw (UnsupportedEncodingException(
                    encoding
                ).initCause(e) as UnsupportedEncodingException)
            } catch (e: UnsupportedCharsetException) {
                throw (UnsupportedEncodingException(
                    encoding
                ).initCause(e) as UnsupportedEncodingException)
            }
            mOutputStream = os
        } else {
            setOutput(
                if (encoding == null) OutputStreamWriter(os) else OutputStreamWriter(os, encoding)
            )
        }
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun setOutput(writer: Writer) {
        mWriter = writer
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun setPrefix(prefix: String, namespace: String) {
        throw UnsupportedOperationException()
    }

    @Throws(IllegalArgumentException::class, IllegalStateException::class)
    override fun setProperty(name: String, value: Any) {
        throw UnsupportedOperationException()
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun startDocument(encoding: String, standalone: Boolean) {
        append(
            """
    <?xml version='1.0' encoding='utf-8' standalone='${if (standalone) "yes" else "no"}' ?>
    
    """.trimIndent()
        )
        mLineStart = true
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun startTag(namespace: String, name: String): XmlSerializer {
        if (mInTag) {
            append(">\n")
        }
        if (mIndent) {
            appendIndent(mNesting)
        }
        mNesting++
        append('<')
        if (namespace != null) {
            append(namespace)
            append(':')
        }
        append(name)
        mInTag = true
        mLineStart = false
        return this
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun text(buf: CharArray, start: Int, len: Int): XmlSerializer {
        if (mInTag) {
            append(">")
            mInTag = false
        }
        escapeAndAppendString(buf, start, len)
        if (mIndent) {
            mLineStart = buf[start + len - 1] == '\n'
        }
        return this
    }

    @Throws(IOException::class, IllegalArgumentException::class, IllegalStateException::class)
    override fun text(text: String): XmlSerializer {
        if (mInTag) {
            append(">")
            mInTag = false
        }
        escapeAndAppendString(text)
        if (mIndent) {
            mLineStart = text.length > 0 && text[text.length - 1] == '\n'
        }
        return this
    }

    companion object {
        private val ESCAPE_TABLE = arrayOf(
            "&#0;", "&#1;", "&#2;", "&#3;", "&#4;", "&#5;", "&#6;", "&#7;",  // 0-7
            "&#8;", "&#9;", "&#10;", "&#11;", "&#12;", "&#13;", "&#14;", "&#15;",  // 8-15
            "&#16;", "&#17;", "&#18;", "&#19;", "&#20;", "&#21;", "&#22;", "&#23;",  // 16-23
            "&#24;", "&#25;", "&#26;", "&#27;", "&#28;", "&#29;", "&#30;", "&#31;",  // 24-31
            null, null, "&quot;", null, null, null, "&amp;", null,  // 32-39
            null, null, null, null, null, null, null, null,  // 40-47
            null, null, null, null, null, null, null, null,  // 48-55
            null, null, null, null, "&lt;", null, "&gt;", null
        )
        private const val DEFAULT_BUFFER_LEN = 32 * 1024
        private const val sSpace = "                                                              "
    }

    init {
        mBufferLen = if (bufferSize > 0) bufferSize else DEFAULT_BUFFER_LEN
        mText = CharArray(mBufferLen)
        mBytes = ByteBuffer.allocate(mBufferLen)
    }
}