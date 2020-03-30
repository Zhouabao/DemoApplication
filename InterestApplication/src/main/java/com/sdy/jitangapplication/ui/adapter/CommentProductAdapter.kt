package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.ProductCommentBean
import kotlinx.android.synthetic.main.item_comment.view.*

/**
 *    author : ZFM
 *    date   : 2020/3/2517:43
 *    desc   : 商品留言
 *    version: 1.0
 */
class CommentProductAdapter :
    BaseQuickAdapter<ProductCommentBean, BaseViewHolder>(R.layout.item_comment) {
    override fun convert(helper: BaseViewHolder, item: ProductCommentBean) {
        GlideUtil.loadCircleImg(mContext, item.avatar, helper.itemView.commentAvator)
        helper.itemView.commentName.text = item.nickname
        helper.itemView.commentStar.numStars = item.stars
        helper.itemView.commentContent.text = item.comments
        helper.itemView.commentTime.text = item.create_time
        helper.itemView.commentPicsRv.layoutManager = GridLayoutManager(mContext, 3)
        val adapter  = CommentPicAdapter()
        adapter.addData(item.pic)
        helper.itemView.commentPicsRv.adapter = adapter

    }
}