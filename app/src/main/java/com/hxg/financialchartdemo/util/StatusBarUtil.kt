package com.hxg.financialchartdemo.util

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.hxg.itemtouchmove.util.XUI

object StatusBarUtil {
    val DEFAULT_STATUS_BAR_ALPHA = 50

    /**
     * 使状态栏透明
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun transparentStatusBar(activity: Activity) {
        activity.window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                statusBarColor = Color.TRANSPARENT
            } else {
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun transparentStatusBarAndNavigation(activity: Activity) {
        activity.window.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                statusBarColor = Color.TRANSPARENT
            } else {
                addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }

    /**
     * 为头部是 ImageView 的界面设置状态栏透明

     * @param activity       需要设置的activity
     * *
     * @param needOffsetView 需要向下偏移的 View
     */
    fun SetTransParentForImageView(activity: Activity, needOffsetView: View) {
        try {
            activity.window.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBarColor = Color.TRANSPARENT
                    decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                } else {
                    setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }

            val layoutParams = needOffsetView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, getHeight(activity), 0, 0)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    /**
     * 获取状态栏高度

     * @param context context
     * *
     * @return 状态栏高度
     */
    fun getHeight(context: Context): Int {
        return try {
            context.resources.getDimensionPixelSize(context.resources.getIdentifier("status_bar_height", "dimen", "android"))
        } catch (e: Throwable) {
            XUI.DpToPx(20f)
        }
    }
}
