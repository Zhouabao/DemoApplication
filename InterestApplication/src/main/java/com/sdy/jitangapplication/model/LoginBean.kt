package com.sdy.jitangapplication.model

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
    val extra_data: IMBean?
)

data class IMBean(val im_token: String = "", val code: Int = 0, val msg: String = "")


data class Userinfo(
    val nickname: String? = "",
    val avatar: String? = "",
    val gender: Int? = 0,
    val birth: String? = "",
    val accid: String? = "",
    val allvisit: Int? = 0,
    val face_audit_state: Int? = 0,
    val isvip: Int? = 0,
    val todayvisit: Int? = 0,
    val vip_express: String? = "",
    var isfaced: Int = 0//0未认证/认证不成功     1认证通过     2认证中
)

data class TagBean(
    var id: Int?,
    var title: String?,
    var path: String?
)

data class UserBean(
    var taglist: MutableList<TagBean?>?,
    var userinfo: Userinfo?
)

