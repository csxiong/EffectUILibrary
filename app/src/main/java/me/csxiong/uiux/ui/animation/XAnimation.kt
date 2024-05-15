package me.csxiong.uiux.ui.animation

import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.annotation.FloatRange
import me.csxiong.library.utils.DeviceUtils
import me.csxiong.library.utils.XAnimator
import me.csxiong.uiux.utils.ViewUtils

/**
 * 动画过渡
 */
fun View.animationTransition(duration: Long = 300, interpolator: Interpolator = AccelerateDecelerateInterpolator(), content: View.() -> Unit): XAnimationTransition {
    //可取消动画 优先取消动画
    tag?.takeIf { it is XAnimationTransition }?.let { (it as XAnimationTransition).cancel() }
    val transition = XAnimationTransition(this, duration, interpolator)
    transition.pose()
    this.apply(content)
    transition.snapShoot()
    transition.start()
    return transition
}

/**
 * @Description :
 * @Author : bear
 * @Date : 2021/12/29
 */
class XAnimationTransition(val rootView: View, val duration: Long, val interpolator: Interpolator) : View.OnAttachStateChangeListener {

    var onTransitionUpdate: ((fraction: Float) -> Unit)? = null

    var onTransitionStart: (() -> Unit)? = null

    var onTransitionEnd: (() -> Unit)? = null

    val engineAnimator = XAnimator.ofFloat(0f, 1f)
        .duration(duration)
        .interpolator(interpolator)
        .setAnimationListener(object : XAnimator.XAnimationListener {
            override fun onAnimationUpdate(fraction: Float, value: Float) {
                transitionFraction(fraction)
                onTransitionUpdate?.invoke(fraction)
                rootView.postInvalidate()
            }

            override fun onAnimationStart(animation: XAnimator?) {
                onTransitionStart?.invoke()
            }

            override fun onAnimationEnd(animation: XAnimator?) {
                onTransitionEnd?.invoke()
                cancel()
            }

            override fun onAnimationCancel(animation: XAnimator?) {
            }
        })

    /**
     * 所有组织内部的动画
     */
    val animationCaptures = ArrayList<XAnimationCapture>()

    fun pose(rootView: View = this.rootView) {
        when {
            rootView is ViewGroup -> {
                animationCaptures.add(XAnimationCapture(rootView))
                //继续遍历
                for (index in 0..rootView.childCount) {
                    rootView.getChildAt(index)?.let { pose(it) }
                }
            }
            else -> animationCaptures.add(XAnimationCapture(rootView))
        }

    }

    private fun transitionFraction(fraction: Float) {
        animationCaptures.forEach { it.transitionFraction(fraction) }
    }

    fun snapShoot() {
        animationCaptures.forEach { it.snapShoot() }
    }

    /**
     * 开启attachWindow状态 如果detach 直接对rootView transition取消
     */
    fun start() {
        //定位到pose状态
        //保存在tag中
        rootView.tag = this
        rootView.addOnAttachStateChangeListener(this)
        transitionFraction(0.0f)
        engineAnimator.cancel()
        engineAnimator.start()
    }

    /**
     * 直接取消所有动画 解绑
     */
    fun cancel() {
        rootView.removeOnAttachStateChangeListener(this)
        engineAnimator.cancel()
        rootView.tag = null
    }

    override fun onViewAttachedToWindow(v: View?) {
        //无操作
    }

    override fun onViewDetachedFromWindow(v: View?) {
        //更安全
        if (v == rootView) {
            rootView.tag?.takeIf { it is XAnimationTransition }?.let {
                it as XAnimationTransition
                it.cancel()
            }
        }
    }

}

/**
 * 动画照片
 */
class XAnimationCapture(val target: View) {

    companion object {
        const val WIDTH = "width"
        const val HEIGHT = "height"
        const val TRANSITION_X = "transitionX"
        const val TRANSITION_Y = "transitionY"
        const val ALPHA = "alpha"
        const val SCALEX = "scaleX"
        const val SCALEY = "scaleY"
        const val MARGIN_LEFT = "marginLeft"
        const val MARGIN_TOP = "marginTop"
        const val MARGIN_RIGHT = "marginRight"
        const val MARGIN_BOTTOM = "marginBottom"
        const val ROTATION = "rotation"
        const val ROTATION_X = "rotationX"
        const val ROTATION_Y = "rotationY"

        val animationKeys = arrayListOf(
            WIDTH,
            HEIGHT,
            TRANSITION_X, TRANSITION_Y,
            ALPHA,
            SCALEX, SCALEY,
            MARGIN_LEFT, MARGIN_TOP, MARGIN_RIGHT, MARGIN_BOTTOM,
            ROTATION, ROTATION_X, ROTATION_Y
        )
    }

    val animationMap = HashMap<String, XAnimatorFraction?>()

    init {
        pose()
    }

    /**
     * 原始姿势
     */
    fun pose() {
        animationKeys.forEach {
            when (it) {
                WIDTH -> {
                    val width = if (target.layoutParams?.width == ViewGroup.LayoutParams.MATCH_PARENT || target.layoutParams?.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        target.measuredWidth.toFloat()
                    } else {
                        target.layoutParams?.width?.toFloat() ?: target.measuredWidth.toFloat()
                    }
                    animationMap[WIDTH] = XAnimatorFraction(width)
                }
                HEIGHT -> {
                    val height = if (target.layoutParams?.height == ViewGroup.LayoutParams.MATCH_PARENT || target.layoutParams?.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        target.measuredHeight.toFloat()
                    } else {
                        target.layoutParams?.height?.toFloat() ?: target.measuredHeight.toFloat()
                    }
                    animationMap[HEIGHT] = XAnimatorFraction(height)
                }
                TRANSITION_X -> animationMap[TRANSITION_X] = XAnimatorFraction(target.translationX)
                TRANSITION_Y -> animationMap[TRANSITION_Y] = XAnimatorFraction(target.translationY)
                ALPHA -> animationMap[ALPHA] = XAnimatorFraction(target.alpha)
                SCALEX -> animationMap[SCALEX] = XAnimatorFraction(target.scaleX)
                SCALEY -> animationMap[SCALEY] = XAnimatorFraction(target.scaleY)
                MARGIN_LEFT -> animationMap[MARGIN_LEFT] = XAnimatorFraction(target.marginLeft.toFloat())
                MARGIN_TOP -> animationMap[MARGIN_TOP] = XAnimatorFraction(target.marginTop.toFloat())
                MARGIN_RIGHT -> animationMap[MARGIN_RIGHT] = XAnimatorFraction(target.marginRight.toFloat())
                MARGIN_BOTTOM -> animationMap[MARGIN_BOTTOM] = XAnimatorFraction(target.marginBottom.toFloat())
                ROTATION -> animationMap[ROTATION] = XAnimatorFraction(target.rotation)
                ROTATION_X -> animationMap[ROTATION_X] = XAnimatorFraction(target.rotationX)
                ROTATION_Y -> animationMap[ROTATION_Y] = XAnimatorFraction(target.rotationY)
            }
        }
    }

    /**
     * 快照
     */
    fun snapShoot() {
        animationKeys.forEach {
            when (it) {
                WIDTH -> {
                    var width: Float = if (target.layoutParams?.width == ViewGroup.LayoutParams.MATCH_PARENT || target.layoutParams?.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        target.measuredWidth.toFloat()
                    } else {
                        target.layoutParams?.width?.toFloat() ?: target.measuredWidth.toFloat()
                    }
                    if (animationMap[WIDTH]?.getValue() == width) {
                        animationMap[WIDTH] = null
                    } else {
                        animationMap[WIDTH]?.to(width)
                    }
                }
                HEIGHT -> {
                    val height: Float = if (target.layoutParams?.height == ViewGroup.LayoutParams.MATCH_PARENT || target.layoutParams?.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                        target.measuredHeight.toFloat()
                    } else {
                        target.layoutParams?.height?.toFloat() ?: target.measuredHeight.toFloat()
                    }
                    if (animationMap[HEIGHT]?.getValue() == height) {
                        animationMap[HEIGHT] = null
                    } else {
                        animationMap[HEIGHT]?.to(height)
                    }
                }
                TRANSITION_X -> {
                    if (animationMap[TRANSITION_X]?.getValue() == target.translationX) {
                        animationMap[TRANSITION_X] = null
                    } else {
                        animationMap[TRANSITION_X]?.to(target.translationX)
                    }
                }
                TRANSITION_Y -> {
                    if (animationMap[TRANSITION_Y]?.getValue() == target.translationY) {
                        animationMap[TRANSITION_Y] = null
                    } else {
                        animationMap[TRANSITION_Y]?.to(target.translationY)
                    }
                }
                ALPHA -> {
                    if (animationMap[ALPHA]?.getValue() == target.alpha) {
                        animationMap[ALPHA] = null
                    } else {
                        animationMap[ALPHA]?.to(target.alpha)
                    }
                }
                SCALEX -> {
                    if (animationMap[SCALEX]?.getValue() == target.scaleX) {
                        animationMap[SCALEX] = null
                    } else {
                        animationMap[SCALEX]?.to(target.scaleX)
                    }
                }
                SCALEY -> {
                    if (animationMap[SCALEY]?.getValue() == target.scaleY) {
                        animationMap[SCALEY] = null
                    } else {
                        animationMap[SCALEY]?.to(target.scaleY)
                    }
                }
                MARGIN_LEFT -> {
                    if (animationMap[MARGIN_LEFT]?.getValue() == target.marginLeft.toFloat()) {
                        animationMap[MARGIN_LEFT] = null
                    } else {
                        animationMap[MARGIN_LEFT]?.to(target.marginLeft.toFloat())
                    }
                }
                MARGIN_TOP -> {
                    if (animationMap[MARGIN_TOP]?.getValue() == target.marginTop.toFloat()) {
                        animationMap[MARGIN_TOP] = null
                    } else {
                        animationMap[MARGIN_TOP]?.to(target.marginTop.toFloat())
                    }
                }
                MARGIN_RIGHT -> {
                    if (animationMap[MARGIN_RIGHT]?.getValue() == target.marginRight.toFloat()) {
                        animationMap[MARGIN_RIGHT] = null
                    } else {
                        animationMap[MARGIN_RIGHT]?.to(target.marginRight.toFloat())
                    }
                }
                MARGIN_BOTTOM -> {
                    if (animationMap[MARGIN_BOTTOM]?.getValue() == target.marginBottom.toFloat()) {
                        animationMap[MARGIN_BOTTOM] = null
                    } else {
                        animationMap[MARGIN_BOTTOM]?.to(target.marginBottom.toFloat())
                    }
                }
                ROTATION -> {
                    if (animationMap[ROTATION]?.getValue() == target.rotation) {
                        animationMap[ROTATION] = null
                    } else {
                        animationMap[ROTATION]?.to(target.rotation)
                    }
                }
                ROTATION_X -> {
                    if (animationMap[ROTATION_X]?.getValue() == target.rotationX) {
                        animationMap[ROTATION_X] = null
                    } else {
                        animationMap[ROTATION_X]?.to(target.rotationX)
                    }
                }
                ROTATION_Y -> {
                    if (animationMap[ROTATION_Y]?.getValue() == target.rotationY) {
                        animationMap[ROTATION_Y] = null
                    } else {
                        animationMap[ROTATION_Y]?.to(target.rotationY)
                    }
                }
            }
        }
    }

    /**
     * 过渡因子
     */
    fun transitionFraction(@FloatRange(from = 0.0, to = 1.0) fraction: Float) {
        animationKeys.forEach {
            when (it) {
                WIDTH -> animationMap[it]?.let { ViewUtils.setWidth(target, it.calculateValue(fraction).toInt()) }
                HEIGHT -> animationMap[it]?.let { ViewUtils.setHeight(target, it.calculateValue(fraction).toInt()) }
                TRANSITION_X -> animationMap[it]?.let { target.translationX = it.calculateValue(fraction) }
                TRANSITION_Y -> animationMap[it]?.let { target.translationY = it.calculateValue(fraction) }
                ALPHA -> animationMap[it]?.let { target.alpha = it.calculateValue(fraction) }
                SCALEX -> animationMap[it]?.let { target.scaleX = it.calculateValue(fraction) }
                SCALEY -> animationMap[it]?.let { target.scaleY = it.calculateValue(fraction) }
                MARGIN_LEFT -> animationMap[it]?.let { ViewUtils.setMarginLeft(target, it.calculateValue(fraction).toInt()) }
                MARGIN_TOP -> animationMap[it]?.let { ViewUtils.setMarginTop(target, it.calculateValue(fraction).toInt()) }
                MARGIN_RIGHT -> animationMap[it]?.let { ViewUtils.setMarginRight(target, it.calculateValue(fraction).toInt()) }
                MARGIN_BOTTOM -> animationMap[it]?.let { ViewUtils.setMarginBottom(target, it.calculateValue(fraction).toInt()) }
                ROTATION -> animationMap[it]?.let { target.rotation = it.calculateValue(fraction) }
                ROTATION_X -> animationMap[it]?.let { target.rotationX = it.calculateValue(fraction) }
                ROTATION_Y -> animationMap[it]?.let { target.rotationY = it.calculateValue(fraction) }
            }
        }
    }
}

/**
 * @Desc : 数值因子
 * @Author : Bear - 2020/5/6
 */
class XAnimatorFraction {

    /**
     * 针对这种不需要动态计算的方式建立的初始值
     *
     * @param startValue
     */
    constructor(startValue: Float) {
        this.startValue = startValue
        value = startValue
        endValue = startValue
    }

    /**
     * 针对这种不需要动态计算的方式建立的初始值
     *
     * @param startValue
     */
    constructor(startValue: Float, endValue: Float) {
        this.startValue = startValue
        value = startValue
        this.endValue = endValue
    }

    /**
     * 计算开始值
     */
    var startValue = 0f

    /**
     * 计算末尾值
     */
    var endValue = 0f

    /**
     * 计算当前值
     */
    private var value = 0f

    /**
     * 开始因子，如果因子小于开始因子，value等于startValue
     */
    var startFaction = 0f

    /**
     * 结束因子，如果因子大于结束因子，value等于endValue
     */
    var endFaction = 1f

    /**
     * 获取当前值
     *
     * @return
     */
    fun getValue(): Float {
        return value
    }

    fun setValue(value: Float) {
        this.value = value
        startValue = value
        endValue = value
    }

    fun setFactionBound(start: Float, end: Float) {
        startFaction = start
        endFaction = end
    }

    /**
     * 直接使用currentValue作为开始值
     *
     * @param endValue
     */
    fun to(endValue: Float) {
        startValue = value
        this.endValue = endValue
    }

    /**
     * 直接标记开始末尾值
     *
     * @param startValue
     * @param endValue
     */
    fun mark(startValue: Float, endValue: Float) {
        this.startValue = startValue
        this.endValue = endValue
    }

    /**
     * 动画因子计算
     *
     * @param fraction
     * @return
     */
    fun calculateValue(fraction: Float): Float {
        var fraction = fraction
        if (fraction < startFaction) {
            value = startValue
        } else if (fraction > endFaction) {
            value = endValue
        } else {
            fraction = (fraction - startFaction) / (endFaction - startFaction)
            value = startValue + (endValue - startValue) * fraction
        }
        return value
    }

    /**
     * 倍速计算动画因子计算
     *
     * @param fraction
     * @param multiSpeed
     * @return
     */
    fun calculateValue(fraction: Float, multiSpeed: Float): Float {
        var fraction = fraction
        fraction *= multiSpeed
        if (fraction >= 1.0f) {
            fraction = 1.0f
        }
        value = calculateValue(fraction)
        return value
    }

    /**
     * 切换开始结束
     */
    fun exchange() {
        val _endValue = startValue
        startValue = endValue
        endValue = _endValue
    }

    override fun toString(): String {
        return "XAnimatorCaculateValuer{" +
                "startValue=" + startValue +
                ", endValue=" + endValue +
                ", value=" + value +
                '}'
    }

    companion object {
        /**
         * 限制value
         *
         * @param value
         * @param min
         * @param max
         * @return
         */
        fun limit(value: Float, min: Float, max: Float): Float {
            if (value < min) {
                return min
            } else if (value > max) {
                return max
            }
            return value
        }
    }
}


/**
 * Returns the left margin if this view's [LayoutParams] is a [ViewGroup.MarginLayoutParams],
 * otherwise 0.
 *
 * @see ViewGroup.MarginLayoutParams
 */
inline val View.marginLeft: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0

/**
 * Returns the top margin if this view's [LayoutParams] is a [ViewGroup.MarginLayoutParams],
 * otherwise 0.
 *
 * @see ViewGroup.MarginLayoutParams
 */
inline val View.marginTop: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0

/**
 * Returns the right margin if this view's [LayoutParams] is a [ViewGroup.MarginLayoutParams],
 * otherwise 0.
 *
 * @see ViewGroup.MarginLayoutParams
 */
inline val View.marginRight: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0

/**
 * Returns the bottom margin if this view's [LayoutParams] is a [ViewGroup.MarginLayoutParams],
 * otherwise 0.
 *
 * @see ViewGroup.MarginLayoutParams
 */
inline val View.marginBottom: Int
    get() = (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
