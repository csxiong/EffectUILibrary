package me.csxiong.uiux.ui.video

import android.graphics.Rect
import android.view.ViewGroup
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import me.csxiong.library.utils.XAnimator
import me.csxiong.uiux.ui.animation.XAnimatorFraction
import me.csxiong.uiux.ui.video.view.XVideoContainer
import me.csxiong.uiux.utils.ViewUtils

/**
 * @Desc : 视频全屏动画 整体封装
 * @Author : meitu - 2021/9/4
 *
 * 目前视频的这种全屏播放思路是rootView中做整体的旋转 不是Android真实的旋转屏幕事件的方案 这种方案旋转效果比较统一
 * 弊端就是 用户的状态栏还是在原垂直方向
 */
class FullScreenTransitionAnimation(val originContainer: XVideoContainer) {

    private val rootContent: ViewGroup = originContainer.rootView.findViewById(Window.ID_ANDROID_CONTENT)
    private val animationContainer = XVideoContainer(rootContent.context)

    private val originVisibleRect = Rect()
    private val fullRect = Rect()

    private val marginTopValuer = XAnimatorFraction(0f)
    private val marginLeftValuer = XAnimatorFraction(0f)
    private val widthValuer = XAnimatorFraction(0f)
    private val heightValuer = XAnimatorFraction(0f)
    private val rotateValuer = XAnimatorFraction(0f)
    private val translateXValuer = XAnimatorFraction(0f)

    private var animator: XAnimator? = null

    private var isHorizontal = false

    init {
        rootContent.getGlobalVisibleRect(fullRect)
        originContainer.getGlobalVisibleRect(originVisibleRect)
        animationContainer.pivotX = 0f
        animationContainer.pivotY = 0f

        val videoWidth = originContainer.videoRenderView?.player?.videoWidth ?: 0
        val videoHeight = originContainer.videoRenderView?.player?.videoHeight ?: 0
        isHorizontal = videoWidth > videoHeight
        if (isHorizontal) {
            rotateValuer.setValue(0f)
        }
        marginTopValuer.setValue(originVisibleRect.top.toFloat())
        marginLeftValuer.setValue( originVisibleRect.left.toFloat())
        widthValuer.setValue(originContainer.width.toFloat())
        heightValuer.setValue(originContainer.height.toFloat())
        translateXValuer.setValue(0f)
    }

    fun enter() {
        rootContent.addView(animationContainer, FrameLayout.LayoutParams(originContainer.width, originContainer.height).apply {
            topMargin = originVisibleRect.top
            leftMargin = originVisibleRect.left
        })
        XPlayer.exchangeVideoContainer(originContainer, animationContainer)
        animator?.cancel()
        marginLeftValuer.to(0f)
        marginTopValuer.to(0f)
        rotateValuer.to(if (isHorizontal) 90f else 0f)
        widthValuer.to(if (isHorizontal) fullRect.height().toFloat() else fullRect.width().toFloat())
        heightValuer.to(if (isHorizontal) fullRect.width().toFloat() else fullRect.height().toFloat())
        translateXValuer.to(if (isHorizontal) fullRect.width().toFloat() else 0f)
        animator = XAnimator.ofFloat(0f, 1f)
            .duration(350)
            .interpolator(AccelerateDecelerateInterpolator())
            .setAnimationListener(object : XAnimator.XAnimationListener{

                override fun onAnimationUpdate(fraction: Float, value: Float) {
                    ViewUtils.setMarginTop(animationContainer, marginTopValuer.calculateValue(fraction).toInt())
                    ViewUtils.setMarginLeft(animationContainer, marginLeftValuer.calculateValue(fraction).toInt())
                    ViewUtils.setHeight(animationContainer, heightValuer.calculateValue(fraction).toInt())
                    ViewUtils.setWidth(animationContainer, widthValuer.calculateValue(fraction).toInt())
                    animationContainer.translationX = translateXValuer.calculateValue(fraction)
                    animationContainer.rotation = rotateValuer.calculateValue(fraction)
                }

                override fun onAnimationStart(animation: XAnimator?) {
                }

                override fun onAnimationEnd(animation: XAnimator?) {
                }

                override fun onAnimationCancel(animation: XAnimator?) {
                }
            })
        animator?.start()
    }

    fun exit() {
        //可以再次检查一次位置 防止部分时候手势偏移了
        originContainer.getGlobalVisibleRect(originVisibleRect)
        animator?.cancel()
        marginLeftValuer.to(originVisibleRect.left.toFloat())
        marginTopValuer.to(originVisibleRect.top.toFloat())
        widthValuer.to(originContainer.width.toFloat())
        heightValuer.to(originContainer.height.toFloat())
        rotateValuer.to(0f)
        translateXValuer.to(0f)
        animator = XAnimator.ofFloat(0f, 1f)
            .duration(350)
            .interpolator(AccelerateDecelerateInterpolator())
            .setAnimationListener(object : XAnimator.XAnimationListener {
                override fun onAnimationUpdate(fraction: Float, value: Float) {
                    ViewUtils.setMarginTop(animationContainer, marginTopValuer.calculateValue(fraction).toInt())
                    ViewUtils.setMarginLeft(animationContainer, marginLeftValuer.calculateValue(fraction).toInt())
                    ViewUtils.setHeight(animationContainer, heightValuer.calculateValue(fraction).toInt())
                    ViewUtils.setWidth(animationContainer, widthValuer.calculateValue(fraction).toInt())
                    animationContainer.translationX = translateXValuer.calculateValue(fraction)
                    animationContainer.rotation = rotateValuer.calculateValue(fraction)
                }

                override fun onAnimationStart(animation: XAnimator?) {
                }

                override fun onAnimationEnd(animation: XAnimator?) {
                    XPlayer.exchangeVideoContainer(animationContainer, originContainer)
                    rootContent.removeView(animationContainer)
                }

                override fun onAnimationCancel(animation: XAnimator?) {
                }
            })
        animator?.start()
    }
}