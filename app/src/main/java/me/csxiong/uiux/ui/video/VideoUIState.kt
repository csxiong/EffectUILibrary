package me.csxiong.uiux.ui.video

import androidx.annotation.IntDef

/**
 * @Desc : 视频UI状态标志位
 * @Author : meitu - 2021/9/3
 */
@IntDef(
    value = [
        VideoUIState.INITIALIZE,
        VideoUIState.PLAY,
        VideoUIState.PAUSE,
        VideoUIState.COMPLETE,
        VideoUIState.ERROR
    ]
)
annotation class VideoUIState {

    companion object {
        const val INITIALIZE = 0
        const val PLAY = 1
        const val PAUSE = 2
        const val COMPLETE = 3
        const val ERROR = 4
    }
}