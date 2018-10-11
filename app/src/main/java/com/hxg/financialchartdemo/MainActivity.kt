package com.hxg.financialchartdemo

import android.os.Bundle
import android.widget.TextView
import com.hxg.financialchartdemo.ui.BaseActivity
import com.hxg.financialchartdemo.ui.FundActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.tv_btn).apply {
            setOnClickListener { go(FundActivity::class.java) }
        }
    }
}
