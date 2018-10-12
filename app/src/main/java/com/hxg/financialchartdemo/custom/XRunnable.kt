package com.hxg.financialchartdemo.custom

import com.hxg.financialchartdemo.interfeet.HasCancel

//crash-safe base-runnable
abstract class XRunnable : Runnable, HasCancel {
    private val cancelErr = Throwable("Runnable Canceled")
    private var errorListener: ErrorListener? = null

    private var cancelled = false
    private var completed = false

    protected abstract fun Run()

    @Synchronized
    fun IsCanceled(): Boolean {
        return cancelled
    }

    @Synchronized
    fun IsCompleted(): Boolean {
        return completed
    }

    //tries to cancel future runnable but will not work if runnable is already executing
    @Synchronized
    override fun Cancel() {
        cancelled = true
    }

    @Synchronized
    fun Restart() {
        cancelled = false
    }

    @Synchronized
    private fun completed() {
        completed = true
    }

    override fun run() {
        //task cancelled..
        if (IsCanceled()) {
            //callback the error to listener
            errorListener?.OnError(cancelErr)
            return
        }

        var oomed = false

        try {
            Run()
        } catch (e: java.lang.OutOfMemoryError) {
            oomed = true
            e.printStackTrace()
        } catch (e: Throwable) {
            //callback the error to listener
            errorListener?.OnError(e)
            e.printStackTrace()
        }

        //try one more time after oom....
        if (oomed) {
            //task cancelled..
            if (IsCanceled()) {
                //callback the error to listener
                errorListener?.OnError(cancelErr)
                return
            }

            try {
                Run()
            } catch (e: Throwable) {
                //callback the error to listener
                errorListener?.OnError(e)
                e.printStackTrace()
            }
        }

        //runnable has
        completed()
    }

    interface ErrorListener {
        fun OnError(e: Throwable)
    }

    fun SetErrorListener(listener: ErrorListener) {
        errorListener = listener
    }
}
