package com.example.demoapplication.ui.adapter

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
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
import kotlinx.android.synthetic.main.item_like_me_one_day_all.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/516:02
 *    desc   :
 *    version: 1.0
 */
class LikeMeOneDayGirdAdapter : BaseQuickAdapter<LikeMeOneDayBean, BaseViewHolder>(R.layout.item_like_me_one_day_all) {
    private val SPAN_COUNT = 3
    override fun convert(holder: BaseViewHolder, item: LikeMeOneDayBean) {
        val itemView = holder.itemView
        val params = itemView.layoutParams as RecyclerView.LayoutParams
        params.width = ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15F) * 2 - (SPAN_COUNT - 1) * SizeUtils.dp2px(10F)) * 1.0F / SPAN_COUNT).toInt()
        params.height = (16 / 9F * params.width).toInt()
        itemView.layoutParams = params
        GlideUtil.loadImg(mContext, item.avatar, itemView.likeMeOneDayAvator)
        if (UserManager.isUserVip()) {
            itemView.likeMeOneDayType.visibility = View.VISIBLE
        } else {
            itemView.likeMeOneDayType.visibility = View.INVISIBLE
            Glide.with(mContext)
                .load(item.avatar ?: "")
                .apply(RequestOptions.bitmapTransform(BlurTransformation(25)))
                .into(holder.itemView.likeMeOneDayAvator)
        }


    }
}