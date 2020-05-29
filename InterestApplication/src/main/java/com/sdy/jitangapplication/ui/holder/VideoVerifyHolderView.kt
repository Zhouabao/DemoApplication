package com.sdy.jitangapplication.ui.holder

import android.view.View
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VideoVerifyBannerBean
import com.zhpan.bannerview.holder.ViewHolder
import kotlinx.android.synthetic.main.item_video_verify_banner.view.*

class VideoVerifyHolderView : ViewHolder<VideoVerifyBannerBean> {
    override fun getLayoutId(): Int {
        return R.layout.item_video_verify_banner
    }

    override fun onBind(itemView: View, data: VideoVerifyBannerBean, position: Int, size: Int) {
        itemView.iv1.setImageResource(data.icon)
        itemView.tv1.text = data.content
        itemView.tv2.text = data.title
    }
}