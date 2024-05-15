package me.csxiong.uiux.ui.video.render

import me.csxiong.uiux.ui.video.decoder.IPlayer

/**
 * @Desc : 渲染组件View的holder
 * @Author : meitu - 2021/8/31
 */
interface IRenderHolder {
    /**
     * 绑定对应Player
     *
     * @param player
     */
    fun bindPlayer(player: IPlayer)
}