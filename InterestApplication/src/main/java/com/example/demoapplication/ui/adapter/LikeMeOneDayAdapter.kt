package com.example.demoapplication.ui.adapter

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.example.demoapplication.model.LikeMeOneDayBean
import com.example.demoapplication.utils.UserManager
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_like_me_one_day.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   :
 *    version: 1.0
 */
class LikeMeOneDayAdapter : BaseQuickAdapter<LikeMeOneDayBean, BaseViewHolder>(R.layout.item_like_me_one_day) {
    override fun convert(holder: BaseViewHolder, item: LikeMeOneDayBean) {
        val itemView = holder.itemView

        val params = itemView.likeMeAvator.layoutParams as ConstraintLayout.LayoutParams
        params.width = SizeUtils.dp2px(180F)
        params.height = (16 / 9F * params.width).toInt()
        itemView.likeMeAvator.layoutParams = params
        GlideUtil.loadImg(mContext, item.avatar, itemView.likeMeAvator)
        itemView.likeMeTag.text = item.tag_title
        itemView.likeMeNickname.text = item.nickname
        itemView.likeMeInfo.text = "${item.age} / ${if (item.gender == 1) {
            "男"
        } else {
            "女"
        }} / ${item.constellation} / ${item.distance} / ${item.job}"
        if (UserManager.isUserVip()) {
            itemView.likeMeTagCover.visibility = View.GONE
            itemView.likeMeInfoCover.visibility = View.GONE
            itemView.likeMeNicknameCover.visibility = View.GONE
            itemView.likeMeType.visibility = View.VISIBLE
        } else {
            itemView.likeMeType.visibility = View.INVISIBLE
            itemView.likeMeTagCover.visibility = View.VISIBLE
            itemView.likeMeInfoCover.visibility = View.VISIBLE
            itemView.likeMeNicknameCover.visibility = View.VISIBLE
            Glide.with(mContext)
                .load( item.avatar?:"")
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                .into(itemView.likeMeAvator)
        }


    }
}