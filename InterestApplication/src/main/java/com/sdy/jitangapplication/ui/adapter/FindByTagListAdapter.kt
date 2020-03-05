package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.kotlin.base.ext.onClick
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.MatchBean
import kotlinx.android.synthetic.main.item_find_by_tag.view.*

class FindByTagListAdapter(var hasmore: Boolean = true) :
    BaseQuickAdapter<MatchBean, BaseViewHolder>(R.layout.item_find_by_tag) {
    override fun convert(helper: BaseViewHolder, item: MatchBean) {
        if (helper.layoutPosition % 2 == 0) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = SizeUtils.dp2px(15f)
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).rightMargin = SizeUtils.dp2px(10F)
        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).leftMargin = SizeUtils.dp2px(0f)
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).rightMargin = SizeUtils.dp2px(15F)
        }
        if (helper.layoutPosition / 2 == 0) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).topMargin = SizeUtils.dp2px(15f)
        } else {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).topMargin = SizeUtils.dp2px(10f)
        }
        if (helper.layoutPosition / 2 == (mData.size - 1) / 2 && !hasmore) {
            (helper.itemView.layoutParams as RecyclerView.LayoutParams).bottomMargin = SizeUtils.dp2px(10f)
        }
        GlideUtil.loadRoundImgCenterCrop(mContext, item.avatar, helper.itemView.sameIv, SizeUtils.dp2px(10F))
        helper.itemView.sameName.text = item.nickname
        helper.itemView.sameInfo.text = "${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }}·${item.age}·${item.constellation}·${item.distance}"


        if (!item.newtags.isNullOrEmpty()) {
            helper.itemView.rvLabelQuality.isVisible = true
            val manager = FlexboxLayoutManager(mContext, FlexDirection.ROW, FlexWrap.WRAP)
            manager.alignItems = AlignItems.STRETCH
            manager.justifyContent = JustifyContent.FLEX_START
            helper.itemView.rvLabelQuality.layoutManager = manager
            val adapter = MatchDetailLabelQualityAdapter(false)
            helper.itemView.rvLabelQuality.adapter = adapter
            adapter.setNewData(item.newtags!![0].label_quality)
        } else
            helper.itemView.rvLabelQuality.isVisible = false
        helper.itemView.sameLike.onClick {
            CommonFunction.commonGreet(mContext, item.accid, helper.itemView.sameLike, helper.layoutPosition,item.avatar?:"")
        }
    }

}