package com.sdy.jitangapplication.model

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
    var used_cnt: Int = 0
) : Serializable


data class AddLabelBean(
    var list: MutableList<NewLabel> = mutableListOf(),
    var menu: MutableList<NewLabel> = mutableListOf()
)


/**
 * 标签特质
 */
data class LabelQualityBean(
    var content: String = "",
    var id: Int = 0,
    var title: String = "",
    var checked: Boolean = false,
    var unable: Boolean = false
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return content
    }
}


data class MyLabelsBean(
    var is_using: MutableList<MyLabelBean> = mutableListOf(),
    var is_removed: MutableList<MyLabelBean> = mutableListOf()
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
    var publish_descr: String = ""
) : Serializable


/**
 * 他人的标签
 */
data class OtherLabelsBean(
    var my: MutableList<MyLabelBean> = mutableListOf(),
    var other: MutableList<MyLabelBean> = mutableListOf()
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
