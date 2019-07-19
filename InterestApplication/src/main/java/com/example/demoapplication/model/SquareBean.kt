package com.example.demoapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/6/2418:17
 *    desc   :  sex：1男2女
 *    version: 1.0
 */


data class SquareListBean(
    var `data`: MutableList<SquareBean>,
    var current_page: String?,
    var last_page: Int?,
    var per_page: Int?,
    var total: Int?
)

/**
 * 广场列表数据
 */
data class SquareBean(
    var isPlayAudio: Int = 0, //0未播放  1 播放中 2暂停  3 停止
    var isvip: Int?,//是否会员 1是 0 不是
    var icon: String?,
    var accid: String?,
    var audio_json: MutableList<String>?,
    var avatar: String?,
    var city_name: String?,
    var comment_cnt: Int?,
    var comment: String? = null,
    var create_time: String?,
    var descr: String?,
    var id: Int?,
    var isliked: Int?,
    var iscollected: Int?,//0没收藏 1收藏
    var like_cnt: Int?,
    var member_level: Int?,
    var nickname: String?,
    var out_time: String?,
    var photo_json: MutableList<String>?,
    var province_name: String?,
    var share_cnt: Int?,
    var tag_id: Int?,
    var title: String?,
    var video_json: MutableList<String>?,
    var type: Int = 1,
    var duration: Long = 0L
) :
    Serializable, MultiItemEntity {


    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val PIC = 1
        const val VIDEO = 2
        const val AUDIO = 3
    }
}

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


data class FriendListBean(
    var list: MutableList<FriendBean?>?
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
    var id: Int?=0,
    var isliked: Int?=0,
    var like_count: Int?=0,
    var member_accid: String? = null,
    var reply_content: String? = null,
    var reply_count: Int?=0,
    var reply_id: Int?=0,
    var replyed_nickname: String? = null,
    var nickname: String? = null,
    var square_id: Int?=0,
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