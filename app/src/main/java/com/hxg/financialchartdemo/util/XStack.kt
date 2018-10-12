package com.hxg.financialchartdemo.util

import com.hxg.financialchartdemo.MainActivity
import com.hxg.financialchartdemo.bean.XActivityInfo
import com.hxg.financialchartdemo.ui.BaseActivity
import java.lang.ref.WeakReference
import java.util.*

//a weak referenced linked list of ui history stack so we can dynamically
//clear memory when stack size is larger than max or when we need more memory
object XStack {
    private var welcomeReference: WeakReference<MainActivity>? = null
    private val history = XStackModel()
    private val leakDetector = ArrayList<WeakReference<BaseActivity>>()

    //get last active activity...
    @Synchronized
    fun GetLastActiveActivity(): BaseActivity? {
        GCWeak()

        val size = history.Data.size

        if (size == 0) {
            return null
        }

        for (i in size - 1 downTo 0) {
            val a = history.Data[i].a.get()
            if (a != null && !a.IsDestroyed()) {
                return a
            }
        }

        return null
    }

    //cleanup all weak nulls
    @Synchronized
    private fun GCWeak() {
        //cleanup weak...
        var i = 0
        while (i < history.Data.size) {
            val info = history.Data[i]
            val a = info.a.get()
            if (a == null && info.intent == null) {
                history.Data.removeAt(i)
                i--
            }
            i++
        }

        GCLeakDetector()
    }

    //for leak..we only test for null...not a.IsDestroyed...
    @Synchronized
    private fun GCLeakDetector(): Int {
        var i = 0
        while (leakDetector.size > 0 && i < leakDetector.size) {
            val a = leakDetector[i].get()
            if (a == null) {
                leakDetector.removeAt(i)
                i--
            }
            i++
        }

        //do we have leaks?...depends on how/when you call this method
        return leakDetector.size
    }
}

data class XStackModel(val Data: ArrayList<XActivityInfo> = ArrayList())