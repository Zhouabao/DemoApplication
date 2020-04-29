package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_people_nearby_photos.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2711:06
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyPhotosAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_people_nearby_photos) {
    var dataSize = 0
    override fun convert(helper: BaseViewHolder, item: String) {
        val layoutParams = helper.itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)) / 5
        layoutParams.height = layoutParams.width
        helper.itemView.layoutParams = layoutParams

        when (helper.layoutPosition) {
            0 -> {
                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    item,
                    helper.itemView.nearPeoplePhoto,
                    SizeUtils.dp2px(10F),
                    RoundedCornersTransformation.CornerType.BOTTOM_LEFT
                )
            }
            4 -> {
                if (dataSize > 5) {
                    helper.itemView.nearPeopleMorePhoto.isVisible = true
                    helper.itemView.nearPeopleMorePhoto.text = "+${dataSize - 5}"
                }
                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    item,
                    helper.itemView.nearPeoplePhoto,
                    SizeUtils.dp2px(10F),
                    RoundedCornersTransformation.CornerType.BOTTOM_RIGHT
                )
            }
            else -> {
                if (helper.layoutPosition < 4)
                    GlideUtil.loadImg(
                        mContext,
                        item,
                        helper.itemView.nearPeoplePhoto
                    )
            }
        }
    }
}