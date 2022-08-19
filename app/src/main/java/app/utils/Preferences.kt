package app.utils

import android.annotation.SuppressLint
import android.content.Context

class Preferences(context: Context) {

    private val pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE)

    fun contains(key: String) = pref.contains(key)

    fun getString(key: String, default: String?) : String? {
        return pref.getString(key, default)
    }

    @SuppressLint("CommitPrefEdits")
    fun putString(key: String, value: String?){
        val edit = pref.edit()
        edit.putString(key, value)
        edit.apply()
    }

    fun getBoolean(key: String, default: Boolean) : Boolean {
        return pref.getBoolean(key, default)
    }

    @SuppressLint("CommitPrefEdits")
    fun putBoolean(key: String, value: Boolean){
        val edit = pref.edit()
        edit.putBoolean(key, value)
        edit.apply()
    }

}