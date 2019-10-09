package com.sdy.jitangapplication.model

import java.io.Serializable

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

data class LabelBean(
    var title: String = "",
    var descr: String = "",
    var icon: String = "",
    var id: Int = -1,
    var level: Int = 0,
    var parent_id: Int = 0,
    var path: String = "",
    var son: MutableList<LabelBean>? = null,
    var video_path: String = "",
    var checked: Boolean = false
) : Serializable


data class Labels(
    var data: MutableList<LabelBean>,
    var version: Int
)


data class NewLabel(
    val title: String,
    val id: Int,
    val parentId: Int,
    var checked: Boolean = false
)

data class NewLabelBean(
    val parent: String,
    val parentId: Int,
    var newLabels: MutableList<NewLabel> = mutableListOf(),
    var checked: Boolean = false

)

