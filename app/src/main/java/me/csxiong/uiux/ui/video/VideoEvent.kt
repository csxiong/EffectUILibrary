package me.csxiong.uiux.ui.video

import androidx.annotation.IntDef

/**
 * @Desc : 视频自定义事件
 * @Author : meitu - 2021/9/4
 */
@IntDef(
    value = [
        VideoEvent.SEEK_TO_START,
        VideoEvent.SEEK_TO_END,
        VideoEvent.VIDEO_RENDERING_START
    ]
)
annotation class VideoEvent {

    companion object {
        const val SEEK_TO_START = 0
        const val SEEK_TO_END = 1

        const val VIDEO_RENDERING_START = 2
    }
}