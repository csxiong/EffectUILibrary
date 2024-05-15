package me.csxiong.uiux.ui.video.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.commsource.util.*
import me.csxiong.uiux.R
import me.csxiong.uiux.ui.video.*
import me.csxiong.uiux.ui.video.decoder.PlayerState
import me.csxiong.uiux.ui.video.XPlayer
import me.csxiong.uiux.utils.gone
import me.csxiong.uiux.utils.visible
import java.security.MessageDigest

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.view.PixelCopy.request

import androidx.annotation.NonNull

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool

import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.VideoDecoder


/**
 * @Desc : 视频Container
 * @Author : meitu - 2021/8/31
 *
 * 整体视频组件视图结构和之后拓展的部分
 * -----------视频整体容器------------>>>XVideoContainer
 *
 * 1 TODO 视频背景
 * 2 --------视频播放视图层--------->>>XVideoRender <<<---通过编辑container的提供默认的UI包提供配置处理
 *
 *    ----------解码器----------->>>AbsPlayer
 *          player --->>在视频无缝切换的这种需要共享解码器 并且支持切换render
 *    ----------解码器----------->>>AbsPlayer
 *
 *    .1 IRender 渲染层
 *
 *    -----------视频逻辑层----------->>>XVideoUIGroup
 *    .2  视频手势控制层
 *    .3  视频播放控制层
 *    .4 TODO 视频播放失败层
 *    .5 TODO 视频播放结束层
 *    .6 TODO ...
 *    ------------------------------
 *   -------------------------<<<XVideoRender
 * 3 视频和背景白底蒙板
 * 4 视频封面
 * 5 TODO 视频播放按钮（支持点播情况 允许用户手动激活某个视频）这种属于交互部分
 * -----------视频整体容器------------
 *
 * 注意事项和代码维护查看：
 * 1.XVideoView对外已经不能暴露任何非UI状态位  内聚要高,提供给外部状态都是容错过的UI状态位
 * 2.所有视频状态UI监听和事件监听都是在XVideoContainer向XVideoView注册监听和回调获取状态位
 * 3.视频断开 整体思想是XVideoContainer和XVideoView整体断开连接,保证在XVideoContainer的生命周期中不残留XVideoView,这点可以做到很好的防止页面泄漏的问题
 * 4.XVideoView中整体的层次封装思路和相机的Cover类似 提供基础的事件传递
 *
 * 还有另一种需求的添加 视频从一个窗口attach到另一个窗口 这个功能现在暂时先加 为将来一些结构做准备
 */
class XVideoContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    /**
     * 渲染层
     */
    val renderContainer: FrameLayout = FrameLayout(context)
    var videoRenderView: XVideoRenderView? = null

    /**
     * 封面
     */
    val videoCover: ImageView = ImageView(context)
    val dismissCover = Runnable {
        videoCover.gone()
    }

    init {
        //TODO 视频背景层
        //TODO 视频手势等逻辑层可集成在VideoView中
        //视频播放层
        addView(renderContainer, LayoutParams(-1, -1, Gravity.CENTER))
        //视频封面
        addView(videoCover, LayoutParams(-1, -1, Gravity.CENTER))
    }

    init {
        elevation = 0f
    }

    /**
     * 准备视频封面
     */
    fun prepareVideoCover(coverPath: String, placeHolder: Drawable = ColorDrawable(0xFF424242.toInt())) {
        Glide.with(context)
            .load(coverPath)
            .apply(RequestOptions.frameOf(100).apply {
                set(VideoDecoder.FRAME_OPTION,MediaMetadataRetriever.OPTION_CLOSEST)
            })
            .apply(RequestOptions.placeholderOf(placeHolder))
            .into(videoCover)
    }

    /**
     * 激活Video播放
     * @param isLazy 是否检测懒加载的情况
     * @param packingVideoView 外部提供的VideoView
     *
     * 简单激活 这种之后都是业务中可自定义编辑和添加的部分
     */
    fun play(playSource: PlaySource, isLazy: Boolean = false) {
        if (isLazy && videoRenderView != null) {
            return
        }
        release()
        videoRenderView = XVideoRenderView(context).apply {
            renderContainer.addView(this, LayoutParams(-1, -1, Gravity.CENTER))
            initVideoUiGroup(VideoUIPackage.getHomeVideoUiPackage())
            registerVideoListener(this)
            bindPlayer()
            player.stop()
            player.setPlaySource(playSource)
            player.setMute()
            player.setLoop()
            player.start()
        }
    }

    /**
     * 请求开启
     */
    fun requestStart() {
        if (videoRenderView?.player?.state == PlayerState.STATE_ERROR) {
            //错误状态下
            videoRenderView?.player?.retry()
        } else {
            videoRenderView?.player?.start()
        }
    }

    /**
     * 请求暂停
     */
    fun requestPause() {
        videoRenderView?.player?.pause()
    }

    /**
     * 直接释放对应的VideoView
     */
    fun release() {
        videoRenderView?.let {
            it.unbindPlayer()
            it.release()
            //断开UI状态监听
            unregisterVideoListener(it)
            //bugFix:不会触发再次的requestLayout
            renderContainer.removeViewInLayout(it)
            //直接释放解码器
            it.player.stop()
            XPlayer.queue(Runnable {
                it.player.reset()
                it.player.destroy()
            })
            videoRenderView = null
        }
    }

    /**
     * 单纯的释放渲染层
     */
    fun releaseRender() {
        videoRenderView?.let {
            unregisterVideoListener(it)
            renderContainer.removeViewInLayout(it)
            videoRenderView = null
        }
    }

    /**
     * 接收VideoContainer
     */
    fun receiveVideoContainer(preContainer: XVideoContainer) {
        release()
        preContainer.videoRenderView?.let {
            preContainer.renderContainer.removeView(it)
            preContainer.unregisterVideoListener(it)
            preContainer.videoRenderView = null

            this.videoRenderView = it
            registerVideoListener(it)
            renderContainer.addView(it)
        }
    }

    /**
     * Video监听
     */
    private fun registerVideoListener(render: XVideoRenderView) {
        render.onVideoUiStateChange = {
            when (it) {
                VideoUIState.INITIALIZE -> {
                    removeCallbacks(dismissCover)
                    videoCover.visible()
                }
                VideoUIState.PLAY -> {
                }
                VideoUIState.PAUSE -> {
                }
                VideoUIState.ERROR -> {
                    removeCallbacks(dismissCover)
                    videoCover.visible()
                }
                VideoUIState.COMPLETE -> {
                }
            }
        }

        render.onVideoEventChange = {
            when (it) {
                VideoEvent.VIDEO_RENDERING_START -> {
                    removeCallbacks(dismissCover)
                    dismissCover.run()
                }
            }
        }

        render.enterFullScreen = {
//            XPlayer.enterFullScreen(FullScreenTransitionAnimation(this))
        }
    }

    /**
     * 注销
     */
    private fun unregisterVideoListener(render: XVideoRenderView) {
        render.onVideoUiStateChange = null
        render.onVideoEventChange = null
        render.enterFullScreen = null
    }

    /**
     * 安全
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }
}