package app.profile

import app.profile.Config.profilesDir
import app.profile.Config.emulatorDir
import app.profile.ProfilesManager.loadConfig
import app.utils.FileUtils.copyFiles
import app.utils.FileUtils.getText
import app.utils.FileUtils.copyFileUsingChannel
import app.profile.ProfilesManager.saveConfig
import app.utils.FileUtils.clearDirectory
import app.activities.BaseActivity
import android.widget.SeekBar.OnSeekBarChangeListener
import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import android.os.storage.StorageManager
import ru.playsoftware.j2meloader.R
import android.content.DialogInterface
import android.text.TextWatcher
import android.text.Editable
import android.view.View.OnLayoutChangeListener
import android.view.View.OnFocusChangeListener
import android.text.TextUtils
import javax.microedition.util.ContextHolder
import javax.microedition.shell.MicroActivity
import ru.playsoftware.j2meloader.settings.KeyMapperActivity
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import yuku.ambilwarna.AmbilWarnaDialog
import android.graphics.drawable.ColorDrawable
import androidx.core.widget.TextViewCompat
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.text.InputFilter
import android.text.Spanned
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.fragment.app.FragmentManager
import androidx.preference.PreferenceManager
import app.utils.Constants
import app.utils.Constants.ACTION_EDIT
import app.utils.Constants.ACTION_EDIT_PROFILE
import app.utils.Constants.KEY_MIDLET_NAME
import app.utils.Constants.KEY_START_ARGUMENTS
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.util.*

class ConfigActivity : BaseActivity(), View.OnClickListener, ShaderTuneAlert.Callback,
    OnSeekBarChangeListener {

    private lateinit var rootContainer: ScrollView
    private lateinit var tfScreenWidth: EditText
    private lateinit var tfScreenHeight: EditText
    private lateinit var cbLockAspect: CheckBox
    private lateinit var tfScaleRatioValue: EditText
    private lateinit var spOrientation: Spinner
    private lateinit var spScaleType: Spinner
    private lateinit var cxFilter: Checkable
    private lateinit var cxImmediate: Checkable
    private lateinit var spGraphicsMode: Spinner
    private lateinit var spShader: Spinner
    private lateinit var cxParallel: CompoundButton
    private lateinit var cxForceFullscreen: Checkable
    private lateinit var tfFontSizeSmall: EditText
    private lateinit var tfFontSizeMedium: EditText
    private lateinit var tfFontSizeLarge: EditText
    private lateinit var cxFontSizeInSP: Checkable
    private lateinit var cxFontAA: Checkable
    private lateinit var cxShowKeyboard: CompoundButton
    private lateinit var rootInputConfig: View
    private lateinit var groupVkConfig: View
    private lateinit var cxVKFeedback: Checkable
    private lateinit var cxTouchInput: Checkable
    private lateinit var spLayout: Spinner
    private lateinit var spButtonsShape: Spinner
    private lateinit var sbVKAlpha: SeekBar
    private lateinit var cxVKForceOpacity: Checkable
    private lateinit var tfVKHideDelay: EditText
    private lateinit var tfVKFore: EditText
    private lateinit var tfVKBack: EditText
    private lateinit var tfVKSelFore: EditText
    private lateinit var tfVKSelBack: EditText
    private lateinit var tfVKOutline: EditText
    private lateinit var tfSystemProperties: EditText
    private var screenPresets = ArrayList<String>()
    private var fontPresetValues = ArrayList<IntArray>()
    private var fontPresetTitles = ArrayList<String>()
    private var keylayoutFile: File? = null
    private var dataDirFile: File? = null
    private var params: ProfileModel? = null
    private var fragmentManager: FragmentManager? = null
    private var isProfile = false
    private var configDir: File? = null
    private var defProfile: String? = null
    private var spShaderAdapter: ArrayAdapter<ShaderInfo>? = null
    private lateinit var shaderContainer: View
    private lateinit var btShaderTune: ImageButton
    private var workDir: String? = null
    private var needShow = false
    private lateinit var tvGravityHorizontal: SeekBar
    private lateinit var tvGravityVertical: SeekBar

    private var displayWidth: Int = 0
    private var displayHeight: Int = 0

    @SuppressLint("StringFormatMatches", "StringFormatInvalid")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = intent
        val action = intent.action
        isProfile = ACTION_EDIT_PROFILE == action
        needShow = isProfile || ACTION_EDIT == action
        val path = intent.dataString
        if (path == null) {
            needShow = false
            finish()
            return
        }
        if (isProfile) {
            setResult(RESULT_OK, Intent().setData(intent.data))
            configDir = File(profilesDir, path)
            workDir = emulatorDir
            title = path
        } else {
            title = intent.getStringExtra(Constants.KEY_MIDLET_NAME)
            val appDir = File(path)
            val convertedDir = appDir.parentFile
            if (!appDir.isDirectory || convertedDir == null || convertedDir.parent.also {
                    workDir = it
                } == null) {
                needShow = false
                var storageName = ""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val sm = getSystemService(STORAGE_SERVICE) as StorageManager
                    if (sm != null) {
                        val storageVolume = sm.getStorageVolume(appDir)
                        if (storageVolume != null) {
                            val desc = storageVolume.getDescription(this)
                            if (desc != null) {
                                storageName = "\"$desc\" "
                            }
                        }
                    }
                }
                AlertDialog.Builder(this)
                    .setTitle(R.string.error)
                    .setMessage(getString(R.string.err_missing_app, storageName))
                    .setPositiveButton(R.string.exit) { d: DialogInterface?, w: Int -> finish() }
                    .setCancelable(false)
                    .show()
                return
            }
            dataDirFile = File(workDir + Config.MIDLET_DATA_DIR + appDir.name)
            dataDirFile!!.mkdirs()
            configDir = File(workDir + Config.MIDLET_CONFIGS_DIR + appDir.name)
        }
        configDir!!.mkdirs()
        defProfile = PreferenceManager.getDefaultSharedPreferences(
            applicationContext
        )
            .getString(Constants.PREF_DEFAULT_PROFILE, null)
        loadConfig()
        if (!params!!.isNew && !needShow) {
            startMIDlet()
            return
        }
        loadKeyLayout()
        setContentView(R.layout.activity_config)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val defaultDisplay =
                DisplayManagerCompat.getInstance(this).getDisplay(Display.DEFAULT_DISPLAY)
            val displayContext = createDisplayContext(defaultDisplay!!)

            displayWidth = displayContext.resources.displayMetrics.widthPixels
            displayHeight = displayContext.resources.displayMetrics.heightPixels
        } else {

            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            displayWidth = displayMetrics.widthPixels
            displayHeight = displayMetrics.heightPixels
        }



        fragmentManager = supportFragmentManager
        rootContainer = findViewById(R.id.configRoot)
        tfScreenWidth = findViewById(R.id.tfScreenWidth)
        tfScreenHeight = findViewById(R.id.tfScreenHeight)
        cbLockAspect = findViewById(R.id.cbLockAspect)
        spScaleType = findViewById(R.id.spScaleType)
        tfScaleRatioValue = findViewById(R.id.tfScaleRatioValue)
        spOrientation = findViewById(R.id.spOrientation)
        cxFilter = findViewById(R.id.cxFilter)
        cxImmediate = findViewById(R.id.cxImmediate)
        spGraphicsMode = findViewById(R.id.spGraphicsMode)
        spShader = findViewById(R.id.spShader)
        btShaderTune = findViewById(R.id.btShaderTune)
        shaderContainer = findViewById(R.id.shaderContainer)
        cxParallel = findViewById(R.id.cxParallel)
        cxForceFullscreen = findViewById(R.id.cxForceFullscreen)
        tvGravityHorizontal = findViewById(R.id.tvGravityHorizontal)
        tvGravityVertical = findViewById(R.id.tvGravityVertical)
        tfFontSizeSmall = findViewById(R.id.tfFontSizeSmall)
        tfFontSizeMedium = findViewById(R.id.tfFontSizeMedium)
        tfFontSizeLarge = findViewById(R.id.tfFontSizeLarge)
        cxFontSizeInSP = findViewById(R.id.cxFontSizeInSP)
        cxFontAA = findViewById(R.id.cxFontAA)
        tfSystemProperties = findViewById(R.id.tfSystemProperties)
        rootInputConfig = findViewById(R.id.rootInputConfig)
        cxTouchInput = findViewById(R.id.cxTouchInput)
        cxShowKeyboard = findViewById(R.id.cxIsShowKeyboard)
        groupVkConfig = findViewById(R.id.groupVkConfig)
        cxVKFeedback = findViewById(R.id.cxVKFeedback)
        cxVKForceOpacity = findViewById(R.id.cxVKForceOpacity)
        spLayout = findViewById(R.id.spLayout)
        spButtonsShape = findViewById(R.id.spButtonsShape)
        sbVKAlpha = findViewById(R.id.sbVKAlpha)
        tfVKHideDelay = findViewById(R.id.tfVKHideDelay)
        tfVKFore = findViewById(R.id.tfVKFore)
        tfVKBack = findViewById(R.id.tfVKBack)
        tfVKSelFore = findViewById(R.id.tfVKSelFore)
        tfVKSelBack = findViewById(R.id.tfVKSelBack)
        tfVKOutline = findViewById(R.id.tfVKOutline)

        fillScreenSizePresets(displayWidth, displayHeight)
        addFontSizePreset("128 x 128", 9, 13, 15)
        addFontSizePreset("128 x 160", 13, 15, 20)
        addFontSizePreset("176 x 220", 15, 18, 22)
        addFontSizePreset("240 x 320", 18, 22, 26)

        cbLockAspect.setOnCheckedChangeListener { cb: CompoundButton, isChecked: Boolean ->
            onLockAspectChanged(
                cb,
                isChecked
            )
        }
        findViewById<View>(R.id.cmdScreenSizePresets).setOnClickListener { v: View ->
            showScreenPresets(
                v
            )
        }
        findViewById<View>(R.id.cmdSwapSizes).setOnClickListener(this)
        findViewById<View>(R.id.cmdAddToPreset).setOnClickListener { v: View? -> addResolutionToPresets() }
        findViewById<View>(R.id.cmdFontSizePresets).setOnClickListener(this)
        findViewById<View>(R.id.cmdKeyMappings).setOnClickListener(this)
        findViewById<View>(R.id.cmdVKBack).setOnClickListener(this)
        findViewById<View>(R.id.cmdVKFore).setOnClickListener(this)
        findViewById<View>(R.id.cmdVKSelBack).setOnClickListener(this)
        findViewById<View>(R.id.cmdVKSelFore).setOnClickListener(this)
        findViewById<View>(R.id.cmdVKOutline).setOnClickListener(this)
        btShaderTune.setOnClickListener { v: View -> showShaderSettings(v) }
        tfScaleRatioValue.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val length = s.length
                if (length > 4) {
                    if (start >= 4) {
                        tfScaleRatioValue.text.delete(4, length)
                    } else {
                        val st = start + count
                        val end = st + if (before == 0) count else before
                        tfScaleRatioValue.text.delete(st, Math.min(end, length))
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                try {
                    val progress = s.toString().toInt()
                    if (progress > 1000) {
                        s.replace(0, s.length, "1000")
                    }
                } catch (e: NumberFormatException) {
                    s.clear()
                }
            }
        })
        spGraphicsMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0, 3 -> {
                        cxParallel.visibility = View.VISIBLE
                        shaderContainer.visibility = View.GONE
                    }
                    1 -> {
                        cxParallel.visibility = View.GONE
                        initShaderSpinner()
                    }
                    2 -> {
                        cxParallel.visibility = View.GONE
                        shaderContainer.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        cxShowKeyboard.setOnClickListener { b: View? ->
            val onLayoutChangeListener: OnLayoutChangeListener = object : OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    val focus = rootContainer.findFocus()
                    focus?.clearFocus()
                    v.scrollTo(0, rootInputConfig.getTop())
                    v.removeOnLayoutChangeListener(this)
                }
            }
            rootContainer.addOnLayoutChangeListener(onLayoutChangeListener)
            groupVkConfig.setVisibility(if (cxShowKeyboard.isChecked()) View.VISIBLE else View.GONE)
        }
        tfVKFore.addTextChangedListener(ColorTextWatcher(tfVKFore))
        tfVKBack.addTextChangedListener(ColorTextWatcher(tfVKBack))
        tfVKSelFore.addTextChangedListener(ColorTextWatcher(tfVKSelFore))
        tfVKSelBack.addTextChangedListener(ColorTextWatcher(tfVKSelBack))
        tfVKOutline.addTextChangedListener(ColorTextWatcher(tfVKOutline))
        tvGravityHorizontal.setProgress(params!!.screenGravityHorizontal)
        tvGravityVertical.setProgress(params!!.screenGravityVertical)
        tvGravityHorizontal.setOnSeekBarChangeListener(this)
        tvGravityVertical.setOnSeekBarChangeListener(this)
    }

    private fun onLockAspectChanged(cb: CompoundButton, isChecked: Boolean) {
        if (isChecked) {
            val w: Float
            w = try {
                tfScreenWidth!!.text.toString().toInt().toFloat()
            } catch (ignored: Exception) {
                0f
            }
            if (w <= 0) {
                cb.isChecked = false
                return
            }
            val h: Float
            h = try {
                tfScreenHeight!!.text.toString().toInt().toFloat()
            } catch (ignored: Exception) {
                0f
            }
            if (h <= 0) {
                cb.isChecked = false
                return
            }
            tfScreenWidth!!.onFocusChangeListener = ResolutionAutoFill(
                tfScreenWidth,
                tfScreenHeight,
                h / w
            )
            tfScreenHeight!!.onFocusChangeListener = ResolutionAutoFill(
                tfScreenHeight,
                tfScreenWidth,
                w / h
            )
        } else {
            var listener = tfScreenWidth!!.onFocusChangeListener
            if (listener != null) {
                listener.onFocusChange(tfScreenWidth, false)
                tfScreenWidth!!.onFocusChangeListener = null
            }
            listener = tfScreenHeight!!.onFocusChangeListener
            if (listener != null) {
                listener.onFocusChange(tfScreenHeight, false)
                tfScreenHeight!!.onFocusChangeListener = null
            }
        }
    }

    fun loadConfig() {
        params = loadConfig(configDir)
        if (params == null && defProfile != null) {
            copyFiles(File(profilesDir, defProfile), configDir!!, null)
            params = loadConfig(configDir)
        }
        if (params == null) {
            params = ProfileModel(configDir)
        }
    }

    private fun showShaderSettings(v: View) {
        val shader = spShader!!.selectedItem as ShaderInfo
        params!!.shader = shader
        ShaderTuneAlert.newInstance(shader).show(supportFragmentManager, "ShaderTuning")
    }

    private fun initShaderSpinner() {
        if (spShaderAdapter != null) {
            shaderContainer!!.visibility = View.VISIBLE
            return
        }
        val dir = File(workDir + Config.SHADERS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val infos = ArrayList<ShaderInfo>()
        spShaderAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, infos)
        spShaderAdapter!!.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spShader!!.adapter = spShaderAdapter
        val files = dir.listFiles { f: File ->
            f.isFile && f.name.lowercase(Locale.getDefault()).endsWith(".ini")
        }
        if (files != null) {
            for (file in files) {
                val text = getText(file.absolutePath)
                val split = text.split("[\\n\\r]+".toRegex()).toTypedArray()
                var info: ShaderInfo? = null
                for (line in split) {
                    if (line.startsWith("[")) {
                        if (info != null && info.fragment != null && info.vertex != null) {
                            infos.add(info)
                        }
                        info = ShaderInfo(line.replace("[\\[\\]]".toRegex(), ""), "unknown")
                    } else if (info != null) {
                        try {
                            info.set(line)
                        } catch (e: Exception) {
                            Log.e(TAG, "initShaderSpinner: ", e)
                        }
                    }
                }
                if (info != null && info.fragment != null && info.vertex != null) {
                    infos.add(info)
                }
            }
            Collections.sort(infos)
        }
        infos.add(0, ShaderInfo(getString(R.string.identity_filter), "woesss"))
        spShaderAdapter!!.notifyDataSetChanged()
        val selected = params!!.shader
        if (selected != null) {
            val position = infos.indexOf(selected)
            if (position > 0) {
                infos[position].values = selected.values
                spShader!!.setSelection(position)
            }
        }
        shaderContainer!!.visibility = View.VISIBLE
        spShader!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val item = parent.getItemAtPosition(position) as ShaderInfo
                val settings = item.settings
                var values = item.values
                if (values == null) {
                    for (i in 0..3) {
                        if (settings[i] != null) {
                            if (values == null) {
                                values = FloatArray(4)
                            }
                            values[i] = settings[i]!!.def
                        }
                    }
                }
                if (values == null) {
                    btShaderTune!!.visibility = View.GONE
                } else {
                    item.values = values
                    btShaderTune!!.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showCharsetPicker(v: View) {
        val charsets = Charset.availableCharsets().keys.toTypedArray()
        AlertDialog.Builder(this).setItems(charsets) { d: DialogInterface?, w: Int ->
            val enc = "microedition.encoding: " + charsets[w]
            val props =
                tfSystemProperties!!.text.toString().split("[\\n\\r]+".toRegex()).toTypedArray()
            val propsLength = props.size
            if (propsLength == 0) {
                tfSystemProperties!!.setText(enc)
                return@setItems
            }
            var i = propsLength - 1
            while (i >= 0) {
                if (props[i].startsWith("microedition.encoding")) {
                    props[i] = enc
                    break
                }
                i--
            }
            if (i < 0) {
                tfSystemProperties!!.append(enc)
                return@setItems
            }
            tfSystemProperties!!.setText(TextUtils.join("\n", props))
        }.setTitle(R.string.pref_encoding_title).show()
    }

    private fun loadKeyLayout() {
        val file = File(configDir, Config.MIDLET_KEY_LAYOUT_FILE)
        keylayoutFile = file
        if (isProfile || file.exists()) {
            return
        }
        if (defProfile == null) {
            return
        }
        val defaultKeyLayoutFile = File(profilesDir + defProfile, Config.MIDLET_KEY_LAYOUT_FILE)
        if (!defaultKeyLayoutFile.exists()) {
            return
        }
        try {
            copyFileUsingChannel(defaultKeyLayoutFile, file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    public override fun onPause() {
        if (needShow && configDir != null) {
            saveParams()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (needShow) {
            loadParams(true)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        fillScreenSizePresets(displayWidth, displayHeight)
    }

    private fun fillScreenSizePresets(w: Int, h: Int) {
        val screenPresets = screenPresets
        screenPresets.clear()
        screenPresets.add("128 x 128")
        screenPresets.add("128 x 160")
        screenPresets.add("132 x 176")
        screenPresets.add("176 x 220")
        screenPresets.add("240 x 320")
        screenPresets.add("352 x 416")
        screenPresets.add("640 x 360")
        screenPresets.add("800 x 480")
        if (w > h) {
            screenPresets.add((h * 3 / 4).toString() + " x " + h)
            screenPresets.add((h * 4 / 3).toString() + " x " + h)
        } else {
            screenPresets.add(w.toString() + " x " + w * 4 / 3)
            screenPresets.add(w.toString() + " x " + w * 3 / 4)
        }
        screenPresets.add("$w x $h")
        val preset = PreferenceManager.getDefaultSharedPreferences(this)
            .getStringSet("ResolutionsPreset", null)
        if (preset != null) {
            screenPresets.addAll(preset)
        }
        Collections.sort(screenPresets) { o1: String, o2: String ->
            val sep1 = o1.indexOf(" x ")
            val sep2 = o2.indexOf(" x ")
            if (sep1 == -1) {
                if (sep2 != -1) return@sort -1 else return@sort 0
            } else if (sep2 == -1) return@sort 1
            val r = Integer.decode(o1.substring(0, sep1))
                .compareTo(Integer.decode(o2.substring(0, sep2)))
            if (r != 0) return@sort r
            Integer.decode(o1.substring(sep1 + 3)).compareTo(Integer.decode(o2.substring(sep2 + 3)))
        }
        var prev: String? = null
        val iterator = screenPresets.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next == prev) iterator.remove() else prev = next
        }
    }

    private fun addFontSizePreset(title: String, small: Int, medium: Int, large: Int) {
        fontPresetValues.add(intArrayOf(small, medium, large))
        fontPresetTitles.add(title)
    }

    private fun parseInt(s: String, radix: Int = 10): Int {
        val result: Int
        result = try {
            s.toInt(radix)
        } catch (e: NumberFormatException) {
            0
        }
        return result
    }

    @SuppressLint("SetTextI18n")
    fun loadParams(reloadFromFile: Boolean) {
        if (reloadFromFile) {
            loadConfig()
        }
        val screenWidth = params!!.screenWidth
        if (screenWidth != 0) {
            tfScreenWidth.setText(screenWidth.toString())
        }
        val screenHeight = params!!.screenHeight
        if (screenHeight != 0) {
            tfScreenHeight.setText(screenHeight.toString())
        }
        tfScaleRatioValue.setText(params!!.screenScaleRatio.toString())
        spOrientation.setSelection(params!!.orientation)
        spScaleType.setSelection(params!!.screenScaleType)
        cxFilter.isChecked = params!!.screenFilter
        cxImmediate.isChecked = params!!.immediateMode
        cxParallel.isChecked = params!!.parallelRedrawScreen
        cxForceFullscreen.isChecked = params!!.forceFullscreen
        spGraphicsMode.setSelection(params!!.graphicsMode)
        tfFontSizeSmall.setText(params!!.fontSizeSmall.toString())
        tfFontSizeMedium.setText(params!!.fontSizeMedium.toString())
        tfFontSizeLarge.setText(params!!.fontSizeLarge.toString())
        cxFontSizeInSP.isChecked = params!!.fontApplyDimensions
        cxFontAA.isChecked = params!!.fontAA
        val showVk = params!!.showKeyboard
        cxShowKeyboard.isChecked = showVk
        groupVkConfig.visibility = if (showVk) View.VISIBLE else View.GONE
        cxVKFeedback.isChecked = params!!.vkFeedback
        cxVKForceOpacity.isChecked = params!!.vkForceOpacity
        cxTouchInput.isChecked = params!!.touchInput
        spLayout.setSelection(params!!.keyCodesLayout)
        spButtonsShape.setSelection(params!!.vkButtonShape)
        sbVKAlpha.progress = params!!.vkAlpha
        val vkHideDelay = params!!.vkHideDelay
        tfVKHideDelay.setText(if (vkHideDelay > 0) vkHideDelay.toString() else "")
        tfVKBack.setText(String.format("%06X", params!!.vkBgColor))
        tfVKFore.setText(String.format("%06X", params!!.vkFgColor))
        tfVKSelBack.setText(String.format("%06X", params!!.vkBgColorSelected))
        tfVKSelFore.setText(String.format("%06X", params!!.vkFgColorSelected))
        tfVKOutline.setText(String.format("%06X", params!!.vkOutlineColor))
        var systemProperties = params!!.systemProperties
        if (systemProperties == null) {
            systemProperties = ContextHolder.getAssetAsString("defaults/system.props")
        }
        tfSystemProperties!!.setText(systemProperties)
    }

    private fun saveParams() {
        try {
            params!!.screenWidth = parseInt(tfScreenWidth!!.text.toString())
            params!!.screenHeight = parseInt(tfScreenHeight!!.text.toString())
            try {
                params!!.screenScaleRatio = tfScaleRatioValue!!.text.toString().toInt()
            } catch (e: NumberFormatException) {
                params!!.screenScaleRatio = 100
            }
            params!!.orientation = spOrientation!!.selectedItemPosition
            params!!.screenScaleType = spScaleType!!.selectedItemPosition
            params!!.screenFilter = cxFilter!!.isChecked
            params!!.immediateMode = cxImmediate!!.isChecked
            val mode = spGraphicsMode!!.selectedItemPosition
            params!!.graphicsMode = mode
            if (mode == 1) {
                if (spShader!!.selectedItemPosition == 0) params!!.shader =
                    null else params!!.shader = spShader!!.selectedItem as ShaderInfo
            }
            params!!.parallelRedrawScreen = cxParallel!!.isChecked
            params!!.forceFullscreen = cxForceFullscreen!!.isChecked
            try {
                params!!.fontSizeSmall = tfFontSizeSmall!!.text.toString().toInt()
            } catch (e: NumberFormatException) {
                params!!.fontSizeSmall = 0
            }
            try {
                params!!.fontSizeMedium = tfFontSizeMedium!!.text.toString().toInt()
            } catch (e: NumberFormatException) {
                params!!.fontSizeMedium = 0
            }
            try {
                params!!.fontSizeLarge = tfFontSizeLarge!!.text.toString().toInt()
            } catch (e: NumberFormatException) {
                params!!.fontSizeLarge = 0
            }
            params!!.fontApplyDimensions = cxFontSizeInSP!!.isChecked
            params!!.fontAA = cxFontAA!!.isChecked
            params!!.showKeyboard = cxShowKeyboard!!.isChecked
            params!!.vkFeedback = cxVKFeedback!!.isChecked
            params!!.vkForceOpacity = cxVKForceOpacity!!.isChecked
            params!!.touchInput = cxTouchInput!!.isChecked
            params!!.keyCodesLayout = spLayout!!.selectedItemPosition
            params!!.vkButtonShape = spButtonsShape!!.selectedItemPosition
            params!!.vkAlpha = sbVKAlpha!!.progress
            params!!.vkHideDelay = parseInt(tfVKHideDelay!!.text.toString())
            try {
                params!!.vkBgColor = tfVKBack!!.text.toString().toInt(16)
            } catch (ignored: Exception) {
            }
            try {
                params!!.vkFgColor = tfVKFore!!.text.toString().toInt(16)
            } catch (ignored: Exception) {
            }
            try {
                params!!.vkBgColorSelected = tfVKSelBack!!.text.toString().toInt(16)
            } catch (ignored: Exception) {
            }
            try {
                params!!.vkFgColorSelected = tfVKSelFore!!.text.toString().toInt(16)
            } catch (ignored: Exception) {
            }
            try {
                params!!.vkOutlineColor = tfVKOutline!!.text.toString().toInt(16)
            } catch (ignored: Exception) {
            }
            params!!.systemProperties = systemProperties
            saveConfig(params)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    private val systemProperties: String
        get() {
            val s = tfSystemProperties.text.toString()
            val lines = s.split("\\n".toRegex()).toTypedArray()
            val sb = StringBuilder(s.length)
            var validCharset = false
            for (i in lines.indices.reversed()) {
                val line = lines[i]
                if (line.trim { it <= ' ' }.isEmpty()) continue
                if (line.startsWith("microedition.encoding:")) {
                    if (validCharset) continue
                    validCharset = try {
                        Charset.forName(line.substring(22).trim { it <= ' ' })
                        true
                    } catch (ignored: Exception) {
                        continue
                    }
                }
                sb.append(line).append('\n')
            }
            return sb.toString()
        }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.config, menu)
        if (isProfile) {
            menu.findItem(R.id.action_start).isVisible = false
            menu.findItem(R.id.action_clear_data).isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_start) {
            startMIDlet()
        } else if (itemId == R.id.action_clear_data) {
            showClearDataDialog()
        } else if (itemId == R.id.action_reset_settings) {
            params = ProfileModel(configDir)
            loadParams(false)
        } else if (itemId == R.id.action_reset_layout) {
            keylayoutFile!!.delete()
            loadKeyLayout()
        } else if (itemId == R.id.action_load_profile) {
            LoadProfileAlert.newInstance(keylayoutFile!!.parent)
                .show(fragmentManager!!, "load_profile")
        } else if (itemId == R.id.action_save_profile) {
            saveParams()
            SaveProfileAlert.getInstance(keylayoutFile!!.parent)
                .show(fragmentManager!!, "save_profile")
        } else if (itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showClearDataDialog() {
        val builder = AlertDialog.Builder(this)
            .setTitle(android.R.string.dialog_alert_title)
            .setMessage(R.string.message_clear_data)
            .setPositiveButton(android.R.string.ok) { d: DialogInterface?, w: Int ->
                clearDirectory(
                    dataDirFile!!
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
        builder.show()
    }

    private fun startMIDlet() {
        val i = Intent(this, MicroActivity::class.java)
        i.data = intent.data
        i.putExtra(KEY_MIDLET_NAME, intent.getStringExtra(KEY_MIDLET_NAME))
        i.putExtra(KEY_START_ARGUMENTS, intent.getStringExtra(KEY_START_ARGUMENTS))
        startActivity(i)
        finish()
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.cmdSwapSizes) {
            val tmp = tfScreenWidth!!.text.toString()
            tfScreenWidth!!.setText(tfScreenHeight!!.text.toString())
            tfScreenHeight!!.setText(tmp)
        } else if (id == R.id.cmdFontSizePresets) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.SIZE_PRESETS))
                .setItems(
                    fontPresetTitles.toTypedArray()
                ) { dialog: DialogInterface?, which: Int ->
                    val values = fontPresetValues[which]
                    tfFontSizeSmall!!.setText(Integer.toString(values[0]))
                    tfFontSizeMedium!!.setText(Integer.toString(values[1]))
                    tfFontSizeLarge!!.setText(Integer.toString(values[2]))
                }
                .show()
        } else if (id == R.id.cmdVKBack) {
            showColorPicker(tfVKBack)
        } else if (id == R.id.cmdVKFore) {
            showColorPicker(tfVKFore)
        } else if (id == R.id.cmdVKSelFore) {
            showColorPicker(tfVKSelFore)
        } else if (id == R.id.cmdVKSelBack) {
            showColorPicker(tfVKSelBack)
        } else if (id == R.id.cmdVKOutline) {
            showColorPicker(tfVKOutline)
        } else if (id == R.id.cmdKeyMappings) {
            val i = Intent(
                intent.action, Uri.parse(configDir!!.path),
                this, KeyMapperActivity::class.java
            )
            startActivity(i)
        }
    }

    private fun showScreenPresets(v: View) {
        val popup = PopupMenu(this, v)
        val menu = popup.menu
        for (preset in screenPresets) {
            menu.add(preset)
        }
        popup.setOnMenuItemClickListener { item: MenuItem ->
            val string = item.title.toString()
            val separator = string.indexOf(" x ")
            tfScreenWidth!!.setText(string.substring(0, separator))
            tfScreenHeight!!.setText(string.substring(separator + 3))
            true
        }
        popup.show()
    }

    private fun showColorPicker(et: EditText?) {
        val colorListener: OnAmbilWarnaListener = object : OnAmbilWarnaListener {
            override fun onOk(dialog: AmbilWarnaDialog, color: Int) {
                et!!.setText(String.format("%06X", color and 0xFFFFFF))
                val drawable = TextViewCompat.getCompoundDrawablesRelative(et)[2] as ColorDrawable
                drawable.color = color
            }

            override fun onCancel(dialog: AmbilWarnaDialog) {}
        }
        val color = parseInt(et!!.text.toString().trim { it <= ' ' }, 16)
        AmbilWarnaDialog(this, color or -0x1000000, colorListener).show()
    }

    private fun addResolutionToPresets() {
        var width = tfScreenWidth!!.text.toString()
        var height = tfScreenHeight!!.text.toString()
        if (width.isEmpty()) width = "-1"
        if (height.isEmpty()) height = "-1"
        val w = parseInt(width)
        val h = parseInt(height)
        if (w <= 0 || h <= 0) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            return
        }
        val preset = "$width x $height"
        if (screenPresets.contains(preset)) {
            Toast.makeText(this, R.string.not_saved_exists, Toast.LENGTH_SHORT).show()
            return
        }
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        var set = preferences.getStringSet("ResolutionsPreset", null)
        if (set == null) {
            set = HashSet(1)
        }
        if (set.add(preset)) {
            preferences.edit().putStringSet("ResolutionsPreset", set).apply()
            screenPresets.add(preset)
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, R.string.not_saved_exists, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTuneComplete(values: FloatArray) {
        params!!.shader!!.values = values
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (seekBar.id == R.id.tvGravityHorizontal) {
            params!!.screenGravityHorizontal = progress
        } else if (seekBar.id == R.id.tvGravityVertical) {
            params!!.screenGravityVertical = progress
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
    private class ColorTextWatcher internal constructor(private val editText: EditText?) :
        TextWatcher {
        private val drawable: ColorDrawable
        private fun filter(
            src: CharSequence,
            ss: Int,
            se: Int,
            dst: Spanned,
            ds: Int,
            de: Int
        ): CharSequence {
            val sb = StringBuilder(se - ss)
            for (i in ss until se) {
                val c = src[i]
                if (c >= '0' && c <= '9' || c >= 'A' && c <= 'F') {
                    sb.append(c)
                } else if (c >= 'a' && c <= 'f') {
                    sb.append((c.code - 32).toChar())
                }
            }
            return sb
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (s.length > 6) {
                if (start >= 6) editText!!.text.delete(6, s.length) else {
                    val st = start + count
                    val end = st + if (before == 0) count else before
                    editText!!.text.delete(st, Math.min(end, s.length))
                }
            }
        }

        override fun afterTextChanged(s: Editable) {
            if (s.length == 0) return
            try {
                val color = s.toString().toInt(16)
                drawable.color = color or Color.BLACK
            } catch (e: NumberFormatException) {
                drawable.color = Color.BLACK
                s.clear()
            }
        }

        init {
            val size = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 32f,
                editText!!.resources.displayMetrics
            ).toInt()
            val colorDrawable = ColorDrawable()
            colorDrawable.setBounds(0, 0, size, size)
            TextViewCompat.setCompoundDrawablesRelative(editText, null, null, colorDrawable, null)
            drawable = colorDrawable
            editText.filters =
                arrayOf(InputFilter { src: CharSequence, ss: Int, se: Int, dst: Spanned, ds: Int, de: Int ->
                    this.filter(
                        src,
                        ss,
                        se,
                        dst,
                        ds,
                        de
                    )
                })
        }
    }

    private class ResolutionAutoFill(
        private val src: EditText?,
        private val dst: EditText?,
        private val aspect: Float
    ) : TextWatcher, OnFocusChangeListener {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            try {
                val size = src!!.text.toString().toInt()
                if (size <= 0) return
                val value = Math.round(size * aspect)
                dst!!.setText(value.toString())
            } catch (ignored: NumberFormatException) {
            }
        }

        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (hasFocus) {
                src!!.addTextChangedListener(this)
            } else {
                src!!.removeTextChangedListener(this)
            }
        }

        init {
            if (src!!.hasFocus()) src.addTextChangedListener(this)
        }
    }

    companion object {
        private val TAG = ConfigActivity::class.java.simpleName
    }
}