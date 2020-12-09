package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_user_center_visit_cover.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的动态封面适配器
 *    version: 1.0
 */
class VisitPayChatAvatorAdater : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_user_center_visit_cover) {

    override fun convert(holder: BaseViewHolder, item: String) {
        val params = holder.itemView.visitCoverImg.layoutParams as RecyclerView.LayoutParams
        params.setMargins(if (holder.layoutPosition != 0) SizeUtils.dp2px(-15F) else 0, 0, 0, 0)

        params.width = SizeUtils.dp2px(58F)
        params.height = SizeUtils.dp2px(58F)

        holder.itemView.visitCoverImg.layoutParams = params
        //如果不是会员，就高斯模糊看过我的
        GlideUtil.loadImg(mContext, item, holder.itemView.visitCoverImg)

    }

}
