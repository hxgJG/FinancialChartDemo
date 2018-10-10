package com.example.hxg.itemtouchmove.util

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.TextView
import com.hxg.financialchartdemo.app.App

object XUI {
    //Application resources
    var RESOURCES: Resources? = null
    var DEVICE_HEIGHT: Int = 0
    var DEVICE_WIDTH: Int = 0

    private var XLARGE_CACHED_VALUE = -1

    //current screen orientation landscape/horizontal
    var SCREEN_ORIENTATION: Int = 0

    //boolean true if screen is 2k/qhd resolution
    private val IS_DEVICE_2K: Boolean

    init {
        val r = GetResources()


        val dm = r.displayMetrics

        DEVICE_HEIGHT = dm.heightPixels
        DEVICE_WIDTH = dm.widthPixels

        IS_DEVICE_2K = dm.widthPixels >= 2000 || dm.heightPixels >= 2000

        //make sure height is always larger even for tablets
        if (DEVICE_WIDTH > DEVICE_HEIGHT) {
            val temp = DEVICE_HEIGHT
            DEVICE_HEIGHT = DEVICE_WIDTH
            DEVICE_WIDTH = temp
        }

        SCREEN_ORIENTATION = r.configuration.orientation
    }

    fun GetResources(): Resources {
        if (RESOURCES == null) {
            RESOURCES = App.INSTANCE.CONTEXT.resources
        }

        return RESOURCES!!
    }

    // determine whether the notch screen
    fun GetDisplayCutout(activity: Activity) = if (Build.VERSION.SDK_INT >= 28) activity.window.decorView.rootWindowInsets?.displayCutout else null

    fun getScreenSizeInches(activity: Activity): Double {
        val display = activity.windowManager.defaultDisplay ?: return 0.0
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)

        val dens = displayMetrics.densityDpi

        val x = Math.pow(displayMetrics.widthPixels.toDouble() / dens, 2.0)
        val y = Math.pow(displayMetrics.heightPixels.toDouble() / dens, 2.0)

        return Math.sqrt(x + y)
    }

    //display change
    fun GetDisplayMetrics(a: Activity): DisplayMetrics {
        //we also need to update the display metrics
        val displayMetrics = DisplayMetrics()

        // post event to tell who use this var , this changed

        val display = a.windowManager.defaultDisplay

        display.getMetrics(displayMetrics)

        return displayMetrics
    }

    fun IsXLarge(): Boolean {
        if (XLARGE_CACHED_VALUE == -1) {
            val isXLarge = GetResources().configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
            XLARGE_CACHED_VALUE = if (isXLarge) 1 else 0
        }

        return when (XLARGE_CACHED_VALUE) {
            1 -> true
            2 -> false
            else -> false
        }
    }

    fun DpToPx(dpValue: Float): Int {
        val scale = GetResources().displayMetrics.density
        return Math.round(dpValue * scale)
    }

    fun DpToPxFloat(dpValue: Float): Float {
        val scale = GetResources().displayMetrics.density
        return dpValue * scale + 0.5f
    }

    fun PxToDp(px: Float): Int {
        val metrics = GetResources().displayMetrics

        return Math.round(px / (metrics.densityDpi / 160f))
    }

    fun SpToPx(spValue: Float): Int {
        val scale = GetResources().displayMetrics.scaledDensity
        return (spValue * scale + 0.5f).toInt()
    }

    fun SpToPxFloat(spValue: Float): Float {
        val scale = GetResources().displayMetrics.scaledDensity
        return (spValue * scale + 0.5f)
    }

    fun HasEllipse(v: TextView): Boolean {
        val l = v.layout
        if (l != null) {
            val lines = l.lineCount
            if (lines > 0) {
                if (l.getEllipsisCount(lines - 1) > 0) {
                    XLog.d("hxg", "Text is ellipsized")
                    return true
                }
            }
        }

        return false
    }

    fun GetScreenSize(): Point {
        val wm = App.INSTANCE.CONTEXT.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size
    }

    fun IsSmallScreen(): Boolean {
        val screenSize = GetScreenSize()

        //anything at or below 1280 x <1280 resolution is considered non-HD, small screen
        return screenSize.x <= 1280 && screenSize.y <= 1280
    }
}
