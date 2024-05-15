package me.csxiong.uiux.ui.gradient

import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import me.csxiong.library.base.BaseActivity
import me.csxiong.library.utils.DeviceUtils
import me.csxiong.library.utils.XDisplayUtil
import me.csxiong.uiux.R
import me.csxiong.uiux.databinding.ActivityGradientBinding
import me.csxiong.uiux.ui.animation.animationTransition
import me.csxiong.uiux.utils.ViewUtils

@Route(path = "/main/gradient", name = "渐变顶部栏")
class GradientViewAcitivity : BaseActivity<ActivityGradientBinding>() {
    override fun getLayoutId(): Int {
        return R.layout.activity_gradient
    }

    override fun initView() {
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun initData() {

    }

    fun onExpand(view: View) {
        mViewBinding.cv.animationTransition {
            ViewUtils.setWidth(mViewBinding.cv, XDisplayUtil.dpToPxInt(80f))
            mViewBinding.tv.alpha = 1f
            mViewBinding.tv.translationX = XDisplayUtil.dpToPx(18f)
        }
    }

    fun onShrink(view: View) {
        mViewBinding.cv.animationTransition {
            ViewUtils.setWidth(mViewBinding.cv, XDisplayUtil.dpToPxInt(30f))
            mViewBinding.tv.alpha = 0f
            mViewBinding.tv.translationX = 0f
        }
    }
}