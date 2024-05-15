package me.csxiong.uiux.ui.video.decoder

/**
 * @Desc : 抽象播放器
 * @Author : meitu - 2021/8/31
 *
 * 基础的状态改变监听
 */
abstract class AbsPlayer : IPlayer {

    override var bufferPercent: Int = 0

    override var duration: Int = 0

    override var currentPosition: Int = 0

    override var audioSessionId: Int = 0

    override var videoWidth: Int = 0

    override var videoHeight: Int = 0

    @PlayerState
    override var state: Int = PlayerState.STATE_IDLE

    /**
     * 当前位置改变
     */
    var onCurrentPositionChange: ((currentPosition: Int) -> Unit)? = null

    /**
     * 总进度改变
     */
    var onDurationChange: ((duration: Int) -> Unit)? = null

    /**
     * 播放状态
     * UI状态最好不要和这个状态混合 这个状态 准备添加线程处理优化耗时
     */
    var onStateChange: ((state: Int) -> Unit)? = null

    /**
     * buffer更新
     */
    var onBufferPercentUpdate: ((bufferPercent: Int) -> Unit)? = null

    /**
     * 视频尺寸变化
     */
    var onVideoSizeChange: ((videoWidth: Int, videoHeight: Int) -> Unit)? = null

    /**
     * 视频事件改变
     */
    var onVideoEventChange: ((event: Int) -> Unit)? = null

    /**
     * 更新当前播放器状态
     */
    fun updateStatus(@PlayerState status: Int) {
        this.state = status
        onStateChange?.invoke(state)
    }

    /**
     * 执行任务带异常保护
     */
    fun executeHandleException(task: (() -> Unit), handleErrorTask: ((e: Exception) -> Unit)? = null) {
        try {
            task.invoke()
        } catch (e: Exception) {
            if (handleErrorTask == null) {
                handleException(e)
            } else {
                handleErrorTask.invoke(e)
            }
        }
    }

    /**
     * 处理异常
     */
    fun handleException(e: Exception) {
        reset()
    }

}