package com.hxg.financialchartdemo.util

import android.content.Context
import android.support.annotation.ArrayRes
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.util.TypedValue

object XColor {

    fun Get(@ColorRes resId: Int): Int {
        return try {
            L.GetResources().getColor(resId)
        } catch (e: Throwable) {
            e.printStackTrace()
            0
        }
    }

    fun GetArrayColor(@ArrayRes resId: Int, index: Int): Int {
        return try {
            L.GetResources().getIntArray(resId)[index]
        } catch (e: Throwable) {
            e.printStackTrace()
            0
        }
    }

    fun GetAttrColor(@AttrRes resId: Int, themedContext: Context? = null): Int {
        var c = themedContext
        if (c == null) {
            c = XContext.GetAliveActivityOrAppContext()
        }

        val typedValue = TypedValue()
        c.theme.resolveAttribute(resId, typedValue, true)
        return typedValue.data
    }

    /**
     * Set the opacity alpha value for color
     * And the previous alpha will been cleared.

     * @param color
     * *
     * @param alpha Alpha component [0..255] of the color
     * *
     * @return the color with the alpha value
     */
    fun SetAlpha(color: Int, alpha: Int): Int {
        return 0x00ffffff and color or (alpha shl 24)
    }
}
