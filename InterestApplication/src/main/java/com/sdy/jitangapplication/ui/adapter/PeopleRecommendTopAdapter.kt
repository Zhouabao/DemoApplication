package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.NearPersonBean
import kotlinx.android.synthetic.main.item_people_recommend_top.view.*


/**
 *    author : ZFM
 *    date   : 2020/4/2710:36
 *    desc   : 首页顶部推荐人员
 *    version: 1.0
 */
class PeopleRecommendTopAdapter :
    BaseQuickAdapter<NearPersonBean, BaseViewHolder>(R.layout.item_people_recommend_top) {
    override fun convert(helper: BaseViewHolder, item: NearPersonBean) {
        val itemView = helper.itemView

        val params = itemView.layoutParams as RecyclerView.LayoutParams
        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 4f)) / 3

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
                itemView.userRankIv.setImageResource(R.drawable.icon_candy_no1)
            }
            1 -> {
                itemView.userRankIv.setImageResource(R.drawable.icon_candy_no2)
            }
            2 -> {
                itemView.userRankIv.setImageResource(R.drawable.icon_candy_no3)
            }
        }


    }
}