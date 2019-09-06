package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2019/8/1715:06
 *    desc   : 聊天界面返回用户信息
 *    version: 1.0
 */

data class NimBean(
    val avatar: String? = "",
    val isfriend: Boolean = false,//	是否好友 true 是  false 不是
    val stared: Boolean = false,//	是否是星标好友 true 是  false 不是
    val isinitiated: Boolean = false,//是否自己发起的 true自己发起的 false 他人发起
    val taglist: ArrayList<Tag>? = arrayListOf(),
    var type: Int = 0,//类型1，新消息 2，倒计时 3，普通样式 4 过期
    val timeout_time: String = "",//过期时间
    val countdown_total: Int = 0,//总倒计时
    val countdown: Int = 0,//剩余时间
    val residue_msg_cnt: Int = 0//	该条招呼的剩余发起消息次数


)


/**
 * 判断当前能否发消息
 */
data class CheckGreetSendBean(
    val isfriend: Boolean = false, //	是否好友 时无限
    val islimit: Boolean = true, //	是否限制发送次数  true限制 false不限制
    val residue_msg_cnt: Int = 0//非好友 当前最新招呼剩余可发消息次数
)

data class CustomerMsgBean(
    val msg: String = "",
    val accid: String? = "",
    val type: Int = 0,
    val extra: Any?)

/**
 * 所有消息的集合
 */
data class AllMsgCount(
    val greetcount: Int = 0,
    val likecount: Int = 0,
    val square_count: Int = 0
)