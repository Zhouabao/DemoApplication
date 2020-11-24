package com.sdy.jitangapplication.model

import java.io.Serializable

data class LoginBean(
    val accid: String?,
    var countdown_time: Int = 0,
    val phone_check: Boolean?,
    val qntk: String?,
    val taglist: List<TagBean>?,
    val token: String?,
    val userinfo: Userinfo?,
    val err_type: String?,
    val register: Boolean?,
    val info_check: Boolean?,
    val qn_prefix: List<String>?,
    val extra_data: ExtraBean?
)

data class RegisterTooManyBean(val countdown_time: Int = 0)


data class ExtraBean(
    val im_token: String = "",
    val code: Int = 0,
    val msg: String = "",
    val city_name: String = "",
    val people_amount: Int = 0,
    val gender_str: String = "",
    var force_vip: Boolean = false,
    var isvip: Boolean = false,
    val want_steps: Boolean = false,
    val share_btn: String = "",

    var supplement: Int = 0,//补充资料 1 前置 2后置 3 关闭
    var threshold: Boolean = false,//门槛开关 开启true 关闭false
    var living_btn: Boolean = false,//活体认证的性别判断
    var tourists: Boolean = false//	游客模式 开启true 关闭false
)


data class Userinfo(
    val nickname: String? = "",
    val avatar: String? = "",
    val gender: Int = 0,
    val birth: Int = 0,
    val accid: String = "",
    val allvisit: Int = 0,
    val face_audit_state: Int? = 0,
    val isvip: Boolean = false,
    val isplatinum: Boolean = false,
    val isdirectvip: Boolean = false,
    val todayvisit: Int = 0,
    val vip_express: String = "",
    val platinum_vip_express: String = "",
    var isfaced: Int = -1,//   0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
    var contact_way: Int = 0,//  联系方式  0  没有 1 电话 2微信 3 qq
    var mv_faced: Int = 0,//      0 没有视频/拒绝   1视频通过  2视频审核中
    var identification: Int = 0,// int（认证分数）
    var percent_complete: Int = 0,// float（百分比例如 80.99）
    var intention: LabelQualityBean? = null, //意向
    var my_candy_amount: Int = 0,
    var my_candy_amount_str: String = ""
)

data class TagBean(
    var id: Int = 0,
    var title: String = "",
    var cheked: Boolean = false,
    var is_expire: Boolean = false
) : Serializable


data class VersionBean(val version: String)

/**
 * 账户信息
 */
data class AccountBean(
    var phone: String = "",
    var wechat: String = ""
)

data class WechatNameBean(
    var nickname: String = ""
)


/**
 * 注销原因
 */
data class loginOffCauseBean(
    var descr: String = "",
    var list: MutableList<String> = mutableListOf()
)

/**
 * 我邀请他人注册
 */
data class MyInviteBean(
    var girl_cnt: Int = 0,//	女孩数目
    var invite_url: String = "",//我的邀请连接
    var invite_title: String = "",//我的邀请标题
    var invite_descr: String = "",//我的邀请描述
    var invite_pic: String = "",//我的邀请描述
    var invited_cnt: Int = 0,//已经邀请了几个
    var invited_list: MutableList<InvitedBean> = mutableListOf(),//	已经邀请了的人列表
    var share_normal_cnt: Int = 0,//标准邀请几位人
    var viplist: MutableList<ViplistBean> = mutableListOf(),//	轮动成为会员的数据
    var wait_invite_cnt: Int = 0//在邀请几个
)

data class InvitedBean(
    var avatar: String = "",
    var nickname: String = ""
)

data class ViplistBean(
    var avatar: String = "",
    var nickname: String = ""
)

/**
 * 手机区号bean
 */
data class CountryCodeBean(
    var code: Int = 0,
    var en: String = "",
    var locale: String = "",
    var pinyin: String = "",
    var sc: String = "",
    var tc: String = "",
    var index: String = ""
)