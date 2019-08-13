package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/8/511:23
 *    desc   :
 *    version: 1.0
 */
data class MessageListBean(
    var title: String,
    val msg: String,
    val count: Int,
    val time: String,
    val icon: Int? = 0
)

/**
 * 给我打招呼的人
 */
data class HiMessageBean(
    val count: Int? = 0,
    val content: String? = "",
    val accid: String? = "",
    val avatar: String? = "",
    val create_time: String? = "",
    val nickname: String? = "",
    val out_time: String? = "",
    val timeout_time: Int? = 0
)

/*************消息列表**************/



data class MessageListBean1(
    val greet: MutableList<HiMessageBean>? = mutableListOf(),
    val greet_cnt: Int? = 0,
    val liked_cnt: Int? = 0,
    val liked_unread_cnt: Int? = 0,
    val liked_time: String? = "",
    val square_nickname: String? = "",
    val square_type: Int? = 0,
    val square_time: String? = "",
    val square_cnt: Int? = 0
)

data class SquareMsg(
    val accid: String? = "",
    val avatar: String? = "",
    val content: String? = "",
    val create_time: String? = "",
    val descr: String? = "",//显示的内容（type==2有人评论是限制的评论内容）
    val id: Int? = 0,
    val nickname: String? = "",
    val type: Int? = 0//1广场点赞 2评论我的 3为我评论点赞的 4@我的列表
)

/***********************对我感兴趣的**************************/

/*-------------------对我感兴趣的-----------------------------*/
data class LikeMeListBean(
    val isvip: Int? = 0,
    val list: MutableList<LikeMeBean>? = mutableListOf()
)

/**
 * 喜欢我的对象
 */
data class LikeMeBean(
    val count: Int? = 0,
    val date: String? = "",
    val list: MutableList<LikeMeOneDayBean>? = mutableListOf()
)

data class LikeMeOneDayBean(
    val age: Int? = 0,
    val avatar: String? = "",
    val birth: Int? = 0,
    val constellation: String? = "",
    val distance: String? = "",
    val gender: Int? = 0,
    val isfriend: Int? = 0,
    val isvip: Int? = 0,
    val job: String? = "",
    val nickname: String? = "",
    val sign: String? = "",
    val tag_title: String? = ""
)


/**************广场消息列表*******************/

/**
 * 广场消息列表
 */
data class SquareLitBean(
    val newest: MutableList<SquareMsgBean>? = mutableListOf(),
    val history: MutableList<SquareMsgBean>? = mutableListOf(),
    val current_page: Int? = 0,
    val last_page: Int? = 0,
    val per_page: Int? = 0,
    val total: Int? = 0
)

/**
 * 广场消息
 */
data class SquareMsgBean(
    val accid: String? = "",
    val avatar: String? = "",
    val content: String? = "",//	type为2 评论的内容
    val create_time: String? = "",
    val id: Int? = 0,
    val nickname: String? = "",
    val cover_url: String? = "",
    val type: Int? = 0,//类型 1，广场点赞 2，评论我的 3。我的评论点赞的 4 @我的
    val category: Int? = 0 //类型 1，广场点赞 2，评论我的 3。我的评论点赞的 4 @我的
)

/****************联系人*******************/


data class ContactDataBean(
    var list: MutableList<ContactBean>? = mutableListOf(),
    var asterisk: MutableList<ContactBean>? = mutableListOf()
)

data class ContactBean(
    var nickname: String? = "",
    var accid: String? = "",
    var avatar: String? = "",
    var member_level: Int? = 0,
    var index: String?=""
//    var index: String? = Cn2Spell.getPinYinFirstLetter(nickname).toUpperCase()
)