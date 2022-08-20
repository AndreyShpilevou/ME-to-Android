package app.utils

import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlSerializer
import org.xmlpull.v1.XmlPullParser
import android.util.Xml
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.NullPointerException
import java.lang.NumberFormatException
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

object XmlUtils {

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeMapXml(map: Map<*, *>?, out: OutputStream?) {
        val serializer: XmlSerializer = FastXmlSerializer()
        serializer.setOutput(out, "UTF-8")
        serializer.startDocument(null, true)
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
        writeMapXml(map, null, serializer)
        serializer.endDocument()
    }

    @JvmOverloads
    @Throws(XmlPullParserException::class, IOException::class)
    fun writeMapXml(
        map: Map<*, *>?,
        name: String?,
        out: XmlSerializer,
        callback: WriteMapCallback? = null
    ) {
        if (map == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "map")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        writeMapXml(map, out, callback)
        out.endTag(null, "map")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeMapXml(map: Map<*, *>?, out: XmlSerializer, callback: WriteMapCallback?) {
        if (map == null) {
            return
        }
        val s: Set<*> = map.entries
        for (value in s) {
            val (key, value1) = value as Map.Entry<*, *>
            writeValueXml(value1, key as String?, out, callback)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeListXml(list: List<*>?, name: String?, out: XmlSerializer) {
        if (list == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "list")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        var i = 0
        while (i < list.size) {
            writeValueXml(list[i], null, out)
            i++
        }
        out.endTag(null, "list")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeSetXml(set: Set<*>?, name: String?, out: XmlSerializer) {
        if (set == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "set")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        for (v in set) {
            writeValueXml(v, null, out)
        }
        out.endTag(null, "set")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeByteArrayXml(array: ByteArray?, name: String?, out: XmlSerializer) {
        if (array == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "byte-array")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "num", array.size.toString())
        val sb = StringBuilder(array.size * 2)
        for (element in array) {
            val b = element.toInt()
            var h = b shr 4 and 0x0f
            sb.append((if (h >= 10) 'a'.code + h - 10 else '0'.code + h).toChar())
            h = b and 0x0f
            sb.append((if (h >= 10) 'a'.code + h - 10 else '0'.code + h).toChar())
        }
        out.text(sb.toString())
        out.endTag(null, "byte-array")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeIntArrayXml(array: IntArray?, name: String?, out: XmlSerializer) {
        if (array == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "int-array")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "num", array.size.toString())
        for (element in array) {
            out.startTag(null, "item")
            out.attribute(null, "value", element.toString())
            out.endTag(null, "item")
        }
        out.endTag(null, "int-array")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeLongArrayXml(array: LongArray?, name: String?, out: XmlSerializer) {
        if (array == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "long-array")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "num", array.size.toString())
        for (element in array) {
            out.startTag(null, "item")
            out.attribute(null, "value", element.toString())
            out.endTag(null, "item")
        }
        out.endTag(null, "long-array")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeDoubleArrayXml(array: DoubleArray?, name: String?, out: XmlSerializer) {
        if (array == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "double-array")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "num", array.size.toString())
        for (element in array) {
            out.startTag(null, "item")
            out.attribute(null, "value", element.toString())
            out.endTag(null, "item")
        }
        out.endTag(null, "double-array")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeStringArrayXml(array: Array<String?>?, name: String?, out: XmlSerializer) {
        if (array == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "string-array")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "num", array.size.toString())
        for (element in array) {
            out.startTag(null, "item")
            out.attribute(null, "value", element)
            out.endTag(null, "item")
        }
        out.endTag(null, "string-array")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeBooleanArrayXml(array: BooleanArray?, name: String?, out: XmlSerializer) {
        if (array == null) {
            out.startTag(null, "null")
            out.endTag(null, "null")
            return
        }
        out.startTag(null, "boolean-array")
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "num", array.size.toString())
        for (element in array) {
            out.startTag(null, "item")
            out.attribute(null, "value", element.toString())
            out.endTag(null, "item")
        }
        out.endTag(null, "boolean-array")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun writeValueXml(v: Any?, name: String?, out: XmlSerializer) {
        writeValueXml(v, name, out, null)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun writeValueXml(
        v: Any?, name: String?, out: XmlSerializer,
        callback: WriteMapCallback?
    ) {
        val typeStr = if (v == null) {
            out.startTag(null, "null")
            if (name != null) {
                out.attribute(null, "name", name)
            }
            out.endTag(null, "null")
            return
        } else if (v is String) {
            out.startTag(null, "string")
            if (name != null) {
                out.attribute(null, "name", name)
            }
            out.text(v.toString())
            out.endTag(null, "string")
            return
        } else if (v is Int) {
            "int"
        } else if (v is Long) {
            "long"
        } else if (v is Float) {
            "float"
        } else if (v is Double) {
            "double"
        } else if (v is Boolean) {
            "boolean"
        } else if (v is ByteArray) {
            writeByteArrayXml(v as ByteArray?, name, out)
            return
        } else if (v is IntArray) {
            writeIntArrayXml(v as IntArray?, name, out)
            return
        } else if (v is LongArray) {
            writeLongArrayXml(v as LongArray?, name, out)
            return
        } else if (v is DoubleArray) {
            writeDoubleArrayXml(v as DoubleArray?, name, out)
            return
        } else if (v is Array<*>) {
            writeStringArrayXml(v as Array<String?>, name, out)
            return
        } else if (v is BooleanArray) {
            writeBooleanArrayXml(v as BooleanArray?, name, out)
            return
        } else if (v is Map<*, *>) {
            writeMapXml(v as Map<*, *>?, name, out)
            return
        } else if (v is List<*>) {
            writeListXml(v as List<*>?, name, out)
            return
        } else if (v is Set<*>) {
            writeSetXml(v as Set<*>?, name, out)
            return
        } else if (v is CharSequence) {
            out.startTag(null, "string")
            if (name != null) {
                out.attribute(null, "name", name)
            }
            out.text(v.toString())
            out.endTag(null, "string")
            return
        } else if (callback != null) {
            callback.writeUnknownObject(v, name, out)
            return
        } else {
            throw RuntimeException("writeValueXml: unable to write value $v")
        }
        out.startTag(null, typeStr)
        if (name != null) {
            out.attribute(null, "name", name)
        }
        out.attribute(null, "value", v.toString())
        out.endTag(null, typeStr)
    }

    @JvmStatic
    @Throws(XmlPullParserException::class, IOException::class)
    fun readMapXml(stream: InputStream?): HashMap<String, Any>? {
        val parser = Xml.newPullParser()
        parser.setInput(stream, "UTF-8")
        return readValueXml(parser, arrayOfNulls(1)) as HashMap<String, Any>?
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisMapXml(
        parser: XmlPullParser, endTag: String,
        name: Array<String?>, callback: ReadMapCallback?
    ): HashMap<String?, *> {
        val map = HashMap<String?, Any?>()
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                map[name[0]] = readThisValueXml(parser, name, callback, false)
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.name == endTag) {
                    return map
                }
                throw XmlPullParserException(
                    "Expected " + endTag + " end tag at: " + parser.name
                )
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException(
            "Document ended before $endTag end tag"
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisArrayMapXml(
        parser: XmlPullParser, endTag: String,
        name: Array<String?>, callback: ReadMapCallback?
    ): HashMap<String?, *> {
        val map = HashMap<String?, Any?>()
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                map[name[0]] = readThisValueXml(parser, name, callback, true)
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.name == endTag) {
                    return map
                }
                throw XmlPullParserException(
                    "Expected " + endTag + " end tag at: " + parser.name
                )
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException(
            "Document ended before $endTag end tag"
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readThisListXml(
        parser: XmlPullParser, endTag: String,
        name: Array<String?>, callback: ReadMapCallback?, arrayMap: Boolean
    ): ArrayList<*> {
        val list: ArrayList<*> = ArrayList<Any?>()
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                list.addAll(
                    readThisValueXml(
                        parser,
                        name,
                        callback,
                        arrayMap
                    ) as Collection<Nothing>
                )
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.name == endTag) {
                    return list
                }
                throw XmlPullParserException(
                    "Expected " + endTag + " end tag at: " + parser.name
                )
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException(
            "Document ended before $endTag end tag"
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readThisSetXml(
        parser: XmlPullParser, endTag: String, name: Array<String?>,
        callback: ReadMapCallback?, arrayMap: Boolean
    ): HashSet<*> {
        val set: HashSet<*> = HashSet<Any?>()
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                set.add(readThisValueXml(parser, name, callback, arrayMap) as Nothing)
            } else if (eventType == XmlPullParser.END_TAG) {
                if (parser.name == endTag) {
                    return set
                }
                throw XmlPullParserException(
                    "Expected " + endTag + " end tag at: " + parser.name
                )
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException(
            "Document ended before $endTag end tag"
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisByteArrayXml(
        parser: XmlPullParser,
        endTag: String, name: Array<String?>?
    ): ByteArray {
        val num = try {
            parser.getAttributeValue(null, "num").toInt()
        } catch (e: NullPointerException) {
            throw XmlPullParserException(
                "Need num attribute in byte-array"
            )
        } catch (e: NumberFormatException) {
            throw XmlPullParserException(
                "Not a number in num attribute in byte-array"
            )
        }
        val array = ByteArray(num)
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.TEXT) {
                if (num > 0) {
                    val values = parser.text
                    if (values == null || values.length != num * 2) {
                        throw XmlPullParserException(
                            "Invalid value found in byte-array: $values"
                        )
                    }
                    for (i in 0 until num) {
                        val nibbleHighChar = values[2 * i]
                        val nibbleLowChar = values[2 * i + 1]
                        val nibbleHigh =
                            if (nibbleHighChar > 'a') nibbleHighChar - 'a' + 10 else nibbleHighChar - '0'
                        val nibbleLow =
                            if (nibbleLowChar > 'a') nibbleLowChar - 'a' + 10 else nibbleLowChar - '0'
                        array[i] = (nibbleHigh and 0x0F shl 4 or (nibbleLow and 0x0F)).toByte()
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                return if (parser.name == endTag) {
                    array
                } else {
                    throw XmlPullParserException(
                        "Expected " + endTag + " end tag at: "
                                + parser.name
                    )
                }
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException(
            "Document ended before $endTag end tag"
        )
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisIntArrayXml(
        parser: XmlPullParser,
        endTag: String, name: Array<String?>?
    ): IntArray {
        val num = try {
            parser.getAttributeValue(null, "num").toInt()
        } catch (e: NullPointerException) {
            throw XmlPullParserException("Need num attribute in int-array")
        } catch (e: NumberFormatException) {
            throw XmlPullParserException("Not a number in num attribute in int-array")
        }
        parser.next()
        val array = IntArray(num)
        var i = 0
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "item") {
                    try {
                        array[i] = parser.getAttributeValue(null, "value").toInt()
                    } catch (e: NullPointerException) {
                        throw XmlPullParserException("Need value attribute in item")
                    } catch (e: NumberFormatException) {
                        throw XmlPullParserException("Not a number in value attribute in item")
                    }
                } else {
                    throw XmlPullParserException("Expected item tag at: ${parser.name}")
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                when (parser.name) {
                    endTag -> return array
                    "item" -> i++
                    else -> throw XmlPullParserException("Expected $endTag end tag at: ${parser.name}")
                }
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException("Document ended before $endTag end tag")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisLongArrayXml(
        parser: XmlPullParser,
        endTag: String,
        name: Array<String?>?
    ): LongArray {
        val num = try {
            parser.getAttributeValue(null, "num").toInt()
        } catch (e: NullPointerException) {
            throw XmlPullParserException("Need num attribute in long-array")
        } catch (e: NumberFormatException) {
            throw XmlPullParserException("Not a number in num attribute in long-array")
        }
        parser.next()
        val array = LongArray(num)
        var i = 0
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "item") {
                    try {
                        array[i] = parser.getAttributeValue(null, "value").toLong()
                    } catch (e: NullPointerException) {
                        throw XmlPullParserException("Need value attribute in item")
                    } catch (e: NumberFormatException) {
                        throw XmlPullParserException("Not a number in value attribute in item")
                    }
                } else {
                    throw XmlPullParserException("Expected item tag at: ${parser.name}")
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                when (parser.name) {
                    endTag -> return array
                    "item" -> i++
                    else -> throw XmlPullParserException("Expected $endTag end tag at: ${parser.name}")
                }
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException("Document ended before $endTag end tag")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisDoubleArrayXml(
        parser: XmlPullParser,
        endTag: String,
        name: Array<String?>?
    ): DoubleArray {
        val num = try {
            parser.getAttributeValue(null, "num").toInt()
        } catch (e: NullPointerException) {
            throw XmlPullParserException("Need num attribute in double-array")
        } catch (e: NumberFormatException) {
            throw XmlPullParserException("Not a number in num attribute in double-array")
        }
        parser.next()
        val array = DoubleArray(num)
        var i = 0
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "item") {
                    try {
                        array[i] = parser.getAttributeValue(null, "value").toDouble()
                    } catch (e: NullPointerException) {
                        throw XmlPullParserException("Need value attribute in item")
                    } catch (e: NumberFormatException) {
                        throw XmlPullParserException("Not a number in value attribute in item")
                    }
                } else {
                    throw XmlPullParserException("Expected item tag at: ${parser.name}")
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                when (parser.name) {
                    endTag -> return array
                    "item" -> i++
                    else -> throw XmlPullParserException("Expected $endTag end tag at: ${parser.name}")
                }
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException("Document ended before $endTag end tag")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisStringArrayXml(
        parser: XmlPullParser,
        endTag: String,
        name: Array<String?>?
    ): Array<String?> {
        val num = try {
            parser.getAttributeValue(null, "num").toInt()
        } catch (e: NullPointerException) {
            throw XmlPullParserException("Need num attribute in string-array")
        } catch (e: NumberFormatException) {
            throw XmlPullParserException("Not a number in num attribute in string-array")
        }
        parser.next()
        val array = arrayOfNulls<String>(num)
        var i = 0
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "item") {
                    try {
                        array[i] = parser.getAttributeValue(null, "value")
                    } catch (e: NullPointerException) {
                        throw XmlPullParserException("Need value attribute in item")
                    } catch (e: NumberFormatException) {
                        throw XmlPullParserException("Not a number in value attribute in item")
                    }
                } else {
                    throw XmlPullParserException("Expected item tag at: ${parser.name}")
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                when (parser.name) {
                    endTag -> return array
                    "item" -> i++
                    else -> throw XmlPullParserException("Expected $endTag end tag at: ${parser.name}")
                }
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException("Document ended before $endTag end tag")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readThisBooleanArrayXml(
        parser: XmlPullParser,
        endTag: String,
        name: Array<String?>?
    ): BooleanArray {
        val num = try {
            parser.getAttributeValue(null, "num").toInt()
        } catch (e: NullPointerException) {
            throw XmlPullParserException("Need num attribute in string-array")
        } catch (e: NumberFormatException) {
            throw XmlPullParserException("Not a number in num attribute in string-array")
        }
        parser.next()
        val array = BooleanArray(num)
        var i = 0
        var eventType = parser.eventType
        do {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.name == "item") {
                    try {
                        array[i] = parser.getAttributeValue(null, "value").toBoolean()
                    } catch (e: NullPointerException) {
                        throw XmlPullParserException("Need value attribute in item")
                    } catch (e: NumberFormatException) {
                        throw XmlPullParserException("Not a number in value attribute in item")
                    }
                } else {
                    throw XmlPullParserException("Expected item tag at: " + parser.name)
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                when (parser.name) {
                    endTag -> return array
                    "item" -> i++
                    else -> throw XmlPullParserException("Expected $endTag end tag at: ${parser.name}")
                }
            }
            eventType = parser.next()
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException("Document ended before $endTag end tag")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun readValueXml(parser: XmlPullParser, name: Array<String?>): Any? {
        var eventType = parser.eventType
        do {
            when (eventType) {
                XmlPullParser.START_TAG -> return readThisValueXml(parser, name, null, false)
                XmlPullParser.END_TAG -> throw XmlPullParserException("Unexpected end tag at: ${parser.name}")
                XmlPullParser.TEXT -> throw XmlPullParserException("Unexpected text: ${parser.text}")
                else -> eventType = parser.next()
            }
        } while (eventType != XmlPullParser.END_DOCUMENT)
        throw XmlPullParserException("Unexpected end of document")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readThisValueXml(
        parser: XmlPullParser,
        name: Array<String?>,
        callback: ReadMapCallback?,
        arrayMap: Boolean
    ): Any? {
        val valueName = parser.getAttributeValue(null, "name")
        val tagName = parser.name
        var res: Any?
        if (tagName == "null") {
            res = null
        } else if (tagName == "string") {
            val value = StringBuilder()
            var eventType: Int
            while (parser.next().also { eventType = it } != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "string") {
                            name[0] = valueName
                            return value.toString()
                        }
                        throw XmlPullParserException("Unexpected end tag in <string>: ${parser.name}")
                    }
                    XmlPullParser.TEXT -> {
                        value.append(parser.text)
                    }
                    XmlPullParser.START_TAG -> {
                        throw XmlPullParserException("Unexpected start tag in <string>: ${parser.name}")
                    }
                }
            }
            throw XmlPullParserException("Unexpected end of document in <string>")
        } else if (readThisPrimitiveValueXml(parser, tagName).also { res = it } != null) {
        } else if (tagName == "byte-array") {
            res = readThisByteArrayXml(parser, "byte-array", name)
            name[0] = valueName
            return res
        } else if (tagName == "int-array") {
            res = readThisIntArrayXml(parser, "int-array", name)
            name[0] = valueName
            return res
        } else if (tagName == "long-array") {
            res = readThisLongArrayXml(parser, "long-array", name)
            name[0] = valueName
            return res
        } else if (tagName == "double-array") {
            res = readThisDoubleArrayXml(parser, "double-array", name)
            name[0] = valueName
            return res
        } else if (tagName == "string-array") {
            res = readThisStringArrayXml(parser, "string-array", name)
            name[0] = valueName
            return res
        } else if (tagName == "boolean-array") {
            res = readThisBooleanArrayXml(parser, "boolean-array", name)
            name[0] = valueName
            return res
        } else if (tagName == "map") {
            parser.next()
            res = if (arrayMap)
                readThisArrayMapXml(parser, "map", name, callback)
            else
                readThisMapXml(parser, "map", name, callback)
            name[0] = valueName
            return res
        } else if (tagName == "list") {
            parser.next()
            res = readThisListXml(parser, "list", name, callback, arrayMap)
            name[0] = valueName
            return res
        } else if (tagName == "set") {
            parser.next()
            res = readThisSetXml(parser, "set", name, callback, arrayMap)
            name[0] = valueName
            return res
        } else if (callback != null) {
            res = callback.readThisUnknownObjectXml(parser, tagName)
            name[0] = valueName
            return res
        } else {
            throw XmlPullParserException("Unknown tag: $tagName")
        }
        var eventType: Int
        while (parser.next().also { eventType = it } != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.END_TAG -> {
                    if (parser.name == tagName) {
                        name[0] = valueName
                        return res
                    }
                    throw XmlPullParserException("Unexpected end tag in <$tagName>: ${parser.name}")
                }
                XmlPullParser.TEXT -> {
                    throw XmlPullParserException("Unexpected text in <$tagName>: ${parser.name}")
                }
                XmlPullParser.START_TAG -> {
                    throw XmlPullParserException("Unexpected start tag in <$tagName>: ${parser.name}")
                }
            }
        }
        throw XmlPullParserException("Unexpected end of document in <$tagName>")
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readThisPrimitiveValueXml(parser: XmlPullParser, tagName: String): Any? {
        return try {
            when (tagName) {
                "int" -> parser.getAttributeValue(null, "value").toInt()
                "long" -> java.lang.Long.valueOf(parser.getAttributeValue(null, "value"))
                "float" -> parser.getAttributeValue(null, "value")
                "double" -> parser.getAttributeValue(null, "value")
                "boolean" -> java.lang.Boolean.valueOf(parser.getAttributeValue(null, "value"))
                else -> null
            }
        } catch (e: NullPointerException) {
            throw XmlPullParserException("Need value attribute in <$tagName>")
        } catch (e: NumberFormatException) {
            throw XmlPullParserException(
                "Not a number in value attribute in <$tagName>"
            )
        }
    }

    interface WriteMapCallback {
        @Throws(XmlPullParserException::class, IOException::class)
        fun writeUnknownObject(v: Any?, name: String?, out: XmlSerializer?)
    }

    interface ReadMapCallback {
        @Throws(XmlPullParserException::class, IOException::class)
        fun readThisUnknownObjectXml(`in`: XmlPullParser?, tag: String?): Any?
    }
}