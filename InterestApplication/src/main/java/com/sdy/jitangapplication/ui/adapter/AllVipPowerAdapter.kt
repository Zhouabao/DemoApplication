package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.android.flexbox.*
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.Constants
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.dialog.ChargeVipDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.fragment_vip_power.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/2910:26
 *    desc   :
 *    version: 1.0
 */
class AllVipPowerAdapter :
    BaseQuickAdapter<VipPowerBean, BaseViewHolder>(R.layout.fragment_vip_power) {
    override fun convert(helper: BaseViewHolder, data: VipPowerBean) {
        val itemview = helper.itemView

        val itemParams = itemview.layoutParams as RecyclerView.LayoutParams
        itemParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(50F)
        if (data.type == VipPowerBean.TYPE_NORMAL_VIP) {
            itemParams.leftMargin = SizeUtils.dp2px(25F)
        } else {
            if (mData.size == 1) {
                itemParams.leftMargin = SizeUtils.dp2px(25F)
            } else {
                itemParams.leftMargin = SizeUtils.dp2px(15F)
                itemParams.rightMargin = SizeUtils.dp2px(25F)
            }
        }
        itemview.layoutParams = itemParams

        val params = (itemview.powerUserBg.layoutParams as ConstraintLayout.LayoutParams)
        params.width = itemParams.width
        params.height = (170 / 325F * itemParams.width).toInt()
        itemview.powerUserBg.layoutParams = params

        when (data.type) {
            VipPowerBean.TYPE_NORMAL_VIP -> {
                itemview.powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_ptvip_10dp)
                itemview.vipTypeLogo.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.vipOutTime.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.vipPowerNickname.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.vipPowerText.setImageResource(R.drawable.icon_vip_power_text)
                itemview.powerUserBg.setImageResource(R.drawable.icon_vip_power_user_bg)
                itemview.vipTypeLogo.text = "黄金会员"
                itemview.vipTypeLogo.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.resources.getDrawable(R.drawable.icon_power_vip_logo),
                    null,
                    null,
                    null
                )
            }

            VipPowerBean.TYPE_PT_VIP -> {
                itemview.powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_vip_10dp)
                itemview.vipTypeLogo.setTextColor(Color.parseColor("#FF5E6473"))
                itemview.vipOutTime.setTextColor(Color.parseColor("#FF5E6473"))
                itemview.vipPowerNickname.setTextColor(Color.parseColor("#FF5E6473"))
                itemview.vipPowerText.setImageResource(R.drawable.icon_pt_vip_power_text)
                itemview.powerUserBg.setImageResource(R.drawable.icon_pt_vip_power_user_bg)
                itemview.vipTypeLogo.text = "钻石会员"
                itemview.vipTypeLogo.setCompoundDrawablesWithIntrinsicBounds(
                    mContext.resources.getDrawable(R.drawable.icon_power_pt_vip_logo),
                    null,
                    null,
                    null
                )
            }

        }
        GlideUtil.loadCircleImg(mContext, UserManager.getAvator(), itemview.vipPowerAvator)
        itemview.vipPowerNickname.text = SPUtils.getInstance(Constants.SPNAME).getString("nickname")

        //支付价格
        val vipChargeAdapter = VipChargeAdapter().apply {
            purchaseType = if (data.type == VipPowerBean.TYPE_NORMAL_VIP) {
                ChargeVipDialog.PURCHASE_RENEW_VIP
            } else {
                ChargeVipDialog.PURCHASE_PT_VIP
            }
        }
        itemview.vipChargeRv.layoutManager =
            LinearLayoutManager(mContext!!, RecyclerView.HORIZONTAL, false)
        itemview.vipChargeRv.adapter = vipChargeAdapter
        vipChargeAdapter.setOnItemClickListener { _, _, position ->
            for (data in vipChargeAdapter.data.withIndex()) {
                data.value.is_promote = data.index == position
            }
            vipChargeAdapter.notifyDataSetChanged()
//            setupPrice()
        }

        val vipPowerAdapter = VipPowerAdapter(data.type)
        val manager = FlexboxLayoutManager(mContext!!, FlexDirection.ROW, FlexWrap.WRAP)
        manager.alignItems = AlignItems.CENTER
        manager.justifyContent = JustifyContent.CENTER
        itemview.vipPowerRv.layoutManager = manager
        itemview.vipPowerRv.adapter = vipPowerAdapter


        if (data.type == VipPowerBean.TYPE_NORMAL_VIP) {
            if (data!!.isvip)
                itemview.vipOutTime.text = "${data!!.vip_express}到期"
            else
                itemview.vipOutTime.text = "立即升级享受更多特权"

            if (!data.list.isNullOrEmpty()) {
                //判断是否有选中推荐的，没有的话就默认选中第一个价格。
                var ispromote = false
                for (charge in data.list) {
                    if (charge.is_promote) {
                        ispromote = true
                        break
                    }
                }
                if (!ispromote && !data.list.isNullOrEmpty()) {
                    data.list[0].is_promote = true
                }
            }

            vipChargeAdapter.setNewData(data.list)
            vipPowerAdapter.setNewData(data.icon_list)

            itemview.vipChargeRv.isVisible = false
            itemview.zhiPayBtn.isVisible = false
            itemview.wechatPayBtn.isVisible = false


        } else {
            if (data!!.isvip)
                itemview.vipOutTime.text = "${data!!.vip_express}到期"
            else
                itemview.vipOutTime.text = "立即升级享受更多特权"

            vipChargeAdapter.setNewData(data.list)
            vipPowerAdapter.setNewData(data.icon_list)
            for (payway in data!!.paylist ?: mutableListOf()) {
                if (payway.payment_type == 1) {
                    itemview.zhiPayBtn.visibility = View.VISIBLE
                } else if (payway.payment_type == 2) {
                    itemview.wechatPayBtn.visibility = View.VISIBLE
                }
            }

            itemview.vipChargeRv.isVisible = true
            itemview.zhiPayBtn.isVisible = true
            itemview.wechatPayBtn.isVisible = true
        }

        helper.addOnClickListener(R.id.zhiPayBtn)
        helper.addOnClickListener(R.id.wechatPayBtn)

    }

}