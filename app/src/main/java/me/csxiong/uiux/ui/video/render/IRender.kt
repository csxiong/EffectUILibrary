package me.csxiong.uiux.ui.video.render

import android.view.View
import me.csxiong.uiux.ui.video.AspectRatio

/**
 * @Desc : 渲染抽象
 * @Author : meitu - 2021/8/31
 */
interface IRender {

    /**
     * 设置对应渲染的回调
     */
    fun setRenderCallback(renderCallback: IRenderCallback?)

    /**
     * TODO 设置旋转
     */
    fun setVideoRotation(degree: Int)

    fun setVideoSampleAspectRatio(videoSarNum: Int, videoSarDen: Int)

    /**
     * 设置比例问题
     *
     * see also
     * [AspectRatio.AspectRatio_16_9]
     * [AspectRatio.AspectRatio_4_3]
     * [AspectRatio.AspectRatio_FIT_PARENT]
     * [AspectRatio.AspectRatio_FILL_PARENT]
     * [AspectRatio.AspectRatio_MATCH_PARENT]
     * [AspectRatio.AspectRatio_ORIGIN]
     *
     * @param aspectRatio
     */
    fun updateAspectRatio(@AspectRatio aspectRatio: Int)

    /**
     * 更新视频尺寸 也会同步更新texture尺寸
     *
     * @param videoWidth
     * @param videoHeight
     */
    fun updateVideoSize(videoWidth: Int, videoHeight: Int)

    /**
     * rootView
     */
    fun getRenderView(): View?

    /**
     * 准备
     */
    fun prepare()

    /**
     * 释放
     */
    fun release()

    /**
     * 判断是否释放
     *
     * @return
     */
    fun isReleased(): Boolean
}