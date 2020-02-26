package com.sdy.jitangapplication.model

/**
 *    author : ZFM
 *    date   : 2019/7/3017:02
 *    desc   :
 *    version: 1.0
 */

data class ChargeWayBeans(
    val icon_list: MutableList<VipDescr>? = mutableListOf(),
    val greet_icon_list: MutableList<VipDescr>? = mutableListOf(),
    val greet_list: MutableList<ChargeWayBean>? = mutableListOf(),//招呼次数购买
    val list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val paylist: MutableList<PaywayBean>? = mutableListOf()
)


data class PaywayBean(
    val comments: String = "",
    val id: Int = 0,
    val payment_type: Int = 0//支付类型 1支付宝 2微信支付 3余额支付
)

data class ChargeWayBean(
    var is_promote: Boolean = false,
    val duration: Int? = 0,
    val ename: String? = "",
    val id: Int = 0,
    var discount_price: Float = 0f,
    val limited_price: Float? = 0f,
    var original_price: Float = 0f,
    val title: String? = "",
    val descr: String? = "",//限时折扣文案
    var type: Int?,//	1 原价售卖 2折扣价售卖 3限时折扣
    var save_percent: Int = 0,//节约的百分比
    var unit_price: Double = 0.0//单价(显示)
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


data class Paylist(
    var comments: String = "",
    var id: Int = 0,
    var payment_type: Int = 0
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
