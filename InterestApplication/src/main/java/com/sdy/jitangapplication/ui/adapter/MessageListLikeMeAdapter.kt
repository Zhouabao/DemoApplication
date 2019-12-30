package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.Likelist
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_message_likelist.view.*


/**
 * 对我感兴趣的适配器
 */
class MessageListLikeMeAdapter : BaseQuickAdapter<Likelist, BaseViewHolder>(R.layout.item_message_likelist) {
    public var freeShow: Boolean = false
    override fun convert(helper: BaseViewHolder, item: Likelist) {
        helper.addOnClickListener(R.id.likeMeAvator)
        helper.addOnClickListener(R.id.likeMeAvatorBtn)
        val itemView = helper.itemView
        if (freeShow) {
            GlideUtil.loadCircleImg(mContext, item.avatar, itemView.likeMeAvator)
            itemView.likeMeAvatorBtn.isVisible = true
        } else {
            itemView.likeMeAvatorBtn.isVisible = false
            val transformation = MultiTransformation(
                CenterCrop(),
                CircleCrop(),
                BlurTransformation(SizeUtils.dp2px(10F))
            )
            Glide.with(mContext)
                .load(item.avatar ?: "")
                .priority(Priority.LOW)
                .thumbnail(0.5F)
                .transform(transformation)
                .into(itemView.likeMeAvator)
        }
        if (item.isfriend) {
            itemView.likeMeAvatorBtn.setImageResource(R.drawable.icon_chat_message_center)
        } else {
            itemView.likeMeAvatorBtn.setImageResource(R.drawable.icon_like_message_center)
        }
    }


}