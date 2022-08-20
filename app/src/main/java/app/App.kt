package app

import org.acra.ACRA.init
import org.acra.ACRA.errorReporter
import androidx.multidex.MultiDex
import javax.microedition.util.ContextHolder
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.DialogConfigurationBuilder
import androidx.appcompat.app.AppCompatDelegate
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.content.pm.PackageManager
import android.content.pm.Signature
import ru.playsoftware.j2meloader.BuildConfig
import ru.playsoftware.j2meloader.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

class App : Application() {

    companion object{
        private val VALID_SIGNATURES = arrayOf(
            "78EF7758720A9902F731ED706F72C669C39B765C",  // GPlay
            "289F84A32207DF89BE749481ED4BD07E15FC268F",  // F-Droid
            "FA8AA497194847D5715BAA62C6344D75A936EBA6" // Private
        )

        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()

        instance = this
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        if (BuildConfig.DEBUG) {
            MultiDex.install(this)
        }
        ContextHolder.setApplication(this)
        init(
            this, CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig::class.java)
                .withParallel(false)
                .withSendReportsInDevMode(false)
                .withPluginConfigurations(
                    DialogConfigurationBuilder()
                        .withTitle(getString(R.string.crash_dialog_title))
                        .withText(getString(R.string.crash_dialog_message))
                        .withPositiveButtonText(getString(R.string.report_crash))
                        .withResTheme(androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog)
                        .withEnabled(true)
                        .build()
                )
        )
        val enabled = getIsSignatureValid() && BuildConfig.FLAVOR != "dev"
        errorReporter.setEnabled(enabled)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    @SuppressLint("PackageManagerGetSignatures")
    private fun getIsSignatureValid(): Boolean {
            try {
                val signatures: Array<Signature> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val info = packageManager
                        .getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                    info.signingInfo.apkContentsSigners
                } else {
                    val info = packageManager
                        .getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                    info.signatures
                }
                val md = MessageDigest.getInstance("SHA-1")
                for (signature in signatures) {
                    md.update(signature.toByteArray())
                    val sha1 = bytesToHex(md.digest())
                    if (Arrays.asList(*VALID_SIGNATURES).contains(sha1)) {
                        return true
                    }
                }
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
            return false
        }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7', '8',
            '9', 'A', 'B', 'C', 'D', 'E', 'F'
        )
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (i in bytes.indices) {
            v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

}