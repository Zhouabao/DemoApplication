package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.IndexTopBean
import kotlinx.android.synthetic.main.item_people_recommend_top.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   : 首页顶部推荐人员
 *    version: 1.0
 */
class PeopleRecommendTopAdapter :
    BaseQuickAdapter<IndexTopBean, BaseViewHolder>(R.layout.item_people_recommend_top) {
    override fun convert(helper: BaseViewHolder, item: IndexTopBean) {
        val itemView = helper.itemView

        val params = itemView.layoutParams as RecyclerView.LayoutParams
        params.height = SizeUtils.dp2px(146F)
        params.width = (SizeUtils.dp2px(146F) / 5 * 4f).toInt()
        if (helper.layoutPosition == mData.size - 1) {
            params.rightMargin = SizeUtils.dp2px(15F)
        }

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.avatar,
            itemView.userAvator,
            SizeUtils.dp2px(10F)
        )

        itemView.userName.text = item.nickname
        itemView.userAgeGender.text = "${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}·${item.age}岁·${item.distance}"

        when (helper.layoutPosition) {
            0 -> {
                itemView.userRankIv.isVisible = true
                itemView.userRankIv.setImageResource(R.drawable.icon_candy_no1)
            }
            1 -> {
                itemView.userRankIv.isVisible = true
                itemView.userRankIv.setImageResource(R.drawable.icon_candy_no2)
            }
            2 -> {
                itemView.userRankIv.isVisible = true
                itemView.userRankIv.setImageResource(R.drawable.icon_candy_no3)
            }
            else->{
                itemView.userRankIv.isVisible = false
            }
        }


    }
}