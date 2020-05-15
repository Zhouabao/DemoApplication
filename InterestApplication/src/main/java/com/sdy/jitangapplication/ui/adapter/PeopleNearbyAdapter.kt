package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.NearPersonBean
import com.sdy.jitangapplication.ui.activity.MatchDetailActivity
import com.sdy.jitangapplication.widgets.CustomPagerSnapHelper
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

        GlideUtil.loadCircleImg(mContext, item.avatar, itemView.nearPeopleAvator)
        itemView.nearPeopleName.text = item.nickname
        itemView.nearPeopleSign.text = item.sign
        itemView.nearPeopleVerify.isVisible = item.isfaced
        itemView.nearPeopleVip.isVisible = item.isvip
        if (item.intention_title.isNullOrEmpty()) {
            itemView.nearPeopleIntention.isVisible = false
            itemView.nearPeopleAvator.borderWidth = 0
        } else {
            itemView.nearPeopleIntention.isVisible = true
            itemView.nearPeopleIntention1.text = item.intention_title
            itemView.nearPeopleAvator.borderWidth = SizeUtils.dp2px(1F)
        }

        itemView.nearPeopleDistance.text =
            "${item.distance}${if (!item.online_time.isNullOrEmpty()) {
                "·${item.online_time}"
            } else {
                ""
            }}"
        itemView.nearPeopleGender.text = "${item.age}岁"
        if (item.gender == 1) {
            itemView.nearPeopleGender.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    R.drawable.icon_gender_man_near_people
                ), null, null, null
            )
        } else {
            itemView.nearPeopleGender.setCompoundDrawablesWithIntrinsicBounds(
                mContext.resources.getDrawable(
                    R.drawable.icon_gender_woman_near_people
                ), null, null, null
            )
        }
        itemView.nearPeopleCollapstation.text = item.constellation
        if (item.photos.isNotEmpty()) {
            itemView.nearPeoplePhotos.isVisible = true
            itemView.nearPeoplePhotos.layoutManager = GridLayoutManager(mContext, 5)
            val adapter = PeopleNearbyPhotosAdapter(item.accid, item.wish_data.isNotEmpty())
            itemView.nearPeoplePhotos.adapter = adapter
            adapter.plusPhotos = item.plus_photo
            adapter.setNewData(item.photos)
        } else {
            itemView.nearPeoplePhotos.isVisible = false
        }


        itemView.clickWithTrigger {
            MatchDetailActivity.start(mContext, item.accid)
        }


        if (item.wish_data.isNullOrEmpty()) {
            itemView.nearPeopleGifts.isVisible = false
        } else {
            itemView.nearPeopleGifts.isVisible = true
            itemView.nearPeopleGifts.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            val giftAdapter = PeopleNearByWishGiftAdapter(item.accid)
            //增加这一段即可
            if (itemView.nearPeopleGifts.onFlingListener == null) {
                CustomPagerSnapHelper().attachToRecyclerView(itemView.nearPeopleGifts)
            }
            itemView.nearPeopleGifts.adapter = giftAdapter
            giftAdapter.setNewData(item.wish_data)
        }
        itemView.nearPeopleGifts.setOnTouchListener { v, event -> itemView.onTouchEvent(event) }
        itemView.nearPeoplePhotos.setOnTouchListener { v, event -> itemView.onTouchEvent(event) }

        //打招呼
        //1.男性打招呼 首先判断是不是会员 不是会员拉起付费弹窗
        //                                是的话就判断女性有没有添加意向  添加了意向就弹起助力和糖果弹窗
        //                                                               未添加就弹起糖果赠送弹窗
        //
        //2.女性打招呼 不管男方有无意愿，都判断认证开关，如果开关开启就判断女性有没有认证 认证了就直接送出招呼
        //                                                                            未认证就弹起认证弹窗
        itemView.hiBtn.clickWithTrigger {
            CommonFunction.commonGreet(
                mContext,
                item.accid,
                itemView.hiBtn,
                helper.layoutPosition,
                item.avatar,
                false
            )
        }
    }
}