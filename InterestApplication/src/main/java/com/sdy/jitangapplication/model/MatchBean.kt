package com.sdy.jitangapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/7/1910:02
 *    desc   :
 *    version: 1.0
 */
data class MatchListBean(
    var exclude: Array<Int?>,
    var list: MutableList<MatchBean>?,
    var lightningcnt: Int?,
    var isvip: Int = 0,    //是否会员 true是 false不是
    var isfaced: Int = 0,   //0未认证/认证不成功     1认证通过     2认证中
    var countdown: Int = 0,
    var motion: Int = -1, //		1，强制替换 2，引导替换 3，引导添加相册 其他不管
    var perfect_times: Int = 0,    //滑动x次数跳【完善相册】
    var replace_times: Int = 0 //滑动x次数跳【替换头像】

)

/**
 * 匹配用户
 */
data class MatchBean(
    var isvip: Int = 0,    //是否会员 true是 false不是
    var isfaced: Int = 0,  //0未认证/认证不成功     1认证通过     2认证中
    var accid: String = "",
    var age: Int? = 0,
    var avatar: String? = null,
    var distance: String? = null,
    var gender: Int? = 0,
    var id: Int? = 0,
    var isdislike: Int? = 0,
    var isliked: Int? = 0,
    var lightning: Int? = 0,
    var member_level: Int? = 0,
    var nickname: String? = null,
    var photos: MutableList<String>? = null,
    var sign: String? = null,
    var job: String? = null,
    var constellation: String? = null,
    var square: MutableList<Square>? = null,
    var square_count: Int? = 0,
    var tagcount: Int? = 0,
    var birth: Int?,
    var tags: MutableList<Tag>?,
    var jobname: String?,
    var lightningcnt: Int?,
    var countdown: Int = 0,
    var isfriend: Int?,
    var residue: Int = 0

) : Serializable


/**
 * 广场封面
 */
data class Square(
    var id: Int?,
    var cover_url: String?
//    var photo_json: String?,
//    var video_json: String?
) : Serializable


/**
 * 匹配状态
 * status :1.喜欢成功  2.匹配成功
 * residue:剩余滑动次数
 */
data class StatusBean(val status: Int, val residue: Int = 0)

/**
 * 用戶標簽
 */
data class Tag(
    var icon: String = "",
    var id: Int = 0,
    var title: String = "",
    var sameLabel: Boolean = false
) : Serializable

//九宫格相册
data class BlockListBean(
    var list: MutableList<Photos>? //	1 图片 2视频
)

/**
 * 用户照片
 */
data class Photos(
    val square_id: Int = 0,
    var url: String?
)


/**
 * 获取打招呼次数和好友关系的model
 */
data class GreetBean(
    val isfriend: Boolean = false,//是否好友
    val isgreet: Boolean = false,//是否打过招呼
    val lightningcnt: Int = -1,//剩余招呼次数
    var countdown: Int = 0

)