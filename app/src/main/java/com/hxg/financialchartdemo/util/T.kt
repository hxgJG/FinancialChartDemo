package com.hxg.financialchartdemo.util

import android.support.annotation.StringRes
import android.widget.Toast
import com.hxg.financialchartdemo.BuildConfig
import com.hxg.financialchartdemo.app.App

object T {
    private var toast: Toast? = null

    fun Toast(str: String, showLong: Boolean = false) {
        if (toast == null) {
            toast = Toast.makeText(App.INSTANCE, str, if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(str)
        }

        showToast()
    }

    private fun showToast(){
        toast?.let {
            if (XThread.IsUIThread()) {
                it.show()
            } else {
                Go(UI) {
                    it.show()
                }
            }
        }
    }

    fun Toast(@StringRes str: Int) {
        Toast(L.S(str))
    }

    fun DebugToast(str: String) {
        if (BuildConfig.DEBUG) {
            Toast(str)
        }
    }
}