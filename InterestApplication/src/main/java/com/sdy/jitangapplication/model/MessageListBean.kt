package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2019/8/511:23
 *    desc   :
 *    version: 1.0
 */
data class MessageListBean(
    var title: String = "",
    var msg: String = "",
    var count: Int = 0,
    var time: String = "",
    var icon: Int = 0,
    var id: String = ""
)


/*************消息列表**************/
data class MessageListBean1(
    var square_count: Int = 0,//评论未读数
    var square_type: Int = 0,// 1广场点赞 2评论我的 3为我评论点赞的 4@我的列表
    var square_time: String = "",
    var square_nickname: String = "",
    var chatup_list_lasttime: String = "",
    var session_list_arr: MutableList<MessageGiftBean> = mutableListOf(),
    var chatup_rid_list: MutableList<String> = mutableListOf(), //要剔除的id
    var chatup_list: MutableList<AccostBean> = mutableListOf()
)


data class MessageGiftBean(
    var mid: String = "",
    var id: Int = 0,
    var state: Int = 0////state  2领取  3过期
)


/**************广场消息列表*******************/

/**
 * 广场消息
 */
data class SquareMsgBean(
    var pos: Int = -1,
    val accid: String? = "",
    val avatar: String? = "",
    val content: String? = "",//	type为2 评论的内容
    val create_time: String? = "",
    val id: Int? = 0,
    val msg_id: Int? = 0,//删除回传
    val nickname: String? = "",
    val cover_url: String? = "",
    val type: Int? = 0,//类型 1，广场点赞 2，评论我的 3。我的评论点赞的 4 @我的
    val category: Int? = 0,//	0文本 1图片 2视频 3 语音
    val is_read: Boolean? = false//是有已读 true已经 false 未读（标红）
)

/****************联系人*******************/


data class ContactDataBean(
    var list: MutableList<ContactBean>? = mutableListOf(),
    var asterisk: MutableList<ContactBean>? = mutableListOf()
)

data class ContactBean(
    var nickname: String = "",
    var stared: Boolean = false,
    var accid: String = "",
    var avatar: String = "",
    var member_level: Int = 0,
    var index: String = ""
//    var index: String? = Cn2Spell.getPinYinFirstLetter(nickname).toUpperCase()
)


data class AccostListBean(var list: MutableList<AccostBean> = mutableListOf())
data class AccostBean(
    var accid: String = "",
    var avatar: String = "",
    var icon: String = "",
    var gender: Int = 0,
    var unreadCnt: Int = 0,
    var time: Long = 0L,
    var nickname: String = "",
    var content: String = ""
)

/****************黑名单*******************/
data class BlackBean(
    val accid: String = "",
    val age: Int = 0,
    val avatar: String = "",
    val constellation: String = "",
    val gender: Int = 0,
    val isvip: Int = 0,
    val nickname: String = ""
)
