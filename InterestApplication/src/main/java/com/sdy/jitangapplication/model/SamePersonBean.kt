package com.sdy.jitangapplication.model

data class SamePersonListBean(
    var list: MutableList<SamePersonBean> = mutableListOf(),
    var people_cnt:Int = 0,
    var square_cnt:Int = 0
)

data class SamePersonBean(
    var accid: String = "",
    var age: Int = 0,
    var avatar: String = "",
    var constellation: String = "",
    var cover_url: String = "",
    var distance: String = "",
    var gender: String = "",
    var hb_city: String = "",
    var id: Int = 0,
    var isliked: Int = 0,
    var originalLike: Int = 0,
    var nickname: String = "",
    var tags: String = ""
)