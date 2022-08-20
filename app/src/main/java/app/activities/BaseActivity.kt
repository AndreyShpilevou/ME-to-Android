package app.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (supportActionBar != null) {
            supportActionBar!!.elevation = resources.displayMetrics.density * 2
        }
        super.onCreate(savedInstanceState)
    }
}