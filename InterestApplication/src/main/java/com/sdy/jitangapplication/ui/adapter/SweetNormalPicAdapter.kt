package com.sdy.jitangapplication.ui.adapter

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.utils.UserManager

/**
 *    author : ZFM
 *    date   : 2019/11/114:40
 *    desc   : 举报上传的图片
 *    version: 1.0
 */
class SweetNormalPicAdapter :
    BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_sweet_normal_pic) {

    override fun convert(helper: BaseViewHolder, item: String) {

        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        if (UserManager.getGender() ==2) {
            params.width = SizeUtils.dp2px(300F)
            params.height = params.width
        } else {
            params.width = SizeUtils.dp2px(300F)
            params.height = params.width / 3 * 4

        }
        params.leftMargin = if (helper.layoutPosition == 0) {
            (ScreenUtils.getScreenWidth() - params.width) / 2
        } else {
            0
        }
        params.rightMargin = if (helper.layoutPosition == data.size - 1) {
            (ScreenUtils.getScreenWidth() - params.width) / 2
        } else {
            SizeUtils.dp2px(15F)
        }
        GlideUtil.loadRoundImgCenterCrop(
            mContext,
            item,
            helper.itemView as ImageView,
            SizeUtils.dp2px(10F)
        )


    }
}