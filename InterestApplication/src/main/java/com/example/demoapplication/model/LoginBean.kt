package com.example.demoapplication.model

data class LoginBean(
    val accid: String?,
    val phoneCheck: Boolean?,
    val qntk: String?,
    val taglist: List<TagBean>?,
    val token: String?,
    val userinfo: Userinfo?,
    val err_type: String?
)

data class Userinfo(
    val birth: String? = "",
    val gender: Int? = 0,
    val accid: String? = "",
    val allvisit: Int? = 0,
    val avatar: String? = "",
    val face_audit_state: Int? = 0,
    val isvip: Int? = 0,
    val nickname: String? = "",
    val todayvisit: Int? = 0,
    val vip_express: String? = "",
    val isverify: Int? = 0
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

