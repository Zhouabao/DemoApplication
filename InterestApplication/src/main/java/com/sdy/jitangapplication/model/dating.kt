package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity

/**
 *    author : ZFM
 *    date   : 2020/8/1017:44
 *    desc   :
 *    version: 1.0
 */


/**
 * 广场兴趣列表
 */
data class DatingBean(
    var accid: String = "",
    var apply_cnt: Int = 0,
    var city_name: String = "",
    var content: String = "",
    var icon: String = "",
    var duration: Int = 0,
    var content_type: Int = 0,
    var cost_money: String = "",
    var cost_type: String = "",
    var dating_distance: String = "",
    var dating_target: String = "",
    var dating_title: String = "",
    var distance: String = "",
    var follow_up: String = "",
    var gender: Int = 0,
    var isplatinumvip: Boolean = false,
    var like_cnt: Int = 0,
    var temp_like_cnt: Int = like_cnt,
    var nickname: String = "",
    var place: String = "",
    var province_name: String = "",
    var title: String = "",
    var avatar: String = "",
    var id: Int = 0,
    var is_hot: Boolean = false,
    var type: Int = TYPE_WOMAN,

    var dating_type: Int = 0,
    var isliked: Boolean = false,
    var online_time: String = "",
    var tempLike: Boolean = isliked,
    var ranking_level: Int = 0,
    var show_type: Boolean = false
) : MultiItemEntity {
    companion object {
        const val TYPE_WOMAN = 1
        const val TYPE_MAN = 2
    }

    override fun getItemType(): Int {
        return if (show_type) {
            TYPE_WOMAN
        } else {
            TYPE_MAN
        }
    }
}

data class DatingOptionsBean(
    var cost_money: MutableList<String> = mutableListOf(),
    var cost_type: MutableList<String> = mutableListOf(),
    var dating_target: MutableList<String> = mutableListOf(),
    var follow_up: MutableList<String> = mutableListOf()
)

data class CheckPublishDatingBean(
    var is_publish: Boolean = false,

    var dating_amount: Int = 0,//报名邀约糖果数
    var isplatinum: Boolean = false,//是否黄金会员 true 是 false 不是
    var private_chat: Boolean = false,//该邀约是否设置黄金会员访问 true设置 false没有设置
    var residue_cnt: Int = 0,//	是否剩余免费次数


    val datingId: Int,
    val content: String,
    val icon: String
)

data class LikeBean(
    var isliked: Boolean = false
)

//id,title,dating_title,icon
data class ApplyDatingBean(
    val id: Int,
    val title: String,
    val dating_title: String,
    val icon: String
)