package com.sdy.jitangapplication.ui.activity

import android.graphics.Color
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.base.ext.onClick
import com.kotlin.base.ui.activity.BaseMvpActivity
import com.sdy.baselibrary.utils.StatusBarUtil
import com.sdy.jitangapplication.R
import com.sdy.jitangapplication.common.clickWithTrigger
import com.sdy.jitangapplication.model.ChargeWayBeans
import com.sdy.jitangapplication.model.VipPowerBean
import com.sdy.jitangapplication.presenter.VipPowerPresenter
import com.sdy.jitangapplication.presenter.view.VipPowerView
import com.sdy.jitangapplication.ui.adapter.AllVipPowerAdapter
import com.sdy.jitangapplication.ui.dialog.ChargePtVipDialog
import com.sdy.jitangapplication.widgets.CenterLayoutManager
import kotlinx.android.synthetic.main.activity_vip_power1.*
import kotlinx.android.synthetic.main.layout_actionbar.*

class VipPowerActivity() :
    BaseMvpActivity<VipPowerPresenter>(), VipPowerView {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vip_power1)
        initView()
        mPresenter.getChargeData()
    }

    private fun initView() {
        mPresenter = VipPowerPresenter()
        mPresenter.context = this
        mPresenter.mView = this

        StatusBarUtil.immersive(this)
        llTitle.setBackgroundColor(Color.TRANSPARENT)
//        llTitle.setBackgroundColor(Color.parseColor("#FF1D1F21"))
        btnBack.setImageResource(R.drawable.icon_back_white)
        divider.isVisible = false
        hotT1.setTextColor(resources.getColor(R.color.colorWhite))
        hotT1.text = "会员权益"
        btnBack.onClick { finish() }

        initVp2()
    }

    private val adapter by lazy { AllVipPowerAdapter() }
    var slideLeft = false
    var lastPosition = false

    private fun initVp2() {
        payBtn.clickWithTrigger {
            if (lastPosition && slideLeft) {
                ChargePtVipDialog(
                    ChargePtVipDialog.VIP_LOGO,
                    this@VipPowerActivity,
                    ChargePtVipDialog.PURCHASE_CONTACT_CARD
                ).show()
            } else {
                ChargePtVipDialog(
                    ChargePtVipDialog.VIP_LOGO,
                    this@VipPowerActivity,
                    ChargePtVipDialog.PURCHASE_PT_VIP
                ).show()
            }
        }

        vpPower.layoutManager = CenterLayoutManager(this, RecyclerView.HORIZONTAL, false)
        LinearSnapHelper().attachToRecyclerView(vpPower)
        vpPower.adapter = adapter
        vpPower.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val last = (vpPower.layoutManager as CenterLayoutManager).findLastCompletelyVisibleItemPosition()
                lastPosition = last == (vpPower.layoutManager as CenterLayoutManager).itemCount - 1
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (last == (vpPower.layoutManager as CenterLayoutManager).itemCount - 1 && slideLeft) {
                        if (chargeWayBeans?.isdirect == true)
                            payBtn.text = "续费至尊直联卡"
                        else
                            payBtn.text = "获取至尊直联卡"
                        payBtn.setTextColor(Color.parseColor("#FFFFD27A"))
                        payBtn.setBackgroundResource(R.drawable.gradient_dark_black_0dp)

                    } else {
                        if (chargeWayBeans?.isplatinum == true)
                            payBtn.text = "续费高级会员"
                        else
                            payBtn.text = "成为高级会员"
                        payBtn.setTextColor(Color.parseColor("#ff1d1f21"))
                        payBtn.setBackgroundResource(R.drawable.gradient_light_orange_0dp)
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                slideLeft = dx > 0
            }
        })


//        itemview.payBtn.text = "获取至尊直联卡"
//        itemview.payBtn.setTextColor(Color.parseColor("#FFFFD27A"))
//        itemview.payBtn.setBackgroundResource(R.drawable.gradient_dark_black_0dp)

//
//        itemview.payBtn.text = "成为高级会员"
//        itemview.payBtn.setTextColor(Color.parseColor("#ff1d1f21"))
//        itemview.payBtn.setBackgroundResource(R.drawable.gradient_light_orange_0dp)

    }


    private var chargeWayBeans: ChargeWayBeans? = null

    override fun getChargeDataResult(data: ChargeWayBeans?) {
        chargeWayBeans = data
        if (data != null) {
            adapter.threshold_btn = data?.threshold_btn ?: false
            adapter.addData(
                VipPowerBean(
                    data.pt_icon_list,
                    data.isplatinum,
                    data.platinum_vip_express,
                    data.platinum_save_str,
                    VipPowerBean.TYPE_PT_VIP
                )
            )
            adapter.addData(
                VipPowerBean(
                    null,
                    data.isdirect,
                    data.direct_vip_express,
                    "每日免费解锁${data.direct_cnt}次联系方式",
                    VipPowerBean.TYPE_CONTACT_CARD
                )
            )
        }
    }


}
