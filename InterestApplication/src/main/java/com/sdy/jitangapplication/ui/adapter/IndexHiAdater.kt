package com.sdy.jitangapplication.ui.adapter

import android.view.Gravity
import android.widget.FrameLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import kotlinx.android.synthetic.main.item_greet_hi_index_left.view.*

/**
 *    author : ZFM
 *    date   : 2019/6/2615:22
 *    desc   : 首页收到的消息头像
 *    version: 1.0
 */
class IndexHiAdater : BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_greet_hi_index_left) {

    override fun convert(holder: BaseViewHolder, item: String) {
        (holder.itemView.viewGroup.layoutParams as FrameLayout.LayoutParams).gravity =
            if (holder.layoutPosition % 2 == 0) {
                Gravity.LEFT
            } else {
                Gravity.RIGHT
            }

        GlideUtil.loadImg(mContext,item,holder.itemView.greetIcon)
    }

}
