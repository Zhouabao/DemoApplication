package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 *    author : ZFM
 *    date   : 2019/6/2810:49
 *    desc   :
 *    version: 1.0
 */

/**
 * 问卷渠道
 */
data class UserRelationshipBean(
    var title: String = "",
    val relationType: Int = 1
) : MultiItemEntity {
    override fun getItemType(): Int {
        return relationType
    }
}


/**
 * 举报对象
 */
data class ReportBean(var reason: String, var checked: Boolean)
