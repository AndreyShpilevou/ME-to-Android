package app.appsList

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.utils.dp
import java.lang.StrictMath.min

class AppsRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, default: Int) : super(context, attrs, default)

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)

        val width = MeasureSpec.getSize(widthSpec)
        if (width != 0) {
            val spans: Int = width / 120.dp
            if (spans > 0) {
                layoutManager = GridLayoutManager(context, spans)
            }
        }

    }

}