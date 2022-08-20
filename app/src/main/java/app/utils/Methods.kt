package app.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import app.App
import com.google.android.material.bottomsheet.BottomSheetDialog

inline fun <reified T : Activity> Context.startActivity(vararg params: Pair<String, Any?>){
    startActivity(intentFor<T>(*params))
}

inline fun <reified T : Activity> Fragment.startActivity(vararg params: Pair<String, Any?>){
    startActivity(requireActivity().intentFor<T>(*params))
}

inline fun <reified T : Activity> Context.intentFor(vararg params: Pair<String, Any?>) : Intent {
    val intent = Intent(this, T::class.java)
    if (params.isNotEmpty()) {
        intent.fillIntentArguments(params)
    }
    return intent
}

inline fun <reified T : Activity> AppCompatActivity.startActivityForResult(
    vararg params: Pair<String, Any?>,
    crossinline callback: (Intent?) -> Unit
){
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            callback.invoke(result.data)
        }
    }

    startForResult.launch(intentFor<T>(*params))
}

inline fun <reified T : Activity> Fragment.startActivityForResult(
    vararg params: Pair<String, Any?>,
    crossinline callback: (Intent?) -> Unit
){
    val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            callback.invoke(result.data)
        }
    }

    startForResult.launch(requireActivity().intentFor<T>(*params))
}

fun Intent.newTask() : Intent {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    return this
}

fun Intent.clearTask() : Intent {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
    return this
}

fun Intent.clearTop() : Intent {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    return this
}

fun Intent.fillIntentArguments(params: Array<out Pair<String, Any?>>) {
    params.forEach {
        when (val value = it.second) {
            //null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> putExtra(it.first, value)
            is Long -> putExtra(it.first, value)
            is CharSequence -> putExtra(it.first, value)
            is String -> putExtra(it.first, value)
            is Float -> putExtra(it.first, value)
            is Double -> putExtra(it.first, value)
            is Char -> putExtra(it.first, value)
            is Short -> putExtra(it.first, value)
            is Boolean -> putExtra(it.first, value)
            //is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> putExtra(it.first, value)
            is Parcelable -> putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> putExtra(it.first, value)
                value.isArrayOf<String>() -> putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> putExtra(it.first, value)
                else -> throw Exception("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            is IntArray -> putExtra(it.first, value)
            is LongArray -> putExtra(it.first, value)
            is FloatArray -> putExtra(it.first, value)
            is DoubleArray -> putExtra(it.first, value)
            is CharArray -> putExtra(it.first, value)
            is ShortArray -> putExtra(it.first, value)
            is BooleanArray -> putExtra(it.first, value)
            else -> throw Exception("Intent extra ${it.first} has wrong type ${value?.javaClass?.name}")
        }
        return@forEach
    }
}

fun <T : View> T.onClick(click: (View) -> Unit) : T {
    setOnClickListener(click)
    return this
}

fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

fun CheckBox.onCheckedChange(update: (button: CompoundButton, isChecked: Boolean) -> Unit){
    setOnCheckedChangeListener(update)
}

fun EditText.onFocusChange(update: (View, Boolean) -> Unit){
    onFocusChangeListener = View.OnFocusChangeListener(update)
}

fun Switch.onCheckedChange(update: (v: View?, isChecked: Boolean) -> Unit){
    setOnCheckedChangeListener(update)
}

val Int.dp : Int
    get() {
        return dp()
    }

fun Int.dp() : Int {
    return dpF().toInt()
}

val Float.dp : Int
    get() {
        return dp()
    }

fun Float.dp() : Int {
    return dpF().toInt()
}

val Int.dpF : Float
    get() {
        return dpF()
    }

fun Int.dpF() : Float {
    return toFloat().dpF()
}

val Float.dpF : Float
    get() {
        return dpF()
    }

fun Float.dpF() : Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, App.instance.resources.displayMetrics)
}

fun Activity.toast(str: String){
    Toast.makeText(this, str, Toast.LENGTH_LONG).show()
}

fun Activity.toast(strId: Int){
    Toast.makeText(this, strId, Toast.LENGTH_LONG).show()
}

fun Fragment.toast(str: String){
    requireActivity().toast(str)
}

fun Fragment.toast(strId: Int){
    requireActivity().toast(strId)
}

fun Context.toast(str: String){
    Toast.makeText(this, str, Toast.LENGTH_LONG).show()
}

fun Context.toast(strId: Int){
    Toast.makeText(this, strId, Toast.LENGTH_LONG).show()
}

fun Dialog.toast(str: String){
    Toast.makeText(context, str, Toast.LENGTH_LONG).show()
}

fun Dialog.toast(strId: Int){
    Toast.makeText(context, strId, Toast.LENGTH_LONG).show()
}

fun BottomSheetDialog.toast(str: String){
    Toast.makeText(context, str, Toast.LENGTH_LONG).show()
}

fun BottomSheetDialog.toast(strId: Int){
    Toast.makeText(context, strId, Toast.LENGTH_LONG).show()
}

fun Activity.getExtras(key: String) = intent.extras?.get(key)


fun Activity.getExtrasString(key: String, default: String = "") =
    intent.extras?.getString(key, default) ?: default

fun Activity.getExtrasStringArray(key: String) =
    intent.extras?.getStringArray(key) ?: emptyArray<String>()

fun Activity.getExtrasStringArrayList(key: String) =
    intent.extras?.getStringArrayList(key) ?: ArrayList<String>()


fun Activity.getExtrasChar(key: String, default: Char = ' ') =
    intent.extras?.getChar(key, default) ?: default

fun Activity.getExtrasCharArray(key: String) =
    intent.extras?.getCharArray(key) ?: emptyArray<Char>()

fun Activity.getExtrasCharSequenceArray(key: String): Array<CharSequence> =
    intent.extras?.getCharSequenceArray(key) ?: emptyArray<CharSequence>()

fun Activity.getExtrasCharSequenceArrayList(key: String) =
    intent.extras?.getCharSequenceArrayList(key) ?: ArrayList<CharSequence>()


fun Activity.getExtrasByte(key: String, default: Byte = 0) =
    intent.extras?.getByte(key, default) ?: default

fun Activity.getExtrasByteArray(key: String, default: Byte = 0) =
    intent.extras?.getByteArray(key) ?: emptyArray<Byte>()


fun Activity.getExtrasShort(key: String, default: Short = 0) =
    intent.extras?.getShort(key, default) ?: default

fun Activity.getExtrasShortArray(key: String, default: Byte = 0) =
    intent.extras?.getShortArray(key) ?: emptyArray<Short>()


fun Activity.getExtrasInt(key: String, default: Int = 0) =
    intent.extras?.getInt(key, default) ?: default

fun Activity.getExtrasIntArray(key: String) =
    intent.extras?.getIntArray(key) ?: emptyArray<Int>()


fun Activity.getExtrasLong(key: String, default: Long = 0L) =
    intent.extras?.getLong(key, default) ?: default

fun Activity.getExtrasLongArray(key: String) =
    intent.extras?.getLongArray(key) ?: emptyArray<Long>()


fun Activity.getExtrasFloat(key: String, default: Float = .0F) =
    intent.extras?.getFloat(key, default) ?: default

fun Activity.getExtrasFloatArray(key: String) =
    intent.extras?.getFloatArray(key) ?: emptyArray<Float>()


fun Activity.getExtrasDouble(key: String, default: Double = .0) =
    intent.extras?.getDouble(key, default) ?: default

fun Activity.getExtrasDoubleArray(key: String) =
    intent.extras?.getDoubleArray(key) ?: emptyArray<Double>()


fun Activity.getExtrasBoolean(key: String, default: Boolean = false) =
    intent.extras?.getBoolean(key, default) ?: default

fun Activity.getExtrasBooleanArray(key: String) =
    intent.extras?.getBooleanArray(key) ?: emptyArray<Boolean>()

//fun <T : View> T.onGlobalLayout(update : (T).() -> Unit){
//
//    val view = this
//
//    val listener = object : ViewTreeObserver.OnGlobalLayoutListener{
//        override fun onGlobalLayout() {
//            viewTreeObserver.removeOnGlobalLayoutListener(this)
//
//            update.invoke(view)
//        }
//    }
//
//    viewTreeObserver.addOnGlobalLayoutListener(listener)
//}