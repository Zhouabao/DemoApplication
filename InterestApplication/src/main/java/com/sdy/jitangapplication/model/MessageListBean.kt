package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity

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

/**
 * 给我打招呼的人
 */

data class HiMessageBean(
    var count: Int = 0,
    var content: String = "",
    var accid: String = "",
    var avatar: String = "",
    var create_time: String = "",
    var nickname: String = "",
    var out_time: String = "",
    var timeout_time: Int = 0,
    var countdown: Int = 0,//	倒计时秒剩余时长（秒）
    var id: Int = 0,
    var read_time: String = "",
    var countdown_total: Int = 0,//	倒计时 总时长（秒）
    var timer: Int = 0,
    var type: Int = 0//	1，新消息 2，倒计时 3，普通样式 4 过期
) : MultiItemEntity {
    override fun getItemType(): Int {
        if (type != null)
            return type
        return -1
    }

}


/**
 * 被删除的超时的accid
 */
data class OuttimeBean(
    val list: MutableList<String> = mutableListOf()
)

/*************消息列表**************/


data class MessageListBean1(
    val greet: MutableList<HiMessageBean>? = mutableListOf(),
    val greet_cnt: Int = 0,//招呼总数
    val liked_cnt: Int? = 0,//感兴趣的数量
    val liked_unread_cnt: Int? = 0,//	未读感兴趣总数
    val liked_time: String? = "",//	感兴趣最后一条时间
    val square_nickname: String? = "",//广场消息的用户昵称
    val square_type: Int? = 0,//广场消息的内容1广场点赞 2评论我的 3为我评论点赞的
    val square_time: String? = "",//最新一条广场消息
    val square_cnt: Int? = 0//广场消息总数
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
    var hasread: Boolean? = false,    //是否有未读 true 有 （标红）false 没有（）
    val list: MutableList<LikeMeOneDayBean>? = mutableListOf()
)

data class LikeMeOneDayBean(
    val age: Int? = 0,
    val avatar: String? = "",
    val accid: String? = "",
    val birth: Int? = 0,
    val constellation: String? = "",
    val distance: String? = "",
    val gender: Int? = 0,
    var isfriend: Int = 0,
    val isvip: Int? = 0,
    val job: String? = "",
    val nickname: String? = "",
    val sign: String? = "",
    val tag_title: String? = "",
    val is_read: Boolean? = false//是有已读 true已经 false 未读（标红）
)


/**************广场消息列表*******************/

/**
 * 广场消息列表
 */
data class SquareLitBean(
    val newest: MutableList<SquareMsgBean>? = mutableListOf(),
    val current_page: Int? = 0,
    val last_page: Int? = 0,
    val per_page: Int? = 0,
    val total: Int? = 0
)

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
