package app.appsList

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDiskIOException
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import app.activities.BaseActivity
import app.dao.AppItem
import app.dao.AppRepository
import app.profile.ProfilesActivity
import app.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import ru.playsoftware.j2meloader.R
import ru.playsoftware.j2meloader.settings.SettingsActivity
import app.views.*
import app.views.anko.CustomFrameLayout
import ru.woesss.j2me.installer.InstallerDialog

class AppsFragment : Fragment() {

    companion object {
        private val TAG = AppsFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(data: Uri?): AppsFragment {
            val fragment = AppsFragment()
            val args = Bundle()
            args.putParcelable(Constants.KEY_APP_URI, data)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var emptyView: View

    private lateinit var recyclerView: RecyclerView
    private lateinit var recyclerViewAdapter: AppsAdapter

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
                setPadding(6.dp, 12.dp, 6.dp, 100.dp)
                clipToPadding = false
            }.lparams(matchParent, matchParent)

            emptyView = textView {
                text = "no data"
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
            }.onLongClick {
                //startActivity<PActivity>()
                false
            }

        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        recyclerViewAdapter = AppsAdapter(requireActivity() as BaseActivity, appRepository)
        recyclerView.adapter = recyclerViewAdapter
    }

    private fun alertDbError(throwable: Throwable) {
        val activity: Activity? = activity
        if (activity == null) {
            Log.e(TAG, "Db error detected", throwable)
            return
        }
        if (throwable is SQLiteDiskIOException) {
            toast(R.string.error_disk_io)
        } else {
            val msg = activity.getString(R.string.error) + ": " + throwable.message
            toast(msg)
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
            activity.finish()

            //LicensesBottomDialog(activity).show()
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

}