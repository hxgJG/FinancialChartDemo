package com.hxg.financialchartdemo.ui

import Safe
import android.os.Bundle
import android.util.Log
import com.example.hxg.itemtouchmove.util.XLog
import com.hxg.financialchartdemo.R
import com.hxg.financialchartdemo.bean.FundMode
import com.hxg.financialchartdemo.bean.OriginFundMode
import com.hxg.financialchartdemo.custom.FundView
import com.hxg.financialchartdemo.http.FundApi
import com.hxg.financialchartdemo.util.XGson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import isNullOrEmpty
import java.util.*
import java.util.concurrent.TimeUnit

class FundActivity : BaseActivity() {
    private var mFundView: FundView? = null

    override fun onDestroy() {
        super.onDestroy()
        mFundView = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund)
        initView()
        loadData()
    }

    private fun initView() {
        mFundView = findViewById<FundView>(R.id.af_fv_fundview)?.apply {
            mBrokenPaint.color = resources.getColor(R.color.colorAccent)//设置折现颜色
            mInnerXPaint.strokeWidth = 1f//设置内部x轴虚线的宽度,px
            mBrokenPaint.strokeWidth = 1f
            mLoadingText = "正在加载，马上就来..."
            setBasePadding(50f, 140f, 40f, 30f)
        }
    }

    private fun loadData() {
        Safe {
            val subscribe = Observable.timer(1000, TimeUnit.MILLISECONDS)
                    .map {
                        val originalFundData = FundApi.getOriginalFundData(mContext!!)
                        if (originalFundData == null) {
                            XLog.e(TAG, "empty data!!!")
                            return@map null
                        }
                        val originFunModes = XGson.fromJson2Object(originalFundData, Array<OriginFundMode>::class.java).asList()
                        //开始适配图表数据
                        adapterData(originFunModes)
                    }
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (isNullOrEmpty(it)) {
                            Log.e(TAG, "loadData: data is empty!")
                        } else {
                            mFundView?.setDataList(it!!)
                        }
                    }
            unSubscription(subscribe)
        }
    }

    private fun adapterData(originFundModeList: List<OriginFundMode>): List<FundMode> {
        val fundModeList = ArrayList<FundMode>()//适配后的数据
        for (originFundMode in originFundModeList) {
            val fundMode = FundMode(originFundMode.timestamp * 1000, originFundMode.actual)
            fundModeList.add(fundMode)
            XLog.e(TAG, "adapterData: 适配之前：" + originFundMode.actual + "----->>" + fundMode.dataY)
        }
        return fundModeList
    }
}
