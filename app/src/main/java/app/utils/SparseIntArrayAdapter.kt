package app.utils

import com.google.gson.TypeAdapter
import android.util.SparseIntArray
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class SparseIntArrayAdapter : TypeAdapter<SparseIntArray?>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, array: SparseIntArray?) {
        out.beginObject()
        for (i in 0 until array!!.size()) {
            out.name(array.keyAt(i).toString()).value(array.valueAt(i).toLong())
        }
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(reader: JsonReader): SparseIntArray? {
        val peek = reader.peek()
        if (peek == JsonToken.NULL) {
            reader.nextNull()
            return null
        }
        val array = SparseIntArray()
        if (peek == JsonToken.STRING) {
            val s = reader.nextString()
            try {
                val jsonArray = JSONArray(s)
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    array.put(item.getInt("key"), item.getInt("value"))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return array
        }
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            val key = name.toInt()
            val value = reader.nextInt()
            array.put(key, value)
        }
        reader.endObject()
        return array
    }
}