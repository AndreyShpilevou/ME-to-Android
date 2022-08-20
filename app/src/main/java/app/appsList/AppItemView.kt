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

    private lateinit var iconView: ImageView
    private var titleView: TextView
    private var authorView: TextView
    private lateinit var versionView: TextView

    init{
        setWillNotDraw(false)
        gravity = Gravity.CENTER_HORIZONTAL

        frameLayout {

            iconView = imageView {
                imageResources = R.mipmap.ic_launcher
            }.lparams(90.dp, 90.dp){
                gravity = Gravity.CENTER_HORIZONTAL
            }

            versionView = textView {
                textSize = 12f
                alpha = 0.8f
                setShadowLayer(4f, 0f, 1f, Color.BLACK)
            }.lparams(wrapContent, wrapContent){
                gravity = Gravity.RIGHT
            }

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
            textSize = 13f
        }.lparams(wrapContent, wrapContent)

        setPadding(12.dp, 12.dp, 12.dp, 12.dp)

    }

    fun setItem(item: AppItem){

        Drawable.createFromPath(item.imagePathExt)?.let{ icon ->
            icon.isFilterBitmap = false
            iconView.setImageDrawable(icon)
        }

        titleView.text = item.title
        authorView.text = item.author
        versionView.text = item.version

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