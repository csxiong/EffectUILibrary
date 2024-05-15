package me.csxiong.uiux.ui.video.decoder

import android.view.Surface
import android.view.SurfaceHolder
import me.csxiong.uiux.ui.video.PlaySource

/**
 * @Desc : 播放器decoder解析
 * @Author : meitu - 2021/8/31
 */
interface IPlayer {

    /**
     * 设置播放源
     */
    fun setPlaySource(playSource: PlaySource)

    /**
     * 设置播放组件
     */
    fun setDisplay(surfaceHolder: SurfaceHolder?)

    /**
     * 设置播放Surface
     */
    fun setSurface(surface: Surface?)

    /**
     * 设置音量
     */
    fun setVolume(left: Float, right: Float)

    /**
     * 静音
     */
    fun setMute()

    /**
     * 设置循环
     */
    fun setLoop()

    /**
     * 播放速度
     */
    fun setSpeed(speed: Float)

    /**
     * 是否在播放
     */
    fun isPlaying(): Boolean

    /**
     * 缓存百分比
     */
    var bufferPercent: Int

    /**
     * 当前播放进度
     */
    var currentPosition: Int

    /**
     * 总时长
     */
    var duration: Int

    /**
     * audio session ID
     */
    var audioSessionId: Int

    /**
     * 当前视频宽度
     */
    var videoWidth: Int

    /**
     * 当前视频高度
     */
    var videoHeight: Int

    /**
     * 当前解码器（播放器状态）
     */
    var state: Int

    /**
     * 开始
     */
    fun start()

    /**
     * 开始 index
     */
    fun start(msc: Int)

    /**
     * 暂停
     */
    fun pause()

    /**
     * 继续播放
     */
    fun resume()

    /**
     * seek
     */
    fun seekTo(msc: Int)

    /**
     * 停止播放
     */
    fun stop()

    /**
     * 重置
     */
    fun reset()

    /**
     * 尝试重试
     */
    fun retry()

    /**
     * 销毁
     */
    fun destroy()

}