package com.hxg.financialchartdemo.util

import Safe
import SafeNoLog
import android.os.Handler
import android.support.annotation.CallSuper
import com.example.hxg.itemtouchmove.util.XLog
import com.hxg.financialchartdemo.interfeet.HasCancel
import com.hxg.financialchartdemo.interfeet.HasRootJob
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.HandlerContext
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.selects.select
import kotlin.coroutines.experimental.CoroutineContext

open class CancellableJob(val j: Job) : Job by j, HasCancel {
    @CallSuper
    override fun Cancel() {
        this.Cancel(null)
    }

}

//a Job that contains a Channel<T>
class ChannelJob<C>(val channel: Channel<C>, j: Job) : CancellableJob(j)

//a Deferred that contains a Channel<T>
class ChannelDeferred<D, C>(val channel: Channel<C>, d: Deferred<D>) : Deferred<D> by d

//quiet cancel
fun Job.Cancel(e: Throwable? = null) {
    if (XThread.IsUIThread()) {
        Go(BG) {
            mCancel(e)
        }
    } else {
        mCancel(e)
    }
}

private fun Job.mCancel(e: Throwable? = null) {
    SafeNoLog {
        if (isActive && !isCancelled && !isCompleted) {
            cancel(e)
        }
    }

    (this as? ChannelJob<*>)?.run {
        //XLog.e("job", "is xjob: " + this)
        channel.Close(EXCEPTION_CANCELLED)
        return
    }

    (this as? ChannelDeferred<*, *>)?.run {
        //XLog.e("job", "is xdeferred: " + this)
        channel.Close(EXCEPTION_CANCELLED)
        return
    }
}

fun Job.CancelChildren(e: Throwable? = null) {
    //XLog.e("job", " in job cancel: " + this)

    SafeNoLog {
        if (isActive && !isCancelled && !isCompleted) {
            cancelChildren(e)
        }
    }

    (this as? ChannelJob<*>)?.run {
        //XLog.e("job", "is xjob: " + this)
        channel.Close(EXCEPTION_CANCELLED)
        return
    }

    (this as? ChannelDeferred<*, *>)?.run {
        //XLog.e("job", "is xdeferred: " + this)
        channel.Close(EXCEPTION_CANCELLED)
        return
    }
}

fun Deferred<*>.Cancel(e: Throwable? = null) {
    //XLog.e("job", " in deferred cancel: " + this)
    (this as Job).Cancel(e)
}


//quiet close
fun Channel<*>.Close(e: Throwable? = null) {
    SafeNoLog { close(e) }
}

/*
WARNING: Go() and GoBlock() async apis are only meant for non-socket based i/o operations since
sockets can hang/block forever. It is ok to use them for File i/o since they are deterministic
when it comes to execution time.
 */

//use this to catch all uncaught exceptions in coroutines
private val unhandledExceptionHandler = CoroutineExceptionHandler { _, e ->
    e.printStackTrace()
}

//db context for async operations
val DB = HandlerContext(DB_HANDLER, "DB") + unhandledExceptionHandler
val UI = kotlinx.coroutines.experimental.android.UI + unhandledExceptionHandler
val BG = CommonPool + unhandledExceptionHandler

//this handler runs on the Main/UI thread
var UI_HANDLER = Handler(XThread.MAIN_LOOPER)

val EXCEPTION_CANCELLED = Exception("cancelled")

typealias AsyncBlock = suspend CoroutineScope.() -> Unit
typealias AsyncBlockDeferred<T> = suspend CoroutineScope.() -> T

//For calling from non-coroutine code
fun Go(execCtx: CoroutineContext, b: AsyncBlock): CancellableJob = Go(execCtx, 0, null, b)

////For callers within coroutine context
//suspend fun <R> Go(execCtx: CoroutineContext, b: AsyncBlockDeferred<R>): Deferred<R> = Go(execCtx, 0, null, b)
//
////delay is time in milliseconds
//suspend fun <R> Go(execCtx: CoroutineContext, delay: Long = 0, parent: Job? = null, b: AsyncBlockDeferred<R>): Deferred<R> {
//    return if (delay <= 0) {
//        async(execCtx, parent = parent) {
//            b()
//        }
//    } else {
//        val ch = Channel<Boolean>(0)
//        ChannelDeferred(ch, async(execCtx, parent = parent) {
//            delay(ch, delay)
//
//            //due to delay, we need to check to see if we are cancelled
//            if (!isActive) {
//                throw EXCEPTION_CANCELLED
//            }
//
//            b()
//        })
//    }
//}

//delay is time in milliseconds
fun Go(execCtx: CoroutineContext, delay: Long = 0, parent: Job? = null, b: AsyncBlock): CancellableJob {
    return if (delay <= 0) {
        CancellableJob(async(execCtx, parent = parent) {
            if (isActive) {
                b()
            }
        })
    } else {
        val ch = Channel<Boolean>(0)
        ChannelJob(ch, async(execCtx, parent = parent) {
            if (isActive && !ch.isClosedForReceive) {
                delay(ch, delay)

                if (isActive && !ch.isClosedForReceive) {
                    b()
                }
            }
        })
    }
}

//HasRootJob helper
fun Go(execCtx: CoroutineContext, delay: Long = 0, parent: HasRootJob, b: AsyncBlock): CancellableJob {
    return Go(execCtx, delay, parent.rootJob, b)
}

private suspend fun delay(ch: Channel<*>, delay: Long) {
    //FIX samsung android 5.0 devices have issue with kotlin delay code
    //if we get an error here, just do nothing and run execution without delay, better than crash
    Safe {
        select<Unit> {
            onTimeout(delay) {
                XLog.e("job", "normal delay timedout:$delay")
            }
            ch.onReceiveOrNull {
                XLog.e("job", "ch closed:$it")
            }
        }
    }
}

//NOTE this BLOCKS the calling thread so use very very carefully when calling from UI thread
fun GoBlock(execCtx: CoroutineContext, b: AsyncBlock) {
    runBlocking(execCtx) {
        Safe {
            if (isActive) {
                b()
            }
        }
    }
}