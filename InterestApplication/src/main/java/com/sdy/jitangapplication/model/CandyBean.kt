package com.sdy.jitangapplication.model

import java.io.Serializable

/**
 *    author : ZFM
 *    date   : 2020/3/2710:02
 *    desc   :
 *    version: 1.0
 */

data class GoodsListBean(
    var banner: MutableList<MutableList<BannerProductBean>> = mutableListOf(),
    var list: MutableList<NewLabel> = mutableListOf(),
    var myinfo: Myinfo = Myinfo(),
    var mycandy: Int = 0

)

data class ProductTitleBean(
    var descr: String = "",
    var id: Int = 0,
    var title: String = ""
)

data class BannerProductBean(
    var descr: String = "",
    var icon: String = "",
    var id: Int = 0,
    var title: String = ""
)


data class Myinfo(
    var yue: Int = 0
)

data class GoodsCategoryBeans(
    var list: MutableList<ProductBean> = mutableListOf()
)

data class ProductBean(
    var amount: Int = 0,
    var descr: String = "",
    var icon: String = "",
    var id: Int = 0,
    var is_recommend: Boolean = false,
    var is_wished: Boolean = false,
    var title: String = "",
    var friend_wish_cnt: Int = 0,
    var wish_cnt: Int = 0
)

/**
 * 商品详情对象
 */
data class ProductDetailBean(
    var amount: Int = 0,
    var comments_cnt: Int = 0,
    var cover_list: MutableList<String> = mutableListOf(),
    var descr: String = "",
    var id: Int = 0,
    var is_recommend: Boolean = false,
    var is_wished: Boolean = false,
    var msg_cnt: Int = 0,
    var my_candy_amount: Int = 0,
    var price: Int = 0,
    var title: String = "",
    var visit_cnt: Int = 0,
    var wish_cnt: Int = 0
)

/**
 * 商品想要对象
 */
data class WantFriendBean(
    var accid: String = "",
    var avatar: String = "",
    var isfriend: Boolean = false,
    var nickname: String = ""
)


/**
 * 商品留言对象
 */
data class ProductMsgBean(
    var accid: String = "",
    var avatar: String = "",
    var content: String = "",
    var create_time: String = "",
    var nickname: String = ""
)

/**
 * 商品评价对象
 */
data class ProductCommentBean(
    var accid: String = "",
    var avatar: String = "",
    var comments: String = "",
    var create_time: String = "",
    var goods_id: Int = 0,
    var nickname: String = "",
    var pic: MutableList<String> = mutableListOf(),
    var stars: Int = 0
)


data class MyAddressBean(
    var list: MutableList<AddressBean> = mutableListOf(),
    var max_cnt: Int = 0
)

/**
 * 收货地址对象
 */
data class AddressBean(
    var area_name: String = "",
    var city_name: String = "",
    var full_address: String = "",
    var member_id: Int = 0,
    var id: Int = 0,
    var nickname: String = "",
    var phone: String = "",
    var postcode: String = "",
    var province_name: String = "",
    var checked: Boolean = false
) : Serializable

data class MyOrderBean(
    var address: String = "",
    var amount: Int = 0,
    var fastmail_company: String = "",
    var fastmail_orders: String = "",
    var icon: String = "",
    var id: Int = 0,
    var state: Int = 0,//状态 1等待发货 2已经退货 3确认收货 4已收货、
    var title: String = ""
)

/**
 * 兑换礼物成功返回对象
 */
data class ExchangeOrderBean(
    var address: String = "",
    var create_time: String = "",
    var goods_amount: Int = 0,
    var goods_icon: String = "",
    var goods_order: String = "",
    var goods_title: String = "",
    var order_remark: String = "",
    var phone: String = "",
    var receiver_name: String = ""
)

/**
 * 交易流水对象
 */
data class BillBean(
    var affect_candy: Int = 0,
    var create_time: String = "",
    var intro: String = "",
    var type_title: String = "",
    var id: Int = 0,
    var info: String = "",
    var icon: String = "",
    var type: Int = 0
)

/**
 * 糖果充值Bean
 */
data class RechargeBean(
    var list: MutableList<RechargeCandyBean> = mutableListOf(),
    var paylist: MutableList<Paylist> = mutableListOf()
)

data class RechargeCandyBean(
    var amount: Int = 0,
    var descr: String = "",
    var discount_price: Int = 0,
    var giving_amount: Int = 0,
    var id: Int = 0,
    var is_promote: Boolean = false,
    var original_price: Int = 0,
    var product_id: String = "",
    var title: String = "",
    var checked: Boolean = false
)


/**
 * 拉起提现bean
 */
data class PullWithdrawBean(
    var alipay: Alipay? = null,
    var has_unread: Boolean = false,
    var candy_amount: Int = 0,
    var money_amount: Float = 0.0F
)

data class Alipay(
    var ali_account: String = "",
    var nickname: String = "",
    var phone: String = ""
) : Serializable


/**
 * 提现成功bean
 */
data class WithDrawSuccessBean(
    var candy_amount: Int = 0,
    var create_tme: String = "",
    var money_amount: Int = 0,
    var trade_no: String = ""
)