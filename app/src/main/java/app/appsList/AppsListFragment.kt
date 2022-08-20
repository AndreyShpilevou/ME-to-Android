package app.appsList

import android.content.SharedPreferences
import app.dao.AppRepository
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import app.dao.AppItem
import ru.playsoftware.j2meloader.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.os.Environment
import android.app.Activity
import android.database.sqlite.SQLiteDiskIOException
import android.widget.Toast
import ru.playsoftware.j2meloader.filepicker.FilteredFilePickerFragment
import ru.woesss.j2me.installer.InstallerDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.content.DialogInterface
import ru.playsoftware.j2meloader.util.AppUtils
import android.view.ContextMenu.ContextMenuInfo
import androidx.core.content.pm.ShortcutManagerCompat
import android.widget.AdapterView.AdapterContextMenuInfo
import android.graphics.Bitmap
import androidx.core.graphics.drawable.IconCompat
import android.app.ActivityManager
import android.content.Context
import android.graphics.RectF
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.*
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import ru.playsoftware.j2meloader.config.ConfigActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.fragment.app.ListFragment
import androidx.preference.PreferenceManager
import ru.playsoftware.j2meloader.config.Config
import ru.playsoftware.j2meloader.info.AboutDialogFragment
import ru.playsoftware.j2meloader.config.ProfilesActivity
import ru.playsoftware.j2meloader.settings.SettingsActivity
import ru.playsoftware.j2meloader.info.HelpDialogFragment
import ru.playsoftware.j2meloader.util.Constants
import ru.playsoftware.j2meloader.util.FileUtils
import ru.playsoftware.j2meloader.util.LogUtils
import java.io.File
import java.io.IOException

class AppsListFragment : ListFragment() {
    private val adapter = AppsListAdapter()
    private var appUri: Uri? = null
    private var preferences: SharedPreferences? = null
    private var appRepository: AppRepository? = null
    private val openFileLauncher = registerForActivityResult(
        FileUtils.getFilePicker()
    ) { uri: Uri? -> onPickFileResult(uri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = requireArguments()
        appUri = args.getParcelable(Constants.KEY_APP_URI)
        args.remove(Constants.KEY_APP_URI)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireActivity())
        val appListModel = ViewModelProvider(requireActivity())[AppListModel::class.java]
        appRepository = appListModel.appRepository
        appRepository!!.observeErrors(this) { throwable: Throwable -> alertDbError(throwable) }
        appRepository!!.observeApps(this) { items: List<AppItem>? -> onDbUpdated(items) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_appslist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(listView)
        setHasOptionsMenu(true)
        listAdapter = adapter
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            var path = preferences!!.getString(Constants.PREF_LAST_PATH, null)
            if (path == null) {
                val dir = Environment.getExternalStorageDirectory()
                if (dir.canRead()) {
                    path = dir.absolutePath
                }
            }
            openFileLauncher.launch(path)
        }
    }

    private fun alertDbError(throwable: Throwable) {
        val activity: Activity? = activity
        if (activity == null) {
            Log.e(TAG, "Db error detected", throwable)
            return
        }
        if (throwable is SQLiteDiskIOException) {
            Toast.makeText(activity, R.string.error_disk_io, Toast.LENGTH_SHORT).show()
        } else {
            val msg = activity.getString(R.string.error) + ": " + throwable.message
            Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPickFileResult(uri: Uri?) {
        if (uri == null) {
            return
        }
        preferences!!.edit()
            .putString(Constants.PREF_LAST_PATH, FilteredFilePickerFragment.getLastPath())
            .apply()
        InstallerDialog.newInstance(uri).show(parentFragmentManager, "installer")
    }

    private fun alertRename(id: Int) {
        val item = adapter.getItem(id)
        val activity = requireActivity()
        val editText = EditText(activity)
        editText.setText(item.title)
        val density = resources.displayMetrics.density
        val linearLayout = LinearLayout(activity)
        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val margin = (density * 20).toInt()
        params.setMargins(margin, 0, margin, 0)
        linearLayout.addView(editText, params)
        val paddingVertical = (density * 16).toInt()
        val paddingHorizontal = (density * 8).toInt()
        editText.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical)
        val builder = AlertDialog.Builder(activity)
            .setTitle(R.string.action_context_rename)
            .setView(linearLayout)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                val title = editText.text.toString().trim { it <= ' ' }
                if (title == "") {
                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show()
                } else {
                    item.title = title
                    appRepository!!.update(item)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    private fun alertDelete(item: AppItem) {
        val builder = AlertDialog.Builder(requireActivity())
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.message_delete)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                AppUtils.deleteApp(item)
                appRepository!!.delete(item)
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val item = adapter.getItem(position)
        Config.startApp(requireActivity(), item.title, item.pathExt, false)
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = requireActivity().menuInflater
        inflater.inflate(R.menu.context_main, menu)
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(requireContext())) {
            menu.findItem(R.id.action_context_shortcut).isVisible = false
        }
        val info = menuInfo as AdapterContextMenuInfo?
        val index = info!!.position
        val appItem = adapter.getItem(index)
        if (!File(appItem.pathExt + Config.MIDLET_RES_FILE).exists()) {
            menu.findItem(R.id.action_context_reinstall).isVisible = false
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        val index = info.position
        val appItem = adapter.getItem(index)
        when (item.itemId) {
            R.id.action_context_shortcut -> {
                requestAddShortcut(appItem)
            }
            R.id.action_context_rename -> {
                alertRename(index)
            }
            R.id.action_context_settings -> {
                Config.startApp(requireActivity(), appItem.title, appItem.pathExt, true)
            }
            R.id.action_context_reinstall -> {
                InstallerDialog.newInstance(appItem.id).show(parentFragmentManager, "installer")
            }
            R.id.action_context_delete -> {
                alertDelete(appItem)
            }
            else -> {
                return super.onContextItemSelected(item)
            }
        }
        return true
    }

    private fun requestAddShortcut(appItem: AppItem) {
        val activity = requireActivity()
        val bitmap = AppUtils.getIconBitmap(appItem)
        val icon: IconCompat
        if (bitmap == null) {
            icon = IconCompat.createWithResource(activity, R.mipmap.ic_launcher)
        } else {
            val width = bitmap.width
            val height = bitmap.height
            val am = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val iconSize = am.launcherLargeIconSize
            val src: Rect? = if (width > height) {
                val left = (width - height) / 2
                Rect(left, 0, left + height, height)
            } else if (width < height) {
                val top = (height - width) / 2
                Rect(0, top, width, top + width)
            } else {
                null
            }
            val scaled = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(scaled)
            canvas.drawBitmap(
                bitmap,
                src,
                RectF(0f, 0f, iconSize.toFloat(), iconSize.toFloat()),
                null
            )
            icon = IconCompat.createWithBitmap(scaled)
        }
        val title = appItem.title
        val launchIntent = Intent(
            Intent.ACTION_DEFAULT, Uri.parse(appItem.pathExt),
            activity, ConfigActivity::class.java
        )
        launchIntent.putExtra(Constants.KEY_MIDLET_NAME, title)
        val shortcut = ShortcutInfoCompat.Builder(activity, title)
            .setIntent(launchIntent)
            .setShortLabel(title)
            .setIcon(icon)
            .build()
        ShortcutManagerCompat.requestPinShortcut(activity, shortcut, null)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = requireActivity()
        val itemId = item.itemId
        if (itemId == R.id.action_about) {
            val aboutDialogFragment = AboutDialogFragment()
            aboutDialogFragment.show(childFragmentManager, "about")
        } else if (itemId == R.id.action_profiles) {
            val intentProfiles = Intent(activity, ProfilesActivity::class.java)
            startActivity(intentProfiles)
        } else if (item.itemId == R.id.action_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        } else if (itemId == R.id.action_help) {
            val helpDialogFragment = HelpDialogFragment()
            helpDialogFragment.show(childFragmentManager, "help")
        } else if (itemId == R.id.action_save_log) {
            try {
                LogUtils.writeLog()
                Toast.makeText(activity, R.string.log_saved, Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
            }
        } else if (itemId == R.id.action_exit_app) {
            activity.finish()
        }
        return false
    }

    private fun onDbUpdated(items: List<AppItem>?) {
        adapter.setItems(items)
        if (appUri != null) {
            InstallerDialog.newInstance(appUri).show(parentFragmentManager, "installer")
            appUri = null
        }
    }

    companion object {
        private val TAG = AppsListFragment::class.java.simpleName
        @JvmStatic
		fun newInstance(data: Uri?): AppsListFragment {
            val fragment = AppsListFragment()
            val args = Bundle()
            args.putParcelable(Constants.KEY_APP_URI, data)
            fragment.arguments = args
            return fragment
        }
    }
}