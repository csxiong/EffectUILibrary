package me.csxiong.uiux.ui.video.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.FrameLayout
import me.csxiong.library.utils.ThreadExecutor
import me.csxiong.uiux.ui.video.*
import me.csxiong.uiux.ui.video.decoder.AbsPlayer
import me.csxiong.uiux.ui.video.decoder.PlayerState
import me.csxiong.uiux.ui.video.render.IRender
import me.csxiong.uiux.ui.video.render.IRenderCallback
import me.csxiong.uiux.ui.video.render.IRenderHolder
import me.csxiong.uiux.ui.video.render.RenderTextureView
import me.csxiong.uiux.ui.video.XPlayer

/**
 * @Desc : 视频播放器封装
 * @Author : meitu - 2021/9/2
 */
class XVideoRenderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    /**
     * 视频宽高
     */
    var videoWidth: Int = 0
    var videoHeight: Int = 0

    /**
     * 内部缓存当前UI状态
     */
    var uiState = VideoUIState.INITIALIZE

    /**
     * 提供外部UI状态回调
     */
    var onVideoUiStateChange: ((uiState: Int) -> Unit)? = null

    /**
     * 视频尺寸变化
     */
    var onVideoSizeChange: ((videoWidth: Int, videoHeight: Int) -> Unit)? = null

    /**
     * 视频事件
     */
    var onVideoEventChange: ((videoEvent: Int) -> Unit)? = null

    /**
     * 全屏改变
     */
    var enterFullScreen: (() -> Unit)? = null

    /**
     * 内部renderholder 主要是保存检测渲染holder是否准备好的判断
     */
    var renderHolder: IRenderHolder? = null

    /**
     * 解码器
     */
    val player: AbsPlayer by lazy {
        XPlayer.createPlayer()
    }

    /**
     * 渲染层
     *
     * 目前在[initRender]只使用texture一种渲染层 一个surfaceView 一个TextureView
     * 总体来说 textureView动画适配适合在列表等各个地方使用 目前仅支持一个策略[RenderTextureView]
     */
    val render: IRender by lazy {
        RenderTextureView(context).apply {
            isTakeOverSurfaceTexture = true
            setRenderCallback(object : IRenderCallback {
                override fun onSurfaceCreated(renderHolder: IRenderHolder, width: Int, height: Int) {
                    this@XVideoRenderView.renderHolder = renderHolder
                    checkRenderPlayerBindAvailable()
                }

                override fun onSurfaceChanged(renderHolder: IRenderHolder, format: Int, width: Int, height: Int) {
                    //没什么要做
                }

                override fun onSurfaceDestroy(renderHolder: IRenderHolder) {
                    this@XVideoRenderView.renderHolder = null
                }
            })
        }
    }

    /**
     * 视频中UI组别的消息
     */
    val videoUiGroup by lazy {
        XVideoUIGroup(context).apply {
            requestStart = {
                player.start()
            }
            requestPause = {
                player.pause()
            }
            requestSeekTo = {
                receiveVideoEvent(VideoEvent.SEEK_TO_START)
                player.seekTo(it)
                player.start(it)
            }
            requestCurrentPosition = {
                player.onCurrentPositionChange?.invoke(player.currentPosition)
            }
            requestFullScreen = {
//                if (it) {
//                    enterFullScreen?.invoke()
//                } else {
//                    XPlayer.exitFullScreen()
//                }
            }
            isPlaying = {
                player.isPlaying()
            }
        }
    }

    init {
        //添加渲染层监听
        render.prepare()
        //添加渲染层
        addView(render.getRenderView(), LayoutParams(-2, -2, Gravity.CENTER))
        render.updateAspectRatio(AspectRatio.AspectRatio_FILL_PARENT)
        render.updateVideoSize(videoWidth, videoHeight)
        //UI组件层
        addView(videoUiGroup, -1, -1)
    }

    /**
     * 动态检测渲染层和播放器绑定
     */
    private fun checkRenderPlayerBindAvailable() {
        renderHolder?.bindPlayer(player)
    }

    /**
     * 更新渲染适配规则
     */
    fun updateRenderAspectRatio(@AspectRatio aspectRatio: Int) {
        render.updateAspectRatio(aspectRatio)
    }

    /**
     * 初始化视频UI组
     */
    fun initVideoUiGroup(videoUIPackage: VideoUIPackage) {
        videoUiGroup.attach(videoUIPackage)
    }

    /**
     * 释放
     */
    fun release() {
        updateUiState(VideoUIState.INITIALIZE)
        onVideoUiStateChange = null
        render.release()
    }

    /**
     * 绑定目标播放器
     */
    fun bindPlayer() {
        unbindPlayer()
        player.apply {
            onVideoSizeChange = { videoWidth, videoHeight ->
                this@XVideoRenderView.videoWidth = videoWidth
                this@XVideoRenderView.videoHeight = videoHeight
                render.updateVideoSize(videoWidth, videoHeight)
                videoUiGroup.receiveVideoSize(videoWidth, videoHeight)
                //再次往外部发送
                this@XVideoRenderView.onVideoSizeChange?.invoke(videoWidth, videoHeight)
            }
            onStateChange = {
                updateUiState(translateVideoUIState(it))
            }
            onVideoEventChange = {
                videoUiGroup.receiveVideoEvent(it)
                this@XVideoRenderView.onVideoEventChange?.invoke(it)
            }
            onDurationChange = {
                videoUiGroup.receiveVideoDuration(it)
            }
            onCurrentPositionChange = {
                videoUiGroup.receiveVideoCurrentPosition(it)
            }
            onBufferPercentUpdate = {
                videoUiGroup.receiveVideoBufferPercent(it)
            }
        }
        checkRenderPlayerBindAvailable()
    }

    /**
     * 解绑目标播放器
     */
    fun unbindPlayer() {
        this.player.apply {
            onVideoSizeChange = null
            onStateChange = null
            onDurationChange = null
            onCurrentPositionChange = null
            onBufferPercentUpdate = null
            onVideoEventChange = null
        }
    }

    @VideoUIState
    private fun translateVideoUIState(@PlayerState state: Int): Int {
        // 这里处理解码器状态问题 并生成UI组件能够识别使用的UI状态
        // 和内部状态分离的原因是 UI状态只要容错即可 不要和解码器状态实时一致，这个是很多人做播放器的一个思维陷阱
        // 并且内部状态因为部分解码器native方法耗时问题，需要优化耗时，所以UI的状态，都是可以优化fake UI的显示。
        return when (state) {
            //初始状态
            PlayerState.STATE_IDLE,
            PlayerState.STATE_INITIALIZED,
            PlayerState.STATE_PREPARED -> VideoUIState.INITIALIZE
            //播放状态
            PlayerState.STATE_STARTED -> VideoUIState.PLAY
            //暂停状态
            PlayerState.STATE_PAUSED,
            PlayerState.STATE_STOPPED -> VideoUIState.PAUSE
            //播放错误
            PlayerState.STATE_ERROR -> VideoUIState.ERROR
            //播放完成
            PlayerState.STATE_PLAYBACK_COMPLETE -> VideoUIState.COMPLETE
            //销毁 这个几乎没用
            PlayerState.STATE_END -> VideoUIState.INITIALIZE
            else -> VideoUIState.INITIALIZE
        }
    }

    /**
     * 更新UI状态
     */
    private fun updateUiState(@VideoUIState uiState: Int) {
        if (this@XVideoRenderView.uiState != uiState) {
            this@XVideoRenderView.uiState = uiState
            ThreadExecutor.runOnUiThread {
                onVideoUiStateChange?.invoke(uiState)
                videoUiGroup.receiveVideoUiState(uiState)
            }
        }
    }

}