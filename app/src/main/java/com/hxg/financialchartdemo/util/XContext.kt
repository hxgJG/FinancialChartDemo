package com.hxg.financialchartdemo.util

import android.content.Context
import android.view.View
import com.hxg.financialchartdemo.app.App
import com.hxg.financialchartdemo.interfeet.HasStates
import java.lang.ref.WeakReference

object XContext {
    fun IsDestroyed(c_in: Any?): Boolean {
        var c: Any = c_in ?: return true

        if (c is View) {
            c = c.context
        }

        if (c is HasStates) {
            return c.IsDestroyed()
        }

        return false
    }

    fun IsDestroyed(c: WeakReference<*>?): Boolean {
        c ?: return true

        return IsDestroyed(c.get())
    }

    fun IsActive(c: Any?): Boolean {
        c ?: return false

        if (c is HasStates) {
            return c.IsActive()
        } else {
            throw Exception("you are calling a state check but passing a non-stateful object" + c.javaClass.simpleName)
        }

        return true
    }

    fun IsActive(c: WeakReference<*>?): Boolean {
        c ?: return false

        return IsActive(c.get())
    }

    fun IsUserVisible(c: Any?): Boolean {
        c ?: return false

        if (c is HasStates) {
            return c.IsUserVisible()
        } else {
            throw Exception("you are calling a state check but passing a non-stateful object" + c.javaClass.simpleName)
        }

        return true
    }

    fun IsUserVisible(c: WeakReference<*>?): Boolean {
        c ?: return false

        return IsUserVisible(c.get())
    }

    //useful helper for getting the best context to use
    fun GetAliveActivityOrAppContext(): Context {
        return XStack.GetLastActiveActivity() ?: App.INSTANCE.CONTEXT
    }
}
