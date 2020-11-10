package com.sdy.jitangapplication.ui.adapter

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.dialog.ConfirmPayCandyDialog
import kotlinx.android.synthetic.main.item_power_pt_vip.view.*
import java.math.BigDecimal

/**
 *    author : ZFM
 *    date   : 2020/5/2910:26
 *    desc   :
 *    version: 1.0
 */
class AllVipPowerAdapter :
    BaseQuickAdapter<VipPowerBean, BaseViewHolder>(R.layout.item_power_pt_vip) {
    var threshold_btn: Boolean = false //门槛开关
    var source_type: Int = -1
    override fun convert(helper: BaseViewHolder, data: VipPowerBean) {
        setPriceData(helper, data)
    }


    fun setPriceData(helper: BaseViewHolder, data: VipPowerBean) {
        var promotePos = -1
        for (tdata in data.list.withIndex()) {
            if (tdata.value.is_promote) {
                promotePos = tdata.index
            }
        }
        if (promotePos == -1) promotePos = 0
        helper.itemView.openVipBtn.text = "¥${if (data.list[promotePos].type == 1) {
            BigDecimal(data.list[promotePos].original_price).setScale(
                0,
                BigDecimal.ROUND_HALF_UP
            )
        } else {
            BigDecimal(data.list[promotePos].discount_price).setScale(
                0,
                BigDecimal.ROUND_HALF_UP
            )
        }} ${if (data.isplatinum) {
            mContext.getString(R.string.vip_renew)
        } else {
            mContext.getString(R.string.vip_buy)
        }}${if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
            mContext.getString(R.string.vip_gold)
        } else {
            mContext.getString(R.string.vip_connection_card)
        }}"

        val chargePriceAdapter by lazy { VipChargeAdapter(data.type) }
        val chargeManager = LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
        helper.itemView.vipPriceRv.layoutManager = chargeManager
        helper.itemView.vipPriceRv.adapter = chargePriceAdapter
        chargePriceAdapter.setNewData(data.list)
        chargePriceAdapter.setOnItemClickListener { _, view, position ->
            for (data in chargePriceAdapter.data) {
                data.is_promote = data == chargePriceAdapter.data[position]
            }
            helper.itemView.openVipBtn.text = "¥${if (chargePriceAdapter.data[position].type == 1) {
                BigDecimal(chargePriceAdapter.data[position].original_price).setScale(
                    0,
                    BigDecimal.ROUND_HALF_UP
                )
            } else {
                BigDecimal(chargePriceAdapter.data[position].discount_price).setScale(
                    0,
                    BigDecimal.ROUND_HALF_UP
                )
            }} ${if (data.isplatinum) {
                mContext.getString(R.string.vip_renew)
            } else {
                mContext.getString(R.string.vip_buy)
            }}${if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
                mContext.getString(R.string.vip_gold)
            } else {
                mContext.getString(R.string.vip_connection_card)
            }}"
            chargePriceAdapter.notifyDataSetChanged()
        }


        helper.itemView.openVipBtn.clickWithTrigger {
            var position: ChargeWayBean? = null
            for (data in chargePriceAdapter.data) {
                if (data.is_promote) {
                    position = data
                    break
                }
            }
            if (position != null)
                ConfirmPayCandyDialog(mContext, position, data.payway, source_type).show()
        }


        val vipPowerAdapter = VipPowerAdapter(data.type)
        val manager = GridLayoutManager(mContext, 2, RecyclerView.VERTICAL, false)
        helper.itemView.vipPowerRv.layoutManager = manager
        helper.itemView.vipPowerRv.adapter = vipPowerAdapter
        vipPowerAdapter.setNewData(data.icon_list)

        when (data.type) {
            VipPowerBean.TYPE_PT_VIP -> {
                helper.itemView.openVipBtn.setBackgroundResource(R.drawable.gradient_pt_vip_24dp)
                helper.itemView.vipLogo.setImageResource(R.drawable.icon_logo_pt_vip)
            }
            VipPowerBean.TYPE_GOLD_VIP -> {
                helper.itemView.vipLogo.setImageResource(R.drawable.icon_logo_gold_vip)
                helper.itemView.openVipBtn.setBackgroundResource(R.drawable.gradient_light_orange_24dp)
            }
        }

    }

}