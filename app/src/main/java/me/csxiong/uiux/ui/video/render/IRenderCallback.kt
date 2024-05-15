package me.csxiong.uiux.ui.video.render

/**
 * @Desc : 和渲染组件回调
 * @Author : meitu - 2021/8/31
 */
interface IRenderCallback {

    fun onSurfaceCreated(renderHolder: IRenderHolder, width: Int, height: Int)

    fun onSurfaceChanged(renderHolder: IRenderHolder, format: Int, width: Int, height: Int)

    fun onSurfaceDestroy(renderHolder: IRenderHolder)
}