package com.sdy.jitangapplication.model

import java.io.Serializable

data class LoginBean(
    val accid: String?,
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

data class ExtraBean(
    val im_token: String = "",
    val code: Int = 0,
    val msg: String = "",
    val mytaglist: MutableList<TagBean>,
    val myinterest: Boolean = false,
    val aboutme: String = "",
    val city_name: String = "",
    val people_amount: Int = 0,
    val gender_str: String = "",
    var force_vip: Boolean = false,
    var isvip: Boolean = false,
    var threshold_state: Boolean = false,
    val want_steps: Boolean = false
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
    val todayvisit: Int = 0,
    val vip_express: String = "",
    val platinum_vip_express: String = "",
    var isfaced: Int = -1,//   0 未认证 1通过 2机审中 3人审中 4被拒（弹框）
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

