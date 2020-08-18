package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

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
) : Serializable


data class MyTapsBean(
    var icon: String = "",
    var id: Int = 0,
    var steps: Int = 0,
    var use_cnt: Int = 0,
    var title: String = "",
    var checked: Boolean = false,
    var type: Int = TYPE_MYTAP,
    var child: MutableList<MyTapsBean> = mutableListOf()
) : MultiItemEntity {
    companion object {
        const val TYPE_INVESTIGATION = 0
        const val TYPE_MYTAP = 1
    }

    override fun getItemType(): Int {
        return type
    }
}