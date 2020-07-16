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
    val complete_percent_normal: Int,//（标准完整度新的）
    val normal_percent_complete: Int,//（标准完整度）
    val my_like_times: Int,//（我的次数）
    val total_like_times: Int,//  total_like_times（最高次数）
    var mytags: MutableList<Newtag>? = null,
    val intention: LabelQualityBean? = null,
    var myinterest_count: Int = 0,
    var interest_times: Int = 0,
    var is_human: Boolean = false,
    var ranking_level: Int = 1,//ranking_level  int型 1 审核中ing      2    //非真人提示    其他不管
    var my_candy_amount: Int = 0,//我的糖果数量
    var is_full: Boolean = false,//兴趣是否完整
    var iscompleteguide: Boolean = false//是否引导过
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
    var isplatinumvip: Boolean = false,    //是否铂金会员 true是 false不是
    var isfaced: Int = 0,  //0未认证/认证不成功     1认证通过     2认证中
    var accid: String = "",
    var age: Int? = 0,
    var avatar: String? = null,
    var distance: String? = null,
    var online_time: String? = null,
    var gender: Int? = 0,
    var isdisliked: Int? = 0,
    var isliked: Int? = 0,
    var member_level: Int? = 0,
    var nickname: String? = null,
    var contact_way: Int = 0,
    var mv_btn: Boolean = false, //是否有视频
    var mv_url: String = "", //视频URL
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
    var need_notice: Boolean = true,
    var intention_title: String = "",
    var personal_info: MutableList<DetailUserInfoBean> = mutableListOf()
) : Serializable


data class LabelQuality(
    var icon: String = "",
    var icon2: String = "",
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
 * 糖果解锁验证
 */
data class UnlockCheckBean(
    var isnew_friend: Boolean = false,
    var isplatinumvip: Boolean = false,
    var mv_url: String = "",
    var amount: Int = 0
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


/**
 * 用户照片
 */
data class Photos(
    val square_id: Int = 0,
    var url: String?
)


data class DetailUserInfoBean(
    var icon: String = "",
    var title: String = "",
    var content: String = ""
)


/**
 * 附近的人
 */
data class NearBean(
    var list: MutableList<NearPersonBean> = mutableListOf(),
    var today_find: CheckBean? = null,
    var iscompleteguide: Boolean = false,
    var isvip: Boolean = false,    //是否会员 true是 false不是
    var isplatinum: Boolean = false,    //是否会员 true是 false不是
    var isfaced: Int = 0,   //0未认证/认证不成功     1认证通过     2认证中
    var my_mv_url: Boolean = false,
    var is_full: Boolean = false,//兴趣是否完整
    var ranking_level: Int = 1,//ranking_level  int型 1 审核中ing      2    //非真人提示    其他不管
    var today_find_pull: Boolean = false,
    var has_face_url: Boolean = false,  //是否进行过人脸验证，true验证过 false未验证
    val complete_percent: Int,//（我的资料完整度）
    val complete_percent_normal: Int//（标准完整度新的）
)

data class UnlockBean(
    var isnew_friend: Boolean = false,
    var contact_way: Int,
    var contact_content: String
)


data class SendGiftBean(
    var amount: Int = 0,
    var isnew_friend: Boolean = false,
    var order_id: Int = 0
)


data class NearPersonBean(
    var accid: String = "",
    var age: Int = 0,
    var all_c: Int = 0,
    var avatar: String = "",
    var constellation: String = "",
    var contact_way: Int = 0,
    var distance: String = "",
    var face_auditing_state: Int = 0,
    var gender: Int = 0,
    var intention_icon: String = "",
    var intention_title: String = "",
    var isfaced: Int = 0,
    var isvip: Boolean = false,
    var mv_faced: Boolean = false,
    var mv_btn: Boolean = false,
    var isplatinumvip: Boolean = false,
    var isfriend: Boolean = false,
    var member_level: Int = 0,
    var nickname: String = "",
    var online_time: String = "",
    var sign: String = "",
    var checked: Boolean = false,
    var want: MutableList<String> = mutableListOf()
)

data class TodayFateBean(
    val today_pull: Boolean = false,
    val gift_amount: Int = 0,
    val mycandy_amount: Int = 0,
    val out_time: String = "",
    val list: MutableList<NearPersonBean> = mutableListOf(),
    var gift_list: MutableList<GiftBean> = mutableListOf()
)



/**
 * 批量打招呼
 */
data class BatchGreetBean(
    var accid: String = "",
    var order_id: String = "",
    var msg: String = ""
)