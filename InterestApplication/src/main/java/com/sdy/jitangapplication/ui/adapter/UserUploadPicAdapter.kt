package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyPhotoBean
import kotlinx.android.synthetic.main.item_user_upload_pic.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/710:11
 *    desc   :
 *    version: 1.0
 */
class UserUploadPicAdapter :
    BaseQuickAdapter<MyPhotoBean, BaseViewHolder>(R.layout.item_user_upload_pic) {
    override fun convert(helper: BaseViewHolder, item: MyPhotoBean) {
        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(25F * 2 + 9 * 3)) / 4
        params.height = (5 / 4F * params.width).toInt()
        helper.itemView.layoutParams = params


        if (item.id == -1) {
            helper.itemView.addedPic.setImageResource(R.drawable.icon_add_phot)
        } else {
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item.url,
                helper.itemView.addedPic,
                SizeUtils.dp2px(5F)
            )
        }

    }
}