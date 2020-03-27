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

//-----------------新的兴趣-----------------
//免费：1已加入 2未加入  付费：3已购买 4付费进入
data class NewLabel(
    var id: Int = -1,
    var android_price: Float = 0F, //android的付费价格
    var icon: String = "",
    var son: MutableList<NewLabel> = mutableListOf(),
    var title: String = "",
    var descr: String = "",
    var checked: Boolean = false,
    var used_cnt: Int = 0,
    var ishot: Boolean = false,
    var ismine: Boolean = false,
    var state: Int = 0//1.无需付费.未添加 2无需付费.已经添加  3.无需付费.已删除 4需要付费.付费进入
    //5.无需要.男性付费 6.无需付费.女性付费  7.需要付费.已过期 8.需要付费.已删除.未过期限
    //9.需要付费.已删除.过期 10.需要付费.已经添加
) : Serializable


data class AddLabelBean(
    var list: MutableList<NewLabel> = mutableListOf(),
    var menu: MutableList<NewLabel> = mutableListOf(),
    var limit_count: Int = 0
)


/**
 * 兴趣特质
 */
data class LabelQualityBean(
    var content: String = "",
    var icon: String = "",
    var id: Int = 0,
    var title: String = "",
    var isfuse: Boolean = false,
    var outtime: Boolean = false//过期标志
) : Serializable, IPickerViewData {
    override fun getPickerViewText(): String {
        return content
    }
}

data class LabelQualitysBean(
    var roll_list: MutableList<LabelQualityBean> = mutableListOf(),
    var list: MutableList<LabelQualityBean> = mutableListOf(),
    var has_button: Boolean = false
)


/**
 * 广场发布兴趣
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
 * 我的兴趣
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
    var same_label: Boolean = false,//是否是相同兴趣
    var same_quality_count: Int = 0,//相同特质数量
    var intro_descr: String = "",
    var publish_descr: String = "",
    var msg: String = "",
    var checked: Boolean = false,
    var isfull: Boolean = false,
    var is_expire: Boolean = false//是否过期  true过期 false未过期
) : Serializable


/**
 * 他人的兴趣
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


data class AddLabelResultBean(
    var is_published: Boolean = false,
    var list: MutableList<TagBean> = mutableListOf()
)


data class ChooseTitleBean(
    val limit_cnt: Int?,
    val list: MutableList<LabelQualityBean>?
)