package com.android.db.swipemenulayout

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Scroller
import java.util.*

/**
 * Swipe menu layout
 *
 * Created by DengBo on 14/03/2018.
 */

open class SwipeMenuLayout: ViewGroup {

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mViewCache: SwipeMenuLayout? = null
        private var mStateCache: MenuState? = null
    }

    /**
     * The state of menu
     */
    enum class MenuState {
        LEFT_OPEN,
        RIGHT_OPEN,
        CLOSE
    }

    private val mMatchParentChildren = ArrayList<View>(1)
    private var mContentViewLp: ViewGroup.MarginLayoutParams? = null
    private var mScroller: Scroller? = null
    private var mContentView: View? = null
    private var mRightView: View? = null
    private var mLeftView: View? = null

    private var mFirstP: PointF? = null
    private var mLastP: PointF? = null

    private var mContentViewResID = 0
    private var mRightViewResID = 0
    private var mLeftViewResID = 0

    private var finallyDistanceX = 0f
    private var mScaledTouchSlop = 0
    private var mFraction = 0.5f
    private var distanceX = 0f

    private var mCanRightSwipe = true
    private var mCanLeftSwipe = true
    private var isSwiping = false

    ////////////////
    // constructor
    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        init(context, attrs, defStyleAttr)
    }

    /**
     * Initial
     */
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        mScaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
        mScroller = Scroller(context)

        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SwipeMenuLayout, defStyleAttr, 0)

        try {
            (0 until typedArray.indexCount).forEach { i ->
                when (typedArray.getIndex(i)) {
                    R.styleable.SwipeMenuLayout_leftView -> {
                        mLeftViewResID = typedArray.getResourceId(R.styleable.SwipeMenuLayout_leftView, -1)
                    }
                    R.styleable.SwipeMenuLayout_rightView -> {
                        mRightViewResID = typedArray.getResourceId(R.styleable.SwipeMenuLayout_rightView, -1)
                    }
                    R.styleable.SwipeMenuLayout_contentView -> {
                        mContentViewResID = typedArray.getResourceId(R.styleable.SwipeMenuLayout_contentView, -1)
                    }
                    R.styleable.SwipeMenuLayout_canLeftSwipe -> {
                        mCanLeftSwipe = typedArray.getBoolean(R.styleable.SwipeMenuLayout_canLeftSwipe, true)
                    }
                    R.styleable.SwipeMenuLayout_canRightSwipe -> {
                        mCanRightSwipe = typedArray.getBoolean(R.styleable.SwipeMenuLayout_canRightSwipe, true)
                    }
                    R.styleable.SwipeMenuLayout_fraction -> {
                        mFraction = typedArray.getFloat(R.styleable.SwipeMenuLayout_fraction, 0.5f)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        isClickable = true
        var count = childCount
        val measureMatchParentChildren = View.MeasureSpec.getMode(widthMeasureSpec) != View.MeasureSpec.EXACTLY || View.MeasureSpec.getMode(heightMeasureSpec) != View.MeasureSpec.EXACTLY
        mMatchParentChildren.clear()
        var maxWidth = 0
        var maxHeight = 0
        var childState = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                val lp = child.layoutParams as ViewGroup.MarginLayoutParams
                maxWidth = Math.max(maxWidth, child.measuredWidth + lp.leftMargin + lp.rightMargin)
                maxHeight = Math.max(maxHeight, child.measuredHeight + lp.topMargin + lp.bottomMargin)
                childState = View.combineMeasuredStates(childState, child.measuredState)
                if (measureMatchParentChildren) {
                    if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT || lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child)
                    }
                }
            }
        }
        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, suggestedMinimumHeight)
        maxWidth = Math.max(maxWidth, suggestedMinimumWidth)
        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState), View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl View.MEASURED_HEIGHT_STATE_SHIFT))

        count = mMatchParentChildren.size
        if (count > 1) {
            for (i in 0 until count) {
                val child = mMatchParentChildren[i]
                val lp = child.layoutParams as ViewGroup.MarginLayoutParams

                val childWidthMeasureSpec = if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                    val width = Math.max(0, measuredWidth - lp.leftMargin - lp.rightMargin)
                    View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                } else {
                    ViewGroup.getChildMeasureSpec(widthMeasureSpec, lp.leftMargin + lp.rightMargin, lp.width)
                }

                val childHeightMeasureSpec = if (lp.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                    val height = Math.max(0, measuredHeight - lp.topMargin - lp.bottomMargin)
                    View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
                } else {
                    ViewGroup.getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height)
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return ViewGroup.MarginLayoutParams(context, attrs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val left = paddingLeft
        val top = paddingTop

        for (i in 0 until count) {
            val child = getChildAt(i)
            if (mLeftView == null && child.id == mLeftViewResID) {
                mLeftView = child
                mLeftView?.isClickable = true
            } else if (mRightView == null && child.id == mRightViewResID) {
                mRightView = child
                mRightView?.isClickable = true
            } else if (mContentView == null && child.id == mContentViewResID) {
                mContentView = child
                mContentView?.isClickable = true
            }
        }

        mContentView?.apply {
            var cTop = top
            var cLeft = left
            var cRight = 0
            var cBottom = 0
            val measuredWidth = measuredWidth
            val measuredHeight = measuredHeight
            mContentViewLp = (layoutParams as ViewGroup.MarginLayoutParams).apply {
                cTop += topMargin
                cLeft += leftMargin
                cRight = left + leftMargin + measuredWidth
                cBottom = cTop + measuredHeight
            }
            layout(cLeft, cTop, cRight, cBottom)
        }
        mLeftView?.apply {
            var cTop = top
            var cLeft = 0
            var cRight = 0
            var cBottom = 0
            val measuredWidth = measuredWidth
            val measuredHeight = measuredHeight
            (layoutParams as ViewGroup.MarginLayoutParams).apply {
                cTop += topMargin
                cLeft = 0 - measuredWidth + leftMargin + rightMargin
                cRight = 0 - rightMargin
                cBottom = cTop + measuredHeight
            }
            layout(cLeft, cTop, cRight, cBottom)
        }
        mRightView?.apply {
            var cTop = top
            var cLeft = 0
            var cRight = 0
            var cBottom = 0
            val measuredWidth = measuredWidth
            val measuredHeight = measuredHeight
            val contentViewRight = mContentView?.right ?: ViewGroup.LayoutParams.MATCH_PARENT
            val contentViewRightMargin = mContentViewLp?.rightMargin ?: 0
            (layoutParams as ViewGroup.MarginLayoutParams).apply {
                cTop += topMargin
                cLeft = contentViewRight + contentViewRightMargin + leftMargin
                cRight = cLeft + measuredWidth
                cBottom = cTop + measuredHeight
            }
            layout(cLeft, cTop, cRight, cBottom)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                isSwiping = false

                if (mLastP == null) {
                    mLastP = PointF()
                }
                mLastP?.set(ev.rawX, ev.rawY)

                if (mFirstP == null) {
                    mFirstP = PointF()
                }
                mFirstP?.set(ev.rawX, ev.rawY)

                mViewCache?.apply {
                    if (this != this@SwipeMenuLayout) {
                        mViewCache?.handleSwipeMenu(MenuState.CLOSE)
                    }
                    parent.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val distanceX = (mLastP?.x ?: 0f) - ev.rawX
                val distanceY = (mLastP?.y ?: 0f) - ev.rawY
                if (Math.abs(distanceY) > mScaledTouchSlop && Math.abs(distanceY) > Math.abs(distanceX)) {
                    return super.dispatchTouchEvent(ev)
                }

                scrollBy(distanceX.toInt(), 0)

                if (scrollX < 0) {
                    if (!mCanRightSwipe || mLeftView == null) {
                        scrollTo(0, 0)
                    } else {//left
                        if (scrollX < (mLeftView?.left ?: 0)) {
                            scrollTo((mLeftView?.left ?: 0), 0)
                        }
                    }
                } else if (scrollX > 0) {
                    if (!mCanLeftSwipe || mRightView == null) {
                        scrollTo(0, 0)
                    } else {
                        if (scrollX > (mRightView?.right ?: 0) - (mContentView?.right ?: 0) - (mContentViewLp?.rightMargin ?: 0)) {
                            scrollTo((mRightView?.right ?: 0) - (mContentView?.right ?: 0) - (mContentViewLp?.rightMargin ?: 0), 0)
                        }
                    }
                }

                if (Math.abs(distanceX) > mScaledTouchSlop) {
                    parent.requestDisallowInterceptTouchEvent(true)
                }
                mLastP?.set(ev.rawX, ev.rawY)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                finallyDistanceX = (mFirstP?.x ?: 0f) - ev.rawX
                if (Math.abs(finallyDistanceX) > mScaledTouchSlop) {
                    isSwiping = true
                }
                handleSwipeMenu(isShouldOpen())
            }
            else -> {
            }
        }

        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {
                if (Math.abs(finallyDistanceX) > mScaledTouchSlop) {
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isSwiping) {
                    isSwiping = false
                    finallyDistanceX = 0f
                    return true
                }
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    /**
     * Handle Swipe state and invalidate view with specified[MenuState]
     * @param result The state of this
     */
    private fun handleSwipeMenu(result: MenuState) {
        when (result) {
            MenuState.LEFT_OPEN -> {
                mScroller?.startScroll(scrollX, 0, (mLeftView?.left ?: 0) - scrollX, 0)
                mViewCache = this
                mStateCache = result
            }
            MenuState.RIGHT_OPEN -> {
                mViewCache = this
                mScroller?.startScroll(scrollX, 0, (mRightView?.right ?: 0) - (mContentView?.right ?: 0) - (mContentViewLp?.rightMargin ?: 0) - scrollX, 0)
                mStateCache = result
            }
            else -> {
                mScroller?.startScroll(scrollX, 0, -scrollX, 0)
                mViewCache = null
                mStateCache = null
            }
        }
        invalidate()
    }

    override fun computeScroll() {
        mScroller?.apply {
            if (computeScrollOffset()) {
                scrollTo(currX, currY)
                invalidate()
            }
        }
    }

    /**
     * Return the state of menu
     * @return state
     */
    private fun isShouldOpen(): MenuState {
        if (mScaledTouchSlop >= Math.abs(finallyDistanceX)) {
            return mStateCache ?: MenuState.CLOSE
        }

        if (finallyDistanceX < 0) {
            if (scrollX < 0 && mLeftView != null) {
                if (Math.abs((mLeftView?.width ?: 0) * mFraction) < Math.abs(scrollX)) {
                    return MenuState.LEFT_OPEN
                }
            }
            if (scrollX > 0 && mRightView != null) {
                return MenuState.CLOSE
            }
        } else if (finallyDistanceX > 0) {
            if (scrollX > 0 && mRightView != null) {
                if (Math.abs((mRightView?.width ?: 0) * mFraction) < Math.abs(scrollX)) {
                    return MenuState.RIGHT_OPEN
                }
            }
            if (scrollX < 0 && mLeftView != null) {
                return MenuState.CLOSE
            }
        }

        return MenuState.CLOSE
    }

    override fun onDetachedFromWindow() {
        mViewCache?.handleSwipeMenu(MenuState.CLOSE)
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mViewCache?.handleSwipeMenu(mStateCache ?: MenuState.CLOSE)
    }

    /**
     *
     */
    fun resetStatus() {
        if (mViewCache != null) {
            if (mStateCache != null && mStateCache != MenuState.CLOSE && mScroller != null) {
                mScroller?.startScroll((mViewCache?.scrollX ?: 0), 0, -(mViewCache?.scrollX ?: 0), 0)
                mViewCache?.invalidate()
                mViewCache = null
                mStateCache = null
            }
        }
    }

    fun getFraction(): Float { return mFraction }

    fun setFraction(mFraction: Float) { this.mFraction = mFraction }

    fun isCanLeftSwipe(): Boolean { return mCanLeftSwipe }

    fun setCanLeftSwipe(mCanLeftSwipe: Boolean) { this.mCanLeftSwipe = mCanLeftSwipe }

    fun isCanRightSwipe(): Boolean { return mCanRightSwipe }

    fun setCanRightSwipe(mCanRightSwipe: Boolean) { this.mCanRightSwipe = mCanRightSwipe }

    fun getViewCache(): SwipeMenuLayout? { return mViewCache }

    fun getStateCache(): MenuState? { return mStateCache }

    private fun isLeftToRight(): Boolean { return distanceX < 0 }
}
