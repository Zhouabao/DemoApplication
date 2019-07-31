package com.example.demoapplication.ui.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.baselibrary.glide.GlideUtil
import com.example.demoapplication.R
import com.kotlin.base.common.BaseApplication.Companion.context
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.item_square_play_detail_audio.view.*
import kotlinx.android.synthetic.main.item_user_center_visit_cover.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 个人中心的动态封面适配器
 *    version: 1.0
 */
class VisitUserAvatorAdater :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_user_center_visit_cover) {

    override fun convert(holder: BaseViewHolder, item: String) {


        GlideUtil.loadImg(mContext, item, holder.itemView.visitCoverImg)
    }

}
