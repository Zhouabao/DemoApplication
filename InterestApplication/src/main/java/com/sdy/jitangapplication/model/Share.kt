package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/7/3117:29
 *    desc   :
 *    version: 1.0
 */

data class MyInvitedBeans(
    var list: MutableList<MyInvitedBean> = mutableListOf(),
    var now_level: Int = 0,
    var now_rate: Int = 0,
    var progress: Progress = Progress(),
    var title: String = ""
)

data class MyInvitedBean(
    var accid: String = "",
    var account: String = "",
    var avatar: String = "",
    var is_payed: Boolean = false,
    var nickname: String = ""
)

data class Progress(
    var all_cnt: Int = 0,
    var invite_cnt: Int = 0,
    var reward_money: Int = 0
)