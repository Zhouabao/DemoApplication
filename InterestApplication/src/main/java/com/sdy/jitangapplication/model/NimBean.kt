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
    var isfriend: Boolean = false,
    var islimit: Boolean = false,
    var normal_chat_times: Int = 0,
    var residue_msg_cnt: Int = 0,//剩余可发送的招呼消息次数
    var matching_content: String = "",
    var matching_icon: String = "",
    var approve_chat_times: Int = 0,//认证后可聊天的人次数
    var mv_state: Int = 0,//视频介绍0 没有 1 通过  2审核中
    var my_gender: Int = 0,
    var my_isfaced: Boolean = false,
    var square: List<Square> = listOf(),
    var square_cnt: Int = 0,
    var stared: Boolean = false,
    var target_gender: Int = 0,
    var target_isfaced: Boolean = false,
    var both_gift_list: MutableList<ChatGiftStateBean> = mutableListOf(),
    var is_send_msg: Boolean = false,
    var chatup_amount: Int = 0,//搭讪支付的糖果
    var lockbtn: Boolean = false,//	true 弹出解锁聊天 false不弹出
    var force_isvip: Boolean = false,
    var chat_expend_amount: Int = 0,//1要显示 2不显示
    var chat_expend_time: Long = 0L,
    var plat_cnt: Int = 0,//剩余的搭讪的次数
    var isplatinum: Boolean = false,
    var private_chat_state: Boolean = false,
    var isdirect: Boolean = false,
    var unlock_contact_way: Int = 0, //是否有联系方式
    var unlock_popup_str: String = "", //	我是否被别人解锁弹框 大于0 弹框显示糖果数目
    var is_unlock_contact: Boolean = false,//是否解锁过联系方式
    var target_ishoney: Boolean = false//	true 是甜心圈 fals 不是甜心圈
) : Serializable

data class ChatGiftStateBean(
    var cate_type: Int = 0,//cate_type  1 礼物  2助力
    var id: Int = 0,
    var state: Int = 0//state  2领取  3过期
)

data class ResidueCountBean(
    var residue_msg_cnt: Int = 0,//剩余可发送的招呼消息次数
    var get_help_amount: Int = 0,

    var ret_tips_arr: MutableList<SendTipBean> = mutableListOf(),
    var rid_data: GiftBean? = null
)

data class CustomerMsgBean @JvmOverloads constructor(
    val type: Int = 0,
    val title: String = "",
    val content: String = "",
    val avatar: Any = "",
    val msg: String = "",
    val accid: String = "",
    val extra: Any? = null
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

/**
 * 注册信息表
 */
data class RegisterFileBean(
    var people_amount: Int = 0,
    var supplement: Int = 0,//补充资料 1 前置 2后置 3 关闭
    var threshold: Boolean = false,//门槛开关 开启true 关闭false
    var living_btn: Boolean = false,//活体认证的性别判断
    var tourists: Boolean = false,//	游客模式 开启true 关闭false
    var experience_state: Boolean = false,//	体验券状态 true开 flase 关闭
    var region: Int = 0  //2为海外模式

)