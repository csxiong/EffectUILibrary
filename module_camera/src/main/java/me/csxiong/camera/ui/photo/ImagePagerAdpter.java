package me.csxiong.camera.ui.photo;

import androidx.core.view.ViewCompat;

import me.csxiong.camera.R;
import me.csxiong.camera.album.ImageEntity;
import me.csxiong.camera.databinding.ItemPagerImageBinding;
import me.csxiong.camera.ui.imageloader.ImageLoader;
import me.csxiong.library.integration.adapter.XPagerAdapter;

public class ImagePagerAdpter extends XPagerAdapter<ItemPagerImageBinding, ImageEntity> {

    @Override
    public int getLayoutId() {
        return R.layout.item_pager_image;
    }

    @Override
    public void onBindView(ItemPagerImageBinding mViewBinding, int position) {
        ImageEntity entity = dataList.get(position);
        ViewCompat.setTransitionName(mViewBinding.iv, entity.getDisplayPath());

        ImageLoader.url(entity.getDisplayPath())
                .into(mViewBinding.iv);
    }

}
