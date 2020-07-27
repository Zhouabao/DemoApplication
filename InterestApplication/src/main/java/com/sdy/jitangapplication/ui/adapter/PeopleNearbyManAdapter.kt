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
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.model.UserRelationshipBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.ui.dialog.TouristDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_people_nearby.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   :
 *    version: 1.0
 */
class PeopleNearbyManAdapter(var fromCard: Boolean = false) :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_nearby) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView
        val params = itemView.layoutParams as RecyclerView.LayoutParams
        if (!fromCard) {
            params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
            params.height = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
            if (helper.layoutPosition == 0) {
                params.topMargin = SizeUtils.dp2px(10F)
            } else {
                params.topMargin = 0
            }
            params.bottomMargin = SizeUtils.dp2px(10F)
        } else {
            //44+ + 20 +x + 15
//            SizeUtils.dp2px(44 + 50 + 10 + 15 + 50F)
            params.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F)
            params.height =
                ScreenUtils.getScreenHeight() - SizeUtils.dp2px(44 + 20 + 10 + 15 + 50F)
            -((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15f * 2) - SizeUtils.dp2px(10 * 12F)) / 9 + SizeUtils.dp2px(
                12F * 2
            ))
            -StatusBarUtil.getStatusBarHeight(mContext)
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

        if (!fromCard) {
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

            if (item.privacy_state)
                itemView.userChatBtn.setImageResource(R.drawable.icon_hi_heartbeat)
            else
                itemView.userChatBtn.setImageResource(R.drawable.icon_chat_pt_vip)

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

        } else {
            itemView.userChatBtn.isVisible = false
            itemView.userContactBtn.isVisible = false
        }
    }
}