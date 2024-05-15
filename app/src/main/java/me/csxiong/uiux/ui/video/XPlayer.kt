package me.csxiong.uiux.ui.video

import me.csxiong.uiux.ui.video.decoder.AbsPlayer
import me.csxiong.uiux.ui.video.decoder.SysMediaPlayer
import me.csxiong.uiux.ui.video.view.XVideoContainer
import me.csxiong.library.base.APP
import me.csxiong.uiux.ui.video.videocache.HttpProxyCacheServer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * @Desc :  VideoCache管理保存类
 * @Author : meitu - 2021/8/31
 *
 * 详细代码实现查看VideoCache
 * https://github.com/danikula/AndroidVideoCache
 *
 * 因为jcenter停止维护 这个项目比较久了 我们直接拷贝到项目中 之后直接维护项目中的结构
 *
 * 基本原理就是本地socket代理提供url，video只读取本地，在线下载部分直接保存到本地文件做buffer
 *
 * 大家只要使用这个即可,[XVideoContainer]是在xml中承载视频组件下发[XVideoView]的容器
 *
 * [IRender]是渲染层的抽象 因为可能有surfaceView和textureView的差别 都可扩展支持
 * [IPlayer]是解码器的抽象 因为解码器如果在长期迭代和更高需求的过程中[MediaPlayer]所支持的解码格式不好说，可拓展支持更多解码器，只要对应更新
 * [AspectRatio]播放比例
 * [RenderMeasure]渲染测算比例的
 * [PlaySource]播放源，目前需求比较简单，支持也就是asserts、url支持
 *
 * 所有url视频缓存均由[HttpProxyCacheServer]AndroidVideoCache这个框架支持，这个框架类似web缓存的部分原理
 */
object XPlayer {

    const val TAG = "XPlayer"

    /**
     * 留给视频封面和播放偏差的时间间隔
     */
    const val VIDEO_COVER_DISMISS_INTERVAL = 250L

    /**
     * 媒体队列
     *
     * 主要是提供异步处理解码器生命周期耗时问题处理的问题
     */
    val mediaQueue by lazy { ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue<Runnable>()); }

    /**
     * 队列处理
     */
    fun queue(runnable: Runnable) {
        mediaQueue.execute(runnable)
    }

    /**
     * 缓存代理
     */
    val videoProxy by lazy {
        HttpProxyCacheServer.Builder(APP.get()).apply {
            maxCacheSize(200 * 1024 * 1024)//200MB缓存空间
        }.build()
    }

    /**
     * 获取解码器
     */
    fun createPlayer(): AbsPlayer {
        return SysMediaPlayer()
    }

    /**
     * 直接解绑
     */
    fun exchangeVideoContainer(
        preContainer: XVideoContainer,
        nextContainer: XVideoContainer,
        exchangeComplete: (() -> Unit)? = null
    ) {
        nextContainer.receiveVideoContainer(preContainer)
        exchangeComplete?.invoke()
    }

//    private var fullScreenTransitionAnimation: FullScreenTransitionAnimation? = null
//
//    fun hasFullScreenTarget(): Boolean {
//        return fullScreenTransitionAnimation != null
//    }
//
//    fun enterFullScreen(fullScreenTransitionAnimation: FullScreenTransitionAnimation?) {
//        exitFullScreen()
//        XPlayer.fullScreenTransitionAnimation = fullScreenTransitionAnimation
//        XPlayer.fullScreenTransitionAnimation?.enter()
//    }
//
//    fun exitFullScreen(): Boolean {
//        if (fullScreenTransitionAnimation != null) {
//            fullScreenTransitionAnimation?.exit()
//            fullScreenTransitionAnimation = null
//            return true
//        }
//        return false
//    }


    /**
     * 获取代理的url,支持边缓存边播放
     */
    fun getProxyUrl(originUrl: String): String {
        return videoProxy.getProxyUrl(originUrl, true)
    }

}