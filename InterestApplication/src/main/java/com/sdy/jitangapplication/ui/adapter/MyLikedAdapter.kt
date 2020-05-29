package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kotlin.base.common.BaseApplication.Companion.context
import com.netease.nim.uikit.impl.NimUIKitImpl
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.MyLikedBean
import kotlinx.android.synthetic.main.item_my_liked.view.*

/**
 *    author : ZFM
 *    date   : 2019/7/1516:34
 *    desc   :我喜欢过的人
 *    version: 1.0
 */
class MyLikedAdapter : BaseQuickAdapter<MyLikedBean, BaseViewHolder>(R.layout.item_my_liked) {

    override fun convert(holder: BaseViewHolder, item: MyLikedBean) {
        GlideUtil.loadAvatorImg(context, item.avatar, holder.itemView.likedIcon)
        holder.itemView.likedName.text = item.nickname
        holder.itemView.likedSign.text = item.sign
        holder.itemView.likedSign.isVisible = !item.sign.isNullOrEmpty()
        holder.itemView.likedIsVip.isVisible = item.isvip
        holder.itemView.likedAge.text = "${item.age}岁"
        if (item.gender == 1) {
            holder.itemView.likedAge.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    R.drawable.icon_gender_man_gray
                ), null, null, null
            )
        } else {
            holder.itemView.likedAge.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    R.drawable.icon_gender_woman_gray
                ), null, null, null
            )
        }
        holder.itemView.likedCollapstation.text = item.constellation

        holder.itemView.likedOnlineState.isVisible =
            NimUIKitImpl.enableOnlineState()
                    && !NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).isNullOrEmpty()
                    && NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(item.accid).contains(
                "在线"
            )

        holder.itemView.likedHi.clickWithTrigger {
            CommonFunction.checkSendGift(mContext, item.accid)
        }
    }

}