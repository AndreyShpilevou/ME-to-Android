package app.views.anko

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.drawerlayout.widget.DrawerLayout

open class CustomDrawerLayout(context: Context) : DrawerLayout(context){
    fun <T : View> T.lparams(context: Context, attrs: AttributeSet, init: (LayoutParams).() -> Unit = {}) : T {
        LayoutParams(context, attrs).also {layoutParams = it.also(init)}
        return this
    }

    fun <T : View> T.lparams(width: Int, height: Int, init: (LayoutParams).() -> Unit = {}) : T {
        LayoutParams(width, height).also {layoutParams = it.also(init)}
        return this
    }

    fun <T : View> T.lparams(lParams: LayoutParams, init: (LayoutParams).() -> Unit = {}) : T {
        LayoutParams(lParams).also {layoutParams = it.also(init)}
        return this
    }
}