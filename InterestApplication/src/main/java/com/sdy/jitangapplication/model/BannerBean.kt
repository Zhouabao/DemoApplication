package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.flyco.tablayout.listener.CustomTabEntity

/**
 *    author : ZFM
 *    date   : 2019/6/2810:49
 *    desc   :
 *    version: 1.0
 */

/**
 * 问卷渠道
 */
data class ChannelBean(
    var id: Int = 0,
    var title: String = "",
    var check: Boolean = false
) : MultiItemEntity {
    override fun getItemType(): Int {
        if (title == "其他") {
            return 0
        } else {
            return 1
        }
    }
}


/**
 * 调查bean
 */
data class InvestigateBean(
    var create_time: String = "",
    var id: Int = 0,
    var input_type: Int = 0,
    var item: MutableList<ChannelBean> = mutableListOf(),
    var question_content: String = "",
    var question_title: String = "",
    var showcard_cnt: Int = 0,
    var sort_num: Int = 0,
    var statistics_id: Int = 0
)


/**
 * 举报对象
 */
data class ReportBean(var reason: String, var checked: Boolean)

data class TabEntity(val title: String, val iconSelect: Int, val iconUnselect: Int) : CustomTabEntity {
    override fun getTabUnselectedIcon(): Int {
        return iconUnselect

    }

    override fun getTabSelectedIcon(): Int {
        return iconSelect
    }

    override fun getTabTitle(): String {
        return title
    }
}