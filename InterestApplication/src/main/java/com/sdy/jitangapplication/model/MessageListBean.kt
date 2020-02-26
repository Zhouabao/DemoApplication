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
    var content: String = "",
    var accid: String = "",
    var avatar: String = "",
    var nickname: String = "",
    var distance: String = "",
    var age: Int = 0,
    var gender: Int = 1,
    var out_time: String = "",
    var type: Int = 0//	1，新消息 2，倒计时 3，普通样式 4 过期
) : MultiItemEntity {
    override fun getItemType(): Int {
        if (type != null)
            return type
        return -1
    }

}

/**
 * 给我打招呼的人
 */
data class GreetedListBean(
    var greet_id: String = "",
    var accid: String = "",
    var age: Int = 0,
    var avatar: String = "",
    var constellation: String = "",
    var distance: String = "",
    var face_auditing_state: Int = 0,
    var gender: Int = 0,
    var hb_city: String = "",
    var intention_icon: String = "",
    var intention_title: String = "",
    var isfaced: Int = 0,
    var isvip: Int = 0,
    var matching_content: String = "",
    var matching_icon: String = "",
    var member_level: Int = 0,
    var nickname: String = "",
    var photos: MutableList<String> = mutableListOf(),
    var sign: String = "",
    var send_msg: String = "",//该用户发出来的招呼消息
    var send_msg_normal: String = ""//该用户发出来的招呼消息
)


/**
 * 被删除的超时的accid
 */
data class OuttimeBean(
    val list: MutableList<String> = mutableListOf()
)

/*************消息列表**************/
data class MessageListBean1(
    var thumbs_up_count: Int = 0,//点赞未读数
    var comment_count: Int = 0,//评论未读数
    val greet_count: Int = 0,//招呼总数
    val likelist: MutableList<Likelist>? = mutableListOf(),
    val liked_unread_cnt: Int? = 0,//	未读感兴趣总数
    val liked_cnt: Int? = 0,//	感兴趣总数
    val like_free_show: Boolean,//	是否展示
    val greet_toast: Boolean = false, //是否弹起开启招呼认证弹窗
    var my_like_times: Int = 0,
    var my_percent_complete: Int = 0,
    var effective_greet: MutableList<Likelist> = mutableListOf(),
    var no_effective_greet: MutableList<Likelist> = mutableListOf(),
    var normal_percent_complete: Int = 0,
    var total_like_times: Int = 0,
    var approve_time: Long,
    var isapprove: Int //0 不验证  1去认证 2去开通会员  3去认证+去会员  4去会员+去认证
)

data class Likelist(
    var accid: String = "",
    var avatar: String = "",
    var is_read: Boolean = false,
    var isfriend: Boolean = false,
    var nickname: String = ""
)

/***********************对我感兴趣的**************************/

/*-------------------对我感兴趣的-----------------------------*/
data class LikeMeListBean(
    val isvip: Int? = 0,
    val free_show: Boolean = false,//true（显示）  false(模糊)
    val list: MutableList<LikeMeBean>? = mutableListOf(),
    val my_percent_complete: Int,//（我的资料完整度）
    val normal_percent_complete: Int,//（标准完整度）
    val my_like_times: Int,//（我的次数）
    val total_like_times: Int//  total_like_times（最高次数）
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

data class NewLikeMeBean(
    var list: MutableList<PositiveLikeBean> = mutableListOf(),
    var count: Int = 0,
    var my_like_times: Int = 0,
    var normal_percent_complete: Int = 0,
    var my_percent_complete: Int = 0,
    var total_like_times: Int = 0
)


data class PositiveLikeBean(
    var accid: String = "",
    var age: Int = 0,
    var avatar: String = "",
    var constellation: String = "",
    var distance: String = "",
    var gender: Int = 0,
    var isfaced: Boolean = false,
    var isvip: Boolean = false,
    var nickname: String = "",
    var tag_id: Int = 0,
    var photo: MutableList<String> = mutableListOf(),
    var sametag: MutableList<String> = mutableListOf(),
    var title: String = ""
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
    var isfaced: Int = 0,
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
