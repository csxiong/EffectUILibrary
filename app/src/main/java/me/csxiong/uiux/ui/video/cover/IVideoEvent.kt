package me.csxiong.uiux.ui.video.cover

import me.csxiong.uiux.ui.video.VideoEvent
import me.csxiong.uiux.ui.video.VideoUIState

/**
 * @Desc : 视频事件方法 这里基本对应一个视频需要传出的事件
 * @Author : meitu - 2021/9/3
 *
 */
interface IVideoEvent {

    /**
     * 视频buffer更新
     */
    fun onVideoBufferPercentChange(bufferPercent: Int)

    /**
     * 视频尺寸改变
     */
    fun onVideoSizeChange(videoWidth: Int, videoHeight: Int)

    /**
     * 视频当前进度回调
     */
    fun onVideoCurrentPositionChange(currentPosition: Int)

    /**
     * 视频时长改变
     */
    fun onVideoDurationChange(duration: Int)

    /**
     * 视频状态改变
     */
    fun onVideoUiStateChange(@VideoUIState state:Int)

    /**
     * 是否处于视频控制状态
     */
    fun onVideoControllChange(isVideoControll:Boolean)

    /**
     * 接收视频事件
     */
    fun onVideoEventChange(@VideoEvent event:Int)
}