package com.example.demoapplication.model

data class LoginBean(
    val accid: String?,
    val phoneCheck: Boolean?,
    val qntk: String?,
    val taglist: List<Any>?,
    val token: String?,
    val userinfo: Userinfo?,
    val err_type: String?
)

data class Userinfo(
    val avatar: String,
    val birth: Int,
    val gender: Int,
    val nickname: String
)