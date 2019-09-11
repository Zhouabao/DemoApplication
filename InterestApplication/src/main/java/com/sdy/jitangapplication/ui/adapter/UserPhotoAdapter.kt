package com.sdy.jitangapplication.ui.adapter

import android.view.View
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyPhotoBean
import com.sdy.jitangapplication.widgets.drag.BaseMultiItemDragQuickAdapter
import kotlinx.android.synthetic.main.item_user_info_img.view.*

/**
 *    author : ZFM
 *    date   : 2019/8/110:23
 *    desc   : 用户照片adapter
 *    version: 1.0
 */
class UserPhotoAdapter(var domain: String? = null, datas: MutableList<MyPhotoBean>) :
    BaseMultiItemDragQuickAdapter<MyPhotoBean, BaseViewHolder>(datas) {

    init {
        addItemType(MyPhotoBean.COVER, R.layout.item_user_info_take)
        addItemType(MyPhotoBean.PHOTO, R.layout.item_user_info_img)
    }


    override fun convert(holder: BaseViewHolder, item: MyPhotoBean) {
        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = (ScreenUtils.getScreenWidth() - 4 * SizeUtils.dp2px(15F)) / 3
        layoutParams.height = (16 / 9F * layoutParams.width).toInt()
        holder.itemView.layoutParams = layoutParams
        when (holder.itemViewType) {
            MyPhotoBean.PHOTO -> {
                if (holder.layoutPosition == 0) {
                    holder.itemView.isAvator.visibility = View.VISIBLE
                } else {
                    holder.itemView.isAvator.visibility = View.INVISIBLE
                }
                holder.itemView.userImg.visibility = View.VISIBLE
                GlideUtil.loadRoundImgCenterCrop(
                    mContext,
                    domain.plus(item.url),
                    holder.itemView.userImg,
                    SizeUtils.dp2px(5F)
                )

//                GlideUtil.loadImg(mContext, domain.plus(item.url), holder.itemView.userImg)
            }
        }

    }

}