package com.sdy.jitangapplication.ui.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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
import kotlinx.android.synthetic.main.item_like_me_one_day.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   :
 *    version: 1.0
 */
class LikeMeOneDayAdapter : BaseQuickAdapter<LikeMeOneDayBean, BaseViewHolder>(R.layout.item_like_me_one_day) {
    override fun convert(holder: BaseViewHolder, item: LikeMeOneDayBean) {
        val itemView = holder.itemView

        val params = itemView.likeMeAvator.layoutParams as ConstraintLayout.LayoutParams
        params.width = SizeUtils.dp2px(180F)
        params.height = (16 / 9F * params.width).toInt()
        itemView.likeMeAvator.layoutParams = params
        itemView.likeMeTag.text = "${item.tag_title}"
        itemView.likeMeNickname.text = "${item.nickname}"
        itemView.likeMeInfo.text = "${item.age} / ${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }} / ${item.constellation} / ${item.distance} / ${item.job}"

        itemView.view.isVisible = !(item.is_read ?: true)
        if (UserManager.isUserVip()) {
            itemView.likeMeTagCover.visibility = View.GONE
            itemView.likeMeInfoCover.visibility = View.GONE
            itemView.likeMeNicknameCover.visibility = View.GONE
            itemView.likeMeType.visibility = View.VISIBLE
            GlideUtil.loadRoundImgCenterCrop(mContext, item.avatar, itemView.likeMeAvator, SizeUtils.dp2px(5F))

            holder.addOnClickListener(R.id.likeMeType)
            if (item.isfriend == 1)
                itemView.likeMeType.setImageResource(R.drawable.icon_chat_with_circle)
            else
                itemView.likeMeType.setImageResource(R.drawable.icon_like_with_circle)
        } else {
            itemView.likeMeType.visibility = View.INVISIBLE
            itemView.likeMeTagCover.visibility = View.VISIBLE
            itemView.likeMeInfoCover.visibility = View.VISIBLE
            itemView.likeMeNicknameCover.visibility = View.VISIBLE
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
                .into(itemView.likeMeAvator)
        }


    }
}