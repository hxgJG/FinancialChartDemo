package com.hxg.financialchartdemo.bean

import android.content.Intent

import com.hxg.financialchartdemo.annotation.JsonObject
import com.hxg.financialchartdemo.ui.BaseActivity

import java.lang.ref.WeakReference

@JsonObject
class XActivityInfo(a: BaseActivity, var intent: Intent) {
    val a: WeakReference<BaseActivity> = WeakReference(a)
}
