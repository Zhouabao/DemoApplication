package com.sdy.jitangapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/7/1910:02
 *    desc   :
 *    version: 1.0
 */

/**
 * 匹配用户
 */
data class MatchBean(
    var intention_icon: String = "",
    var isvip: Int = 0,    //是否会员 true是 false不是
    var isdirectvip: Boolean = false,    //是否铂金会员 true是 false不是
    var isplatinumvip: Boolean = false,    //是否铂金会员 true是 false不是
    var myisplatinumvip: Boolean = false,    //是否铂金会员 true是 false不是
    var isfaced: Int = 0,  //0未认证/认证不成功     1认证通过     2认证中
    var accid: String = "",
    var age: Int? = 0,
    var avatar: String? = null,
    var distance: String? = null,
    var face_str: String? = null,
    var online_time: String? = null,
    var gender: Int? = 0,
    var nickname: String? = null,
    var contact_way: Int = 0,
    var mv_btn: Boolean = false, //是否有视频
    var photos: MutableList<String>? = null,
    var sign: String? = null,
    var constellation: String? = null,
    var isfriend: Int?,
    var isblock: Int = 1,//1 互相没有拉黑  2 我拉黑了他  3  ta拉黑了我   4 互相拉黑
    var gift_list: MutableList<GiftBean> = mutableListOf(),
    var label_quality: MutableList<LabelQuality> = mutableListOf(),
    var mycandy_amount: Int = 0,
    var need_notice: Boolean = true,
    var intention_title: String = "",
    var personal_info: MutableList<DetailUserInfoBean> = mutableListOf(),
    var birth: Int = 0,
    var mv_url: String = "",
    var mv_faced: Boolean = false
) : Serializable


data class LabelQuality(
    var icon: String = "",
    var icon2: String = "",
    var title: String = ""
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
    var today_pull_share: Boolean = false,
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
    var face_str: String = "",
    var isvip: Boolean = false,
    var mv_faced: Boolean = false,
    var private_chat_state: Boolean = false,
    var mv_btn: Boolean = false,
    var isplatinumvip: Boolean = false,
    var isdirectvip: Boolean = false,
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