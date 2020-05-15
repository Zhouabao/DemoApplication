package com.sdy.jitangapplication.ui.adapter

import android.graphics.Typeface
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_people_nearby_photos.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2711:06
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyPhotosAdapter(val accid: String, var hasGift: Boolean = true) :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_people_nearby_photos) {
    var plusPhotos = 0
    override fun convert(helper: BaseViewHolder, item: String) {
        val layoutParams = helper.itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)) / 5
        layoutParams.height = layoutParams.width
        helper.itemView.layoutParams = layoutParams

        if (hasGift) {
            if (plusPhotos > 0 && helper.layoutPosition == mData.size - 1) {
                helper.itemView.nearPeopleMorePhoto.isVisible = true
                helper.itemView.nearPeopleMorePhoto.setBackgroundResource(R.drawable.shape_rectangle_halfblack)
                helper.itemView.nearPeopleMorePhoto.text =
                    SpanUtils.with(helper.itemView.nearPeopleMorePhoto).append("+")
                        .append("$plusPhotos")
                        .setTypeface(
                            Typeface.createFromAsset(
                                mContext.assets,
                                "DIN_Alternate_Bold.ttf"
                            )
                        ).create()
            }
            GlideUtil.loadImg(
                mContext,
                item,
                helper.itemView.nearPeoplePhoto
            )
        } else {
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
                    if (plusPhotos > 0) {
                        helper.itemView.nearPeopleMorePhoto.isVisible = true
                        helper.itemView.nearPeopleMorePhoto.text =
                            SpanUtils.with(helper.itemView.nearPeopleMorePhoto).append("+")
                                .append("$plusPhotos")
                                .setTypeface(
                                    Typeface.createFromAsset(
                                        mContext.assets,
                                        "DIN_Alternate_Bold.ttf"
                                    )
                                ).create()
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
                    if (helper.layoutPosition < 4) {
                        GlideUtil.loadImg(
                            mContext,
                            item,
                            helper.itemView.nearPeoplePhoto
                        )
                    }
                }
            }
        }


        helper.itemView.clickWithTrigger {
            MatchDetailActivity.start(mContext, accid)
        }
    }
}