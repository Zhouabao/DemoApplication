package com.sdy.jitangapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/8/1715:06
 *    desc   : 聊天界面返回用户信息
 *    version: 1.0
 */
data class NimBean(
    var approve_time: Long = 0L,
    var avatar: String = "",
    var isapprove: Int = 0,
    var isblocked: Boolean = false,
    var isfriend: Boolean = false,
    var isgreet: Boolean = false,//招呼是否有效
    var isinitiated: Boolean = false,
    var islimit: Boolean = false,
    var issend: Boolean = false,
    var issended: Boolean = false,//是否发送过消息  true发送过 false  没有发送过消息
    var matching_content: String = "",
    var matching_icon: String = "",
    var my_gender: Int = 0,
    var my_isfaced: Boolean = false,
    var my_want_cnt: Boolean = false,
    var residue_msg_cnt: Int = 0,//剩余可发送的招呼消息次数
    var square: List<Square> = listOf(),
    var square_cnt: Int = 0,
    var stared: Boolean = false,
    var target_gender: Int = 0,
    var target_isfaced: Boolean = false,
    var target_want_cnt: Boolean = false,
    var tips_chat_cnt: Int = 0,
    var tips_gift_cnt: Int = 0,
    var both_gift_list: MutableList<ChatGiftStateBean> = mutableListOf(),
    var is_send_msg: Boolean = false
//    var is_send_msg: Boolean = false  //本用户是否发送过消息

) : Serializable

data class ChatGiftStateBean(
    var cate_type: Int = 0,//cate_type  1 礼物  2助力
    var id: Int = 0,
    var state: Int = 0//state  2领取  3过期
)

data class ResidueCountBean(
    var residue_msg_cnt: Int = 0,//剩余可发送的招呼消息次数
    var get_help_amount: Int = 0
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
    val extra: Any?
)

/**
 * 所有消息的集合
 */
data class AllMsgCount(
    val greetcount: Int = 0, //招呼未读
    val likecount: Int = 0,//点赞未读
    val square_count: Int = 0//
)