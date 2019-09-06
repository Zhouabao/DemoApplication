package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LikeMeOneDayBean
import com.sdy.jitangapplication.utils.UserManager
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import kotlinx.android.synthetic.main.item_like_me_one_day_all.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   :
 *    version: 1.0
 */
class LikeMeOneDayGirdAdapter : BaseQuickAdapter<LikeMeOneDayBean, BaseViewHolder>(R.layout.item_like_me_one_day_all) {
    private val SPAN_COUNT = 3
    override fun convert(holder: BaseViewHolder, item: LikeMeOneDayBean) {
        val itemView = holder.itemView
        val params = itemView.layoutParams as RecyclerView.LayoutParams
        params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - (SPAN_COUNT - 1) * SizeUtils.dp2px(10F)) * 1.0F / SPAN_COUNT).toInt()
        params.height = (16 / 9F * params.width).toInt()
        itemView.layoutParams = params
        if (UserManager.isUserVip()) {
            itemView.likeMeOneDayType.visibility = View.VISIBLE
            GlideUtil.loadRoundImgCenterCrop(mContext, item.avatar, itemView.likeMeOneDayAvator, SizeUtils.dp2px(5F))
        } else {
            itemView.likeMeOneDayType.visibility = View.INVISIBLE
            val transformation = MultiTransformation(
                CenterCrop(),
                BlurTransformation(SizeUtils.dp2px(25F)),
                RoundedCornersTransformation(SizeUtils.dp2px(5F), 0)
            )
            Glide.with(mContext)
                .load(item.avatar ?: "")
                .priority(Priority.NORMAL)
                .thumbnail(0.5F)
                .transform(transformation)
                .into(itemView.likeMeOneDayAvator)

        }


    }
}