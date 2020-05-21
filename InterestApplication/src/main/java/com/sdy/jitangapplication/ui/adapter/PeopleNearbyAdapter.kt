package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.SpanUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import kotlinx.android.synthetic.main.item_people_nearby.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyAdapter :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_nearby) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView
        val params = itemView.layoutParams as RecyclerView.LayoutParams
        params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
        params.height = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
        if (helper.layoutPosition == 0)
            params.topMargin = SizeUtils.dp2px(15F)
        params.bottomMargin = SizeUtils.dp2px(10F)
        itemView.layoutParams = params

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
        itemView.userVip.isVisible = item.isvip
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


        //	0没有留下联系方式 1 电话 2 微信 3 qq 99隐藏
        when (item.contact_way) {
            1 -> {
                itemView.userContactBtn.isVisible = true
                itemView.userContactBtn.setImageResource(R.drawable.icon_phone_heartbeat)
            }
            2 -> {
                itemView.userContactBtn.isVisible = true
                itemView.userContactBtn.setImageResource(R.drawable.icon_wechat_heartbeat)
            }
            3 -> {
                itemView.userContactBtn.isVisible = true
                itemView.userContactBtn.setImageResource(R.drawable.icon_qq_heartbeat)
            }
            else -> {
                itemView.userContactBtn.isVisible = false
            }
        }


        //获取联系方式
        itemView.userContactBtn.clickWithTrigger {
            CommonFunction.checkUnlockContact(mContext, item.accid, item.gender)
        }

        itemView.clickWithTrigger {
            MatchDetailActivity.start(mContext, item.accid)
        }


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


        //打招呼
        //1.男性打招呼 首先判断是不是会员 不是会员拉起付费弹窗
        //                                是的话就判断女性有没有添加意向  添加了意向就弹起助力和糖果弹窗
        //                                                               未添加就弹起糖果赠送弹窗
        //
        //2.女性打招呼 不管男方有无意愿，都判断认证开关，如果开关开启就判断女性有没有认证 认证了就直接送出招呼
        //                                                                            未认证就弹起认证弹窗
        itemView.userChatBtn.clickWithTrigger {
            CommonFunction.commonGreet(
                mContext,
                item.accid,
                itemView.userChatBtn,
                helper.layoutPosition,
                item.avatar,
                false
            )
        }


    }
}