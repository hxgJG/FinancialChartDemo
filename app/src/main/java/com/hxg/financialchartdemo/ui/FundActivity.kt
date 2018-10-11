package com.hxg.financialchartdemo.ui

import android.os.Bundle
import com.hxg.financialchartdemo.R
import com.hxg.financialchartdemo.bean.OriginFundMode
import com.hxg.financialchartdemo.custom.FundView
import java.util.*

class FundActivity : BaseActivity() {
    private var mFundView: FundView? = null
    private lateinit var mOriginFundModeList: List<OriginFundMode>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fund)
        initView()
        initData()
        loadData()
    }

    private fun initView() {
        mFundView = findViewById<FundView>(R.id.af_fv_fundview)?.apply {
            /**
             * 定制,所有的画笔以及其它属性都已经暴露出来，有了更加大的定时灵活性。更多参数可以直接查看源码...
             */
            //常规set、get...
            getBrokenPaint().color = resources.getColor(R.color.colorAccent)//设置折现颜色
            getInnerXPaint().strokeWidth = 1f//设置内部x轴虚线的宽度,px
            getBrokenPaint().strokeWidth = 1f
            //链式调用
            setBasePaddingTop(140f)
                    .setBasePaddingLeft(50f)
                    .setBasePaddingRight(40f)
                    .setBasePaddingBottom(30f)
                    .setLoadingText("正在加载，马上就来...")
        }
    }

    private fun initData() {
        mOriginFundModeList = ArrayList<OriginFundMode>()

    }

    private fun loadData() {
    }

}
