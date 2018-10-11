package com.hxg.financialchartdemo.bean

import com.hxg.financialchartdemo.util.RegexUtils

class FundMode(//x轴原始时间数据，ms
        var datetime: Long, //y轴的原始数据
        var originDataY: String,
        //y轴的转换后的数据
        var dataY: Float = RegexUtils.getPureDouble(originDataY),//提取后的Y周的值
        //在自定义view:FundView中的位置坐标
        var floatX: Float = 0f,
        var floatY: Float = 0f)
