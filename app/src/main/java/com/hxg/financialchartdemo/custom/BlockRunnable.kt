package com.hxg.financialchartdemo.custom

//block based runnable
class BlockRunnable(val block: (r: BlockRunnable) -> Unit) : XRunnable() {
    override fun Run() {
        block(this)
    }
}