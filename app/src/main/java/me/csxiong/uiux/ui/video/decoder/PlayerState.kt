package me.csxiong.uiux.ui.video.decoder

import androidx.annotation.IntDef

/**
 * @Desc : 播放状态抽象
 * @Author : meitu - 2021/8/31
 */
@IntDef(
    value = [
        PlayerState.STATE_END,
        PlayerState.STATE_ERROR,
        PlayerState.STATE_IDLE,
        PlayerState.STATE_INITIALIZED,
        PlayerState.STATE_PREPARED,
        PlayerState.STATE_STARTED,
        PlayerState.STATE_PAUSED,
        PlayerState.STATE_STOPPED,
        PlayerState.STATE_PLAYBACK_COMPLETE
    ]
)
annotation class PlayerState {
    companion object {
        const val STATE_END = -2
        const val STATE_ERROR = -1
        const val STATE_IDLE = 0
        const val STATE_INITIALIZED = 1
        const val STATE_PREPARED = 2
        const val STATE_STARTED = 3
        const val STATE_PAUSED = 4
        const val STATE_STOPPED = 5
        const val STATE_PLAYBACK_COMPLETE = 6
    }
}
