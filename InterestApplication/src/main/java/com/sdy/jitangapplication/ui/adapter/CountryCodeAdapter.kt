package com.sdy.jitangapplication.ui.adapter

import android.util.Log
import android.view.View
import com.blankj.utilcode.util.LanguageUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.CommonFunction
import com.sdy.jitangapplication.model.CountryCodeBean
import kotlinx.android.synthetic.main.item_country_code.view.*
import java.util.*

/**
 *    author : ZFM
 *    date   : 2019/8/614:30
 *    desc   :
 *    version: 1.0
 */
class CountryCodeAdapter :
    BaseQuickAdapter<CountryCodeBean, BaseViewHolder>(R.layout.item_country_code) {
    override fun convert(helper: BaseViewHolder, item: CountryCodeBean) {
        val position = helper.layoutPosition
        //因为添加了头部 所以位置要移动
        if ((position == 0 || data[position - 1].index != item.index)) {
            helper.itemView.tv_index.visibility = View.VISIBLE
            helper.itemView.friendDivider0.visibility = View.VISIBLE
            helper.itemView.tv_index.text = item.index
            helper.itemView.friendDivider.visibility = View.GONE
        } else {
            helper.itemView.tv_index.visibility = View.GONE
            helper.itemView.friendDivider0.visibility = View.GONE
            helper.itemView.friendDivider.visibility = View.VISIBLE
        }


        helper.itemView.friendName.text =
            "${if (CommonFunction.isEnglishLanguage()) item.en else item.sc}\t+${item.code}"
    }

}