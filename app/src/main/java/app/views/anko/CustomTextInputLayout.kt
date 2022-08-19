package app.views.anko

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.material.textfield.TextInputLayout

open class CustomTextInputLayout(context: Context) : TextInputLayout(context){

    fun <T : View> T.lparams(context: Context, attrs: AttributeSet, init: (LayoutParams).() -> Unit = {}) : T {
        LayoutParams(context, attrs).also {layoutParams = it.also(init)}
        return this
    }

    fun <T : View> T.lparams(width: Int, height: Int, init: (LayoutParams).() -> Unit = {}) : T {
        LayoutParams(width, height).also {layoutParams = it.also(init)}
        return this
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun <T : View> T.lparams(lParams: LayoutParams, init: (LayoutParams).() -> Unit = {}) : T {
        LayoutParams(lParams).also {layoutParams = it.also(init)}
        return this
    }
}