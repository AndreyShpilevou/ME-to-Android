package app.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat

@SuppressLint("MissingPermission")
class AppVibrator(context: Context) {

    private val vibrator: Vibrator
    init {
        vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator
    }

    fun vibrate(duration: Long){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    fun hasVibrator() : Boolean{
        return vibrator.hasVibrator()
    }

    fun cancel(){
        vibrator.cancel()
    }
}