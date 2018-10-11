package com.hxg.financialchartdemo.util

import java.util.Random

object StringUtils {
    val string: String?
        get() = if (getRadomNum(0, 1) == 0) null else "sdaas"

    fun isEmptyString(str: String?): Boolean {
        return str == null || str.isEmpty()
    }

    fun isNotEmptyString(str: String): Boolean {
        return !isEmptyString(str)
    }

    fun isEmptyList(list: List<*>?): Boolean {
        return list == null || list.isEmpty()
    }

    fun isNotEmptyList(list: List<*>): Boolean {
        return !isEmptyList(list)
    }


    fun isBlank(vararg strs: String): Boolean {
        for (str in strs) {
            if (str == "") return true
        }
        return false
    }

    fun isTrimBlank(vararg strs: String): Boolean {
        for (str in strs) {
            if (str.trim { it <= ' ' } == "") return true
        }
        return false
    }

    fun isEmpty(list: List<*>?): Boolean {
        return list == null || list.isEmpty()
    }

    /**
     * 随机获取[m,n]之间的一个数字
     *
     * @param min
     * @param max
     * @return
     */
    fun getRadomNum(min: Int, max: Int): Int {
        val rdm = Random()
        return rdm.nextInt(max - min + 1) + min
    }
}
