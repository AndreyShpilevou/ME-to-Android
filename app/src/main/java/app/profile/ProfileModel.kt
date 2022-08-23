/*
 *  Copyright 2020 Yury Kharchenko
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.profile

import com.google.gson.annotations.SerializedName
import ru.playsoftware.j2meloader.config.ShaderInfo
import com.google.gson.annotations.JsonAdapter
import app.utils.SparseIntArrayAdapter
import android.util.SparseIntArray
import app.profile.ProfileModel
import java.io.File
import javax.microedition.lcdui.keyboard.VirtualKeyboard
import javax.microedition.util.ContextHolder

class ProfileModel {
    /** True if this is a new profile (not yet saved to file)  */
    @JvmField
    @Transient
    val isNew: Boolean

    @JvmField
    @Transient
    var dir: File? = null

    @SerializedName("Version")
    var version = 0

    @JvmField
    @SerializedName("ScreenWidth")
    var screenWidth = 0

    @JvmField
    @SerializedName("ScreenHeight")
    var screenHeight = 0

    @JvmField
    @SerializedName("ScreenBackgroundColor")
    var screenBackgroundColor = 0

    @JvmField
    @SerializedName("ScreenScaleRatio")
    var screenScaleRatio = 0

    @JvmField
    @SerializedName("Orientation")
    var orientation = 0

    @SerializedName("ScreenScaleToFit")
    var screenScaleToFit = false

    @SerializedName("ScreenKeepAspectRatio")
    var screenKeepAspectRatio = false

    @JvmField
    @SerializedName("ScreenScaleType")
    var screenScaleType = 0

    @JvmField
    @SerializedName("ScreenGravity")
    var screenGravity = 0

    @JvmField
    @SerializedName("ScreenFilter")
    var screenFilter = false

    @JvmField
    @SerializedName("ImmediateMode")
    var immediateMode = false

    @SerializedName("HwAcceleration")
    var hwAcceleration = false

    @JvmField
    @SerializedName("GraphicsMode")
    var graphicsMode = 0

    @JvmField
    @SerializedName("Shader")
    var shader: ShaderInfo? = null

    @JvmField
    @SerializedName("ParallelRedrawScreen")
    var parallelRedrawScreen = false

    @JvmField
    @SerializedName("ShowFps")
    var showFps = false

    @JvmField
    @SerializedName("FpsLimit")
    var fpsLimit = 0

    @JvmField
    @SerializedName("ForceFullscreen")
    var forceFullscreen = false

    @JvmField
    @SerializedName("FontSizeSmall")
    var fontSizeSmall = 0

    @JvmField
    @SerializedName("FontSizeMedium")
    var fontSizeMedium = 0

    @JvmField
    @SerializedName("FontSizeLarge")
    var fontSizeLarge = 0

    @JvmField
    @SerializedName("FontApplyDimensions")
    var fontApplyDimensions = false

    @JvmField
    @SerializedName("FontAntiAlias")
    var fontAA = false

    @JvmField
    @SerializedName("TouchInput")
    var touchInput = false

    @JvmField
    @SerializedName("ShowKeyboard")
    var showKeyboard = false

    @JvmField
    @SerializedName("VirtualKeyboardType")
    var vkType = 0

    @JvmField
    @SerializedName("ButtonShape")
    var vkButtonShape = 0

    @JvmField
    @SerializedName("VirtualKeyboardAlpha")
    var vkAlpha = 0

    @JvmField
    @SerializedName("VirtualKeyboardForceOpacity")
    var vkForceOpacity = false

    @JvmField
    @SerializedName("VirtualKeyboardFeedback")
    var vkFeedback = false

    @JvmField
    @SerializedName("VirtualKeyboardDelay")
    var vkHideDelay = 0

    @JvmField
    @SerializedName("VirtualKeyboardColorBackground")
    var vkBgColor = 0

    @JvmField
    @SerializedName("VirtualKeyboardColorBackgroundSelected")
    var vkBgColorSelected = 0

    @JvmField
    @SerializedName("VirtualKeyboardColorForeground")
    var vkFgColor = 0

    @JvmField
    @SerializedName("VirtualKeyboardColorForegroundSelected")
    var vkFgColorSelected = 0

    @JvmField
    @SerializedName("VirtualKeyboardColorOutline")
    var vkOutlineColor = 0

    @JvmField
    @SerializedName("Layout")
    var keyCodesLayout = 0

    @JsonAdapter(SparseIntArrayAdapter::class)
    @SerializedName("KeyCodeMap")
    var keyCodeMap: SparseIntArray? = null

    @JvmField
    @JsonAdapter(SparseIntArrayAdapter::class)
    @SerializedName("KeyMappings")
    var keyMappings: SparseIntArray? = null

    @JvmField
    @SerializedName("SystemProperties")
    var systemProperties: String? = null

    // Gson uses default constructor if present
    constructor() {
        isNew = false
    }

    constructor(dir: File?) {
        this.dir = dir
        isNew = true
        version = VERSION
        screenWidth = 240
        screenHeight = 320
        screenBackgroundColor = 0xD0D0D0
        screenScaleType = 1
        screenGravity = 1
        screenScaleRatio = 100
        screenScaleToFit = true
        screenKeepAspectRatio = true
        graphicsMode = 1
        fontSizeSmall = 18
        fontSizeMedium = 22
        fontSizeLarge = 26
        fontAA = true
        showKeyboard = true
        touchInput = true
        vkButtonShape = VirtualKeyboard.ROUND_RECT_SHAPE
        vkAlpha = 64
        vkBgColor = 0xD0D0D0
        vkFgColor = 0x000080
        vkBgColorSelected = 0x000080
        vkFgColorSelected = 0xFFFFFF
        vkOutlineColor = 0xFFFFFF
        systemProperties = ContextHolder.getAssetAsString("defaults/system.props")
    }

    companion object {
        const val VERSION = 3
    }
}