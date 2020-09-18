package com.sdy.jitangapplication.ui.adapter

import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MediaParamBean
import kotlinx.android.synthetic.main.item_sweet_verify_pic.view.*

/**
 *    author : ZFM
 *    date   : 2019/11/114:40
 *    desc   : 举报上传的图片
 *    version: 1.0
 */
class SweetVerifyPicAdapter :
    BaseQuickAdapter<MediaParamBean, BaseViewHolder>(R.layout.item_sweet_verify_pic) {

    override fun convert(helper: BaseViewHolder, item: MediaParamBean) {

        val params = helper.itemView.layoutParams
        params.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2F + 10 * 2)) / 3
        params.height = params.width


        if (item.url=="") {
            helper.itemView.sweetPicDelete.isVisible = false
            helper.itemView.sweetPic.setImageResource(R.drawable.icon_upload_sweet)
        } else {
            helper.addOnClickListener(R.id.sweetPicDelete)
            helper.itemView.sweetPicDelete.isVisible = true
            GlideUtil.loadRoundImgCenterCrop(
                mContext,
                item.url,
                helper.itemView.sweetPic,
                SizeUtils.dp2px(10F)
            )
        }
    }
}