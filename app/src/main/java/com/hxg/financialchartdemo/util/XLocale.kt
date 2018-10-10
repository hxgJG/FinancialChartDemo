package com.example.hxg.itemtouchmove.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.support.v4.text.TextUtilsCompat
import android.support.v4.view.ViewCompat
import android.text.TextUtils
import android.view.View
import java.util.*

object XLocale {
    val ENGLISH_LOCALE = Locale("en")

    //default to true
    var LAYOUT_LTR = -1

    //returns if layout direction of the current locale language is Right to Left
    fun IsLTR(): Boolean {
        //check for cache
        if (LAYOUT_LTR > -1) {
            return LAYOUT_LTR == 1
        }

        //get current layout direction
        LAYOUT_LTR = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR) {
                1
            } else {
                0
            }
        } else {
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                1
            } else {
                0
            }
        }

        return LAYOUT_LTR == 1
    }

    fun IsRTL(): Boolean {
        return !IsLTR()
    }


    @Suppress("DEPRECATION")
    fun SetLocale(ctx: Context, newLocale: Locale): Context {
        try {
            Locale.setDefault(newLocale)

            val res = ctx.resources
            val conf = res.configuration

            conf.locale = newLocale

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                conf.setLayoutDirection(newLocale)
            }

            res.updateConfiguration(conf, res.displayMetrics)

            return ctx
        } finally {
//            XLocale.reloadLocaleSpecificData()
        }
    }


    //since api 24, conf.locale is deprecated..use this helper for compat...
    fun GetPrimaryLocale(conf: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= 24) conf.locales.get(0) else conf.locale
    }

    @Suppress("DEPRECATION")
    fun GetLocale(conf: Configuration): Locale {
        //the configuration locale language
        return if (Build.VERSION.SDK_INT >= 24) {
            conf.locales.get(0)
        } else {
            conf.locale
        }
    }
}
