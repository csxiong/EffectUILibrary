package me.csxiong.uiux.ui.video.cover

import me.csxiong.uiux.ui.video.view.XVideoUIGroup

/**
 * @Desc : 基础的接口
 * @Author : meitu - 2021/9/3
 */
interface IVideoCover : IVideoEvent {

    /**
     * 绑定
     */
    fun onAttach(uiGroup: XVideoUIGroup)

    /**
     * 解绑
     */
    fun onDetach()

}