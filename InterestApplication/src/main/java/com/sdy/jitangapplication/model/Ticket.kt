package com.sdy.jitangapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2020/6/1910:09
 *    desc   :
 *    version: 1.0
 */

data class TicketBean(
    var my_mv_url: Int = 0, //0未上传 1通过 2审核中
    var isplatinum: Boolean = false,
    var gender: Int = 0,
    var mv_free_cnt: Int = 0,
    var my_ticket_sum: Int = 0,
    var platinum_free_cnt: Int = 0,
    var isfaced: Int = 0,
    var ticket: Ticket = Ticket(),
    var list: MutableList<IndexTopBean> = mutableListOf()
)

data class Ticket(
    var amount: Int = 0,
    var descr: String = "",
    var title: String = ""
)



/**
 * 开屏页推荐
 */
data class IndexTopBean(
    var accid: String = "",
    var age: Int = 0,
    var amount: Int = 0,
    var avatar: String = "",
    var distance: String = "",
    var gender: Int = 0,
    var nickname: String = "",
    var checked: Boolean = true
) : Serializable

data class IndexListBean(var list: MutableList<IndexTopBean> = mutableListOf()) : Serializable