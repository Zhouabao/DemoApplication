package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 *    author : ZFM
 *    date   : 2019/7/3014:31
 *    desc   : 个人中心请求model
 *    version: 1.0
 */
data class UserInfoBean(
    val squarelist: Squarelist? = Squarelist(),//展示的广场
    val userinfo: Userinfo? = null,
    val hide_distance: Boolean = false,//（true开启隐藏  false  关闭隐藏）
    val hide_book: Boolean = false,//（ true 屏蔽通讯录     false  关闭隐藏通讯录）
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

/**
 * 我评论过的
 */
data class MyCommentBean(
    val avatar: String? = "",
    val content: String? = "",
    val create_time: String? = "",
    val id: Int? = 0,
    val nickname: String? = "",
    val reply_content: String? = "",
    val replyed_nickname: String? = "",
    val square_id: Int? = 0
)


data class MyCommentList(
    val list: MutableList<MyCommentBean>? = mutableListOf()
)


/**
 * 个人中心信息
 */
data class UserInfoSettingBean(
    val avatar: String? = "",
    val birth: String? = "",
    val gender: Int? = 0,
    val job: String? = "",
    val nickname: String? = "",
    val photos: MutableList<String>? = mutableListOf(),
    val qiniu_domain: String? = "",
    val sign: String? = ""
)


data class MyPhotoBean(val type: Int, val url: String) : MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val COVER = 1
        const val PHOTO = 2
    }
}