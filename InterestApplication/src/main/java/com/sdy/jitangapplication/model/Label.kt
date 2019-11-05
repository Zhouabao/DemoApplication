package com.sdy.jitangapplication.model

import com.contrarywind.interfaces.IPickerViewData
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/6/219:19
 *    desc   :
 *    version: 1.0
 */
//旧的标签对象
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


//-----------------新的标签-----------------
//data class NewLabel(
//    val title: String,
//    val id: Int,
//    val parentId: Int,
//    var checked: Boolean = false
//)

data class NewLabelBean(
    val parent: String,
    val parentId: Int,
    var newLabels: MutableList<NewLabel> = mutableListOf(),
    var checked: Boolean = false
)


data class NewLabel(
    var icon: String = "",
    var id: Int = -1,
    var level: Int = 1,
    var parent_id: Int = -1,
    var son: MutableList<NewLabel> = mutableListOf(),
    var title: String = "",
    var checked: Boolean = false

)


//旧的标签对象
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
