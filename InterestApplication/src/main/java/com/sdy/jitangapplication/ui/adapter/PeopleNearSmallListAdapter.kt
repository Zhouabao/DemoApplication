package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
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
import kotlinx.android.synthetic.main.item_people_nearby_small_list.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class PeopleNearSmallListAdapter :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_nearby_small_list) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView
        val params = itemView.userContentCl.layoutParams as ConstraintLayout.LayoutParams
        if (helper.layoutPosition == 0) {
            params.topMargin = SizeUtils.dp2px(8F)
        } else {
            params.topMargin = 0
        }

        itemView.userContentCl.layoutParams = params


        //0 不是甜心圈 1 资产认证 2豪车认证 3身材 4职业  5高额充值
        if (item.assets_audit_way != 0) {
            if (item.assets_audit_way == 1 || item.assets_audit_way == 2|| item.assets_audit_way == 5) {
                itemView.userContentCl.setBackgroundResource(R.drawable.icon_sweet_heart_bg_list_man)
                itemView.userNameAge.setTextColor(Color.parseColor("#FFCD52"))
                itemView.userOnline.setTextColor(Color.parseColor("#66FFCE52"))
                itemView.userAgeDistance.setTextColor(Color.parseColor("#66FFCE52"))
                itemView.userChatBtn.setImageResource(R.drawable.icon_hi_heartbeat)
                itemView.sweetAnimation.setAnimation("data_sweet_style_list_man.json")
                itemView.sweetAnimation.playAnimation()
            } else {
                itemView.userContentCl.setBackgroundResource(R.drawable.icon_sweet_heart_bg_list_woman)
                itemView.userNameAge.setTextColor(Color.parseColor("#FFFFFFFF"))
                itemView.userOnline.setTextColor(Color.parseColor("#80ffffff"))
                itemView.userAgeDistance.setTextColor(Color.parseColor("#80ffffff"))
                itemView.userChatBtn.setImageResource(R.drawable.icon_chat_woman)
                itemView.sweetAnimation.setAnimation("data_sweet_style_list_woman.json")
                itemView.sweetAnimation.playAnimation()
            }
        } else {
            itemView.userContentCl.setBackgroundResource(R.drawable.icon_bg_near_people_woman)
            itemView.userNameAge.setTextColor(Color.parseColor("#191919"))
            itemView.userOnline.setTextColor(Color.parseColor("#FFC5C6C8"))
            itemView.userAgeDistance.setTextColor(Color.parseColor("#FFC5C6C8"))
            itemView.userChatBtn.setImageResource(R.drawable.icon_hi_heartbeat)
        }


        GlideUtil.loadCircleImg(mContext, item.avatar, itemView.userAvator)

        itemView.userNameAge.text = item.nickname

        itemView.userAgeDistance.text = "${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}·${item.constellation}·${item.distance}"

        if (item!!.isfaced == 1) {
            itemView.userVerify.isVisible = true
            itemView.userVerify.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    if (item!!.gender == 1) {
                        R.drawable.icon_gender_man_detail
                    } else {
                        R.drawable.icon_gender_woman_detail
                    }
                ), null, null, null
            )
            if (item!!.face_str.isNullOrEmpty()) {
                itemView.userVerify.text = "已认证"
            } else {
                itemView.userVerify.text = item!!.face_str
            }
        } else {
            itemView.userVerify.isVisible = false
        }
        itemView.userVip.isVisible = item.isplatinumvip || item.isdirectvip
        if (item.isplatinumvip) {
            itemView.userVip.setImageResource(R.drawable.icon_vip)
        } else if (item.isdirectvip) {
            itemView.userVip.setImageResource(R.drawable.icon_direct_vip)
        }

        itemView.userOnline.text = "${if (!item.online_time.isNullOrEmpty()) {
            "${item.online_time}"
        } else {
            ""
        }}"

        ////0 不是甜心圈 1 资产认证 2豪车认证 3身材 4职业  5高额充值
        if (item.assets_audit_way != 0 || !item.want.isNullOrEmpty() || !item.intention_title.isNullOrEmpty()) {
            itemView.userRelationshipRv.isVisible = true
            val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
            manager.alignItems = AlignItems.STRETCH
            manager.justifyContent = JustifyContent.FLEX_START
            itemView.userRelationshipRv.layoutManager = manager
            val adapter = UserRelationshipAdapter(item.assets_audit_way)
            itemView.userRelationshipRv.adapter = adapter

            val datas = mutableListOf<UserRelationshipBean>()
            if (!item.title.isNullOrEmpty())
                datas.add(UserRelationshipBean(item.title, 0, item.invitation_id))

            if (item.assets_audit_way != 0)
                datas.add(
                    UserRelationshipBean(
                        when (item.assets_audit_way) {
                            1 -> {
                                "资产认证进入甜心圈"
                            }
                            2 -> {
                                "豪车认证进入甜心圈"
                            }
                            3 -> {
                                "身材认证进入甜心圈"
                            }
                            4 -> {
                                "职业认证进入甜心圈"
                            }
                            else -> {
                                "高额充值进入甜心圈"
                            }
                        },
                        2,
                        item.assets_audit_way
                    )
                )

            for (data in item.want) {
                datas.add(UserRelationshipBean(data, 1))
            }
            adapter.setNewData(datas)
            if (adapter.data.size > 5) {
                adapter.remove(adapter.data.size - 1)
            }
            itemView.userRelationshipRv.setOnTouchListener { _, event ->
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