package app.appsList

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDiskIOException
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import app.dao.AppItem
import app.dao.AppRepository
import app.dialogs.AppCardBottomDialog
import app.dialogs.LicensesBottomDialog
import app.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.playsoftware.j2meloader.R
import ru.playsoftware.j2meloader.config.Config
import ru.playsoftware.j2meloader.config.ConfigActivity
import ru.playsoftware.j2meloader.config.ProfilesActivity
import ru.playsoftware.j2meloader.settings.SettingsActivity
import app.views.*
import app.views.anko.CustomFrameLayout
import ru.woesss.j2me.installer.InstallerDialog
import java.io.File

class AppsListFragment : Fragment() {

    private lateinit var emptyView: View

    private lateinit var recyclerView: RecyclerView
    private val recyclerViewAdapter = AppsListAdapter()

    private var appUri: Uri? = null
    private var preferences: SharedPreferences? = null

    private lateinit var appRepository: AppRepository

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
        appRepository.observeErrors(this) { throwable: Throwable -> alertDbError(throwable) }
        appRepository.observeApps(this) { items: List<AppItem> -> onDbUpdated(items) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = CustomFrameLayout(requireContext()).apply {

            recyclerView = customView<AppsRecyclerView> {
                setPadding(6.dp, 6.dp, 6.dp, 100.dp)
                clipToPadding = false
            }.lparams(matchParent, matchParent)

            emptyView = textView {
                text = "no_data_for_display"
            }.lparams(wrapContent, wrapContent){
                gravity = Gravity.CENTER
            }

            customView<FloatingActionButton> {
                imageResources = R.drawable.ic_add_white
            }.lparams(wrapContent, wrapContent){
                gravity = Gravity.RIGHT or Gravity.BOTTOM
                setMargins(0, 0, 16.dp, 16.dp)
            }.onClick {
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        recyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter.control = object : AppsListAdapter.Control{

            override fun onClickItem(position: Int) {
                val item = recyclerViewAdapter.getItem(position)
                Config.startApp(requireActivity(), item.getDisplayTitle(), item.getPathExt(), false)
            }

            override fun onLongClickItem(position: Int, view: View) {
                //showPopupMenu(position, view)

                val appItem = recyclerViewAdapter.getItem(position)
                AppCardBottomDialog(requireActivity(), appRepository, appItem).show()
            }
        }
    }

//    private fun showPopupMenu(position: Int, view: View) {
//
//        val appItem = recyclerViewAdapter.getItem(position)
//
//        val popupMenu = PopupMenu(view.context, view)
//        popupMenu.inflate(R.menu.context_main)
//        popupMenu.setOnMenuItemClickListener { item ->
//                when (item.itemId) {
//                    R.id.action_context_shortcut -> {
//                        //requestAddShortcut(appItem)
//                    }
//                    R.id.action_context_rename -> {
//                        //alertRename(position)
//                    }
//                    R.id.action_context_settings -> {
//                        Config.startApp(requireActivity(), appItem.title, appItem.getPathExt(), true)
//                    }
//                    R.id.action_context_reinstall -> {
//                        InstallerDialog.newInstance(appItem.id).show(parentFragmentManager, "installer")
//                    }
//                    R.id.action_context_delete -> {
//                        //alertDelete(appItem)
//                    }
//                }
//                false
//            }
//        popupMenu.show()
//
//        if (!File(appItem.getPathExt() + Config.MIDLET_RES_FILE).exists()) {
//            popupMenu.menu.findItem(R.id.action_context_reinstall).isVisible = false
//        }
//    }

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
//        preferences!!.edit()
//            .putString(Constants.PREF_LAST_PATH, FilteredFilePickerFragment.getLastPath())
//            .apply()
        InstallerDialog.newInstance(uri).show(parentFragmentManager, "installer")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val activity = requireActivity()
        val itemId = item.itemId
        if (itemId == R.id.action_profiles) {
            val intentProfiles = Intent(activity, ProfilesActivity::class.java)
            startActivity(intentProfiles)
        } else if (item.itemId == R.id.action_settings) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
        } else if (itemId == R.id.action_exit_app) {
            //activity.finish()

            LicensesBottomDialog(activity).show()
        }
        return false
    }

    private fun onDbUpdated(items: List<AppItem>) {

        recyclerViewAdapter.setItems(items)

        emptyView.isVisible = items.isEmpty()

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