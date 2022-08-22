package app.dialogs

import android.app.ActivityManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.fragment.app.FragmentActivity
import app.dao.AppItem
import app.dao.AppRepository
import app.utils.*
import app.views.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.playsoftware.j2meloader.R
import ru.playsoftware.j2meloader.config.Config
import ru.playsoftware.j2meloader.config.ConfigActivity
import ru.woesss.j2me.installer.InstallerDialog
import java.io.File

class AppCardBottomDialog(
    private val activity: FragmentActivity,
    private val appRepository: AppRepository,
    private val appItem: AppItem
) : BottomSheetDialog(activity, R.style.BottomSheetDialog) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        behavior.state = BottomSheetBehavior.STATE_COLLAPSED

        (context.resources.displayMetrics.heightPixels * 0.9f).toInt().let {
            behavior.peekHeight = it
            behavior.maxHeight = it
        }



        verticalLayout {

            verticalLayout {
                backgroundResources = R.drawable.round_bg

                cardView {
                    setCardBackgroundColor(ContextCompat.getColor(context, R.color.background))
                    radius = 12.dpF
                }.lparams(60.dp, 6.dp) {
                    setMargins(0, 12.dp, 0, 12.dp)
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                linearLayout {
                    gravity = Gravity.CENTER_VERTICAL

                    imageView {
                        Drawable.createFromPath(appItem.getImagePathExt())?.also { icon ->
                            icon.isFilterBitmap = false
                            imageDrawable = icon
                        } ?: let {
                            imageResources = R.mipmap.ic_launcher
                        }
                    }.lparams(70.dp, 70.dp) {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    verticalLayout {

                        textView {
                            text = appItem.getDisplayTitle()
                            textSize = 20f
                            textColor = Color.BLACK
                            typeface = Typeface.DEFAULT_BOLD
                        }.lparams(matchParent, wrapContent)

                        linearLayout {
                            gravity = Gravity.CENTER_VERTICAL

                            textView {
                                text = appItem.author
                                textSize = 15f
                                textColor = Color.BLACK
                                alpha = 0.9f
                            }.lparams(matchParent, wrapContent, 1f)

                            textView {
                                text = appItem.version
                                textSize = 14f
                                textColor = Color.BLACK
                                alpha = 0.8f
                            }.lparams(wrapContent, wrapContent)

                        }.lparams(matchParent, wrapContent) {
                            setMargins(0, 6.dp, 0, 0)
                        }

                    }.lparams(matchParent, wrapContent, 1f) {
                        setMargins(18.dp, 0, 0, 0)
                    }

                }.lparams(matchParent, wrapContent) {
                    setMargins(24.dp, 12.dp, 24.dp, 24.dp)
                }

            }.lparams(matchParent, wrapContent) {
                setMargins(12.dp, 0, 12.dp, 18.dp)
            }

            button {
                text = "Play"
                backgroundResources = R.drawable.round_bg
            }.lparams(matchParent, wrapContent) {
                setMargins(12.dp, 6.dp, 12.dp, 6.dp)
            }.onClick {
                Config.startApp(activity, appItem.getDisplayTitle(), appItem.getPathExt(), false)
            }

            button {
                text = "AddShortcut"
                backgroundResources = R.drawable.round_bg
            }.lparams(matchParent, wrapContent) {
                setMargins(12.dp, 6.dp, 12.dp, 6.dp)
            }.onClick {
                requestAddShortcut()
            }

            button {
                text = "Rename"
                backgroundResources = R.drawable.round_bg
            }.lparams(matchParent, wrapContent) {
                setMargins(12.dp, 6.dp, 12.dp, 6.dp)
            }.onClick {
                alertRename()
            }

            button {
                text = "Settings"
                backgroundResources = R.drawable.round_bg
            }.lparams(matchParent, wrapContent) {
                setMargins(12.dp, 6.dp, 12.dp, 6.dp)
            }.onClick {
                Config.startApp(activity, appItem.getDisplayTitle(), appItem.getPathExt(), true)
            }

            if (File(appItem.getPathExt() + Config.MIDLET_RES_FILE).exists()) {
                button {
                    text = "Reinstall"
                    backgroundResources = R.drawable.round_bg
                }.lparams(matchParent, wrapContent) {
                    setMargins(12.dp, 6.dp, 12.dp, 6.dp)
                }.onClick {
                    InstallerDialog.newInstance(appItem.id)
                        .show(activity.supportFragmentManager, "installer")
                }
            }

            button {
                text = "Delete"
                textColor = Color.RED
                backgroundResources = R.drawable.round_bg
            }.lparams(matchParent, wrapContent) {
                setMargins(12.dp, 6.dp, 12.dp, 12.dp)
            }.onClick {
                alertDelete()
            }

        }

    }

    private fun requestAddShortcut() {
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

    private fun alertRename() {
        val editText = EditText(activity)
        editText.setText(appItem.getDisplayTitle())
        val density = activity.resources.displayMetrics.density
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
                    Toast.makeText(activity, R.string.error, Toast.LENGTH_SHORT).show()
                } else {
                    appItem.displayName = title
                    appRepository.update(appItem)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    private fun alertDelete() {
        val builder = AlertDialog.Builder(activity)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.message_delete)
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                AppUtils.deleteApp(appItem)
                appRepository.delete(appItem)

                dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

}