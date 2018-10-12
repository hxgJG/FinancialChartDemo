package com.hxg.financialchartdemo.http

import android.content.Context
import java.io.IOException
import java.io.InputStream
import java.util.*

object FundApi {

    /**
     * 获取去最原始的数据信息
     *
     * @return json data
     */
    fun getOriginalFundData(context: Context): String? {
        var input: InputStream? = null
        try {
            input = context.assets.open("fund.json")
            return convertStreamToString(input)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * input 流转换为字符串
     *
     * @param inps
     * @return
     */
    private fun convertStreamToString(inps: InputStream): String? {
        var s: String? = null
        try {
            val scanner = Scanner(inps, "UTF-8").useDelimiter("\\A")
            if (scanner.hasNext()) s = scanner.next()
            inps.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return s
    }
}
