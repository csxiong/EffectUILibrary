package me.csxiong.uiux.ui.video

import androidx.annotation.IntDef
import java.io.Serializable

/**
 * @Desc : 定义画面比例
 * @Author : meitu - 2021/8/31
 */
@IntDef(
    value = [
        AspectRatio.AspectRatio_16_9,
        AspectRatio.AspectRatio_4_3,
        AspectRatio.AspectRatio_MATCH_PARENT,
        AspectRatio.AspectRatio_FILL_PARENT,
        AspectRatio.AspectRatio_FIT_PARENT,
        AspectRatio.AspectRatio_ORIGIN,
        AspectRatio.AspectRatio_FILL_WIDTH,
        AspectRatio.AspectRatio_FILL_HEIGHT
    ]
)
annotation class AspectRatio {

    companion object {
        const val AspectRatio_16_9 = 1
        const val AspectRatio_4_3 = 2
        const val AspectRatio_MATCH_PARENT = 3
        const val AspectRatio_FILL_PARENT = 4
        const val AspectRatio_FIT_PARENT = 5
        const val AspectRatio_ORIGIN = 6
        const val AspectRatio_FILL_WIDTH = 7
        const val AspectRatio_FILL_HEIGHT = 8
    }
}