package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.GiftBean
import kotlinx.android.synthetic.main.item_gift_usercenter.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的礼礼物墙
 *    version: 1.0
 */
class UserCenteGiftAdapter :
    BaseQuickAdapter<GiftBean, BaseViewHolder>(R.layout.item_gift_usercenter) {

    override fun convert(holder: BaseViewHolder, item: GiftBean) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        if ((holder.layoutPosition - headerLayoutCount) % 2 == 0) {
            params.leftMargin = SizeUtils.dp2px(15F)
            params.rightMargin = SizeUtils.dp2px(10F)
        } else if ((holder.layoutPosition - headerLayoutCount) % 2 == 1) {
            params.rightMargin = SizeUtils.dp2px(15F)
            params.leftMargin = 0
        }
        holder.itemView.layoutParams = params

        GlideUtil.loadImg(
            mContext,
            item.icon,
            holder.itemView.giftIcon
        )
    }

}