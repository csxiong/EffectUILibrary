package me.csxiong.uiux.ui.video.cover

import android.view.ViewStub
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import me.csxiong.uiux.ui.video.view.XVideoUIGroup

/**
 * @Desc : 抽象的视频Cover
 * @Author : meitu - 2021/9/3
 */
abstract class AbsVideoCover<T : ViewDataBinding>(@LayoutRes val layoutId: Int) : IVideoCover {

    lateinit var group: XVideoUIGroup

    lateinit var vsCover: ViewStub

    /**
     * 提供外部的ViewBinding
     */
    var mViewBinding: T? = null

    override fun onAttach(uiGroup: XVideoUIGroup) {
        this.group = uiGroup
        vsCover = ViewStub(uiGroup.context, layoutId)
        uiGroup.addView(vsCover)
    }

    override fun onDetach() {

    }

    /**
     * 创建Cover
     * 可以重复调用的方法 很安全 使用了lazy的cover之后 [mViewBinding]是可空的类型，使用上强制了安全性 保证不会存在问题
     */
    fun createCover() {
        if (mViewBinding == null) {
            var rootView = vsCover.inflate()
            mViewBinding = DataBindingUtil.bind<T>(rootView) as T
            //默认的顺序还是保证了Cover内的结构
            initView()
        }
    }

    /**
     * 初始化View
     */
    abstract fun initView()

    override fun onVideoBufferPercentChange(bufferPercent: Int) {
    }

    override fun onVideoSizeChange(videoWidth: Int, videoHeight: Int) {
    }

    override fun onVideoCurrentPositionChange(currentPosition: Int) {
    }

    override fun onVideoDurationChange(duration: Int) {
    }

    override fun onVideoUiStateChange(state: Int) {
    }

    override fun onVideoControllChange(isVideoControll: Boolean) {
    }

    override fun onVideoEventChange(event: Int) {

    }
}