package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/6/219:19
 *    desc   :
 *    version: 1.0
 */

//可以设计一个字段来包含父级和子级之间的关系 比如A A1 A11当取消选中A时 就删除所有包含A的标签（当然一级标签不能删除 最好是以名字来连接）
data class Label(
    var name: String,
    var level: Int = 1,
    var checked: Boolean,
    var parId: Int = -1,
    var subId: Int = -1,
    var subSubId: Int = -1
)