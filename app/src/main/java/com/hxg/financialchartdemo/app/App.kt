package com.hxg.financialchartdemo.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

@SuppressLint("StaticFieldLeak")
class App : Application() {

    @Volatile
    lateinit var CONTEXT: Context

    override fun attachBaseContext(ctx: Context) {
        try {
            CONTEXT = ctx
            INSTANCE = this
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            super.attachBaseContext(CONTEXT)
        }
    }

    companion object {
        @Volatile
        lateinit var INSTANCE: App
    }
}