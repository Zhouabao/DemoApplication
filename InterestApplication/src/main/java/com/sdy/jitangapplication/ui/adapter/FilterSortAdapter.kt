package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.CheckBean
import kotlinx.android.synthetic.main.item_filter_sort.view.*

/**
 *    author : ZFM
 *    date   : 2020/4/2715:53
 *    desc   :
 *    version: 1.0
 */
class FilterSortAdapter : BaseQuickAdapter<CheckBean, BaseViewHolder>(R.layout.item_filter_sort) {
    override fun convert(helper: BaseViewHolder, item: CheckBean) {
        val layoutParams = helper.itemView.layoutParams as RecyclerView.LayoutParams
        layoutParams.width = (ScreenUtils.getScreenWidth() - SizeUtils.dp2px(15 * 2 + 10 * 2F)) / 3
        helper.itemView.layoutParams = layoutParams
        if (item.checked) {
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_filter_check_15dp)
            helper.itemView.sortIcon.setImageResource(item.checkedIcon)
            helper.itemView.sortTitle.setTextColor(Color.parseColor("#FF6318"))
        } else {
            helper.itemView.setBackgroundResource(R.drawable.shape_rectangle_filter_uncheck_15dp)
            helper.itemView.sortIcon.setImageResource(item.normalIcon)
            helper.itemView.sortTitle.setTextColor(Color.parseColor("#4F4B55"))
        }

        helper.itemView.sortTitle.text = item.title
    }
}