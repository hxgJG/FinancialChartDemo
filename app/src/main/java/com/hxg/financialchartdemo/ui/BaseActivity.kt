package com.hxg.financialchartdemo.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.example.hxg.itemtouchmove.util.XLog
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus

open class BaseActivity : AppCompatActivity() {

    protected var mContext: Context? = null
    protected var TAG: String? = null
    protected lateinit var mActivity: Activity
    /**
     * 对系统系统的toast进行简单封装，方便使用
     */
    private var toast: Toast? = null

    private var compositeDisposable: CompositeDisposable? = null

    var isBindEventBusHere = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = this
        mContext = this
        TAG = this.javaClass.simpleName
        // getBundleExtras
        val extras = intent.extras
        if (null != extras) {
            getBundleExtras(extras)
        }
        if (isBindEventBusHere) {
            EventBus.getDefault().register(mActivity)
        }
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    protected fun getBundleExtras(extras: Bundle) {

    }

    /**
     * 添加disposable
     *
     * @param disposable
     */
    fun unSubscription(disposable: Disposable) {
        if (compositeDisposable == null) {
            synchronized(CompositeDisposable::class.java) {
                if (compositeDisposable == null) {
                    compositeDisposable = CompositeDisposable()
                }
            }
        }
        compositeDisposable!!.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBindEventBusHere) {
            EventBus.getDefault().unregister(mActivity)
        }
        if (compositeDisposable != null) {
            XLog.d(TAG!!, "base activity dispose")
            compositeDisposable!!.clear()
        }
    }

    /**
     * startActivity
     *
     * @param clazz target Activity
     */
    fun go(clazz: Class<out Activity>) {
        _goActivity(clazz, null, NON_CODE, false)
    }

    /**
     * startActivity with bundle
     *
     * @param clazz  target Activity
     * @param bundle
     */
    fun go(clazz: Class<out Activity>, bundle: Bundle) {
        _goActivity(clazz, bundle, NON_CODE, false)
    }

    /**
     * startActivity then finish this
     *
     * @param clazz target Activity
     */
    fun goAndFinish(clazz: Class<out Activity>) {
        _goActivity(clazz, null, NON_CODE, true)
    }

    /**
     * startActivity with bundle and then finish this
     *
     * @param clazz  target Activity
     * @param bundle bundle extra
     */
    fun goAndFinish(clazz: Class<out Activity>, bundle: Bundle) {
        _goActivity(clazz, bundle, NON_CODE, true)
    }

    /**
     * startActivityForResult
     *
     * @param clazz
     * @param requestCode
     */
    protected fun goForResult(clazz: Class<out Activity>, requestCode: Int) {
        _goActivity(clazz, null, requestCode, false)
    }

    /**
     * startActivityForResult with bundle
     *
     * @param clazz
     * @param bundle
     * @param requestCode
     */
    protected fun goForResult(clazz: Class<out Activity>, bundle: Bundle, requestCode: Int) {
        _goActivity(clazz, bundle, requestCode, false)
    }

    /**
     * startActivityForResult then finish this
     *
     * @param clazz
     * @param requestCode
     */
    protected fun goForResultAndFinish(clazz: Class<out Activity>, requestCode: Int) {
        _goActivity(clazz, null, requestCode, true)
    }

    /**
     * startActivityForResult with bundle and then finish this
     *
     * @param clazz
     * @param bundle
     * @param requestCode
     */
    protected fun goForResultAndFinish(clazz: Class<out Activity>, bundle: Bundle, requestCode: Int) {
        _goActivity(clazz, bundle, requestCode, true)
    }


    //可以立刻刷新Toast。推荐使用该方式。
    fun showToast(str: String) {
        if (toast == null) {
            toast = Toast.makeText(applicationContext, str, Toast.LENGTH_SHORT)
        } else {
            toast!!.setText(str)
        }
        toast!!.show()
    }

    private fun _goActivity(clazz: Class<out Activity>?, bundle: Bundle?, requestCode: Int, finish: Boolean) {
        if (null == clazz) {
            throw IllegalArgumentException("you must pass a target activity where to go.")
        }
        val intent = Intent(this, clazz)
        if (null != bundle) {
            intent.putExtras(bundle)
        }
        if (requestCode > NON_CODE) {
            startActivityForResult(intent, requestCode)
        } else {
            startActivity(intent)
        }
        if (finish) {
            finish()
        }
    }

    companion object {
        /**
         * Activity 跳转
         *
         * @param clazz  目标activity
         * @param bundle 传递参数
         * @param finish 是否结束当前activity
         */
        val NON_CODE = -1
    }
}
