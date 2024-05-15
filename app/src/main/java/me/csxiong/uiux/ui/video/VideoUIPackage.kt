package me.csxiong.uiux.ui.video

import me.csxiong.uiux.ui.video.cover.IVideoCover

/**
 * @Desc : 视频UI包
 * @Author : meitu - 2021/9/3
 */
data class VideoUIPackage(val covers: MutableList<IVideoCover>) {

    companion object {

        /**
         * 获取常规视频UI包
         */
        fun getCommonVideoUIPackage(): VideoUIPackage = VideoUIPackage(ArrayList<IVideoCover>().apply {
//            add(VideoTitleCover())
//            add(VideoControllCover())
        })

        /**
         * 目前首页视频
         * 啥也不加
         */
        fun getHomeVideoUiPackage(): VideoUIPackage = VideoUIPackage(arrayListOf<IVideoCover>().apply {

        })

    }
}