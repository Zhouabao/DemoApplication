package com.example.demoapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/7/1910:02
 *    desc   :
 *    version: 1.0
 */
data class MatchListBean(
    var list: MutableList<MatchBean1>?,
    var lightningcnt: Int?
)


data class MatchBean(
    var name: String,
    var age: Int,
    var sex: Int,
    var imgs: MutableList<String>,
    var type: Int = 1,
    var zan: Boolean = false
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


data class MatchBean1(
    var accid: String? = null,
    var age: Int? = 0,
    var avatar: String? = null,
    var distance: String? = null,
    var gender: Int? = 0,
    var id: Int? = 0,
    var isdislike: Int? = 0,
    var isliked: Int? = 0,
    var isvip: Int? = 0,
    var lightning: Int? = 0,
    var member_level: Int? = 0,
    var nickname: String? = null,
    var photos: MutableList<String>? = null,
    var sign: String? = null,
    var job: String? = null,
    var constellation: String? = null,
    var square: MutableList<Square>? = null,
    var square_count: Int? = 0,
    var tagcount: Int? = 0
) : Serializable


data class Square(
    var id: Int?,
    var photo_json: String?,
    var video_json: String?
)



data class MatchUserDetailBean(
    var accid: String?,
    var avatar: String?,
    var birth: Int?,
    var constellation: String?,
    var home_cover: String?,
    var isvip: Int?,
    var login_date_cnt: Int?,
    var nickname: String?,
    var photos: MutableList<String>?,
    var sign: String?,
    var square: MutableList<Square>?,
    var tags: MutableList<Tag>?
)

data class Tag(
    var icon: String?,
    var id: Int?,
    var title: String?
)
