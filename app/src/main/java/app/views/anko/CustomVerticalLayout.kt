package app.views.anko

import android.content.Context
import app.views.anko.CustomLinearLayout

open class CustomVerticalLayout(context: Context) : CustomLinearLayout(context){
    init{orientation = VERTICAL}
}