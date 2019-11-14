package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.contrarywind.interfaces.IPickerViewData
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/7/3014:31
 *    desc   : 个人中心请求model
 *    version: 1.0
 */
data class UserInfoBean(
    val squarelist: Squarelist? = Squarelist(),//展示的广场
    val userinfo: Userinfo? = null,
    val hide_distance: Boolean = false,//（true开启隐藏  false  关闭隐藏）
    val hide_book: Boolean = false,//（ true 屏蔽通讯录     false  关闭隐藏通讯录）
    val greet_status: Boolean = false,//true 开启招呼认证 false关闭招呼认证
    val free_show: Boolean = false,//  true（显示）  false(模糊)
    val vip_descr: MutableList<VipDescr>? = mutableListOf(),//会员权益描述
    val visitlist: MutableList<String>? = mutableListOf()//看过我的头像列表

)

//设置中心开关
data class SettingsBean(
    val hide_distance: Boolean = false,//（true开启隐藏  false  关闭隐藏）
    val hide_book: Boolean = false,//（ true 屏蔽通讯录     false  关闭隐藏通讯录）
    val greet_switch: Boolean = false,//true 开启招呼 false关闭招呼
    val greet_status: Boolean = false//true 开启招呼认证 false关闭招呼认证
)

//个人中心展示封面
data class Squarelist(
    val count: Int? = 0,
    val list: MutableList<CoverSquare>? = mutableListOf()
)

//动态封面
data class CoverSquare(
    val cover_url: String? = "",
    val id: Int? = 0,
    val type: Int? = 0 //1 图 2视频
)


//vip权益描述广告
data class VipDescr(
    val rule: String? = "",
    val title: String? = "",
    val url: String? = "",
    var countdown: Int = 0,
    val title_pay:  String = ""
)


/**
 * 访客
 */
data class VisitorBean(
    val accid: String? = "",
    val age: Int? = 0,
    val avatar: String? = "",
    val constellation: String? = "",
    val distance: String? = "",
    val gender: Int? = 0,
    val isvip: Int? = 0,
    val nickname: String? = "",
    val visitcount: Int? = 0
)

/**
 * 我评论过的
 */
data class MyCommentBean(
    val avatar: String? = "",
    val content: String? = "",
    val create_time: String? = "",
    val id: Int? = 0,
    val nickname: String? = "",
    val reply_content: String? = "",
    val replyed_nickname: String? = "",
    val square_id: Int? = 0
)


data class MyCommentList(
    val list: MutableList<MyCommentBean>? = mutableListOf()
)


/**
 * 个人中心信息
 */
data class UserInfoSettingBean(
    val qiniu_domain: String? = "",
    val job: String? = "",
    val sign: String? = "",
    val avatar: String? = "",
    val face_state: Boolean = false,
    val photos: MutableList<String>? = mutableListOf(),
    val photos_wall: MutableList<MyPhotoBean?>? = mutableListOf(),
    val nickname: String? = "",
    val gender: Int? = 0,
    val birth: String? = "",
    val constellation: String? = "",
    val height: Int = 0,
    val emotion_state: Int = 0,
    val emotion_list: MutableList<String> = mutableListOf(),
    val hometown: String? = "",
    val present_address: String? = "",
    val personal_job: String? = "",
    val making_friends: Int = 0,
    val personal_school: String? = "",
    val personal_drink: Int = 0,
    val personal_smoke: Int = 0,
    val personal_schedule: Int = 0,
    val making_friends_list: MutableList<String> = mutableListOf(),
    val personal_drink_list: MutableList<String> = mutableListOf(),
    val personal_smoke_list: MutableList<String> = mutableListOf(),
    val personal_schedule_list: MutableList<String> = mutableListOf(),
    val score_rule: ScoreRule? = null

)


data class BaseInfo(
    val height: Int = 0,
    val emotion_list: MutableList<String> = mutableListOf(),
    val emotion_state: String? = "",
    val hometown: String? = "",
    val present_address: String? = "",
    val personal_job: String? = "",
    val making_friends: String? = "",
    val personal_school: String? = "",
    val personal_drink: String? = "",
    val personal_smoke: String? = "",
    val personal_schedule: String? = ""
)


data class ScoreRule(
    var about: Int = 0,
    var base: Int = 0,
    var emotion: Int = 0,
    var height: Int = 0,
    var hometown: Int = 0,
    var making_friends: Int = 0,
    var personal_drink: Int = 0,
    var personal_schedule: Int = 0,
    var personal_job: Int = 0,
    var personal_school: Int = 0,
    var personal_smoke: Int = 0,
    var photo: Int = 0,
    var present_address: Int = 0,
    var total: Int = 0,
    var base_total: Int = 0,
    var percent_complete: Int = 0
)


/**
 * 照片墙
 */
data class MyPhotoBean(
    var has_face: Int = 0,//1 没有 2 有
    var id: Int = 0,
    var type: Int,
    var url: String = "",
    var photoScore: Int = 0
) : MultiItemEntity {
    override fun getItemType(): Int {
        return type
    }

    companion object {
        const val COVER = 1
        const val PHOTO = 2
    }
}


data class SchoolBean(
    var id: Int? = 0,
    var school_title: String? = ""
) : Serializable


data class ProvinceBean(
    var city: MutableList<City> = mutableListOf(),
    var name: String = ""
) : IPickerViewData {
    //这个用来显示在PickerView上面的字符串,PickerView会通过getPickerViewText方法获取字符串显示出来。
    override fun getPickerViewText(): String {
        return name
    }

}

data class City(
    var area: MutableList<String> = mutableListOf(),
    var name: String = ""
)

//范本关于我
data class ModelAboutBean(
    val title: String = "",
    val content: String = ""
) : Serializable


data class LoginHelpBean(
    val title: String = "",
//    val content: String = ""
    val content: MutableList<String>
) : Serializable
