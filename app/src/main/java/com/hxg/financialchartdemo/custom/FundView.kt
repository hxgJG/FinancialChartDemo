package com.hxg.financialchartdemo.custom

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
import java.text.SimpleDateFormat

class FundView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseView(context, attrs, defStyleAttr) {
    //数据源
    internal var mFundModeList: List<FundMode>? = null

    //上下左右padding,允许修改
    protected var mBasePaddingTop = 100f
    protected var mBasePaddingBottom = 70f
    protected var mBasePaddingLeft = 50f
    protected var mBasePaddingRight = 50f

    //Y轴对应的最大值和最小值,注意，这里存的是对象。原则上不允许修改。
    protected lateinit var mMinFundMode: FundMode
    protected lateinit var mMaxFundMode: FundMode

    //X、Y轴每一个data对应的大小。原则上不允许修改。
    protected var mPerX: Float = 0.toFloat()
    protected var mPerY: Float = 0.toFloat()

    //正在加载中,允许修改
    protected lateinit var mLoadingPaint: Paint
    protected var mLoadingTextSize = 20f
    protected var mLoadingText = "数据加载，请稍后"
    //原则上不允许修改。
    protected var mDrawLoadingPaint = true


    //外围X、Y轴线文字。允许修改。
    lateinit var xyPaint: Paint

    //x、y轴指示文字字体的大小
    var xyTextSize = 14f

    //左侧文字距离左边线线的距离
    protected var mLeftTxtPadding = 16f
    //底部文字距离底部线的距离
    protected var mBottomTxtPadding = 20f


    //内部X轴虚线。允许修改。
    protected lateinit var mInnerXPaint: Paint
    protected var mInnerXStrokeWidth = 1f

    //折线。允许修改。
    protected lateinit var mBrokenPaint: Paint
    //单位：sp.。允许修改。
    protected var mBrokenStrokeWidth = 1f

    //长按的十字线，允许修改。
    protected lateinit var mLongPressPaint: Paint
    //原则上不允许修改。
    protected var mDrawLongPressPaint = false
    //长按处理,原则上不允许修改。
    protected var mPressTime: Long = 0
    protected var mPressX: Float = 0.toFloat()
    protected var mPressY: Float = 0.toFloat()

    //最上面默认显示累计收益金额，允许修改。
    protected lateinit var mDefAllIncomePaint: Paint
    protected var mDefAllIncomeTextSize = 20f


    //长按情况下x轴和y轴要显示的文字,允许修改。
    protected lateinit var mLongPressTxtPaint: Paint
    protected var mLongPressTextSize = 20f

    init {
        initAttrs()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //setDefAttrs();

        //默认加载loading界面
        showLoadingPaint(canvas)
        if (mFundModeList == null || mFundModeList!!.size == 0) return

        //加载三个核心Paint
        drawInnerXPaint(canvas)
        drawBrokenPaint(canvas)
        drawXYPaint(canvas)

        drawTopTxtPaint(canvas)

        drawLongPress(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> mPressTime = event.downTime
            MotionEvent.ACTION_MOVE -> if (event.eventTime - mPressTime > def_longpress_length) {
                XLog.d(TAG, "onTouchEvent: 长按了。。。")
                mPressX = event.x
                mPressY = event.y
                //处理长按后的逻辑
                showLongPressView()
            }
            MotionEvent.ACTION_UP ->
                //处理松手后的逻辑
                hiddenLongPressView()
            else -> {
            }
        }

        return true
    }

    private fun initAttrs() {
        initLoadingPaint()
        initInnerXPaint()
        initXYPaint()
        initBrokenPaint()
        initLongPressPaint()
        initTopTxt()
    }

    private fun initLoadingPaint() {
        mLoadingPaint = Paint()
        mLoadingPaint.color = getColor(R.color.color_fundView_xyTxtColor)
        mLoadingPaint.textSize = mLoadingTextSize
        mLoadingPaint.isAntiAlias = true
    }

    //初始化绘制虚线的画笔
    private fun initInnerXPaint() {
        mInnerXPaint = Paint()
        mInnerXPaint.color = getColor(R.color.color_fundView_xLineColor)
        mInnerXPaint.strokeWidth = mInnerXStrokeWidth
        mInnerXPaint.style = Paint.Style.STROKE
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)//禁用硬件加速
        val effects = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
        mInnerXPaint.pathEffect = effects
    }

    private fun initXYPaint() {
        xyPaint = Paint()
        xyPaint.color = getColor(R.color.color_fundView_xyTxtColor)
        xyPaint.textSize = xyTextSize
        xyPaint.isAntiAlias = true
    }

    private fun initBrokenPaint() {
        mBrokenPaint = Paint()
        mBrokenPaint.color = getColor(R.color.color_fundView_brokenLineColor)
        mBrokenPaint.style = Paint.Style.STROKE
        mBrokenPaint.isAntiAlias = true
        mBrokenPaint.strokeWidth = mBrokenStrokeWidth
    }

    private fun initLongPressPaint() {
        mLongPressPaint = Paint()
        mLongPressPaint.color = getColor(R.color.color_fundView_longPressLineColor)
        mLongPressPaint.style = Paint.Style.FILL
        mLongPressPaint.isAntiAlias = true
        mLongPressPaint.textSize = mLongPressTextSize
    }

    //折线上面显示文字信息
    private fun initTopTxt() {
        mDefAllIncomePaint = Paint()
        mDefAllIncomePaint.color = getColor(R.color.color_fundView_defIncomeTxt)
        mDefAllIncomePaint.textSize = mLongPressTextSize
        mDefAllIncomePaint.isAntiAlias = true

        mLongPressTxtPaint = Paint()
        mLongPressTxtPaint.color = getColor(R.color.color_fundView_longPressLineColor)
        mLongPressTxtPaint.textSize = mLongPressTextSize
        mLongPressTxtPaint.isAntiAlias = true
    }

    /**
     * 将画笔使用的属性在这里设置。
     * 主要是为了覆盖用户动态设置的属性，
     * 因为在构造方法中设置的会无效（用户设置的在构造方法之后）。
     * 注意：这个方法不能使用，因为会覆盖Paint内部的设置属性的方法
     */
    @Deprecated("")
    private fun setDefAttrs() {
        mLoadingPaint.textSize = mLoadingTextSize
        mInnerXPaint.strokeWidth = mInnerXStrokeWidth
        xyPaint.textSize = xyTextSize
        mBrokenPaint.strokeWidth = mBrokenStrokeWidth
        mLongPressPaint.textSize = mLongPressTextSize
        mDefAllIncomePaint.textSize = mLongPressTextSize
        mLongPressTxtPaint.textSize = mLongPressTextSize
    }

    private fun showLoadingPaint(canvas: Canvas) {
        if (!mDrawLoadingPaint) return
        //这里特别注意，x轴的起始点要减去文字宽度的一半
        canvas.drawText(mLoadingText, baseWidth / 2 - mLoadingPaint.measureText(mLoadingText) / 2, (baseHeight / 2).toFloat(), mLoadingPaint)
    }

    private fun drawInnerXPaint(canvas: Canvas) {
        //画5条横轴的虚线
        //首先确定最大值和最小值的位置
        val perHight = (baseHeight - mBasePaddingBottom - mBasePaddingTop) / 4

        canvas.drawLine(0 + mBasePaddingLeft, mBasePaddingTop,
                baseWidth - mBasePaddingRight, mBasePaddingTop, mInnerXPaint)//最上面的那一条

        canvas.drawLine(0 + mBasePaddingLeft, mBasePaddingTop + perHight * 1,
                baseWidth - mBasePaddingRight, mBasePaddingTop + perHight * 1, mInnerXPaint)//2

        canvas.drawLine(0 + mBasePaddingLeft, mBasePaddingTop + perHight * 2,
                baseWidth - mBasePaddingRight, mBasePaddingTop + perHight * 2, mInnerXPaint)//3

        canvas.drawLine(0 + mBasePaddingLeft, mBasePaddingTop + perHight * 3,
                baseWidth - mBasePaddingRight, mBasePaddingTop + perHight * 3, mInnerXPaint)//4

        canvas.drawLine(0 + mBasePaddingLeft, baseHeight - mBasePaddingBottom,
                baseWidth - mBasePaddingRight, baseHeight - mBasePaddingBottom, mInnerXPaint)//最下面的那一条

    }

    private fun drawBrokenPaint(canvas: Canvas) {
        //先画第一个点
        val fundMode = mFundModeList!![0]
        val path = Path()
        //这里需要说明一下，x轴的起始点，其实需要加上mPerX，但是加上之后不是从起始位置开始，不好看。
        // 同理，for循环内x轴其实需要(i+1)。现在这样处理，最后会留一点空隙，其实挺好看的。
        val floatY = baseHeight - mBasePaddingBottom - mPerY * (fundMode.dataY - mMinFundMode.dataY)
        fundMode.floatX = mBasePaddingLeft
        fundMode.floatY = floatY
        path.moveTo(mBasePaddingLeft, floatY)
        for (i in 1 until mFundModeList!!.size) {
            val fm = mFundModeList!![i]
            val floatX2 = mBasePaddingLeft + mPerX * i
            val floatY2 = baseHeight - mBasePaddingBottom - mPerY * (fm.dataY - mMinFundMode.dataY)
            fm.floatX = floatX2
            fm.floatY = floatY2
            path.lineTo(floatX2, floatY2)
            //Log.e(TAG, "drawBrokenPaint: " + mBasePaddingLeft + mPerX * i + "-----" + (baseHeight - mClosePerY * (mFundModeList.get(i).dataY - mMinFundMode.dataY) - mBasePaddingBottom));
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
        var finalFundMode = mFundModeList!![0]
        var minXLen = Integer.MAX_VALUE.toFloat()
        for (i in mFundModeList!!.indices) {
            val currFunMode = mFundModeList!![i]
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
        val hight = mBasePaddingTop - 30
        val bgColor = Paint(Paint.ANTI_ALIAS_FLAG)
        bgColor.color = getColor(R.color.color_fundView_pressIncomeTxtBg)
        canvas.drawRect(0f, 0f, baseWidth.toFloat(), hight, bgColor)

        //开始画按下之后左边的日期文字
        val timePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        timePaint.textSize = mLongPressTextSize
        timePaint.color = getColor(R.color.color_fundView_xyTxtColor)
        canvas.drawText(processDateTime(finalFundMode.datetime) + "",
                10f, hight / 2 + getFontHeight(mLongPressTextSize, timePaint) / 2, timePaint)

        //右边红色收益文字
        canvas.drawText(finalFundMode.dataY.toString() + "",
                baseWidth - mBasePaddingRight - mLongPressPaint.measureText(finalFundMode.dataY.toString() + ""),
                hight / 2 + getFontHeight(mLongPressTextSize, timePaint) / 2, mLongPressPaint)

        //右边的左边的提示文字
        val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        hintPaint.textSize = mLongPressTextSize
        hintPaint.color = getColor(R.color.color_fundView_xyTxtColor)
        canvas.drawText(getString(R.string.string_fundView_pressHintTxt),
                baseWidth - mBasePaddingRight - mLongPressPaint.measureText(finalFundMode.dataY.toString() + "")
                        - hintPaint.measureText(getString(R.string.string_fundView_pressHintTxt)),
                hight / 2 + getFontHeight(mLongPressTextSize, timePaint) / 2, hintPaint)


    }

    //找到最大时间、最小时间和中间时间显示即可
    private fun drawXPaint(canvas: Canvas) {
        val beginTime = mFundModeList!![0].datetime
        val midTime = mFundModeList!![(mFundModeList!!.size - 1) / 2].datetime
        val endTime = mFundModeList!![mFundModeList!!.size - 1].datetime
        val bengin = processDateTime(beginTime)
        val mid = processDateTime(midTime)
        val end = processDateTime(endTime)

        //x轴文字的高度
        val hight = baseHeight - mBasePaddingBottom + mBottomTxtPadding

        canvas.drawText(bengin,
                mBasePaddingLeft,
                hight, xyPaint)

        canvas.drawText(mid,
                mBasePaddingLeft + (baseWidth - mBasePaddingLeft - mBasePaddingRight) / 2,
                hight, xyPaint)

        canvas.drawText(end,
                baseWidth - mBasePaddingRight - xyPaint.measureText(end),
                hight, xyPaint)//特别注意x轴的处理：- mXYPaint.measureText(end)

    }

    private fun drawYPaint(canvas: Canvas) {
        //现将最小值、最大值画好
        //draw min
        val txtWigth = xyPaint.measureText(mMinFundMode.originDataY) + mLeftTxtPadding
        canvas.drawText(mMinFundMode.originDataY + "",
                mBasePaddingLeft - txtWigth,
                baseHeight - mBasePaddingBottom, xyPaint)
        //draw max
        canvas.drawText(mMaxFundMode.dataY.toString() + "",
                mBasePaddingLeft - txtWigth,
                mBasePaddingTop, xyPaint)
        //因为横线是均分的，所以只要取到最大值最小值的差值，均分即可。
        val perYValues = (mMaxFundMode.dataY - mMinFundMode.dataY) / 4
        val perYWidth = (baseHeight - mBasePaddingBottom - mBasePaddingTop) / 4
        //从下到上依次画
        for (i in 1..3) {
            canvas.drawText((mMinFundMode.dataY + perYValues * i).toString() + "",
                    mBasePaddingLeft - txtWigth,
                    baseHeight - mBasePaddingBottom - perYWidth * i, xyPaint)
        }
    }

    private fun drawDefTopTxtpaint(canvas: Canvas) {
        //画默认情况下前面的蓝色小圆点
        val buleDotPaint = Paint()
        buleDotPaint.color = getColor(R.color.color_fundView_brokenLineColor)
        buleDotPaint.isAntiAlias = true
        val r = 6f
        buleDotPaint.style = Paint.Style.FILL
        canvas.drawCircle(mBasePaddingLeft + r / 2, mBasePaddingTop / 2 + r, r, buleDotPaint)

        val txtHight = getFontHeight(mDefAllIncomeTextSize, mDefAllIncomePaint)

        //先画hint文字
        val hintPaint = Paint()
        hintPaint.color = getColor(R.color.color_fundView_xyTxtColor)
        hintPaint.isAntiAlias = true
        hintPaint.textSize = mDefAllIncomeTextSize
        val hintTxt = getString(R.string.string_fundView_defHintTxt)
        canvas.drawText(hintTxt, mBasePaddingLeft + r + 10f, mBasePaddingTop / 2 + txtHight / 2,
                mDefAllIncomePaint)


        if (mFundModeList == null || mFundModeList!!.isEmpty()) return
        canvas.drawText(mFundModeList!![mFundModeList!!.size - 1].dataY.toString() + "",
                mBasePaddingLeft + r + 10f + hintPaint.measureText(getString(R.string.string_fundView_defHintTxt)) + 5f,
                mBasePaddingTop / 2 + txtHight / 2, mDefAllIncomePaint)
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

    private fun processDateTime(beginTime: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(beginTime)
    }

    override fun getFontHeight(fontSize: Float, paint: Paint): Float {
        paint.textSize = fontSize
        val fm = paint.fontMetrics
        return (Math.ceil((fm.descent - fm.top).toDouble()) + 2).toFloat()
    }

    /**
     * 程序入口，设置数据
     */
    fun setDataList(fundModeList: List<FundMode>?) {
        if (fundModeList == null || fundModeList.size == 0) return
        this.mFundModeList = fundModeList

        //开始获取最大值最小值；单个数据尺寸等
        mMinFundMode = mFundModeList!![0]
        mMaxFundMode = mFundModeList!![0]
        for (fundMode in mFundModeList!!) {
            if (fundMode.dataY < mMinFundMode.dataY) {
                mMinFundMode = fundMode
            }
            if (fundMode.dataY > mMaxFundMode.dataY) {
                mMaxFundMode = fundMode
            }
        }
        //获取单个数据X/y轴的大小
        mPerX = (baseWidth - mBasePaddingLeft - mBasePaddingRight) / mFundModeList!!.size
        mPerY = (baseHeight - mBasePaddingTop - mBasePaddingBottom) / (mMaxFundMode.dataY - mMinFundMode.dataY)
        XLog.e(TAG, "setDataList: $mMinFundMode,$mMaxFundMode...$mPerX,$mPerY")

        //数据过来，隐藏加载更多
        hiddenLoadingPaint()

        //刷新界面
        invalidate()
    }


    //-----------------------对开发者暴露可以修改的参数-------


    fun getFundModeList(): List<FundMode>? {
        return mFundModeList
    }

    fun setFundModeList(fundModeList: List<FundMode>): FundView {
        mFundModeList = fundModeList
        return this
    }

    fun getBasePaddingTop(): Float {
        return mBasePaddingTop
    }

    fun setBasePaddingTop(basePaddingTop: Float): FundView {
        mBasePaddingTop = basePaddingTop
        return this
    }

    fun getBasePaddingBottom(): Float {
        return mBasePaddingBottom
    }

    fun setBasePaddingBottom(basePaddingBottom: Float): FundView {
        mBasePaddingBottom = basePaddingBottom
        return this
    }

    fun getBasePaddingLeft(): Float {
        return mBasePaddingLeft
    }

    fun setBasePaddingLeft(basePaddingLeft: Float): FundView {
        mBasePaddingLeft = basePaddingLeft
        return this
    }

    fun getBasePaddingRight(): Float {
        return mBasePaddingRight
    }

    fun setBasePaddingRight(basePaddingRight: Float): FundView {
        mBasePaddingRight = basePaddingRight
        return this
    }

    fun getMinFundMode(): FundMode {
        return mMinFundMode
    }

    fun setMinFundMode(minFundMode: FundMode): FundView {
        mMinFundMode = minFundMode
        return this
    }

    fun getMaxFundMode(): FundMode {
        return mMaxFundMode
    }

    fun setMaxFundMode(maxFundMode: FundMode): FundView {
        mMaxFundMode = maxFundMode
        return this
    }

    fun getPerX(): Float {
        return mPerX
    }

    fun setPerX(perX: Float): FundView {
        mPerX = perX
        return this
    }

    fun getPerY(): Float {
        return mPerY
    }

    fun setPerY(perY: Float): FundView {
        mPerY = perY
        return this
    }

    fun getLoadingPaint(): Paint {
        return mLoadingPaint
    }

    fun setLoadingPaint(loadingPaint: Paint): FundView {
        mLoadingPaint = loadingPaint
        return this
    }

    fun getLoadingTextSize(): Float {
        return mLoadingTextSize
    }

    private fun setLoadingTextSize(loadingTextSize: Float): FundView {
        mLoadingTextSize = loadingTextSize
        return this
    }

    fun getLoadingText(): String {
        return mLoadingText
    }

    fun setLoadingText(loadingText: String): FundView {
        mLoadingText = loadingText
        return this
    }

    fun isDrawLoadingPaint(): Boolean {
        return mDrawLoadingPaint
    }

    fun setDrawLoadingPaint(drawLoadingPaint: Boolean): FundView {
        mDrawLoadingPaint = drawLoadingPaint
        return this
    }

    fun setXYPaint(XYPaint: Paint): FundView {
        xyPaint = XYPaint
        return this
    }

    private fun setXYTextSize(XYTextSize: Float): FundView {
        xyTextSize = XYTextSize
        return this
    }

    fun getLeftTxtPadding(): Float {
        return mLeftTxtPadding
    }

    fun setLeftTxtPadding(leftTxtPadding: Float): FundView {
        mLeftTxtPadding = leftTxtPadding
        return this
    }

    fun getBottomTxtPadding(): Float {
        return mBottomTxtPadding
    }

    fun setBottomTxtPadding(bottomTxtPadding: Float): FundView {
        mBottomTxtPadding = bottomTxtPadding
        return this
    }

    fun getInnerXPaint(): Paint {
        return mInnerXPaint
    }

    fun setInnerXPaint(innerXPaint: Paint): FundView {
        mInnerXPaint = innerXPaint
        return this
    }

    fun getInnerXStrokeWidth(): Float {
        return mInnerXStrokeWidth
    }

    private fun setInnerXStrokeWidth(innerXStrokeWidth: Float): FundView {
        mInnerXStrokeWidth = innerXStrokeWidth
        return this
    }

    fun getBrokenPaint(): Paint {
        return mBrokenPaint
    }

    fun setBrokenPaint(brokenPaint: Paint): FundView {
        mBrokenPaint = brokenPaint
        return this
    }

    fun getBrokenStrokeWidth(): Float {
        return mBrokenStrokeWidth
    }

    private fun setBrokenStrokeWidth(brokenStrokeWidth: Float): FundView {
        mBrokenStrokeWidth = brokenStrokeWidth
        return this
    }

    fun getLongPressPaint(): Paint {
        return mLongPressPaint
    }

    fun setLongPressPaint(longPressPaint: Paint): FundView {
        mLongPressPaint = longPressPaint
        return this
    }

    fun isDrawLongPressPaint(): Boolean {
        return mDrawLongPressPaint
    }

    fun setDrawLongPressPaint(drawLongPressPaint: Boolean): FundView {
        mDrawLongPressPaint = drawLongPressPaint
        return this
    }

    fun getPressTime(): Long {
        return mPressTime
    }

    fun setPressTime(pressTime: Long): FundView {
        mPressTime = pressTime
        return this
    }

    fun getPressX(): Float {
        return mPressX
    }

    fun setPressX(pressX: Float): FundView {
        mPressX = pressX
        return this
    }

    fun getPressY(): Float {
        return mPressY
    }

    fun setPressY(pressY: Float): FundView {
        mPressY = pressY
        return this
    }

    fun getDefAllIncomePaint(): Paint {
        return mDefAllIncomePaint
    }

    fun setDefAllIncomePaint(defAllIncomePaint: Paint): FundView {
        mDefAllIncomePaint = defAllIncomePaint
        return this
    }

    fun getDefAllIncomeTextSize(): Float {
        return mDefAllIncomeTextSize
    }

    fun setDefAllIncomeTextSize(defAllIncomeTextSize: Float): FundView {
        mDefAllIncomeTextSize = defAllIncomeTextSize
        return this
    }

    fun getLongPressTxtPaint(): Paint {
        return mLongPressTxtPaint
    }

    fun setLongPressTxtPaint(longPressTxtPaint: Paint): FundView {
        mLongPressTxtPaint = longPressTxtPaint
        return this
    }

    fun getLongPressTextSize(): Float {
        return mLongPressTextSize
    }

    private fun setLongPressTextSize(longPressTextSize: Float): FundView {
        mLongPressTextSize = longPressTextSize
        return this
    }
}
