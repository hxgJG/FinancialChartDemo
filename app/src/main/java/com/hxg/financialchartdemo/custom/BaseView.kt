package com.hxg.financialchartdemo.custom

import android.content.Context
import android.graphics.Paint
import android.support.annotation.ColorRes
import android.support.annotation.StringRes
import android.util.AttributeSet
import android.view.View

import android.view.View.MeasureSpec.AT_MOST
import com.example.hxg.itemtouchmove.util.XUI

abstract class BaseView @JvmOverloads constructor(protected var mContext: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(mContext, attrs, defStyleAttr) {
    protected var TAG: String = this.javaClass.simpleName

    //长按阀值，默认多长时间算长按（ms）。不再设置为final,允许用户修改。
    var def_longpress_length: Long = 700
    //单击阀值
    var def_clickpress_length: Long = 100
    //移动阀值。手指移动多远算移动的阀值（单位：sp）
    var def_pull_length: Long = 5
    //onFling的阀值
    protected var def_onfling = 5f

    //控件默认宽高。当控件的宽高设置为wrap_content时会采用该参数进行默认的设置（单位：sp）。
    //不允许用户修改，想要修改宽高，使用mBaseWidth、mBaseHeight。
    val deF_WIDTH = 650f
    val deF_HIGHT = 400f

    //测量的控件宽高，会在onMeasure中进行测量。
    var baseWidth: Int = 0
    var baseHeight: Int = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = View.MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = View.MeasureSpec.getSize(heightMeasureSpec)

        when {
            widthSpecMode == AT_MOST && heightSpecMode == AT_MOST -> setMeasuredDimension(deF_WIDTH.toInt(), deF_HIGHT.toInt())
            widthSpecMode == AT_MOST -> setMeasuredDimension(deF_WIDTH.toInt(), heightSpecSize)
            heightSpecMode == AT_MOST -> setMeasuredDimension(widthSpecSize, deF_HIGHT.toInt())
            else -> setMeasuredDimension(widthSpecSize, heightSpecSize)
        }

        baseWidth = measuredWidth
        baseHeight = measuredHeight
    }

    protected fun getColor(@ColorRes colorId: Int) = XUI.GetResources().getColor(colorId)

    protected fun getString(@StringRes stringId: Int) = XUI.GetResources().getString(stringId)

    /**
     * 测量指定画笔的文字的高度
     *
     * @param fontSize
     * @param paint
     * @return
     */
    protected open fun getFontHeight(fontSize: Float, paint: Paint): Float {
        paint.textSize = fontSize
        val fm = paint.fontMetrics
        return (Math.ceil((fm.descent - fm.top).toDouble()) + 2f).toFloat()
    }
}
