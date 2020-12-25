package com.sdy.jitangapplication.ui.adapter

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.MyTapsBean
import kotlinx.android.synthetic.main.item_get_relationship_pic.view.*
import kotlinx.android.synthetic.main.item_man_taps_detail.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/714:45
 *    desc   :
 *    version: 1.0
 */
class ManTapDetailAdapter :
    BaseQuickAdapter<MyTapsBean, BaseViewHolder>(R.layout.item_man_taps_detail) {
    override fun convert(helper: BaseViewHolder, item: MyTapsBean) {
        helper.itemView.innerBeing.text = item.title
        if (item.checked) {
            helper.itemView.innerBeing.setBackgroundResource(R.drawable.shape_orange_7dp)
            helper.itemView.innerBeing.setTextColor(mContext.resources.getColor(R.color.colorWhite))
        } else {
            helper.itemView.innerBeing.setBackgroundResource(R.drawable.shape_gray_f1f1f1_7dp)
            helper.itemView.innerBeing.setTextColor(mContext.resources.getColor(R.color.colorBlack19))
        }

    }
}