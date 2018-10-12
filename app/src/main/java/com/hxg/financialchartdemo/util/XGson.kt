package com.hxg.financialchartdemo.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type

/**
 *
 * GSON工具类
 *
 * @author
 * @version $Id: GsonUtil.java
 */
object XGson {

    private val gson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create()
    private val prettyGson = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setPrettyPrinting()
            .create()

    /**
     * 小写下划线的格式解析JSON字符串到对象
     *
     * 例如 is_success->isSuccess
     *
     * @param json
     * @param classOfT
     * @return
     */
    fun <T> fromJsonUnderScoreStyle(json: String, classOfT: Class<T>): T {
        return gson.fromJson(json, classOfT)
    }

    /**
     * JSON字符串转为Map<String></String>,String>
     *
     * @param json
     * @return
     */
    fun <T> fronJson2Map(json: String): T {
        return gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
    }

    /**
     * 小写下划线的格式将对象转换成JSON字符串
     *
     * @param src
     * @return
     */
    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    fun toPrettyString(src: Any): String {
        return prettyGson.toJson(src)
    }

    fun <T> fromJson2Object(src: String, t: Class<T>): T {
        return gson.fromJson(src, t)
    }

    fun <T> fromJson2Object(src: String, typeOfT: Type): T {
        return gson.fromJson(src, typeOfT)
    }
}
