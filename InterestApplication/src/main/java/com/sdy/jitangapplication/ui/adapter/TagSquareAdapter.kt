package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquarePicBean
import com.sdy.jitangapplication.model.SquareTagBean
import kotlinx.android.synthetic.main.item_layout_tag_square.view.*

/**
 * 兴趣标签适配器
 */
class TagSquareAdapter : BaseQuickAdapter<SquareTagBean, BaseViewHolder>(R.layout.item_layout_tag_square) {
    override fun convert(helper: BaseViewHolder, item: SquareTagBean) {
        val itemview = helper.itemView
        itemview.rvTagSquareImg.layoutManager = GridLayoutManager(mContext, 3)
        val adapter = TagSquarePicAdapter(3)
        for (data in item.cover_list) {
            adapter.addData(SquarePicBean(data))
        }
        itemview.rvTagSquareImg.adapter = adapter
        GlideUtil.loadCircleImg(mContext, item.icon, itemview.tagImg)
        itemview.tagName.text = item.title
        if (item.is_hot) {
            itemview.tagIsHot.text = "热门标签"
            itemview.tagIsHot.isVisible = true
        } else if (item.is_join) {
            itemview.tagIsHot.text = "已加入的标签"
            itemview.tagIsHot.isVisible = true
        } else {
            itemview.tagIsHot.isVisible = false
        }
    }
}