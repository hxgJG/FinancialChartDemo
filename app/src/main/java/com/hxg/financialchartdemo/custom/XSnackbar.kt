package com.hxg.financialchartdemo.custom

import OnClick
import RemoveClick
import Use
import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.support.annotation.ColorInt
import android.support.annotation.IntDef
import android.support.annotation.IntRange
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.SwipeDismissBehavior
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import bind
import com.hxg.financialchartdemo.R
import com.hxg.financialchartdemo.util.AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
import com.hxg.financialchartdemo.util.StatusBarUtil

class XSnackbar private constructor(private val mTargetParent: ViewGroup, isShowInTop: Boolean) {
    /**
     * Callback class for instances.
     */
    abstract class Callback {
        /**
         * @hide
         */
        @IntDef(DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT, DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE)
        @kotlin.annotation.Retention(kotlin.annotation.AnnotationRetention.SOURCE)
        annotation class DismissEvent

        /**
         * Called when the given has been dismissed, either through a time-out,
         * having been manually dismissed, or an action being clicked.

         * @param snackbar The snackbar which has been dismissed.
         * *
         * @param event    The event which caused the dismissal. One of either:
         * *                 [.DISMISS_EVENT_SWIPE], [.DISMISS_EVENT_ACTION],
         * *                 [.DISMISS_EVENT_TIMEOUT], [.DISMISS_EVENT_MANUAL] or
         * *                 [.DISMISS_EVENT_CONSECUTIVE].
         */
        fun onDismissed(snackbar: XSnackbar, @XSnackbar.Callback.DismissEvent event: Int) {
            // empty
        }

        /**
         * Called when the given is visible.

         * @param XSnackbar The XSnackbar which is now visible.
         */
        fun onShown(snackbar: XSnackbar) {
            // empty
        }

        companion object {
            /**
             * Indicates that the Snackbar was dismissed via a swipe.
             */
            const val DISMISS_EVENT_SWIPE = 0
            /**
             * Indicates that the Snackbar was dismissed via an action click.
             */
            const val DISMISS_EVENT_ACTION = 1
            /**
             * Indicates that the Snackbar was dismissed via a timeout.
             */
            const val DISMISS_EVENT_TIMEOUT = 2
            /**
             * Indicates that the Snackbar was dismissed via a call to [.dismiss].
             */
            const val DISMISS_EVENT_MANUAL = 3
            /**
             * Indicates that the Snackbar was dismissed from a new Snackbar being shown.
             */
            const val DISMISS_EVENT_CONSECUTIVE = 4
        }
    }

    /**
     * @hide
     */
    @IntDef(LENGTH_INDEFINITE, LENGTH_SHORT, LENGTH_LONG)
    @IntRange(from = 1)
    @kotlin.annotation.Retention(kotlin.annotation.AnnotationRetention.SOURCE)
    annotation class Duration

    private val mContext: Context = mTargetParent.context
    internal val mView: XSnackbar.SnackbarLayout
    private var mDuration: Int = 0
    private var mCallback: XSnackbar.Callback? = null
    private var isShowInTop = false

    private val mAccessibilityManager: AccessibilityManager

    init {
        val inflater = LayoutInflater.from(mContext)
        if (isShowInTop) {
            mView = inflater.inflate(
                    R.layout.design_layout_snackbar_top, mTargetParent, false) as XSnackbar.SnackbarLayout

            mView.setPadding(mView.paddingLeft, StatusBarUtil.getHeight(mContext),
                    mView.paddingRight, 0)
        } else {
            mView = inflater.inflate(
                    R.layout.design_layout_snackbar_bottom, mTargetParent, false) as XSnackbar.SnackbarLayout
        }

        mAccessibilityManager = mContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    }

    /**
     * Set the action to be displayed in this.

     * @param resId    String resource to display
     * *
     * @param listener callback to be invoked when the action is clicked
     */
    fun setAction(@StringRes resId: Int, listener: View.OnClickListener): XSnackbar {
        return setAction(mContext.getText(resId), listener)
    }

    /**
     * Set the action to be displayed in this.

     * @param text     Text to display
     * *
     * @param listener callback to be invoked when the action is clicked
     */
    fun setAction(text: CharSequence, listener: View.OnClickListener?): XSnackbar {
        val tv = mView.actionView

        if (TextUtils.isEmpty(text) || listener == null) {
            tv!!.visibility = View.GONE
            tv.RemoveClick()
        } else {
            tv!!.visibility = View.VISIBLE
            tv.text = text
            tv.OnClick(View.OnClickListener { view ->
                listener.onClick(view)
                // Now dismiss the Snackbar
                dispatchDismiss(Callback.DISMISS_EVENT_ACTION)
            })
        }

        return this
    }

    /**
     * Sets the text color of the action specified in
     * [.setAction].
     */
    fun setActionTextColor(colors: ColorStateList): XSnackbar {
        val tv = mView.actionView
        tv!!.setTextColor(colors)
        return this
    }

    /**
     * Sets the text color of the action specified in
     * [.setAction].
     */
    fun setActionTextColor(@ColorInt color: Int): XSnackbar {
        val tv = mView.actionView
        tv!!.setTextColor(color)
        return this
    }

    /**
     * Update the text in this [XSnackbar].

     * @param message The new text for the Toast.
     */
    fun setText(message: CharSequence): XSnackbar {
        val tv = mView.messageView
        tv!!.text = message
        return this
    }

    /**
     * Update the text in this [XSnackbar].

     * @param resId The new text for the Toast.
     */
    fun setText(@StringRes resId: Int): XSnackbar {
        return setText(mContext.getText(resId))
    }

    /**
     * Set how long to show the view for.

     * @param duration either be one of the predefined lengths:
     * *                 [.LENGTH_SHORT], [.LENGTH_LONG], or a custom duration
     * *                 in milliseconds.
     */
    fun setDuration(@Duration duration: Int): XSnackbar {
        mDuration = duration
        return this
    }

    /**
     * Return the duration.

     * @see .setDuration
     */
    @Duration
    fun getDuration(): Int {
        return mDuration
    }

    /**
     * Returns the [XSnackbar]'s view.
     */
    val view: View
        get() = mView

    /**
     * Show the [XSnackbar].
     */
    fun show() {
        SnackbarManager.instance.show(mDuration, mManagerCallback)
    }

    /**
     * Dismiss the.
     */
    fun dismiss() {
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL)
    }

    internal fun dispatchDismiss(@Callback.DismissEvent event: Int) {
        SnackbarManager.instance.dismiss(mManagerCallback, event)
    }

    /**
     * Set a callback to be called when this the visibility of this changes.
     */
    fun setCallback(callback: XSnackbar.Callback): XSnackbar {
        mCallback = callback
        return this
    }

    /**
     * Return whether this is currently being shown.
     */
    val isShown: Boolean
        get() = SnackbarManager.instance.isCurrent(mManagerCallback)

    /**
     * Returns whether this is currently being shown, or is queued to be
     * shown next.
     */
    val isShownOrQueued: Boolean
        get() = SnackbarManager.instance.isCurrentOrNext(mManagerCallback)

    internal val mManagerCallback: SnackbarManager.Callback = object : SnackbarManager.Callback {
        override fun show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, this@XSnackbar))
        }

        override fun dismiss(event: Int) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0, this@XSnackbar))
        }
    }

    internal fun showView() {
        if (mView.parent == null) {
            val lp = mView.layoutParams

            if (lp is CoordinatorLayout.LayoutParams) {
                // If our LayoutParams are from a CoordinatorLayout, we'll setup our Behavior

                val behavior = Behavior()
                behavior.setStartAlphaSwipeDistance(0.1f)
                behavior.setEndAlphaSwipeDistance(0.6f)
                behavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END)
                behavior.setListener(object : SwipeDismissBehavior.OnDismissListener {
                    override fun onDismiss(view: View) {
                        view.visibility = View.GONE
                        dispatchDismiss(Callback.DISMISS_EVENT_SWIPE)
                    }

                    override fun onDragStateChanged(state: Int) {
                        when (state) {
                            SwipeDismissBehavior.STATE_DRAGGING, SwipeDismissBehavior.STATE_SETTLING ->
                                // If the view is being dragged or settling, cancel the timeout
                                SnackbarManager.instance.pauseTimeout(mManagerCallback)
                            SwipeDismissBehavior.STATE_IDLE ->
                                // If the view has been released and is idle, restore the timeout
                                SnackbarManager.instance.restoreTimeoutIfPaused(mManagerCallback)
                        }
                    }
                })
                lp.behavior = behavior
                // Also set the inset edge so that views can dodge the snackbar correctly
                lp.insetEdge = Gravity.BOTTOM
            }

            mTargetParent.addView(mView)
        }

        mView.setOnAttachStateChangeListener(object : XSnackbar.SnackbarLayout.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}

            override fun onViewDetachedFromWindow(v: View) {
                if (isShownOrQueued) {
                    // If we haven't already been dismissed then this event is coming from a
                    // non-user initiated action. Hence we need to make sure that we callback
                    // and keep our state up to date. We need to post the call since removeView()
                    // will call through to onDetachedFromWindow and thus overflow.
                    sHandler.post { onViewHidden(XSnackbar.Callback.DISMISS_EVENT_MANUAL) }
                }
            }
        })

        if (ViewCompat.isLaidOut(mView)) {
            if (shouldAnimate()) {
                // If animations are enabled, animate it in
                if (isShowInTop) {
                    animateViewInTop()
                } else {
                    animateViewInBottom()
                }
            } else {
                // Else if anims are disabled just call back now
                onViewShown()
            }
        } else {
            // Otherwise, add one of our layout change listeners and show it in when laid out
            mView.setOnLayoutChangeListener(object : XSnackbar.SnackbarLayout.OnLayoutChangeListener {
                override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int) {
                    mView.setOnLayoutChangeListener(null)

                    if (shouldAnimate()) {
                        // If animations are enabled, animate it in
                        if (isShowInTop) {
                            animateViewInTop()
                        } else {
                            animateViewInBottom()
                        }
                    } else {
                        // Else if anims are disabled just call back now
                        onViewShown()
                    }
                }
            })
        }
    }

    private fun animateViewInBottom() {
        mView.translationY = mView.height.toFloat()
        ViewCompat.animate(mView)
                .translationY(0f)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationStart(view: View?) {
                        mView.animateChildrenIn(ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                                ANIMATION_FADE_DURATION)
                    }

                    override fun onAnimationEnd(view: View?) {
                        onViewShown()
                    }
                }).start()
    }

    private fun animateViewOutBottom(event: Int) {
        ViewCompat.animate(mView)
                .translationY(mView.height.toFloat())
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationStart(view: View?) {
                        mView.animateChildrenOut(0, ANIMATION_FADE_DURATION)
                    }

                    override fun onAnimationEnd(view: View?) {
                        onViewHidden(event)
                    }
                }).start()
    }

    private fun animateViewInTop() {
        mView.translationY = (-mView.height).toFloat()
        ViewCompat.animate(mView)
                .translationY(0f)
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationStart(view: View?) {
                        mView.animateChildrenIn(ANIMATION_DURATION - ANIMATION_FADE_DURATION,
                                ANIMATION_FADE_DURATION)
                    }

                    override fun onAnimationEnd(view: View?) {
                        if (mCallback != null) {
                            mCallback!!.onShown(this@XSnackbar)
                        }
                        SnackbarManager.instance
                                .onShown(mManagerCallback)
                    }
                }).start()
    }

    private fun animateViewOutTop(event: Int) {
        ViewCompat.animate(mView)
                .translationY((-mView.height).toFloat())
                .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                .setDuration(ANIMATION_DURATION.toLong())
                .setListener(object : ViewPropertyAnimatorListenerAdapter() {
                    override fun onAnimationStart(view: View?) {
                        mView.animateChildrenOut(0, ANIMATION_FADE_DURATION)
                    }

                    override fun onAnimationEnd(view: View?) {
                        onViewHidden(event)
                    }
                }).start()
    }

    internal fun hideView(@XSnackbar.Callback.DismissEvent event: Int) {
        if (shouldAnimate() && mView.visibility == View.VISIBLE) {

            if (isShowInTop) {
                animateViewOutTop(event)
            } else {
                animateViewOutBottom(event)
            }
        } else {
            // If anims are disabled or the view isn't visible, just call back now
            onViewHidden(event)
        }
    }

    internal fun onViewShown() {
        SnackbarManager.instance.onShown(mManagerCallback)
        if (mCallback != null) {
            mCallback!!.onShown(this)
        }
    }

    internal fun onViewHidden(event: Int) {
        // First tell the SnackbarManager that it has been dismissed
        SnackbarManager.instance.onDismissed(mManagerCallback)
        // Now call the dismiss listener (if available)
        if (mCallback != null) {
            mCallback!!.onDismissed(this, event)
        }

        // Lastly, hide and remove the view from the parent (if attached)
        val parent = mView.parent
        if (parent is ViewGroup) {
            parent.removeView(mView)
        }
    }

    /**
     * Returns true if we should animate the Snackbar view in/out.
     */
    internal fun shouldAnimate(): Boolean {
        //        return !mAccessibilityManager.isEnabled();
        //todo tmp fix some phone show toast without animation
        return true
    }

    /**
     * @hide
     */
    class SnackbarLayout constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
        internal var messageView: TextView? = null
            private set
        internal var actionView: Button? = null
            private set

        private var mMaxWidth: Int = 0
        private var mMaxInlineActionWidth: Int = 0

        internal interface OnLayoutChangeListener {
            fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int)
        }

        internal interface OnAttachStateChangeListener {
            fun onViewAttachedToWindow(v: View)

            fun onViewDetachedFromWindow(v: View)
        }

        private var mOnLayoutChangeListener: XSnackbar.SnackbarLayout.OnLayoutChangeListener? = null
        private var mOnAttachStateChangeListener: XSnackbar.SnackbarLayout.OnAttachStateChangeListener? = null

        init {
            context.obtainStyledAttributes(attrs, android.support.design.R.styleable.SnackbarLayout).Use { a ->
                mMaxWidth = a.getDimensionPixelSize(android.support.design.R.styleable.SnackbarLayout_android_maxWidth, -1)
                mMaxInlineActionWidth = a.getDimensionPixelSize(
                        android.support.design.R.styleable.SnackbarLayout_maxActionInlineWidth, -1)
                if (a.hasValue(android.support.design.R.styleable.SnackbarLayout_elevation)) {
                    ViewCompat.setElevation(this, a.getDimensionPixelSize(
                            android.support.design.R.styleable.SnackbarLayout_elevation, 0).toFloat())
                }
            }

            isClickable = true

            // Now inflate our content. We need to do this manually rather than using an <include>
            // in the layout since older versions of the Android do not inflate includes with
            // the correct Context.
            LayoutInflater.from(context).inflate(R.layout.design_layout_snackbar_include, this)

            ViewCompat.setAccessibilityLiveRegion(this,
                    ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
            ViewCompat.setImportantForAccessibility(this,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES)

            // Make sure that we fit system windows and have a listener to apply any insets
            //            ViewCompat.setFitsSystemWindows(this, true);
            //            ViewCompat.setOnApplyWindowInsetsListener(this,
            //                    new android.support.v4.view.OnApplyWindowInsetsListener() {
            //                        @Override
            //                        public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            //                            // Copy over the bottom inset as padding so that we're displayed above the
            //                            // navigation bar
            //                            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(),
            //                                    v.getPaddingRight(), insets.getSystemWindowInsetBottom());
            //                            return insets;
            //                        }
            //                    });
        }

        override fun onFinishInflate() {
            super.onFinishInflate()
            messageView = bind(R.id.snackbar_text)
            actionView = bind(R.id.snackbar_action)
        }

        override fun onMeasure(widthMeasureSpecIn: Int, heightMeasureSpec: Int) {
            var widthMeasureSpec = widthMeasureSpecIn
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)

            if (mMaxWidth in 1..(measuredWidth - 1)) {
                widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(mMaxWidth, View.MeasureSpec.EXACTLY)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }

            val multiLineVPadding = resources.getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical_2lines)
            val singleLineVPadding = resources.getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical)
            val isMultiLine = messageView!!.layout.lineCount > 1

            var remeasure = false
            if (isMultiLine && mMaxInlineActionWidth > 0
                    && actionView!!.measuredWidth > mMaxInlineActionWidth) {
                if (updateViewsWithinLayout(LinearLayout.VERTICAL, multiLineVPadding,
                                multiLineVPadding - singleLineVPadding)) {
                    remeasure = true
                }
            } else {
                val messagePadding = if (isMultiLine) multiLineVPadding else singleLineVPadding
                if (updateViewsWithinLayout(LinearLayout.HORIZONTAL, messagePadding, messagePadding)) {
                    remeasure = true
                }
            }

            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }

        internal fun animateChildrenIn(delay: Int, duration: Int) {
            messageView?.run { viewDoAnimation(this, 0f, 1f, delay, duration) }
            actionView?.run { viewDoAnimation(this, 0f, 1f, delay, duration) }
        }

        internal fun animateChildrenOut(delay: Int, duration: Int) {
            messageView?.run { viewDoAnimation(this, 1f, 0f, delay, duration) }
            actionView?.run { viewDoAnimation(this, 1f, 0f, delay, duration) }
        }

        private fun viewDoAnimation(view: View, startAlpha: Float, endAlpha: Float, delay: Int, duration: Int) {
            view.run {
                if (view is TextView) {
                    alpha = startAlpha
                    ViewCompat.animate(this).alpha(endAlpha).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()
                } else if (view is Button) {
                    if (visibility == View.VISIBLE) {
                        alpha = 1f
                        ViewCompat.animate(this).alpha(0f).setDuration(duration.toLong()).setStartDelay(delay.toLong()).start()
                    }
                }
            }
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            super.onLayout(changed, l, t, r, b)
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener!!.onLayoutChange(this, l, t, r, b)
            }
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener!!.onViewAttachedToWindow(this)
            }

            ViewCompat.requestApplyInsets(this)
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener!!.onViewDetachedFromWindow(this)
            }
        }

        internal fun setOnLayoutChangeListener(onLayoutChangeListener: XSnackbar.SnackbarLayout.OnLayoutChangeListener?) {
            mOnLayoutChangeListener = onLayoutChangeListener
        }

        internal fun setOnAttachStateChangeListener(listener: XSnackbar.SnackbarLayout.OnAttachStateChangeListener) {
            mOnAttachStateChangeListener = listener
        }

        private fun updateViewsWithinLayout(orientation: Int,
                                            messagePadTop: Int, messagePadBottom: Int): Boolean {
            var changed = false
            if (orientation != getOrientation()) {
                setOrientation(orientation)
                changed = true
            }

            if (messageView!!.paddingTop != messagePadTop || messageView!!.paddingBottom != messagePadBottom) {
                updateTopBottomPadding(messageView!!, messagePadTop, messagePadBottom)
                changed = true
            }

            return changed
        }

        private fun updateTopBottomPadding(view: View, topPadding: Int, bottomPadding: Int) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view,
                        ViewCompat.getPaddingStart(view), topPadding,
                        ViewCompat.getPaddingEnd(view), bottomPadding)
            } else {
                view.setPadding(view.paddingLeft, topPadding,
                        view.paddingRight, bottomPadding)
            }
        }
    }

    internal inner class Behavior : SwipeDismissBehavior<SnackbarLayout>() {
        override fun canSwipeDismissView(child: View): Boolean {
            return child is XSnackbar.SnackbarLayout
        }

        override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: XSnackbar.SnackbarLayout,
                                           event: MotionEvent): Boolean {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    // We want to make sure that we disable any Snackbar timeouts if the user is
                    // currently touching the Snackbar. We restore the timeout when complete
                    if (parent.isPointInChildBounds(child, event.x.toInt(), event.y.toInt())) {
                        SnackbarManager.instance.pauseTimeout(mManagerCallback)
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> SnackbarManager.instance.restoreTimeoutIfPaused(mManagerCallback)
            }

            return super.onInterceptTouchEvent(parent, child, event)
        }
    }

    companion object {
        /**
         * Show the Snackbar indefinitely. This means that the Snackbar will be displayed from the time
         * that is [shown][.show] until either it is dismissed, or another Snackbar is shown.

         * @see .setDuration
         */
        const val LENGTH_INDEFINITE = -2

        /**
         * Show the Snackbar for a short period of time.

         * @see .setDuration
         */
        const val LENGTH_SHORT = -1

        /**
         * Show the Snackbar for a long period of time.

         * @see .setDuration
         */
        const val LENGTH_LONG = 0

        internal const val ANIMATION_DURATION = 250
        internal const val ANIMATION_FADE_DURATION = 180

        internal val sHandler: Handler
        internal const val MSG_SHOW = 0
        internal const val MSG_DISMISS = 1

        init {
            sHandler = Handler(Looper.getMainLooper(), Handler.Callback { message ->
                when (message.what) {
                    MSG_SHOW -> {
                        (message.obj as XSnackbar).showView()
                        return@Callback true
                    }

                    MSG_DISMISS -> {
                        (message.obj as XSnackbar).hideView(message.arg1)
                        return@Callback true
                    }
                }

                false
            })
        }

        /**
         * Make a Snackbar to display a message
         *
         *
         *
         * Snackbar will try and find a parent view to hold Snackbar's view from the value given
         * to `view`. Snackbar will walk up the view tree trying to find a suitable parent,
         * which is defined as a [CoordinatorLayout] or the window decor's content view,
         * whichever comes first.
         *
         *
         *
         * Having a [CoordinatorLayout] in your view hierarchy allows Snackbar to enable
         * certain features, such as swipe-to-dismiss and automatically moving of widgets like
         * [FloatingActionButton].

         * @param view     The view to find a parent from.
         * *
         * @param text     The text to show.  Can be formatted text.
         * *
         * @param duration How long to display the message.  Either [.LENGTH_SHORT] or [                 ][.LENGTH_LONG]
         */
        fun make(view: View, text: CharSequence,
                 @Duration duration: Int, isShowInTop: Boolean): XSnackbar {
            val snackBar = XSnackbar(findSuitableParent(view), isShowInTop)
            snackBar.setText(text)
            snackBar.setDuration(duration)
            snackBar.isShowInTop = isShowInTop
            return snackBar
        }

        /**
         * Make a Snackbar to display a message.
         *
         *
         *
         * Snackbar will try and find a parent view to hold Snackbar's view from the value given
         * to `view`. Snackbar will walk up the view tree trying to find a suitable parent,
         * which is defined as a [CoordinatorLayout] or the window decor's content view,
         * whichever comes first.
         *
         *
         *
         * Having a [CoordinatorLayout] in your view hierarchy allows Snackbar to enable
         * certain features, such as swipe-to-dismiss and automatically moving of widgets like
         * [FloatingActionButton].

         * @param view     The view to find a parent from.
         * *
         * @param resId    The resource id of the string resource to use. Can be formatted text.
         * *
         * @param duration How long to display the message.  Either [.LENGTH_SHORT] or [                 ][.LENGTH_LONG]
         */
        fun make(view: View, @StringRes resId: Int, @Duration duration: Int, isShowInTop: Boolean): XSnackbar {
            return make(view, view.resources.getText(resId), duration, isShowInTop)
        }

        private fun findSuitableParent(viewIn: View?): ViewGroup {
            var view = viewIn
            var fallback: ViewGroup? = null
            do {
                if (view is CoordinatorLayout) {
                    // We've found a CoordinatorLayout, use it
                    return view
                } else if (view is FrameLayout) {
                    if (view.id == android.R.id.content) {
                        // If we've hit the decor content view, then we didn't find a CoL in the
                        // hierarchy, so use it.
                        return view
                    } else {
                        // It's not the content view but we'll use it as our fallback
                        fallback = view
                    }
                }

                if (view != null) {
                    // Else, we will loop and crawl up the view hierarchy and try to find a parent
                    val parent = view.parent
                    view = if (parent is View) parent else null
                }
            } while (view != null)

            // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
            return fallback!!
        }
    }
}

