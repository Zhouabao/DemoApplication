package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NearPersonBean
import kotlinx.android.synthetic.main.item_today_fate.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   : 喜欢我的
 *    version: 1.0
 */
class TodayFateAdapter :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_today_fate) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.avatar,
            itemView.userAvator,
            SizeUtils.dp2px(10F)
        )

        SpanUtils.with(itemView.userNameAge)
            .append(item.nickname)
            .setFontSize(19, true)
            .append(" ${item.age}岁")
            .create()
        itemView.userVerify.isVisible = item.isfaced == 1
        itemView.userIntroduceVideoBtn.isVisible = item.mv_btn

        itemView.userVip.isVisible = item.isvip || item.isplatinumvip
        if (item.isplatinumvip) {
            itemView.userVip.setImageResource(R.drawable.icon_pt_vip)
        } else {
            itemView.userVip.setImageResource(R.drawable.icon_vip)
        }
        if (item.intention_title.isNullOrEmpty()) {
            itemView.userIntention.isVisible = false
        } else {
            itemView.userIntention.isVisible = true
            itemView.userIntentionContent.text = item.intention_title
//            GlideUtil.loadCircleImg(mContext, item.intention_icon, itemView.userIntentionIcon)
        }

        itemView.userOnline.text =
            "${item.distance}${if (!item.online_time.isNullOrEmpty()) {
                ",\t${item.online_time}"
            } else {
                ""
            }}"

        if (!item.want.isNullOrEmpty()) {
            itemView.userRelationshipRv.isVisible = true
            itemView.userRelationshipRv.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            val adapter = UserRelationshipAdapter()
            itemView.userRelationshipRv.adapter = adapter
            adapter.setNewData(item.want)
            itemView.userRelationshipRv.setOnTouchListener { v, event ->
                itemView.onTouchEvent(event)
            }
        } else {
            itemView.userRelationshipRv.isVisible = false
        }

        itemView.userChatBtn.isVisible = false
        itemView.userContactBtn.isVisible = false
    }
}