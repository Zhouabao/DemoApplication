package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/6/2418:17
 *    desc   :  sex：1男2女
 *    version: 1.0
 */


data class SquareListBean(
    var list: MutableList<SquareBean>?,
    var banner_title: MutableList<TopicBean>? = mutableListOf(),//标题
    var friend_list: MutableList<FriendBean>?,
    var myinterest_count: Int = 0
)

/**
 * 广场列表数据
 */
data class SquareBean(

    var isvip: Int = 0,//是否会员 1是 0 不是
    var accid: String = "",
    var audio_json: MutableList<VideoJson>?,
    var avatar: String = "",
    var city_name: String = "",
    var comment_cnt: Int = 0,
    var create_time: String = "",
    var descr: String? = "",
    var id: Int?,
    var isliked: Int = 0,
    var iscollected: Int?,//0没收藏 1收藏
    var like_cnt: Int = 0,
    var member_level: Int?,
    var nickname: String?,
    var out_time: String?,
    var puber_address: String?,
    var photo_json: MutableList<VideoJson>?,
    var province_name: String?,
    var share_cnt: Int?,
    var tag_id: Int?,
    var title: String?,
    var cover_url: String?,
    var tags: String?,
    var video_json: MutableList<VideoJson>?,
    var isfriend: Boolean = true,
    var greet_switch: Boolean = true,//接收招呼开关   true  接收招呼      false   不接受招呼
    var greet_state: Boolean = true,// 认证招呼开关   true  开启认证      flase   不开启认证
    var icon: String = "",
    var isPlayAudio: Int = 0, //0未播放  1 播放中 2暂停  3 停止
    var comment: String = "",
    val distance: String = "",
    var link_url: String?,
    var type: Int = 1,
    var category_type: Int = 1,
    var duration: Long = 0L,
    var clickTime: Int = 0,
    var originalLike: Int = 0,
    var originalLikeCount: Int = 0,
    var isgreeted: Boolean = true,//招呼是否仍然有效

    var member_id: Int? = null
) :
    Serializable, MultiItemEntity {


    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val PIC = 1
        const val VIDEO = 2
        const val AUDIO = 3
        const val OFFICIAL_NOTICE = 4
    }
}


data class TopicBean(
    var icon: String = "",
    var id: Int = 0,
    var son: MutableList<SquarePicBean> = mutableListOf(),
    var tag_id: Int = 0,
    var tag_title: String = "",
    var title: String = "",
    var used_cnt: Int = 0
) : Serializable

data class SquarePicBean(
    var cover_url: String = "",
    var square_id: Int = 0
) : Serializable


data class VideoJson(
    val duration: Int = 0,
    val url: String = "",
    var width: Float = 0f,
    var height: Float = 0f,
    var leftTime: Int = 0//倒计时剩下的时间
) : Serializable


/**
 * 广场列表好友数据
 */
data class FriendBean(
    var square_id: Int?,
    var accid: String?,
    var avatar: String?,
    var id: Int?,
    var nickname: String?
)




data class SquareRecentlyListBean(
    var list: MutableList<SquareBean?>?
)

data class AllCommentBean(
    var hotlist: MutableList<CommentBean?>?,
    var list: MutableList<CommentBean?>?
)


/**
 * 评论数据
 */
data class CommentBean(
    var avatar: String? = null,
    var content: String? = null,
    var create_time: String? = null,
    var id: Int? = 0,
    var isliked: Int? = 0,
    var like_count: Int? = 0,
    var member_accid: String? = null,
    var reply_content: String? = null,
    var reply_count: Int? = 0,
    var reply_id: Int? = 0,
    var replyed_nickname: String? = null,
    var nickname: String? = null,
    var square_id: Int? = 0,
    var type: Int = 1 //1数据  0标题
) : MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val TITLE = 0
        const val CONTENT = 1
    }


}

data class SquareTitleBean(val title: String, var checked: Boolean)
