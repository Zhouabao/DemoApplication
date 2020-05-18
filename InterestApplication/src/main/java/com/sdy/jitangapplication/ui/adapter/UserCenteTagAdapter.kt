package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.LabelQuality
import kotlinx.android.synthetic.main.item_tag_usercenter.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的兴趣管理
 *    version: 1.0
 */
class UserCenteTagAdapter :
    BaseQuickAdapter<LabelQuality, BaseViewHolder>(R.layout.item_tag_usercenter) {

    override fun convert(holder: BaseViewHolder, item: LabelQuality) {
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(43F)) / 2

        if ((holder.layoutPosition - headerLayoutCount) % 2 == 0) {
            params.leftMargin = SizeUtils.dp2px(15F)
            params.rightMargin = SizeUtils.dp2px(13F)
        } else if ((holder.layoutPosition - headerLayoutCount) % 2 == 1) {
            params.leftMargin = 0
            params.rightMargin = SizeUtils.dp2px(15F)
        }
        holder.itemView.layoutParams = params

        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item.icon,
            holder.itemView.tagIcon,
            SizeUtils.dp2px(10F)
        )
        holder.itemView.tagName.text = item.title
    }

}