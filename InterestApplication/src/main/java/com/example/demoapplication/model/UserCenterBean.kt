package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/7/3014:31
 *    desc   : 个人中心请求model
 *    version: 1.0
 */
data class UserInfoBean(
    val squarelist: Squarelist? = Squarelist(),//展示的广场
    val userinfo: Userinfo? = null,
    val vip_descr: MutableList<VipDescr>? = mutableListOf(),//会员权益描述
    val visitlist: MutableList<String>? = mutableListOf()//看过我的头像列表

)

//个人中心展示封面
data class Squarelist(
    val count: Int? = 0,
    val list: MutableList<CoverSquare>? = mutableListOf()
)

//动态封面
data class CoverSquare(
    val cover_url: String? = "",
    val id: Int? = 0,
    val type: Int? = 0 //1 图 2视频
)


//vip权益描述广告
data class VipDescr(
    val rule: String? = "",
    val title: String? = "",
    val url: String? = ""
)


/**
 * 访客
 */
data class VisitorBean(
    val accid: String? = "",
    val age: Int? = 0,
    val avatar: String? = "",
    val constellation: String? = "",
    val distance: String? = "",
    val gender: Int? = 0,
    val isvip: Int? = 0,
    val nickname: String? = "",
    val visitcount: Int? = 0
)
