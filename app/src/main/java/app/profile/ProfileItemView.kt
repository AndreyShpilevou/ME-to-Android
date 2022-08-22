package app.profile

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.setPadding
import app.utils.dp
import app.utils.dpF
import app.views.matchParent
import app.views.wrapContent

@SuppressLint("AppCompatCustomView")
class ProfileItemView(context: Context) : TextView(context) {

    init{
        setPadding(12.dp)

        layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
    }

    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.dpF
        color = Color.WHITE
    }

    private var pX: Float = 0f
    private var pY: Float = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        pX = width.toFloat()
        pY = height.toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas.apply {

            drawLine(0f, pY, pX, pY, paintLine)

        })
    }

}