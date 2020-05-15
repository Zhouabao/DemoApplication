package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.CheckBean
import kotlinx.android.synthetic.main.item_today_want.view.*
import org.jetbrains.anko.textColor

/**
 *    author : ZFM
 *    date   : 2020/5/810:46
 *    desc   :
 *    version: 1.0
 */
class TodayWantAdapter : BaseQuickAdapter<CheckBean, BaseViewHolder>(R.layout.item_today_want) {
    override fun convert(helper: BaseViewHolder, item: CheckBean) {
//
        val params = helper.itemView.layoutParams as RecyclerView.LayoutParams
        params.width = SizeUtils.dp2px(77F)
//        params.height = SizeUtils.dp2px(77F)
        params.rightMargin = if ((helper.layoutPosition + 1) % 3 != 0) {
            ((ScreenUtils.getScreenWidth() - SizeUtils.dp2px(25 * 2F + 77 * 3F)) / 2f).toInt()
        } else {
            0
        }
        helper.itemView.layoutParams = params

        if (item.checked) {
            helper.itemView.todayWantTitle.textColor =
                mContext.resources.getColor(R.color.colorOrange)
            helper.itemView.todayWantIv.borderColor =
                mContext.resources.getColor(R.color.colorOrange)
            helper.itemView.todayWantIv.borderWidth = SizeUtils.dp2px(2F)
        } else {
            helper.itemView.todayWantTitle.textColor =
                mContext.resources.getColor(R.color.colorBlack19)
            helper.itemView.todayWantIv.borderColor =
                mContext.resources.getColor(R.color.colorWhite)
            helper.itemView.todayWantIv.borderWidth = 0
        }

        GlideUtil.loadCircleImg(mContext, item.icon, helper.itemView.todayWantIv)
        helper.itemView.todayWantTitle.text = item.title

    }
}