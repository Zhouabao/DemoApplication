package com.sdy.jitangapplication.ui.adapter

import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyLabelBean
import kotlinx.android.synthetic.main.item_tag_usercenter.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的兴趣管理
 *    version: 1.0
 */
class UserCenteTagAdapter : BaseQuickAdapter<MyLabelBean, BaseViewHolder>(R.layout.item_tag_usercenter) {

    override fun convert(holder: BaseViewHolder, item: MyLabelBean) {
        GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, holder.itemView.tagIcon, SizeUtils.dp2px(10F))
        holder.itemView.tagName.text = item.title
        holder.itemView.tagEdit.text = when {
            item.is_expire -> "立即续费"
            item.isfull -> "编辑兴趣"
            else -> "完善兴趣"
        }
    }

}