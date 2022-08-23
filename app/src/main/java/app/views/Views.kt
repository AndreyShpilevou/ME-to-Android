package app.views

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.views.anko.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout

const val matchParent = MATCH_PARENT
const val wrapContent = WRAP_CONTENT

inline fun <reified T : View> Context.customView() : T {
    val constructor = (T::class.java).getConstructor(Context::class.java)
    return constructor.newInstance(this)
}


inline fun <reified T : View> Activity.customView(init: (T).() -> Unit = {}) : T {
    val view = (this as Context).customView<T>()
    setContentView(view.also(init))
    return view
}

inline fun <reified T : View> AppCompatDialog.customView(init: (T).() -> Unit = {}) : T {
    val view = context.customView<T>()
    setContentView(view.also(init))
    return view
}

inline fun <reified T : View> ViewGroup.customView(init: (T).() -> Unit = {}) : T {
    val view = context.customView<T>()
    addView(view.also(init))
    return view
}



fun Activity.nestedScrollView(init : (CustomNestedScrollView).() -> Unit = {}) : CustomNestedScrollView {
    return customView(init)
}

fun AppCompatDialog.nestedScrollView(init : (CustomNestedScrollView).() -> Unit = {}) : CustomNestedScrollView {
    return customView(init)
}

fun ViewGroup.nestedScrollView(init : (CustomNestedScrollView).() -> Unit = {}) : CustomNestedScrollView {
    return customView(init)
}


fun Activity.linearLayout(init : (CustomLinearLayout).() -> Unit = {}) : CustomLinearLayout {
    return customView(init)
}

fun AppCompatDialog.linearLayout(init : (CustomLinearLayout).() -> Unit = {}) : CustomLinearLayout {
    return customView(init)
}

fun ViewGroup.linearLayout(init : (CustomLinearLayout).() -> Unit = {}) : CustomLinearLayout {
    return customView(init)
}


fun Activity.verticalLayout(init : (CustomVerticalLayout).() -> Unit = {}) : CustomVerticalLayout {
    return customView(init)
}

fun AppCompatDialog.verticalLayout(init : (CustomVerticalLayout).() -> Unit = {}) : CustomVerticalLayout {
    return customView(init)
}

fun ViewGroup.verticalLayout(init : (CustomVerticalLayout).() -> Unit = {}) : CustomVerticalLayout {
    return customView(init)
}


fun Activity.frameLayout(init : (CustomFrameLayout).() -> Unit = {}) : CustomFrameLayout {
    return customView(init)
}

fun AppCompatDialog.frameLayout(init : (CustomFrameLayout).() -> Unit = {}) : CustomFrameLayout {
    return customView(init)
}

fun ViewGroup.frameLayout(init : (CustomFrameLayout).() -> Unit = {}) : CustomFrameLayout {
    return customView(init)
}


fun Activity.relativeLayout(init : (CustomRelativeLayout).() -> Unit = {}) : CustomRelativeLayout {
    return customView(init)
}

fun AppCompatDialog.relativeLayout(init : (CustomRelativeLayout).() -> Unit = {}) : CustomRelativeLayout {
    return customView(init)
}

fun ViewGroup.relativeLayout(init : (CustomRelativeLayout).() -> Unit = {}) : CustomRelativeLayout {
    return customView(init)
}


fun Activity.recyclerView(init : (RecyclerView).() -> Unit = {}) : RecyclerView{
    return customView(init)
}

fun AppCompatDialog.recyclerView(init : (RecyclerView).() -> Unit = {}) : RecyclerView {
    return customView(init)
}

fun ViewGroup.recyclerView(init : (RecyclerView).() -> Unit = {}) : RecyclerView{
    return customView(init)
}


fun Activity.view(init : (View).() -> Unit = {}) : View{
    return customView(init)
}

fun AppCompatDialog.view(init : (View).() -> Unit = {}) : View{
    return customView(init)
}

fun ViewGroup.view(init : (View).() -> Unit = {}) : View{
    return customView(init)
}


fun Activity.textView(init : (TextView).() -> Unit = {}) : TextView{
    return customView(init)
}

fun AppCompatDialog.textView(init : (TextView).() -> Unit = {}) : TextView{
    return customView(init)
}

fun ViewGroup.textView(init : (TextView).() -> Unit = {}) : TextView{
    return customView(init)
}


fun Activity.imageView(init : (ImageView).() -> Unit = {}) : ImageView {
    return customView(init)
}

fun AppCompatDialog.imageView(init : (ImageView).() -> Unit = {}) : ImageView{
    return customView(init)
}

fun ViewGroup.imageView(init : (ImageView).() -> Unit = {}) : ImageView{
    return customView(init)
}


fun Activity.editText(init : (EditText).() -> Unit = {}) : EditText{
    return customView(init)
}

fun AppCompatDialog.editText(init : (EditText).() -> Unit = {}) : EditText{
    return customView(init)
}

fun ViewGroup.editText(init : (EditText).() -> Unit = {}) : EditText{
    return customView(init)
}


fun Activity.button(init : (Button).() -> Unit = {}) : Button{
    return customView(init)
}

fun AppCompatDialog.button(init : (Button).() -> Unit = {}) : Button{
    return customView(init)
}

fun ViewGroup.button(init : (Button).() -> Unit = {}) : Button{
    return customView(init)
}


fun Activity.imageButton(init : (ImageButton).() -> Unit = {}) : ImageButton{
    return customView(init)
}

fun AppCompatDialog.imageButton(init : (ImageButton).() -> Unit = {}) : ImageButton{
    return customView(init)
}

fun ViewGroup.imageButton(init : (ImageButton).() -> Unit = {}) : ImageButton{
    return customView(init)
}


fun Activity.checkBox(init : (CheckBox).() -> Unit = {}) : CheckBox{
    return customView(init)
}

fun AppCompatDialog.checkBox(init : (CheckBox).() -> Unit = {}) : CheckBox{
    return customView(init)
}

fun ViewGroup.checkBox(init : (CheckBox).() -> Unit = {}) : CheckBox{
    return customView(init)
}


fun Activity.tabLayout(init : (TabLayout).() -> Unit = {}) : TabLayout{
    return customView(init)
}

fun AppCompatDialog.tabLayout(init : (TabLayout).() -> Unit = {}) : TabLayout{
    return customView(init)
}

fun ViewGroup.tabLayout(init : (TabLayout).() -> Unit = {}) : TabLayout{
    return customView(init)
}


fun Activity.viewPager2(init : (ViewPager2).() -> Unit = {}) : ViewPager2{
    return customView(init)
}

fun AppCompatDialog.viewPager2(init : (ViewPager2).() -> Unit = {}) : ViewPager2{
    return customView(init)
}

fun ViewGroup.viewPager2(init : (ViewPager2).() -> Unit = {}) : ViewPager2{
    return customView(init)
}


fun Activity.cardView(init : (CustomCardView).() -> Unit = {}) : CustomCardView {
    return customView(init)
}

fun AppCompatDialog.cardView(init : (CustomCardView).() -> Unit = {}) : CustomCardView {
    return customView(init)
}

fun ViewGroup.cardView(init : (CustomCardView).() -> Unit = {}) : CustomCardView {
    return customView(init)
}

var TextView.textColor: Int
    get() = 0
    set(value) {
        setTextColor(value)
    }

var TextView.textColorResources: Int
    get() = 0
    set(value) {
        setTextColor(ContextCompat.getColor(context, value))
    }

var ImageView.imageResources: Int
    get() = 0
    set(value) {
        setImageResource(value)
    }

var ImageView.imageDrawable: Drawable?
    get() = null
    set(value) {
        setImageDrawable(value)
    }

var View.backgroundResources: Int
    get() = 0
    set(value) {
        setBackgroundResource(value)
    }

var View.backgroundColor: Int
    get() = 0
    set(value) {
        setBackgroundColor(value)
    }