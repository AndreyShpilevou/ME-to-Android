package app.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import ru.playsoftware.j2meloader.R
import app.appsList.AppListModel
import app.appsList.AppsListFragment
import ru.playsoftware.j2meloader.config.Config
import app.utils.Constants
import app.utils.FileUtils
import ru.woesss.j2me.installer.InstallerDialog
import java.io.File

class MainActivity : BaseActivity() {
    private val permissionsLauncher =
        registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { status: Map<String, Boolean> ->
            onPermissionResult(
                status
            )
        }

    private lateinit var preferences: SharedPreferences
    private lateinit var appListModel: AppListModel

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            val fragment = AppsListFragment.newInstance(uri)
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment).commit()
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        if (!preferences.contains(Constants.PREF_TOOLBAR)) {
            val enable = !ViewConfiguration.get(this).hasPermanentMenuKey()
            preferences.edit().putBoolean(Constants.PREF_TOOLBAR, enable).apply()
        }
        val warningShown = preferences.getBoolean(Constants.PREF_STORAGE_WARNING_SHOWN, false)
        if (!FileUtils.isExternalStorageLegacy() && !warningShown) {
            showScopedStorageDialog()
            preferences.edit().putBoolean(Constants.PREF_STORAGE_WARNING_SHOWN, true).apply()
        }
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    private fun checkAndCreateDirs() {
        val emulatorDir = Config.getEmulatorDir()
        val dir = File(emulatorDir)
        if (dir.isDirectory && dir.canWrite()) {
            FileUtils.initWorkDir(dir)
            appListModel!!.appRepository.onWorkDirReady()
            return
        }
        if (dir.exists() || dir.parentFile == null || !dir.parentFile.isDirectory
            || !dir.parentFile.canWrite()
        ) {
            alertDirCannotCreate(emulatorDir)
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
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(this)
                .setTitle(android.R.string.dialog_alert_title)
                .setCancelable(false)
                .setMessage(R.string.permission_request_failed)
                .setNegativeButton(R.string.retry) { d, w ->
                    permissionsLauncher.launch(
                        STORAGE_PERMISSIONS
                    )
                }
                .setPositiveButton(R.string.exit) { d, w -> finish() }
                .show()
        } else {
            Toast.makeText(this, R.string.permission_request_failed, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun showScopedStorageDialog() {
        val message = getString(R.string.scoped_storage_warning) + Config.getEmulatorDir()
        AlertDialog.Builder(this)
            .setTitle(R.string.warning)
            .setCancelable(false)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun onPickDirResult(uri: Uri?) {
        if (uri == null) {
            checkAndCreateDirs()
            return
        }
        val file = FileUtils.getFileForUri(uri)
        applyWorkDir(file)
    }

    @SuppressLint("StringFormatMatches")
    private fun alertCreateDir() {
        val emulatorDir = Config.getEmulatorDir()
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