package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.core.view.isVisible
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.SquareLabelBean
import kotlinx.android.synthetic.main.item_choose_label.view.*
import kotlinx.android.synthetic.main.item_choose_label_title.view.*

class ChooseLabelAdapter : BaseMultiItemQuickAdapter<SquareLabelBean, BaseViewHolder>(mutableListOf()) {

    init {
        addItemType(SquareLabelBean.TITLE, R.layout.item_choose_label_title)
        addItemType(SquareLabelBean.CONTENT, R.layout.item_choose_label)
    }

    override fun convert(helper: BaseViewHolder, item: SquareLabelBean) {

        when (helper.itemViewType) {
            SquareLabelBean.TITLE -> {
                helper.itemView.labelTitle.text = item.title
            }
            SquareLabelBean.CONTENT -> {
                helper.itemView.divider.isVisible = helper.layoutPosition != mData.size - 1
                helper.itemView.labelName.text = item.title
                GlideUtil.loadRoundImgCenterCrop(mContext, item.icon, helper.itemView.labelIcon, SizeUtils.dp2px(6F))

                if (item.checked) {
                    helper.itemView.setBackgroundColor(Color.parseColor("#FFFFEFE7"))
                } else {
                    helper.itemView.setBackgroundColor(mContext.resources.getColor(R.color.colorWhite))
                }
                if (item.cnt > 0) {
                    helper.itemView.labelUsedCount.isVisible = true
                    helper.itemView.labelUsedCount.text = mContext.getString(R.string.has_published) + item.cnt + mContext.getString(
                                            R.string.piece_square)
                } else {
                    helper.itemView.labelUsedCount.isVisible = false
                }

                if (!item.cover_url.isNullOrEmpty()) {
                    helper.itemView.labelUsedCover.isVisible = true
                    GlideUtil.loadRoundImgCenterCrop(
                        mContext,
                        item.cover_url,
                        helper.itemView.labelUsedCover,
                        SizeUtils.dp2px(6F)
                    )
                } else
                    helper.itemView.labelUsedCover.isVisible = false
            }
        }


    }
}