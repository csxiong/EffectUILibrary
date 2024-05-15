package me.csxiong.uiux.ui.video.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import me.csxiong.uiux.ui.video.VideoEvent
import me.csxiong.uiux.ui.video.VideoUIPackage
import me.csxiong.uiux.ui.video.VideoUIState

/**
 * @Desc : 这是一个配合视频播放交互的容器
 * @Author : meitu - 2021/9/3
 *
 * 目的很简单 就是一个视频渲染层之上的UI组 如命名一般就是处理视频组UIUX 的组别
 *
 * 主要是分发一些视频UI状态
 * 分发一些视频信息
 * 分发一些手势处理
 *
 * 这边的代码都是桥梁 衔接UI结构和视频状态的接收和发送问题
 * 目前这边实现不是支持拓展的方式 是直接一对一方法的方式
 * 可开放部分自定义数据请求 接口等
 */
class XVideoUIGroup @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var uiPackage: VideoUIPackage? = null

//    var gestureCover = GestureVideoCover()

    fun attach(uiPackage: VideoUIPackage) {
        //默认添加手势层
//        if (uiPackage.covers.isEmpty()) {
//            //BugFix:这里一个逻辑  如果UI包是空 说明不需要任何交互 那么手势层也不添加
//            removeAllViews()
//            return
//        } else {
//            uiPackage.covers.add(0, gestureCover)
//        }
        this.uiPackage = uiPackage
        //直接抛弃旧数据
        removeAllViews()
        uiPackage.covers.forEach {
            it.onAttach(this)
        }
    }

    fun detach() {
        uiPackage?.covers?.forEach {
            it.onDetach()
        }
        removeAllViews()
        uiPackage = null
    }

    fun requestStart() {
        requestStart?.invoke()
    }

    fun requestPause() {
        requestPause?.invoke()
//        gestureCover.focusVideoControll()
    }

    fun requestCurrentPosition() {
        requestCurrentPosition?.invoke()
    }

    fun requestFullScreen(isFullScreen: Boolean) {
        requestFullScreen?.invoke(isFullScreen)
    }

    fun requestSeekTo(seekTo: Int) {
        requestSeekTo?.invoke(seekTo)
    }

    fun isPlaying(): Boolean {
        return isPlaying?.invoke() ?: false
    }

    var isPlaying: (() -> Boolean)? = null
    var requestFullScreen: ((isFullScreen: Boolean) -> Unit)? = null
    var requestStart: (() -> Unit)? = null
    var requestPause: (() -> Unit)? = null
    var requestCurrentPosition: (() -> Unit)? = null
    var requestSeekTo: ((seekTo: Int) -> Unit)? = null

    /**
     * 刷新视频控制
     */
    fun refreshVideoControll() {
//        gestureCover.refreshDismissVideoControll()
    }

    /**
     * 停留在视频控制界面
     */
    fun focusVideoControll() {
//        gestureCover.focusVideoControll()
    }

    fun receiveVideoUiState(@VideoUIState videoUIState: Int) {
        uiPackage?.covers?.forEach {
            it.onVideoUiStateChange(videoUIState)
        }
    }

    fun receiveVideoDuration(duration: Int) {
        uiPackage?.covers?.forEach {
            it.onVideoDurationChange(duration)
        }
    }

    fun receiveVideoCurrentPosition(currentPosition: Int) {
        uiPackage?.covers?.forEach {
            it.onVideoCurrentPositionChange(currentPosition)
        }
    }

    fun receiveVideoSize(videoWidth: Int, videoHeight: Int) {
        uiPackage?.covers?.forEach {
            it.onVideoSizeChange(videoWidth, videoHeight)
        }
    }

    fun receiveVideoBufferPercent(bufferPercent: Int) {
        uiPackage?.covers?.forEach {
            it.onVideoBufferPercentChange(bufferPercent)
        }
    }

    fun receiveVideoControll(isVideoControll: Boolean) {
        uiPackage?.covers?.forEach {
            it.onVideoControllChange(isVideoControll)
        }
    }

    fun receiveVideoEvent(@VideoEvent event: Int) {
        uiPackage?.covers?.forEach {
            it.onVideoEventChange(event)
        }
    }

}