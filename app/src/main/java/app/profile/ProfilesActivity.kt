package app.profile

import android.annotation.SuppressLint
import app.activities.BaseActivity
import android.widget.AdapterView.OnItemClickListener
import android.content.SharedPreferences
import androidx.activity.result.contract.ActivityResultContract
import android.content.Intent
import ru.playsoftware.j2meloader.config.ConfigActivity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import ru.playsoftware.j2meloader.R
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.AdapterView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.utils.Constants.ACTION_EDIT_PROFILE
import app.utils.Constants.PREF_DEFAULT_PROFILE
import app.utils.dp
import app.views.*
import ru.playsoftware.j2meloader.config.EditNameAlert

class ProfilesActivity : BaseActivity(), EditNameAlert.Callback, OnItemClickListener {

    private lateinit var emptyView: View

    private lateinit var profilesAdapter: ProfilesAdapter

    private lateinit var preferences: SharedPreferences

    private val editProfileLauncher = registerForActivityResult(
        object : ActivityResultContract<String?, String?>() {
            override fun createIntent(context: Context, input: String?): Intent {
                return Intent(
                    ACTION_EDIT_PROFILE, Uri.parse(input),
                    applicationContext, ConfigActivity::class.java
                )
            }

            override fun parseResult(resultCode: Int, intent: Intent?): String? {
                return if (resultCode == RESULT_OK && intent != null) {
                    intent.dataString!!
                } else null
            }
        }
    ) { name: String? ->
        if (name != null) {
            profilesAdapter.addItem(Profile(name))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        setTitle(R.string.profiles)

        preferences = PreferenceManager.getDefaultSharedPreferences(this)

        val profiles = ProfilesManager.profiles

        profilesAdapter = ProfilesAdapter(profiles)

        frameLayout {

            recyclerView {
                layoutManager = LinearLayoutManager(context)
                setPadding(0, 3.dp, 0, 100.dp)
                clipToPadding = false
                adapter = profilesAdapter
            }.lparams(matchParent, matchParent)

            emptyView = textView {
                text = "no data"
                isVisible = profiles.isEmpty()
            }.lparams(wrapContent, wrapContent){
                gravity = Gravity.CENTER
            }

        }

        val def = preferences.getString(PREF_DEFAULT_PROFILE, null)
        if (def != null) {
            for (i in profiles.indices.reversed()) {
                val profile = profiles[i]
                if (profile.name == def) {
                    profilesAdapter.setDefault(profile)
                    break
                }
            }
        }

        profilesAdapter.control = object : ProfilesAdapter.Control{
            override fun onClickItem(position: Int, view: View) {

                val popup = PopupMenu(this@ProfilesActivity, view)
                popup.inflate(R.menu.profile)

                popup.setOnMenuItemClickListener { item ->

                    val profile = profilesAdapter.getItem(position)

                    when (item.itemId) {
                        R.id.action_context_default -> {
                            preferences.edit().putString(PREF_DEFAULT_PROFILE, profile.name).apply()
                            profilesAdapter.setDefault(profile)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.action_context_edit -> {
                            val intent = Intent(
                                ACTION_EDIT_PROFILE,
                                Uri.parse(profile.name),
                                applicationContext, ConfigActivity::class.java
                            )
                            startActivity(intent)
                            return@setOnMenuItemClickListener true
                        }
                        R.id.action_context_rename -> {
                            EditNameAlert.newInstance(getString(R.string.enter_new_name), position)
                                .show(supportFragmentManager, "alert_rename_profile")
                        }
                        R.id.action_context_delete -> {
                            profile.delete()
                            profilesAdapter.removeItem(position)
                        }
                    }

                    return@setOnMenuItemClickListener false
                }

                popup.show()
            }

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_profiles, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == android.R.id.home) {
            finish()
            return true
        } else if (itemId == R.id.add) {
            EditNameAlert.newInstance(getString(R.string.enter_name), -1)
                .show(supportFragmentManager, "alert_create_profile")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
        val inflater = menuInflater
        inflater.inflate(R.menu.profile, menu)
        val info = menuInfo as AdapterContextMenuInfo
        val profile = profilesAdapter.getItem(info.position)
        if (!profile.hasConfig() && !profile.hasOldConfig()) {
            menu.findItem(R.id.action_context_default).isVisible = false
            menu.findItem(R.id.action_context_edit).isVisible = false
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterContextMenuInfo
        val index = info.position
        val profile = profilesAdapter.getItem(index)
        val itemId = item.itemId
        if (itemId == R.id.action_context_default) {
            preferences.edit().putString(PREF_DEFAULT_PROFILE, profile.name).apply()
            profilesAdapter.setDefault(profile)
            return true
        } else if (itemId == R.id.action_context_edit) {
            val intent = Intent(
                ACTION_EDIT_PROFILE,
                Uri.parse(profile.name),
                applicationContext, ConfigActivity::class.java
            )
            startActivity(intent)
            return true
        } else if (itemId == R.id.action_context_rename) {
            EditNameAlert.newInstance(getString(R.string.enter_new_name), index)
                .show(supportFragmentManager, "alert_rename_profile")
        } else if (itemId == R.id.action_context_delete) {
            profile.delete()
            profilesAdapter.removeItem(index)
        }
        return super.onContextItemSelected(item)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onNameChanged(id: Int, newName: String) {

        Log.d("onNameChanged", "$id $newName")

        if (id == -1) {
            editProfileLauncher.launch(newName)
            return
        }
        val profile = profilesAdapter.getItem(id)
        profile.renameTo(newName)
        profilesAdapter.notifyDataSetChanged()
        if (profilesAdapter.isDefault(profile)) {
            preferences.edit().putString(PREF_DEFAULT_PROFILE, newName).apply()
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        parent.showContextMenuForChild(view)
    }
}