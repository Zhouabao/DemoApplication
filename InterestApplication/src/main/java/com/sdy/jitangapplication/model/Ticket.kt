package com.sdy.jitangapplication.model

import com.chad.library.adapter.base.entity.MultiItemEntity
import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2020/6/1910:09
 *    desc   :
 *    version: 1.0
 */

data class TicketBean(
    var my_mv_url: Int = 0, //0未上传 1通过 2审核中
    var isplatinum: Boolean = false,
    var gender: Int = 0,
    var mv_free_cnt: Int = 0,
    var my_ticket_sum: Int = 0,
    var platinum_free_cnt: Int = 0,
    var isfaced: Int = 0,
    var ticket: Ticket = Ticket(),
    var list: MutableList<IndexTopBean> = mutableListOf()
)

data class Ticket(
    var amount: Int = 0,
    var descr: String = "",
    var title: String = ""
)


/**
 * 开屏页推荐
 */
data class IndexTopBean(
    var amount: Int = 0,
    var type: Int = 1,
    var accid: String = "",
    var age: Int = 0,
    var avatar: String = "",
    var distance: String = "",
    var gender: Int = 0,
    var juli: Int = 0,
    var nickname: String = "",
    var source_type: Int = 0
) : MultiItemEntity, Serializable {
    override fun getItemType(): Int {
        return type
    }
}


data class IndexListBean(
    var list: MutableList<IndexTopBean> = mutableListOf(),
    var free_show: Boolean = false,//是否免费查看 true 免费 false 不能查看
    var gender: Int = 0,//我的性别
    var isplatinumvip: Boolean = false,//我是否 钻石会员 true 是 false不是
    var mv_url: Boolean = false,//我是否有视频
    var today_exposure_cnt: Int = 0,//总到访
    var today_visit_cnt: Int = 0,//今日来访
    var total_exposure_cnt: Int = 0,//today_exposure_cnt
    var total_visit_cnt: Int = 0//总曝光
) : Serializable

