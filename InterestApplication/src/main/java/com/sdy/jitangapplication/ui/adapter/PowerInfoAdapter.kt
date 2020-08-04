package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.ChargeWayBean
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.ui.dialog.ConfirmPayCandyDialog
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_power_info.view.*
import java.math.BigDecimal

/**
 *    author : ZFM
 *    date   : 2020/5/2910:26
 *    desc   :
 *    version: 1.0
 */
class PowerInfoAdapter : BaseQuickAdapter<VipPowerBean, BaseViewHolder>(R.layout.item_power_info) {
    var threshold_btn: Boolean = false //门槛开关
    override fun convert(helper: BaseViewHolder, data: VipPowerBean) {
        val itemview = helper.itemView

        val itemParams = itemview.layoutParams as RecyclerView.LayoutParams
        itemParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(60F)
        if (data.type == VipPowerBean.TYPE_GOLD_VIP) {
            itemParams.leftMargin = SizeUtils.dp2px(30F)
        } else {
//            if (mData.size == 1) {
//                itemParams.leftMargin = SizeUtils.dp2px(30F)
//            } else {
            itemParams.leftMargin = SizeUtils.dp2px(10F)
            itemParams.rightMargin = SizeUtils.dp2px(30F)
//            }
        }
        itemview.layoutParams = itemParams

        val params = (itemview.powerUserBg.layoutParams as ConstraintLayout.LayoutParams)
        params.width = itemParams.width
        params.height = (160 / 315F * itemParams.width).toInt()
        itemview.powerUserBg.layoutParams = params

        helper.addOnClickListener(R.id.payBtn)
        itemview.vipSaveAmount.text = data.platinum_save_str
        when (data.type) {
            VipPowerBean.TYPE_GOLD_VIP -> {
                itemview.vipPowerNickname.text =
                    "黄金会员（${(data.icon_list ?: mutableListOf()).size}项特权）"
                itemview.powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_ptvip_10dp)
                itemview.vipOutTime.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.vipSaveAmount.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.vipPowerNickname.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.powerUserBg.setImageResource(R.drawable.icon_pt_vip_power_user_bg)
                if (data!!.isplatinum)
                    itemview.vipOutTime.text = "${data!!.platinum_vip_express}到期"
                else
                    itemview.vipOutTime.text = "暂未激活特权"
                itemview.vipPowerAvator.borderColor = Color.parseColor("#FFEBA35A")

            }

            VipPowerBean.TYPE_PT_VIP -> {
                itemview.vipPowerNickname.text = "钻石会员"
                itemview.powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_vip_10dp)
                itemview.vipOutTime.setTextColor(Color.parseColor("#FF5E6473"))
                itemview.vipPowerNickname.setTextColor(Color.parseColor("#FF5E6473"))
                itemview.powerUserBg.setImageResource(R.drawable.icon_power_contact_card)
                itemview.vipSaveAmount.setTextColor(Color.parseColor("#FF5E6473"))

                if (data!!.isplatinum)
                    itemview.vipOutTime.text = "${data!!.platinum_vip_express}到期"
                else
                    itemview.vipOutTime.text = "暂未激活特权"

                itemview.vipPowerAvator.borderColor = Color.parseColor("#FF565259")
            }

        }
        GlideUtil.loadCircleImg(mContext, UserManager.getAvator(), itemview.vipPowerAvator)


    }


}