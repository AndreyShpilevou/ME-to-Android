package app.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import app.dao.AppItem
import app.utils.dp
import app.views.anko.CustomVerticalLayout
import ru.playsoftware.j2meloader.R

class AppItemView(context: Context) : CustomVerticalLayout(context) {

    private var iconView: ImageView
    private var titleView: TextView
    private lateinit var authorView: TextView
    private lateinit var versionView: TextView

    init{
        setWillNotDraw(false)
        gravity = Gravity.CENTER_HORIZONTAL

        iconView = imageView {

        }.lparams(90.dp, 90.dp)

        titleView = textView {
            gravity = Gravity.CENTER_HORIZONTAL
            typeface = Typeface.DEFAULT_BOLD
        }.lparams(matchParent, wrapContent){
            setMargins(0, 6.dp, 0, 6.dp)
        }

        linearLayout {

            authorView = textView {

            }.lparams(matchParent, wrapContent, 1f)

            versionView = textView {

            }.lparams(wrapContent, wrapContent)

        }.lparams(matchParent, wrapContent)

        setPadding(12.dp, 12.dp, 12.dp, 12.dp)

    }

    fun setItem(item: AppItem){

        val icon = Drawable.createFromPath(item.imagePathExt)
        if (icon != null) {
            icon.isFilterBitmap = false
            iconView.setImageDrawable(icon)
        } else {
            iconView.setImageResource(R.mipmap.ic_launcher)
        }

        titleView.text = item.title
        authorView.text = item.author
        versionView.text = item.version

    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas.apply {

            drawRoundRect(
                RectF(12f, 12f, width-12f, height-12f),
                12f, 12f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                    color = Color.BLACK
                }
            )

        })
    }

}