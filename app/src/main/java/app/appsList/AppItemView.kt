package app.appsList

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.TextView
import app.dao.AppItem
import app.utils.dp
import app.utils.dpF
import app.views.*
import app.views.anko.CustomVerticalLayout
import ru.playsoftware.j2meloader.R

class AppItemView(context: Context) : CustomVerticalLayout(context) {

    private var iconView: ImageView
    private var titleView: TextView
    private var authorView: TextView

    init{
        setWillNotDraw(false)
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(12.dp, 12.dp, 12.dp, 12.dp)

        iconView = imageView {
            imageResources = R.mipmap.ic_launcher
            backgroundResources = R.color.primary
        }.lparams(90.dp, 90.dp){
            gravity = Gravity.CENTER_HORIZONTAL
        }

        titleView = textView {
            gravity = Gravity.CENTER_HORIZONTAL
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
        }.lparams(matchParent, wrapContent){
            setMargins(0, 6.dp, 0, 6.dp)
        }

        authorView = textView {
            gravity = Gravity.CENTER_HORIZONTAL
            textSize = 12f
            alpha = 0.9f
        }.lparams(wrapContent, wrapContent)

    }

    fun setItem(item: AppItem){

        Drawable.createFromPath(item.getImagePathExt())?.also{ icon ->
            icon.isFilterBitmap = false
            iconView.imageDrawable = icon
        } ?: let {
            iconView.imageResources = R.mipmap.ic_launcher
        }

        titleView.text = item.getDisplayTitle()
        authorView.text = item.author
    }

    private val innerPadding = 3.dpF
    private val cornerBorder = 12.dpF
    private var rectF = RectF()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.WHITE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        rectF = RectF(innerPadding, innerPadding, width-innerPadding, height-innerPadding)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas.apply {

            drawRoundRect(rectF, cornerBorder, cornerBorder, borderPaint)

        })
    }

}