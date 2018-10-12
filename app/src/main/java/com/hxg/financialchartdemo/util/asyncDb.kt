package com.hxg.financialchartdemo.util

import android.os.Handler
import android.os.HandlerThread

//dedicated thread for db operations
val DB_THREAD by lazy {
    val t = HandlerThread("DbThread")
    t.start()
    t
}

//db Looper
val DB_LOOPER = DB_THREAD.looper!!

//db handler
val DB_HANDLER = Handler(DB_LOOPER)