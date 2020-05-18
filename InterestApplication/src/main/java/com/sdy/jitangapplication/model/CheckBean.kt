package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2020/4/2715:58
 *    desc   :
 *    version: 1.0
 */
data class CheckBean(
    var normalIcon: Int = -1,
    var checkedIcon: Int = -1,
    var title: String = "",
    var checked: Boolean = false,
    var icon: String = "",
    var id: Int = -1
)

data class RelationshipBean(val title: String, val relationships: MutableList<CheckBean>)


data class MyTapsBean(
    var icon: String = "",
    var id: Int = 0,
    var steps: Int = 0,
    var use_cnt: Int = 0,
    var title: String = "",
    var checked: Boolean = false,
    var child: MutableList<MyTapsBean>
)