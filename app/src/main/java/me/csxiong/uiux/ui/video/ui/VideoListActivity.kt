package me.csxiong.uiux.ui.video.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.commsource.util.scroll.ActiveScrollListener
import me.csxiong.library.base.BaseActivity
import me.csxiong.library.integration.adapter.AdapterDataBuilder
import me.csxiong.library.integration.adapter.XItem
import me.csxiong.library.integration.adapter.XRecyclerViewAdapter
import me.csxiong.library.integration.adapter.XViewHolder
import me.csxiong.uiux.R
import me.csxiong.uiux.databinding.ActivityVideoBinding
import me.csxiong.uiux.databinding.ItemVideoListBinding
import me.csxiong.uiux.ui.color.ColorWheelViewHolder
import me.csxiong.uiux.ui.layoutManager.active.CenterActiveStrategy
import me.csxiong.uiux.ui.video.AssetsUtil
import me.csxiong.uiux.ui.video.PlaySource

/**
 * @Description : 视频列表页面
 * @Author : bear
 * @Date : 2022/1/23
 */
@Route(path = "/main/videoList", name = "游标控件")
class VideoListActivity : BaseActivity<ActivityVideoBinding>() {

    override fun getLayoutId(): Int {
        return R.layout.activity_video
    }

    val adapter by lazy { }

    override fun initView() {
        mViewBinding.rv.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = XRecyclerViewAdapter(context).apply {
                setOnEntityClickListener({ position, entity ->

                    false
                }, String::class.java)

                updateItemEntities(
                    AdapterDataBuilder.create()
                        .addEntities(
                            arrayListOf(
                                "onboarding_video_as_edit.mp4",
                                "onboarding_video_as_slim.mp4",
                                "onboarding_video_jp_makeup.mp4",
                                "onboarding_video_kr_th_makeup.mp4",
                                "onboarding_video_na_edit.mp4",
                                "onboarding_video_na_makeup.mp4",
                                "onboarding_video_na_slim.mp4",
                                "onboarding_video_as_slim.mp4",
                                "onboarding_video_as_edit.mp4",
                                "onboarding_video_na_slim.mp4",
                                "onboarding_video_na_makeup.mp4",
                                "onboarding_video_jp_makeup.mp4"
                            ), VideoListViewHolder::class.java
                        )
                        .build()
                )

                addOnScrollListener(object : ActiveScrollListener(mViewBinding.rv, CenterActiveStrategy(true), false) {
                    override fun getTargetView(position: Int): View? {
                        val holder = mViewBinding.rv.findViewHolderForLayoutPosition(position)
                        if (holder is VideoListViewHolder) {
                            return holder.itemView
                        }
                        return null
                    }

                    override fun onActive(position: Int) {
                        val holder = mViewBinding.rv.findViewHolderForLayoutPosition(position)
                        if (holder is VideoListViewHolder) {
                            holder.mViewBinding.container.play(PlaySource.createAssetPlaySource("video/${holder.item.entity}"))
                        }
                    }

                    override fun onDeActive(position: Int) {
                        val holder = mViewBinding.rv.findViewHolderForLayoutPosition(position)
                        if (holder is VideoListViewHolder) {
                            holder.mViewBinding.container.release()
                        }
                    }

                })
            }
        }
    }

    override fun initData() {
    }

    class VideoListViewHolder(context: Context?, parent: ViewGroup?) : XViewHolder<ItemVideoListBinding, String>(context, parent, R.layout.item_video_list) {
        override fun onBindViewHolder(position: Int, item: XItem<String>?, payloads: MutableList<Any>?) {
            super.onBindViewHolder(position, item, payloads)
            mViewBinding.container.prepareVideoCover(AssetsUtil.AssetsRoot + "video/${item?.entity}")

        }
    }
}