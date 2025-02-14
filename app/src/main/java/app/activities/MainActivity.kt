package app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import app.appsList.AppListModel
import app.appsList.AppsFragment
import app.utils.Constants
import app.utils.FileUtils
import app.utils.ViewIdGenerator
import app.utils.toast
import app.views.*
import ru.playsoftware.j2meloader.R
import app.profile.Config
import ru.woesss.j2me.installer.InstallerDialog
import java.io.File

class MainActivity : BaseActivity() {

    private val permissionsLauncher =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { status: Map<String, Boolean> ->
            onPermissionResult(status)
        }

    private lateinit var preferences: SharedPreferences
    private lateinit var appListModel: AppListModel

    private lateinit var frameLayout: FrameLayout

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verticalLayout {

            frameLayout = frameLayout {
                id = ViewIdGenerator.generateViewId()

            }.lparams(matchParent, matchParent, 1f)

        }

        if (FileUtils.isExternalStorageLegacy()) {
            permissionsLauncher.launch(STORAGE_PERMISSIONS)
        }

        appListModel = ViewModelProvider(this)[AppListModel::class.java]

        if (savedInstanceState == null) {
            val intent = intent
            var uri: Uri? = null
            if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0) {
                uri = intent.data
            }

            val appsFragment = AppsFragment.newInstance(uri)

            supportFragmentManager.beginTransaction()
                .replace(frameLayout.id, appsFragment).commit()

        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun checkAndCreateDirs() {
        val emulatorDir = Config.emulatorDir

        val dir = File(emulatorDir)
        if (dir.isDirectory && dir.canWrite()) {
            FileUtils.initWorkDir(dir)
            appListModel.appRepository.onWorkDirReady()
            return
        }

        if (dir.exists() || dir.parentFile == null || !dir.parentFile!!.isDirectory || !dir.parentFile!!.canWrite()) {
            alertDirCannotCreate(emulatorDir!!)
            return
        }

        alertCreateDir()
    }

    private fun alertDirCannotCreate(emulatorDir: String) {
        AlertDialog.Builder(this)
            .setTitle(R.string.error)
            .setCancelable(false)
            .setMessage(getString(R.string.create_apps_dir_failed, emulatorDir))
            .setNegativeButton(R.string.exit) { d, w -> finish() }
            .show()
    }

    private fun onPermissionResult(status: Map<String, Boolean>) {
        if (!status.containsValue(false)) {
            checkAndCreateDirs()
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            AlertDialog.Builder(this)
                .setTitle(android.R.string.dialog_alert_title)
                .setCancelable(false)
                .setMessage(R.string.permission_request_failed)
                .setNegativeButton(R.string.retry) { d, w ->
                    permissionsLauncher.launch(STORAGE_PERMISSIONS)
                }
                .setPositiveButton(R.string.exit) { d, w -> finish() }
                .show()
        } else {
            toast(R.string.permission_request_failed)
            finish()
        }
    }

//    private fun onPickDirResult(uri: Uri?) {
//        if (uri == null) {
//            checkAndCreateDirs()
//            return
//        }
//        val file = FileUtils.getFileForUri(uri)
//        applyWorkDir(file)
//    }

    @SuppressLint("StringFormatMatches")
    private fun alertCreateDir() {
        val emulatorDir = Config.emulatorDir
        val lblChange = getString(R.string.change)
        val msg = getString(R.string.alert_msg_workdir_not_exists, emulatorDir, lblChange)
        AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setCancelable(false)
            .setMessage(msg)
            .setPositiveButton(R.string.create) { d, w -> applyWorkDir(File(emulatorDir)) }
            .setNegativeButton(R.string.exit) { d, w -> finish() }
            .show()
    }

    private fun applyWorkDir(file: File) {
        val path = file.absolutePath
        if (!FileUtils.initWorkDir(file)) {
            alertDirCannotCreate(path)
            return
        }
        preferences.edit().putString(Constants.PREF_EMULATOR_DIR, path).apply()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        if (uri != null) {
            InstallerDialog.newInstance(uri).show(supportFragmentManager, "installer")
        }
    }

    companion object {
        private val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}