package com.sdy.jitangapplication.model

import android.os.Parcel
import android.os.Parcelable

/**
 *    author : ZFM
 *    date   : 2019/7/3017:02
 *    desc   :
 *    version: 1.0
 */

data class ChargeWayBeans(
    val icon_list: MutableList<VipDescr>? = mutableListOf(),
    val list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val pt_icon_list: MutableList<VipDescr>? = mutableListOf(),
    val direct_icon_list: MutableList<VipDescr>? = mutableListOf(),
    val pt_list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val direct_list: MutableList<ChargeWayBean>? = mutableListOf(),//会员按月购买
    val paylist: MutableList<PaywayBean>? = mutableListOf(),
    val isvip: Boolean = false,
    val mycandy_amount: Int = 0,
    val vip_express: String = "",
    val first_recharge: String = "",
    val threshold_btn: Boolean = false,
    val isdirect: Boolean = false,
    val direct_cnt: Int = 0,
    val same_sex_cnt: Int = 0,
    val direct_vip_express: String = "",
    val isplatinum: Boolean = false,
    val platinum_save_str: String = "",
    val direct_save_str: String = "",
    val platinum_vip_express: String = "",
    val experience_title: String = "",
    val experience_amount: String = "",
    val experience_time: String = ""
)

data class VipPowerBean(
    val icon_list: MutableList<VipDescr>? = mutableListOf(),
    val isplatinum: Boolean = false,
    val platinum_vip_express: String = "",
    val platinum_save_str: String = "",
    var type: Int = TYPE_PT_VIP,
    val list: MutableList<ChargeWayBean> = mutableListOf(),
    val payway: MutableList<PaywayBean> = mutableListOf()

) {
    companion object {
        const val TYPE_PT_VIP = 1
        const val TYPE_GOLD_VIP = 0
    }
}


data class ChargeWayBean(
    var is_promote: Boolean = false,
    val ename: String? = "",
    val id: Int = 0,
    var discount_price: String = "0",
    var original_price: String = "0",
    val title: String? = "",
    val giving_amount: Int = 0,
    val giving_gold_day: String = "",
    val descr: String? = "",//限时折扣文案
    var type: Int?,//	1 原价售卖 2折扣价售卖 3限时折扣
    var unit_price: String = "",//单价(显示)
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
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(comments)
        parcel.writeInt(id)
        parcel.writeInt(payment_type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaywayBean> {
        override fun createFromParcel(parcel: Parcel): PaywayBean {
            return PaywayBean(parcel)
        }

        override fun newArray(size: Int): Array<PaywayBean?> {
            return arrayOfNulls(size)
        }
    }
}

data class PayBean(
    val order_id: String? = "",
    val otn: String? = "",
    val wechat: Wechat? = Wechat(),
    val reqstr: String


)

data class PaypalTokenBean(
    var order_id: String = "",
    var clientoken: String = ""
)

data class GoogleTokenBean(
    var acknowledgementState: Int = 0,
    var consumptionState: Int = 0,
    var developerPayload: String = "",
    var kind: String = "",
    var orderId: String = "",
    var purchaseState: Int = 0,
    var purchaseTimeMillis: String = "",
    var purchaseType: Int = 0,
    var regionCode: String = ""
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
