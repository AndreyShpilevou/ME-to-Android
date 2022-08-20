package app.appsList

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.utils.dp
import java.lang.StrictMath.min

class AppsRecyclerView(context: Context) :
    RecyclerView(context) {

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