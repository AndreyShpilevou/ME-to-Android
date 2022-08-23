package app.appsList

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.drawable.IconCompat
import androidx.recyclerview.widget.RecyclerView
import app.activities.BaseActivity
import app.dao.AppItem
import app.dao.AppRepository
import app.utils.*
import app.views.matchParent
import app.views.wrapContent
import ru.playsoftware.j2meloader.R
import ru.playsoftware.j2meloader.config.Config
import ru.playsoftware.j2meloader.config.ConfigActivity
import ru.woesss.j2me.installer.InstallerDialog
import java.io.File

class AppsAdapter(val activity: BaseActivity, private val appRepository: AppRepository)
    : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

    var list: List<AppItem> = listOf()

    inner class ViewHolder(val view: AppItemView) : RecyclerView.ViewHolder(view) {

        @SuppressLint("SetTextI18n")
        fun bind(appItem: AppItem){
            view.setItem(appItem)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(list: List<AppItem>){
        this.list = list

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        AppItemView(parent.context)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])

        val appItem = list[position]
        holder.itemView.onClick {
            Config.startApp(activity, appItem.getDisplayTitle(), appItem.getPathExt(), false)
        }

        holder.itemView.onLongClick {
            popupShow(appItem, it)
            false
        }
    }

    override fun getItemCount() = list.size

    private fun popupShow(appItem: AppItem, anchor: View) {

        val popup = PopupMenu(activity, anchor, Gravity.TOP)
        popup.inflate(R.menu.app_menu)
        popup.setOnMenuItemClickListener { menuItem ->

            when(menuItem.itemId){
                R.id.action_context_shortcut -> {
                    requestAddShortcut(appItem)
                }
                R.id.action_context_rename -> {
                    alertRename(appItem)
                }
                R.id.action_context_settings -> {
                    Config.startApp(activity, appItem.getDisplayTitle(), appItem.getPathExt(), true)
                }
                R.id.action_context_reinstall -> {
                    InstallerDialog.newInstance(appItem.id).show(activity.supportFragmentManager, "installer")
                }
                R.id.action_context_delete -> {
                    alertDelete(appItem)
                }
            }

            return@setOnMenuItemClickListener false
        }

        if (!File(appItem.getPathExt() + Config.MIDLET_RES_FILE).exists()) {
            popup.menu.findItem(R.id.action_context_reinstall).isVisible = false
        }

        popup.show()

    }

    private fun requestAddShortcut(appItem: AppItem) {
        val bitmap = AppUtils.getIconBitmap(appItem)
        val icon: IconCompat = if (bitmap == null) {
            IconCompat.createWithResource(activity, R.mipmap.ic_launcher)
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

            val dst = RectF(0f, 0f, iconSize.toFloat(), iconSize.toFloat())

            val scaled = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
            scaled.applyCanvas {
                drawBitmap(bitmap, src, dst, null)
            }
            IconCompat.createWithBitmap(scaled)
        }

        val title = appItem.getDisplayTitle()
        val launchIntent = Intent(
            Intent.ACTION_DEFAULT, Uri.parse(appItem.getPathExt()),
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

    private fun alertRename(appItem: AppItem) {
        val editText = EditText(activity)
        editText.setText(appItem.getDisplayTitle())

        val linearLayout = LinearLayout(activity)
        val params = LinearLayout.LayoutParams(matchParent, wrapContent)
        params.setMargins(20.dp, 0, 20.dp, 0)

        linearLayout.addView(editText, params)
        editText.setPadding(8.dp, 16.dp, 8.dp, 16.dp)

        val builder = AlertDialog.Builder(activity)
            .setTitle(R.string.action_context_rename)
            .setView(linearLayout)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                val title = editText.text.toString().trim { it <= ' ' }
                if (title == "") {
                    activity.toast(R.string.error)
                } else {
                    appItem.displayName = title
                    appRepository.update(appItem)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    private fun alertDelete(appItem: AppItem) {
        AlertDialog.Builder(activity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.message_delete)
            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface, _: Int ->
                AppUtils.deleteApp(appItem)
                appRepository.delete(appItem)

                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

}