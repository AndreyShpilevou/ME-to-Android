package app.dialogs

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import app.utils.dp
import app.utils.dpF
import app.views.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.playsoftware.j2meloader.R
import javax.microedition.util.ContextHolder

class LicensesBottomDialog(context: Context)
    : BottomSheetDialog(context, R.style.BottomSheetDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        (context.resources.displayMetrics.heightPixels * 0.9f).toInt().let {
            behavior.peekHeight = it
            behavior.maxHeight = it
        }

        verticalLayout {
            backgroundResources = R.drawable.bottom_dialog_bg

            cardView {
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.background))
                radius = 12.dpF
            }.lparams(60.dp, 6.dp){
                setMargins(0, 12.dp, 0, 12.dp)
                gravity = Gravity.CENTER_HORIZONTAL
            }

            nestedScrollView {

                textView {

                    val string = ContextHolder.getAssetAsString("licenses.html")
                    text = HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    textColor = Color.DKGRAY

                }.lparams(matchParent, matchParent){
                    setMargins(24.dp, 24.dp, 24.dp, 24.dp)
                }

            }.lparams(matchParent, matchParent)

        }

    }

}