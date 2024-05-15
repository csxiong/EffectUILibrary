package me.csxiong.uiux.ui.video.decoder

import android.media.MediaPlayer
import android.text.TextUtils
import android.view.Surface
import android.view.SurfaceHolder
import me.csxiong.uiux.ui.video.PlaySource
import me.csxiong.uiux.ui.video.VideoEvent
import me.csxiong.uiux.ui.video.XPlayer
import me.csxiong.uiux.utils.print

/**
 * @Desc : 系统播放器
 * @Author : meitu - 2021/8/31
 *
 * 使用的方法都是可以在UI线程直接调动的
 * 里面的一个重要思想就是
 * 通过state来标记状态 因为播放视频是多个回调接口交叉作用的结果 所以需要有[mTargetState]来预标记一些UI手势和用户期望的状态位
 * 在异步状态下一些关键回调会再次判断UI所"期望"的状态
 */
class SysMediaPlayer : AbsPlayer() {

    /**
     * 系统自带解码器
     */
    val mediaPlayer = MediaPlayer()

    /**
     * 播放源
     */
    var mPlaySource: PlaySource? = null

    /**
     * 启播位置
     */
    var startSeekPos: Int = 0

    /**
     * 视频宽度
     */
    override var videoWidth: Int
        get() = mediaPlayer.videoWidth
        set(value) {}

    /**
     * 视频高度
     */
    override var videoHeight: Int
        get() = mediaPlayer.videoHeight
        set(value) {}

    /**
     * 当前视频duration
     */
    override var duration: Int
        get() {
            return when (state) {
                PlayerState.STATE_INITIALIZED,
                PlayerState.STATE_ERROR,
                PlayerState.STATE_IDLE -> 0
                else -> mediaPlayer.duration
            }
        }
        set(value) {}

    /**
     * 获取当前播放进度
     */
    override var currentPosition: Int
        get() {
            return when (state) {
                PlayerState.STATE_PREPARED,
                PlayerState.STATE_STARTED,
                PlayerState.STATE_PAUSED,
                PlayerState.STATE_PLAYBACK_COMPLETE -> mediaPlayer.currentPosition
                else -> 0
            }
        }
        set(value) {}

    @PlayerState
    var mTargetState: Int = PlayerState.STATE_IDLE

    private val onPrepareListener by lazy {
        MediaPlayer.OnPreparedListener {
            updateStatus(PlayerState.STATE_PREPARED)
            videoWidth = it.videoWidth
            videoHeight = it.videoHeight
            //启播位置
            var seekToPosition = startSeekPos
            if (seekToPosition != 0) {
                it.seekTo(seekToPosition)
                startSeekPos = 0
            }
            //处理状态
            when (mTargetState) {
                PlayerState.STATE_STARTED -> start()
                PlayerState.STATE_PAUSED -> pause()
                PlayerState.STATE_STOPPED, PlayerState.STATE_IDLE -> reset()
            }
        }
    }

    private val onVideoSizeChangedListener by lazy {
        MediaPlayer.OnVideoSizeChangedListener { mp, width, height ->
            onVideoSizeChange?.invoke(width, height)
        }
    }

    private val onCompleteListener by lazy {
        MediaPlayer.OnCompletionListener {
            updateStatus(PlayerState.STATE_PLAYBACK_COMPLETE)
            mTargetState = PlayerState.STATE_PLAYBACK_COMPLETE
        }
    }

    private val onErrorListener by lazy {
        MediaPlayer.OnErrorListener { mp, what, extra ->
            updateStatus(PlayerState.STATE_ERROR)
            mTargetState = PlayerState.STATE_ERROR
            true
        }
    }

    private val onInfoListener by lazy {
        MediaPlayer.OnInfoListener { mp, what, extra ->
            //TODO 网络等一系列问题处理
            "onInfo : $what : $extra".print(XPlayer.TAG)
            "onInfo : $what : $extra".print(XPlayer.TAG)
            when (what) {
                MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING -> {
                    updateStatus(PlayerState.STATE_ERROR)
                    mTargetState = PlayerState.STATE_ERROR
                }
                MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                    onVideoEventChange?.invoke(VideoEvent.VIDEO_RENDERING_START)
                }
            }
            true
        }
    }

    private val onSeekCompleteListener by lazy {
        MediaPlayer.OnSeekCompleteListener {
            onVideoEventChange?.invoke(VideoEvent.SEEK_TO_END)
        }
    }

    private val onBufferingUpdateListener by lazy {
        MediaPlayer.OnBufferingUpdateListener { mp, percent ->
            bufferPercent = percent
            onBufferPercentUpdate?.invoke(bufferPercent)
        }
    }

    override fun setPlaySource(playSource: PlaySource) {
        executeHandleException({
            stop()
            reset()
            resetListener()

            mediaPlayer.setOnPreparedListener(onPrepareListener)
            mediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener)
            mediaPlayer.setOnCompletionListener(onCompleteListener)
            mediaPlayer.setOnErrorListener(onErrorListener)
            mediaPlayer.setOnInfoListener(onInfoListener)
            mediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener)
            mediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener)
            updateStatus(PlayerState.STATE_INITIALIZED)
            this.mPlaySource = playSource

//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            if (!TextUtils.isEmpty(playSource.path)) {
                mediaPlayer.setDataSource(playSource.path)
            } else if (playSource.fileDescriptor != null) {
                //BugFix:onErrorListener报未知错误
                //mediaPlayer.setDataSource(playSource.fileDescriptor)
                mediaPlayer.setDataSource(
                    playSource.fileDescriptor.fileDescriptor,
                    playSource.fileDescriptor.startOffset,
                    playSource.fileDescriptor.length
                )
            }
            mediaPlayer.setScreenOnWhilePlaying(true)
            mediaPlayer.prepareAsync()
        }, {
            updateStatus(PlayerState.STATE_ERROR)
            mTargetState = PlayerState.STATE_ERROR
        })
    }

    override fun setDisplay(surfaceHolder: SurfaceHolder?) {
        executeHandleException({
            mediaPlayer.setDisplay(surfaceHolder)
        })
    }

    override fun setSurface(surface: Surface?) {
        executeHandleException({
            mediaPlayer.setSurface(surface)
        })
    }

    override fun setVolume(left: Float, right: Float) {
        mediaPlayer.setVolume(left, right)
    }

    override fun setMute() {
        mediaPlayer.setVolume(0f, 0f)
    }

    override fun setLoop() {
        mediaPlayer.isLooping = true
    }

    override fun setSpeed(speed: Float) {

    }

    override fun isPlaying(): Boolean {
        return state != PlayerState.STATE_ERROR && mediaPlayer.isPlaying
    }

    override fun start() {
        when (state) {
            PlayerState.STATE_PREPARED,
            PlayerState.STATE_PAUSED,
            PlayerState.STATE_PLAYBACK_COMPLETE -> {
                "mediaPlayer.start".print(XPlayer.TAG)
                mediaPlayer.start()
                updateStatus(PlayerState.STATE_STARTED)
                onDurationChange?.invoke(duration)
            }
        }
        "mTargetState目标状态为 start".print(XPlayer.TAG)
        mTargetState = PlayerState.STATE_STARTED
    }

    override fun start(msc: Int) {
        startSeekPos = msc
        start()
    }

    override fun pause() {
        when (state) {
            PlayerState.STATE_END,
            PlayerState.STATE_ERROR,
            PlayerState.STATE_IDLE,
            PlayerState.STATE_INITIALIZED,
            PlayerState.STATE_PAUSED,
            PlayerState.STATE_STOPPED -> {
            }
            else -> {
                "mediaPlayer.pause".print(XPlayer.TAG)
                mediaPlayer.pause()
                updateStatus(PlayerState.STATE_PAUSED)
            }
        }
        "mTargetState目标状态为 pause".print(XPlayer.TAG)
        mTargetState = PlayerState.STATE_PAUSED
    }

    override fun resume() {
        if (state == PlayerState.STATE_PAUSED) {
            mediaPlayer.start()
            updateStatus(PlayerState.STATE_STARTED)
            onDurationChange?.invoke(duration)
        }
        mTargetState = PlayerState.STATE_STARTED
    }

    override fun seekTo(msc: Int) {
        when (state) {
            PlayerState.STATE_PREPARED,
            PlayerState.STATE_STARTED,
            PlayerState.STATE_PAUSED,
            PlayerState.STATE_PLAYBACK_COMPLETE -> {
                mediaPlayer.seekTo(msc)
            }
        }
    }

    override fun stop() {
        when (state) {
            PlayerState.STATE_PREPARED,
            PlayerState.STATE_STARTED,
            PlayerState.STATE_PAUSED,
            PlayerState.STATE_PLAYBACK_COMPLETE -> {
                mediaPlayer.stop()
                updateStatus(PlayerState.STATE_STOPPED)
            }
        }
        mTargetState = PlayerState.STATE_STOPPED
    }

    override fun reset() {
        try {
            mediaPlayer.reset()
            updateStatus(PlayerState.STATE_IDLE)
        } catch (e: Exception) {
            updateStatus(PlayerState.STATE_IDLE)
        }
        mTargetState = PlayerState.STATE_IDLE
    }

    override fun retry() {
        if (state == PlayerState.STATE_ERROR) {
            mPlaySource?.let {
                reset()
                setPlaySource(it)
                setMute()
                setLoop()
                start()
            }
        }
    }

    override fun destroy() {
        updateStatus(PlayerState.STATE_END)
        resetListener()
        mediaPlayer.release()
    }

    //重置所有media监听
    fun resetListener() {
        mediaPlayer.setOnPreparedListener(null)
        mediaPlayer.setOnVideoSizeChangedListener(null)
        mediaPlayer.setOnCompletionListener(null)
        mediaPlayer.setOnErrorListener(null)
        mediaPlayer.setOnInfoListener(null)
        mediaPlayer.setOnBufferingUpdateListener(null)
    }

}