/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hxg.financialchartdemo.custom

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.design.widget.Snackbar
import java.lang.ref.WeakReference

/**
 * Manages [Snackbar]s.
 */
internal class SnackbarManager private constructor() {
    private val mLock: Any
    private val mHandler: Handler

    private var mCurrentSnackbar: SnackbarRecord? = null
    private var mNextSnackbar: SnackbarRecord? = null

    init {
        mLock = Any()
        mHandler = Handler(Looper.getMainLooper(), Handler.Callback { message ->
            when (message.what) {
                MSG_TIMEOUT -> {
                    handleTimeout(message.obj as SnackbarRecord)
                    return@Callback true
                }
            }
            false
        })
    }

    internal interface Callback {
        fun show()
        fun dismiss(event: Int)
    }

    fun show(duration: Int, callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentSnackbar!!.duration = duration

                // If this is the Snackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar)
                scheduleTimeoutLocked(mCurrentSnackbar!!)
                return
            } else if (isNextSnackbarLocked(callback)) {
                // We'll just update the duration
                mNextSnackbar!!.duration = duration
            } else {
                // Else, we need to create a new record and queue it
                mNextSnackbar = SnackbarRecord(duration, callback)
            }

            if (mCurrentSnackbar != null && cancelSnackbarLocked(mCurrentSnackbar!!,
                            XSnackbar.Callback.DISMISS_EVENT_CONSECUTIVE)) {
                // If we currently have a Snackbar, try and cancel it and wait in line
                return
            } else {
                // Clear out the current snackbar
                mCurrentSnackbar = null
                // Otherwise, just show it now
                showNextSnackbarLocked()
            }
        }
    }

    fun dismiss(callback: Callback, event: Int) {
        synchronized(mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                cancelSnackbarLocked(mCurrentSnackbar!!, event)
            } else if (isNextSnackbarLocked(callback)) {
                cancelSnackbarLocked(mNextSnackbar!!, event)
            }

            return@synchronized
        }
    }

    /**
     * Should be called when a Snackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    fun onDismissed(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                // If the callback is from a Snackbar currently show, remove it and show a new one
                mCurrentSnackbar = null
                if (mNextSnackbar != null) {
                    showNextSnackbarLocked()
                }
            }
        }
    }

    /**
     * Should be called when a Snackbar is being shown. This is after any entrance animation has
     * finished.
     */
    fun onShown(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbarLocked(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar!!)
            }
        }
    }

    fun pauseTimeout(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbarLocked(callback) && !mCurrentSnackbar!!.paused) {
                mCurrentSnackbar!!.paused = true
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar)
            }
        }
    }

    fun restoreTimeoutIfPaused(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbarLocked(callback) && mCurrentSnackbar!!.paused) {
                mCurrentSnackbar!!.paused = false
                scheduleTimeoutLocked(mCurrentSnackbar!!)
            }
        }
    }

    fun isCurrent(callback: Callback): Boolean {
        synchronized(mLock) {
            return isCurrentSnackbarLocked(callback)
        }
    }

    fun isCurrentOrNext(callback: Callback): Boolean {
        synchronized(mLock) {
            return isCurrentSnackbarLocked(callback) || isNextSnackbarLocked(callback)
        }
    }

    private class SnackbarRecord internal constructor(internal var duration: Int, callback: Callback) {
        internal val callback: WeakReference<Callback>
        internal var paused: Boolean = false

        init {
            this.callback = WeakReference(callback)
        }

        internal fun isSnackbar(callback: Callback?): Boolean {
            return callback != null && this.callback.get() === callback
        }
    }

    private fun showNextSnackbarLocked() {
        if (mNextSnackbar != null) {
            mCurrentSnackbar = mNextSnackbar
            mNextSnackbar = null

            val callback = mCurrentSnackbar!!.callback.get()
            if (callback != null) {
                callback.show()
            } else {
                // The callback doesn't exist any more, clear out the Snackbar
                mCurrentSnackbar = null
            }
        }
    }

    private fun cancelSnackbarLocked(record: SnackbarRecord, event: Int): Boolean {
        val callback = record.callback.get()
        if (callback != null) {
            // Make sure we remove any timeouts for the SnackbarRecord
            mHandler.removeCallbacksAndMessages(record)
            callback.dismiss(event)
            return true
        }

        return false
    }

    private fun isCurrentSnackbarLocked(callback: Callback): Boolean {
        return mCurrentSnackbar != null && mCurrentSnackbar!!.isSnackbar(callback)
    }

    private fun isNextSnackbarLocked(callback: Callback): Boolean {
        return mNextSnackbar != null && mNextSnackbar!!.isSnackbar(callback)
    }

    private fun scheduleTimeoutLocked(r: SnackbarRecord) {
        if (r.duration == Snackbar.LENGTH_INDEFINITE) {
            // If we're set to indefinite, we don't want to set a timeout
            return
        }

        var durationMs = LONG_DURATION_MS
        if (r.duration > 0) {
            durationMs = r.duration
        } else if (r.duration == Snackbar.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS
        }
        mHandler.removeCallbacksAndMessages(r)
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs.toLong())
    }

    private fun handleTimeout(record: SnackbarRecord) {
        synchronized(mLock) {
            if (mCurrentSnackbar === record || mNextSnackbar === record) {
                cancelSnackbarLocked(record, Snackbar.Callback.DISMISS_EVENT_TIMEOUT)
            }
        }
    }

    companion object {

        val MSG_TIMEOUT = 0

        private val SHORT_DURATION_MS = 1500
        private val LONG_DURATION_MS = 2750

        private var sSnackbarManager: SnackbarManager? = null

        val instance: SnackbarManager
            get() {
                if (sSnackbarManager == null) {
                    sSnackbarManager = SnackbarManager()
                }

                return sSnackbarManager!!
            }
    }
}
