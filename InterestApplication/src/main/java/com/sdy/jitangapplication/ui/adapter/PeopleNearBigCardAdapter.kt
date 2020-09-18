package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
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
import com.sdy.jitangapplication.model.UserRelationshipBean
import com.sdy.jitangapplication.ui.activity.DatingDetailActivity
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_people_nearby_big_card.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class PeopleNearBigCardAdapter :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_nearby_big_card) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView
        val userAvatorPrams = itemView.userAvator.layoutParams as ConstraintLayout.LayoutParams
        userAvatorPrams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
        userAvatorPrams.height = userAvatorPrams.width
        userAvatorPrams.leftMargin = SizeUtils.dp2px(10F)
        userAvatorPrams.rightMargin = SizeUtils.dp2px(10F)


        val params = itemView.layoutParams as RecyclerView.LayoutParams
        ////0 不是甜心圈 1 资产认证 2豪车认证 3身材 4职业  5高额充值
        if (item.assets_audit_way != 0) {
            itemView.sweetHeartContent.isVisible = true
            itemView.itemBgIv.isVisible = true
            itemView.itemBgIvAnimation.isVisible = true

            itemView.sweetHeartContent.text = when (item.assets_audit_way) {
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
            }
            if (item.gender == 1) {
                itemView.sweetHeartContent.setBackgroundResource(R.drawable.icon_sweet_heart_verify_man_bg)
                itemView.sweetHeartContent.setTextColor(Color.parseColor("#FFFFCD52"))

                itemView.itemBgIvAnimation.imageAssetsFolder = "images_sweet_style_card_man"
                itemView.itemBgIvAnimation.setAnimation("data_sweet_style_card_man.json")
                itemView.itemBgIv.setImageResource(R.drawable.icon_sweet_heart_card_man_bg)
            } else {
                itemView.sweetHeartContent.setBackgroundResource(R.drawable.icon_sweet_heart_verify_woman_bg)
                itemView.sweetHeartContent.setTextColor(Color.WHITE)

                itemView.itemBgIv.setImageResource(R.drawable.icon_sweet_heart_card_woman_bg)
                itemView.itemBgIvAnimation.imageAssetsFolder = "images_sweet_style_card_woman"
                itemView.itemBgIvAnimation.setAnimation("data_sweet_style_card_woman.json")

            }
//            itemView.itemBgIvAnimation.playAnimation()


            val itemBgIvParams = (itemView.itemBgIv.layoutParams as ConstraintLayout.LayoutParams)
            itemBgIvParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(5 * 2F)
            itemBgIvParams.height = (itemBgIvParams.width * (1110 / 1095F)).toInt()

            userAvatorPrams.topMargin = SizeUtils.dp2px(22F)

            params.topMargin = SizeUtils.dp2px(5F)
            params.leftMargin = SizeUtils.dp2px(5F)
            params.rightMargin = SizeUtils.dp2px(5F)
        } else {
            itemView.sweetHeartContent.isVisible = false
            itemView.itemBgIv.isVisible = false

            userAvatorPrams.topMargin = SizeUtils.dp2px(0F)

            params.topMargin = SizeUtils.dp2px(10F)
            params.leftMargin = SizeUtils.dp2px(15F)
            params.rightMargin = SizeUtils.dp2px(15F)
        }

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



        itemView.userIntroduceVideoBtn.isVisible = item.mv_btn

        itemView.userVip.isVisible = item.isplatinumvip
        if (item.isplatinumvip) {
            itemView.userVip.setImageResource(R.drawable.icon_vip)
        } else if (item.isdirectvip) {
            itemView.userVip.setImageResource(R.drawable.icon_direct_vip)
        }
        if (item.title.isNullOrEmpty()) {
            itemView.userIntention.isVisible = false
        } else {
            itemView.userIntention.isVisible = true
            itemView.userIntentionContent.text = "她想和你：${item.title}·${item.dating_title}"
            itemView.userIntention.clickWithTrigger {
                DatingDetailActivity.start2Detail(mContext, item.invitation_id)
            }
//            GlideUtil.loadCircleImg(mContext, item.intention_icon, itemView.userIntentionIcon)
        }

        itemView.userOnline.text =
            "${item.distance}${if (!item.online_time.isNullOrEmpty()) {
                ",\t${item.online_time}"
            } else {
                ""
            }}"

        if (!item.want.isNullOrEmpty() && (item.assets_audit_way == 0 || item.title.isNullOrEmpty())) {
            itemView.userRelationshipRv.isVisible = true
            itemView.userRelationshipRv.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            val adapter = UserRelationshipAdapter()
            itemView.userRelationshipRv.adapter = adapter
            val datas = mutableListOf<UserRelationshipBean>()
            for (data in item.want) {
                datas.add(UserRelationshipBean(data))
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
        //搭讪
        if (item.private_chat_state)
            itemView.userChatBtn.setImageResource(R.drawable.icon_chat_pt_vip)
        else
            itemView.userChatBtn.setImageResource(R.drawable.icon_hi_heartbeat)

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
        itemView.userIntroduceVideoBtn.clickWithTrigger {
            //                CommonFunction.startToVideoIntroduce(mContext)

            if (UserManager.touristMode)
                TouristDialog(mContext).show()
            else
                CommonFunction.checkUnlockIntroduceVideo(mContext, item.accid)
        }

        itemView.clickWithTrigger {
            if (UserManager.touristMode) {
                TouristDialog(mContext).show()
            } else {
                MatchDetailActivity.start(mContext, item.accid)
            }

//                val intent = Intent(mContext, MatchDetailActivity::class.java)
//                intent.putExtra("target_accid", item.accid)
//
//                val bundler = ActivityOptionsCompat.makeSceneTransitionAnimation(
//                    mContext as Activity,
//                    itemView.userAvator, "matchdetailImg"
//                ).toBundle()
//                mContext.startActivity(intent, bundler)

        }


    }
}