package com.hxg.financialchartdemo.util

import java.util.regex.Pattern

object RegexUtils {
    fun getPureDouble(str: String?): Float {
        if (str == null || str.isEmpty()) return 0f
        var result = 0f
        try {
            val compile = Pattern.compile("(\\d+\\.\\d+)|(\\d+)")//如何提取带负数d ???
            val matcher = compile.matcher(str)
            matcher.find()
            val string = matcher.group()//提取匹配到的结果
            result = java.lang.Float.parseFloat(string)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        }
        return result
    }
}
