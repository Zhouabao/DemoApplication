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
    var normal_chat_times: Int = 0,
    var residue_msg_cnt: Int = 0,//剩余可发送的招呼消息次数
    var issend: Boolean = false,
    var issended: Boolean = false,//是否发送过消息  true发送过 false  没有发送过消息
    var matching_content: String = "",
    var matching_icon: String = "",
    var my_gender: Int = 0,
    var my_isfaced: Boolean = false,
    var my_want_cnt: Boolean = false,
    var square: List<Square> = listOf(),
    var square_cnt: Int = 0,
    var stared: Boolean = false,
    var target_gender: Int = 0,
    var target_isfaced: Boolean = false,
    var target_want_cnt: Boolean = false,
    var both_gift_list: MutableList<ChatGiftStateBean> = mutableListOf(),
    var is_send_msg: Boolean = false,
    var force_isvip: Boolean = false
//    var is_send_msg: Boolean = false  //本用户是否发送过消息

) : Serializable

data class ChatGiftStateBean(
    var cate_type: Int = 0,//cate_type  1 礼物  2助力
    var id: Int = 0,
    var state: Int = 0//state  2领取  3过期
)

data class ResidueCountBean(
    var residue_msg_cnt: Int = 0,//剩余可发送的招呼消息次数
    var get_help_amount: Int = 0,
    var ret_tips_arr: MutableList<SendTipBean> = mutableListOf()
)

data class CustomerMsgBean(
    val msg: String = "",
    val accid: String? = "",
    val type: Int = 0,
    val extra: Any?,
    val avatar: String,
    val content: String,
    val title: String
)

/**
 * 所有消息的集合
 */
data class AllMsgCount(
    val likecount: Int = 0,//点赞未读
    val square_count: Int = 0//
)

/**
 * 附近的人的个数
 */
data class NearCountBean(
    val nearly_tips_cnt: Int,
    val nearly_tips_str: String
)