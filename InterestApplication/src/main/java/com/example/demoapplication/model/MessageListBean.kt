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
    val time: String
)

/**
 * 给我打招呼的人
 */
data class HiMessageBean(
    val accid: String? = "",
    val avatar: String? = "",
    val count: Int? = 0,
    val create_time: String? = "",
    val nickname: String? = "",
    val out_time: String? = "",
    val timeout_time: Int? = 0,
    val content: String? = ""
)


/***********************对我感兴趣的**************************/

/*-------------------对我感兴趣的-----------------------------*/
data class SquareMsgListBean(
    val isvip: Int? = 0,
    val list: MutableList<LikeMeBean>? = mutableListOf()
)

data class LikeMeBean(
    val count: Int? = 0,
    val date: String? = "",
    val list: MutableList<LikeMeOneDayBean>? = mutableListOf()
)


/**
 * 喜欢我的对象
 */
data class LikeMeListBean(
    val count: Int? = 0,
    val date: String? = "",
    val list: List<LikeMeBean>? = mutableListOf()
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
