package com.sdy.jitangapplication.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SizeUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.sdy.baselibrary.glide.GlideUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.utils.UserManager
import kotlinx.android.synthetic.main.item_power_pt_vip.view.*

/**
 *    author : ZFM
 *    date   : 2020/5/2910:26
 *    desc   :
 *    version: 1.0
 */
class AllVipPowerAdapter :
    BaseQuickAdapter<VipPowerBean, BaseViewHolder>(R.layout.item_power_pt_vip) {
    var threshold_btn: Boolean = false //门槛开关
    override fun convert(helper: BaseViewHolder, data: VipPowerBean) {
        val itemview = helper.itemView

        val itemParams = itemview.layoutParams as RecyclerView.LayoutParams
        itemParams.width = ScreenUtils.getScreenWidth() - SizeUtils.dp2px(60F)
        if (data.type == VipPowerBean.TYPE_PT_VIP) {
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
        params.height = (170 / 325F * itemParams.width).toInt()
        itemview.powerUserBg.layoutParams = params

        helper.addOnClickListener(R.id.payBtn)
        itemview.vipSaveAmount.text = data.platinum_save_str
        when (data.type) {
            VipPowerBean.TYPE_CONTACT_CARD -> {
                itemview.vipPowerRv.isVisible = false
                itemview.contactCl.isVisible = true
                itemview.seemoreBtn.isVisible = false
                itemview.vipPowerNickname.text = "至尊直联卡"
                itemview.powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_vip_10dp)
                itemview.vipOutTime.setTextColor(Color.parseColor("#FFFFD27A"))
                itemview.vipPowerNickname.setTextColor(Color.parseColor("#FFFFD27A"))
                itemview.powerUserBg.setImageResource(R.drawable.icon_power_contact_card)

                if (data!!.isplatinum)
                    itemview.vipOutTime.text = "${data!!.platinum_vip_express}到期"
                else
                    itemview.vipOutTime.text = "暂未激活特权"

            }
            VipPowerBean.TYPE_PT_VIP -> {
                itemview.vipPowerNickname.text =
                    "高级会员（${(data.icon_list ?: mutableListOf()).size}项特权）"
                itemview.powerUserBgExtend.setBackgroundResource(R.drawable.rectangle_left_ptvip_10dp)
                itemview.vipOutTime.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.vipPowerNickname.setTextColor(Color.parseColor("#ffcd7e14"))
                itemview.powerUserBg.setImageResource(R.drawable.icon_pt_vip_power_user_bg)
                itemview.vipPowerRv.isVisible = true
                itemview.contactCl.isVisible = false
                itemview.seemoreBtn.isVisible = true
                itemview.rvVipPower.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                    val childAt = v.getChildAt(0)
                    val childHeight = childAt.height
                    val myHeight = v.height
                    if (scrollY + myHeight < childHeight) {
                        itemview.seemoreBtn.visibility = View.VISIBLE
                    } else {
                        itemview.seemoreBtn.visibility = View.INVISIBLE
                    }
                    Log.d(
                        "rvVipPower",
                        "====childHeight=${childHeight},====scrollY=${scrollY},====myHeight=${myHeight}"
                    )
                }


                val vipPowerAdapter = VipPowerAdapter(data.type)
                val manager = GridLayoutManager(mContext!!, 2, RecyclerView.VERTICAL, false)
                itemview.vipPowerRv.layoutManager = manager
                itemview.vipPowerRv.adapter = vipPowerAdapter
                if (data!!.isplatinum)
                    itemview.vipOutTime.text = "${data!!.platinum_vip_express}到期"
                else
                    itemview.vipOutTime.text = "暂未激活特权"

                vipPowerAdapter.setNewData(data.icon_list)
            }
        }
        GlideUtil.loadCircleImg(mContext, UserManager.getAvator(), itemview.vipPowerAvator)


    }

}