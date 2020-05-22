package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.contrarywind.interfaces.IPickerViewData
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2019/7/3014:31

 *    version: 1.0
 */
data class UserInfoBean(
    val mytags_count: Int = 0,//兴趣个数 *    desc   : 个人中心请求model
    val label_quality: MutableList<LabelQuality> = mutableListOf(),//展示的兴趣
    val userinfo: Userinfo? = null,
    val sign: String? = "",
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
    val greet_status: Boolean = false,//true 开启招呼认证 false关闭招呼认证
    val notify_square_like_state: Boolean = true,//true 开启招呼认证 false关闭招呼认证
    val notify_square_comment_state: Boolean = true//true 开启招呼认证 false关闭招呼认证
)



//vip权益描述广告
data class VipDescr(
    val rule: String? = "",
    val title: String? = "",
    val url: String? = "",
    val icon_vip: String? = "",
    var countdown: Int = 0,
    var id: Int = 0,
    val title_pay: String = ""
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
    var answer_list: MutableList<AnswerBean> = mutableListOf(),
    var avatar: String = "",
    var birth: String = "",
    var constellation: String = "",
    var face_state: Boolean = false,
    var gender: Int = 0,
    var job: String = "",
    var nickname: String = "",
    var photos: MutableList<String> = mutableListOf(),
    var photos_wall: MutableList<MyPhotoBean> = mutableListOf(),
    var qiniu_domain: String = "",
    var score_rule: ScoreRule = ScoreRule(),
    var sign: String = ""
)


data class AnswerBean(
    var child: MutableList<FindTagBean> = mutableListOf(),
    var find_tag: FindTagBean? = null,
    var id: Int = 0,
    var title: String = "",
    var point: Int = 0,
    var descr: String = ""
)

data class FindTagBean(
    var id: Int = -1,
    var title: String = ""
) : IPickerViewData {
    override fun getPickerViewText(): String {
        return title
    }
}

data class ScoreRule(
    var about: Int = 0,
    var base: Int = 0,
    var base_total: Int = 0,
    var me: Int = 0,
    var photo: Int = 0,
    var total: Int = 0
)



/**
 * 照片墙
 */
data class MyPhotoBean(
    var has_face: Int = 0,//1 没有 2 有
    var id: Int = 0,
    var type: Int = PHOTO,
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



data class City(
    var area: MutableList<String> = mutableListOf(),
    var name: String = ""
)

//范本关于我
data class ModelAboutBean(
    val id: Int = 0,
    val title: String = "",
    val content: String = ""
) : Serializable


data class LoginHelpBean(
    val title: String = "",
//    val content: String = ""
    val content: MutableList<String>
) : Serializable

data class HelpBean(
    val qntk: String = "",
//    val content: String = ""
    val list: MutableList<LoginHelpBean>
) : Serializable


data class MoreMatchBean(
    var city_name: String = "",
    var gender_str: String = "",
    var people_amount: Int = 0,
    var avatar: String = "",
    var force_vip: Boolean = false,
    var isvip: Boolean = false
) : Serializable


data class ContactWayBean(
    var contact_way: Int = 0,
    var contact_way_content: String = "",
    var contact_way_hide: Int = 0
)