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
 * 举报对象
 */
data class ReportBean(var reason: String, var checked: Boolean)
