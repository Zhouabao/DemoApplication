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
    var thumbs_up_count: Int = 0,//点赞未读数
    var comment_count: Int = 0,//评论未读数
    val liked_unread_cnt: Int? = 0,//	未读感兴趣总数
    val liked_cnt: Int? = 0,//	感兴趣总数
    val like_free_show: Boolean,//	是否展示
    var my_like_times: Int = 0,
    var my_percent_complete: Int = 0,
    var normal_percent_complete: Int = 0,
    var total_like_times: Int = 0,
    var approve_time: Long,
    var isapprove: Int,//0 不验证  1去认证 2去开通会员  3去认证+去会员  4去会员+去认证
    var session_list_arr: MutableList<MessageGiftBean> = mutableListOf()
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
