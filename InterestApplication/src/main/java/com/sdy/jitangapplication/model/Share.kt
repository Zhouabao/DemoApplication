package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/7/3117:29
 *    desc   :
 *    version: 1.0
 */

/**我的邀请**/
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
    var nickname: String = "",
    var amount: Int = 0,
    var descr: String = ""
)

data class Progress(
    var all_cnt: Int = 0,
    var invite_cnt: Int = 0,
    var reward_money: Int = 0
)

/**我的奖励**/
data class MyRewardBeans(
    var list: MutableList<MyInvitedBean> = mutableListOf(),
    var red_balance_money: Int = 0,
    var red_freeze_money: Int = 0,
    var red_withdraw_money: Int = 0
)


/*我的分享要请------邀请有礼*/
data class InvitePoliteBean(
    var invite_amount: Int = 0,
    var invite_cnt: Int = 0,
    var level_list: MutableList<Level> = mutableListOf(),
    var now_level: Int = 0,
    var now_rate: Int = 0,
    var progress: Progress = Progress(),
    var reward_list: MutableList<String> = mutableListOf(),
    var title: String = ""
)

data class Level(
    var commission_rate: Int = 0,
    var isget: Boolean = false,
    var reward_money: Double = 0.0,
    var set_cnt: Int = 0,
    var title: String = ""
)



data class MyBillBeans(
    val list: MutableList<BillBean> = mutableListOf()
)