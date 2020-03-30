package com.sdy.jitangapplication.ui.adapter

import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_comment_pic.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/114:40
 *    desc   : 举报上传的图片
 *    version: 1.0
 */
class CommentPicAdapter : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_comment_pic) {

    override fun convert(helper: BaseViewHolder, item: String) {
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item,
            helper.itemView.commentPic,
            SizeUtils.dp2px(8F)
        )
    }
}