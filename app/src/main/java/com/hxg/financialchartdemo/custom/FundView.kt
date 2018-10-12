package com.hxg.financialchartdemo.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.hxg.itemtouchmove.util.XLog
import com.hxg.financialchartdemo.R
import com.hxg.financialchartdemo.bean.FundMode
import com.hxg.financialchartdemo.util.L
import com.hxg.financialchartdemo.util.XColor
import java.text.SimpleDateFormat

class FundView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseView(context, attrs, defStyleAttr) {
    //数据源
    private var mFundModeList: List<FundMode> = listOf()

    private var mBasePaddingTop = 100f
    private var mBasePaddingBottom = 70f
    private var mBasePaddingLeft = 50f
    private var mBasePaddingRight = 50f

    //Y轴对应的最大值和最小值,注意，这里存的是对象。
    private lateinit var mMinFundMode: FundMode
    private lateinit var mMaxFundMode: FundMode

    //X、Y轴每一个data对应的大小。
    private var mPerX: Float = 0f
    private var mPerY: Float = 0f

    //正在加载中
    private var mLoadingPaint: Paint
    private var mLoadingTextSize = 20f
    var mLoadingText = "数据加载，请稍后"
    private var mDrawLoadingPaint = true


    //外围X、Y轴线文字。
    private var xyPaint: Paint

    //x、y轴指示文字字体的大小
    private var xyTextSize = 14f

    //左侧文字距离左边线线的距离
    private var mLeftTxtPadding = 16f
    //底部文字距离底部线的距离
    private var mBottomTxtPadding = 20f


    //内部X轴虚线。
    var mInnerXPaint: Paint
    private var mInnerXStrokeWidth = 1f

    //折线。
    var mBrokenPaint: Paint

    private var mBrokenStrokeWidth = 1f

    //长按的十字线
    private var mLongPressPaint: Paint

    private var mDrawLongPressPaint = false
    private var mPressX: Float = 0f
    private var mPressY: Float = 0f

    //最上面默认显示累计收益金额
    private var mDefAllIncomePaint: Paint
    private var mDefAllIncomeTextSize = 20f


    //长按情况下x轴和y轴要显示的文字
    private var mLongPressTxtPaint: Paint
    private var mLongPressTextSize = 20f

    init {
        mLoadingPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_xyTxtColor)
            textSize = mLoadingTextSize
            isAntiAlias = true
        }

        // 绘制虚线的画笔
        mInnerXPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_xLineColor)
            strokeWidth = mInnerXStrokeWidth
            style = Paint.Style.STROKE

            setLayerType(View.LAYER_TYPE_SOFTWARE, null)//禁用硬件加速
            val effects = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
            pathEffect = effects
        }

        xyPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_xyTxtColor)
            textSize = xyTextSize
            isAntiAlias = true
        }

        mBrokenPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_brokenLineColor)
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeWidth = mBrokenStrokeWidth
        }

        mLongPressPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_longPressLineColor)
            style = Paint.Style.FILL
            isAntiAlias = true
            textSize = mLongPressTextSize
        }

        //折线上面显示文字信息
        mDefAllIncomePaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_defIncomeTxt)
            textSize = mLongPressTextSize
            isAntiAlias = true
        }

        mLongPressTxtPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_longPressLineColor)
            textSize = mLongPressTextSize
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //默认加载loading界面
        showLoadingPaint(canvas)
        if (mFundModeList.isEmpty()) return

        //加载三个核心Paint
        drawInnerXPaint(canvas)
        drawBrokenPaint(canvas)
        drawXYPaint(canvas)

        drawTopTxtPaint(canvas)

        drawLongPress(canvas)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                XLog.d(TAG, "onTouchEvent: 长按了。。。")
                mPressX = event.x
                mPressY = event.y
                showLongPressView()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> hiddenLongPressView()
        }

        return true
    }

    private fun showLoadingPaint(canvas: Canvas) {
        if (!mDrawLoadingPaint) return
        //这里特别注意，x轴的起始点要减去文字宽度的一半
        canvas.drawText(mLoadingText, baseWidth / 2 - mLoadingPaint.measureText(mLoadingText) / 2, (baseHeight / 2).toFloat(), mLoadingPaint)
    }

    //画5条横轴的虚线
    private fun drawInnerXPaint(canvas: Canvas) {
        //首先确定最大值和最小值的位置
        val perHight = (baseHeight - mBasePaddingBottom - mBasePaddingTop) / 4

        val stopX = baseWidth - mBasePaddingRight

        canvas.drawLine(mBasePaddingLeft, mBasePaddingTop,
                stopX, mBasePaddingTop, mInnerXPaint)//最上面的那一条

        canvas.drawLine(mBasePaddingLeft, mBasePaddingTop + perHight,
                stopX, mBasePaddingTop + perHight, mInnerXPaint)//2

        canvas.drawLine(mBasePaddingLeft, mBasePaddingTop + perHight * 2,
                stopX, mBasePaddingTop + perHight * 2, mInnerXPaint)//3

        canvas.drawLine(mBasePaddingLeft, mBasePaddingTop + perHight * 3,
                stopX, mBasePaddingTop + perHight * 3, mInnerXPaint)//4

        canvas.drawLine(mBasePaddingLeft, baseHeight - mBasePaddingBottom,
                stopX, baseHeight - mBasePaddingBottom, mInnerXPaint)//最下面的那一条

    }

    private fun drawBrokenPaint(canvas: Canvas) {
        if (mFundModeList.isEmpty()) return
        //先画第一个点
        val fundMode = mFundModeList[0]
        val path = Path()
        //这里需要说明一下，x轴的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x轴其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        val floatY = baseHeight - mBasePaddingBottom - mPerY * (fundMode.dataY - mMinFundMode.dataY)
        fundMode.floatX = mBasePaddingLeft
        fundMode.floatY = floatY
        path.moveTo(mBasePaddingLeft, floatY)
        for (i in 1 until mFundModeList.size) {
            val fm = mFundModeList[i]
            val floatX2 = mBasePaddingLeft + mPerX * i
            val floatY2 = baseHeight - mBasePaddingBottom - mPerY * (fm.dataY - mMinFundMode.dataY)
            fm.floatX = floatX2
            fm.floatY = floatY2
            path.lineTo(floatX2, floatY2)
        }

        canvas.drawPath(path, mBrokenPaint)
    }

    private fun drawXYPaint(canvas: Canvas) {
        //先处理y轴方向文字
        drawYPaint(canvas)

        //处理x轴方向文字
        drawXPaint(canvas)
    }

    private fun drawTopTxtPaint(canvas: Canvas) {
        //先画默认情况下的top文字
        drawDefTopTxtpaint(canvas)
        //按下的文字信息在按下之后处理，see:drawLongPress(Canvas canvas)
    }

    /**
     * 这里处理画十字的逻辑:这里的十字不是手指按下的位置，这样没有意义。
     * 而是当前按下的距离x轴最近的时间（注意：并不一定按下对应的x轴就是有时间的，如果没有取最近的）。
     * 当取到x轴的值，之后算出来对应的y轴的值，这个才是十字对应的位置坐标。
     * 如何获取x轴最近的时间？我们可以在FundMode中定义x\y的位置参数，遍历对比找到最小即可。
     * (see: drawBrokenPaint(canvas);)
     *
     * @param canvas
     */
    private fun drawLongPress(canvas: Canvas) {
        if (!mDrawLongPressPaint) return

        //获取距离最近按下的位置的model
        val pressX = mPressX
        //循环遍历，找到距离最短的x轴的mode
        var finalFundMode = mFundModeList[0]
        var minXLen = Integer.MAX_VALUE.toFloat()
        for (i in mFundModeList.indices) {
            val currFunMode = mFundModeList[i]
            val abs = Math.abs(pressX - currFunMode.floatX)
            if (abs < minXLen) {
                finalFundMode = currFunMode
                minXLen = abs
            }
        }

        //x
        canvas.drawLine(mBasePaddingLeft, finalFundMode.floatY, baseWidth - mBasePaddingRight, finalFundMode.floatY, mLongPressPaint)
        //y
        canvas.drawLine(finalFundMode.floatX, mBasePaddingTop, finalFundMode.floatX, baseHeight - mBasePaddingBottom, mLongPressPaint)

        //开始处理按下之后top的文字信息
        //先画背景
        val height = mBasePaddingTop - 30
        val bgColor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = XColor.Get(R.color.color_fundView_pressIncomeTxtBg)
        }
        canvas.drawRect(0f, 0f, baseWidth.toFloat(), height, bgColor)

        //开始画按下之后左边的日期文字
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = mLongPressTextSize
            color = XColor.Get(R.color.color_fundView_xyTxtColor)
        }
        canvas.drawText(processDateTime(finalFundMode.datetime) + "",
                10f, height / 2 + getFontHeight(mLongPressTextSize, timePaint) / 2, timePaint)

        //右边红色收益文字
        canvas.drawText(finalFundMode.dataY.toString() + "",
                baseWidth - mBasePaddingRight - mLongPressPaint.measureText(finalFundMode.dataY.toString() + ""),
                height / 2 + getFontHeight(mLongPressTextSize, timePaint) / 2, mLongPressPaint)

        //右边的左边的提示文字
        val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = mLongPressTextSize
            color = XColor.Get(R.color.color_fundView_xyTxtColor)
        }
        canvas.drawText(L.S(R.string.string_fundView_pressHintTxt),
                baseWidth - mBasePaddingRight - mLongPressPaint.measureText(finalFundMode.dataY.toString() + "")
                        - hintPaint.measureText(L.S(R.string.string_fundView_pressHintTxt)),
                height / 2 + getFontHeight(mLongPressTextSize, timePaint) / 2, hintPaint)
    }

    //找到最大时间、最小时间和中间时间显示即可
    private fun drawXPaint(canvas: Canvas) {
        val end = processDateTime(mFundModeList[mFundModeList.size - 1].datetime)

        //x轴文字的高度
        val height = baseHeight - mBasePaddingBottom + mBottomTxtPadding

        canvas.drawText(processDateTime(mFundModeList[0].datetime), mBasePaddingLeft, height, xyPaint)

        canvas.drawText(processDateTime(mFundModeList[(mFundModeList.size - 1) / 2].datetime),
                mBasePaddingLeft + (baseWidth - mBasePaddingLeft - mBasePaddingRight) / 2, height, xyPaint)

        //特别注意x轴的处理：- mXYPaint.measureText(end)
        canvas.drawText(end, baseWidth - mBasePaddingRight - xyPaint.measureText(end), height, xyPaint)
    }

    private fun drawYPaint(canvas: Canvas) {
        //现将最小值、最大值画好
        //draw min
        val txtWidth = xyPaint.measureText(mMinFundMode.originDataY) + mLeftTxtPadding
        canvas.drawText(mMinFundMode.originDataY, mBasePaddingLeft - txtWidth, baseHeight - mBasePaddingBottom, xyPaint)
        //draw max
        canvas.drawText("${mMaxFundMode.dataY}", mBasePaddingLeft - txtWidth, mBasePaddingTop, xyPaint)
        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
        val perYValues = (mMaxFundMode.dataY - mMinFundMode.dataY) / 4
        val perYWidth = (baseHeight - mBasePaddingBottom - mBasePaddingTop) / 4
        //从下到上依次画
        for (i in 1..3) {
            canvas.drawText((mMinFundMode.dataY + perYValues * i).toString(),
                    mBasePaddingLeft - txtWidth,
                    baseHeight - mBasePaddingBottom - perYWidth * i, xyPaint)
        }
    }

    private fun drawDefTopTxtpaint(canvas: Canvas) {
        //画默认情况下前面的蓝色小圆点
        val blueDotPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_brokenLineColor)
            isAntiAlias = true
            style = Paint.Style.FILL
        }

        val r = 6f
        canvas.drawCircle(mBasePaddingLeft + r / 2, mBasePaddingTop / 2 + r, r, blueDotPaint)

        val txtHeight = getFontHeight(mDefAllIncomeTextSize, mDefAllIncomePaint)

        //先画hint文字
        val hintPaint = Paint().apply {
            color = XColor.Get(R.color.color_fundView_xyTxtColor)
            isAntiAlias = true
            textSize = mDefAllIncomeTextSize
        }
        val hintTxt = L.S(R.string.string_fundView_defHintTxt)
        canvas.drawText(hintTxt, mBasePaddingLeft + r + 10f, mBasePaddingTop / 2 + txtHeight / 2, mDefAllIncomePaint)


        if (mFundModeList.isEmpty()) return
        canvas.drawText(mFundModeList[mFundModeList.size - 1].dataY.toString() + "",
                mBasePaddingLeft + r + 10f + hintPaint.measureText(L.S(R.string.string_fundView_defHintTxt)) + 5f,
                mBasePaddingTop / 2 + txtHeight / 2, mDefAllIncomePaint)
    }

    private fun showLongPressView() {
        mDrawLongPressPaint = true
        invalidate()
    }

    private fun hiddenLongPressView() {
        //实现蚂蚁金服延迟消失十字线
        postDelayed({
            mDrawLongPressPaint = false
            invalidate()
        }, 1000)
    }

    // 只需要把画笔颜色置为透明即可
    private fun hiddenLoadingPaint() {
        mLoadingPaint.color = 0x00000000
        mDrawLoadingPaint = false
    }

    @SuppressLint("SimpleDateFormat")
    private fun processDateTime(beginTime: Long): String {
        return SimpleDateFormat("yyyy-MM-dd").format(beginTime)
    }

    override fun getFontHeight(fontSize: Float, paint: Paint): Float {
        paint.textSize = fontSize
        val fm = paint.fontMetrics
        return (Math.ceil((fm.descent - fm.top).toDouble()) + 2).toFloat()
    }

    /**
     * 程序入口，设置数据
     */
    fun setDataList(fundModeList: List<FundMode>) {
        if (fundModeList.isEmpty()) return
        this.mFundModeList = fundModeList

        //开始获取最大值最小值；单个数据尺寸等
        mMinFundMode = fundModeList[0]
        mMaxFundMode = fundModeList[0]
        for (fundMode in fundModeList) {
            if (fundMode.dataY < mMinFundMode.dataY) {
                mMinFundMode = fundMode
            }
            if (fundMode.dataY > mMaxFundMode.dataY) {
                mMaxFundMode = fundMode
            }
        }
        //获取单个数据X/y轴的大小
        mPerX = (baseWidth - mBasePaddingLeft - mBasePaddingRight) / fundModeList.size
        mPerY = (baseHeight - mBasePaddingTop - mBasePaddingBottom) / (mMaxFundMode.dataY - mMinFundMode.dataY)
        XLog.e(TAG, "setDataList: $mMinFundMode,$mMaxFundMode...$mPerX,$mPerY")

        //数据过来，隐藏加载更多
        hiddenLoadingPaint()

        //刷新界面
        invalidate()
    }

    fun setBasePadding(l: Float = 50f, t: Float = 100f, r: Float = 50f, b: Float = 70f): FundView {
        mBasePaddingLeft = l
        mBasePaddingTop = t
        mBasePaddingRight = r
        mBasePaddingBottom = b
        return this
    }
}
