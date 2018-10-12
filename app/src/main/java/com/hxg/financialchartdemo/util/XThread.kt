package com.hxg.financialchartdemo.util

import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object XThread {
    //this is the UI/MAIN looper for this android app..
    val MAIN_LOOPER = Looper.getMainLooper()!!
    private val NEXT_THREAD_COUNT = AtomicInteger(0)
    var SHARED = ScheduledThreadPoolExecutor(4, threadFactory("Gopher"))

    init {
        //do not use keepalive for our pool...
        AppEntersForeground()
    }

    //exception free sleep..
    fun Sleep(sleepMs: Long) {
        SystemClock.sleep(sleepMs)
    }

    //creating a new Thread may fail due to fragmented memory, we need to try many times before giving up
    fun NewHandlerThread(name: String): HandlerThread? {
        //only try 4 times
        for (i in 0..3) {
            try {
                return HandlerThread(name)
            } catch (e: Throwable) {
                e.printStackTrace()

                if (i != 0) {
                    //sleep for 100ms and try again
                    XThread.Sleep(100L)
                }
            }
        }

        return null
    }

    fun IsUIThread(): Boolean {
        //gets static/app instance of realm...must be on UI thread...
        return MAIN_LOOPER == Looper.myLooper()
    }

    fun AppEntersForeground() {
        //FIX ME..doesn't appear to have any effect
        //when in foreground...kill idle threads after 5 minutes of idle
        if (SHARED.getKeepAliveTime(TimeUnit.SECONDS) != 60L) {
            SHARED.setKeepAliveTime(60L, TimeUnit.SECONDS)
        }
    }

    private fun threadFactory(name: String): ThreadFactory {
        return ThreadFactory { runnable ->
            val threadId = name + "#" + NEXT_THREAD_COUNT.getAndAdd(1)
            try {
                return@ThreadFactory Thread(runnable, threadId)
            } catch (e: OutOfMemoryError) {
                System.gc()
                System.runFinalization()
                return@ThreadFactory Thread(runnable, threadId)
            }

            //CRITICAL some android devices might give only 1 core to background threads with extremely low priority if set to background priority
            //XThread.MoveToBackground();
        }
    }
}