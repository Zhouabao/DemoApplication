package com.sdy.jitangapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/7/1910:02
 *    desc   :
 *    version: 1.0
 */
data class MatchListBean(
    var exclude: MutableList<Int>?,
    var list: MutableList<MatchBean>?,
    var lightningcnt: Int?,
    var isvip: Int = 0,    //是否会员 true是 false不是
    var isfaced: Int = 0,   //0未认证/认证不成功     1认证通过     2认证中
    var countdown: Int = 0,
    var motion: Int = -1, //		1，强制替换 2，引导替换 3，引导添加相册 其他不管
    var perfect_times: Int = 0,    //滑动x次数跳【完善相册】
    var replace_times: Int = 0, //滑动x次数跳【替换头像】
    var like_times: Int = 0, //剩余滑动次数
    var highlight_times: Int = 0,//提示剩余次数的节点
    val my_percent_complete: Int,//（我的资料完整度）
    val normal_percent_complete: Int,//（标准完整度）
    val my_like_times: Int,//（我的次数）
    val total_like_times: Int,//  total_like_times（最高次数）
    var mytags: MutableList<Newtag>? = null,
    val intention: LabelQualityBean? = null,
    var myinterest_count: Int = 0,
    var interest_times: Int = 0,
    var is_human: Boolean = false,
    var ranking_level: Int = 1,//ranking_level  int型 1 审核中ing      2    //非真人提示    其他不管
    var is_full: Boolean = false,//兴趣是否完整
    var iscompleteguide: Boolean = false//是否引导过
)


data class FindByTagBean(
    var countdown: Int = 0,
    var is_full: Boolean = false,
    var is_human: Boolean = false,
    var isfaced: Int = 0,
    var isjoin: Boolean = false,
    var isvip: Int = 0,
    var lightningcnt: Int = 0,
    var tag_icon: String = "",
    var list: MutableList<MatchBean>?,
    var motion: Int = 0,
    var mytags: MutableList<Newtag>? = null,
    var tag_info: MyLabelBean? = null
)

data class AddSinlgLabelBean(
    var tag_list: MutableList<TagBean>,
    var tag_info: MyLabelBean? = null
)

/**
 * 匹配用户
 */
data class MatchBean(
    var matching_content: String = "",
    var matching_icon: String = "",
    var face_auditing_state: Int = 0,
    var intention_icon: String = "",
    var isvip: Int = 0,    //是否会员 true是 false不是
    var isfaced: Int = 0,  //0未认证/认证不成功     1认证通过     2认证中
    var accid: String = "",
    var age: Int? = 0,
    var avatar: String? = null,
    var distance: String? = null,
    var gender: Int? = 0,
    var isdislike: Int? = 0,
    var isliked: Int? = 0,
    var member_level: Int? = 0,
    var nickname: String? = null,
    var photos: MutableList<String>? = null,
    var sign: String? = null,
    var job: String? = null,
    var constellation: String? = null,
    var square: MutableList<Square>? = null,
    var square_cnt: Int? = 0,
    var tags: MutableList<TagBean>?,
    var lightningcnt: Int?,
    var countdown: Int = 0,
    var isfriend: Int?,
    var residue: Int = 0,
    var isblock: Int = 1,//1 互相没有拉黑  2 我拉黑了他  3  ta拉黑了我   4 互相拉黑
    var greet_switch: Boolean = true,//接收招呼开关   true  接收招呼      false   不接受招呼
    var greet_state: Boolean = true,// 认证招呼开关   true  开启认证      flase   不开启认证
    var isgreeted: Boolean = true,//招呼是否仍然有效
    val base_info: BaseInfo,
    val my_percent_complete: Int,//（我的资料完整度）
    val normal_percent_complete: Int,//（标准完整度）
    val my_like_times: Int,//（我的次数）
    val total_like_times: Int,//  total_like_times（最高次数）
    var interesttags: MutableList<TagBean>? = null,
    var newtags: MutableList<Newtag>? = null,
    var gift_list: MutableList<GiftBean> = mutableListOf(),
    var wish_list: MutableList<GiftBean> = mutableListOf(),
    var wish_cnt: Int = 0,
    var birth: Int = 0,
    var label_quality: MutableList<LabelQuality> = mutableListOf(),
    var jobname: String = "",
    var label_quality_cnt: Int = 0,
    var mycandy_amount: Int = 0,
    var need_notice: Boolean = true
) : Serializable


data class LabelQuality(
    var icon: String = "",
    var title: String = ""
) : Serializable


data class Newtag(
    var id: Int = 0,
    var label_quality: MutableList<String> = mutableListOf(),
    var title: String = ""
)

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
 * 获取招呼的次数
 */
data class GreetTimesBean(
    val normal_cnt: Int,
    val isfaced: Int = 0,
    val isvip: Int = 0,
    val default_msg: String = ""
)

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
    var countdown: Int = 0,
    var free_greet: Boolean = false//true 免费   false 付费冲会员


)


data class DetailUserInfoBean(
    var icon: Int,
    var title: String,
    var content: String
)