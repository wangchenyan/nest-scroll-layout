package me.wcy.nest_scroll_layout

import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.OverScroller

/**
 * Created by wangchenyan on 2018/9/6.
 */
class NestScrollLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), NestedScrollingParent {
    private var headerView: View? = null
    private var contentView: View? = null
    private val scroller = OverScroller(context)
    private var animator: ValueAnimator? = null

    private var onScrollListener: OnScrollListener? = null
    private var headerRemainHeight: Int
    private var isSticky: Boolean

    interface OnScrollListener {
        fun onScroll(scrollY: Int, dy: Int, percent: Float)
    }

    init {
        orientation = VERTICAL
        val ta = context.obtainStyledAttributes(attrs, R.styleable.NestScrollLayout)
        headerRemainHeight = ta.getDimensionPixelOffset(R.styleable.NestScrollLayout_nslHeaderRemainHeight, 0)
        isSticky = ta.getBoolean(R.styleable.NestScrollLayout_snlIsSticky, true)
        ta.recycle()
    }

    fun setOnScrollListener(listener: OnScrollListener) {
        onScrollListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 2) {
            throw IllegalStateException("NestScrollLayout should have two child")
        }
        headerView = getChildAt(0)
        contentView = getChildAt(1)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        headerView!!.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
        val lp = contentView!!.layoutParams
        lp.height = measuredHeight - headerRemainHeight
        setMeasuredDimension(measuredWidth, measuredHeight + getMaxScrollY())
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int): Boolean {
        stopStickyAnimator()
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        val consume = (dy > 0 && scrollY < getMaxScrollY())
                || (dy < 0 && scrollY > 0 && !target.canScrollVertically(-1))
        if (consume) {
            scrollBy(0, dy)
            consumed[1] = dy
        }
    }

    override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
        if (isSticky || scrollY >= getMaxScrollY()) {
            return false
        }
        fling(velocityY.toInt())
        return true
    }

    private fun fling(velocityY: Int) {
        scroller.fling(0, scrollY, 0, velocityY, 0, 0, 0, getMaxScrollY())
        invalidate()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(0, scroller.currY)
            invalidate()
        }
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        val scrollY = t
        val dy = t - oldt
        val percent = scrollY.toFloat() / getMaxScrollY()
        onScrollListener?.onScroll(scrollY, dy, percent)
    }

    override fun scrollTo(x: Int, y: Int) {
        var y1 = Math.max(y, 0)
        y1 = Math.min(y1, getMaxScrollY())
        if (y1 != scrollY) {
            super.scrollTo(x, y1)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev?.action == MotionEvent.ACTION_UP || ev?.action == MotionEvent.ACTION_CANCEL) {
            if (isSticky) {
                stopStickyAnimator()
                startStickyAnimator()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun startStickyAnimator() {
        if (scrollY == 0 || scrollY == getMaxScrollY()) {
            return
        }
        animator = if (scrollY >= getMaxScrollY() / 2) {
            ValueAnimator.ofInt(scrollY, getMaxScrollY())
        } else {
            ValueAnimator.ofInt(scrollY, 0)
        }
        animator!!.addUpdateListener {
            scrollTo(0, it.animatedValue as Int)
            invalidate()
        }
        animator!!.interpolator = DecelerateInterpolator()
        animator!!.duration = 150
        animator!!.start()
    }

    private fun stopStickyAnimator() {
        if (animator != null && animator!!.isRunning) {
            animator?.cancel()
        }
    }

    private fun getMaxScrollY(): Int {
        return headerView!!.measuredHeight - headerRemainHeight
    }
}