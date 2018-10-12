package com.hxg.financialchartdemo.interfeet

interface HasStates {
    fun IsActive(): Boolean

    fun IsDestroyed(): Boolean

    //different from android visibility. For example, is framgment B visible in a viewPager if A is active on screen? Answer is: no/false
    fun IsUserVisible(): Boolean
}
