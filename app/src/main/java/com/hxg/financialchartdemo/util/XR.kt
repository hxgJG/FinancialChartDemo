package com.hxg.financialchartdemo.util

import Use
import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.support.annotation.ArrayRes
import android.support.annotation.AttrRes
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.support.v7.content.res.AppCompatResources
import android.util.AttributeSet
import android.util.TypedValue
import com.example.hxg.itemtouchmove.util.XUI
import com.hxg.financialchartdemo.R
import com.hxg.financialchartdemo.app.App

//android resource helper
object XR {
    private val XMLNS = "http://schemas.android.com/apk/res/android"

    fun D(@DrawableRes resid: Int): Drawable {
        return AppCompatResources.getDrawable(App.INSTANCE.CONTEXT, resid)!!
    }

    fun DmAsPxFloat(@DimenRes resid: Int): Float {
        //XLog.e("xr", "new size:" + size);
        return L.GetResources().getDimension(resid)
    }

    fun DmAsPx(@DimenRes resid: Int, min: Int = 0, max: Int = 0): Int {
        val size = L.GetResources().getDimension(resid).toInt()

        if (min > 0 && size < min) {
            //XLog.e("xr", "new min size:" + minimumSize);
            return min
        }

        if (max > 0 && size > max) {
            return max
        }

        //XLog.e("xr", "new size:" + size);
        return size
    }

    fun UserIconSmall(@DimenRes resid: Int): Int {
        return DmAsPx(resid, 0, 120)
    }

    fun UserIconLarge(@DimenRes resid: Int): Int {
        return DmAsPx(resid, 0, 192) //xxx based on 46dp
    }

    /**
     * @param themedContext a context with a theme, typically the current Activity (i.e. 'this')
     * *
     * @param attr          the attribute we define using like R.attr.someAttribute
     * *
     * @return a drawable object
     */

    fun GetDrawable(@AttrRes attr: Int, themedContext: Context? = null): Drawable {
        var c = themedContext
        if (c == null) {
            c = XContext.GetAliveActivityOrAppContext()
        }

        // Create an array of the attributes we want to resolve using values from a theme
        val attrs = intArrayOf(attr)

        //Obtain the styled attributes. 'themedContext' is a context with a theme, typically the current Activity (i.e. 'this')
        return c.obtainStyledAttributes(attrs).Use { a ->
            return@Use a.getDrawable(0)
        }
    }

    fun GetDrawableId(@AttrRes resId: Int, themedContext: Context? = null): Int {
        var c = themedContext
        if (c == null) {
            c = XContext.GetAliveActivityOrAppContext()
        }

        val typedValue = TypedValue()
        c.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.resourceId
    }

    fun GetDrawable(@DrawableRes resId: Int): Drawable {
        return XR.D(resId)
    }

    fun GetStringArray(@ArrayRes attr: Int): Array<String> {
        return L.GetResources().getStringArray(attr)
    }

    fun GetIntArray(@ArrayRes attr: Int): IntArray {
        return L.GetResources().getIntArray(attr)
    }

    fun GetDimension(context: Context, @AttrRes attr: Int): Int {
        val textSizeAttr = intArrayOf(attr)
        val typedValue = TypedValue()
        return context.obtainStyledAttributes(typedValue.data, textSizeAttr).Use { a ->
            return@Use a.getDimensionPixelSize(0, -1)
        }
    }

    fun GetAttributeValue(attrs: AttributeSet, name: String): String? {
        return attrs.getAttributeValue(XMLNS, name)
    }

    fun GetAttributeColor(context: Context, attrs: AttributeSet, name: String, defaultValue: Int): Int {
        try {
            val value = attrs.getAttributeValue(XMLNS, name)

            if (value.isNullOrEmpty()) {
                return defaultValue
            }

            return when (value[0]) {
                '?' -> {
                    val attrValue = Integer.parseInt(value.substring(1, value.length))
                    val typedValue = TypedValue()
                    context.theme.resolveAttribute(attrValue, typedValue, true)
                    typedValue.resourceId
                }

                '@' -> Integer.parseInt(value.substring(1, value.length))

                else -> defaultValue
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            return defaultValue
        }
    }

    /**
     * the 820dp screen height in portrait and 520dp screen height in landscape is according our test data from our test pad and phones.
     * following is our test data:
     *
     * the screen dp height of in portrait/landscape: and the screen pixels height in portrait/landscape
     * <pre>samsung 4.1.2 : 508/294 800/480</pre>
     * <pre>coolpad 6.0.1 : 707/387 1920/1080</pre>
     * <pre>samsung 4.2.4 : 614/334 960/540</pre>
     * <pre>amazon 5.1.1 pad: 952/528 976/552</pre>
     * <pre>chiwei 4.4.4 pad: 1206/726 1848/1128</pre>
     * <pre>google 6.0 pad: 888/528 1824/1104</pre>
     * <pre>xiaomi 6.0.1 xxh: 678/372 1920/1080</pre>
     * <pre>samsung 6.0 xxxh: 616/336 2560/1440</pre>
     */
    fun IsBigScreen(context: Context): Boolean {
        val screenHeight = context.resources.configuration.screenHeightDp
        return if (XUI.SCREEN_ORIENTATION == Configuration.ORIENTATION_PORTRAIT) {
            screenHeight > 880
        } else {
            screenHeight > 520
        }
    }
}
