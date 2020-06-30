package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2019/7/3017:02
 *    desc   :
 *    version: 1.0
 */

data class ChargeWayBeans(
    val greet_icon_list: MutableList<VipDescr>? = mutableListOf(),
    val greet_list: MutableList<ChargeWayBean>? = mutableListOf(),//招呼次数购买
    val icon_list: MutableList<VipDescr>? = mutableListOf(),
    val list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val pt_icon_list: MutableList<VipDescr>? = mutableListOf(),
    val pt_list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val paylist: MutableList<PaywayBean>? = mutableListOf(),
    val isvip: Boolean = false,
    val candyCount: Int = 0,
    val vip_express: String = "",
    val isplatinum: Boolean = false,
    val threshold_btn: Boolean = false,
    val platinum_vip_express: String = ""
)

data class VipPowerBean(
    val list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val icon_list: MutableList<VipDescr>? = mutableListOf(),
    val isvip: Boolean = false,
    val vip_express: String = "",
    val paylist: MutableList<PaywayBean>? = mutableListOf(),
    var type: Int = TYPE_NORMAL_VIP
) {
    companion object {
        const val TYPE_NORMAL_VIP = 0
        const val TYPE_PT_VIP = 1
    }
}


data class ChargeWayBean(
    var is_promote: Boolean = false,
    val duration: Int? = 0,
    val ename: String? = "",
    val id: Int = 0,
    var discount_price: Double = 0.0,
    val limited_price: Float? = 0f,
    var original_price: Double = 0.0,
    val title: String? = "",
    val giving_amount: Int = 0,
    val descr: String? = "",//限时折扣文案
    var type: Int?,//	1 原价售卖 2折扣价售卖 3限时折扣
    var unit_price: Double = 0.0,//单价(显示)
    var amount: Int = 0,
    var isfirst: Boolean = false,//是否首充  true  首充   false  常规
    var product_id: String = "",
    var checked: Boolean = false
)

data class LabelChargeWayBean(
    var paylist: MutableList<PaywayBean> = mutableListOf(),
    var duration: Int = 0,
    var icon: String = "",
    var id: Int = 0,
    var ios_product_id: String = "",
    var is_new: Boolean = false,
    var price: String = "0",
    var title: String = ""
)

data class PaywayBean(
    val comments: String = "",
    val id: Int = 0,
    val payment_type: Int = 0//支付类型 1支付宝 2微信支付 3余额支付
)

data class PayBean(
    val order_id: String? = "",
    val otn: String? = "",
    val wechat: Wechat? = Wechat(),
    val reqstr: String


)


data class Wechat(
    val `package`: String? = "",
    val appid: String? = "",
    val noncestr: String? = "",
    val partnerid: String? = "",
    val prepayid: String? = "",
    val sign: String? = "",
    val timestamp: String? = ""
)
