package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.contrarywind.interfaces.IPickerViewData
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/6/219:19
 *    desc   :
 *    version: 1.0
 */
data class Labels(
    var data: MutableList<NewLabel>,
    var version: Int
)


//-----------------新的标签-----------------
data class NewLabel(
    var icon: String = "",
    var id: Int = -1,
    var level: Int = 1,
    var parent_id: Int = -1,
    var son: MutableList<NewLabel> = mutableListOf(),
    var title: String = "",
    var checked: Boolean = false,
    var removed: Boolean = false,
    var intro_descr: String = "",
    var publish_descr: String = "",
    var used_cnt: Int = 0,
    var state: Int = 0//0 没有使用过 1正在使用中 2使用过的
) : Serializable


data class AddLabelBean(
    var list: MutableList<NewLabel> = mutableListOf(),
    var menu: MutableList<NewLabel> = mutableListOf(),
    var limit_count: Int = 0
)


/**
 * 标签特质
 */
data class LabelQualityBean(
    var content: String = "",
    var icon: String = "",
    var id: Int = 0,
    var title: String = "",
    var isfuse: Boolean = false
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return content
    }
}


/**
 * 广场发布标签
 */
data class SquareLabelsBean(
    var all_list: MutableList<SquareLabelBean> = mutableListOf(),
    var used_list: MutableList<SquareLabelBean> = mutableListOf()
)


data class SquareLabelBean(
    var cnt: Int = 0,
    var cover_url: String = "",
    var icon: String = "",
    var id: Int = 0,
    var title: String = "",
    var type: Int = 0,
    var checked: Boolean = false

) : Serializable, MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val TITLE = 0
        const val CONTENT = 1
    }
}


data class MyLabelsBean(
    var is_using: MutableList<MyLabelBean> = mutableListOf(),
    var is_removed: MutableList<MyLabelBean> = mutableListOf(),
    var limit_count: Int = 0
)


/**
 * 我的标签
 */

data class MyLabelBean(
    var describle: String = "",
    var icon: String = "",
    var id: Int = 0,
    var tag_id: Int = 0,
    var intention: MutableList<LabelQualityBean> = mutableListOf(),
    var label_quality: MutableList<LabelQualityBean> = mutableListOf(),
    var title: String = "",
    var editMode: Boolean = false,//是否处于编辑模式
    var same_label: Boolean = false,//是否是相同标签
    var same_quality_count: Int = 0,//相同特质数量
    var intro_descr: String = "",
    var publish_descr: String = "",
    var msg: String = "",
    var checked: Boolean = false,
    var animated: Boolean = false
):Serializable


/**
 * 他人的标签
 */
data class OtherLabelsBean(
    var my: MutableList<MyLabelBean> = mutableListOf(),
    var other_tags: MutableList<MyLabelBean> = mutableListOf(),
    var other_interest: MutableList<LabelQualityBean> = mutableListOf(),
    var my_interest: MutableList<LabelQualityBean> = mutableListOf()
)

//职业
data class NewJobBean(
    var title: String = "",
    var descr: String = "",
    var icon: String = "",
    var id: Int = -1,
    var level: Int = 0,
    var parent_id: Int = 0,
    var path: String = "",
    var son: MutableList<NewJobBean>? = null,
    var video_path: String = "",
    var checked: Boolean = false
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return title
    }
}


data class AddLabelResultBean(var is_published: Boolean = false, var list: MutableList<TagBean> = mutableListOf())
