package com.example.demoapplication.model

/**
 *    author : ZFM
 *    date   : 2019/7/3017:02
 *    desc   :
 *    version: 1.0
 */

data class ChargeWayBeans(
    val icon_list: MutableList<VipDescr>? = mutableListOf(),
    val list: MutableList<ChargeWayBean>? = mutableListOf(),
    val paylist: MutableList<PaywayBean>? = mutableListOf()
)


data class PaywayBean(
    val comments: String? = "",
    val id: Int? = 0,//支付类型 1支付宝 2微信支付 3余额支付
    val payment_type: Int? = 0
)

data class ChargeWayBean(
    var check: Boolean = false,
    val discount_price: Float? = 0f,
    val duration: Int? = 0,
    val ename: String? = "",
    val id: Int? = 0,
    val limited_price: Float? = 0f,
    val original_price: Float? = 0f,
    val title: String? = "",
    val type: Int?//	1 原价售卖 2折扣价售卖 3限时折扣
)
