package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.contrarywind.interfaces.IPickerViewData

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
    var icon: String = "",
    var id: Int = 0,
    var is_hot: Boolean = false,
    var is_join: Boolean = false,
    var cover_list: MutableList<SquarePicBean> = mutableListOf(),
    var place_type: Int = 0,//位置类型 0 没有操作 1置顶 2置底
    var title: String = "",
    var type: Int = TYPE_WOMAN
) : MultiItemEntity {
    companion object {
        const val TYPE_WOMAN = 1
        const val TYPE_MAN = 2
    }

    override fun getItemType(): Int {
        return type
    }
}


data class DatingConditionBean(
    var title: String = "",
    var param: String = "",
    var son: MutableList<DatingConditionBean> = mutableListOf()
) : IPickerViewData {
    override fun getPickerViewText(): String {
        return title
    }
}


data class DatingOptionsBean(
    var cost_money: MutableList<String> = mutableListOf(),
    var cost_type: MutableList<String> = mutableListOf(),
    var dating_target: MutableList<String> = mutableListOf(),
    var follow_up: MutableList<String> = mutableListOf()
)