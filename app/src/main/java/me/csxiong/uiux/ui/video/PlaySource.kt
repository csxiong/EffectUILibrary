package me.csxiong.uiux.ui.video

import android.content.res.AssetFileDescriptor
import me.csxiong.library.base.APP

/**
 * @Desc : 播放资源
 * @Author : meitu - 2021/8/31
 */
data class PlaySource(
    val coverPath: String,
    val path: String? = "",
    val fileDescriptor: AssetFileDescriptor? = null
) {

    companion object {

        /**
         * 创建资源文件
         */
        fun createAssetPlaySource(assetPath: String): PlaySource {
            return PlaySource("file:///android_asset/$assetPath", null, AssetsUtil.findFile(APP.get(), assetPath))
        }

        /**
         * 创建本地播放源
         */
        fun createLocalPlaySource(localPath: String): PlaySource {
            return PlaySource(localPath, localPath, null)
        }

        /**
         * 创建在线播放源
         */
        fun createOnlinePlaySource(onlinePath: String): PlaySource {
            return PlaySource(onlinePath, XPlayer.getProxyUrl(onlinePath), null)
        }
    }
}