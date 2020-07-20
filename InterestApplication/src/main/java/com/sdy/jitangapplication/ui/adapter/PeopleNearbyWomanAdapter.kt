package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.model.UserRelationshipBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_people_nearby_woman.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyWomanAdapter() :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_nearby_woman) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView
        val params = itemView.layoutParams as RecyclerView.LayoutParams
        if (helper.layoutPosition == 0) {
            params.topMargin = SizeUtils.dp2px(8F)
        } else {
            params.topMargin = 0
        }

        itemView.layoutParams = params


        GlideUtil.loadCircleImg(mContext, item.avatar, itemView.userAvator)

        itemView.userNameAge.text = item.nickname
        itemView.userAgeDistance.text = "${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}·${item.constellation}·${item.distance}"

        itemView.userVerify.isVisible = item.isfaced == 1
        itemView.userVip.isVisible = item.isplatinumvip
        if (item.isplatinumvip) {
            itemView.userVip.setImageResource(R.drawable.icon_vip)
        }

        itemView.userOnline.text = "${if (!item.online_time.isNullOrEmpty()) {
            "${item.online_time}"
        } else {
            ""
        }}"

        if (!item.want.isNullOrEmpty() || !item.intention_title.isNullOrEmpty()) {
            itemView.userRelationshipRv.isVisible = true
            val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
            manager.alignItems = AlignItems.STRETCH
            manager.justifyContent = JustifyContent.FLEX_START
            itemView.userRelationshipRv.layoutManager = manager
            val adapter = UserRelationshipAdapter()
            itemView.userRelationshipRv.adapter = adapter

            val datas = mutableListOf<UserRelationshipBean>()
            if (!item.intention_title.isNullOrEmpty())
                datas.add(UserRelationshipBean(item.intention_title, 0))
            for (data in item.want) {
                datas.add(UserRelationshipBean(data, 1))
            }
            adapter.setNewData(datas)
            itemView.userRelationshipRv.setOnTouchListener { v, event ->
                itemView.onTouchEvent(event)
            }
        } else {
            itemView.userRelationshipRv.isVisible = false
        }

        //	0没有留下联系方式 1 电话 2 微信 3 qq 99隐藏
        when (item.contact_way) {
            1 -> {
                itemView.userContactBtn.isVisible = true
                itemView.userContactBtn.setImageResource(R.drawable.icon_phone_near_woman)
            }
            2 -> {
                itemView.userContactBtn.isVisible = true
                itemView.userContactBtn.setImageResource(R.drawable.icon_wechat_near_woman)
            }
            3 -> {
                itemView.userContactBtn.isVisible = true
                itemView.userContactBtn.setImageResource(R.drawable.icon_qq_near_woman)
            }
            else -> {
                itemView.userContactBtn.isVisible = false
            }
        }
        //搭讪
        itemView.userChatBtn.clickWithTrigger {
            if (UserManager.touristMode)
                TouristDialog(mContext).show()
            else
                CommonFunction.checkChat(mContext, item.accid)
        }
        //获取联系方式
        itemView.userContactBtn.clickWithTrigger {
            if (UserManager.touristMode)
                TouristDialog(mContext).show()
            else
                CommonFunction.checkUnlockContact(mContext, item.accid, item.gender)
        }

        //用户视频介绍
//        itemView.userIntroduceVideoBtn.clickWithTrigger {
//            if (UserManager.touristMode)
//                TouristDialog(mContext).show()
//            else
//                CommonFunction.checkUnlockIntroduceVideo(mContext, item.accid, item.gender)
//        }

        itemView.clickWithTrigger {
            if (UserManager.touristMode) {
                TouristDialog(mContext).show()
            } else {
                MatchDetailActivity.start(mContext, item.accid)
            }

        }


    }
}